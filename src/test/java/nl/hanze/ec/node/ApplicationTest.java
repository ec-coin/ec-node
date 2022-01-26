package nl.hanze.ec.node;

import io.github.novacrypto.bip39.SeedCalculator;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.network.peers.commands.responses.HeadersResponse;
import nl.hanze.ec.node.utils.BaseNUtils;
import nl.hanze.ec.node.utils.HashingUtils;
import nl.hanze.ec.node.utils.SignatureUtils;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;

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
        String type = "block";
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
    public void testWalletAndNodeSignatureCompatibility() {
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

    @Test
    public void testAddressGeneration() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        Security.addProvider(new BouncyCastleProvider());

        String mnemonic = "assume mistake soft attract panic engage become hood best consider sunset quiz";

        // Mnemonic -> entropy
        byte[] entropy = new SeedCalculator().calculateSeed(mnemonic, "");

        // entropy -> sha256(entropy)
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BigInteger hash = new BigInteger(1, md.digest(entropy));

        // sha256(entropy) -> ECPrivateKey
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");

        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(hash, ecSpec);
        ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);

        // ECPrivateKey -> ECPublicKey
        ECPoint Q = (new FixedPointCombMultiplier()).multiply(ecSpec.getG(), privateKey.getD()).normalize();
        ECPublicKeySpec pubSpec = new ECPublicKeySpec(Q, ecSpec);
        ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(pubSpec);

        String address = HashingUtils.getAddress(publicKey);

        assertEquals( "HbTtVZF7avhnZTSCNcxHkfdxZg3FXhydmjvnJUynhdr1", address);
    }

    @Test
    public void testTimestampToDatetime() {
        DateTime date = new DateTime(Long.parseLong("1642842392900"));

        assertEquals("2022-01-22T10:06:32.900+01:00", date.toString());
    }

    @Test void testDatetimeToTimestamp() {
        DateTime date = DateTime.parse("2022-01-22T09:57:13.944Z");

        assertEquals(1642845433944L, date.getMillis());
    }
}