package nl.hanze.ec.node.network.peers.commands.responses;

import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.app.workers.WorkerFactory;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.network.peers.commands.AbstractCommand;
import nl.hanze.ec.node.network.peers.commands.Command;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class HeadersResponse extends AbstractCommand implements Response {
    public static class Header {
        public final String hash;
        public final String previousBlockHash;
        public final String merkleRootHash;
        public final int blockHeight;
        public final DateTime timestamp;

        public Header(String hash, String previousBlockHash, String merkleRootHash, int blockHeight, DateTime timestamp) {
            this.hash = hash;
            this.previousBlockHash = previousBlockHash;
            this.merkleRootHash = merkleRootHash;
            this.blockHeight = blockHeight;
            this.timestamp = timestamp;
        }

        public Map<String, Object> toMap() {
            return new HashMap<>() {{
                put("hash", hash);
                put("previous_block_hash", previousBlockHash);
                put("merkle_root_hash", merkleRootHash);
                put("block_height", blockHeight);
                put("timestamp", timestamp.toString());
            }};
        }

        @Override
        public String toString() {
            return "Header{" +
                    "hash='" + hash + '\'' + ", previousBlockHash='" + previousBlockHash + '\'' +
                    ", merkleRootHash='" + merkleRootHash + '\'' + ", blockHeight=" + blockHeight +
                    ", timestamp=" + timestamp + '}';
        }
    }

    List<Header> headers;
    int responseTo;

    public HeadersResponse(Collection<Block> headers, int responseTo) {
        this.headers = headers.stream()
                .map(header -> new Header(
                        header.getHash(),
                        header.getPreviousBlockHash(),
                        header.getMerkleRootHash(),
                        header.getBlockHeight(),
                        header.getTimestamp()
                )).collect(Collectors.toList());

        this.responseTo = responseTo;
    }

    public HeadersResponse(JSONObject payload, WorkerFactory workerFactory) {
        super(payload, workerFactory);

        this.responseTo = payload.getInt("responseTo");

        List<Object> jArray = payload.getJSONArray("headers").toList();
        this.headers = new ArrayList<>();
        for (Object obj : jArray) {
            if (obj instanceof HashMap) {
                HashMap<?, ?> header = (HashMap<?, ?>) obj;

                if (header.get("hash") instanceof String &&
                        header.get("previous_block_hash") instanceof String &&
                        header.get("merkle_root_hash") instanceof String &&
                        header.get("block_height") instanceof Integer &&
                        header.get("timestamp") instanceof String) {
                    int blockHeight = (Integer) header.get("block_height");

                    this.headers.add(new Header(
                            header.get("hash").toString(),
                            header.get("previous_block_hash").toString(),
                            header.get("merkle_root_hash").toString(),
                            blockHeight,
                            DateTime.parse(header.get("timestamp").toString())
                    ));
                }
            }
        }
    }

    @Override
    protected JSONObject getData(JSONObject payload) {
        payload.put("headers", this.headers.stream().map(Header::toMap).collect(Collectors.toList()));
        payload.put("responseTo", this.responseTo);

        return payload;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    @Override
    public String getCommandName() {
        return "headers-response";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return null;
    }

    @Override
    public Integer inResponseTo() {
        return this.responseTo;
    }
}
