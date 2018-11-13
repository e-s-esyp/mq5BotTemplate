package com.gmware.lib.neuro.mynet.Mappings;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.Maps.NeuroNet1p5N;
import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.mynet.NeuroNet;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.neuro.mynet.NeuroTrainer;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Тренер.
 * см. https://ru.wikipedia.org/wiki/Градиентный_спуск
 * Находим первоначольные сети не случайным выбором, как
 * в NeuroTrainerStandart, а при помощи ситемы уравнений.
 * <p/>
 * Created by Gauss on 14.01.2016.
 */

public class NeuroTrainerNormalRegressionFirst extends NeuroTrainer {

    @Override
    protected NeuroMap trainMap(final NetImage[] images,
                                final NeuroMapType mapType,
                                final Rnd rnd,
                                final PrintStream log,
                                final String netOutPath) {
        return train(images, mapType, rnd, log, netOutPath);
    }

    public static NeuroNet train(
            final NetImage[] images,
            final NeuroMapType mapType,
            final Rnd rnd,
            final PrintStream log,
            final String netOutPath) {
        if (images == null) return null;
        if (images.length == 0) return null;
        if (images[0] == null) return null;
        final int trainSize = (images.length * 3) / 4;
        final int testSize = images.length - trainSize;
        log.println("trainSize=" + trainSize);
        log.println("testSize=" + testSize);
        NeuroMap[] normalizationNets = null;
        if (mapType.isNormalised) {
            if (netOutPath != null)
                normalizationNets = normalize(images, NeuroMapType.Polinom, netOutPath);
        }
        final NeuroNet[] pretenders = generateNets(images, trainSize, testSize, mapType, log);
        log.println(pretenders[0].toString());
        final NeuroNet net = trainNets(pretenders, log);
        if (!net.stopTraining) {
            log.println(" Finish training. ");
            finishTrain(net, log);
        }
        log.println(" RESULT NET:");
        net.printNetWeights(log);
        net.normalization = normalizationNets;
        printImages(net, log);
        return net;
    }

    private static NeuroNet[] generateNets(final NetImage[] images,
                                           final int trainSize,
                                           final int testSize,
                                           final NeuroMapType netType,
                                           final PrintStream log) {
        final int numImages = images.length;
        final int num = netType.getNumPretenders(numImages);
        final NeuroNet[] nets = new NeuroNet[num];
        for (int i = 0; i < num; ++i) {
            final double strength = (double) i / num;
            nets[i] = netType.createNewRandomNet(netType.makeStructure(numImages, images[0], strength), trainSize, testSize, images, rnd);
            nets[i].setRelativeErrors();
            nets[i].tag = i;
            nets[i].strength = strength;
            if (i == 0) {
                nets[i].makeAverageNet();
                nets[i].setStaticParameters();
                nets[i].setRelativeErrors();
                log.println(" AVERAGE NET:");
                nets[i].printNetWeights(log);
            }
            nets[i].step = -0.3 / trainSize;
        }
        return nets;
    }

    private static NeuroNet trainNets(
            final NeuroNet[] pretenders,
            final PrintStream log) {
        int numActiveNets = pretenders.length;
        Arrays.sort(pretenders, new Comparator<NeuroNet>() {
            @Override
            public int compare(final NeuroNet n1, final NeuroNet n2) {
                return (n1.rrError < n2.rrError) ? -1 : 1;
            }
        });
        final int[] indNeuron = new int[pretenders.length];
        final int[] numNeurons = new int[pretenders.length];
        for (int i = 0; i < numActiveNets; ++i) {
            indNeuron[i] = 0;
            numNeurons[i] = pretenders[i].structure.getNumNeurons();
        }
        print(pretenders, indNeuron, numActiveNets, log);
        int iter = 0;
        while (numActiveNets > 1) {
            for (int i = 0; i < numActiveNets; ++i) {
                stepByGradient(pretenders[i], indNeuron[i]);
                if (pretenders[i].numErrors > 3) {
                    indNeuron[i] = (indNeuron[i] + 1) % numNeurons[i];
                    pretenders[i].step = -0.3 / pretenders[i].trainImages.length;
                }
            }
            Arrays.sort(pretenders, new Comparator<NeuroNet>() {
                @Override
                public int compare(final NeuroNet n1, final NeuroNet n2) {
                    return (n1.rrError < n2.rrError) ? -1 : 1;
                }
            });
            if (iter == 200) {
                for (int i = 0; i < numActiveNets; ++i) {
                    indNeuron[i] = (indNeuron[i] + 1) % numNeurons[i];
                }
                numActiveNets--;
                iter = 0;
            } else {
                iter++;
            }
            if (numActiveNets < 1) numActiveNets = 1;
            print(pretenders, indNeuron, numActiveNets, log);
        }
        return pretenders[0];
    }

