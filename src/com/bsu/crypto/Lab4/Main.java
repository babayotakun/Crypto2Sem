package com.bsu.crypto.Lab4;

import com.bsu.crypto.Lab3.Hash;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Main {

    /** Holds p, g, and y (public key) */
    public static ArrayList<BigInteger> publicKeys = new ArrayList<>();

    /** Holds x (private key) */
    public static ArrayList<BigInteger> privateKeys = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Hash of a message:");
        BigInteger hash = new BigInteger(Hash.getHash("Hello world!"));
        System.out.println(hash);
        doKeyGeneration(256);
        List<BigInteger> signature = doEncryption(hash);
        printPublicKeys();
        printPrivateKeys();
        printSignature(signature);
        System.out.println("\nVERIFIED:");
        System.out.println(doDecryption(hash, signature));
    }

    public static void printSignature(List<BigInteger> signature) {
        System.out.println("\nSIGNATURE: ");
        System.out.println("A: " + signature.get(0));
        System.out.println("B: " + signature.get(1));
    }

    public static void printPublicKeys() {
        System.out.println("\nPUBLIC KEYS: ");
        System.out.println("P: " + publicKeys.get(0));
        System.out.println("G: " + publicKeys.get(1));
        System.out.println("Y: " + publicKeys.get(2));
    }

    public static void printPrivateKeys() {
        System.out.println("\nPRIVATE KEYS:");
        System.out.println("X: " + privateKeys.get(0));
    }


    private static void doKeyGeneration(int bits) {
        /** @var keyParts Holds p, g, h, and x. */
        ArrayList<BigInteger> keyParts = KeyGenerator.generateKeys(bits);

        publicKeys.add(keyParts.get(0));
        publicKeys.add(keyParts.get(1));
        publicKeys.add(keyParts.get(2));
        privateKeys.add(keyParts.get(3));
    }

    public static List<BigInteger> doEncryption(BigInteger messageHash) {
        BigInteger p = publicKeys.get(0);
        BigInteger g = publicKeys.get(1);
        BigInteger y = publicKeys.get(2);
        BigInteger tempKey;
        tempKey = new BigInteger(p.bitLength(), new SecureRandom()).mod(p);
        BigInteger a = g.modPow(tempKey, p);
        BigInteger b = y.modPow(tempKey, p).multiply(messageHash).mod(p);
        List<BigInteger> resultPair = new ArrayList<>();
        resultPair.add(a);
        resultPair.add(b);
        return resultPair;
    }

    public static boolean doDecryption(BigInteger messageHash, List<BigInteger> signature) {
        BigInteger p = publicKeys.get(0);
        BigInteger x = privateKeys.get(0);
        BigInteger a = signature.get(0);
        BigInteger b = signature.get(1);
        BigInteger exponent = p.subtract(BigInteger.ONE).subtract(x);
        BigInteger signedHash = a.modPow(exponent, p).multiply(b).mod(p);

        return signedHash.compareTo(messageHash) == 0;
    }
}