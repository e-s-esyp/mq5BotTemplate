package com.gmware.lib.neuro.mynet.F;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.Mappings.NeuroMappingPartitionByDeterminationNoTest;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapPartition;
import com.gmware.lib.neuro.mynet.NeuroComputationType;
import com.gmware.lib.neuro.mynet.NeuroImage;
import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.games.holdem.common.rnd.Rnd517;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

/**
 * -
 * Created by Gauss on 15.04.2016.
 */
public class NeuroFPredictorBoost {
    static final String[] NAMES = {
            "\\0-close-up.map",
            "\\1-open.map",
            "\\2-close-down.map"};
    static final int[] NUM_KLASSES = {2, 3, 2};
    String path = null;
    TimedDoubles[] knownData = null;
    NeuroMapPartition closeUpMap = null;
    NeuroMapPartition openMap = null;
    NeuroMapPartition closeDownMap = null;
    PrintStream log = null;
    NeuroMapPartition[] maps = new NeuroMapPartition[3];
    int[] mult = new int[]{2, 1, 2};
    NeuroMapPartition[][] maps2 = new NeuroMapPartition[10][];

    static final int[][][] DOWNDATER = new int[][][]{
            {},
            {},
            {{0, 1}, {1, 0}},
            {{1, 2, 0}, {0, 1, 2}, {2, 0, 1}}
    };

    static final int[][][] UPDATER = new int[][][]{
            {},
            {},
            {{0, 1}, {1, 0}},
            {{1, 2, 0}, {0, 1, 2}, {2, 0, 1}}
    };

    static final int[][][] UPDATER2 = new int[][][]{
            {},
            {},
            {{0, 1}, {1, 0}},
            {{0, 2, 1}, {2, 1, 0}, {1, 0, 2}}
    };

    final void setMaps() {
        maps[0] = closeUpMap;
        maps[1] = openMap;
        maps[2] = closeDownMap;
    }

    NeuroFPredictorBoost() {
    }

    NeuroFPredictorBoost(final String path) throws IOException {
        this.path = path;
        closeUpMap = (NeuroMapPartition) NeuroMap.load(path + NAMES[0]);
        openMap = (NeuroMapPartition) NeuroMap.load(path + NAMES[1]);
        closeDownMap = (NeuroMapPartition) NeuroMap.load(path + NAMES[2]);
//        initParameters(data, startInd);
        setMaps();
    }

    void train(
            final TimedDoubles[] trainData,
            final String path) throws IOException {
        this.path = path;
        knownData = trainData;
        initParameters(trainData, trainData.length);
        final int numImages = NeuroF.getNumImages(trainData, 1);
//        System.out.println("images.length = " + numImages);
        final NeuroImage[] images0 = new NeuroImage[numImages];
        final NeuroImage[] images1 = new NeuroImage[numImages];
        final NeuroImage[] images2 = new NeuroImage[numImages];
        NeuroF.fillImages(trainData, images0, images1, images2);
//        printStats(images0);
//        printStats(images1);
//        printStats(images2);
        final Rnd rnd = new Rnd517();
        (closeUpMap = trainMap(images0, rnd, path + NAMES[0] + ".log")).save(path + NAMES[0]);
        (openMap = trainMap(images1, rnd, path + NAMES[1] + ".log")).save(path + NAMES[1]);
        (closeDownMap = trainMap(images2, rnd, path + NAMES[2] + ".log")).save(path + NAMES[2]);
        setMaps();
    }

    void train2(
            final TimedDoubles[] trainData,
            final String path) throws IOException {
        this.path = path;
        knownData = trainData;
        initParameters(trainData, trainData.length);
        final int numImages = NeuroF.getNumImages(trainData, 1);
        final NeuroImage[] images0 = new NeuroImage[numImages];
        final NeuroImage[] images1 = new NeuroImage[numImages];
        final NeuroImage[] images2 = new NeuroImage[numImages];
        NeuroF.fillImages(trainData, images0, images1, images2);
        final Rnd rnd = new Rnd517();
        for (int i = 0; i < maps2.length; i++) {
            (closeUpMap = trainMap(images0, rnd, path + NAMES[0] + ".log")).save(path + NAMES[0]);
            (openMap = trainMap(images1, rnd, path + NAMES[1] + ".log")).save(path + NAMES[1]);
            (closeDownMap = trainMap(images2, rnd, path + NAMES[2] + ".log")).save(path + NAMES[2]);
            maps2[i] = new NeuroMapPartition[]{
                    closeUpMap,
                    openMap,
                    closeDownMap};
            updateImages(closeUpMap, images0);
            updateImages(openMap, images1);
            updateImages(closeDownMap, images2);
        }
    }

    /**
     * @param map
     * @param images
     * @return количество измененных образов
     */
    private int updateImages(final NeuroMapPartition map, final NeuroImage[] images) {
        final int numKlasses = images[0].numKlasses;
        int result = 0;
        for (final NeuroImage image : images) {
            final int klass = map.propagateStructure(image.in).out.klass;
            final int k1 = DOWNDATER[numKlasses][klass][image.klass];
            if (image.klass != k1) {
                result++;
                image.klass = k1;
                final double[] out = new double[numKlasses];
                for (int i = 0; i < numKlasses; i++) {
                    out[i] = image.out[DOWNDATER[numKlasses][klass][i]];
                }
                image.out = out;
            }
        }
        return result;
    }

