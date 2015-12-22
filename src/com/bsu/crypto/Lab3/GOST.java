package com.bsu.crypto.Lab3;

import java.nio.charset.Charset;

public class GOST {

    private int mode;
    //public static String key;
    private String text;
    private static byte[] gamma = new byte[8];

    private byte[] k = new byte[32];

    private static final byte[][] Sbox = new byte[][] {
            { 0x04, 0x0a, 0x09, 0x02, 0x0d, 0x08, 0x00, 0x0e, 0x06, 0x0B, 0x01, 0x0c, 0x07, 0x0f, 0x05, 0x03 },
            { 0x0e, 0x0b, 0x04, 0x0c, 0x06, 0x0d, 0x0f, 0x0a, 0x02, 0x03, 0x08, 0x01, 0x00, 0x07, 0x05, 0x09 },
            { 0x05, 0x08, 0x01, 0x0d, 0x0a, 0x03, 0x04, 0x02, 0x0e, 0x0f, 0x0c, 0x07, 0x06, 0x00, 0x09, 0x0b },
            { 0x07, 0x0d, 0x0a, 0x01, 0x00, 0x08, 0x09, 0x0f, 0x0e, 0x04, 0x06, 0x0c, 0x0b, 0x02, 0x05, 0x03 },
            { 0x06, 0x0c, 0x07, 0x01, 0x05, 0x0f, 0x0d, 0x08, 0x04, 0x0a, 0x09, 0x0e, 0x00, 0x03, 0x0b, 0x02 },
            { 0x04, 0x0b, 0x0a, 0x00, 0x07, 0x02, 0x01, 0x0d, 0x03, 0x06, 0x08, 0x05, 0x09, 0x0c, 0x0f, 0x0e },
            { 0x0d, 0x0b, 0x04, 0x01, 0x03, 0x0f, 0x05, 0x09, 0x00, 0x0a, 0x0e, 0x07, 0x06, 0x08, 0x02, 0x0c },
            { 0x01, 0x0f, 0x0d, 0x00, 0x05, 0x07, 0x0a, 0x04, 0x09, 0x02, 0x03, 0x0e, 0x06, 0x0b, 0x08, 0x0c } };

    public GOST(String text,  int mode) {
//		if (key.length() <= 31) {
//			throw new IllegalArgumentException("Key must be 32+ symbols");
//		}

        if (text.length() == 0) {
            throw new IllegalArgumentException("Error. Text is empty.");
        }

        this.mode = mode;
        //this.key = key;
        this.text = text;
        //this.k = this.key.getBytes(Charset.forName("UTF-8"));
        this.gamma = new byte[8];

        if (mode == 1) {
            System.out.println("*** Режим простой замены ***");
            byte[] encoded = gostEncodeBasic(text, k);
            System.out.println("Байты закодированного текста: ");
            PrintByteArray(encoded);
            byte[] decoded = gostDecodeBasic(encoded, k);
            System.out.println("Расшифрованное сообщение: ");
            PrintByteArray(decoded);
        }

        byte[] textByteArray = prepareByteArray1(text);
        if (mode == 2) {
            System.out.println("*** Гаммирование ***");
            byte[] encoded = gostEncodeGamma(textByteArray, k);
            System.out.println("Байты закодированного текста: ");
            PrintByteArray(encoded);
            byte[] decoded = gostEncodeGamma(encoded, k);
            System.out.println("Расшифрованное сообщение: ");
            System.out.println(new String(decoded, Charset.forName("UTF-8")));
        }
        if (mode == 3) {
            System.out.println("*** Гаммирование с обратной связью ***");
            byte[] encoded = gostEncodeReverse(textByteArray, k);
            System.out.println("Байты закодированного текста: ");
            PrintByteArray(encoded);
            byte[] decoded = gostDecodeReverse(encoded, k);
            System.out.println("Расшифрованное сообщение: ");
            String dec = new String(decoded, Charset.forName("UTF-8"));
            System.out.println(dec.substring(0, dec.indexOf("1")));
        }
        if (mode == 4) {
            System.out.println("*** Выработка имитовставки ***");
            byte[] check = gostAuthentication(textByteArray, this.k);
            PrintByteArray(check);
            byte[] encoded = gostEncodeReverse(textByteArray, k);
            byte[] decoded = gostDecodeReverse(encoded, k);
            byte[] check2 = gostAuthentication(decoded, this.k);
            byte[] text1 = prepareByteArray(text);
            byte[] check3 = gostAuthentication(text1, this.k);
            PrintByteArray(check2);
            PrintByteArray(check3);
        }

    }

