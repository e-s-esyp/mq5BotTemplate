package com.gmware.lib.games.holdem.common.rnd;


import com.gmware.lib.games.holdem.common.Rnd;

public final class StaticRnd517 extends Rnd {
    private static double rnds[] = null;
    private static int counter = -1;

    private static int rnds1326[] = null;
    private static int counter1326 = -1;

    private static int rnds52[] = null;
    private static int counter52 = -1;

    public static synchronized void staticInit(final long seed, final int length) {
        if (rnds == null) {
            rnds = new double[length];
            rnds1326 = new int[length];
            rnds52 = new int[length];
            final Rnd517 rnd517 = new Rnd517(seed);
            for (int i = 0; i < length; i++) {
                rnds[i] = rnd517.rnd();
                rnds1326[i] = (int) (1326 * rnd517.rnd());
                rnds52[i] = (int) (52 * rnd517.rnd());
            }
        }
    }

    @Override
    public void setSeed(final long seed) {

    }

    @Override
    public final double rnd() {
        try { // try-catch в тестах быстрее на 3% (при 20 млн.), чем if-else. И он безопаснее в мультипоточности.
            return rnds[++counter];
        } catch (final ArrayIndexOutOfBoundsException e) {
            return rnds[counter = 0];
        }
        /*if (counter < rnds.length - 1) {
            return rnds[++counter];
        } else {
            return rnds[counter = 0];
        } */
    }

    @Override
    public final int rnd(final int n) {
        switch (n) {
            case 1326:
                try {
                    return rnds1326[++counter1326];
                } catch (final ArrayIndexOutOfBoundsException e) {
                    return rnds1326[counter1326 = 0];
                }
            case 52:
                try {
                    return rnds52[++counter52];
                } catch (final ArrayIndexOutOfBoundsException e) {
                    return rnds52[counter52 = 0];
                }
            default:
                return (int) (n * rnd());
        }
    }
}
