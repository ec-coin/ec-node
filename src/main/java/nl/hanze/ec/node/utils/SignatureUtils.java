package nl.hanze.ec.node.utils;

import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.SeedCalculator;
import io.github.novacrypto.bip39.Words;
import io.github.novacrypto.bip39.wordlists.English;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class SignatureUtils {

    public synchronized static KeyPair generateKeyPair() {
        KeyPair keyPair = null;

        try {
            Security.addProvider(new BouncyCastleProvider());

            // Generate mnemonic
            StringBuilder mnemonic = new StringBuilder();
            byte[] buffer = new byte[Words.TWELVE.byteLength()];
            new SecureRandom().nextBytes(buffer);
            new MnemonicGenerator(English.INSTANCE)
                    .createMnemonic(buffer, mnemonic::append);

            // Mnemonic -> entropy
            byte[] entropy = new SeedCalculator().calculateSeed(mnemonic.toString(), "");

            // entropy -> sha256(entropy)
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            BigInteger hash = new BigInteger(1, md.digest(entropy));

            // sha256(entropy) -> ECPrivateKey
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");

            ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(hash, ecSpec);
            ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);
            System.out.println(privateKey.toString());

            // ECPrivateKey -> ECPublicKey
            ECPoint Q = (new FixedPointCombMultiplier()).multiply(ecSpec.getG(), privateKey.getD()).normalize();
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(Q, ecSpec);
            PublicKey publicKey = keyFactory.generatePublic(pubSpec);

            keyPair = new KeyPair(publicKey, privateKey);
            FileUtils.writeToResources("privateKey.txt", privateKey);
            FileUtils.writeToResources("publicKey.txt", privateKey);
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return keyPair;
    }

    public synchronized static byte[] sign(KeyPair keyPair, String value) {
        PrivateKey privateKey = keyPair.getPrivate();
        byte[] signature = null;
        try {
            Signature signer = Signature.getInstance("SHA256WithECDSA", "BC");
            signer.initSign(privateKey);
            signer.update(value.getBytes());
            signature = signer.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return signature;
    }

    public synchronized static boolean verify(PublicKey publicKey, byte[] signature, String value) {
        boolean legitimateSignature = false;
        try {
            Signature signer = Signature.getInstance("SHA256WithECDSA", "BC");
            signer.initVerify(publicKey);
            byte[] bytes = value.getBytes();
            signer.update(bytes);
            legitimateSignature = signer.verify(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return legitimateSignature;
    }
}
