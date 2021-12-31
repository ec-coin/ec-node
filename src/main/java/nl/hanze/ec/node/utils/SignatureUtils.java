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

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
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

    public synchronized static void storeKeyPairInKeyStore() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            char[] password = "changeit".toCharArray();
            String path = "src/main/resources/secret/cacerts";
            FileInputStream fis = new FileInputStream(path);

            keyStore.load(fis, password);
            loadKeyFromKeyStore(keyStore, password, fis);
            System.out.println("data stored");
        }
        catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void retrieveKeyFromKeyStore() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            char[] password = "changeit".toCharArray();
            FileInputStream fis = new FileInputStream("src/main/resources/secret/cacerts");

            keyStore.load(fis, password);
            KeyStore.ProtectionParameter protectionParam = loadKeyFromKeyStore(keyStore, password, fis);
            KeyStore.SecretKeyEntry secretKeyEnt = (KeyStore.SecretKeyEntry) keyStore.getEntry("secretKeyAlias", protectionParam);

            SecretKey mysecretKey = secretKeyEnt.getSecretKey();
            System.out.println("Algorithm used to generate key : " + mysecretKey.getAlgorithm());
            System.out.println("Format used for the key: " + mysecretKey.getFormat());
        }
        catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
            e.printStackTrace();
        }
    }

    private synchronized static KeyStore.ProtectionParameter loadKeyFromKeyStore(KeyStore keyStore, char[] password, java.io.FileInputStream fis) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        //Creating the KeyStore.ProtectionParameter object
        KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection(password);

        //Creating SecretKey object
        SecretKey mySecretKey = new SecretKeySpec("myPassword".getBytes(), "ECDSA");

        //Creating SecretKeyEntry object
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(mySecretKey);
        keyStore.setEntry("privateKeyNode", secretKeyEntry, protectionParam);

        return protectionParam;
    }
}
