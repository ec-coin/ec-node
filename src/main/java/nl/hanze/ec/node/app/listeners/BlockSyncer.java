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
        // Determine a sync node
        if (syncingPeer == null || syncingPeer.getState() == PeerState.CLOSED) {
            determineSyncingPeer();
        }

        waitIfStateIncorrect();

        while (localBlockHeight < syncingPeer.getStartHeight()) {
            String hash = blockRepository.getCurrentBlockHash(localBlockHeight);
            Block block = blockRepository.getBlock(localBlockHeight);

            WaitForResponse command = new WaitForResponse(new HeadersRequest(hash));
            peerPool.sendCommand(syncingPeer, command);
            command.await();

            if (command.getResponse() == null) {
                // TODO: fix new syncingPeer
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
                // Validate header.
                if (!prevHeader.hash.equals(header.previousBlockHash) ||
                        prevHeader.blockHeight+1 != header.blockHeight) {
                    // TODO: INVALID BLOCK choose other node to sync with
                    System.out.println("INVALID HEADER FOUND");
                    System.out.println("prev" + prevHeader);
                    System.out.println("curr" + header);
                    break;
                }

                // TODO: use timestamp from syncing node?
                blockRepository.createBlock(
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

        // TODO: only retrieve blocks that miss transactions
        List<Block> blocks = blockRepository.getAllBlocks();

        for (Block block : blocks) {
            WaitForResponse command = new WaitForResponse(new TransactionsRequest(block.getHash()));
            command.await();

            if (command.getResponse() == null) {
                // TODO: fix new syncingPeer
                continue;
            }

            TransactionsResponse response = ((TransactionsResponse) command.getResponse());

            List<TransactionsResponse.Tx> transactions = response.getTransactions();

            // TODO: Validate merkle root of transactions with merkle root of block in DB.

            for(TransactionsResponse.Tx transaction : transactions) {
                // TODO: Validate transaction.
                // (1) signature
                // (2) valid amount

                // TODO: use timestamp from syncing node?
                transactionRepository.createTransaction(
                        transaction.hash,
                        block,
                        transaction.from,
                        transaction.to,
                        transaction.amount,
                        transaction.signature,
                        transaction.addressType
                );
            }

            waitIfStateIncorrect();
        }

        // TODO: if all blocks & transactions are received & verified go to PARTICIPATING
//        if (syncingPeer.getStartHeight() == localBlockHeight) {
//            nodeStateQueue.add(NodeState.PARTICIPATING);
//        }
    }

    /**
     * Determines which peer to sync with.
     * (Choose peer w/ highest block start height)
     */
    private void determineSyncingPeer() {
        Set<Peer> peers = peerPool.getConnectedPeers();
        int maxStartHeight = 0;
        for (Peer peer : peers) {
            if (peer.getStartHeight() > maxStartHeight) {
                syncingPeer = peer;
                maxStartHeight = peer.getStartHeight();
            }
        }
    }

    public List<NodeState> listenFor() {
        return listenFor;
    }
}
