package com.gmware.lib.games.holdem.common;

public abstract class Rnd {

    /**
     * ����� �� ��������� ��� ���� ����������� �������� ��������� �����.
     */
    private static long defaultSeed = 0;

    /**
     * ���������� ����� �� ��������� ��� ���� ����������� �������� ��������� �����.
     * ���� {@code defaultSeed == 0}, �� �������������� ����� ����������� ����������
     * <pre>
     * System.nanoTime() + seedUniquifier
     * </pre>.
     * ����� �������������� ����� ����� ������� �������� {@code defaultSeed}.
     *
     * @param defaultSeed ����� �� ���������.
     */
    public static synchronized void setDefaultSeed(final long defaultSeed) {
        Rnd.defaultSeed = defaultSeed;
    }

    private static long seedUniquifier = 1;
    /**
     * ��������� �������� ���������� �����
     */
    public long a = 0;

    public static final double DIVISOR = 1.0 / 1099511627776.0; // x * DIVISOR = x / 2^40

    public static final long MULTIPLIER = 762939453125L; // x * MULTIPLIER = x * 5^17

    public static final long MODULATOR = 0xFFFFFFFFFFL; // x & MODULATOR = x mod 2^40

    /**
     * @return ����� ��� ����� ������������ ������� ��������� �����.
     */
    public static synchronized long getSeed() {
        if (defaultSeed == 0) {
            final long seed = System.nanoTime() + seedUniquifier;
            seedUniquifier += 2;
            return seed;
        } else {
            return defaultSeed;
        }
    }

    /**
     * ��������� ����� ����������.
     *
     * @param seed ����� ����������.
     */
    public abstract void setSeed(long seed);

    /**
     * �������� ��������� ��������� ����� �� ��������� (0;1).
     *
     * @return ��������� ����� �� ��������� (0;1).
     */
    public abstract double rnd();

    /**
     * �������� ��������� ��������� ����� �� ��������� {0, 1, ..., n-1}.
     *
     * @param n �����, �������� �������� ��������� �����.
     * @return ��������� ����� �� ��������� {0, 1, ..., n-1}.
     */
    public int rnd(final int n) {
        return (int) (n * rnd());
    }

    /**
     * �������� ��������� ��������� �����, ���������� ��������������
     * �� ��������� (a;b).
     *
     * @param a ����� ������� ���������.
     * @param b ������ ������� ���������.
     * @return ��������� �����, ���������� �������������� �� ��������� (a;b).
     */
    public final double rnd(final double a, final double b) {
        return a + (b - a) * rnd();
    }
}
