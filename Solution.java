import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.*;

// Fraction class to handle exact rational arithmetic
class Fraction {
    BigInteger num;
    BigInteger den;

    Fraction(BigInteger n, BigInteger d) {
        if (d.signum() == 0) throw new ArithmeticException("Denominator zero");
        if (d.signum() < 0) { // keep denominator positive
            n = n.negate();
            d = d.negate();
        }
        BigInteger g = n.gcd(d);
        num = n.divide(g);
        den = d.divide(g);
    }

    Fraction add(Fraction other) {
        BigInteger n = this.num.multiply(other.den).add(other.num.multiply(this.den));
        BigInteger d = this.den.multiply(other.den);
        return new Fraction(n, d);
    }

    Fraction multiply(Fraction other) {
        return new Fraction(this.num.multiply(other.num), this.den.multiply(other.den));
    }

    Fraction negate() {
        return new Fraction(this.num.negate(), this.den);
    }

    long toLong() {
        if (!den.equals(BigInteger.ONE)) {
            throw new ArithmeticException("Not an integer result: " + num + "/" + den);
        }
        return num.longValue();
    }
}

public class Solution {
    // Parse a string containing digits/letters into BigInteger given base (2..36)
    static BigInteger parseBigIntegerInBase(String s, int base) {
        if (s == null || s.isEmpty()) throw new NumberFormatException("Empty value string");
        if (base < Character.MIN_RADIX || base > Character.MAX_RADIX)
            throw new NumberFormatException("Base out of range: " + base);
        s = s.trim();
        boolean negative = false;
        if (s.startsWith("-")) { negative = true; s = s.substring(1); }
        s = s.toLowerCase(Locale.ROOT);
        BigInteger result = BigInteger.ZERO;
        BigInteger B = BigInteger.valueOf(base);
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            int digit = Character.digit(c, base);
            if (digit == -1) throw new NumberFormatException("Invalid digit '" + c + "' for base " + base);
            result = result.multiply(B).add(BigInteger.valueOf(digit));
        }
        return negative ? result.negate() : result;
    }

    public static void main(String[] args) throws Exception {
        String filename = (args.length > 0) ? args[0] : "input2.json"; // default
        String json;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            json = sb.toString();
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
            return;
        }

        // extract k
        Matcher km = Pattern.compile("\"k\"\\s*:\\s*(\\d+)").matcher(json);
        if (!km.find()) {
            System.err.println("Could not find key \"k\" in JSON.");
            return;
        }
        int k = Integer.parseInt(km.group(1));

        // Prepare lists for x and y
        ArrayList<BigInteger> xs = new ArrayList<>();
        ArrayList<BigInteger> ys = new ArrayList<>();

        // Pattern to extract each indexed object like "1": { "base": "10", "value":"4" }
        for (int i = 1; i <= k; ++i) {
            String patStr = "\"" + i + "\"\\s*:\\s*\\{[^}]*\"base\"\\s*:\\s*\"(\\d+)\"[^}]*\"value\"\\s*:\\s*\"([^\"]+)\"";
            Matcher m = Pattern.compile(patStr).matcher(json);
            if (!m.find()) {
                System.err.println("Warning: entry for index " + i + " not found or not in expected format.");
                continue;
            }
            int base = Integer.parseInt(m.group(1));
            String valueStr = m.group(2);
            xs.add(BigInteger.valueOf(i));
            ys.add(parseBigIntegerInBase(valueStr, base));
        }

        if (xs.size() < k) {
            System.err.println("Found only " + xs.size() + " valid points but expected k=" + k + ". Aborting.");
            return;
        }

        // Lagrange interpolation at x = 0 using Fraction
        Fraction result = new Fraction(BigInteger.ZERO, BigInteger.ONE);
        for (int i = 0; i < k; ++i) {
            Fraction Li = new Fraction(BigInteger.ONE, BigInteger.ONE);
            for (int j = 0; j < k; ++j) {
                if (i == j) continue;
                Li = Li.multiply(new Fraction(xs.get(j).negate(), xs.get(i).subtract(xs.get(j))));
            }
            Li = Li.multiply(new Fraction(ys.get(i), BigInteger.ONE));
            result = result.add(Li);
        }

        // Print exact constant term as long
        try {
            long constant = result.toLong();
            System.out.println(constant);
        } catch (ArithmeticException ex) {
            System.err.println("Result is too large to fit in long: " + ex.getMessage());
        }
    }
}
