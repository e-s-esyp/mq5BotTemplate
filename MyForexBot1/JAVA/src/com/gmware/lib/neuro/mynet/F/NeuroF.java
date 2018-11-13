package com.gmware.lib.neuro.mynet.F;

import com.gmware.lib.neuro.mynet.NeuroImage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * --
 * Created by Gauss on 17.03.2016.
 */
public class NeuroF {
    static final double POINT = 0.00001;

    final static int NUM_IN = 4;//лучшее 4,6 из {4,5,6,7,8}
    final static int NUM_USED_OUT = 60;

    static final double[] MAC = new double[10];
    static final double[][] MA = new double[MAC.length][];

    static {
        MAC[0] = 0.5;
        for (int i = 1; i < MAC.length; i++) {
            MAC[i] = MAC[i - 1] * 0.5;
        }
    }

    final static int NUM_OUT = 2;
    static String typeOut = "";
    //---------------------------------------------------------
    final static int NUM_USED_IN = 1 << NUM_IN;
    final static int NUM_USED_IN1 = NUM_USED_IN - 1;
    final static int NUM_USED = NUM_USED_IN + NUM_USED_OUT;

    final static int NUM_CHECKED = NUM_USED_IN + 2;
    NeuroFPredictor predictor = null;
    final static int NUM_IMAGES_IN = NUM_IN * 3 + 1 + MAC.length - 1;
    final double[] in = new double[NUM_IMAGES_IN];
    final double[] out = new double[NUM_OUT];
    // первая минута, про которую не известно
    int lastTime = 0;
    TimedDoubles[] data = null;

    static boolean simulateMode = true;

    public final void printError(final String mes) {
        System.err.println(System.nanoTime() + ": " + mes);
    }

    public static void printOut(final String mes) {
        if (simulateMode) return;
        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        final Calendar cal = Calendar.getInstance();
        final Date date = cal.getTime();
        final long dateL = date.getTime();
        date.setTime(dateL + 3600000);
        System.out.println(dateFormat.format(date) + "." + (dateL % 1000) + " " + mes);
    }

    public NeuroF(final String fileName) {
        System.out.println("NeuroF(" + fileName + ")");
        try {
            predictor = new NeuroFPredictorCells(fileName);
        } catch (final Exception e) {
            printError("Error while loading " + fileName);
            e.printStackTrace();
        }
        if(!simulateMode) printOut("Predictor = " + predictor.getShortDescription());
    }

    public void setInitData(final long[] x) {
        lastTime = (int) x[1] / 6;
        data = new TimedDoubles[lastTime + 60 * 24 * 7];
        int j = 2;
        for (int i = 0; i < lastTime; ++i) {
            data[i] = new TimedDoubles(
                    x[j++],
                    x[j++] / 1000000.0,
                    x[j++] / 1000000.0,
                    x[j++] / 1000000.0,
                    x[j++] / 1000000.0,
                    (int) x[j++]);
        }
        if(!simulateMode) printOut("data.length=" + data.length);
        for (int i = lastTime - 10; i < lastTime + 2; ++i) {
            if(!simulateMode) printOut("data[" + i + "] = " + data[i]);
        }
        if(!simulateMode) printOut("lastTime=" + lastTime);
        setMAs(data);
    }

    public void propagate(final long[] x) {
        if(!simulateMode) printOut("x.length = " + x.length);
        int j = 4;
        final TimedDoubles c = new TimedDoubles(x[j++], x[j++] / 1000000.0, x[j++] / 1000000.0, x[j++] / 1000000.0, x[j++] / 1000000.0, (int) x[j++]);
        final TimedDoubles b = new TimedDoubles(x[j++], x[j++] / 1000000.0, x[j++] / 1000000.0, x[j++] / 1000000.0, x[j++] / 1000000.0, (int) x[j++]);
        final TimedDoubles a = new TimedDoubles(x[j++], x[j++] / 1000000.0, x[j++] / 1000000.0, x[j++] / 1000000.0, x[j++] / 1000000.0, (int) x[j]);
        if(!simulateMode) printOut("a = " + a);
        if(!simulateMode) printOut("b = " + b);
        if(!simulateMode) printOut("c = " + c);
        final long t = data[lastTime - 1].time;
        if (t + 1 == a.time) {
            lastTime += 2;
        } else {
            if (t + 1 == b.time) {
                ++lastTime;
            } else {
                if (t + 1 != c.time) {
                    if(!simulateMode) printOut("Error in NeuroF.propagate()");
                    out[0] = -1;
                    return;
                }
            }
        }
        data[lastTime - 2] = a;
        data[lastTime - 1] = b;
        data[lastTime] = c;
        setMA(data, lastTime - 2);
        setMA(data, lastTime - 1);
        setMA(data, lastTime);
        if(!simulateMode) printOut("lastTime=" + lastTime);
        if(!simulateMode) printOut("data[" + lastTime + "] = " + data[lastTime]);
        out[0] = predictor.getDecision((int) x[2], (int) x[3], data, lastTime);
        if(!simulateMode) printOut("out = " + out[0]);
    }

