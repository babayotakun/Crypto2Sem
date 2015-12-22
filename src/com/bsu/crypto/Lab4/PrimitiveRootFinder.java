package com.bsu.crypto.Lab4;

import java.math.BigInteger;
import java.security.SecureRandom;

public class PrimitiveRootFinder {
    private static BigInteger ONE = BigInteger.ONE;
    private static BigInteger TWO = new BigInteger("2");


    public static BigInteger findPrimitiveRootModP(BigInteger p) {
        BigInteger g;
        BigInteger pMinusOne = p.subtract(ONE);
        BigInteger x = pMinusOne.divide(TWO);
        g = new BigInteger(p.bitLength(), new SecureRandom()).mod(p).add(TWO);
        BigInteger r1;
        BigInteger r2;
        while (true) {
            r1 = g.modPow(pMinusOne.divide(TWO), p);
            r2 = g.modPow(pMinusOne.divide(x), p);
            if (!r1.equals(ONE) && !r2.equals(ONE)) {
                break;
            }
            g = new BigInteger(p.bitLength(), new SecureRandom()).mod(p).add(TWO);
        }

        return g;
    }
}