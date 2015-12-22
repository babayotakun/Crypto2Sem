package com.bsu.crypto.Lab4;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

public class KeyGenerator {

    public static ArrayList<BigInteger> generateKeys(int length) {
        BigInteger p = BigInteger.probablePrime(length, new Random());
        BigInteger g = PrimitiveRootFinder.findPrimitiveRootModP(p);
        BigInteger x = new BigInteger(p.bitLength(), new SecureRandom()).mod(p).add(BigInteger.ONE);
        BigInteger y = g.modPow(x, p);
        ArrayList<BigInteger> keys = new ArrayList<>();
        keys.add(p);
        keys.add(g);
        keys.add(y);
        keys.add(x);

        return keys;
    }
}