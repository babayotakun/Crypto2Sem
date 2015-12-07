package com.bsu.crypto;

import java.math.BigInteger;
import java.util.Random;

import static java.math.BigInteger.ONE;

public class Lab1 {

    private static int BITS = 512;
    private static int COUNT_OF_ROUNDS = 512;
    private static Random random = new Random();
    private static BigInteger TWO = BigInteger.valueOf(2);
    private static BigInteger FOUR = BigInteger.valueOf(4);
    private static BigInteger SEVEN = BigInteger.valueOf(7);
    private static BigInteger EIGHT = BigInteger.valueOf(8);

    public static void main(String[] args) {
        System.out.println("Probably prime by Solovay-Strassen test (size in bits: " + BITS + ", count of rounds: " + COUNT_OF_ROUNDS + ")");
        System.out.println(getProbablyPrimeBySolovayStrassen(BITS));
        System.out.println("\nProbably prime by Miller-Rabin test (size in bits: " + BITS + ", count of rounds: " + COUNT_OF_ROUNDS + ")");
        System.out.println(getProbablyPrimeByMillerRabin(BITS));
        System.out.println("\nStrong prime by Gordon algorithm");
        System.out.println(getPrimeByGordon(BITS/2));
    }

    /**
     *
     * @return
     */
    public static BigInteger getProbablyPrimeBySolovayStrassen(int bits) {
        while (true) {
            BigInteger probablyPrime = new BigInteger(bits, random);
            boolean isProbablyPrime = true;
            for (int i = 0; i < COUNT_OF_ROUNDS; i++) {
                if (!isPrimeBySolovayStrassen(probablyPrime)) {
                    isProbablyPrime = false;
                    break;
                }
            }
            if (isProbablyPrime) {
                return probablyPrime;
            }
        }
    }

    /**
     * генерирует псевдопростое число, используя тест Миллера-Рабина
     * с COUNT_OF_ROUNDS раундами
     */
    public static BigInteger getProbablyPrimeByMillerRabin(int bits) {
        while (true) {
            BigInteger probablyPrime = new BigInteger(bits, random);
            boolean isProbablyPrime = true;
            for (int i = 0; i < COUNT_OF_ROUNDS; i++) {
                if (!isPrimeByMillerRabin(probablyPrime)) {
                    isProbablyPrime = false;
                    break;
                }
            }
            if (isProbablyPrime) {
                return probablyPrime;
            }
        }
    }

    public static BigInteger getPrimeByGordon(int bits) {
        BigInteger s = getProbablyPrimeByMillerRabin(bits);
        BigInteger t = getProbablyPrimeByMillerRabin(bits);
        int i = random.nextInt(100);
        boolean isPrime = false;
        BigInteger r = null;
        while (!isPrime) {
            r = t.multiply(BigInteger.valueOf(2 * i)).add(ONE);
            isPrime = isPrimeByMillerRabin(r);
            i++;
        }
        isPrime = false;
        BigInteger p0 = (s.modPow(r.subtract(TWO), r).multiply(TWO).multiply(s)).subtract(ONE);
        int j = random.nextInt(100);
        BigInteger p = null;
        while (!isPrime) {
            p = r.multiply(BigInteger.valueOf(2 * j)).multiply(s).add(p0);
            isPrime = isPrimeByMillerRabin(p);
            j++;
        }
        return p;
    }

    /**
     * генерирует псевдопростое число, используя тест Соловьёва-Штрассена
     * с COUNT_OF_ROUNDS раундами
     */
    public static boolean isPrimeBySolovayStrassen(BigInteger p) {
        // случайное число а, меньшее, чем входное число p
        BigInteger a = generate(p);

        // если числа а и р не взаимнопросты, то возвращаем "составное"
        if (p.gcd(a).compareTo(ONE) != 0) {
            return false;
        }

        // считаем а^(p-1)/n mod p
        BigInteger euler = a.modPow(p.subtract(ONE).divide(BigInteger.valueOf(2)), p);
        // p-1 mod p = -1
        if (euler.add(ONE).mod(p).compareTo(BigInteger.ZERO) == 0) {
            euler = BigInteger.valueOf(-1);
        }
        // считаем символ Якоби: (a/p)
        BigInteger jacoby = BigInteger.valueOf(Jacobi(a, p));
        return euler.compareTo(jacoby) == 0;
    }

    public static boolean isPrimeByMillerRabin(BigInteger probablyPrime) {
        BigInteger a = generate(probablyPrime);
        BigInteger pMinusOne = probablyPrime.subtract(ONE);
        BigInteger t = pMinusOne;
        int s = t.getLowestSetBit();
        t = t.shiftRight(s);
        BigInteger aToPower = a.modPow(t, probablyPrime);
        if (aToPower.equals(ONE)) {
            return true;
        }
        for (int i = 0; i < s; i++) {
            if (aToPower.equals(pMinusOne)) {
                return true;
            }
            aToPower = aToPower.multiply(aToPower).mod(probablyPrime);
        }
        return false;
    }

    /**
     * Получает случайное число, меньшее, чем входное
     */
    public static BigInteger generate(BigInteger maxValue) {
        while (true) {
            BigInteger randomValue = new BigInteger(maxValue.bitLength(), random);
            if (randomValue.compareTo(maxValue) == -1) {
                return randomValue;
            }
        }
    }

    public static int Jacobi(BigInteger m, BigInteger n) {
        if (m.compareTo(n) >= 0) {
            m = m.mod(n);
            return Jacobi(m, n);
        }
        if (n.equals(ONE) || m.equals(ONE)) {
            return 1;
        }
        if (m.equals(BigInteger.ZERO)) {
            return 0;
        }

        int twoCount = m.getLowestSetBit();
        m = m.shiftRight(twoCount);

        int J2n = n.mod(EIGHT).equals(ONE) || n.mod(EIGHT).equals(SEVEN) ? 1 : -1;
        int rule8multiplier = (twoCount % 2 == 0) ? 1 : J2n;

        int tmp = Jacobi(n, m);
        int rule6multiplier = n.mod(FOUR).equals(ONE) || m.mod(FOUR).equals(ONE) ? 1 : -1;

        return tmp * rule6multiplier * rule8multiplier;
    }
}