    private void printStats(final NeuroImage[] images) {
        final double[] e = new double[images[0].numOut];
        final double[] a = new double[images[0].numOut];
        final int[] n = new int[images[0].numOut];
        for (final NeuroImage im : images) {
            for (int i = 0; i < e.length; i++) {
                e[i] += im.out[i];
            }
            a[im.klass] += im.out[im.klass];
            n[im.klass]++;
        }
        for (int i = 0; i < e.length; i++) {
            System.out.print(e[i] + " / " + n[i] + " = " + e[i] / n[i] + "  ");
        }
        System.out.print("(");
        for (int i = 0; i < e.length; i++) {
            System.out.print(a[i] + "  ");
        }
        System.out.print(")");
        System.out.println();
    }

    private NeuroImage[] normalize(final NeuroImage[] images) {
        final double[] e = new double[images[0].numOut];
        final int[] n = new int[images[0].numOut];
        for (final NeuroImage im : images) {
            for (int i = 0; i < e.length; i++) {
                e[i] += im.out[i];
            }
            n[im.klass]++;
        }
        int min = images.length;
        for (final int a : n) {
            if (a < min) min = a;
        }
        final int[] m = new int[e.length];
        final NeuroImage[] result = new NeuroImage[min * e.length];
        for (int i = images.length - 1, j = 0; i >= 0; --i) {
            final int klass = images[i].klass;
            if (m[klass]++ < min) {
                result[j++] = images[i];
            }
        }
        return result;
    }

    private NeuroMapPartition trainMap(
            final NeuroImage[] images,
            final Rnd rnd,
            final String name) throws FileNotFoundException {
        final NeuroComputationType computationType = new NeuroComputationType();
        computationType.setF();
        return NeuroMappingPartitionByDeterminationNoTest.trainContinuous(
                images,
                rnd,
                new PrintStream(new File(name)),
                computationType);
    }

    final void initParameters(final TimedDoubles[] data, final int startInd) {
        NeuroF.setMAs(data);
    }

    public int getDecision(final int order, final TimedDoubles[] data, final int ind) {
        final double[] in = new double[NeuroF.NUM_IMAGES_IN];
        NeuroF.fillIn(ind - NeuroF.NUM_USED_IN1, data, in);
        final int action = maps[order].propagateStructure(in).out.klass * mult[order];
        if (order != action && log != null) {
            log.println(maps[order].getPropagation(in)[0]);
        }
        return action;
    }

    public int getDecisionRandom(final int order, final TimedDoubles[] data, final int ind) {
        if (Math.random() < 0.0002) {
            return 2 * (int) (Math.random() * 2);
        } else {
            return order;
        }
    }

    public int getDecision2(final int order, final TimedDoubles[] data, final int ind) {
        final double[] in = new double[NeuroF.NUM_IMAGES_IN];
        NeuroF.fillIn(ind - NeuroF.NUM_USED_IN1, data, in);
        int action = -1;
        for (final NeuroMapPartition[] ms : maps2) {
            if (action == -1) {
                action = ms[order].propagateStructure(in).out.klass;
            } else {
                if (ms != null)
                    action = UPDATER[NUM_KLASSES[order]][action][ms[order].propagateStructure(in).out.klass];
            }
        }
        action *= mult[order];
        return action;
    }

//----------------------------------------------------------------------------------------------------------------------

    void train3(
            final NeuroImage[] images,
            final String path) throws IOException {
        this.path = path;
        (new File(path)).mkdirs();
        final Rnd rnd = new Rnd517();
        int k = 0;
        for (int i = 0; i < maps2.length; i++) {
            final String name = path + NAMES[1] + "." + i;
            (openMap = trainMap(images, rnd, name + ".log")).save(name);
            if (updateImages(openMap, images) > 0) {
                maps2[k++] = new NeuroMapPartition[]{
                        openMap
                };
            }
            if (numKlasses(images) == 1) {
                for (; k < maps2.length; k++) {
                    maps2[k] = null;
                }
                return;
            }
        }
    }

    private int numKlasses(final NeuroImage[] images) {
        final int numKlasses = images[0].numKlasses;
        final int[] nk = new int[numKlasses];
        for (final NeuroImage image : images) {
            nk[image.klass]++;
        }
        int result = 0;
        for (int i = 0; i < numKlasses; i++) {
            if (nk[i] > 0) result++;
        }
        return result;
    }

    public static void main(final String[] args) throws IOException {
        final int[][] im = new int[][]{
                {0, 0, 1},
                {1, 1, 0},
                {1, 1, 0},
                {1, 1, 0},
        };
        final NeuroImage[] images = new NeuroImage[12];
        int k = 0;
        for (int i = 0; i < im.length; i++) {
            final int[] imm = im[i];
            for (int j = 0; j < imm.length; j++) {
                final NeuroImage image = new NeuroImage();
                image.numIn = 2;
                image.in = new double[]{i, j};
                image.numKlasses = 2;
                image.klass = imm[j];
                image.numOut = 2;
                if (imm[j] == 0) {
                    image.out = new double[]{0, -1};
                } else {
                    image.out = new double[]{-1, 0};
                }
                images[k++] = image;
            }
        }
        final NeuroFPredictorBoost p = new NeuroFPredictorBoost();
        p.train3(images, "D:\\F\\test\\t6");

    }

}
