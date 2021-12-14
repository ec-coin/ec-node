package nl.hanze.ec.node.app.workers;

import com.google.gson.Gson;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.network.peers.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class NewBlockAnnouncementWorker extends Worker {
    public NewBlockAnnouncementWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        super(receivedCommand, peerCommandQueue);
    }

    @Override
    public void run() {
        System.out.println("New block received");
        JSONObject payload = receivedCommand.getPayload();
        Object blockObject = payload.get("block");
        Block block = fromJSONToObject(blockObject);

        // If this was a request a response could be sent like this.
        // peerCommandQueue.add(new TestResponse());
    }

    private Block fromJSONToObject(@NotNull Object block) {
        Gson gson= new Gson();
        return gson.fromJson(block.toString(), Block.class);
    }
}
