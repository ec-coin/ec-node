package nl.hanze.ec.node;

import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.network.peers.commands.responses.HeadersResponse;
import nl.hanze.ec.node.utils.FileUtils;
import nl.hanze.ec.node.utils.HashingUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.SeedCalculator;
import io.github.novacrypto.bip39.Words;
import io.github.novacrypto.bip39.wordlists.English;
import nl.hanze.ec.node.utils.SignatureUtils;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.junit.jupiter.api.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationTest {
    @Test
    public void testRun() {
        assertTrue(true);
    }

    @Test
    public void testHashing() {
        String previousBlockHash = "GENESIS";
        String merkleRootHash = HashingUtils.hash("" + 0);
        String hash = HashingUtils.hash(previousBlockHash + merkleRootHash);

        assertEquals("5feceb66ffc86f38d952786c6d696c79c2dbc239dd4e91b46729d73a27fb57e9", merkleRootHash);
        assertEquals("95acc55d4646f4e482d85fdd573689f0b490535a181af6a7ab1c20f94b6ca509", hash);
    }

    @Test
    public void testHeadersResponse() {
        String type = "full";
        DateTime now = new DateTime();
        HeadersResponse resp1 = new HeadersResponse(new ArrayList<>() {{ add(new Block("$hash$", "$previousBlockHash$", "$merkleRootHash$", 0, type, now)); }}, 1);

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
}
