package nl.hanze.ec.node;

import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.network.peers.commands.responses.HeadersResponse;
import nl.hanze.ec.node.utils.BaseNUtils;
import nl.hanze.ec.node.utils.HashingUtils;
import nl.hanze.ec.node.utils.SignatureUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.math.BigInteger;
import java.security.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {
    @Test
    public void testRun() {
        assertTrue(true);
    }

    @Test
    public void testBlockHashing() {
        String previousBlockHash = "GENESIS";
        String merkleRootHash = "5feceb66ffc86f38d952786c6d696c79c2dbc239dd4e91b46729d73a27fb57e9";
        DateTime dateTime = new DateTime("2022-01-03T14:05:33.379+01:00");
        String hash = HashingUtils.generateBlockHash(merkleRootHash, previousBlockHash, dateTime);

        assertEquals("a0500af02626cb6491e5ac09b249000ddc52a2ba3c6f4061ef43cbfd42378441", hash);
    }

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
    public void testSignatureUtils() {
        KeyPair keyPair = SignatureUtils.generateKeyPair();
        String value = "message payload";
        String signature = SignatureUtils.sign(keyPair, value);
        String publicKeyString = SignatureUtils.encodePublicKey(keyPair.getPublic());
        System.out.println("public key: " + publicKeyString);
        PublicKey publicKey = SignatureUtils.decodePublicKey(publicKeyString);
        boolean verified = SignatureUtils.verify(publicKey, signature, value);
        assertTrue(verified);
    }

    @Test
    public void testBaseNUtils() {
        String encoding = BaseNUtils.Base58Encode("123456789",10);
        assertEquals("BukQL", encoding);
        String decoding = BaseNUtils.Base58Decode(encoding, 10);
        assertEquals("123456789", decoding);
        decoding = BaseNUtils.Base58Decode(encoding, 16);
        assertEquals("75bcd15", decoding);

        encoding = BaseNUtils.Base58Encode("1b0ee1e3",16);
        assertEquals("h7fYN", encoding);
        decoding = BaseNUtils.Base58Decode(encoding, 10);
        assertEquals("453960163", decoding);
        decoding = BaseNUtils.Base58Decode(encoding, 16);
        assertEquals("1b0ee1e3", decoding);

        encoding = BaseNUtils.Base64Encode1("green".getBytes());
        assertEquals("Z3JlZW4=", encoding);
    }

    @Test
    public void testSignatureAndBaseUtilsBetweenNodeAndWallet() {
        String walletPublicKeyString = "04e60ca651f6c87275943e26c3f9ae0c38c08b055393b2d253ff75b20c6f856f1782d3bbf8ee2e0590dd3570b2d2361e2d0c5809ae9b6c25d511611996b105b90b";
        String walletAddress = BaseNUtils.Base58Encode(new BigInteger(1, HashingUtils.hash(walletPublicKeyString)).toString(16), 16);
        PublicKey publicKey = SignatureUtils.decodeWalletPublicKey(walletPublicKeyString);

        assertEquals("5KMW7Uf81FDffEz5uoMyWVEYLkvJzJdPhxk1ytB22ome", walletAddress);
        assertNotNull(publicKey);
    }

    @Test
    public void testStuff() {
        StringBuilder hex = new StringBuilder();
        String msg = "hello world";
        for (byte i : msg.getBytes()) {
            hex.append(String.format("%02X ", i));
        }
        System.out.println(hex);

        System.out.println(SignatureUtils.generateKeyPair().getPublic());

        String hexJS = "3046022100bb024cd744b0f0d2d81398df1ad41e241b1b228fc353e187bab8643291bf6874022100e1cacdd6f4f4ae8e25b3c4bad83800eb826d89e4ca83319ea40ff3dc2b4ab023";
        String hexJava = SignatureUtils.sign(SignatureUtils.generateKeyPair(), msg);

        System.out.println("JS: " + hexJS);

        System.out.println("Java: " + hexJava);

        boolean valid = SignatureUtils.verify(SignatureUtils.generateKeyPair().getPublic(), hexJS, msg);

        assertTrue(valid);

        boolean valid1 = SignatureUtils.verify(SignatureUtils.generateKeyPair().getPublic(), hexJava, msg);

        assertTrue(valid1);
    }
}