    private static void print(
            final NeuroNet[] pretenders,
            final int[] indNeuron,
            final int numActiveNets,
            final PrintStream log) {
        String s = "numActiveNets=" + numActiveNets +
                "  best error = " +
                t(pretenders[0].rrError) +
                "  worst error = " +
                t(pretenders[numActiveNets - 1].rrError) + ";" +
                "  | ";
        for (int i = 0; i <= numActiveNets && i < pretenders.length; ++i) {
            if (i == numActiveNets) s += " --- ";
            s += pretenders[i].tag + "," + indNeuron[i] + "," + t(pretenders[i].step) + " ";
//                    + "( /=" +
//                    t(pretenders[i].rrError) +
//                    (pretenders[i].stopTraining ? "*" : "") +
//                    " s=" + t(pretenders[i].step) +
//                    " strength=" + t(pretenders[i].strength) +
//                    " w.length=" + pretenders[i].w.length +
//                    " ) ";
        }
        System.out.println(s);
        log.println(s);
    }

    private static void printImages(final NeuroNet net, final PrintStream log) {
        NetImage[] images = net.trainImages;
        log.println("                               Train-образы, " + images.length + "шт.");
        for (int i = 0; i < images.length && i < 10; ++i) {
            log.print(images[i].toString1());
            log.println(" net: " + Arrays.toString(net.propagate(images[i].in)));
        }
        images = net.testImages;
        log.println("                               Test-образы, " + images.length + "шт.");
        for (int i = 0; i < images.length && i < 10; ++i) {
            log.print(images[i].toString1());
            log.println(" net: " + Arrays.toString(net.propagate(images[i].in)));
        }
        log.println("Average: " + Arrays.toString(net.averageOut));
        log.println("");
    }

    public static void finishTrain(
            final NeuroNet net,
            final PrintStream log) {
        System.out.println("Finish training.");
        for (int i = 0; i < 10000 && net.rrError > 0.001; ++i) {
            stepByGradient(net);
            System.out.println(i + ": " +
                    "[" + net.tag +
                    "] er=" + t(net.rrError) +
                    "  s=" + t(net.step) +
                    " strength=" + t(net.strength) +
                    " structure=" + net.structure +
                    ((net.type == NeuroMapType.SumOfSigmoids) ? ((NeuroNet1p5N) net).getSigmoidParametersAsSting() : ""));
        }
        net.setRelativeErrors();
    }

    private static void stepByGradient(final NeuroNet net) {
        if (prepareGradient(net)) return;
//        final double step = getLinearStep(net);
//        final double step = -7.4/net.trainImages.length;//1000
//        final double step = -7.5 / net.trainImages.length;//10000
        if (Double.isNaN(net.step)) {
            System.out.println("--- step is NaN, generating random net ---");
            NeuroNet.randomize(net, rnd);
            net.setRelativeErrors();
        } else {
//            net.step = step;
            net.addGradientToWeights();
            net.setRelativeErrorsAndBack();
        }
        if (Double.isNaN(net.rrError)) {
            System.out.println("--- rrError is NaN, generating random net ---");
            NeuroNet.randomize(net, rnd);
            net.setRelativeErrors();
        }
        net.step *= 1.2;
    }

    private static void makeSomeSteps(final NeuroNet net, final int indNeuron, final int itterations) {
        for (int i = 0; i < itterations; ++i) {
            stepByGradient(net, indNeuron);
        }
    }

    private static void stepByGradient(final NeuroNet net, final int indNeuron) {
        if (prepareGradient(net)) return;
        if (Double.isNaN(net.step)) {
            System.out.println("--- step is NaN, generating random net ---");
            NeuroNet.randomize(net, rnd);
            net.setRelativeErrors();
        } else {
            net.addGradientToWeights(indNeuron);
//            net.addGradientToWeights();
            net.setRelativeErrorsAndBack(indNeuron);
        }
        if (Double.isNaN(net.rrError)) {
            System.out.println("--- rrError is NaN, generating random net ---");
            NeuroNet.randomize(net, rnd);
            net.setRelativeErrors();
        }
        net.step *= 1.2;
    }

}
