package com.bsu.crypto.Lab3;

import java.io.IOException;
import java.math.BigInteger;


public class Main {

    public static void main(String[] args) throws IOException {

        System.out.println(new BigInteger(Hash.getHash("Hello world!")));
        System.out.println(new BigInteger(Hash.getHash("Helol world!")));
    }
}