    public static byte[] gostEncodeBasic(String text, byte[] k) {
        byte[] TextByteArray = prepareByteArray(text);
        byte[] result = new byte[TextByteArray.length];

        int offset = 0;

        while (offset < TextByteArray.length) {
            byte[] encrypted = gostEncodeBlockFunction(get64BitBlockFromArray(TextByteArray, offset), k);
            System.arraycopy(encrypted, 0, result, offset, encrypted.length);
            offset += 8;
        }

        return result;
    }

    public byte[] gostEncodeGamma(byte[] TextByteArray, byte[] k) {
        byte[] result = new byte[TextByteArray.length];
        byte[] temp = this.gamma;

        int offset = 0;
        byte[] g = gostEncodeBlockFunction(temp, this.k);

        while (offset < TextByteArray.length) {

            int G1 = bytesToint(get32BitBlockFromArray(g, 0), 0);
            int G2 = bytesToint(get32BitBlockFromArray(g, 4), 0);

            G1 += 16843012;
            G2 += 16843009;

            intTobytes(G1, g, 0);
            intTobytes(G2, g, 4);

            temp = gostEncodeBlockFunction(g, this.k);

            byte[] block = get64BitBlockFromArray(TextByteArray, offset);
            int gam1 = bytesToint(temp, 0);
            int gam2 = bytesToint(temp, 4);

            int block1 = bytesToint(block, 0);
            int block2 = bytesToint(block, 4);

            int res1 = gam1 ^ block1;
            int res2 = gam2 ^ block2;

            byte[] encrypted = new byte[8];
            intTobytes(res1, encrypted, 0);
            intTobytes(res2, encrypted, 4);

            System.arraycopy(encrypted, 0, result, offset, 8);
            offset += 8;
        }
        return result;
    }

    public static byte[] gostEncodeChain(byte[] TextByteArray, byte[] k) {
        byte[] result = new byte[TextByteArray.length];
        byte[] temp = gamma;

        int offset = 0;
        byte[] g = gostEncodeBlockFunction(temp, k);

        while (offset < TextByteArray.length) {

            byte[] block = get64BitBlockFromArray(TextByteArray, offset);
            int gam1 = bytesToint(g, 0);
            int gam2 = bytesToint(g, 4);

            int block1 = bytesToint(block, 0);
            int block2 = bytesToint(block, 4);

            int res1 = gam1 ^ block1;
            int res2 = gam2 ^ block2;

            byte[] encrypted = new byte[8];
            intTobytes(res1, encrypted, 0);
            intTobytes(res2, encrypted, 4);

            byte[] encr = gostEncodeBlockFunction(g, k);
            g = encr;

            System.arraycopy(encr, 0, result, offset, 8);
            offset += 8;
        }
        return result;
    }

    public byte[] gostEncodeReverse(byte[] TextByteArray, byte[] k) {
        byte[] result = new byte[TextByteArray.length];
        byte[] temp = gamma;

        int offset = 0;

        while (offset < TextByteArray.length) {
            byte[] g = gostEncodeBlockFunction(temp, this.k);

            byte[] block = get64BitBlockFromArray(TextByteArray, offset);
            int gam1 = bytesToint(g, 0);
            int gam2 = bytesToint(g, 4);

            int block1 = bytesToint(block, 0);
            int block2 = bytesToint(block, 4);

            int res1 = gam1 ^ block1;
            int res2 = gam2 ^ block2;

            byte[] encrypted = new byte[8];
            intTobytes(res1, encrypted, 0);
            intTobytes(res2, encrypted, 4);

            System.arraycopy(encrypted, 0, result, offset, 8);
            temp = encrypted;
            offset += 8;
        }
        return result;
    }

