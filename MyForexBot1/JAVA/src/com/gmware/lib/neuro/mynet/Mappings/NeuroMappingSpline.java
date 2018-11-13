package com.gmware.lib.neuro.mynet.Mappings;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapSpline;
import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.mynet.NeuroMapping;
import com.gmware.lib.neuro.NetImage;

import java.io.PrintStream;

/**
 * Поиск сплайнового отображения.
 * <p/>
 * Created by Gauss on 16.02.2016.
 */
public class NeuroMappingSpline extends NeuroMapping {
    @Override
    protected NeuroMap trainMap(final NetImage[] images,
                                final NeuroMapType mapType,
                                final Rnd rnd,
                                final PrintStream log,
                                final String netOutPath) {
        return train(images, mapType, rnd, log, netOutPath);
    }

    public static NeuroMap train(final NetImage[] images,
                                 final NeuroMapType mapType,
                                 final Rnd rnd,
                                 final PrintStream log,
                                 final String netOutPath) {
        final NeuroMapSpline map = new NeuroMapSpline();
        if (images == null) return null;
        if (images.length == 0) return map;
        final double[] in = new double[images.length];
        for (int i = 0; i < images.length; ++i) {
            in[i] = images[i].in[0];
        }
        map.structure = trainStructure(images, in, 0, images.length, log);
        log.println(map.structure.toString(1));
        return map;
    }

    private static NeuroMapSpline.SplineStructure trainStructure(final NetImage[] images,
                                                                 final double[] in,
                                                                 final int limit1,
                                                                 final int limit2,
                                                                 final PrintStream log) {
        System.out.println("[" + limit1 + "," + limit2 + "]   " +
                "[" + in[limit1] + "," + in[limit2 - 1] + "]   ");
        final NeuroMapSpline.SplineStructure s = new NeuroMapSpline.SplineStructure();
        if (in[limit1] == in[limit2 - 1]) {
            s.p = new double[]{in[limit1]};
            return s;
        }
        final double[] a = new double[(limit2 - limit1 == 2) ? 2 : NeuroMapSpline.SPLINE_POWER];
        final double error = makePolinom(a, images, limit1, limit2, log);
        s.p = a;
        if (error > 0.001) {
            double error1;
            double error2;
            double x1 = images[limit1].in[0];
            double x2 = images[limit2 - 1].in[0];
            int l1 = limit1;
            int l2 = limit2;
            double divisor = (x1 + x2) / 2;
            double lastDivisor = divisor;
            int divisorInd = binarySearch0(in, l1, l2, divisor) + 1;
            int lastDivisorInd = divisorInd;
            final double[] b = new double[NeuroMapSpline.SPLINE_POWER];
            while (true) {
                error1 = makePolinom(a, images, limit1, divisorInd, log);
                error2 = makePolinom(b, images, divisorInd, limit2, log);
                if (error1 > error2) {
                    x2 = divisor;
                    l2 = divisorInd;
                } else {
                    x1 = divisor;
                    l1 = divisorInd;
                }
                divisor = (x1 + x2) / 2;
                divisorInd = binarySearch0(in, l1, l2, divisor) + 1;
                if (limit1 + 1 >= divisorInd) break;
                if (divisorInd + 1 >= limit2) break;
                if (divisorInd == lastDivisorInd) break;
                lastDivisor = divisor;
                lastDivisorInd = divisorInd;
            }
            if (limit1 + 1 >= divisorInd) {
                divisorInd = limit1 + 1;
                while (divisorInd < limit2 - 1 && in[limit1] == in[divisorInd]) {
                    divisorInd++;
                }
                divisor = in[divisorInd];
            }
            if (divisorInd + 1 >= limit2) {
                divisorInd = limit2 - 1;
                while (limit1 < divisorInd - 1 && in[divisorInd] == in[limit2 - 1]) {
                    divisorInd--;
                }
                divisorInd++;
                divisor = in[divisorInd];
            }
            System.out.println("[" + limit1 + ", " + divisorInd + ", " + limit2 + "]    " +
                    "[" + in[limit1] + ", " + in[divisorInd - 1] + ", " + in[limit2 - 1] + "]");
            //divisor
            s.divided = true;
            s.divisor = divisor;
            s.downPart = trainStructure(images, in, limit1, divisorInd, log);
            s.upPart = trainStructure(images, in, divisorInd, limit2, log);
        }
        return s;
    }

