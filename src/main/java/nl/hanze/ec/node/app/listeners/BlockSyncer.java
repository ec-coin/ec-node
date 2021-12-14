package nl.hanze.ec.node.app.listeners;

import com.google.inject.Inject;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.network.peers.PeerPool;
import nl.hanze.ec.node.network.peers.commands.WaitForResponse;
import nl.hanze.ec.node.network.peers.commands.requests.InventoryRequest;
import nl.hanze.ec.node.network.peers.commands.requests.Request;
import nl.hanze.ec.node.network.peers.commands.responses.InventoryResponse;
import nl.hanze.ec.node.network.peers.commands.responses.Response;
import nl.hanze.ec.node.network.peers.peer.Peer;
import nl.hanze.ec.node.network.peers.peer.PeerState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class BlockSyncer extends StateListener {
    private final List<NodeState> listenFor = new ArrayList<>() {
        {
            add(NodeState.SYNCING);
        }
    };

    private Peer syncingPeer = null;
    private List<String> blockHashesToBeFetched;
    private int blockHeight = -1;

    private final BlockRepository blockRepository;

    public BlockSyncer(
            @NodeStateQueue BlockingQueue<NodeState> nodeStateQueue,
            PeerPool peerPool,
            BlockRepository blockRepository
    ) {
        super(nodeStateQueue, peerPool);
        this.blockRepository = blockRepository;
        this.blockHashesToBeFetched = new ArrayList<>();
    }

    protected void doWork() {
        // todo: determine if syncing peer went offline
        if (syncingPeer == null) { //|| syncingPeer.getState() == PeerState.CLOSED) {
            blockHashesToBeFetched = new ArrayList<>();
            determineSyncingPeer();
        }

        canContinue();

        if (blockHeight == -1) {
            blockHeight = blockRepository.getCurrentBlockHeight();
        }

        canContinue();

        if (blockHashesToBeFetched.isEmpty()) {
            String hash = blockRepository.getCurrentBlockHash(blockHeight);

            WaitForResponse command = new WaitForResponse(new InventoryRequest(new ArrayList<>() {{ add(hash); }}));

            command.await();

            InventoryResponse response = ((InventoryResponse) command.getResponse());

            blockHashesToBeFetched = response.getBlockHashes();
        }

        canContinue();

        for (String hash : blockHashesToBeFetched) {

        }

        // nodeStateQueue.add(NodeState.PARTICIPATING);
    }

    protected void beforeSleep() {
        this.blockHashesToBeFetched = new ArrayList<>();
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
