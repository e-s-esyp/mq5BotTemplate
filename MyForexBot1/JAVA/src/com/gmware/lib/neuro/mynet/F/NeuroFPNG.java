package com.gmware.lib.neuro.mynet.F;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.NeuroImage;
import com.gmware.lib.games.holdem.common.rnd.Rnd517;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * ---
 * Created by Gauss on 17.05.2016.
 */
public class NeuroFPNG {
    static class PNGImage {
        double in1d = 0;
        double in2d = 0;
        int in1 = 0;
        int in2 = 0;
        int numOut = 0;
        double out0 = 0;
        double out1 = 0;
        double out2 = 0;
        double random = 0;

        public PNGImage(final int ind1, final int ind2, final NeuroImage image, final double rnd) {
            in1d = image.in[ind1];
            in2d = image.in[ind2];
            numOut = image.numOut;
            if (numOut == 2) {
                out0 = image.out[0];
                out1 = image.out[1];
            } else {
                out0 = image.out[0];
                out1 = image.out[2];
                out2 = image.out[1];
            }
            random = rnd;
        }
    }

    static class PNGoutImage {
        double outd0 = 0;
        double outd1 = 0;
        double outd2 = 0;
        int outn = 0;
        double outAv = 0;
        int out = 0;
    }

    static class PNGGraph {
        double in1d = 0;
        double out0o = 0;
        double out1o = 0;
        double out0s = 0;
        double out1s = 0;
        double random = 0;

        public PNGGraph(final int ind1, final NeuroImage imageo, final NeuroImage images, final double rnd) {
            in1d = imageo.in[ind1];
            out0o = imageo.out[0];
            out1o = imageo.out[1];
            out0s = images.out[0];
            out1s = images.out[1];
            random = rnd;
        }
    }

    static class PNGoutGraph {
        int x = 0;
        double y1 = 0;
        double y2 = 0;
        double y3 = 0;
        double y4 = 0;
        int i1 = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int s = 0;
    }

