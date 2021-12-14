package nl.hanze.ec.node.app.listeners;

import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.network.peers.PeerPool;
import nl.hanze.ec.node.network.peers.commands.WaitForResponse;
import nl.hanze.ec.node.network.peers.commands.requests.BlocksRequest;
import nl.hanze.ec.node.network.peers.commands.requests.HeadersRequest;
import nl.hanze.ec.node.network.peers.commands.responses.HeadersResponse;
import nl.hanze.ec.node.network.peers.peer.Peer;
import nl.hanze.ec.node.network.peers.peer.PeerState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class BlockSyncer extends StateListener {
    private final List<NodeState> listenFor = new ArrayList<>() {{
        add(NodeState.SYNCING);
    }};

    private Peer syncingPeer = null;
    private List<String> hashChain;
    private int localBlockHeight = -1;

    private final BlockRepository blockRepository;

    public BlockSyncer(
            @NodeStateQueue BlockingQueue<NodeState> nodeStateQueue,
            PeerPool peerPool,
            BlockRepository blockRepository
    ) {
        super(nodeStateQueue, peerPool);
        this.blockRepository = blockRepository;
        this.hashChain = new ArrayList<>();
    }

    protected void iteration() {
        // Determine a sync node
        if (syncingPeer == null || syncingPeer.getState() == PeerState.CLOSED) {
            // hashChain = new ArrayList<>();
            determineSyncingPeer();
        }

        waitIfStateIncorrect();

        // Determine the starting block height
        if (localBlockHeight == -1) {
            localBlockHeight = blockRepository.getCurrentBlockHeight();
        }

        waitIfStateIncorrect();

        // TODO: get header chain (not in memory, takes to much mem)
        while (hashChain.size() < syncingPeer.getStartHeight() ) {
            String hash = blockRepository.getCurrentBlockHash(localBlockHeight);

            WaitForResponse command = new WaitForResponse(new HeadersRequest(new ArrayList<>() {{ add(hash); }}));
            command.await();

            if (command.getResponse() != null) {
                HeadersResponse response = ((HeadersResponse) command.getResponse());

                List<HeadersResponse.Header> headers = response.getHeaders();

                // Update syncing peer start height.

                // Validate hashes.

//                for(Object hashObj : hashes) {
//                    try {
//                        hashChain.add((String) hashObj);
//                    } catch (ClassCastException ignore) {}
//                }
            }
        }

        waitIfStateIncorrect();

        // TODO: retrieve all block data from header chain
        Iterator<String> it = hashChain.iterator();
        while (it.hasNext()) {
            String hash = it.next();

            WaitForResponse command = new WaitForResponse(new BlocksRequest(new ArrayList<>() {{ add(hash); }}));
            command.await();

            if (command.getResponse() != null) {
                // BlockResponse response = ((BlockResponse) command.getResponse());

                // Validate transactions in a block

                // Save in database

                localBlockHeight++;
                it.remove();
            }

            waitIfStateIncorrect();
        }

        if (syncingPeer.getStartHeight() == localBlockHeight) {
            nodeStateQueue.add(NodeState.PARTICIPATING);
        }
    }

    protected void beforeSleep() {
        // this.blockHashesToBeFetched = new ArrayList<>();
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
