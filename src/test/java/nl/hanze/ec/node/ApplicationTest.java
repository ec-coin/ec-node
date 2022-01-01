package nl.hanze.ec.node;

import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.network.peers.commands.responses.HeadersResponse;
import nl.hanze.ec.node.utils.FileUtils;
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
    public void testHeadersResponse() {
        String type = "full";
        HeadersResponse resp1 = new HeadersResponse(new ArrayList<>() {{ add(new Block("$hash$", "$previousBlockHash$", "$merkleRootHash$", 0, type)); }}, 1);

        String json = resp1.getPayload().toString();

        System.out.println(json);

        HeadersResponse resp2 = new HeadersResponse(new JSONObject(json), null);

        System.out.println(resp2.getHeaders());

        assertEquals("{\"number\":0," +
                "\"headers\":[{" +
                    "\"merkle_root_hash\":\"$merkleRootHash$\"," +
                    "\"previous_block_hash\":\"$previousBlockHash$\"," +
                    "\"block_height\":0,\"type\":\"full\",\"hash\":\"$hash$\"" +
                "}]," +
                "\"command\":\"headers-response\",\"responseTo\":1}", json);
    }

    @Test
    public void bip39ToKeyPair() {
        SignatureUtils.generateKeyPair();
        //SignatureUtils.retrieveKeyFromKeyStore();
        return;
        /*try {
            Security.addProvider(new BouncyCastleProvider());

            // Generate mnemonic
            StringBuilder mnemonic = new StringBuilder();
            byte[] buffer = new byte[Words.TWELVE.byteLength()];
            new SecureRandom().nextBytes(buffer);
            new MnemonicGenerator(English.INSTANCE)
                    .createMnemonic(buffer, mnemonic::append);
            System.out.println("Mnemonic: " + mnemonic);

            // Mnemonic -> entropy
            byte[] entropy = new SeedCalculator().calculateSeed(mnemonic.toString(), "");
            System.out.println("Entropy: " + new BigInteger(1, entropy).toString(16));

            // entropy -> sha256(entropy)
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            BigInteger hash = new BigInteger(1, md.digest(entropy));
            System.out.println("Hash: " + hash.toString(16));

            // sha256(entropy) -> ECPrivateKey
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");

            BigInteger privKey = hash;
            ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privKey, ecSpec);
            ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);
            System.out.println(privateKey.toString());

            // ECPrivateKey -> ECPublicKey
            ECPoint Q = (new FixedPointCombMultiplier()).multiply(ecSpec.getG(), privateKey.getD()).normalize();
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(Q, ecSpec);
            PublicKey publicKey = keyFactory.generatePublic(pubSpec);
            System.out.println(publicKey.toString());

//            byte[] signature = SignatureUtils.sign(new KeyPair(publicKey, privateKey), "hello world");
//            System.out.println("Signature: " + new BigInteger(signature).toString(16));

            Signature signer = Signature.getInstance("SHA256WithECDSA", "BC");
            signer.initSign(privateKey);
            signer.update("hello world".getBytes());
            byte[] signature = signer.sign();
            System.out.println("Signature: " + new BigInteger(signature).toString(16));

            KeyPair keyPair = new KeyPair(publicKey, privateKey);
            byte[] sig = SignatureUtils.sign(keyPair, "hello world");
            boolean verified = SignatureUtils.verify(publicKey, sig, "hello world");
            System.out.println("verified: " + verified);

            System.out.println(FileUtils.writeToResources("privateKey.txt", privateKey));

            // return new KeyPair(publicKey, privateKey);
        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
        }*/
    }
}
