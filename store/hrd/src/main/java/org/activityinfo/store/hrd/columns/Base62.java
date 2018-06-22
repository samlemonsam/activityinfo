package org.activityinfo.store.hrd.columns;

public class Base62 {

    private static final char[] DIGITS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int BASE = DIGITS.length;

    public static String encode(long x) {
        if (x == 0) {
            // Simply return "0"
            return "0";
        } else if (x > 0) {
            return encodePositive(x);
        } else {
            char[] buf = new char[64];
            int i = buf.length;

            // Separate off the last digit using unsigned division. That will leave
            // a number that is nonnegative as a signed integer.
            long quotient;

            // Fast path for the usual case where the radix is even.
            quotient = (x >>> 1) / (BASE >>> 1);

            long rem = x - quotient * BASE;
            buf[--i] = DIGITS[(int) rem];
            x = quotient;

            // Simple modulo/division approach
            while (x > 0) {
                buf[--i] = DIGITS[(int) (x % BASE)];
                x /= BASE;
            }
            // Generate string
            return new String(buf, i, buf.length - i);
        }
    }


    private static String encodePositive(long number) {
        char[] buf = new char[64];
        int i = buf.length;

        while (number > 0) {
            buf[--i] = DIGITS[(int) (number % BASE)];
            number /= BASE;
        }
        return new String(buf, i, buf.length - i);
    }
}
