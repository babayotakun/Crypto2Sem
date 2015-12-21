package com.bsu.crypto.Lab2;

import java.math.BigInteger;
import java.util.*;

import static java.math.BigInteger.valueOf;

/**
 * Created by Калач on 21.12.2015.
 */
public class DLOG {

    public static void main(String[] args) {
        //should be 4
        System.out.println(getDiscreteLog(3, 13, 17));

        //should be 78
        // System.out.println(valueOf(34).modPow(valueOf(78), valueOf(1997)));
        System.out.println(getDiscreteLog(34, 4, 1997));
    }


    /**
     * @param a
     * @param b
     * @param p should be prime
     * @return x from a^x == b (mod p) or -1 if something wrong
     */
    public static int getDiscreteLog(int a, int b, int p) {
        int h = (int) Math.floor(Math.pow(p * 1.0, 0.5)) + 1;
        BigInteger P = valueOf(p);
        if (!P.isProbablePrime(200)) {
            return -1;
        }
        BigInteger B = valueOf(b);
        BigInteger A = valueOf(a);
        BigInteger C = A.modPow(valueOf(h), P);
        Map<Integer, Integer> values = new HashMap<>();
        for (int u = 1; u <= h; u++) {
            values.put(C.modPow(valueOf(u), P).intValue(), u);
        }
        for (int v = 0; v <= h; v++) {
            int currentV = A.modPow(valueOf(v), P).multiply(B).mod(P).intValue();
            if (values.containsKey(currentV)) {
                int root = h * values.get(currentV) - v;
                if (root < p) {
                    return root;
                }
            }
        }
        return -1;
    }
}
