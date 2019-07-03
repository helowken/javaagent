package test;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;

public class Test111 {
    @Test
    public void test1() throws Exception {
        try (DataInputStream in = new DataInputStream(new FileInputStream("/home/helowken/test_clang/endianess.txt"))) {
            System.out.println("num from file: " + in.readInt());
        }
        System.out.println("==============");
        try (DataInputStream in = new DataInputStream(new FileInputStream("/home/helowken/test_clang/endianess.txt"))) {
            byte b = in.readByte();
            System.out.println("byte from file: " + b + ", " + Integer.toHexString(b));
            b = in.readByte();
            System.out.println("byte from file: " + b + ", " + Integer.toHexString(b));
            b = in.readByte();
            System.out.println("byte from file: " + b + ", " + Integer.toHexString(b));
            b = in.readByte();
            System.out.println("byte from file: " + b + ", " + Integer.toHexString(b));
        }
    }
}
