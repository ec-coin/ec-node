package nl.hanze.ec.node.network.peers.commands.responses;

import nl.hanze.ec.node.app.workers.WorkerFactory;
import nl.hanze.ec.node.network.peers.commands.AbstractCommand;
import nl.hanze.ec.node.app.workers.NeighborResponseWorker;
import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.services.CollectionMappingService;
import org.json.JSONObject;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class NeighborsResponse extends AbstractCommand implements Response {
    private List<String> ips;
    private int responseTo;

    public NeighborsResponse(List<String> ips, int responseTo) {
        this.ips = ips;
        this.responseTo = responseTo;
    }

    public NeighborsResponse(JSONObject payload, WorkerFactory workerFactory) {
        super(payload, workerFactory);
        this.ips = CollectionMappingService.mapToStringList(payload.getJSONArray("ips").toList());
        this.responseTo = payload.getInt("responseTo");
    }

    @Override
    protected JSONObject getData(JSONObject payload) {
        payload.put("ips", this.ips);
        payload.put("responseTo", this.responseTo);

        return payload;
    }

    public List<String> getIps() {
        return ips;
    }

    @Override
    public String getCommandName() {
        return "neighbors-response";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return workerFactory.create(NeighborResponseWorker.class, receivedCommand, peerCommandQueue);
    }

    @Override
    public Integer inResponseTo() {
        return this.responseTo;
    }
}
