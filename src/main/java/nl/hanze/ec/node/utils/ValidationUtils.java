package nl.hanze.ec.node.utils;

import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.exceptions.InvalidTransaction;

import java.security.PublicKey;

public class ValidationUtils {

    public synchronized static void validateTransaction(Transaction transaction) throws InvalidTransaction {
        PublicKey publicKey = SignatureUtils.decodePublicKey(transaction.getPublicKey());
        if (!HashingUtils.getAddress(publicKey).equals(transaction.getFrom())) {
            throw new InvalidTransaction("Public key not valid [curr:" + transaction + "]");
        }

        String message = transaction.getFrom() + transaction.getTo() + transaction.getTimestamp() + transaction.getAmount();
        if (!SignatureUtils.verify(publicKey, transaction.getSignature(), message)) {
            throw new InvalidTransaction("Signature not valid [curr:" + transaction + "]");
        }

        String calculatedHash = HashingUtils.generateTransactionHash(transaction.getFrom(), transaction.getTo(), transaction.getAmount(), transaction.getSignature());
        if (!calculatedHash.equals(transaction.getHash())) {
            throw new InvalidTransaction("Transaction Hash not valid [curr:" + transaction + "]");
        }
    }
}
