package nl.hanze.ec.node.app.listeners;

import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.database.repositories.TransactionRepository;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.network.peers.PeerPool;
import nl.hanze.ec.node.network.peers.commands.WaitForResponse;
import nl.hanze.ec.node.network.peers.commands.requests.TransactionsRequest;
import nl.hanze.ec.node.network.peers.commands.requests.HeadersRequest;
import nl.hanze.ec.node.network.peers.commands.responses.HeadersResponse;
import nl.hanze.ec.node.network.peers.commands.responses.TransactionsResponse;
import nl.hanze.ec.node.network.peers.peer.Peer;
import nl.hanze.ec.node.network.peers.peer.PeerState;
import nl.hanze.ec.node.utils.HashingUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class BlockSyncer extends StateListener {
    private final List<NodeState> listenFor = new ArrayList<>() {{
        add(NodeState.SYNCING);
    }};

    private Peer syncingPeer = null;
    private int localBlockHeight;

    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;

    public BlockSyncer(
            @NodeStateQueue BlockingQueue<NodeState> nodeStateQueue,
            PeerPool peerPool,
            BlockRepository blockRepository,
            TransactionRepository transactionRepository
    ) {
        super(nodeStateQueue, peerPool);
        this.blockRepository = blockRepository;
        this.transactionRepository = transactionRepository;
        localBlockHeight = blockRepository.getCurrentBlockHeight();
    }

    protected void iteration() {
        // TODO: only download blocks up until a certain threshold (to prevent downloading blocks that are incorrect)
        // Historic data / simulator data (use number of unverified txs?) [block sync window]
        while (localBlockHeight < syncingPeer.getStartHeight()) {
            String hash = blockRepository.getCurrentBlockHash(localBlockHeight);
            Block block = blockRepository.getBlock(localBlockHeight);

            WaitForResponse command = new WaitForResponse(new HeadersRequest(hash));
            peerPool.sendCommand(syncingPeer, command);
            command.await();

            if (command.getResponse() == null) {
                System.out.println("Dead syncing node, choosing a new one");
                determineSyncingPeer();
                continue;
            }

            HeadersResponse response = ((HeadersResponse) command.getResponse());

            List<HeadersResponse.Header> headers = response.getHeaders();

            // TODO: Update syncing peer start height.

            HeadersResponse.Header prevHeader = new HeadersResponse.Header(
                    block.getHash(),
                    block.getPreviousBlockHash(),
                    block.getMerkleRootHash(),
                    block.getBlockHeight()
            );

            for(HeadersResponse.Header header : headers) {
                // Validate previous hash.
                if (!prevHeader.hash.equals(header.previousBlockHash) ||
                        prevHeader.blockHeight+1 != header.blockHeight) {
                    System.out.println("INVALID HEADER FOUND [prev:" + prevHeader + "] [curr:" + header + "]");
                    break;
                }

                // Validate hash.
                String blockHash = HashingUtils.hash(header.previousBlockHash + header.merkleRootHash);
                if (!blockHash.equals(header.hash)) {
                    System.out.println("INVALID HASH FOUND [prev:" + prevHeader + "] [curr:" + header + "]");
                    break;
                }

                // Validate block height
                if (header.blockHeight != (localBlockHeight + 1)) {
                    System.out.println("INVALID BLOCK_HEIGHT FOUND [prev:" + prevHeader + "] [curr:" + header + "]");
                }

                // TODO: use timestamp from syncing node?
                blockRepository.createHeader(
                        header.hash,
                        header.previousBlockHash,
                        header.merkleRootHash,
                        header.blockHeight
                );

                localBlockHeight++;
                prevHeader = header;
            }

            waitIfStateIncorrect();
        }

        List<Block> blocks = blockRepository.getAllBlocksOfParticularType("header");

        for (Block block : blocks) {
            WaitForResponse command = new WaitForResponse(new TransactionsRequest(block.getHash()));
            peerPool.sendCommand(syncingPeer, command);
            command.await();

            if (command.getResponse() == null) {
                determineSyncingPeer();
                continue;
            }

            TransactionsResponse response = ((TransactionsResponse) command.getResponse());

            List<TransactionsResponse.Tx> transactions = response.getTransactions();

            // TODO: Validate merkle root of transactions with merkle root of block in DB.

            for(TransactionsResponse.Tx transaction : transactions) {
                // TODO: use timestamp from syncing node?
                transactionRepository.createTransaction(
                        transaction.hash,
                        block,
                        transaction.from,
                        transaction.to,
                        transaction.amount,
                        transaction.signature,
                        transaction.addressType,
                        transaction.publicKey
                );
            }

            block.setType("full");
            blockRepository.update(block);

            waitIfStateIncorrect();
        }

        localBlockHeight = blockRepository.getCurrentBlockHeight();
        blocks = blockRepository.getAllBlocksOfParticularType("header");
        if (localBlockHeight == syncingPeer.getStartHeight() && blocks.size() == 0) {
            nodeStateQueue.add(NodeState.PARTICIPATING);
        }
    }

    @Override
    protected void beforeWakeup() {
        if (syncingPeer == null || syncingPeer.getState() == PeerState.CLOSED) {
            determineSyncingPeer();
        }
    }

    /**
     * Determines which peer to sync with.
     * (Choose peer w/ highest block start height)
     */
    private void determineSyncingPeer() {
        Set<Peer> peers = peerPool.getConnectedPeers();
        int maxStartHeight = 0;
        for (Peer peer : peers) {
            if (peer.getStartHeight() > maxStartHeight && peer.getState() == PeerState.ESTABLISHED) {
                syncingPeer = peer;
                maxStartHeight = peer.getStartHeight();
            }
        }
    }

    public List<NodeState> listenFor() {
        return listenFor;
    }
}
