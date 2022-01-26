package nl.hanze.ec.node.app.listeners;

import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.database.repositories.TransactionRepository;
import nl.hanze.ec.node.exceptions.InvalidTransaction;
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
import nl.hanze.ec.node.utils.SignatureUtils;
import nl.hanze.ec.node.utils.ValidationUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class BlockSyncer extends StateListener {
    private static final Logger logger = LogManager.getLogger(BlockSyncer.class);
    public static final int blockSyncWindow = 10;

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
        while (localBlockHeight < syncingPeer.getStartHeight()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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

            HeadersResponse.Header prevHeader = new HeadersResponse.Header(
                    block.getHash(),
                    block.getPreviousBlockHash(),
                    block.getMerkleRootHash(),
                    block.getBlockHeight(),
                    block.getTimestamp()
            );

            for(HeadersResponse.Header header : headers) {
                // Validate previous hash.
                if (!prevHeader.hash.equals(header.previousBlockHash) ||
                        prevHeader.blockHeight+1 != header.blockHeight) {
                    System.out.println("INVALID HEADER FOUND [prev:" + prevHeader + "] [curr:" + header + "]");
                    break;
                }

                // Validate hash.
                String blockHash = HashingUtils.generateBlockHash(header.merkleRootHash, header.previousBlockHash, header.timestamp);
                if (!blockHash.equals(header.hash)) {
                    System.out.println("INVALID HASH FOUND [curr:" + header + "]");
                    break;
                }

                // Validate block height
                if (header.blockHeight != (localBlockHeight + 1)) {
                    System.out.println("INVALID BLOCK_HEIGHT FOUND [prev:" + prevHeader + "] [curr:" + header + "]");
                    break;
                }

                blockRepository.createHeader(
                        header.hash,
                        header.previousBlockHash,
                        header.merkleRootHash,
                        header.blockHeight,
                        header.timestamp
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
            List<String> transactionHashes = new ArrayList<>();

            for(TransactionsResponse.Tx transaction : transactions) {
                try {
                    ValidationUtils.validateTransaction(new Transaction(
                            transaction.hash, block, transaction.from, transaction.to,
                            transaction.amount, transaction.signature, transaction.status,
                            transaction.addressType, transaction.publicKey, transaction.timestamp)
                    );
                }
                catch (InvalidTransaction e) {
                    logger.info(e.getMessage());
                }
                transactionHashes.add(transaction.hash);
            }

            if (!HashingUtils.validateMerkleRootHash(block.getMerkleRootHash(), transactionHashes)) {
                logger.info("Merkle Root Hash not valid [curr:" + transactionHashes + "]");
                continue;
            }

            for(TransactionsResponse.Tx transaction : transactions) {
                transactionRepository.createTransaction(
                        transaction.hash,
                        block,
                        transaction.from,
                        transaction.to,
                        transaction.amount,
                        transaction.signature,
                        transaction.status,
                        transaction.addressType,
                        transaction.publicKey,
                        transaction.timestamp
                );
            }

            block.setType("block");
            blockRepository.update(block);

            waitIfStateIncorrect();
        }

        localBlockHeight = blockRepository.getCurrentBlockHeight();
        blocks = blockRepository.getAllBlocksOfParticularType("header");
        if (localBlockHeight == syncingPeer.getStartHeight() && blocks.size() == 0) {
            logger.info("Blockchain is now in sync, transitioning to PARTICIPATING");
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
