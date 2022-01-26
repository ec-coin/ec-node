package nl.hanze.ec.node.utils;

import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.exceptions.InvalidTransaction;
import org.bouncycastle.jce.interfaces.ECPublicKey;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Objects;

public class ValidationUtils {

    public synchronized static void validateTransaction(Transaction transaction) throws InvalidTransaction {
        PublicKey publicKey = SignatureUtils.decodePublicKey(transaction.getPublicKey());
        String address = transaction.getFrom().equals("minter") ? transaction.getTo() : transaction.getFrom();

        if (!HashingUtils.getAddress((ECPublicKey) publicKey).equals(address)) {
            throw new InvalidTransaction("Public key not valid [curr:" + transaction + "]");
        }

        String message = transaction.getFrom() + transaction.getTo() + transaction.getTimestamp().getMillis() + transaction.getAmount();
        System.out.println("payload ValidationUtils: " + message);
        if (!SignatureUtils.verify(publicKey, transaction.getSignature(), message)) {
            throw new InvalidTransaction("Signature not valid [curr:" + transaction + "]");
        }

        String calculatedHash = HashingUtils.generateTransactionHash(transaction.getFrom(), transaction.getTo(), transaction.getAmount(), transaction.getSignature());
        if (!calculatedHash.equals(transaction.getHash())) {
            throw new InvalidTransaction("Transaction Hash not valid [curr:" + transaction + "]");
        }
    }

    public synchronized static void validateWalletTransaction(String address, PublicKey publicKey, String signature, String payload) throws InvalidTransaction {
        String walletAddress = HashingUtils.getAddress((ECPublicKey) publicKey);

        if (!address.equals(walletAddress)) {
            throw new InvalidTransaction("Wallet address is not valid!");
        }
        if (!SignatureUtils.verify(publicKey, signature, payload)) {
            throw new InvalidTransaction("Signature is not valid!");
        }
    }
}
