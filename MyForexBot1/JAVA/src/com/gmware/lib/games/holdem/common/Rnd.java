package com.gmware.lib.games.holdem.common;

public abstract class Rnd {

    /**
     * Зерно по умолчанию для всех создаваемых датчиков случайных чисел.
     */
    private static long defaultSeed = 0;

    /**
     * Установить зерно по умолчанию для всех создаваемых датчиков случайных чисел.
     * Если {@code defaultSeed == 0}, то первоначальное зерно вычисляется выражением
     * <pre>
     * System.nanoTime() + seedUniquifier
     * </pre>.
     * Иначе первоначальное зерно равно данному значению {@code defaultSeed}.
     *
     * @param defaultSeed зерно по умолчанию.
     */
    public static synchronized void setDefaultSeed(final long defaultSeed) {
        Rnd.defaultSeed = defaultSeed;
    }

    private static long seedUniquifier = 1;
    /**
     * Последнее значение случайного числа
     */
    public long a = 0;

    public static final double DIVISOR = 1.0 / 1099511627776.0; // x * DIVISOR = x / 2^40

    public static final long MULTIPLIER = 762939453125L; // x * MULTIPLIER = x * 5^17

    public static final long MODULATOR = 0xFFFFFFFFFFL; // x & MODULATOR = x mod 2^40

    /**
     * @return зерно для вновь создаваемого датчика случайных чисел.
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
     * Установка зерна генератора.
     *
     * @param seed зерно генератора.
     */
    public abstract void setSeed(long seed);

    /**
     * Получить очередное случайное число из интервала (0;1).
     *
     * @return случайное число из интервала (0;1).
     */
    public abstract double rnd();

    /**
     * Получить очередное случайное число из множества {0, 1, ..., n-1}.
     *
     * @param n число, задающее диапазон случайных чисел.
     * @return случайное число из множества {0, 1, ..., n-1}.
     */
    public int rnd(final int n) {
        return (int) (n * rnd());
    }

    /**
     * Получить очередное случайное число, равномерно распределенное
     * на интервале (a;b).
     *
     * @param a левая граница интервала.
     * @param b правая граница интервала.
     * @return случайное число, равномерно распределенное на интервале (a;b).
     */
    public final double rnd(final double a, final double b) {
        return a + (b - a) * rnd();
    }
}
