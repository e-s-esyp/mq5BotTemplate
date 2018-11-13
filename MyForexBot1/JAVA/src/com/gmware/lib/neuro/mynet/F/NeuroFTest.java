package com.gmware.lib.neuro.mynet.F;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.NeuroImage;
import com.gmware.lib.games.holdem.common.rnd.Rnd517;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * Created by Gauss on 22.04.2016.
 */
public class NeuroFTest {
    //======================================================================================================================
    static final int SWAP = 7;

    private static void getTrends(final int[] b, final int start, final int[] t) {
        final int[] c;
        if (start > 0) {
            c = new int[b.length];
            for (int i = start; i < b.length; i++) {
                c[i] = b[i] - b[start - 1];
            }
        } else {
            c = b;
        }
        int ti = 0;
        int min = 0;
        int max = 0;
        int minInd = -1;
        int maxInd = -1;
        int changeInd = 0;
        boolean isUp = false;
        int j = 0;
        for (; j < start; ++j) {
            t[ti++] = -1;
        }
        changeInd = start;
        for (; j < c.length; j++) {
            if (c[j] <= min) {
                min = c[j];
                minInd = j;
            }
            if (c[j] >= max) {
                max = c[j];
                maxInd = j;
            }
            if (max - min > SWAP) {
                isUp = maxInd > minInd;
                final int ind = Math.min(minInd, maxInd);
                for (int k = changeInd; k <= ind; ++k) {
                    t[ti++] = 1;
                }
                if (changeInd < ind + 1) {
                    changeInd = ind + 1;
                }
                break;
            }
        }
        int changes = 0;
        for (; j < c.length && changes < 2; j++) {
            //isUp
            if (isUp) {
                if (c[j] - max < -SWAP) {
                    //fix UP
                    for (int k = changeInd; k <= maxInd; ++k) {
                        t[ti++] = 0;
                    }
                    changeInd = maxInd + 1;
                    isUp = false;
                    changes++;
                    min = c[j];
                    minInd = j;
                } else {
                    if (c[j] >= max) {
                        max = c[j];
                        maxInd = j;
                    }
                }
            } else {
                if (c[j] - min > SWAP) {
                    //fix DOWN
                    for (int k = changeInd; k <= minInd; ++k) {
                        t[ti++] = 2;
                    }
                    changeInd = minInd + 1;
                    isUp = true;
                    changes++;
                    max = c[j];
                    maxInd = j;
                } else {
                    if (c[j] <= min) {
                        min = c[j];
                        minInd = j;
                    }
                }
            }
        }
        if (changes >= 2) return;
        if (max - min > SWAP) {
            if (isUp) {
                for (int k = changeInd; k <= maxInd; ++k) {
                    t[ti++] = 0;
                }
            } else {
                for (int k = changeInd; k <= minInd; ++k) {
                    t[ti++] = 2;
                }
            }
        }
    }

    static void fillOutOpen(final int[] b, final NeuroImage image) {
        final int[] t0 = new int[b.length];
        final int[] t1 = new int[b.length];
        getTrends(b, 0, t0);
        getTrends(b, 1, t1);
        final int tFirst = t0[0];
        int bLast = 0;
        int trend = -2;
        boolean found = false;
        for (int i = 0; i < t0.length - 1; i++) {
            final boolean eq = (t0[i] == t1[i]);
            final boolean nz0 = ((t0[i] == 0) || t0[i] == 2);
            if (eq && nz0) {
                trend = t0[i];
                bLast = b[i - 1];
                found = true;
                break;
            }
        }
        image.klass = tFirst;
        if (found) {
            if (trend == 0) {
                //+
                image.out = new double[]{bLast - SWAP, -SWAP, -bLast - 2 * SWAP};
            } else {
                //-
                image.out = new double[]{bLast - 2 * SWAP, -SWAP, -bLast - SWAP};
            }
        } else {
            image.out = new double[]{0, 0, 0};
        }
    }

    static void fillOutOpen(final int[] b, final int[] t0, final int[] t1, final NeuroImage image) {
        final int tFirst = t0[0];
        int bLast = 0;
        int trend = -2;
        boolean found = false;
        for (int i = 0; i < t0.length - 1; i++) {
            final boolean eq = (t0[i] == t1[i]);
            final boolean nz0 = ((t0[i] == 0) || t0[i] == 2);
            if (eq && nz0) {
                trend = t0[i];
                bLast = b[i - 1];
                found = true;
                break;
            }
        }
        image.klass = tFirst;
        if (found) {
            if (trend == 0) {
                //+
                image.out = new double[]{bLast - SWAP, -SWAP, -bLast - 2 * SWAP};
            } else {
                //-
                image.out = new double[]{bLast - 2 * SWAP, -SWAP, -bLast - SWAP};
            }
        } else {
            image.out = new double[]{0, 0, 0};
        }
    }

