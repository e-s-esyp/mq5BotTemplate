package com.gmware.lib.neuro.mynet;

import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.net2.ImmutableNNet3Le;

import java.io.*;

/**
 * Created by Gauss on 12.04.2016.
 */
public class NeuroCSVMapOnImages {

    static void make(final File mapFile, final NetImage[] images, final File summaryFile) throws IOException {
        if (mapFile.exists()) {
            final long startTime = System.nanoTime();
            final DataInputStream mapDis = new DataInputStream(new BufferedInputStream(new FileInputStream(mapFile)));
            final NeuroMap map = NeuroMap.load(mapDis);
            final PrintStream summary = new PrintStream(new BufferedOutputStream(new FileOutputStream(summaryFile, true)));
            final long middleTime = System.nanoTime();
            for (final NetImage image : images) {
                summary.println(arrayToCsv1(image.out) + "," + arrayToCsv2(map.propagate(image.in)));
            }
            summary.flush();
            final long finishTime = System.nanoTime();
            System.out.println("время загрузки " + (middleTime - startTime) / 1000000000 + "s");
            System.out.println("время выплнения " + (finishTime - middleTime) / 1000000000 + "s");
        }
    }

    static void make2(final File netFile, final NetImage[] images, final File summaryFile) throws IOException {
        if (netFile.exists()) {
            final long startTime = System.nanoTime();
            final DataInputStream netDis = new DataInputStream(new BufferedInputStream(new FileInputStream(netFile)));
            final ImmutableNNet3Le net = new ImmutableNNet3Le(netDis, true, true);
            final PrintStream summary = new PrintStream(new BufferedOutputStream(new FileOutputStream(summaryFile, true)));
            final long middleTime = System.nanoTime();
            for (final NetImage image : images) {
                summary.println(arrayToCsv1(image.out) + "," + arrayToCsv2(net.propagateMulty(image.in)));
            }
            summary.flush();
            final long finishTime = System.nanoTime();
            System.out.println("время загрузки " + (middleTime - startTime) / 1000000000 + "s");
            System.out.println("время выплнения " + (finishTime - middleTime) / 1000000000 + "s");
        }
    }


    private static String arrayToCsv1(final double[] out) {
        if (out == null) return "";
        if (out.length == 0) return "";
        String s = fcrToString(out[0]);
        for (int i = 1; i < out.length; i++) {
            s += "," + fcrToString(out[i]);
        }
        return s;
    }

    private static String fcrToString(final double v) {
        if (v == +0.5) return "1";
        if (v == -0.5) return "0";
        return "";
    }

    private static String arrayToCsv2(final double[] out) {
        if (out == null) return "";
        if (out.length == 0) return "";
        String s = doubleToString(out[0]);
        for (int i = 1; i < out.length; i++) {
            s += "," + doubleToString(out[i]);
        }
        return s;
    }

    private static String doubleToString(final double v) {
        return String.format("%1.6f", v + 0.5).replace(',', '.');
    }

    static NetImage[] imagesBuffer = new NetImage[2000000];

    static synchronized public NetImage[] assignImages(final DataInputStream dis) {
        int numImages = 0;
        int index = 0;
        try {
            while (true) {
                imagesBuffer[index] = new NetImage(dis);
                numImages++;
                index = numImages % imagesBuffer.length;
            }
        } catch (final EOFException ignored) {
        } catch (final IOException e) {
            e.printStackTrace();
        }
        numImages = (numImages < imagesBuffer.length) ? numImages : imagesBuffer.length;
        final NetImage[] images = new NetImage[numImages];
        System.arraycopy(imagesBuffer, 0, images, 0, numImages);
        return images;
    }

    static public void main(final String args[]) throws IOException {
        final String mapName = "\\\\10.10.40.53\\Gauss\\RESULT\\PNL13\\nets\\PBC2\\SOLVER1\\RESULT\\actions-FR0024.net";
        final String netName = "\\\\10.10.40.53\\Gauss\\RESULT\\PNL13\\nets\\net4\\actions-FR0024.net4";
        final String imagesName = "\\\\10.10.40.53\\Share\\PNL13\\images2-validation\\actions-FR0024";
        final String summaryName = "D:\\DATA\\actions-FR0024-PBC2-v2.csv";
        final String summaryName2 = "D:\\DATA\\actions-FR0024-TNL-v2.csv";
        final File imageFile = new File(imagesName);
        if (imageFile.exists()) {
            final long startTime = System.nanoTime();
            final DataInputStream imagesDis = new DataInputStream(new BufferedInputStream(new FileInputStream(imageFile)));
            final NetImage[] images = assignImages(imagesDis);
            final long middleTime = System.nanoTime();
            System.out.println("время загрузки " + (middleTime - startTime) / 1000000000 + "s");
            System.out.println("количество образов " + images.length);
            make(new File(mapName), images, new File(summaryName));
            make2(new File(netName), images, new File(summaryName2));
        }
    }
}
