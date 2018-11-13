package com.gmware.lib.neuro.mynet;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.games.holdem.common.rnd.Rnd517;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

/**
 * ---
 * Created by Gauss on 07.04.2016.
 */
public class NeuroPNG {


    static class PNGImage {
        double in1d = 0;
        double in2d = 0;
        int in1 = 0;
        int in2 = 0;
        double out = 0;
        double random = 0;

        public PNGImage(final int ind1, final int ind2, final NetImage image, final double rnd) {
            in1d = image.in[ind1];
            in2d = image.in[ind2];
            out = image.out[0];
            random = rnd;
        }
    }

    static class PNGout {
        double outd = 0;
        int out = 0;
    }

    public static void printPNG(final NetImage[] images,
                                final int ind1,
                                final int ind2,
                                final String s) throws IOException {
        final int shift0 = 5;//размер картинки
        final int size = 1 << shift0;
        final int shift = 2 * shift0 - 8;

        final Rnd rnd = new Rnd517();
        final PNGImage[] p = new PNGImage[images.length];
        for (int i = 0; i < p.length; ++i) {
            p[i] = new PNGImage(ind1, ind2, images[i], rnd.rnd());
        }
        Arrays.sort(p, new Comparator<PNGImage>() {
            @Override
            public int compare(final PNGImage i1, final PNGImage i2) {
                final double o1 = i1.in1d;
                final double o2 = i2.in1d;
                if (o1 < o2) return -1;
                if (o1 > o2) return 1;
                final double o3 = i1.random;
                final double o4 = i2.random;
                if (o3 < o4) return -1;
                if (o3 > o4) return 1;
                return 0;
            }
        });
        int d = p.length / size;
        while ((p.length - 1) / d >= size) {
            ++d;
        }
        for (int i = 0; i < p.length; ++i) {
            p[i].in1 = i / d;
        }
        Arrays.sort(p, new Comparator<PNGImage>() {
            @Override
            public int compare(final PNGImage i1, final PNGImage i2) {
                final double o3 = i1.in2d;
                final double o4 = i2.in2d;
                if (o3 < o4) return -1;
                if (o3 > o4) return 1;
                final double o1 = i1.random;
                final double o2 = i2.random;
                if (o1 < o2) return -1;
                if (o1 > o2) return 1;
                return 0;
            }
        });
        for (int i = 0; i < p.length; ++i) {
            p[i].in2 = i / d;
        }
        final PNGout[][] table = new PNGout[size][];
        for (int i = 0; i < table.length; i++) {
            table[i] = new PNGout[size];
            for (int j = 0; j < table[i].length; j++) {
                table[i][j] = new PNGout();
            }
        }
        for (final PNGImage image : p) {
            table[image.in1][image.in2].outd += image.out;
        }
        final PNGout[] row = new PNGout[size * size];
        int k = 0;
        for (final PNGout[] a : table) {
            for (final PNGout b : a) {
                row[k++] = b;
            }
        }
        Arrays.sort(row, new Comparator<PNGout>() {
            @Override
            public int compare(final PNGout i1, final PNGout i2) {
                final double o3 = i1.outd;
                final double o4 = i2.outd;
                if (o3 < o4) return -1;
                if (o3 > o4) return 1;
                return 0;
            }
        });
        for (int i = 0; i < row.length; ++i) {
            if (row[i].outd != 0.0) {
                if (shift > 0) {
                    row[i].out = ((i >> shift) << 8) | ((1 - (i >> shift)) << 16);
                } else {
                    row[i].out = ((i << -shift) << 8) | ((1 - (i << -shift)) << 16);
                }
            } else {
                row[i].out = 0x707070;
            }
        }
        final BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[i].length; j++) {
                img.setRGB(i, j, table[i][j].out);
            }
        }
        final String fileName = s + "." + ind1 + "." + ind2 + ".png";
        ImageIO.write(img, "png", new File(fileName));
    }

    public static void printPNGAutocorrelation(final double[] x,
                                               final int size,
                                               final String s) throws IOException {
        final double norm = getNorm(x);
        if (x.length < 100000) {
            printPNGAutocorrelation(0, x.length, x, norm, size, s);
        } else {
            final int numParts = 10;
            final int num = 2;
            final int partLength = (num * x.length) / (numParts + num - 1);
            final int partDistance = (x.length - partLength) / (numParts - 1);
            for (int i = 0; i < numParts; i++) {
                printPNGAutocorrelation(
                        i * partDistance,
                        (i * partDistance) + partLength,
                        x,
                        norm,
                        size,
                        s + "." + i);
            }
        }
    }

    public static double getNorm(final double[] x) {
        double s = 0;
        for (final double a : x) {
            s += a * a;
        }
        return s / x.length;
    }

    public static void printPNGAutocorrelation(int start,
                                               int finish,
                                               final double[] x,
                                               final double norm,
                                               final int size,
                                               final String s) throws IOException {
        if (start < 0) start = 0;
        if (finish > x.length) finish = x.length;
        final double[] a = new double[size];
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < size; i++) {
            double b = 0;
            for (int j = start; j < finish - size + 1; j++) {
                b += x[j] * x[j + i];
            }
            a[i] = b;
            max = Math.max(Math.abs(b), max);
            min = Math.min(Math.abs(b), min);
        }
        final int[][] table = new int[size][];
        for (int i = 0; i < table.length; i++) {
            table[i] = new int[size];
        }
        final double base = (finish - size + 1 - size);
        for (int i = 0; i < size; i++) {
            final int l = (int) (((a[i] / base - norm) * size)/10);
            if (l < 0) {
                for (int j = l + size / 2; j < size / 2; j++) {
                    if (j >= 0 && j < size) table[i][j] = 0x00FF00;
                }
            } else {
                for (int j = size / 2; j < l + size / 2; j++) {
                    if (j >= 0 && j < size) table[i][j] = 0x00FF00;
                }
            }
        }
        final BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[i].length; j++) {
                img.setRGB(i, j, table[i][j]);
            }
        }
        final String fileName = s + ".autocorr." + start + "." + finish + ".png";
        ImageIO.write(img, "png", new File(fileName));
    }


    PrintStream summary = null;

    private int getNumInClasses(final NetImage[] images, final int ind) {
        Arrays.sort(images, new Comparator<NetImage>() {
            @Override
            public int compare(final NetImage i1, final NetImage i2) {
                final double o1 = i1.in[ind];
                final double o2 = i2.in[ind];
                if (o1 < o2) return -1;
                if (o1 > o2) return 1;
                return 0;
            }
        });
        int numClasses = 1;
        for (int i = 1; i < images.length; ++i) {
            if (images[i].in[ind] != images[i - 1].in[ind]) ++numClasses;
        }
        return numClasses;
    }

    private int getNumOutClasses(final NetImage[] images) {
        Arrays.sort(images, new Comparator<NetImage>() {
            @Override
            public int compare(final NetImage i1, final NetImage i2) {
                final double o1 = i1.out[0];
                final double o2 = i2.out[0];
                if (o1 < o2) return -1;
                if (o1 > o2) return 1;
                return 0;
            }
        });
        int numClasses = 1;
        for (int i = 1; i < images.length; ++i) {
            if (images[i].out[0] != images[i - 1].out[0]) ++numClasses;
        }
        return numClasses;
    }

    private int getNumDays(final NetImage[] images) {
        Arrays.sort(images, new Comparator<NetImage>() {
            @Override
            public int compare(final NetImage i1, final NetImage i2) {
                final double o1 = i1.tag;
                final double o2 = i2.tag;
                if (o1 < o2) return -1;
                if (o1 > o2) return 1;
                return 0;
            }
        });
        return (int) ((images[images.length - 1].tag - images[0].tag) / 1440);
    }

    private void fillRGBImage(final BufferedImage img, final NetImage[] images, final int ind) {
        final int[] colors = new int[]{0x0000FF, 0x7F7F7F, 0xFF0000};
        final int firstDay = (int) (images[0].tag / 1440);
        summary.println(ind + ": firstDay=" + firstDay + " width=" + img.getWidth() + " height=" + img.getHeight());
        final int w = img.getWidth();
        final int h = img.getHeight();
        for (final NetImage image : images) {
            final int x = (int) image.in[ind];
            final int y = (int) (image.tag / 1440) - firstDay;
            if (x < 0 || x >= w || y < 0 || y >= h) {
                summary.println("x=" + x + " y=" + y);
            }
            final int color = colors[(int) (image.out[0]) + 1];
            img.setRGB(x, y, color);
        }
        for (int i = 0; i < w; ++i) {
            img.setRGB(i, 0, 0x00FF00);
            if (i % 60 == 0) {
                for (int j = 0; j < h; ++j) {
                    img.setRGB(i, j, 0x00FF00);
                }
            }
        }
    }

    private void fillRGBImage2(final BufferedImage img, final NetImage[] images, final int ind) {
        final int[] colors = new int[]{0x0000FF, 0x7F7F7F, 0xFF0000};
        final int firstDay = (int) (images[0].tag / 1440);
        summary.println(ind + ": firstDay=" + firstDay + " width=" + img.getWidth() + " height=" + img.getHeight());
        final int w = img.getWidth();
        final int h = img.getHeight();
        final int[] stat0 = new int[w];
        final int[] stat1 = new int[w];
        final int[] stat2 = new int[w];
        for (final NetImage image : images) {
            final int x = (int) image.in[ind];
            final int o = (int) image.out[0];
            if (o == 1) {
                stat2[x]++;
            } else {
                if (o == -1) {
                    stat0[x]++;
                } else {
                    stat1[x]++;
                }
            }
        }
        for (int i = 0; i < w; ++i) {
            for (int j = 0; j < h; ++j) {
                img.setRGB(i, j, 0xFFFFFF);
            }
        }
        for (int i = 0; i < w; ++i) {
            img.setRGB(i, h / 2, 0x00FF00);
            if (i % 60 == 0) {
                for (int j = 0; j < h; ++j) {
                    img.setRGB(i, j, 0x00FF00);
                }
            }
        }
        for (int i = 0; i < w; ++i) {
            final int rel = (int) (h * Math.log((double) stat2[i] / stat0[i]) * 0.5D + h * 0.5D);
            if (0 < rel && rel <= h) {
                img.setRGB(i, h - rel, colors[0]);
            }
        }
    }

    private void fillRGBImage3(final BufferedImage img, final NetImage[] images, final int ind1, final int ind2) {
        summary.println(ind1 + " " + ind2 + " width=" + img.getWidth() + " height=" + img.getHeight());
        final int w = img.getWidth();
        final int h = img.getHeight();
        final int[][] intD = new int[3][];
        for (int i = 0; i < intD.length; ++i) {
            intD[i] = new int[w * h];
        }
        for (final NetImage image : images) {
            final int x = (int) image.in[ind1];
            final int y = (int) image.in[ind2];
            if (x < 0 || x >= w || y < 0 || y >= h) {
                summary.println("x=" + x + " y=" + y);
            }
            intD[(int) (image.out[0]) + 1][x + y * w]++;
        }
        //TODO: нормализовать цвета

        final int[] intRGB = new int[w * h];
        for (int i = 0; i < intRGB.length; ++i) {
            for (int j = 0; j < intD.length; j++) {
                intD[j][i] <<= 6;
                if (intD[j][i] > 0xFF) intD[j][i] = 0xFF;
            }
            intRGB[i] = intD[0][i] | (intD[1][i] << 8) | (intD[2][i] << 16);
        }
        img.setRGB(0, 0, w, h, intRGB, 0, h);
    }

    public static void main(final String[] args) {

    }


}
