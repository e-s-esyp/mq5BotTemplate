package com.gmware.lib.neuro.mynet.F;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.NeuroComputationType;
import com.gmware.lib.neuro.mynet.Mappings.NeuroMappingCellsDefinition;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapCells;
import com.gmware.lib.neuro.mynet.NeuroImage;
import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.games.holdem.common.rnd.Rnd517;

import java.io.*;
import java.util.Properties;

/**
 * ----
 * Created by Gauss on 31.05.2016.
 * ---
 * Created by Gauss on 13.05.2016.
 * -
 * Created by Gauss on 15.04.2016.
 */
public class NeuroFPredictorCells extends NeuroFPredictor {
    static final String[] NAMES = {
            "\\0-close-up.map",
            "\\1-open.map",
            "\\2-close-down.map"};
    static final int[] NUM_KLASSES = {2, 3, 2};
    String path = null;
    TimedDoubles[] knownData = null;
    PrintStream log = null;
    int[] mult = new int[]{2, 1, 2};
    NeuroMapCells[][] maps2 = new NeuroMapCells[1][];

    NeuroFPredictorCells() {
    }

    NeuroFPredictorCells(final String path) throws IOException {
//        initParameters(data, startInd);
        final File properties = new File(path + "\\predictor.properties");
        if (!properties.exists()) {
            for (int i = 0; i < maps2.length; i++) {
                final String pathi = "." + String.format("%02d", i);
                maps2[i] = new NeuroMapCells[]{
                        (NeuroMapCells) NeuroMap.load(path + NAMES[0] + pathi),
                        (NeuroMapCells) NeuroMap.load(path + NAMES[1] + pathi),
                        (NeuroMapCells) NeuroMap.load(path + NAMES[2] + pathi)};
                System.out.print("*");
            }
        } else {
            final Properties p = new Properties();
            try (InputStream is = new FileInputStream(properties)) {
                p.load(is);
            }
            final int num = Integer.parseInt(p.getProperty("NUM_MAPS"));
            maps2 = new NeuroMapCells[num][];
            for (int i = 0; i < maps2.length; i++) {
                final String pathi1 = p.getProperty("MAP" + i);
                final String pathi = "." + "00";
                maps2[i] = new NeuroMapCells[]{
                        (NeuroMapCells) NeuroMap.load(path + "\\" + pathi1 + NAMES[0] + pathi),
                        (NeuroMapCells) NeuroMap.load(path + "\\" + pathi1 + NAMES[1] + pathi),
                        (NeuroMapCells) NeuroMap.load(path + "\\" + pathi1 + NAMES[2] + pathi)};
                System.out.print("*");
            }

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
        maps2 = new NeuroMapCells[par.length][];
        for (int i = 0; i < maps2.length; i++) {
            final String pathi = "." + String.format("%02d", i);
            final NeuroMapCells closeUpMap = trainMap(par[i], images0, rnd, path + NAMES[0] + ".log" + pathi);
            final NeuroMapCells openMap = trainMap(par[i], images1, rnd, path + NAMES[1] + ".log" + pathi);
            final NeuroMapCells closeDownMap = trainMap(par[i], images2, rnd, path + NAMES[2] + ".log" + pathi);
            closeUpMap.save(path + NAMES[0] + pathi);
            openMap.save(path + NAMES[1] + pathi);
            closeDownMap.save(path + NAMES[2] + pathi);
            maps2[i] = new NeuroMapCells[]{
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

    private NeuroMapCells trainMap(
            final int[] par,
            final NeuroImage[] images,
            final Rnd rnd,
            final String name) throws FileNotFoundException {
        final NeuroComputationType computationType = new NeuroComputationType();
        computationType.setF();
        final int[] par1 = new int[par.length - 1];
        System.arraycopy(par, 1, par1, 0, par1.length);
        return NeuroMappingCellsDefinition.train(
                images,
                par1,
                par[0],
                rnd,
                new PrintStream(new File(name)),
                "PATH");
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
     * @param order открытый ордер   0-buy 1-nothing 2-sell
     * @param data  данные
     * @param ind   индекс первой неизвестной data
     * @return действие
     */
    @Override
    public int getDecision(final int order, final int value, final TimedDoubles[] data, final int ind) {
        final double[] in = new double[NeuroF.NUM_IMAGES_IN];
        final TimedDoubles newData = data[ind];
        data[ind] = null;
        NeuroF.fillIn(ind, data, in);
        data[ind] = newData;
        if (!NeuroF.simulateMode) NeuroF.printOut("order=" + order);
        if (!NeuroF.simulateMode) NeuroF.printOut("value=" + value);
        final int numOuts = maps2[0][order].numOuts;
        final double[] ps = new double[numOuts];
        int numUsedMaps = 0;
        final int[] r = new int[maps2.length];
        for (int j = 0; j < maps2.length; j++) {
            final NeuroMapCells[] ms = maps2[j];
            final NeuroMapCells.Out out = ms[order].getOut(in);
            if (out.numImages > 0) {
                ++numUsedMaps;
                final double[] p = out.profits;
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
            if (order == 1) {
                r[j] = action;
            } else {
                r[j] = action * 2;
            }
        }
        if (numUsedMaps == 0) {
            return 1;
        }
        if (order == 1) {
            int sum = -r.length;
            for (final int a : r) {
                sum += a;
            }
            if (sum < 0) {
                return 0;
            }
            if (sum > 0) {
                return 2;
            }
            return 1;
        } else {
            for (final int a : r) {
                if (order != a) {
                    return a;
                }
            }
            return order;
        }
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
        final NeuroFPredictorCells p = new NeuroFPredictorCells();
//        p.train3(images, "D:\\F\\test\\t6");

    }

    public NeuroImage[] simulate(final int order, final NeuroImage[] images0) {
        final NeuroImage[] images = new NeuroImage[images0.length];
        final int numOuts = maps2[0][order].numOuts;
        for (int j = 0; j < images0.length; ++j) {
            final NeuroImage image0 = images0[j];
            final double[] ps = new double[numOuts];
            for (final NeuroMapCells[] ms : maps2) {
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
        return (new NeuroMapCells()).type.getShortDescription();
    }
}
