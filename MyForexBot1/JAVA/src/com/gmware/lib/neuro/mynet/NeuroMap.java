package com.gmware.lib.neuro.mynet;

import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.net2.Predictor;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapUnknown;

import java.io.*;

/**
 * Произвольное отображение R^n -> R^m
 * Created by Gauss on 10.02.2016.
 */
public abstract class NeuroMap {

    public long tag = -1;
    public NeuroMapType type = null;

    protected int numIns = 0;
    public int numOuts = 0;
    protected int numImages = 0;
    /**
     * Параметры отображения, веса нейросети.
     */
    public double[] w = null;
    // среднее значение выходов
    public double[] averageOut = null;
    protected ShiftMapping shift = null;
    // Отличие ошибки отображения от ошибки среднего
    public double rrError = Double.MAX_VALUE;

    //-------------------------------
    public abstract void setType();

    /**
     * Запись в двоичном виде.
     *
     * @param dos поток
     */
    abstract public void save(final DataOutputStream dos);

    public void save(final String name) throws IOException {
        final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(name)));
        save(dos);
        dos.flush();
    }

    public static NeuroMap load(final DataInputStream dis) throws IOException {
        final long head = dis.readLong();
        final NeuroMapType[] types = NeuroMapType.values();
        for (final NeuroMapType t : types) {
            if (t.getFormatCode() == head) {
                return t.load(dis);
            }
        }
        exception("Net not found or unknown net.");
        return new NeuroMapUnknown();
    }

    public static NeuroMap load(final String name) throws IOException {
        final DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(name)));
        return load(dis);
    }

    abstract public void loadBody(final DataInputStream dis) throws IOException;

    abstract public double[] propagate(final double[] doubles);

    public String[] getPropagation(final double[] in) {
        return null;
    }

    protected double getErrorQuadratic(final NetImage[] images) {
        if (images == null) new Exception(" Empty set of images. ");
        if (images.length == 0) return 0;
        double e = 0;
        try {
            double d;
            for (final NetImage image : images) {
                final double[] oi = propagate(image.in);
                for (int i = 0; i < oi.length; ++i) {
                    d = oi[i] - image.out[i];
                    e += d * d;
                }
            }
        } catch (final Exception ex) {
            System.out.println("Error: getErrorQuadratic");
        }
        e /= images.length;
        if (Double.isNaN(e)) {
            e = 1E10;
        }
        return e;
    }

    protected double getErrorQuadratic2(final NetImage[] images) {
        if (images == null) new Exception(" Empty set of images. ");
        if (images.length == 0) return 0;
        double e = 0;
        try {
            double d;
            for (final NetImage image : images) {
                final double[] oi = propagate(image.in);
                for (int i = 0; i < oi.length; ++i) {
                    d = NeuroCommon.signum(oi[i]) - image.out[i];
                    e += d * d;
                }
            }
        } catch (final Exception ex) {
            System.out.println("Error: getErrorQuadratic");
        }
        e /= images.length;
        if (Double.isNaN(e)) {
            e = 1E10;
        }
        return e;
    }

    protected double getErrorQuadratic3(final NetImage[] images) {
        if (images == null) new Exception(" Empty set of images. ");
        if (images.length == 0) return 0;
        double e = 0;
        try {
            double d;
            for (final NetImage image : images) {
                final double[] oi = propagate(image.in);
                for (int i = 0; i < oi.length; ++i) {
                    d = oi[i] - image.out[i];
                    e += Math.abs(d);
                }
            }
        } catch (final Exception ex) {
            System.out.println("Error: getErrorQuadratic");
        }
        e /= images.length;
        if (Double.isNaN(e)) {
            e = 1E10;
        }
        return e;
    }

    /**
     * Вычислить общую дисперсию обучающих выходов.
     *
     * @param images массив образов
     * @return общая дисперсия обучающих выходов
     */
    public static double getVariance(final NetImage[] images) {
        if (images.length == 0) return 0;
        final double sum = images.length * images[0].out.length;
        double sum1 = 0;
        double sum2 = 0;
        for (final NetImage image : images) {
            final double[] out = image.out;
            for (final double oi : out) {
                final double d = oi / sum;
                sum1 += d;
                sum2 += oi * d;
            }
        }
        return sum2 - sum1 * sum1;
    }

    public static void setAverageOut(final double[] anAverageOut, final NetImage[] images) {
        if (images == null) return;
        double d;
        for (int i = 0; i < anAverageOut.length; ++i) {
            d = 0;
            for (final NetImage image : images) {
                d += image.out[i];
            }
            anAverageOut[i] = d / (images.length);
        }
    }

    public static double getErrorOfAverage(final double[] anAverageOut, final NetImage[] images) {
        if (images == null) new Exception(" Empty set of images. ");
        if (images.length == 0) return 0;
        double e = 0;
        try {
            double d;
            for (final NetImage image : images) {
                for (int i = 0; i < image.out.length; ++i) {
                    d = anAverageOut[i] - image.out[i];
                    e += d * d;
                }
            }
        } catch (final Exception ex) {
            System.out.println("Error: getErrorOfAverage");
        }
        return e / images.length;
    }

    public static double getErrorOfAverage2(final double[] anAverageOut, final NetImage[] images) {
        if (images == null) new Exception(" Empty set of images. ");
        if (images.length == 0) return 0;
        double e = 0;
        try {
            double d;
            for (final NetImage image : images) {
                for (int i = 0; i < image.out.length; ++i) {
                    d = NeuroCommon.signum(anAverageOut[i]) - image.out[i];
                    e += d * d;
                }
            }
        } catch (final Exception ex) {
            System.out.println("Error: getErrorOfAverage");
        }
        return e / images.length;
    }

    public static double getErrorOfAverage3(final double[] anAverageOut, final NetImage[] images) {
        if (images == null) new Exception(" Empty set of images. ");
        if (images.length == 0) return 0;
        double e = 0;
        try {
            double d;
            for (final NetImage image : images) {
                for (int i = 0; i < image.out.length; ++i) {
                    d = anAverageOut[i] - image.out[i];
                    e += Math.abs(d);
                }
            }
        } catch (final Exception ex) {
            System.out.println("Error: getErrorOfAverage");
        }
        return e / images.length;
    }

    //-------------------------------
    protected static void exception(final String s) {
        try {
            throw new Exception(s);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public double[] propagateMulty(final double[] features) {
        return new double[0];
    }

    public Predictor.Stats getNetStats() {
        return null;
    }
}
