package com.gmware.lib.games.holdem.common.rnd;

import com.gmware.lib.games.holdem.common.Rnd;

import java.util.Random;

/**
 * Стандартный java-датчик.
 */
public final class RndKnuth extends Rnd {

    private final Random random;

    /**
     * Создать генератор случайных чисел с зерном {@link Rnd#getSeed()}.
     */
    public RndKnuth() {
        random = new Random(Rnd.getSeed());
    }

    @Override
    public void setSeed(final long seed) {
        random.setSeed(seed);
    }

    @Override
    public double rnd() {
        return random.nextDouble();
    }
}
