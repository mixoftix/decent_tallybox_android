package net.mixoftix.tallybox;
public class hash_shake256 {
    private static final int[] KECCAK_ROUND_CONSTANTS = {
            0x00000001, 0x00008082, 0x0000808a, 0x80008000, 0x0000808b, 0x80000001,
            0x80008081, 0x00008009, 0x0000008a, 0x00000088, 0x80008009, 0x8000000a,
            0x8000808b, 0x0000008b, 0x00008089, 0x00008003, 0x00008002, 0x00000080,
            0x0000800a, 0x8000000a, 0x80008081, 0x00008080, 0x80000001, 0x80008008
    };

    private static final int KECCAK_LANE_SIZE = 64;
    private static final int KECCAK_STATE_SIZE = 25;
    private static final int KECCAK_RATE = 136;
    private static final int KECCAK_CAPACITY = 32;

    private long[] state;
    private byte[] buffer;
    private int bufferPos;

    public hash_shake256() {
        state = new long[KECCAK_STATE_SIZE];
        buffer = new byte[KECCAK_RATE];
        bufferPos = 0;
    }

    public void update(byte[] input) {
        update(input, 0, input.length);
    }

    public void update(byte[] input, int offset, int length) {
        while (length > 0) {
            int toCopy = Math.min(KECCAK_RATE - bufferPos, length);
            System.arraycopy(input, offset, buffer, bufferPos, toCopy);
            bufferPos += toCopy;
            offset += toCopy;
            length -= toCopy;

            if (bufferPos == KECCAK_RATE) {
                absorb();
                bufferPos = 0;
            }
        }
    }

    public byte[] digest(int outputLength) {
        // Padding
        buffer[bufferPos] = 0x1F;
        bufferPos++;
        if (bufferPos == KECCAK_RATE) {
            absorb();
            bufferPos = 0;
        }
        buffer[bufferPos] = (byte) 0x80;
        bufferPos++;
        if (bufferPos == KECCAK_RATE) {
            absorb();
            bufferPos = 0;
        }

        // Final absorb
        absorb();

        // Squeeze
        byte[] output = new byte[outputLength];
        int outputPos = 0;
        while (outputPos < outputLength) {
            int toCopy = Math.min(KECCAK_RATE, outputLength - outputPos);
            System.arraycopy(buffer, 0, output, outputPos, toCopy);
            outputPos += toCopy;
            if (outputPos < outputLength) {
                keccakF();
            }
        }

        return output;
    }

    private void absorb() {
        for (int i = 0; i < KECCAK_RATE / 8; i++) {
            state[i] ^= bytesToLong(buffer, i * 8);
        }
        keccakF();
    }

    private void keccakF() {
        long[] lanes = new long[KECCAK_LANE_SIZE];
        for (int round = 0; round < 24; round++) {
            theta(lanes);
            rhoPi(lanes);
            chi(lanes);
            iota(lanes, round);
        }
    }

    private void theta(long[] lanes) {
        long[] c = new long[5];
        for (int x = 0; x < 5; x++) {
            c[x] = state[x] ^ state[x + 5] ^ state[x + 10] ^ state[x + 15] ^ state[x + 20];
        }
        for (int x = 0; x < 5; x++) {
            long d = c[(x + 4) % 5] ^ Long.rotateLeft(c[(x + 1) % 5], 1);
            for (int y = 0; y < 5; y++) {
                state[x + 5 * y] ^= d;
            }
        }
    }

    private void rhoPi(long[] lanes) {
        long temp = state[1];
        for (int t = 0; t < 24; t++) {
            int x = 1;
            int y = 0;
            lanes[t] = state[x + 5 * y];
            state[x + 5 * y] = Long.rotateLeft(temp, (t + 1) * (t + 2) / 2);
            temp = lanes[t];
        }
    }

    private void chi(long[] lanes) {
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                lanes[x + 5 * y] = state[x + 5 * y] ^ ((~state[(x + 1) % 5 + 5 * y]) & state[(x + 2) % 5 + 5 * y]);
            }
        }
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                state[x + 5 * y] = lanes[x + 5 * y];
            }
        }
    }

    private void iota(long[] lanes, int round) {
        state[0] ^= KECCAK_ROUND_CONSTANTS[round];
    }

    private long bytesToLong(byte[] bytes, int offset) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result |= (bytes[offset + i] & 0xFFL) << (8 * i);
        }
        return result;
    }

    public static void test(String input_str) {
        hash_shake256 shake256 = new hash_shake256();
        byte[] input = input_str.getBytes();
        shake256.update(input);
        byte[] output = shake256.digest(32); // 32 bytes output
        Access_log.log_it("w", "shahin", "shake256_new(hex): " + bytesToHex(output));
        /*
        for (byte b : output) {
            System.out.printf("%02x", b);
        }
        */
    }

    static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


}