package edu.neumont.server.services;

import edu.neumont.server.test.MyTest;

/**
 * User: Sean Yergensen
 */
public class MathLogic {
    private static MathLogic ourInstance = new MathLogic();

    public static MathLogic getInstance() {
        return ourInstance;
    }

    private MathLogic() {
    }

    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int add(MyTest[] a, int[] b, float[] c, double[] d, long[] e, short[] f, boolean[] g, String[] h, byte[] i) {
        int total = 0;
//        for (int a1 : a) {
//            total += a1;
//        }
        return total;
    }
}