    final static int[] DIFS = {1, 5, 15, 30, 60, 240, 10080, 43200};

    public static void setMA(final TimedDoubles[] d, final int j) {
        if(!simulateMode) printOut(String.format("setMA j=%d d.length=%d MA.length=%d", j, d.length, MA.length));
        if(!simulateMode) printOut("d[j]=" + d[j]);
        for (int i = 0; i < MA.length; i++) {
            MA[i][j] = (1 - MAC[i]) * MA[i][j - 1] + MAC[i] * d[j].close;
        }
    }

    public static void setMAs(final TimedDoubles[] d) {
        if(!simulateMode) printOut("Setting MAs. MA.length=" + MA.length + " d.length=" + d.length);
        for (int i = 0; i < MA.length; i++) {
            final double[] m = new double[d.length];
            m[0] = d[0].close;
            for (int j = 1; j < d.length; j++) {
                if (d[j] != null) {
                    m[j] = (1 - MAC[i]) * m[j - 1] + MAC[i] * d[j].close;
                }
            }
            MA[i] = m;
            if(!simulateMode) printOut("MA[" + i + "]=" + MA[i]);
        }
    }

    /**
     * 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 | 17
     *
     * @param j0 первый неизвестный индекс
     * @param d
     * @param in
     */
    public static void fillIn(final int j0, final TimedDoubles[] d, final double[] in) {
        final int j = j0 - NUM_USED_IN;
        if(!simulateMode) printOut("fillIn() j=" + j);
        if(!simulateMode) printOut("j + NUM_USED_IN1=" + (j + NUM_USED_IN1));
        final double[] bfMin = new double[NUM_IN + 1];
        final double[] bfAvr = new double[NUM_IN + 1];
        final double[] bfMax = new double[NUM_IN + 1];
        int l = 1 << (NUM_IN - 1);
        int k2 = j;
        for (int k = 0; k <= NUM_IN; ++k) {
            if (k2 < 0) {
                System.out.println();
            }
            if(!simulateMode) printOut("d[" + k2 + "]=" + d[k2]);
            bfMax[k] = d[k2].max;
            bfMin[k] = d[k2].min;
            double sum = (d[k2].max + d[k2].min) / 2;
            ++k2;
            for (int k3 = 1; k3 < l; ++k3) {
                if(!simulateMode) printOut("d[" + k2 + "]=" + d[k2]);
                if (bfMax[k] < d[k2].max) {
                    bfMax[k] = d[k2].max;
                }
                if (bfMin[k] > d[k2].min) {
                    bfMin[k] = d[k2].min;
                }
                sum += (d[k2].max + d[k2].min) / 2;
                ++k2;
            }
            bfAvr[k] = (l > 1) ? (sum / l) : sum;
            l >>= 1;
        }
        if(!simulateMode) printOut("d[" + (j + NUM_USED_IN1) + "]=" + d[j + NUM_USED_IN1]);
        for (int k = 0; k < NUM_IN; ++k) {
            in[3 * k] = 1000 * Math.log(bfMax[k] / bfMax[k + 1]);
            in[3 * k + 1] = 1000 * Math.log(bfAvr[k] / d[j + NUM_USED_IN1].close);
            in[3 * k + 2] = 1000 * Math.log(bfMax[k] / bfMin[k]);
        }
        for (int i = 1; i < MA.length; i++) {
            in[NUM_IMAGES_IN - 1 - i] = MA[i][j + NUM_USED_IN1] / MA[0][j + NUM_USED_IN1];
        }
        in[NUM_IMAGES_IN - 1] = d[j + NUM_USED_IN1].time % (60 * 24); //сутки
    }

    static double range = 1.0;//значение по-умолчанию

    static {
        typeOut = "O4";
    }


    public static int getNumImages(final TimedDoubles[] d, final int dif) {
        int j = 0;
        int n = 2;
        for (int i = 1; i < d.length; ++i) {
            if (d[i - 1].time + dif != d[i].time) {
                if (n >= NUM_CHECKED) {
                    j += n - NUM_CHECKED;
                }
                n = 1;
            }
            ++n;
        }
        if (n >= NUM_CHECKED) {
            j += n - NUM_CHECKED;
        }
        return j;
    }