    static void fillOutCloseUp(final int[] b, final NeuroImage image) {
        final int[] t0 = new int[b.length];
        final int[] t1 = new int[b.length];
        getTrends(b, 0, t0);
        getTrends(b, 1, t1);
        final int tFirst = t0[0];
        int bLast = 0;
        int trend = -2;
        boolean found = false;
        for (int i = 0; i < t0.length - 1; i++) {
            final boolean eq = (t0[i] == t1[i]);
            final boolean nz0 = ((t0[i] == 0) || t0[i] == 2);
            if (eq && nz0) {
                trend = t0[i];
                bLast = b[i - 1];
                found = true;
                break;
            }
        }
        image.klass = tFirst;
        if (found) {
            if (trend == 0) {
                //+
                image.out = new double[]{bLast, -SWAP, -bLast - 2 * SWAP};
            } else {
                //-
                image.out = new double[]{bLast - SWAP, -SWAP, -bLast - SWAP};
            }
        } else {
            image.out = new double[]{0, 0, 0};
        }
    }

    static void fillOutCloseUp(final int[] b, final int[] t0, final int[] t1, final NeuroImage image) {
        final int tFirst = t0[0];
        int bLast = 0;
        int trend = -2;
        boolean found = false;
        for (int i = 0; i < t0.length - 1; i++) {
            final boolean eq = (t0[i] == t1[i]);
            final boolean nz0 = ((t0[i] == 0) || t0[i] == 2);
            if (eq && nz0) {
                trend = t0[i];
                bLast = b[i - 1];
                found = true;
                break;
            }
        }
        image.klass = tFirst;
        if (found) {
            if (trend == 0) {
                //+
                image.out = new double[]{bLast, -SWAP, -bLast - 2 * SWAP};
            } else {
                //-
                image.out = new double[]{bLast - SWAP, -SWAP, -bLast - SWAP};
            }
        } else {
            image.out = new double[]{0, 0, 0};
        }
    }

    static void fillOutCloseDown(final int[] b, final NeuroImage image) {
        final int[] t0 = new int[b.length];
        final int[] t1 = new int[b.length];
        getTrends(b, 0, t0);
        getTrends(b, 1, t1);
        final int tFirst = t0[0];
        int bLast = 0;
        int trend = -2;
        boolean found = false;
        for (int i = 0; i < t0.length - 1; i++) {
            final boolean eq = (t0[i] == t1[i]);
            final boolean nz0 = ((t0[i] == 0) || t0[i] == 2);
            if (eq && nz0) {
                trend = t0[i];
                bLast = b[i - 1];
                found = true;
                break;
            }
        }
        image.klass = tFirst;
        if (found) {
            if (trend == 0) {
                //+
                image.out = new double[]{bLast - SWAP, -SWAP, -bLast - SWAP};
            } else {
                //-
                image.out = new double[]{bLast - 2 * SWAP, -SWAP, -bLast};
            }
        } else {
            image.out = new double[]{0, 0, 0};
        }
    }

    static void fillOutCloseDown(final int[] b, final int[] t0, final int[] t1, final NeuroImage image) {
        final int tFirst = t0[0];
        int bLast = 0;
        int trend = -2;
        boolean found = false;
        for (int i = 0; i < t0.length - 1; i++) {
            final boolean eq = (t0[i] == t1[i]);
            final boolean nz0 = ((t0[i] == 0) || t0[i] == 2);
            if (eq && nz0) {
                trend = t0[i];
                bLast = b[i - 1];
                found = true;
                break;
            }
        }
        image.klass = tFirst;
        if (found) {
            if (trend == 0) {
                //+
                image.out = new double[]{bLast - SWAP, -SWAP, -bLast - SWAP};
            } else {
                //-
                image.out = new double[]{bLast - 2 * SWAP, -SWAP, -bLast};
            }
        } else {
            image.out = new double[]{0, 0, 0};
        }
    }

    static void test() throws FileNotFoundException {
        final Rnd rnd = new Rnd517(0);
        final int l = 30;
        final NeuroImage image = new NeuroImage(0, 3, 3);
        final NeuroImage image1 = new NeuroImage(0, 3, 3);
//        int[] b = new int[]{8, 10, 6, 5, 1, 2, 5, -4, 0, -7};
//        System.out.println(getTrendsS(b));
        final int[] b = new int[l];
        final int[] t0 = new int[l];
        final int[] t1 = new int[l];
        final PrintStream log = new PrintStream(new File("D:\\DATA\\NeuroF.log"));
        final int size = 10000;
        final String[] strings = new String[size];
        for (int i = 0; i < size; i++) {
            b[0] = rnd.rnd(19) - 9;
            String sb = "" + String.format("%4d", b[0]);
            for (int j = 1; j < l; j++) {
                b[j] = b[j - 1] + rnd.rnd(19) - 9;
                sb += ", " + String.format("%4d", b[j]);
            }
            fillOutCloseUp(b, image);
            final String sc = getTrendsS(b, 0, t0) + " \n" + getTrendsS(b, 1, t1) + " \n";
            strings[i] = makePattern(t0, t1);
            fillOutCloseUp(b, t0, t1, image1);
            if (image.out[0] != image1.out[0]) {
                System.out.println(sc + sb + " | " + image + " | " + image1);
                fillOutCloseUp(b, image);
                fillOutCloseUp(b, t0, t1, image1);
            }
        }
        Arrays.sort(strings);
        for (int i = 0; i < size; i++) {
            if (i == 0) {
                log.println(strings[i]);
            } else {
                if (!strings[i].equals(strings[i - 1])) {
                    log.println(strings[i]);
                }
            }
        }
    }

