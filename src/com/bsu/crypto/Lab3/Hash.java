package com.bsu.crypto.Lab3;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

public class Hash {

    public static Random rnd = new Random();
    private static int currentIndex = 0;
    private static int currentValue = -128;
    private static byte[] result1 = new byte[32];

    private static int currentIndex2 = 0;
    private static int currentValue2 = -128;
    private static byte[] result2 = new byte[32];

    private static int currentIndex3 = 0;
    private static int currentValue3 = -128;
    private static byte[] result3 = new byte[32];

    public static byte[] prepareByteArray(String text) {
        byte[] original = text.getBytes(Charset.forName("UTF-8"));
        int blocksCount = original.length % 32;

        if (blocksCount != 0) {
            text += "1";
            for (int i = 32 - blocksCount - 1; i > 0; i--) {
                text += "0";
            }
            original = text.getBytes(Charset.forName("UTF-8"));
            blocksCount = original.length / 32;
        } else {
            text += "10000000000000000000000000000000";
            original = text.getBytes(Charset.forName("UTF-8"));
            blocksCount = original.length / 32 + 1;
        }

        byte[] result = new byte[blocksCount * 32];
        System.arraycopy(text.getBytes(Charset.forName("UTF-8")), 0, result, 0, original.length);

        return result;
    }

    public static byte[] xor64(byte[] x, byte[] y) {
        int leftX = GOST.bytesToint(GOST.get32BitBlockFromArray(x, 0), 0);
        int rightX = GOST.bytesToint(GOST.get32BitBlockFromArray(x, 4), 0);// last

        int leftY = GOST.bytesToint(GOST.get32BitBlockFromArray(y, 0), 0);// first
        int rightY = GOST.bytesToint(GOST.get32BitBlockFromArray(y, 4), 0);// last

        byte[] result = new byte[8];
        GOST.intTobytes(leftX ^ leftY, result, 0);
        GOST.intTobytes(rightX ^ rightY, result, 4);

        return result;
    }

    public static byte[] xor256(byte[] x, byte[] y) {
        byte[] result = new byte[32];
        for (int i = 0; i < 4; i++) {
            byte[] x64 = GOST.get64BitBlockFromArray(x, i * 8);
            byte[] y64 = GOST.get64BitBlockFromArray(y, i * 8);
            byte[] res64 = xor64(x64, y64);
            System.arraycopy(res64, 0, result, i * 8, res64.length);
        }
        return result;
    }


    /** F(X + Y) + X */
    public static byte[] getHash(String m) {
        byte[] preparedText = prepareByteArray(m);
        System.out.println(preparedText.length);
        int blockCount = preparedText.length / 32;
        byte[] y0 = new byte[32];
        for (int i = 0; i < blockCount; i++) {
            byte[] result = new byte[32];
            byte[] s = new byte[32];
            for (int j = 0; j < 4; j++) {
                byte[] s64 = GOST.get64BitBlockFromArray(preparedText, 8 * j + i * 32);
                System.arraycopy(s64, 0, s, j * 8, s64.length);
                byte[] xPLUSy = xor64(s64, y0);
                byte[] encoded = GOST.gostEncodeBlockFunction(xPLUSy, y0);
                System.arraycopy(encoded, 0, result, j * 8, encoded.length);
            }
            y0 = xor256(result, s);
        }
        return y0;// Longs.fromByteArray(y0);

    }

    public static byte[] getHash2(byte[] x, byte[] y0) {
        int blockCount = x.length / 32;
        for (int i = 0; i < blockCount; i++) {
            byte[] result = new byte[32];
            for (int j = 0; j < 4; j++) {
                byte[] y64 = GOST.get64BitBlockFromArray(y0, 8 * j + i * 32);
                byte[] encoded = GOST.gostEncodeBlockFunction(y64, x);
                System.arraycopy(encoded, 0, result, j * 8, encoded.length);
            }
            y0 = xor256(result, x);
        }
        return y0;

    }

    public static byte[] getHash2Reverse(byte[] x, byte[] y0) {
        int blockCount = x.length / 32;
        for (int i = 0; i < blockCount; i++) {
            y0 = xor256(y0, x);
            byte[] result = new byte[32];
            for (int j = 0; j < 4; j++) {
                byte[] y64 = GOST.get64BitBlockFromArray(y0, 8 * j + i * 32);
                byte[] encoded = GOST.gostDecodeBlockFunction(y64, x);
                System.arraycopy(encoded, 0, result, j * 8, encoded.length);
            }
            y0 = result;
        }
        return y0;
    }

    public static void findCollisions() {
        while (currentIndex3 != 32) {
            byte[] y0 = getNewY();
            int i = 0;
            while (currentIndex != 32) {
                byte[] x = getNewX1();
                byte[] res = getHash2(x, y0);
                while (currentIndex2 != 32) {
                    byte[] compare = getNewX2();
                    byte[] yRestored = getHash2Reverse(compare, res);
                    if (Arrays.equals(y0, yRestored) && !Arrays.equals(x, compare)) {
                        GOST.PrintByteArray(compare);
                        GOST.PrintByteArray(x);
                    }// else
                    //System.out.println(i++);
                }
                currentIndex2 = 0;
            }
            currentIndex = 0;

        }
    }

    public static byte[] getNewX1() {

        for (int i = 0; i < result1.length; i++) {
            if (i == currentIndex) {
                result1[i] = (byte) currentValue;
                currentValue++;
            }
        }

        if (currentValue == 127) {
            currentIndex++;
            currentValue = -128;
        }
        return result1;
    }

    public static byte[] getNewX2() {
        // byte[] result = new byte[32];
        for (int i = 0; i < result2.length; i++) {
            if (i == currentIndex2) {
                result2[i] = (byte) currentValue2;
                currentValue2++;
            }
        }
        currentValue2++;
        if (currentValue2 == 127) {
            currentIndex2++;
            currentValue2 = -128;
        }
        return result2;
    }

    public static byte[] getNewY() {
        // byte[] result = new byte[32];
        for (int i = 0; i < result3.length; i++) {
            if (i == currentIndex3) {
                result3[i] = (byte) currentValue3;
                currentValue3++;
            }
        }
        // currentValue3++;
        if (currentValue3 == 127) {
            currentIndex3++;
            currentValue3 = -128;
        }
        return result3;
    }
}
