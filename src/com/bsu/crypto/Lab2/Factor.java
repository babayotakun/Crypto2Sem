package com.bsu.crypto.Lab2;

import java.math.BigInteger;

/**
 * Created by Калач on 21.12.2015.
 */
public class Factor {

    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final BigInteger FIRST_STEP = BigInteger.valueOf(25);

    public static void main(String[] args) {
        System.out.println(factor(BigInteger.valueOf(2).pow(64).subtract(BigInteger.ONE)));
        System.out.println(factor(BigInteger.valueOf(1997 * 11)));
    }

    public static BigInteger factor(BigInteger n) {
        BigInteger X = sqrt(n);
        BigInteger sqrtY = X.multiply(X).subtract(n);
        BigInteger Y = sqrt(sqrtY);
        BigInteger XMinusY = X.subtract(Y);

        BigInteger XMinusYPrevious = X.subtract(Y).add(FIRST_STEP);
        while (!sqrt(sqrtY).pow(2).equals(sqrtY) && XMinusYPrevious.subtract(XMinusY).compareTo(FIRST_STEP) > -1) {
            X = X.add(BigInteger.ONE);
            sqrtY = X.multiply(X).subtract(n);
            Y = sqrt(sqrtY);
            XMinusYPrevious = XMinusY;
            XMinusY = X.subtract(Y);
        }
        if (!sqrt(sqrtY).pow(2).equals(sqrtY)) {
            boolean solved = false;
            BigInteger p = XMinusY.add(TWO);
            if (p.remainder(TWO).intValue() == 0) {
                p = p.add(BigInteger.ONE);
            }
            while (!solved) {
                p = p.subtract(TWO);
                if (n.remainder(p).equals(BigInteger.ZERO)) {
                    solved = true;
                }
            }
            return p;
        } else {
            return XMinusY;
        }
    }

    public static BigInteger sqrt(BigInteger n) {
        BigInteger r = BigInteger.ZERO;
        BigInteger m = r.setBit(2 * n.bitLength());
        BigInteger nr;
        do {
            nr = r.add(m);
            if (nr.compareTo(n) != 1) {
                n = n.subtract(nr);
                r = nr.add(m);
            }
            r = r.shiftRight(1);
            m = m.shiftRight(2);
        } while (m.bitCount() != 0);
        return r;
    }
}