    /**
     * @param q      многочлен
     * @param images образы
     * @param limit1 нижний индекс образов
     * @param limit2 верхняя граница образов
     * @param log    лог
     * @return ошибка
     */
    static double makePolinom(final double[] q,
                              final NetImage[] images,
                              final int limit1,
                              final int limit2,
                              final PrintStream log) {
        final int power = q.length - 1;
        final double[] p = new double[2 * power + 1];
        for (int i = 0; i < p.length; ++i) p[i] = 0;
        for (int i = 0; i < q.length; ++i) q[i] = 0;
        double minX = 0;
        try {
            minX = images[limit1].in[0];
        } catch (Exception e) {
            System.out.println("minX: " + images.length + ", " + limit1);
        }
        for (int i = limit1; i < limit2; ++i) {
            final NetImage image = images[i];
            final double x = image.in[0] - minX;
            final double y = image.out[0];
            double v = 1;
            for (int j = 0; j < p.length; ++j) {
                p[j] += v;
                if (j < q.length) {
                    q[j] += y * v;
                }
                v *= x;
            }
        }
        if (p[1] == 0) {
            for (int i = 1; i < q.length; ++i) {
                q[i] = 0;
            }
            q[0] = getAverage(images, limit1, limit2);
            return 0;
        }
        final double[][] a = new double[q.length][];
        for (int i = 0; i < q.length; ++i) {
            a[i] = new double[q.length];
            System.arraycopy(p, i, a[i], 0, q.length);
        }
        solveLinearEquation(a, q);
        shift(q, -minX);
        return getError(q, images, limit1, limit2);
    }

    static final int[][] binom = new int[20][];

    static {
        for (int i = 0; i < binom.length; ++i) {
            binom[i] = new int[i + 1];
            for (int j = 0; j <= i; ++j) {
                if (j == 0 || j == i) {
                    binom[i][j] = 1;
                } else {
                    binom[i][j] = binom[i - 1][j - 1] + binom[i - 1][j];
                }
            }
        }
    }

    /**
     * сдвиг аргумента
     *
     * @param a  коэффициенты полинома
     * @param x0 величина сдвига
     */
    static void shift(final double[] a, final double x0) {
        for (int k = 0; k < a.length; ++k) {
            double s = 0;
            double v = 1;
            for (int i = k; i < a.length; ++i) {
                s += a[i] * binom[i][k] * v;
                v *= x0;
            }
            a[k] = s;
        }
    }

    static private double getError(final double[] q,
                                   final NetImage[] images,
                                   final int limit1,
                                   final int limit2) {
        double d = 0;
        for (int i = limit1; i < limit2; ++i) {
            final double x = images[i].in[0];
            double y = 0;
            for (int j = q.length - 1; j >= 0; --j) {
                y = y * x + q[j];
            }
            final double e = Math.abs(images[i].out[0] - y);
            if (d < e) d = e;
        }
        return d;
    }

    static double getAverage(final NetImage[] images,
                             final int limit1,
                             final int limit2) {
        double d = 0;
        for (int i = limit1; i < limit2; ++i) {
            d += images[i].out[0];
        }
        return d / images.length;
    }

    private static int binarySearch0(final double[] a,
                                     final int fromIndex,
                                     final int toIndex,
                                     final double key) {
        int low = fromIndex;
        int high = toIndex - 1;
        int mid = fromIndex;
        while (low <= high) {
            mid = (low + high) >>> 1;
            final double midVal = a[mid];

            if (midVal < key)
                low = mid + 1;  // Neither val is NaN, thisVal is smaller
            else if (midVal > key)
                high = mid - 1; // Neither val is NaN, thisVal is larger
            else {
                final long midBits = Double.doubleToLongBits(midVal);
                final long keyBits = Double.doubleToLongBits(key);
                if (midBits == keyBits)     // Values are equal
                    return mid;             // Key found
                else if (midBits < keyBits) // (-0.0, 0.0) or (!NaN, NaN)
                    low = mid + 1;
                else                        // (0.0, -0.0) or (NaN, !NaN)
                    high = mid - 1;
            }
        }
        return mid;  // key not found.
    }

}
