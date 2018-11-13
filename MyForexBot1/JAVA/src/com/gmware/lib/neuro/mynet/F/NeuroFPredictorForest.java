package com.gmware.lib.neuro.mynet.F;

/**
 * ---
 * Created by Gauss on 13.05.2016.
 */

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.Mappings.NeuroMappingPartitionByDeterminationNoTest;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapPartition;
import com.gmware.lib.neuro.mynet.NeuroComputationType;
import com.gmware.lib.neuro.mynet.NeuroImage;
import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.neuro.mynet.NeuroOut;
import com.gmware.lib.games.holdem.common.rnd.Rnd517;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

/**
 * -
 * Created by Gauss on 15.04.2016.
 */
public class NeuroFPredictorForest extends NeuroFPredictor{
    static final String[] NAMES = {
            "\\0-close-up.map",
            "\\1-open.map",
            "\\2-close-down.map"};
    static final int[] NUM_KLASSES = {2, 3, 2};
    String path = null;
    TimedDoubles[] knownData = null;
    int[] mult = new int[]{2, 1, 2};
    NeuroMapPartition[][] maps2 = new NeuroMapPartition[1][];

    NeuroFPredictorForest() {
    }

    NeuroFPredictorForest(final String path) throws IOException {
//        initParameters(data, startInd);
        for (int i = 0; i < maps2.length; i++) {
            final String pathi = "." + String.format("%02d", i);
            maps2[i] = new NeuroMapPartition[]{
                    (NeuroMapPartition) NeuroMap.load(path + NAMES[0] + pathi),
                    (NeuroMapPartition) NeuroMap.load(path + NAMES[1] + pathi),
                    (NeuroMapPartition) NeuroMap.load(path + NAMES[2] + pathi)};
            System.out.print("*");
        }
        System.out.println();
    }

    @Override
    public void train2(
            final int[][] par,
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
        maps2 = new NeuroMapPartition[par.length][];
        for (int i = 0; i < maps2.length; i++) {
            final String pathi = "." + String.format("%02d", i);
            final NeuroMapPartition closeUpMap = trainMap(par[i], images0, rnd, path + NAMES[0] + ".log" + pathi);
            final NeuroMapPartition openMap = trainMap(par[i], images1, rnd, path + NAMES[1] + ".log" + pathi);
            final NeuroMapPartition closeDownMap = trainMap(par[i], images2, rnd, path + NAMES[2] + ".log" + pathi);
            closeUpMap.save(path + NAMES[0] + pathi);
            openMap.save(path + NAMES[1] + pathi);
            closeDownMap.save(path + NAMES[2] + pathi);
            maps2[i] = new NeuroMapPartition[]{
                    closeUpMap,
                    openMap,
                    closeDownMap};
        }
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
            final int[] par,
            final NeuroImage[] images,
            final Rnd rnd,
            final String name) throws FileNotFoundException {
        final NeuroComputationType computationType = new NeuroComputationType();
        computationType.setF();
        return NeuroMappingPartitionByDeterminationNoTest.train(
                images,
                par,
                rnd,
                new PrintStream(new File(name)),
                computationType);
    }

    @Override
    public final void initParameters(final TimedDoubles[] data, final int startInd) {
        NeuroF.setMAs(data);
    }

    public int getDecisionRandom(final int order, final TimedDoubles[] data, final int ind) {
        if (Math.random() < 0.0002) {
            return 2 * (int) (Math.random() * 2);
        } else {
            return order;
        }
    }

    /**
     * @param order -
     * @param value -
     * @param data  -
     * @param ind   - первый неизвестный индекс
     * @return      -
     */
    public int getDecision(final int order, final int value, final TimedDoubles[] data, final int ind) {
        final double[] in = new double[NeuroF.NUM_IMAGES_IN];
        final TimedDoubles newData = data[ind];
        data[ind] = null;
        NeuroF.fillIn(ind, data, in);
        data[ind] = newData;
        NeuroF.printOut("order=" + order);
        NeuroF.printOut("value=" + value);
        final int numOuts = maps2[0][order].numOuts;
        final double[] ps = new double[numOuts];
        int numUsedMaps = 0;
        for (final NeuroMapPartition[] ms : maps2) {
            final NeuroOut out = ms[order].getOut(in);
            if (out.numImages > 0) {
                ++numUsedMaps;
                final double[] p = out.profits;
                for (int i = 0; i < numOuts; i++) {
                    ps[i] += p[i];
                }
            }
        }
        if (numUsedMaps == 0) {
            return 1;
        }
        int action = -1;
        double best = -1E300;
        for (int i = 0; i < numOuts; i++) {
            if (best < ps[i]) {
                best = ps[i];
                action = i;
            }
        }
        if (order == 0) {
            if (action == 0) return 1;
            if (action == 1) return 2;
        }
        if (order == 1) {
            return action;
        }
        if (order == 2) {
            if (action == 0) return 0;
            if (action == 1) return 1;
        }
        return 0;
    }

//----------------------------------------------------------------------------------------------------------------------

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
        final NeuroFPredictorForest p = new NeuroFPredictorForest();
//        p.train3(images, "D:\\F\\test\\t6");

    }

    public NeuroImage[] simulate(final int order, final NeuroImage[] images0) {
        final NeuroImage[] images = new NeuroImage[images0.length];
        final int numOuts = maps2[0][order].numOuts;
        for (int j = 0; j < images0.length; ++j) {
            final NeuroImage image0 = images0[j];
            final double[] ps = new double[numOuts];
            for (final NeuroMapPartition[] ms : maps2) {
                final double[] p = ms[order].getOut(image0.in).profits;
                for (int i = 0; i < numOuts; i++) {
                    ps[i] += p[i];
                }
            }
            int action = -1;
            double best = -1E300;
            for (int i = 0; i < numOuts; i++) {
                if (best < ps[i]) {
                    best = ps[i];
                    action = i;
                }
            }
            final NeuroImage image = new NeuroImage();
            image.numIn = image0.numIn;
            image.numOut = image0.numOut;
            image.in = image0.in;
            image.klass = action;
            image.out = ps;
            images[j] = image;
        }
        return images;
    }

    @Override
    public String getShortDescription() {
        return (new NeuroMapPartition()).type.getShortDescription();
    }
}
