package com.gmware.lib.games.holdem.common.rnd;

import com.gmware.lib.games.holdem.common.Rnd;

/**
 * ����������������� ������ ��������� ����� � ���������� {@code 2^40} �
 * ������� {@code 5^17}.
 */
public final class Rnd517 extends Rnd {

    //------------------------------------------------------------------------------------------------------------------

    /**
     * ������� ��������� ��������� ����� � ������ {@link Rnd#getSeed()}.
     */
    public Rnd517() {
        this(getSeed());
    }

    /**
     * ���� ����������� ������� ��������� � �������� ������.
     *
     * @param seed ����� ����������.
     */
    public Rnd517(final long seed) {
        a = (((seed << 1) & MODULATOR) | 1L);
    }

    @Override
    public void setSeed(final long seed) {
        a = (((seed << 1) & MODULATOR) | 1L);
    }

    @Override
    public double rnd() {
        a *= MULTIPLIER; // *5^17
        a &= MODULATOR; // mod 2^40
        return DIVISOR * a;
    }

    @Override
    public int rnd(final int n) {
        a *= MULTIPLIER; // *5^17
        a &= MODULATOR; // mod 2^40
        return (int) (n * DIVISOR * a);
    }
}
