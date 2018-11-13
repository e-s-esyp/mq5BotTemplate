package com.gmware.lib.neuro.mynet;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.games.holdem.common.rnd.Rnd517;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Отображение.
 * Используется для нормализации входов.
 * Created by Gauss on 19.01.2016.
 */
public abstract class NeuroMapping {
    //Линейное преобразование
    double[] in = null;
    //Статистическая нормализация
    double[] inNorm = null;
    byte[] times = null;

    public void makeBuffers(final int length) {
        in = new double[length];
        inNorm = new double[length];
        times = new byte[length];
        for (int i = 0; i < length; ++i) {
            times[i] = 0;
        }
    }

    void clear() {
        for (int i = 0; i < times.length; ++i) {
            times[i] = 0;
        }
    }

    private void addImages(final NetImage[] images, final int fi) {
        final int l = images.length - 1;
        int numPlates = 0;
        boolean plate = false;
        int start = -1;
        double last = -Double.MAX_VALUE;
        for (int j = 0; j < images.length; ++j) {
            if (last < images[j].in[fi]) {
                numPlates++;
                last = images[j].in[fi];
                if (plate) {
                    final double v = 0.5 * (((double) (j + start) / l) - 1);
                    for (int k = start; k < j; ++k) {
                        addPoint(k, images[k].in[fi], v);
                    }
                    plate = false;
                }
                addPoint(j, images[j].in[fi], ((double) j / l) - 0.5);
            } else {
                if (!plate) {
                    start = j - 1;
                    plate = true;
                }
            }
        }
        if (plate) {
            final double v = 0.5 * (((double) (l + start) / l) - 1);
            for (int k = start; k <= l; ++k) {
                addPoint(k, images[k].in[fi], v);
            }
        }
        String s = "";
        final double min = images[0].in[fi];
        final double max = images[images.length - 1].in[fi];
        final double dif = max - min;
        s += fi + ": plates=" + numPlates + "  min=" + t(min) + "  max=" + t(max) + "  dif=" + t(dif);
        if (dif == 0.0) {
            s += "  all equals";

        }
        System.out.println("NeuroMapping:" + s);
    }

    private ShiftMapping linearShift() {
        final double min = in[0];
        final double max = in[in.length - 1];
        final double dif = max - min;
        final double a;
        final double b;
        if (dif == 0) {
            a = 1;
            b = -min;
        } else {
            a = 1 / dif;
            b = -(min / dif);
        }
        for (int i = 0; i < in.length; ++i) {
            in[i] = a * in[i] + b;
        }
        return new ShiftMapping(a, b);

    }

