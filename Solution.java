import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;

public class Solution {
    static MathContext mc = new MathContext(50); // Precision of 50 digits

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("input2.json"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        String json = sb.toString();

        int k = Integer.parseInt(json.replaceAll(".*\"k\"\\s*:\\s*(\\d+).*", "$1"));

        ArrayList<Integer> xs = new ArrayList<>();
        ArrayList<BigDecimal> ys = new ArrayList<>();

        for (int i = 1; i <= k; i++) {
            String pattern = "\"" + i + "\"\\s*:\\s*\\{[^}]*\"base\"\\s*:\\s*\"(\\d+)\"[^}]*\"value\"\\s*:\\s*\"([^\"]+)\"";
            if (json.matches(".*" + pattern + ".*")) {
                String baseStr = json.replaceAll(".*" + pattern + ".*", "$1");
                String valueStr = json.replaceAll(".*" + pattern + ".*", "$2");
                int base = Integer.parseInt(baseStr);
                xs.add(i);
                BigInteger bigVal = new BigInteger(valueStr, base);
                ys.add(new BigDecimal(bigVal));
            }
        }

        int deg = k - 1;
        BigDecimal[][] matrix = new BigDecimal[k][k + 1];

        // Build augmented matrix
        for (int i = 0; i < k; i++) {
            int x = xs.get(i);
            BigDecimal y = ys.get(i);
            for (int j = deg, col = 0; j >= 0; j--, col++) {
                matrix[i][col] = BigDecimal.valueOf(Math.pow(x, j));
            }
            matrix[i][k] = y;
        }

        BigDecimal[] coeffs = gauss(matrix);
        System.out.println("Constant term c = " + coeffs[deg].toPlainString());
    }

    // Gaussian elimination for BigDecimal
    static BigDecimal[] gauss(BigDecimal[][] m) {
        int n = m.length;

        for (int i = 0; i < n; i++) {
            // Pivot by max abs element in column i
            int maxRow = i;
            for (int r = i + 1; r < n; r++) {
                if (m[r][i].abs(mc).compareTo(m[maxRow][i].abs(mc)) > 0) {
                    maxRow = r;
                }
            }
            // Swap rows
            BigDecimal[] temp = m[i];
            m[i] = m[maxRow];
            m[maxRow] = temp;

            // Check for zero pivot
            if (m[i][i].compareTo(BigDecimal.ZERO) == 0) {
                throw new ArithmeticException("Matrix is singular.");
            }

            // Normalize pivot row
            BigDecimal pivot = m[i][i];
            for (int c = i; c <= n; c++) {
                m[i][c] = m[i][c].divide(pivot, mc);
            }

            // Eliminate below
            for (int r = i + 1; r < n; r++) {
                BigDecimal f = m[r][i];
                for (int c = i; c <= n; c++) {
                    m[r][c] = m[r][c].subtract(f.multiply(m[i][c], mc), mc);
                }
            }
        }

        // Back substitution
        BigDecimal[] sol = new BigDecimal[n];
        for (int i = n - 1; i >= 0; i--) {
            sol[i] = m[i][n];
            for (int c = i + 1; c < n; c++) {
                sol[i] = sol[i].subtract(m[i][c].multiply(sol[c], mc), mc);
            }
        }
        return sol;
    }
}