    static final String[] PATTERNS = new String[]{
            "++\n*+\n",
            "+++\n*0+\n",
            "++-\n*0-\n",
            "+-\n*-\n",
            "-+\n*+\n",
            "--\n*-\n",
            "--+\n*0+\n",
            "---\n*0-\n",
            "0+\n*+\n",
            "0-\n*-\n",
            "00+\n*0+\n",
            "00-\n*0-\n"
    };

    private static char symbol(final int n) {
        if (n == 0) return '+';
        if (n == 1) return '0';
        if (n == 2) return '-';
        return '*';
    }

    private static String makePattern(final int[] t0, final int[] t1) {
        String s0 = "";
        String s1 = "";
        for (int i = 0; i < t0.length - 1; i++) {
            final boolean eq = (t0[i] == t1[i]);
            final boolean nz0 = ((t0[i] == 0) || t0[i] == 2);
            final boolean same = (t0[i] == t0[i + 1]) && (t1[i] == t1[i + 1]);
            if (!same || (eq && nz0)) {
                s0 += symbol(t0[i]);
                s1 += symbol(t1[i]);
            }
            if (eq && nz0) {
                break;
            }
        }
        return s0 + "\n" + s1 + "\n";
    }

    private static String getTrendsS(final int[] b, final int start, final int[] t) {
        final int[] c;
        if (start > 0) {
            c = new int[b.length];
            for (int i = start; i < b.length; i++) {
                c[i] = b[i] - b[start - 1];
            }
        } else {
            c = b;
        }
        String s = "";
        int ti = 0;
        int min = 0;
        int max = 0;
        int minInd = -1;
        int maxInd = -1;
        int changeInd = 0;
        boolean isUp = false;
        int j = 0;
        for (; j < start; ++j) {
            s += "   *  ";
            t[ti++] = -1;
        }
        changeInd = start;
        for (; j < c.length; j++) {
            if (c[j] <= min) {
                min = c[j];
                minInd = j;
            }
            if (c[j] >= max) {
                max = c[j];
                maxInd = j;
            }
            if (max - min > SWAP) {
                isUp = maxInd > minInd;
                final int ind = Math.min(minInd, maxInd);
                for (int k = changeInd; k <= ind; ++k) {
                    s += "   0  ";
                    t[ti++] = 1;
                }
                if (changeInd < ind + 1) {
                    changeInd = ind + 1;
                }
                break;
            }
        }
        int changes = 0;
        for (; j < c.length && changes < 2; j++) {
            //isUp
            if (isUp) {
                if (c[j] - max < -SWAP) {
                    //fix UP
                    for (int k = changeInd; k <= maxInd; ++k) {
                        s += "   +  ";
                        t[ti++] = 0;
                    }
                    changeInd = maxInd + 1;
                    isUp = false;
                    changes++;
                    min = c[j];
                    minInd = j;
                } else {
                    if (c[j] >= max) {
                        max = c[j];
                        maxInd = j;
                    }
                }
            } else {
                if (c[j] - min > SWAP) {
                    //fix DOWN
                    for (int k = changeInd; k <= minInd; ++k) {
                        s += "   -  ";
                        t[ti++] = 2;
                    }
                    changeInd = minInd + 1;
                    isUp = true;
                    changes++;
                    max = c[j];
                    maxInd = j;
                } else {
                    if (c[j] <= min) {
                        min = c[j];
                        minInd = j;
                    }
                }
            }
        }
        if (changes >= 2) return s;
        if (max - min > SWAP) {
            if (isUp) {
                for (int k = changeInd; k <= maxInd; ++k) {
                    s += "  (+) ";
                    t[ti++] = 0;
                }
                changeInd = maxInd + 1;
            } else {
                for (int k = changeInd; k <= minInd; ++k) {
                    s += "  (-) ";
                    t[ti++] = 2;
                }
                changeInd = minInd + 1;
            }
        }
        for (int k = changeInd; k < c.length; ++k) {
            s += "  (0) ";
            t[ti++] = 1;
        }
        return s;
    }

    public static void main(final String[] args) throws FileNotFoundException {
        test();
    }

}
