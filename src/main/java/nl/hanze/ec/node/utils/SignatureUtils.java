package nl.hanze.ec.node.utils;

import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.SeedCalculator;
import io.github.novacrypto.bip39.Words;
import io.github.novacrypto.bip39.wordlists.English;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.*;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class SignatureUtils {

    public synchronized static KeyPair generateKeyPair() {
        KeyPair keyPair = null;

        try {
            Security.addProvider(new BouncyCastleProvider());
            String savedMnemonic = FileUtils.readFromResources("secret/mnemonic.txt");

            //StringBuilder mnemonic;
            if (savedMnemonic.equals("")) {
                StringBuilder mnemonic = new StringBuilder();
                byte[] buffer = new byte[Words.TWELVE.byteLength()];
                new SecureRandom().nextBytes(buffer);

                new MnemonicGenerator(English.INSTANCE)
                        .createMnemonic(buffer, mnemonic::append);
                savedMnemonic = mnemonic.toString();
            }

            FileUtils.writeToResources("mnemonic.txt", savedMnemonic);

            // Mnemonic -> entropy
            byte[] entropy = new SeedCalculator().calculateSeed(savedMnemonic, "");

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
            PublicKey publicKey = keyFactory.generatePublic(pubSpec);

            keyPair = new KeyPair(publicKey, privateKey);
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return keyPair;
    }

    public synchronized static String sign(KeyPair keyPair, String value) {
        PrivateKey privateKey = keyPair.getPrivate();
        byte[] signature = null;
        try {
            Signature signer = Signature.getInstance("SHA256withECDSA", "BC");
            signer.initSign(privateKey);
            signer.update(value.getBytes());
            signature = signer.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return new BigInteger(1, signature).toString(16);
    }

    public synchronized static boolean verify(PublicKey publicKey, String signatureHex, String value) {
        boolean legitimateSignature = false;
        try {
            Signature signer = Signature.getInstance("SHA256withECDSA", "BC");
            signer.initVerify(publicKey);
            byte[] bytes = value.getBytes();
            signer.update(bytes);
            legitimateSignature = signer.verify(new BigInteger(signatureHex, 16).toByteArray());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return legitimateSignature;
    }

    public synchronized static String encodePublicKey(PublicKey publicKey) {
        return BaseNUtils.Base64Encode(publicKey.getEncoded());
    }

    public synchronized static PublicKey decodePublicKey(String publicKeyString) {
        byte[] encodedKey = BaseNUtils.Base64Decode(publicKeyString);

        PublicKey publicKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
            publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    public synchronized static PublicKey decodeWalletPublicKey(String publicKeyStringHex) {
        ECNamedCurveParameterSpec parameterSpecs = ECNamedCurveTable.getParameterSpec("secp256k1");
        Security.addProvider(new BouncyCastleProvider());

        String publicKeyString = publicKeyStringHex.substring(2);
        BigInteger x = new BigInteger(publicKeyString.substring(0, 64), 16);
        BigInteger y = new BigInteger(publicKeyString.substring(64), 16);

        PublicKey publicKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
            java.security.spec.ECPoint ecPoint = new java.security.spec.ECPoint(x, y);
            java.security.spec.ECParameterSpec spec = new ECNamedCurveSpec("secp256k1", parameterSpecs.getCurve(),
                    parameterSpecs.getG(), parameterSpecs.getN(), parameterSpecs.getH(), parameterSpecs.getSeed());
            publicKey = keyFactory.generatePublic(new java.security.spec.ECPublicKeySpec(ecPoint, spec));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKey;
    }
}
