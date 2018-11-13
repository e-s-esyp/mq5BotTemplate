package com.gmware.lib.neuro.mynet.Mappings;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapPolinom;
import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.neuro.mynet.NeuroMapping;

import java.io.PrintStream;

/**
 * UNDER CONSTRUCTION !!!
 * <p/>
 * Поиск отображения по статистическим данным.
 * Created by Gauss on 11.02.2016.
 */
public class NeuroMappingPolinom extends NeuroMapping {

    @Override
    protected NeuroMap trainMap(final NetImage[] images,
                                final NeuroMapType mapType,
                                final Rnd rnd,
                                final PrintStream log,
                                final String netOutPath) {
        return train(images, mapType, rnd, log, netOutPath);
    }

    /**
     * Рекомендуется использовать эту функцию для образов, чьи входы
     * лежат в отрезке [-1,+1].
     */
    public static NeuroMap train(final NetImage[] images,
                                 final NeuroMapType mapType,
                                 final Rnd rnd,
                                 final PrintStream log,
                                 final String netOutPath) {
        int power = 0;
        final NeuroMapPolinom map = new NeuroMapPolinom();
        map.setErrorOfAverage(images);
        double errorRR = Double.MAX_VALUE;
        double lastErrorRR;
        double[] lastW;
        do {
            power++;
            final double[] p = new double[2 * power + 1];
            final double[] q = new double[power + 1];
            for (final NetImage image : images) {
                final double x = image.in[0];
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
                map.w = new double[]{map.averageOut[0]};
                return map;
            }
            final double[][] a = new double[q.length][];
            for (int i = 0; i < q.length; ++i) {
                a[i] = new double[q.length];
                System.arraycopy(p, i, a[i], 0, q.length);
            }
            lastErrorRR = errorRR;
            lastW = map.w;
            map.w = solveLinearEquation(a, q);
            errorRR = map.getRelativeErrors(images);
            final String s = power + ": " + errorRR;
            log.println(s);
            System.out.println(s);
            if (lastErrorRR < errorRR) {
                map.w = lastW;
                errorRR = map.getRelativeErrors(images);
                final String s1 = power + ": " + errorRR;
                log.println(s1);
                System.out.println(s1);
                return map;
            }
        } while (errorRR > 0.0001 && power < 50);
        return map;
    }


    public static void main(final String[] args) {
        solveLinearEquation(new double[][]{{1, 1}, {3, 4}}, new double[]{1, 1});
    }
}
