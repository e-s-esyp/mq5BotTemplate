package com.gmware.lib.games.holdem.common.rnd;

import java.util.Random;

/**
 *  ласс, который используетс€ дл€ генерации случайных чисел в нашем проекте.
 * ѕо умолчанию примен€етс€ класс Random, используемый Java.
 * ќднако, есть возможность использовани€ датчика случайных чисел 5^17,
 * разработанного новосибирскими учеными на ¬÷ —ќ –јЌ.
 * Ёто датчик прост, имеет большой период и легко переноситс€
 * (в отличие от датчика Java) на другие платформы (например, на —и).
 */

public final class RndKnuthStatic {

    private static final Random javaRandom = new Random();

    /**
     * ”становка стартового числа.
     *
     * @param seed начальное значение.
     */
    public static synchronized void setSeed(final long seed) {
        javaRandom.setSeed(seed);
    }

    /**
     * ѕолучить очередное случайное число из интервала (0;1).
     *
     * @return случайное число из интервала (0;1).
     */
    public static synchronized double rnd() {
        return javaRandom.nextDouble();
    }

    /**
     * ѕолучить очередное случайное число из множества {0, 1, ..., n-1}.
     *
     * @param n число, задающее диапазон случайных чисел.
     * @return случайное число из множества {0, 1, ..., n-1}.
     */
    public static synchronized int rnd(final int n) {
        return (int) (n * rnd());
    }

    /**
     * ѕолучить очередное случайное число, равномерно распределенное
     * на интервале (a;b).
     *
     * @param a лева€ граница интервала.
     * @param b права€ граница интервала.
     * @return случайное число, равномерно распределенное на интервале (a;b).
     */
    public static synchronized double rnd(final double a, final double b) {
        return a + (b - a) * rnd();
    }
}
