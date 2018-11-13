package com.gmware.lib.neuro.mynet;


import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.net2.ImmutableNNet3L;
import com.gmware.lib.neuro.mynet.Mappings.NeuroTrainerStandart;
import com.gmware.lib.games.holdem.common.rnd.Rnd517;

import java.io.*;

/**
 * Тест тренера.
 * Created by Gauss on 22.12.2015.
 */
public class NeuroTest {

    static double f(final double x) {
        return NeuroNet.f(x);
    }

    static void writeTestImagesSample(final DataOutputStream dos) { //TODO !neuro!test
        final int numIn = 2;
        final int numOut = 1;
        final double[] valueIn = new double[numIn];
        final double[] valueOut = new double[numOut];
        for (int i = 0; i < 10000; ++i) {
            for (int indIn = 0; indIn < numIn; ++indIn) {
                valueIn[indIn] = Math.random() * 2 - 1;
            }
            for (int indOut = 0; indOut < numOut; ++indOut) {
                valueOut[indOut] = f(f(f(f(valueIn[0])) - valueIn[1]) +
                                f(f(f(valueIn[0])) + valueIn[1]) +
                                f(valueIn[1])
                );
            }
            final NetImage im = new NetImage(numIn, numOut, valueIn, valueOut);
            try {
                im.writeImage(dos);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void writeTestImagesRandom(final DataOutputStream dos) { //TODO !neuro!test
        final int numIn = 130;
        final int numOut = 1;
        final double[] valueIn = new double[numIn];
        final double[] valueOut = new double[numOut];
        for (int i = 0; i < 10000; ++i) {
            for (int indIn = 0; indIn < numIn; ++indIn) {
                valueIn[indIn] = Math.random() * 4 - 2;
            }
            for (int indOut = 0; indOut < numOut; ++indOut) {
                valueOut[indOut] = f((1 / NeuroNet.A) * (
                        Math.random() * 2 - 1 + valueIn[0] * valueIn[1] + valueIn[2] * (valueIn[3] - 1)));
            }
            final NetImage im = new NetImage(numIn, numOut, valueIn, valueOut);
            try {
                im.writeImage(dos);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void writeTestImagesRandomNormalizer(final DataOutputStream dos) { //TODO !neuro!test
        final int numIn = 1;
        final int numOut = 1;
        final double[] valueIn = new double[numIn];
        final double[] valueOut = new double[numOut];
        for (int i = 0; i < 10000; ++i) {
            for (int indIn = 0; indIn < numIn; ++indIn) {
                final double x = Math.random() * Math.PI * 3;
                valueIn[indIn] = Math.sin(x) + x;
            }
            for (int indOut = 0; indOut < numOut; ++indOut) {
                valueOut[indOut] = 0;
            }
            final NetImage im = new NetImage(numIn, numOut, valueIn, valueOut);
            try {
                im.writeImage(dos);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void writeTestImagesLinearNormalizer(final DataOutputStream dos) { //TODO !neuro!test
        final int numImages = 10000;
        final int numIn = 1;
        final int numOut = 1;
        final double[] valueIn = new double[numIn];
        final double[] valueOut = new double[numOut];
        for (int i = 0; i < numImages; ++i) {
            for (int indIn = 0; indIn < numIn; ++indIn) {
                final double x = ((double) i / numImages) * Math.PI * 3;
                valueIn[indIn] = Math.sin(x) + x;
            }
            for (int indOut = 0; indOut < numOut; ++indOut) {
                valueOut[indOut] = 0;
            }
            final NetImage im = new NetImage(numIn, numOut, valueIn, valueOut);
            try {
                im.writeImage(dos);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void makeTestImages() {
        final String fileName = "z:\\DATA\\NeuroNet4NTest\\images\\1";
        try {
            final FileOutputStream fos = new FileOutputStream(fileName);
            final BufferedOutputStream bos = new BufferedOutputStream(fos);
//            writeTestImagesSample(new DataOutputStream(bos));
            writeTestImagesRandom(new DataOutputStream(bos));
            bos.flush();
            fos.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    static void makeTestImagesNormalizer() {
        final String fileName = "z:\\DATA\\NeuroNet4NTest\\images\\NormalizerTest";
        try {
            final FileOutputStream fos = new FileOutputStream(fileName);
            final BufferedOutputStream bos = new BufferedOutputStream(fos);
            writeTestImagesLinearNormalizer(new DataOutputStream(bos));
            bos.flush();
            fos.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    static void testMakeNetPartitionByCorrelation() {
        final NeuroNetSolver neuroNetSolver = new NeuroNetSolver(
                "NeuroTest2",
                1,
                new File("C:\\DATA"),
                new File("z:\\DATA\\PartitionByCorrelation\\nets"),
                100000,
                NeuroMapType.PartitionByCorrelation,
                true);
        try {
            neuroNetSolver.start();
        } catch (InterruptedException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static void testMakeNetStandartOpponent4N() {
        final NeuroNetSolver neuroNetSolver = new NeuroNetSolver(
                "StandartOpponentNeuroTest",
                1,
                new File("z:\\DATA\\images"),
                new File("z:\\DATA\\NeuroNet4NTest\\nets"),
                10000,
                NeuroMapType.FourNodeLayers,
                true);
        try {
            neuroNetSolver.start();
        } catch (InterruptedException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static void testMakeNetStandartOpponent2N() {
        final NeuroNetSolver neuroNetSolver = new NeuroNetSolver(
                "StandartOpponentNeuroTest",
                1,
                new File("z:\\DATA\\NeuroNet2NTest\\images"),
                new File("z:\\DATA\\NeuroNet2NTest\\nets"),
                10000,
                NeuroMapType.TwoNodeLayers,
                NeuroTrainerType.Standart,
                true);
        try {
            neuroNetSolver.start();
        } catch (InterruptedException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static void testMakeNetFractalFounder() {
        final NeuroNetSolver neuroNetSolver = new NeuroNetSolver(
                "FractalFounderNeuroTest2",
                1,
                new File("z:\\DATA\\NeuroNet4NTest\\images"),
                new File("z:\\DATA\\NeuroNet4NTest\\nets"),
                1000,
                NeuroMapType.FourNodeLayers,
                NeuroTrainerType.Developing,
                true);
        try {
            neuroNetSolver.start();
        } catch (InterruptedException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static void testStepByGradient() {
        final NetImage[] images = new NetImage[1000];
        final int numIn = 1;
        final int numOut = 1;
        final double[] valueIn = new double[numIn];
        final double[] valueOut = new double[numOut];
        for (int i = 0; i < images.length; ++i) {
            final double x = ((3.0 * i) / images.length) - 1;
            valueIn[0] = x;
            valueOut[0] = f(f(20 * x) + 0.5 * f(20 * x - 20));
            images[i] = new NetImage(numIn, numOut, valueIn, valueOut);
        }
        final NeuroNetStructure structure = new NeuroNetStructure(new int[][]{{1}, {2}, {1}});
        final NeuroNet net = NeuroMapType.TwoNodeLayers.createNewRandomNet(
                structure, images.length, 0, images, new Rnd517(5));
        net.setRelativeErrors();
        net.tag = 1;
        net.strength = 1;
        net.step = -0.001;
        final File file = new File("z:\\DATA\\NeuroNet2NTest\\nets\\StandartOpponentNeuroTest\\testStepByGradient.log");
        PrintStream log = null;
        try {
            log = new PrintStream(file);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
        NeuroTrainerStandart.finishTrain(net, log);
        for (final NetImage image : images) {
            final double a1 = image.in[0];
            final double a2 = image.out[0];
            final double a3 = net.propagate(image.in)[0];
            log.println(t(a1) + "\t" + t(a2) + "\t" + t(a3));
        }
        log.flush();
        log.close();
    }

    static String t(final double x) {
        return String.format("%01.5f", x);
    }

    static void testSigmoid() {
        double maxError1 = 0;
        double maxErrorX1 = 0;
        double maxError2 = 0;
        double maxErrorX2 = 0;
        int numErrors = 0;
        for (int i = -300000; i < 300000; ++i) {
            final double x = i / 10000.0;
            final double e = Math.abs(NeuroNet.f(x) - NeuroNet.strictF(x));
            final double e2 = Math.abs(ImmutableNNet3L.f(x) - ImmutableNNet3L.fMath(x));
            if (maxError1 < e) {
                maxError1 = e;
                maxErrorX1 = x;
            }
            if (maxError2 < e2) {
                maxError2 = e2;
                maxErrorX2 = x;
            }
//            if (e2 < e) {
//                System.out.println((++numErrors) + ": e(" + x + ")= " + e + "  \tf(x)= " + NeuroNet.f(x) + "  \tstrictF(x)= " + NeuroNet.strictF(x) +
//                        "              \te2= " + e2 + "  \tf2(x)= " + ImmutableNNet3L.f(x) + "  \tstrictf2(x)= " + ImmutableNNet3L.fMath(x));
//            }
        }
        for (int j = 0; j < 10; ++j) {
            final long t1 = System.nanoTime();
            for (int i = -600000; i < 600000; ++i) {
                final double x = i / 20000.0;
                final double e = NeuroNet.f(x);
            }
            final long t2 = System.nanoTime();
            System.out.print("Time f1 = " + (t2 - t1) / 1000000.0);

            final long t3 = System.nanoTime();
            for (int i = -600000; i < 600000; ++i) {
                final double x = i / 20000.0;
                final double e = ImmutableNNet3L.f(x);
            }
            final long t4 = System.nanoTime();
            System.out.println("  Time f2 = " + (t4 - t3) / 1000000.0);

            final long t5 = System.nanoTime();
            for (int i = -600000; i < 600000; ++i) {
                final double x = i / 20000.0;
                final double e = NeuroNet.strictF(x);
            }
            final long t6 = System.nanoTime();
            System.out.print("Time strict f1 = " + (t6 - t5) / 1000000.0);

            final long t7 = System.nanoTime();
            for (int i = -600000; i < 600000; ++i) {
                final double x = i / 20000.0;
                final double e = ImmutableNNet3L.fMath(x);
            }
            final long t8 = System.nanoTime();
            System.out.println("  Time strict f2 = " + (t8 - t7) / 1000000.0);
        }
        System.out.println("max e1(" + maxErrorX1 + ")= " + maxError1);
        System.out.println("max e2(" + maxErrorX2 + ")= " + maxError2);

    }

    static void testDiffSigmoid() {
        double maxError1 = 0;
        double maxErrorX1 = 0;
        double maxError2 = 0;
        double maxErrorX2 = 0;
        for (int i = -600000; i < 600000; ++i) {
            final double x = i / 20000.0;
            final double e = Math.abs(NeuroNet.df(x) - NeuroNet.strictDF(x));
            final double e2 = Math.abs(ImmutableNNet3L.f(x) - ImmutableNNet3L.fMath(x));
            if (maxError1 < e) {
                maxError1 = e;
                maxErrorX1 = x;
            }
            if (maxError2 < e2) {
                maxError2 = e2;
                maxErrorX2 = x;
            }
        }
        for (int j = 0; j < 10; ++j) {
            final long t1 = System.nanoTime();
            for (int i = -600000; i < 600000; ++i) {
                final double x = i / 20000.0;
                final double e = NeuroNet.df(x);
            }
            final long t2 = System.nanoTime();
            System.out.print("Time df1 = " + (t2 - t1) / 1000000.0);

            final long t3 = System.nanoTime();
            for (int i = -600000; i < 600000; ++i) {
                final double x = i / 20000.0;
                final double e = ImmutableNNet3L.f(x);
            }
            final long t4 = System.nanoTime();
            System.out.println("  Time f2 = " + (t4 - t3) / 1000000.0);

            final long t5 = System.nanoTime();
            for (int i = -600000; i < 600000; ++i) {
                final double x = i / 20000.0;
                final double e = NeuroNet.strictDF(x);
            }
            final long t6 = System.nanoTime();
            System.out.print("Time strict df1 = " + (t6 - t5) / 1000000.0);

            final long t7 = System.nanoTime();
            for (int i = -600000; i < 600000; ++i) {
                final double x = i / 20000.0;
                final double e = ImmutableNNet3L.fMath(x);
            }
            final long t8 = System.nanoTime();
            System.out.println("  Time strict f2 = " + (t8 - t7) / 1000000.0);
        }
        System.out.println("max d e1(" + maxErrorX1 + ")= " + maxError1);
        System.out.println("max e2(" + maxErrorX2 + ")= " + maxError2);

    }

    static void loadImages() {
        final String fileName = "x:\\RESULT\\PLOHL\\images\\actions-0025";
        DataInputStream dis = null;
        try {
            final File file = new File(fileName);
            if (file.exists()) {
                dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            }
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
        final NetImage[] images = assignImages(dis, 10000);
        System.out.println(images[0].getImageInfo());
    }

    static void loadMaps() {
        final String fileName = "Z:\\JAVA\\properties\\PLOHL\\p\\PLOHL-PBC2-actions.nets";
        DataInputStream dis = null;
        try {
            final File file = new File(fileName);
            if (file.exists()) {
                dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            }
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
        /**
         * Число ситуаций на префлопе.
         */
        final int NUM_PREFLOP_SITUATIONS = 224;
        /**
         * Число ситуаций на флопе.
         */
        final int NUM_FLOP_SITUATIONS = 1008;
        /**
         * Число ситуаций на торне.
         */
        final int NUM_TURN_SITUATIONS = 1008;
        /**
         * Число ситуаций на ривере.
         */
        final int NUM_RIVER_SITUATIONS = 1008;
        /**
         * Общее число ситуаций.
         */
        final int NUM_SITUATIONS = NUM_PREFLOP_SITUATIONS + NUM_FLOP_SITUATIONS + NUM_TURN_SITUATIONS + NUM_RIVER_SITUATIONS;

        final NeuroMap[] nets = new NeuroMap[NUM_SITUATIONS];
        for (int k = 0; k < nets.length; k++) {
            NeuroMap map = null;
            try {
                map = NeuroMap.load(dis);
            } catch (final IOException e) {
                e.printStackTrace();
            }
            nets[k] = map;
        }
        System.out.println(nets[0].rrError);

    }

    static NetImage[] assignImages(final DataInputStream dis, final int l) {
        final NetImage[] imagesBuffer = new NetImage[l];
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

    static public void main(final String[] args) {
//        testStepByGradient();

//        makeTestImagesNormalizer();
//        testMakeNetStandartOpponent4N();

//        testMakeNetPartitionByCorrelation();

//        testSigmoid();
//        testDiffSigmoid();

//        loadImages();
        loadMaps();
    }

}
