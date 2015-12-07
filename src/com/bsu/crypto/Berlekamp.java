package com.bsu.crypto;

import java.util.Arrays;

/**
 * Created by Калач on 06.12.2015.
 */
public class Berlekamp {

    //Алгоритм Берлекэмпа — Мэсси
    static byte[] b, c, t, s = {0, 1, 1, 1, 0, 0, 1, 0, 1, 1, 1, 0};
    static int N, L, m;

    public static void main(String[] args) {
        setUp();
        runTest();
        System.out.println(m);
        System.out.println(Arrays.toString(Arrays.copyOf(c, m)));
    }

    public static void setUp() {
        int sequenceLength = s.length;
        b = new byte[sequenceLength];
        c = new byte[sequenceLength];
        t = new byte[sequenceLength];
        for (int i = 0; i < sequenceLength; i++)
            b[i] = c[i] = t[i] = 0;
        b[0] = c[0] = 1;
        N = L = 0;
        m = -1;
    }

    private static void runTest() {
        int d;
        while (N < s.length) {
            d = 0;
            for (int i = 0; i <= L; i++)
                d += s[N - i] * c[i];
            d = d % 2;

            if (d != 0) {
                t = c.clone();
                for (int i = 0; i <= s.length + m - 1 - N; i++)
                    c[N - m + i] = (byte) (c[N - m + i] ^ b[i]);
                if (L <= (N / 2)) {
                    L = N + 1 - L;
                    m = N;
                    b = t.clone();
                }
            }
            N++;
        }
    }
}
