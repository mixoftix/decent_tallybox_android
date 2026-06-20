package net.mixoftix.tallybox;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class pqc_mlkem {

    // Variables
    static boolean USE_NTT = false; // Toggle NTT (true) or naive multiplication (false)
    static final int K = 2;
    static final int ETA2 = 2;
    static final int N = 256;
    static final int Q_KEM = 3329;
    static final int DU = 10;
    static final int DV = 4;

    // Main functions
    static byte[] shake128(byte[] input, int outputLength) {
        Keccak k = new Keccak(256);
        k.absorb(input);
        k.padAndSqueeze(0x1F);
        return k.squeeze(outputLength);
    }
    static byte[] shake256(byte[] input, int outputLength) {
        Keccak k = new Keccak(512);
        k.absorb(input);
        k.padAndSqueeze(0x1F);
        return k.squeeze(outputLength);
    }
    static byte[] sha3_256(byte[] input) {
        Keccak k = new Keccak(512);
        k.absorb(input);
        k.padAndSqueeze(0x06);
        return k.squeeze(32);
    }
    static byte[] sha3_512(byte[] input) {
        Keccak k = new Keccak(1024);
        k.absorb(input);
        k.padAndSqueeze(0x06);
        return k.squeeze(64);
    }

    static class Keccak {
        private static final int[] R = {
                0, 1, 62, 28, 27, 36, 44, 6, 55, 20, 3, 10, 43, 25, 39,
                41, 45, 15, 21, 8, 18, 2, 61, 56, 14
        };
        private static final long[] RC = {
                0x0000000000000001L, 0x0000000000008082L, 0x800000000000808AL,
                0x8000000080008000L, 0x000000000000808BL, 0x0000000080000001L,
                0x8000000080008081L, 0x8000000000008009L, 0x000000000000008AL,
                0x0000000000000088L, 0x0000000080008009L, 0x000000008000000AL,
                0x000000008000808BL, 0x800000000000008BL, 0x8000000000008089L,
                0x8000000000008003L, 0x8000000000008002L, 0x8000000000000080L,
                0x000000000000800AL, 0x800000008000000AL, 0x8000000080008081L,
                0x8000000000008080L, 0x0000000080000001L, 0x8000000080008008L
        };
        private long[] state;
        private int rate;
        private byte[] buffer;
        private int pos;

        Keccak(int capacity) {
            this.state = new long[25];
            this.rate = (1600 - capacity) / 8;
            this.buffer = new byte[rate];
            this.pos = 0;
        }

        void absorb(byte[] input) {
            for (byte b : input) {
                buffer[pos++] = b;
                if (pos == rate) {
                    keccakAbsorb();
                    pos = 0;
                }
            }
        }

        void padAndSqueeze(int d) {
            buffer[pos++] = (byte) d;
            while (pos < rate) {
                buffer[pos++] = 0;
            }
            buffer[rate - 1] |= (byte) 0x80;
            keccakAbsorb();
            pos = 0;
        }

        byte[] squeeze(int outputLength) {
            byte[] output = new byte[outputLength];
            int outPos = 0;
            while (outPos < outputLength) {
                if (pos == rate) {
                    keccakF();
                    pos = 0;
                }
                int toCopy = Math.min(rate - pos, outputLength - outPos);
                System.arraycopy(toBytes(state), pos, output, outPos, toCopy);
                pos += toCopy;
                outPos += toCopy;
            }
            return output;
        }

        private void keccakAbsorb() {
            byte[] stateBytes = toBytes(state);
            for (int i = 0; i < rate; i++) {
                stateBytes[i] ^= buffer[i];
            }
            state = fromBytes(stateBytes);
            keccakF();
        }

        private void keccakF() {
            long[] a = state.clone();
            for (int round = 0; round < 24; round++) {
                long[] c = new long[5];
                for (int x = 0; x < 5; x++) {
                    c[x] = a[x] ^ a[x + 5] ^ a[x + 10] ^ a[x + 15] ^ a[x + 20];
                }
                for (int x = 0; x < 5; x++) {
                    long d = c[(x + 4) % 5] ^ Long.rotateLeft(c[(x + 1) % 5], 1);
                    for (int y = 0; y < 25; y += 5) {
                        a[x + y] ^= d;
                    }
                }
                long[] b = new long[25];
                for (int i = 0; i < 25; i++) {
                    int x = i % 5;
                    int y = i / 5;
                    b[(2 * x + 3 * y) % 5 + 5 * ((2 * x + 3 * y) / 5)] = Long.rotateLeft(a[i], R[i]);
                }
                for (int y = 0; y < 25; y += 5) {
                    for (int x = 0; x < 5; x++) {
                        a[x + y] = b[x + y] ^ ((~b[(x + 1) % 5 + y]) & b[(x + 2) % 5 + y]);
                    }
                }
                a[0] ^= RC[round];
            }
            state = a;
        }

        private byte[] toBytes(long[] s) {
            byte[] result = new byte[200];
            for (int i = 0; i < 25; i++) {
                for (int j = 0; j < 8; j++) {
                    result[i * 8 + j] = (byte) (s[i] >>> (j * 8));
                }
            }
            return result;
        }

        private long[] fromBytes(byte[] bytes) {
            long[] result = new long[25];
            for (int i = 0; i < 25; i++) {
                long val = 0;
                for (int j = 0; j < 8; j++) {
                    val |= ((long) (bytes[i * 8 + j] & 0xFF)) << (j * 8);
                }
                result[i] = val;
            }
            return result;
        }
    }
    static class Polynomial {
        int[] coeffs;
        int q;
        static final int N = 256;
        static final int Q = 3329;
        static final int INV_N = 3303;

        static final int[] ZETAS = {
                2285, 2571, 2970, 1812, 1493, 1422, 287,  202,  3158, 622,  1577, 182,  962,  2127, 1855, 1468,
                573,  2004, 264,  383,  2500, 1458, 1727, 3199, 2648, 1017, 732,  608,  1787, 411,  3124, 1758,
                1223, 652,  2777, 1015, 2036, 1491, 3047, 1785, 516,  3321, 3009, 2663, 1711, 2167, 126,  1469,
                2476, 3239, 3058, 830,  107,  1908, 3082, 2378, 2931, 961,  1821, 2604, 448,  2264, 677,  2054,
                2226, 430,  555,  843,  2078, 871,  1550, 105,  422,  587,  177,  3094, 3038, 2869, 1574, 1653,
                3083, 778,  1159, 3182, 2552, 1483, 2727, 1119, 1739, 644,  2457, 349,  418,  329,  3173, 3254,
                817,  1097, 603,  610,  1322, 2044, 1864, 384,  2114, 3193, 1218, 1994, 2455, 220,  2142, 1670,
                2144, 1799, 2051, 794,  1819, 2475, 2459, 478,  3221, 3021, 996,  991,  958,  1869, 1522, 1628
        };


        // Reverse ZETAS_INV to match Kyber's layer order
        static final int[] ZETAS_INV = {
                1694, 1027, 1206, 1479, 1865, 2837, 443,  2675, 1513, 117,  1806, 1457, 2031, 1936, 774,  1256,
                2649, 795,  1538, 1844, 1782, 1018, 2648, 862,  1694, 1027, 1206, 1479, 1865, 2837, 443,  2675,
                1513, 117,  1806, 1457, 2031, 1936, 774,  1256, 2649, 795,  1538, 1844, 1782, 1018, 2648, 862,
                1694, 1027, 1206, 1479, 1865, 2837, 443,  2675, 1513, 117,  1806, 1457, 2031, 1936, 774,  1256,
                862,  2648, 1018, 1782, 1844, 1538, 795,  2649, 1256, 774,  1936, 2031, 1457, 1806, 117,  1513,
                2675, 443,  2837, 1865, 1479, 1206, 1027, 1694, 862,  2648, 1018, 1782, 1844, 1538, 795,  2649,
                1256, 627,  3171, 2385, 784,  2045, 202,  811,  2039, 2807, 2987, 1902, 2126, 1156, 767,  2119,
                1189, 1355, 1530, 1278, 2535, 1510, 854,  870,  2851, 108,  308,  2333, 2338, 2371, 1460, 1807,
                1701
        };

        Polynomial(int q) {
            this.q = q;
            coeffs = new int[N];
        }

        Polynomial(int[] coeffs, int q) {
            if (coeffs.length != N) {
                throw new IllegalArgumentException("Coefficient array must have length " + N);
            }
            this.q = q;
            this.coeffs = coeffs.clone();
        }

        static int mod(int a, int q) {
            a %= q;
            return a < 0 ? a + q : a;
        }

        // Bit-reversal table for N=256
        static final int[] BIT_REVERSE = new int[N];
        static {
            for (int i = 0; i < N; i++) {
                BIT_REVERSE[i] = (Integer.reverse(i) >>> 24); // Reverse 8 bits
            }
        }

        Polynomial intt() {
            int[] temp = new int[N];
            for (int i = 0; i < N; i++) {
                temp[i] = coeffs[BIT_REVERSE[i]];
            }
            int k = 0;
            for (int len = N; len >= 2; len >>= 1) {
                int groups = N / (2 * len);
                for (int g = 0; g < groups; g++) {
                    int start = g * len * 2;
                    int zeta = ZETAS_INV[k++];
                    Access_log.log_it("w", "shahin", "INTT k=" + (k-1) + ", zeta=" + zeta + ", len=" + len + ", start=" + start);
                    for (int j = start; j < start + len / 2; j++) {
                        int t = temp[j];
                        int u = temp[j + len / 2];
                        temp[j] = mod(t + u, q);
                        int diff = mod(t - u + q, q);
                        temp[j + len/2] = mod((int) ((long) zeta * diff % q), q);
                    }
                }
                Access_log.log_it("w", "shahin", "INTT after len=" + len + ": " + new Polynomial(temp, q).toDebugString(5));
            }
            int[] result = new int[N];
            for (int i = 0; i < N; i++) {
                result[i] = mod((int) ((long) temp[i] * INV_N % q), q);
            }
            Access_log.log_it("w", "shahin", "INTT before scaling: " + new Polynomial(temp, q).toDebugString(5));
            return new Polynomial(result, q);
        }

        Polynomial ntt() {
            if (coeffs.length != N) {
                throw new IllegalStateException("coeffs.length=" + coeffs.length + " but N=" + N);
            }
            int[] temp = coeffs.clone();
            int k_start = 0; // Starting index for each layer
            for (int len = 2; len <= N; len <<= 1) {
                int halfLen = len / 2;
                //int groups = N / len;
                int groups = N / (len * 2);
                //k_start += groups; // Move to next layer’s zetas
                Access_log.log_it("w", "shahin", "bgn of loop k_resetter: len=" + len + " groups=" + groups + " k_start=" + k_start);
                int k = k_start; // Reset to layer start

                for (int g = 0; g < groups; g++) {
                    int start = g * len;
                    if (k >= ZETAS.length) {
                        Access_log.log_it("w", "shahin", "ERROR: k=" + k + " >= ZETAS.length=" + ZETAS.length);
                        Access_log.log_it("w", "shahin", "ERROR: g=" + g + " groups=" + groups);
                        Access_log.log_it("w", "shahin", "ERROR: len=" + len + " k_start=" + k_start);

                        throw new ArrayIndexOutOfBoundsException("Zeta index out of bounds");
                    }
                    int zeta = ZETAS[k];
                    Access_log.log_it("w", "shahin", "NTT k=" + k + ", zeta=" + zeta + ", len=" + len + ", start=" + start);
                    k++;
                    for (int j = start; j < start + halfLen; j++) {
                        int t = temp[j];
                        int u = temp[j + halfLen];
                        temp[j] = mod(t + u, q);
                        temp[j + halfLen] = mod((int) ((long) zeta * (t - u + q) % q), q);
                    }
                }
                Access_log.log_it("w", "shahin", "NTT after len=" + len + ": " + new Polynomial(temp, q).toDebugString(5));
                k_start += groups; // Move to next layer’s zetas
                Access_log.log_it("w", "shahin", "end of loop k_resetter: len=" + len + " k_start=" + k_start);
            }
            int[] result = new int[N];
            for (int i = 0; i < N; i++) {
                result[i] = temp[BIT_REVERSE[i]];
            }
            Access_log.log_it("w", "shahin", "NTT final: " + new Polynomial(result, q).toDebugString(5));
            return new Polynomial(result, q);
        }

        Polynomial multiply(Polynomial other) {
            if (USE_NTT) {
                Polynomial a_ntt = this.ntt();
                Polynomial b_ntt = other.ntt();
                Access_log.log_it("w", "shahin", "a_ntt: " + a_ntt.toDebugString(5));
                Access_log.log_it("w", "shahin", "b_ntt: " + b_ntt.toDebugString(5));
                Polynomial c = new Polynomial(q);
                for (int i = 0; i < N; i++) {
                    c.coeffs[i] = mod((int) ((long) a_ntt.coeffs[i] * b_ntt.coeffs[i] % q), q);
                }
                Access_log.log_it("w", "shahin", "c_ntt: " + c.toDebugString(5));
                Polynomial result = c.intt();
                Access_log.log_it("w", "shahin", "result_ntt: " + result.toDebugString(5));
                Polynomial naive_check = new Polynomial(q);
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        int k_idx = (i + j) % N;
                        long prod = (long) coeffs[i] * other.coeffs[j];
                        if (i + j >= N) {
                            naive_check.coeffs[k_idx] = mod(naive_check.coeffs[k_idx] - (int) (prod % q), q);
                        } else {
                            naive_check.coeffs[k_idx] = mod(naive_check.coeffs[k_idx] + (int) (prod % q), q);
                        }
                    }
                }
                Access_log.log_it("w", "shahin", "result_naive: " + naive_check.toDebugString(5));
                return result;
            } else {
                Polynomial result = new Polynomial(q);
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        int k_idx = (i + j) % N;
                        long prod = (long) coeffs[i] * other.coeffs[j];
                        if (i + j >= N) {
                            result.coeffs[k_idx] = mod(result.coeffs[k_idx] - (int) (prod % q), q);
                        } else {
                            result.coeffs[k_idx] = mod(result.coeffs[k_idx] + (int) (prod % q), q);
                        }
                    }
                }
                //Access_log.log_it("w", "shahin", "result_naive: " + result.toDebugString(5));
                return result;
            }
        }

        Polynomial add(Polynomial other) {
            Polynomial result = new Polynomial(q);
            for (int i = 0; i < N; i++) {
                result.coeffs[i] = mod(coeffs[i] + other.coeffs[i], q);
            }
            return result;
        }

        Polynomial subtract(Polynomial other) {
            Polynomial result = new Polynomial(q);
            for (int i = 0; i < N; i++) {
                result.coeffs[i] = mod(coeffs[i] - other.coeffs[i], q);
            }
            return result;
        }

        byte[] compress(int d) {
            byte[] result = new byte[(N * d + 7) / 8];
            int bitPos = 0;
            for (int i = 0; i < N; i++) {
                int c = (int) Math.round((double) (1 << d) / Q * coeffs[i]) % (1 << d);
                for (int j = 0; j < d; j++) {
                    if (bitPos / 8 < result.length) {
                        result[bitPos / 8] |= (byte) (((c >> j) & 1) << (bitPos % 8));
                    }
                    bitPos++;
                }
            }
            return result;
        }

        static Polynomial decompress(byte[] compressed, int d, int q) {
            Polynomial p = new Polynomial(q);
            int bitPos = 0;
            for (int i = 0; i < N; i++) {
                int c = 0;
                for (int j = 0; j < d; j++) {
                    if (bitPos / 8 < compressed.length) {
                        c |= ((compressed[bitPos / 8] >> (bitPos % 8)) & 1) << j;
                    }
                    bitPos++;
                }
                p.coeffs[i] = (int) Math.round((double) Q / (1 << d) * c);
            }
            return p;
        }

        String toDebugString(int limit) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < Math.min(N, limit); i++) {
                sb.append(coeffs[i]);
                if (i < Math.min(N, limit) - 1) sb.append(", ");
            }
            if (N > limit) sb.append("...");
            sb.append("]");
            return sb.toString();
        }



        void testNTT() {
            Access_log.log_it("w", "shahin", "coeffs.length=" + coeffs.length + ", N=" + N);
            coeffs[0] = 1;
            coeffs[1] = 1;
            Access_log.log_it("w", "shahin", "Input: " + toDebugString(5));
            Polynomial ntt = this.ntt();
            Access_log.log_it("w", "shahin", "NTT: " + ntt.toDebugString(5));
            Polynomial intt = ntt.intt();
            Access_log.log_it("w", "shahin", "iNTT: " + intt.toDebugString(5));
            Polynomial naive = new Polynomial(q);
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    int k_idx = (i + j) % N;
                    long prod = (long) coeffs[i] * coeffs[j];
                    naive.coeffs[k_idx] = mod(naive.coeffs[k_idx] + (int) (prod % q), q);
                }
            }
            Access_log.log_it("w", "shahin", "Naive self-multiply: " + naive.toDebugString(5));
            Polynomial a_ntt = this.ntt();
            Access_log.log_it("w", "shahin", "a_ntt: " + a_ntt.toDebugString(5));
            Polynomial b_ntt = this.ntt();
            Access_log.log_it("w", "shahin", "b_ntt: " + b_ntt.toDebugString(5));
            Polynomial c = new Polynomial(q);
            for (int i = 0; i < N; i++) {
                c.coeffs[i] = mod((int) ((long) a_ntt.coeffs[i] * b_ntt.coeffs[i] % q), q);
            }
            Access_log.log_it("w", "shahin", "NTT pointwise product: " + c.toDebugString(5));
            Polynomial ntt_mult = c.intt();
            Access_log.log_it("w", "shahin", "NTT self-multiply: " + ntt_mult.toDebugString(5));
        }

    }
    static class Ciphertext {
        byte[] compressed;

        Ciphertext(byte[] compressed) {
            this.compressed = compressed;
        }

        Polynomial[] getU() {
            Polynomial[] u = new Polynomial[K];
            for (int i = 0; i < K; i++) {
                byte[] uBytes = new byte[320];
                System.arraycopy(compressed, i * 320, uBytes, 0, 320);
                u[i] = Polynomial.decompress(uBytes, DU, Q_KEM);
            }
            return u;
        }

        Polynomial getV() {
            byte[] vBytes = new byte[128];
            System.arraycopy(compressed, K * 320, vBytes, 0, 128);
            return Polynomial.decompress(vBytes, DV, Q_KEM);
        }
    }

    static Polynomial sampleCBD(int eta, byte[] seed, int offset) {
        Polynomial p = new Polynomial(Q_KEM);
        byte[] expanded = shake256(concat(seed, new byte[]{(byte) offset}), 64 * eta);
        int bitIndex = 0;
        for (int i = 0; i < N; i++) {
            int a = 0, b = 0;
            for (int j = 0; j < eta; j++) {
                a += (expanded[bitIndex / 8] >> (bitIndex % 8)) & 1;
                bitIndex++;
                b += (expanded[bitIndex / 8] >> (bitIndex % 8)) & 1;
                bitIndex++;
            }
            p.coeffs[i] = a - b;
        }
        return p;
    }
    static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    static Polynomial[] generateA(byte[] rho) {
        Polynomial[] A = new Polynomial[K * K];
        for (int i = 0; i < K * K; i++) {
            A[i] = new Polynomial(Q_KEM);
            byte[] seed = concat(rho, new byte[]{(byte) i});
            byte[] bytes = shake128(seed, N * 2);
            for (int j = 0; j < N; j++) {
                int val = (bytes[j * 2] & 0xFF) | ((bytes[j * 2 + 1] & 0xFF) << 8);
                A[i].coeffs[j] = val % Q_KEM;
            }
        }
        return A;
    }
    static Polynomial encodeMessage(byte[] m) {
        Polynomial p = new Polynomial(Q_KEM);
        for (int i = 0; i < N; i++) {
            int bit = (m[i / 8] >> (i % 8)) & 1;
            p.coeffs[i] = bit * (Q_KEM / 2);
        }
        return p;
    }

    // Encapsulation functions
    static class EncapsulationResult {
        Ciphertext ct;
        byte[] key;
        EncapsulationResult(Ciphertext ct, byte[] key) {
            this.ct = ct;
            this.key = key;
        }
    }
    static EncapsulationResult encapsulate(byte[] pk) {
        // Rho is the last 32 bytes of pk (public key format: t[0..K-1] compressed + rho)
        byte[] rho = new byte[32];
        System.arraycopy(pk, pk.length - 32, rho, 0, 32);

        byte[] m = null;
        try {
            SecureRandom rand = new SecureRandom();
            byte[] randBytes = new byte[32];
            rand.nextBytes(randBytes);
            //m = sha3_256(hexToBytes("499602d2")); // debug only
            m = sha3_256(randBytes);
        } catch (Exception e) {
            Access_log.log_it("e", "shahin", "Error generating random m: " + e.getMessage());
            throw e;
        }

        Access_log.log_it("e", "shahin", "Original m (hex): " + bytesToHex(m));
        byte[] seed = sha3_256(concat(m, sha3_256(pk)));
        Polynomial[] A = generateA(rho);
        Polynomial mPoly = encodeMessage(m);
        Polynomial[] r = new Polynomial[K];
        Polynomial[] e1 = new Polynomial[K];
        Polynomial e2 = new Polynomial(Q_KEM);
        for (int i = 0; i < K; i++) {
            r[i] = sampleCBD(ETA2, seed, i);
            e1[i] = sampleCBD(ETA2, seed, K + i);
        }
        e2 = sampleCBD(ETA2, seed, 2 * K);

        Polynomial[] t = new Polynomial[K];
        for (int i = 0; i < K; i++) {
            byte[] tBytes = new byte[320];
            System.arraycopy(pk, i * 320, tBytes, 0, 320);
            t[i] = Polynomial.decompress(tBytes, 10, Q_KEM);
        }

        Polynomial[] u = new Polynomial[K];
        for (int i = 0; i < K; i++) {
            u[i] = new Polynomial(Q_KEM);
            for (int j = 0; j < K; j++) {
                u[i] = u[i].add(A[j * K + i].multiply(r[j]));
            }
            u[i] = u[i].add(e1[i]);
        }
        Polynomial v = new Polynomial(Q_KEM);
        for (int i = 0; i < K; i++) {
            v = v.add(t[i].multiply(r[i]));
        }
        v = v.add(e2).add(mPoly);

        Access_log.log_it("w", "shahin", "u[0] before: " + u[0].toDebugString(5));
        Access_log.log_it("w", "shahin", "v before: " + v.toDebugString(5));

        byte[] ctCompressed = new byte[K * 320 + 128];
        for (int i = 0; i < K; i++) {
            byte[] uCompressed = u[i].compress(DU);
            System.arraycopy(uCompressed, 0, ctCompressed, i * 320, 320);
        }
        byte[] vCompressed = v.compress(DV);
        System.arraycopy(vCompressed, 0, ctCompressed, K * 320, 128);
        Ciphertext ct = new Ciphertext(ctCompressed);

        byte[] key = sha3_512(concat(sha3_256(m), sha3_256(ctCompressed)));
        return new EncapsulationResult(ct, key);
    }

    // HKDF implementation
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int SHA256_LENGTH = 32;
    private static byte[] hkdfExtract(byte[] ikm, byte[] salt) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            byte[] effectiveSalt = (salt != null && salt.length > 0) ? salt : new byte[SHA256_LENGTH];
            SecretKeySpec keySpec = new SecretKeySpec(effectiveSalt, HMAC_ALGORITHM);
            mac.init(keySpec);
            return mac.doFinal(ikm);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Access_log.log_it("e", "shahin", "HKDF Extract failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    private static byte[] hkdfExpand(byte[] prk, byte[] info, int outputLength) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(prk, HMAC_ALGORITHM);
            mac.init(keySpec);

            int iterations = (outputLength + SHA256_LENGTH - 1) / SHA256_LENGTH;
            byte[] result = new byte[outputLength];
            byte[] t = new byte[0];

            for (int i = 0; i < iterations; i++) {
                mac.reset();
                mac.update(t);
                if (info != null) {
                    mac.update(info);
                }
                mac.update((byte) (i + 1));
                t = mac.doFinal();
                int copyLength = Math.min(SHA256_LENGTH, outputLength - i * SHA256_LENGTH);
                System.arraycopy(t, 0, result, i * SHA256_LENGTH, copyLength);
            }

            return result;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Access_log.log_it("e", "shahin", "HKDF Expand failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    static byte[] deriveSessionKey(byte[] key, String salt_str) {
        // Derive a 32-byte session key from the 64-byte ML-KEM key
        byte[] salt = salt_str.getBytes(); // null; // Optional, could use sha3_256(pk) if desired
        byte[] info = "session_key".getBytes(); // Context for derivation
        return hkdfExpand(hkdfExtract(key, salt), info, 32);
    }

    // Helper functions
    static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    static String bytesToBase64(byte[] bytes) {
        // this is internally ASCII charset
        return Base64.getEncoder().encodeToString(bytes);
    }

    // Sample usage
    public static void pqc_psk_pk()
    {
        Access_log.log_it("w", "shahin", "\n\r");
        Access_log.log_it("w", "shahin", "Client (Java):");

        byte[] pk = Base64.getDecoder().decode(MainActivity.app_pqc_pk);
        EncapsulationResult encResult = encapsulate(pk);
        Ciphertext ct = encResult.ct;
        String cipher_base64 = bytesToBase64(ct.compressed);

        MainActivity.app_pqc_psk = bytesToHex(deriveSessionKey(encResult.key, MainActivity.wallet_address));
        MainActivity.app_pqc_psk_cipher = cipher_base64;

        Access_log.log_it("w", "shahin", "Compressed Ciphertext (Base64): ");
        Access_log.log_it("w", "shahin", MainActivity.app_pqc_psk_cipher);
        Access_log.log_it("w", "shahin", "Encapsulation Key (Base64): " + bytesToBase64(encResult.key));
        Access_log.log_it("w", "shahin", "PSK (hex): " + MainActivity.app_pqc_psk);
        Access_log.log_it("w", "shahin", "PSK (Base64): " + bytesToBase64(deriveSessionKey(encResult.key, MainActivity.wallet_address)));

    }

}