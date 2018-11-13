package com.gmware.lib.games.holdem.common.rnd;

import com.gmware.lib.games.holdem.common.Rnd;

/**
 * ћультипликативный датчик случайных чисел с множителем {@code 2^40} и
 * модулем {@code 5^17}.
 */
public final class Rnd517 extends Rnd {

    //------------------------------------------------------------------------------------------------------------------

    /**
     * —оздать генератор случайных чисел с зерном {@link Rnd#getSeed()}.
     */
    public Rnd517() {
        this(getSeed());
    }

    /**
     * Ётот конструктор создает генератор с заданным зерном.
     *
     * @param seed зерно генератора.
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
