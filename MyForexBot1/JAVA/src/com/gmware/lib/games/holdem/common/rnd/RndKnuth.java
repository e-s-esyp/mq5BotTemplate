package com.gmware.lib.games.holdem.common.rnd;

import com.gmware.lib.games.holdem.common.Rnd;

import java.util.Random;

/**
 * ����������� java-������.
 */
public final class RndKnuth extends Rnd {

    private final Random random;

    /**
     * ������� ��������� ��������� ����� � ������ {@link Rnd#getSeed()}.
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