    //======================================================================================================================
    static final double SPREAD = 0.0001;
    static final int HORIZONT = 1000;

    //b[start] прошло только что
    private static void setTrends(final int start, final TimedDoubles[] b, final int[] t) {
//        final double[] c;
//        c = new double[t.length];
//        for (int i = 0; i < c.length && i + 1 < b.length; i++) {
//            c[i] = b[i + 1 + start].close - b[i + start].close;
//        }
        int ti = 0;
        double min = b[start].close;
        double max = b[start].close;
        int minInd = -1;
        int maxInd = -1;
        boolean isUp = false;
        int j = start;
        int changeInd = start;
        for (; j < b.length; j++) {
            if (b[j].close <= min) {
                min = b[j].close;
                minInd = j;
            }
            if (b[j].close >= max) {
                max = b[j].close;
                maxInd = j;
            }
            if (max - min > SPREAD) {
                isUp = maxInd > minInd;
                final int ind = Math.min(minInd, maxInd);
                for (int k = changeInd; k <= ind; ++k) {
                    if (ti >= t.length) return;
                    t[ti++] = 1;
                }
                if (changeInd < ind + 1) {
                    changeInd = ind + 1;
                }
                break;
            }
        }
        int changes = 0;
        for (; j < b.length && changes < 2; j++) {
            //isUp
            if (isUp) {
                if (b[j].close - max < -SPREAD) {
                    //fix UP
                    for (int k = changeInd; k <= maxInd; ++k) {
                        if (ti >= t.length) return;
                        t[ti++] = 0;
                    }
                    changeInd = maxInd + 1;
                    isUp = false;
                    changes++;
                    min = b[j].close;
                    minInd = j;
                } else {
                    if (b[j].close >= max) {
                        max = b[j].close;
                        maxInd = j;
                    }
                }
            } else {
                if (b[j].close - min > SPREAD) {
                    //fix DOWN
                    for (int k = changeInd; k <= minInd; ++k) {
                        if (ti >= t.length) return;
                        t[ti++] = 2;
                    }
                    changeInd = minInd + 1;
                    isUp = true;
                    changes++;
                    max = b[j].close;
                    maxInd = j;
                } else {
                    if (b[j].close <= min) {
                        min = b[j].close;
                        minInd = j;
                    }
                }
            }
        }
        if (changes >= 2) return;
        if (max - min > SPREAD) {
            if (isUp) {
                for (int k = changeInd; k <= maxInd; ++k) {
                    if (ti >= t.length) return;
                    t[ti++] = 0;
                }
            } else {
                for (int k = changeInd; k <= minInd; ++k) {
                    if (ti >= t.length) return;
                    t[ti++] = 2;
                }
            }
        }
    }

    static class TrendChange {
        boolean found = false;
        int trend = -2;
        double bLast = 0;
        int klass = -1;
    }

    private static void prepare(final int start, final TimedDoubles[] b, final TrendChange change) {
        final int[] t0 = new int[HORIZONT];
        final int[] t1 = new int[HORIZONT];
        setTrends(start - 1, b, t0);
        setTrends(start, b, t1);
        final int tFirst = t0[1];
        for (int i = 1; i < t0.length - 1 && start + i - 1 < b.length; i++) {
            final boolean eq = (t0[i] == t1[i - 1]);
            final boolean nz0 = ((t0[i] == 0) || t0[i] == 2);
            if (eq && nz0) {
                change.trend = t0[i];
                change.bLast = b[start + i - 2].close - b[start - 1].close;
                change.found = true;
                break;
            }
        }
        change.klass = tFirst;
    }

    //0
    public static void fillOutCloseUp(final int start, final TimedDoubles[] b, final NeuroImage image) {
        final TrendChange change = new TrendChange();
        prepare(start, b, change);
        image.klass = change.klass;
        image.numKlasses = 3;
        if (change.found) {
            if (change.trend == 0) {
                //+
                image.out = new double[]{change.bLast, -SPREAD, -change.bLast - 2 * SPREAD};
            } else {
                //-
                image.out = new double[]{change.bLast - SPREAD, -SPREAD, -change.bLast - SPREAD};
            }
        } else {
            image.out = new double[]{0, 0, 0};
        }
    }