    public byte[] gostDecodeReverse(byte[] TextByteArray, byte[] k) {
        byte[] result = new byte[TextByteArray.length];
        byte[] temp = this.gamma;

        int offset = 0;

        while (offset < TextByteArray.length) {
            byte[] g = gostEncodeBlockFunction(temp, this.k);

            byte[] block = get64BitBlockFromArray(TextByteArray, offset);

            int gam1 = bytesToint(g, 0);
            int gam2 = bytesToint(g, 4);

            int block1 = bytesToint(block, 0);
            int block2 = bytesToint(block, 4);

            int res1 = gam1 ^ block1;
            int res2 = gam2 ^ block2;

            byte[] encrypted = new byte[8];
            intTobytes(res1, encrypted, 0);
            intTobytes(res2, encrypted, 4);

            System.arraycopy(encrypted, 0, result, offset, 8);
            temp = block;

            offset += 8;
        }
        return result;
    }

    public static byte[] gostDecodeBasic(byte[] array, byte[] k) {
        byte[] TextByteArray = array;
        byte[] result = new byte[TextByteArray.length];

        int offset = 0;

        while (offset < TextByteArray.length) {
            byte[] encrypted = gostDecodeBlockFunction(get64BitBlockFromArray(TextByteArray, offset), k);
            System.arraycopy(encrypted, 0, result, offset, encrypted.length);
            offset += 8;
        }

        return result;
    }

    public static byte[] gostEncodeBlockFunction(byte[] block, byte[] k) {
        int left = bytesToint(get32BitBlockFromArray(block, 0), 0);// first 32
        // bits
        int right = bytesToint(get32BitBlockFromArray(block, 4), 0);// last 32
        // bits

        for (int i = 0; i < 3; i++) {
            for (int ki = 0; ki < 8; ki++) {
                int temp = left;
                left = right ^ gostMainStep(left, bytesToint(get32BitBlockFromArray(k, ki * 4), 0));
                right = temp;
            }
        }

        for (int ki = 7; ki > 0; ki--) {
            int tmp = left;
            left = right ^ gostMainStep(left, bytesToint(get32BitBlockFromArray(k, ki * 4), 0)); // XOR
            right = tmp;
        }

        right = right ^ gostMainStep(left, bytesToint(get32BitBlockFromArray(k, 0), 0));

        byte[] result = new byte[8];
        intTobytes(left, result, 0);
        intTobytes(right, result, 4);

        return result;

    }

    public byte[] gostAuthentication(byte[] text, byte[] k) {
        byte[] encrypted = new byte[8];
        int offset = 0;
        byte[] g = gostEncodeBlockFunction16Z(get64BitBlockFromArray(text, offset), this.k);
        while (offset < text.length - 8) {

            byte[] block = get64BitBlockFromArray(text, offset + 8);
            int gam1 = bytesToint(g, 0);
            int gam2 = bytesToint(g, 4);

            int block1 = bytesToint(block, 0);
            int block2 = bytesToint(block, 4);

            int res1 = gam1 ^ block1;
            int res2 = gam2 ^ block2;

            intTobytes(res1, encrypted, 0);
            intTobytes(res2, encrypted, 4);

            offset += 8;
            g = gostEncodeBlockFunction16Z(encrypted, this.k);
        }
        return encrypted;
    }

    public static byte[] gostEncodeBlockFunction16Z(byte[] block, byte[] k) {
        int left = bytesToint(get32BitBlockFromArray(block, 0), 0);// first 32
        // bits
        int right = bytesToint(get32BitBlockFromArray(block, 4), 0);// last 32
        // bits

        for (int i = 0; i < 2; i++) {
            for (int ki = 0; ki < 8; ki++) {
                int temp = left;
                left = right ^ gostMainStep(left, bytesToint(get32BitBlockFromArray(k, ki * 4), 0));
                right = temp;
            }
        }

        right = right ^ gostMainStep(left, bytesToint(get32BitBlockFromArray(k, 0), 0)); // 32
        // step
        // (left=left)

        byte[] result = new byte[8];
        intTobytes(left, result, 0);
        intTobytes(right, result, 4);

        return result;

    }

