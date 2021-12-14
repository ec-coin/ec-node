package nl.hanze.ec.node;

import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.network.peers.commands.responses.HeadersResponse;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationTest {
    @Test
    public void testRun() {
        assertTrue(true);
    }

    @Test
    public void testHeadersResponse() {
        HeadersResponse resp1 = new HeadersResponse(new ArrayList<>() {{ add(new Block("$hash$", "$previousBlockHash$", "$merkleRootHash$", 0)); }}, 1);

        String json = resp1.getPayload().toString();

        System.out.println(json);

        HeadersResponse resp2 = new HeadersResponse(new JSONObject(json), null);

        System.out.println(resp2.getHeaders());

        assertEquals("{\"number\":0," +
                "\"headers\":[{" +
                    "\"merkle_root_hash\":\"$merkleRootHash$\"," +
                    "\"previous_block_hash\":\"$previousBlockHash$\"," +
                    "\"block_height\":0,\"hash\":\"$hash$\"" +
                "}]," +
                "\"command\":\"headers-response\"}", json);
    }
}