    //1
    public static void fillOutOpen(final int start, final TimedDoubles[] b, final NeuroImage image) {
        final TrendChange change = new TrendChange();
        prepare(start, b, change);
        image.klass = change.klass;
        image.numKlasses = 3;
        if (change.found) {
            if (change.trend == 0) {
                //+
                image.out = new double[]{change.bLast - SPREAD, -SPREAD, -change.bLast - 2 * SPREAD};
            } else {
                //-
                image.out = new double[]{change.bLast - 2 * SPREAD, -SPREAD, -change.bLast - SPREAD};
            }
        } else {
            image.out = new double[]{0, 0, 0};
        }
    }

    //2
    public static void fillOutCloseDown(final int start, final TimedDoubles[] b, final NeuroImage image) {
        final TrendChange change = new TrendChange();
        prepare(start, b, change);
        image.klass = change.klass;
        image.numKlasses = 3;
        if (change.found) {
            if (change.trend == 0) {
                //+
                image.out = new double[]{change.bLast - SPREAD, -SPREAD, -change.bLast - SPREAD};
            } else {
                //-
                image.out = new double[]{change.bLast - 2 * SPREAD, -SPREAD, -change.bLast};
            }
        } else {
            image.out = new double[]{0, 0, 0};
        }
    }

    public static void fillOut(
            final int start,
            final TimedDoubles[] b,
            final NeuroImage image0,
            final NeuroImage image1,
            final NeuroImage image2) {
        final TrendChange change = new TrendChange();
        prepare(start, b, change);
        if (change.klass == 1) {
            image0.klass = 0;
            image1.klass = 1;
            image2.klass = 1;
        } else {
            if (change.klass == 0) {
                image0.klass = 0;
                image1.klass = 0;
                image2.klass = 0;
            } else {
                image0.klass = 1;
                image1.klass = 2;
                image2.klass = 1;
            }
        }
        image0.numKlasses = 2;
        image1.numKlasses = 3;
        image2.numKlasses = 2;
        if (change.found) {
            if (change.trend == 0) {
                //+
                image0.out = new double[]{change.bLast, -change.bLast - 2 * SPREAD};
                image1.out = new double[]{change.bLast - SPREAD, -SPREAD, -change.bLast - 2 * SPREAD};
                image2.out = new double[]{change.bLast - SPREAD, -change.bLast - SPREAD};
            } else {
                //-
                image0.out = new double[]{change.bLast - SPREAD, -change.bLast - SPREAD};
                image1.out = new double[]{change.bLast - 2 * SPREAD, -SPREAD, -change.bLast - SPREAD};
                image2.out = new double[]{change.bLast - 2 * SPREAD, -change.bLast};
            }
            normalize(image0);
            normalize(image1);
            normalize(image2);
        } else {
            image0.out = new double[]{0, 0};
            image1.out = new double[]{0, 0, 0};
            image2.out = new double[]{0, 0};
        }
        image0.numOut = image0.out.length;
        image1.numOut = image1.out.length;
        image2.numOut = image2.out.length;
    }

    private static void normalize(final NeuroImage image) {
        final double max = image.out[image.klass];
        for (int i = 0; i < image.out.length; i++) {
            image.out[i] -= max;
            if (image.out[i] > 0) {
                System.out.println("Error: " + image);
            }
        }
    }

    public static void fillImages(final TimedDoubles[] d,
                                  final NeuroImage[] images0,
                                  final NeuroImage[] images1,
                                  final NeuroImage[] images2) {
        setMAs(d);//TODO: перенести в initParameters
        int n = 0;
        int j = 0;
        while (j + NUM_CHECKED <= d.length) {
            for (int i = 1; i < NUM_CHECKED && i + j < d.length; ++i) {
                if (d[j + i - 1].time + 1 != d[j + i].time) {
                    j += i;
                    i = 0;
                }
            }
            while (j + NUM_CHECKED <= d.length) {
                if (d[j + NUM_CHECKED - 2].time + 1 != d[j + NUM_CHECKED - 1].time) {
                    break;
                }
//----------------------------------------------------------------------------------------------
                final double[] imageIn = new double[NUM_IMAGES_IN];
                fillIn(j + NUM_USED_IN, d, imageIn);
//----------------------------------------------------------------------------------------------
                final NeuroImage image0 = new NeuroImage();
                final NeuroImage image1 = new NeuroImage();
                final NeuroImage image2 = new NeuroImage();
                fillOut(j + NUM_USED_IN, d, image0, image1, image2);
//----------------------------------------------------------------------------------------------
                image0.in = imageIn;
                image1.in = imageIn;
                image2.in = imageIn;
                image0.numIn = imageIn.length;
                image1.numIn = imageIn.length;
                image2.numIn = imageIn.length;
                images0[n] = image0;
                images1[n] = image1;
                images2[n] = image2;
                ++n;
                ++j;
            }
        }
    }
}
