package nl.hanze.ec.node;

import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.network.peers.commands.responses.HeadersResponse;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CommandsTest {
    @Test
    public void testHeadersResponse() {
        String type = "full";
        DateTime now = new DateTime();
        HeadersResponse resp1 = new HeadersResponse(new ArrayList<>() {{
            add(new Block("$hash$", "$previousBlockHash$", "$merkleRootHash$", 0, type, now));
        }}, 1);

        String json = resp1.getPayload().toString();

        System.out.println(json);

        HeadersResponse resp2 = new HeadersResponse(new JSONObject(json), null);
        System.out.println(resp2.getHeaders());

        assertEquals("{\"number\":0," +
                "\"headers\":[{" +
                "\"merkle_root_hash\":\"$merkleRootHash$\"," +
                "\"previous_block_hash\":\"$previousBlockHash$\"," +
                "\"block_height\":0," +
                "\"hash\":\"$hash$\"," +
                "\"timestamp\":\"" + now + "\"" +
                "}]," +
                "\"command\":\"headers-response\",\"responseTo\":1}", json);
    }

    @Test
    public void testSchemaValidation() {
        InputStream inputStream = getClass().getResourceAsStream("/json-schemas/version.json");
        JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
        Schema schema = SchemaLoader.load(rawSchema);
        schema.validate(new JSONObject("{\"command\": \"version\", \"version\" : 1.0, \"number\": 0, \"start_height\": 0}"));

        assertDoesNotThrow(() -> schema.validate(new JSONObject("{\"command\": \"version\", \"version\" : 1.0, \"number\": 0, \"start_height\": 0}")));
        assertDoesNotThrow(() -> schema.validate(new JSONObject("{\"command\": \"version\", \"version\" : 1, \"number\": 0, \"start_height\": 0}")));
        assertThrows(ValidationException.class, () -> schema.validate(new JSONObject("{\"command\": \"other\", \"version\" : 1.0, \"number\": 0, \"start_height\": 0}")));
        assertThrows(ValidationException.class, () -> schema.validate(new JSONObject("{\"command\": \"version\", \"version\" : \"string\", \"number\": 0, \"start_height\": 0}")));
    }
}