    public static void drawTable1(
            final int size,
            final PNGImage[] p,
            final int mg,
            final double[] edge1,
            final double[] edge2,
            final int in1,
            final int in2,
            final String fileName,
            final String ad) throws IOException {
        final int numOut = p[0].numOut;
        final PNGoutImage[][] table = new PNGoutImage[size][];
        for (int i = 0; i < table.length; i++) {
            table[i] = new PNGoutImage[size];
            for (int j = 0; j < table[i].length; j++) {
                table[i][j] = new PNGoutImage();
            }
        }
        for (final PNGImage image : p) {
            table[image.in1][image.in2].outd0 += image.out0;
            table[image.in1][image.in2].outd1 += image.out1;
            if (numOut == 3)
                table[image.in1][image.in2].outd2 += image.out2;
            table[image.in1][image.in2].outn++;
        }
        final PNGoutImage[] row = new PNGoutImage[size * size];
        int k = 0;
        for (final PNGoutImage[] a : table) {
            for (final PNGoutImage b : a) {
                row[k++] = b;
            }
        }
        Arrays.sort(row, new Comparator<PNGoutImage>() {
            @Override
            public int compare(final PNGoutImage i1, final PNGoutImage i2) {
                final double o3 = i1.outd0;
                final double o4 = i2.outd0;
                if (o3 < o4) return 1;
                if (o3 > o4) return -1;
                return 0;
            }
        });
        // blue
        for (int i = 0; i < row.length; ++i) {
            row[i].out = (i << 8) / row.length;
        }
        Arrays.sort(row, new Comparator<PNGoutImage>() {
            @Override
            public int compare(final PNGoutImage i1, final PNGoutImage i2) {
                final double o3 = i1.outd1;
                final double o4 = i2.outd1;
                if (o3 < o4) return 1;
                if (o3 > o4) return -1;
                return 0;
            }
        });
        // red
        for (int i = 0; i < row.length; ++i) {
            row[i].out |= ((i << 8) / row.length) << 16;
        }
//        Arrays.sort(row, new Comparator<PNGoutImage>() {
//            @Override
//            public int compare(final PNGoutImage i1, final PNGoutImage i2) {
//                final double o3 = i1.outn;
//                final double o4 = i2.outn;
//                if (o3 < o4) return -1;
//                if (o3 > o4) return 1;
//                return 0;
//            }
//        });
//        // green
//        for (int i = 0; i < row.length; ++i) {
//            row[i].out |= ((i << 7) / row.length) << 8;
//        }
        final int marginX = 70;
        final int marginY = 60;
        final BufferedImage img = new BufferedImage(size * mg + marginX, size * mg + marginY, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[i].length; j++) {
                if (table[i][j].outn != 0) {
                    for (int l = 0; l < mg; l++) {
                        for (int m = 0; m < mg; m++) {
                            img.setRGB(i * mg + l, j * mg + m, table[i][j].out);
                        }
                    }
                    if (numOut == 2) {
                        if (table[i][j].outd0 != table[i][j].outd1) {
                            final int color = (table[i][j].outd0 > table[i][j].outd1) ? 0xFF0000 : 0x0000FF;
                            for (int l = mg / 4; l < 3 * mg / 4; l++) {
                                for (int m = mg / 4; m < 3 * mg / 4; m++) {
                                    img.setRGB(i * mg + l, j * mg + m, color);
                                }
                            }
                        }
                    } else {
                        final int color;
                        if (table[i][j].outd0 > table[i][j].outd2 || table[i][j].outd1 > table[i][j].outd2) {
                            color = (table[i][j].outd0 > table[i][j].outd1) ? 0xFF0000 : 0x0000FF;
                        } else {
                            color = 0x7F7F7F;
                        }
                        for (int l = mg / 4; l < 3 * mg / 4; l++) {
                            for (int m = mg / 4; m < 3 * mg / 4; m++) {
                                img.setRGB(i * mg + l, j * mg + m, color);
                            }

                        }
                    }
                } else {
                    for (int l = 0; l < mg; l++) {
                        for (int m = 0; m < mg; m++) {
                            img.setRGB(i * mg + l, j * mg + m, 0x00FF00);
                        }
                    }
                }
                if (mg >= 40) {
                    final char[] sn = String.format("%d", table[i][j].outn).toCharArray();
                    final char[] s0 = String.format("%5.3f", table[i][j].outd0).toCharArray();
                    final char[] s1 = String.format("%5.3f", table[i][j].outd1).toCharArray();
                    img.getGraphics().drawChars(sn, 0, sn.length, i * mg, j * mg + 12);
                    img.getGraphics().drawChars(s0, 0, s0.length, i * mg, j * mg + 24);
                    img.getGraphics().drawChars(s1, 0, s1.length, i * mg, j * mg + 36);
                    if (numOut == 3 && mg >= 50) {
                        final char[] s2 = String.format("=%5.3f", table[i][j].outd2).toCharArray();
                        img.getGraphics().drawChars(s2, 0, s2.length, i * mg, j * mg + 48);
                    }
                }
//                System.out.print(table[i][j].outd0 + "\t");
            }
//            System.out.print("\n");
        }
        //X
        char[] s = String.format("[%d]", in1).toCharArray();
        img.getGraphics().drawChars(s, 0, s.length, 2, size * mg + 12);
        for (int i = 1; i < size; i++) {
            s = String.format("%9.6f", edge1[i]).toCharArray();
            img.getGraphics().drawChars(s, 0, s.length, i * mg + 2, size * mg + 12 + (i % 4) * 12);
        }
        //Y
        s = String.format("[%d]", in2).toCharArray();
        img.getGraphics().drawChars(s, 0, s.length, size * mg, 12);
        for (int i = 1; i < size; i++) {
            s = String.format("%9.6f", edge2[i]).toCharArray();
            img.getGraphics().drawChars(s, 0, s.length, size * mg, i * mg + 3);
        }
        ImageIO.write(img, "png", new File(fileName + "." + String.format("%02d", in1) + "." + String.format("%02d", in2) + ad + ".png"));

    }

    public static void drawTable2(
            final int size,
            final PNGImage[] p,
            final int mg,
            final double[] edge1,
            final double[] edge2,
            final int in1,
            final int in2,
            final String fileName,
            final String ad) throws IOException {
        final int numOut = p[0].numOut;
        final PNGoutImage[][] table = new PNGoutImage[size][];
        for (int i = 0; i < table.length; i++) {
            table[i] = new PNGoutImage[size];
            for (int j = 0; j < table[i].length; j++) {
                table[i][j] = new PNGoutImage();
            }
        }
        for (final PNGImage image : p) {
            table[image.in1][image.in2].outd0 += image.out0;
            table[image.in1][image.in2].outd1 += image.out1;
            if (numOut == 3) {
                table[image.in1][image.in2].outd2 += image.out2;
            }
            table[image.in1][image.in2].outn++;
        }
        final PNGoutImage[] row = new PNGoutImage[size * size];
        int k = 0;
        for (final PNGoutImage[] a : table) {
            for (final PNGoutImage b : a) {
                row[k++] = b;
            }
        }
        for (final PNGoutImage a : row) {
            if (a.outn > 0) {
                a.outAv = (a.outd0 - a.outd1) / a.outn;
            } else {
                a.outAv = 0;
            }
        }
        Arrays.sort(row, new Comparator<PNGoutImage>() {
            @Override
            public int compare(final PNGoutImage i1, final PNGoutImage i2) {
                final double o3 = i1.outAv;
                final double o4 = i2.outAv;
                if (o3 < o4) return 1;
                if (o3 > o4) return -1;
                return 0;
            }
        });
        // blue
        for (int i = 0; i < row.length; ++i) {
            row[i].out = (i << 8) / row.length;
        }
        final int marginX = 70;
        final int marginY = 60;
        final BufferedImage img = new BufferedImage(size * mg + marginX, size * mg + marginY, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[i].length; j++) {
                if (table[i][j].outn != 0) {
                    for (int l = 0; l < mg; l++) {
                        for (int m = 0; m < mg; m++) {
                            img.setRGB(i * mg + l, j * mg + m, table[i][j].out);
                        }
                    }
                } else {
                    for (int l = 0; l < mg; l++) {
                        for (int m = 0; m < mg; m++) {
                            img.setRGB(i * mg + l, j * mg + m, 0x00FF00);
                        }
                    }
                }
                if (mg >= 40) {
                    final char[] sn = String.format("%d", table[i][j].outn).toCharArray();
                    final char[] s0 = String.format("%5.3f", table[i][j].outd0).toCharArray();
                    final char[] s1 = String.format("%5.3f", table[i][j].outd1).toCharArray();
                    img.getGraphics().drawChars(sn, 0, sn.length, i * mg, j * mg + 10);
                    img.getGraphics().drawChars(s0, 0, s0.length, i * mg, j * mg + 20);
                    img.getGraphics().drawChars(s1, 0, s1.length, i * mg, j * mg + 30);
                    if (numOut == 3) {
                        final char[] s2 = String.format("=%5.3f", table[i][j].outd2).toCharArray();
                        img.getGraphics().drawChars(s2, 0, s2.length, i * mg, j * mg + 40);
                    }
                }
//                System.out.print(table[i][j].outd0 + "\t");
            }
//            System.out.print("\n");
        }
        //X
        for (int i = 1; i < size; i++) {
            final char[] s = String.format("%9.6f", edge1[i]).toCharArray();
            img.getGraphics().drawChars(s, 0, s.length, i * mg + 2, size * mg + 12 + (i % 4) * 12);
        }
        //Y
        for (int i = 1; i < size; i++) {
            final char[] s = String.format("%9.6f", edge2[i]).toCharArray();
            img.getGraphics().drawChars(s, 0, s.length, size * mg, i * mg + 3);
        }
        ImageIO.write(img, "png", new File(fileName + "." + String.format("%02d", in1) + "." + String.format("%02d", in2) + ad + ".png"));

    }

    public static void printPNGimage(
            final NeuroImage[] images,
            final int size,           //размер изображения
            final int in1,
            final int in2,
            final String fileName,
            final String ad,
            final int fullSize          //[640]
    ) throws IOException {
        final int mg = fullSize / size; //[20]
        final Rnd rnd = new Rnd517(0);
        final PNGImage[] p = new PNGImage[images.length];
        for (int i = 0; i < p.length; ++i) {
            p[i] = new PNGImage(in1, in2, images[i], rnd.rnd());
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
        final double[] edge1 = new double[size];
        for (int i = 0; i < edge1.length; i++) {
            edge1[i] = p[i * d].in1d;
        }
        //--------------------------------------------
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
        final double[] edge2 = new double[size];
        for (int i = 0; i < edge2.length; i++) {
            edge2[i] = p[i * d].in2d;
        }
        //--------------------------------------------
        if (DRAW_TYPE == 1) {
            drawTable1(size, p, mg, edge1, edge2, in1, in2, fileName, ad);
        } else {
            drawTable2(size, p, mg, edge1, edge2, in1, in2, fileName, ad);
        }
    }

    public static void printPNGimageRealScale(
            final NeuroImage[] images,
            final int size,//размер картинки
            final int in1,
            final int in2,
            final String fileName,
            final String ad,
            final int fullSize) throws IOException {
        final int mg = fullSize / size;
        final Rnd rnd = new Rnd517(0);
        final PNGImage[] p = new PNGImage[images.length];
        for (int i = 0; i < p.length; ++i) {
            p[i] = new PNGImage(in1, in2, images[i], rnd.rnd());
        }
        //--------------------------------------------
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
        final double min1 = p[d].in1d;
        final double max1 = p[(size - 1) * d].in1d;
        final double[] edge1 = new double[size];
        for (int i = 0; i < size; i++) {
            edge1[i] = (((i - 1) * (max1 - min1)) / (size - 2)) + min1;
        }
        for (int i = 0; i < d; ++i) {
            p[i].in1 = 0;
        }
        for (int i = d; i < p.length - d; ++i) {
            final PNGImage a = p[i];
            a.in1 = (int) (((a.in1d - min1) * (size - 2)) / (max1 - min1)) + 1;
            if (a.in1 >= size) {
                a.in1 = size - 1;
            }
            if (a.in1 < 0) {
                a.in1 = 0;
            }
            if (a.in1 < 0 || a.in1 >= size) {
                System.out.println("error");
            }
        }
        for (int i = p.length - d; i < p.length; ++i) {
            p[i].in1 = size - 1;
        }
        //--------------------------------------------
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
        final double min2 = p[d].in2d;
        final double max2 = p[(size - 1) * d].in2d;
        final double[] edge2 = new double[size];
        for (int i = 0; i < size; i++) {
            edge2[i] = (((i - 1) * (max2 - min2)) / (size - 2)) + min2;
        }

        for (int i = 0; i < d; ++i) {
            p[i].in2 = 0;
        }
        for (int i = d; i < p.length - d; ++i) {
            final PNGImage a = p[i];
            a.in2 = (int) (((a.in2d - min2) * (size - 2)) / (max2 - min2)) + 1;
            if (a.in2 >= size) {
                a.in2 = size - 1;
            }
            if (a.in2 < 0) {
                a.in2 = 0;
            }
            if (a.in2 < 0 || a.in2 >= size) {
                System.out.println("error");
            }
        }
        for (int i = p.length - d; i < p.length; ++i) {
            p[i].in2 = size - 1;
        }
        //--------------------------------------------
        if (DRAW_TYPE == 1) {
            drawTable1(size, p, mg, edge1, edge2, in1, in2, fileName, ad);
        } else {
            drawTable2(size, p, mg, edge1, edge2, in1, in2, fileName, ad);
        }
    }

    public static void printPNGgraph(
            final NeuroImage[] imageso,
            final NeuroImage[] imagess,
            final int size,//размер картинки
            final int in1,
            final String fileName,
            final int fullSize) throws IOException {
        final int mg = fullSize / size;
        final Rnd rnd = new Rnd517(0);
        final PNGGraph[] p = new PNGGraph[imageso.length];
        for (int i = 0; i < p.length; ++i) {
            p[i] = new PNGGraph(in1, imageso[i], imagess[i], rnd.rnd());
        }
        Arrays.sort(p, new Comparator<PNGGraph>() {
            @Override
            public int compare(final PNGGraph i1, final PNGGraph i2) {
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
        final PNGoutGraph[] pogs = new PNGoutGraph[size];
        for (int i = 0; i < p.length; ++i) {
            final int ind = i / d;
            if (pogs[ind] == null) {
                pogs[ind] = new PNGoutGraph();
            }
            final PNGoutGraph pog = pogs[ind];
            pog.x = ind;
            pog.y1 += p[i].out0o;
            pog.y2 += p[i].out0s;
            pog.y3 += p[i].out1o;
            pog.y4 += p[i].out1s;
            pog.s++;
        }
        for (final PNGoutGraph pog : pogs) {
            if (pog == null) continue;
            if (pog.s == 0) continue;
            pog.y1 /= pog.s;
            pog.y2 /= pog.s;
            pog.y3 /= pog.s;
            pog.y4 /= pog.s;
        }
        double min1 = Double.MAX_VALUE;
        double min2 = Double.MAX_VALUE;
        double min3 = Double.MAX_VALUE;
        double min4 = Double.MAX_VALUE;
        double max1 = -Double.MAX_VALUE;
        double max2 = -Double.MAX_VALUE;
        double max3 = -Double.MAX_VALUE;
        double max4 = -Double.MAX_VALUE;
        for (final PNGoutGraph pog : pogs) {
            if (pog == null) continue;
            if (min1 > pog.y1) min1 = pog.y1;
            if (min2 > pog.y2) min2 = pog.y2;
            if (min3 > pog.y3) min3 = pog.y3;
            if (min4 > pog.y4) min4 = pog.y4;
            if (max1 < pog.y1) max1 = pog.y1;
            if (max2 < pog.y2) max2 = pog.y2;
            if (max3 < pog.y3) max3 = pog.y3;
            if (max4 < pog.y4) max4 = pog.y4;
        }
        for (final PNGoutGraph pog : pogs) {
            if (pog == null) continue;
            pog.i1 = (int) (((size - 1) * (pog.y1 - min1)) / (max1 - min1));
            pog.i2 = (int) (((size - 1) * (pog.y2 - min2)) / (max2 - min2));
            pog.i3 = (int) (((size - 1) * (pog.y3 - min3)) / (max3 - min3));
            pog.i4 = (int) (((size - 1) * (pog.y4 - min4)) / (max4 - min4));
        }
        final BufferedImage img = new BufferedImage(size * mg, size * mg, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < size * mg; ++i) {
            for (int j = 0; j < size * mg; ++j) {
                img.setRGB(i, j, 0xFFFFFF);
            }
        }
        for (int i = 0; i < pogs.length; ++i) {
            final PNGoutGraph pog = pogs[i];
            if (pog == null) continue;
            bar(img, i * mg, 0, i * mg + 4, pog.i1 * mg, 0xFF0000);
            bar(img, i * mg + 5, 0, i * mg + 9, pog.i2 * mg, 0xFF7F7F);
            bar(img, i * mg + 10, 0, i * mg + 14, pog.i3 * mg, 0x0000FF);
            bar(img, i * mg + 15, 0, i * mg + 19, pog.i4 * mg, 0x7F7FFF);
        }
        /*
        for (int i = 1; i < pogs.length; ++i) {
            final PNGoutGraph pog1 = pogs[i - 1];
            final PNGoutGraph pog2 = pogs[i];
            if (pog1 == null || pog2 == null) continue;
            line(img, (i - 1) * mg, pog1.i1 * mg, i * mg, pog2.i1 * mg, 0xFF0000);
            line(img, (i - 1) * mg, pog1.i2 * mg, i * mg, pog2.i2 * mg, 0xFF7F7F);
            line(img, (i - 1) * mg, pog1.i3 * mg, i * mg, pog2.i3 * mg, 0x0000FF);
            line(img, (i - 1) * mg, pog1.i4 * mg, i * mg, pog2.i4 * mg, 0x7F7FFF);
        }
        */
        ImageIO.write(img, "png", new File(fileName + "." + String.format("%02d", in1) + ".g.png"));
    }

    public static void printPNGtimedDouble(
            final NeuroFSimulatorTiming[] result,
            final String s,
            final long[] middleTime,
            final long middlePoint) throws IOException {
        final int width = result.length / 200 + 2;
        int min = 0;
        int max = 0;
        final int startValue = (int) (result[0].ask.close * 2000);
        for (final NeuroFSimulatorTiming x : result) {
            final int profit = (int) (x.value * 2000);
            if (profit < min) {
                min = profit;
            }
            if (profit > max) {
                max = profit;
            }
            final int value = (int) (x.ask.close * 2000) - startValue;
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }
        final int zero = min;
        final int height = max - min;
        final BufferedImage img = new BufferedImage(width, height + 2, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                img.setRGB(i, j, 0xFFFFFF);
            }
        }
        line(img, 0, height + zero, width - 1, height + zero, 0x00FF00);
        int lastProfit = 0;
        int lastValue = 0;
        int j = 1;
        int i = 1;
        for (final NeuroFSimulatorTiming x : result) {
            final int profit = (int) (x.value * 2000);
            final int value = (int) (x.ask.close * 2000) - startValue;
            line(img, i, height + zero - lastProfit, i, height + zero - profit, 0xFF0000);
            line(img, i, height + zero - lastValue, i, height + zero - value, 0x0000FF);
            lastProfit = profit;
            lastValue = value;
            ++j;
            if (j % 200 == 199) i++;
        }
        for (final long t : middleTime) {
            line(img, (int) (t / 200), 0, (int) (t / 200), height - 1, 0x00FF00);
        }
        line(img, (int) (middlePoint / 200), 0, (int) (middlePoint / 200), height - 1, 0x000000);
        ImageIO.write(img, "png", new File(s));
    }

    private static void line(
            final BufferedImage img,
            int x1,
            int y1,
            int x2,
            int y2,
            final int c) {
        final int w = img.getWidth();
        final int h = img.getHeight();
        if (x2 < x1) {
            final int x3 = x1;
            x1 = x2;
            x2 = x3;
        }
        if (y2 < y1) {
            final int y3 = y1;
            y1 = y2;
            y2 = y3;
        }
        final int lx = x2 - x1;
        final int ly = y2 - y1;
        if (lx > ly) {
            for (int i = 0; i <= lx; ++i) {
                final int x = x1 + i;
                final int y = y1 + (i * ly) / lx;
                if (x >= 0 && x < w && y >= 0 && y < h) img.setRGB(x, y, c);
            }
        } else {
            if (ly == 0) {
                img.setRGB(x1, y1, c);
                return;
            }
            for (int i = 0; i <= ly; ++i) {
                final int x = x1 + (i * lx) / ly;
                final int y = y1 + i;
                if (x >= 0 && x < w && y >= 0 && y < h) img.setRGB(x, y, c);
            }
        }
    }

    private static void bar(
            final BufferedImage img,
            final int x1,
            int y1,
            final int x2,
            int y2,
            final int c) {
        final int w = img.getWidth();
        final int h = img.getHeight();
        y1 = h - y1 - 1;
        y2 = h - y2 - 1;
        for (int x = x1; x <= x2; ++x) {
            for (int y = y2; y <= y1; ++y) {
                if (x >= 0 && x < w && y >= 0 && y < h) img.setRGB(x, y, c);
            }
        }
    }

    static final String[] NAMES = {
            "\\0-close-up.map",
            "\\1-open.map",
            "\\2-close-down.map"};

    static void print1(final String[] args) throws IOException {
        final long startNanoTime = System.nanoTime();
        final String pathResult = "G:\\F\\test\\PNG-PREDICTOR-2012-2016";
        final String trainNamePath = "G:\\F\\test\\t4\\log-bin\\" + "EU1-gkfx-2012-2015.bin";
        final String predictorPath = "G:\\F\\test\\t8\\DYCELLS-single0.5.4-EU1-gkfx-2012-2015.bin-EU1-gkfx-2012-2016.bin\\PREDICTOR\\-13-00-01";
        //--------------------------------------------------------------------------------------------------------------
        final File resultDir = new File(pathResult);
        resultDir.mkdirs();
        final TimedDoubles[] trainData = NeuroFLoader.loadBinData(new File(trainNamePath));
//        TimedDoubles.randomMix(trainData, new Rnd517(0));
//        final PrintStream log = new PrintStream(pathResult + "\\predictor.log");
        final int numImages = NeuroF.getNumImages(trainData, 1);
        final NeuroImage[] images0 = new NeuroImage[numImages];
        final NeuroImage[] images1 = new NeuroImage[numImages];
        final NeuroImage[] images2 = new NeuroImage[numImages];
        NeuroF.fillImages(trainData, images0, images1, images2);
        final NeuroFPredictorForest predictor = new NeuroFPredictorForest(predictorPath);
        final NeuroImage[] images0s = predictor.simulate(0, images0);
        printPNGimage(images0, SIZE, 2, 5, pathResult + NAMES[0], ".o", FULL_SIZE);
        printPNGimage(images0s, SIZE, 2, 5, pathResult + NAMES[0], ".s", FULL_SIZE);
        printPNGimage(images0, SIZE, 3, 24, pathResult + NAMES[0], ".o", FULL_SIZE);
        printPNGimage(images0s, SIZE, 3, 24, pathResult + NAMES[0], ".s", FULL_SIZE);
        for (int i = 1; i < images0[0].numIn; i++) {
            printPNGimage(images0, SIZE, i - 1, i, pathResult + NAMES[0], ".o", FULL_SIZE);
            printPNGimage(images0s, SIZE, i - 1, i, pathResult + NAMES[0], ".s", FULL_SIZE);
        }
        for (int i = 0; i < images0[0].numIn; i++) {
            printPNGgraph(images0, images0s, SIZE, i, pathResult + NAMES[0], FULL_SIZE);
        }
        System.out.println("elapsed time = " + (System.nanoTime() - startNanoTime) / 1000000 + "ms");
    }

    static void print2(final String[] args) throws IOException {
        final long startNanoTime = System.nanoTime();
        final String pathResult = PATH_RESULT + "-" + SIZE;
        final String trainNamePath = TRAIN_NAME_PATH;
        //--------------------------------------------------------------------------------------------------------------
        final File resultDir = new File(pathResult);
        resultDir.mkdirs();
        final TimedDoubles[] trainData = NeuroFLoader.loadBinData(new File(trainNamePath));
//        TimedDoubles.randomMix(trainData, new Rnd517(0));
//        final PrintStream log = new PrintStream(pathResult + "\\predictor.log");
        final int numImages = NeuroF.getNumImages(trainData, 1);
        final NeuroImage[] images0 = new NeuroImage[numImages];
        final NeuroImage[] images1 = new NeuroImage[numImages];
        final NeuroImage[] images2 = new NeuroImage[numImages];
        NeuroF.fillImages(trainData, images0, images1, images2);
        for (int i = 1; i < images0[0].numIn; i++) {
            for (int j = 0; j < i; j++) {
//                if (i != 1 || j != 0) continue;
                printPNGimage(images0, SIZE, j, i, pathResult + NAMES[0], ".o", FULL_SIZE);
                printPNGimage(images1, SIZE, j, i, pathResult + NAMES[1], ".o", FULL_SIZE);
                printPNGimage(images2, SIZE, j, i, pathResult + NAMES[2], ".o", FULL_SIZE);
            }
        }
        System.out.println("elapsed time = " + (System.nanoTime() - startNanoTime) / 1000000 + "ms");
    }

    static void print3(final String[] args) throws IOException {
        final long startNanoTime = System.nanoTime();
        final String pathResult = PATH_RESULT + "-real-" + SIZE;
        final String trainNamePath = TRAIN_NAME_PATH;
        //--------------------------------------------------------------------------------------------------------------
        final File resultDir = new File(pathResult);
        resultDir.mkdirs();
        final TimedDoubles[] trainData = NeuroFLoader.loadBinData(new File(trainNamePath));
//        TimedDoubles.randomMix(trainData, new Rnd517(0));
//        final PrintStream log = new PrintStream(pathResult + "\\predictor.log");
        final int numImages = NeuroF.getNumImages(trainData, 1);
        final NeuroImage[] images0 = new NeuroImage[numImages];
        final NeuroImage[] images1 = new NeuroImage[numImages];
        final NeuroImage[] images2 = new NeuroImage[numImages];
        NeuroF.fillImages(trainData, images0, images1, images2);
        for (int i = 1; i < images0[0].numIn; i++) {
            for (int j = 0; j < i; j++) {
//                if (i != 1 || j != 0) continue;
                printPNGimageRealScale(images0, SIZE, j, i, pathResult + NAMES[0], ".r", FULL_SIZE);
                printPNGimageRealScale(images1, SIZE, j, i, pathResult + NAMES[1], ".r", FULL_SIZE);
                printPNGimageRealScale(images2, SIZE, j, i, pathResult + NAMES[2], ".r", FULL_SIZE);
            }
        }
        System.out.println("elapsed time = " + (System.nanoTime() - startNanoTime) / 1000000 + "ms");
    }

    static void print4(final String[] args) throws IOException {
        final long startNanoTime = System.nanoTime();
        final String pathResult = PATH_RESULT + "-" + SIZE;
        final String trainNamePath = TRAIN_NAME_PATH;
        //--------------------------------------------------------------------------------------------------------------
        final File resultDir = new File(pathResult);
        resultDir.mkdirs();
        final TimedDoubles[] trainData = NeuroFLoader.loadBinData(new File(trainNamePath));
        final int numImages = NeuroF.getNumImages(trainData, 1);
        final NeuroImage[] images0 = new NeuroImage[numImages];
        final NeuroImage[] images1 = new NeuroImage[numImages];
        final NeuroImage[] images2 = new NeuroImage[numImages];
        NeuroF.fillImages(trainData, images0, images1, images2);
        final int[][] sel = new int[][]{
                {10, 1, 19},
                {12, 13, 14},
                {14, 13, 14},
                {24, 0, 9},
                {24, 20, 21},
                {30, 5, 15}
        };
        for (final int[] a : sel) {
            printPNGimage(images0, a[0], a[1], a[2], pathResult + NAMES[0], ".o", a[0] * 40);
            printPNGimage(images1, a[0], a[1], a[2], pathResult + NAMES[1], ".o", a[0] * 50);
            printPNGimage(images2, a[0], a[1], a[2], pathResult + NAMES[2], ".o", a[0] * 40);
        }
        System.out.println("elapsed time = " + (System.nanoTime() - startNanoTime) / 1000000 + "ms");
    }

    final static int DRAW_TYPE = 1;
    final static int SIZE = 40;
    final static int FULL_SIZE = 600;
    final static String PATH_RESULT = "G:\\F\\test\\PNG-selected" + DRAW_TYPE + "-" + FULL_SIZE;
    final static String TRAIN_NAME_PATH = "G:\\F\\test\\t4\\log-bin\\" + "F-2016-1-3.bin";

    public static void main(final String[] args) throws IOException {
        print4(args);
    }

}