    public void log(final String name) {
        final PrintStream log;
        try {
            log = new PrintStream(new BufferedOutputStream(
                    new FileOutputStream(name)));
            for (int i = 0; i < in.length; ++i) {
                log.println(in[i] + "\t" + inNorm[i]);
            }
            log.flush();
            log.close();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addPoint(final int k, final double vin, final double vout) {
        if (times[k] == 0) in[k] = vin;
        inNorm[k] = vout;
        times[k]++;
    }

    private NeuroMap makeMap(final int fi, final NeuroMapType mapType, final PrintStream log) {
        final NetImage[] images = new NetImage[in.length];
        for (int i = 0; i < images.length; ++i) {
            images[i] = new NetImage(1, 1);
            images[i].in = new double[]{in[i]};
            images[i].out = new double[]{inNorm[i]};
        }
        return mapType.makeDefaultTrainer().trainMap(images, mapType, new Rnd517(), log, null);
    }

    protected abstract NeuroMap trainMap(final NetImage[] images,
                                         final NeuroMapType mapType,
                                         final Rnd rnd,
                                         final PrintStream log,
                                         final String netOutPath);

    private void changeImages(final NeuroMap net, final NetImage[] imagesOriginal, final int fi, final PrintStream log) {
        log.println("NeuroMapping:");
        log.println("Original\tLinear\tStatisticNorm\tNormNetOut ");
        for (int i = 0; i < imagesOriginal.length; ++i) {
            log.print(t(imagesOriginal[i].in[fi]) + "\t");
            imagesOriginal[i].in[fi] = net.propagate(new double[]{imagesOriginal[i].in[fi]})[0];
            log.println(t(in[i]) + "\t" + t(inNorm[i]) + "\t" + t(imagesOriginal[i].in[fi]));
        }
    }

    protected static NeuroMap[] normalize(final NetImage[] images, final NeuroMapType mapType, final String netOutPath) {
        System.out.println("NeuroMapping:" + "images.length=" + images.length);
        final NeuroMap[] maps = new NeuroMap[images[0].in.length];
        final File mappingDir = new File(netOutPath);
        mappingDir.mkdirs();
        final NeuroMapping mapping = mapType.makeDefaultTrainer();
        mapping.makeBuffers(images.length);
        for (int i = 0; i < images[0].in.length; ++i) {
            final int fi = i;
            Arrays.sort(images, new Comparator<NetImage>() {
                @Override
                public int compare(final NetImage i1, final NetImage i2) {
                    if (i1.in[fi] < i2.in[fi]) return -1;
                    if (i1.in[fi] > i2.in[fi]) return 1;
                    return 0;
                }
            });
            mapping.clear();
            mapping.addImages(images, fi);
            final PrintStream log;
            try {
                log = new PrintStream(new BufferedOutputStream(
                        new FileOutputStream(netOutPath + "\\" + String.format("%03d", fi))));
                final ShiftMapping shiftMapping = mapping.linearShift();
                maps[fi] = mapping.makeMap(fi, mapType, log);
                maps[fi].shift = shiftMapping;
                mapping.changeImages(maps[fi], images, fi, log);
                log.flush();
                log.close();
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            }

        }
        return maps;
    }

    protected static NeuroMap[] normalizeNoShift(final NetImage[] images, final String netOutPath) {
        System.out.println("NeuroMapping:" + "images.length=" + images.length);
        final NeuroMapType mapType = NeuroMapType.LinearSegments;
        final NeuroMap[] maps = new NeuroMap[images[0].in.length];
        final File mappingDir = new File(netOutPath);
        mappingDir.mkdirs();
        final NeuroMapping mapping = mapType.makeDefaultTrainer();
        mapping.makeBuffers(images.length);
        for (int i = 0; i < images[0].in.length; ++i) {
            final int fi = i;
            Arrays.sort(images, new Comparator<NetImage>() {
                @Override
                public int compare(final NetImage i1, final NetImage i2) {
                    if (i1.in[fi] < i2.in[fi]) return -1;
                    if (i1.in[fi] > i2.in[fi]) return 1;
                    return 0;
                }
            });
            mapping.clear();
            mapping.addImages(images, fi);
            try {
                final PrintStream log = new PrintStream(new BufferedOutputStream(
                        new FileOutputStream(netOutPath + "\\" + String.format("%03d", fi))));
                maps[fi] = mapping.makeMap(fi, mapType, log);
                mapping.changeImages(maps[fi], images, fi, log);
                log.flush();
                log.close();
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return maps;
    }

    protected static NeuroMap normalizeNoShift(final NetImage[] images, final int fi, final PrintStream log) {
        final NeuroMapType mapType = NeuroMapType.LinearSegments;
        final NeuroMapping mapping = mapType.makeDefaultTrainer();
        mapping.makeBuffers(images.length);
        Arrays.sort(images, new Comparator<NetImage>() {
            @Override
            public int compare(final NetImage i1, final NetImage i2) {
                if (i1.in[fi] < i2.in[fi]) return -1;
                if (i1.in[fi] > i2.in[fi]) return 1;
                return 0;
            }
        });
        mapping.clear();
        mapping.addImages(images, fi);
        final NeuroMap map = mapping.makeMap(fi, mapType, log);
        mapping.changeImages(map, images, fi, log);
        return map;
    }

    static public double[] getCorrelation(final NetImage[] images) {
        double m = 0;
        for (final NetImage image : images) {
            final double y = image.out[0];
            m += y;
        }
        m /= images.length;
        double a = 0;
        for (final NetImage image : images) {
            final double y = image.out[0];
            a += (y - m) * (y - m);
        }
        a = StrictMath.sqrt(a);
        final double[] v = new double[images[0].numIn];
        for (int i = 0; i < images[0].numIn; ++i) {
            double mi = 0;
            for (final NetImage image : images) {
                final double x = image.in[i];
                mi += x;
            }
            mi /= images.length;
            double b = 0;
            double c = 0;
            for (final NetImage image : images) {
                final double x = image.in[i];
                final double y = image.out[0];
                b += (x - mi) * (y - m);
                c += (x - mi) * (x - mi);
            }
            v[i] = (b / a) / StrictMath.sqrt(c);
        }
        return v;

    }

//--------------------------------------------------------------------

    protected static void selectTrainTestImages(final NetImage[] images,
                                                final NetImage[] trainImages,
                                                final NetImage[] testImages,
                                                final Rnd rnd) {
        int trainNum = 0;
        int testNum = 0;
        for (final NetImage image : images) {
            final int trainLack = trainImages.length - trainNum;
            final int testLack = testImages.length - testNum;
            if (rnd.rnd(trainLack + testLack) < trainLack) {
                trainImages[trainNum++] = image;
            } else {
                testImages[testNum++] = image;
            }
        }
    }

    protected static double[] solveLinearEquation(final double[][] a, final double[] b) {
        final int l = b.length;
        for (int i = 0; i < l; ++i) {
            final double s = a[i][i];
            if (s == 0) return null;
            for (int k = 0; k < l; ++k) {
                a[i][k] /= s;
            }
            b[i] /= s;
            for (int j = i + 1; j < l; ++j) {
                final double t = a[j][i];
                for (int k = i; k < l; ++k) {
                    a[j][k] -= a[i][k] * t;
                }
                b[j] -= b[i] * t;
            }
        }
        for (int i = l - 1; i >= 0; --i) {
            for (int j = i - 1; j >= 0; --j) {
                final double t = a[j][i];
                for (int k = i; k < l; ++k) {
                    a[j][k] -= a[i][k] * t;
                }
                b[j] -= b[i] * t;
            }
        }
        return b;
    }

    protected static String t(final double x) {
        return String.format("%01.10f", x);
    }

}