    public static byte[] gostDecodeBlockFunction(byte[] block, byte[] k) {
        int left = bytesToint(get32BitBlockFromArray(block, 0), 0);// first 32
        // bits
        int right = bytesToint(get32BitBlockFromArray(block, 4), 0);// last 32
        // bits

        for (int ki = 0; ki < 8; ki++) {
            int tmp = left;
            left = right ^ gostMainStep(left, bytesToint(get32BitBlockFromArray(k, ki * 4), 0)); // CM2
            right = tmp;
        }
        for (int i = 0; i < 3; i++) {
            for (int ki = 7; ki >= 0; ki--) {
                if ((i == 2) && (ki == 0)) {
                    break;
                }
                int tmp = left;
                left = right ^ gostMainStep(left, bytesToint(get32BitBlockFromArray(k, ki * 4), 0));
                right = tmp;
            }
        }

        right = right ^ gostMainStep(left, bytesToint(get32BitBlockFromArray(k, 0), 0));

        byte[] result = new byte[8];
        intTobytes(left, result, 0);
        intTobytes(right, result, 4);

        return result;
    }

    public static int gostMainStep(int left, int key) {
        int xorK = (key + left); // CM1

        // Преобразование S-блоков
        int replace = Sbox[0][((xorK >> (0 * 4)) & 0xF)] << (0 * 4);
        replace += Sbox[1][((xorK >> (1 * 4)) & 0xF)] << (1 * 4);
        replace += Sbox[2][((xorK >> (2 * 4)) & 0xF)] << (2 * 4);
        replace += Sbox[3][((xorK >> (3 * 4)) & 0xF)] << (3 * 4);
        replace += Sbox[4][((xorK >> (4 * 4)) & 0xF)] << (4 * 4);
        replace += Sbox[5][((xorK >> (5 * 4)) & 0xF)] << (5 * 4);
        replace += Sbox[6][((xorK >> (6 * 4)) & 0xF)] << (6 * 4);
        replace += Sbox[7][((xorK >> (7 * 4)) & 0xF)] << (7 * 4);
        return replace << 11 | replace >>> (32 - 11); // <<< 11
    }

    public static byte[] prepareByteArray(String text) {
        byte[] original = text.getBytes(Charset.forName("UTF-8"));
        int blocksCount = original.length % 8;

        if (blocksCount != 0) {
            blocksCount = original.length / 8 + 1;
        } else {
            blocksCount = original.length / 8;
        }

        byte[] result = new byte[blocksCount * 8];
        System.arraycopy(text, 0, result, 0, original.length);

        return result;
    }

    public static byte[] prepareByteArray1(String text) {
        byte[] original = text.getBytes(Charset.forName("UTF-8"));
        int blocksCount = original.length % 8;

        if (blocksCount != 0) {
            text += "1";
            for (int i = 8 - blocksCount - 1; i > 0; i--) {
                text += "0";
            }
            original = text.getBytes(Charset.forName("UTF-8"));
            blocksCount = original.length / 8 + 1;
        } else {
            text += "10000000";
            original = text.getBytes(Charset.forName("UTF-8"));
            blocksCount = original.length / 8 + 1;
        }

        byte[] result = new byte[blocksCount * 8];
        System.arraycopy(text.getBytes(Charset.forName("UTF-8")), 0, result, 0, original.length);

        return result;
    }

    public static byte[] get64BitBlockFromArray(byte[] array, int offset) {
        byte[] result = new byte[8];
        System.arraycopy(array, offset, result, 0, 8);
        return result;
    }

    public static byte[] get32BitBlockFromArray(byte[] array, int offset) {
        byte[] result = new byte[4];
        System.arraycopy(array, offset, result, 0, 4);
        return result;
    }

    // array of bytes to type int
    public static int bytesToint(byte[] in, int inOff) {
        return ((in[inOff + 3] << 24) & 0xff000000) + ((in[inOff + 2] << 16) & 0xff0000)
                + ((in[inOff + 1] << 8) & 0xff00) + (in[inOff] & 0xff);
    }

    // int to array of bytes
    public static void intTobytes(int num, byte[] out, int outOff) {
        out[outOff + 3] = (byte) (num >>> 24);
        out[outOff + 2] = (byte) (num >>> 16);
        out[outOff + 1] = (byte) (num >>> 8);
        out[outOff] = (byte) num;
    }

    public static void PrintByteArray(byte[] array) {
        String result = "";
        for (byte b : array) {
            byte[] temp = new byte[1];
            temp[0] = b;
            result += Byte.toString(b) + " ";
        }

        System.out.println(result);
    }
}