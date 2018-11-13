package com.gmware.lib.games.holdem.common.rnd;

import java.util.Random;

/**
 * �����, ������� ������������ ��� ��������� ��������� ����� � ����� �������.
 * �� ��������� ����������� ����� Random, ������������ Java.
 * ������, ���� ����������� ������������� ������� ��������� ����� 5^17,
 * �������������� �������������� ������� �� �� �� ���.
 * ��� ������ �����, ����� ������� ������ � ����� �����������
 * (� ������� �� ������� Java) �� ������ ��������� (��������, �� ��).
 */

public final class RndKnuthStatic {

    private static final Random javaRandom = new Random();

    /**
     * ��������� ���������� �����.
     *
     * @param seed ��������� ��������.
     */
    public static synchronized void setSeed(final long seed) {
        javaRandom.setSeed(seed);
    }

    /**
     * �������� ��������� ��������� ����� �� ��������� (0;1).
     *
     * @return ��������� ����� �� ��������� (0;1).
     */
    public static synchronized double rnd() {
        return javaRandom.nextDouble();
    }

    /**
     * �������� ��������� ��������� ����� �� ��������� {0, 1, ..., n-1}.
     *
     * @param n �����, �������� �������� ��������� �����.
     * @return ��������� ����� �� ��������� {0, 1, ..., n-1}.
     */
    public static synchronized int rnd(final int n) {
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
    public static synchronized double rnd(final double a, final double b) {
        return a + (b - a) * rnd();
    }
}
