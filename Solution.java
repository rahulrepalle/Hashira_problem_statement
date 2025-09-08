import java.io.*;
import java.math.BigInteger;
import java.util.*;

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

    BigInteger toBigInteger() {
        if (!den.equals(BigInteger.ONE)) {
            throw new ArithmeticException("Not an integer result: " + num + "/" + den);
        }
        return num;
    }
}

public class Solution {
    public static void main(String[] args) throws Exception {
        // Read JSON file (replace "input2.json" with the actual input filename)
        BufferedReader br = new BufferedReader(new FileReader("input2.json"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        String json = sb.toString();

        // Extract k
        int k = Integer.parseInt(json.replaceAll(".*\"k\"\\s*:\\s*(\\d+).*", "$1"));

        // Collect (xi, yi)
        ArrayList<BigInteger> xs = new ArrayList<>();
        ArrayList<BigInteger> ys = new ArrayList<>();

        for (int i = 1; i <= k; ++i) {
            String pattern = "\"" + i + "\"\\s*:\\s*\\{[^}]*\"base\"\\s*:\\s*\"(\\d+)\"[^}]*\"value\"\\s*:\\s*\"([^\"]+)\"";
            if (json.matches(".*" + pattern + ".*")) {
                String baseStr = json.replaceAll(".*" + pattern + ".*", "$1");
                String valueStr = json.replaceAll(".*" + pattern + ".*", "$2");
                int base = Integer.parseInt(baseStr);
                xs.add(BigInteger.valueOf(i));
                ys.add(new BigInteger(valueStr, base));
            }
        }

        // Lagrange interpolation at x=0
        Fraction result = new Fraction(BigInteger.ZERO, BigInteger.ONE);

        for (int i = 0; i < k; i++) {
            Fraction Li = new Fraction(BigInteger.ONE, BigInteger.ONE);
            for (int j = 0; j < k; j++) {
                if (i == j) continue;
                // Multiply by (-xj)/(xi - xj)
                Li = Li.multiply(new Fraction(xs.get(j).negate(), xs.get(i).subtract(xs.get(j))));
            }
            Li = Li.multiply(new Fraction(ys.get(i), BigInteger.ONE));
            result = result.add(Li);
        }

        // Print exact integer constant
        System.out.println(result.toBigInteger());
    }
}
