package nl.hanze.ec.node.services;

import java.security.*;

public class SignatureService {

    public synchronized static KeyPair generateKeyPair() {
        KeyPair pair = null;
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DSA");
            keyPairGen.initialize(2048);
            pair = keyPairGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return pair;
    }

    public synchronized static byte[] sign(KeyPair keyPair, String value) {
        PrivateKey privateKey = keyPair.getPrivate();
        byte[] signature = null;
        try {
            Signature sig = Signature.getInstance("SHA256withDSA");
            sig.initSign(privateKey);
            byte[] bytes = value.getBytes();
            sig.update(bytes);
            signature = sig.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return signature;
    }

    public synchronized static boolean verify(PublicKey publicKey, byte[] signature, String value) {
        boolean legitimateSignature = false;
        try {
            Signature sig = Signature.getInstance("SHA256withDSA");
            sig.initVerify(publicKey);
            byte[] bytes = value.getBytes();
            sig.update(bytes);
            legitimateSignature = sig.verify(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return legitimateSignature;
    }
}
