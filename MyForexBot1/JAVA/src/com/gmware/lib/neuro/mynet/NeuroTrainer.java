package com.gmware.lib.neuro.mynet;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.games.holdem.common.rnd.Rnd517;

/**
 * Общий учитель.
 * Created by Gauss on 12.01.2016.
 */
abstract public class NeuroTrainer extends NeuroMapping{
    protected static final Rnd rnd = new Rnd517(0);

    static void selectTrainTestImages(
            final NetImage[] images,
            final NeuroNet net) {
        final NetImage[] trainImages = net.trainImages;
        final NetImage[] testImages = net.testImages;
        selectTrainTestImages(images, trainImages, testImages, rnd);
    }

    static void selectTrainTestImages(
            final NetImage[] images,
            final NetImage[] trainImages,
            final NetImage[] testImages) {
        selectTrainTestImages(images, trainImages, testImages, rnd);
    }

    protected static String t(final double x) {
        return String.format("%01.6f", x);
    }

    protected static boolean prepareGradient(final NeuroNet net) {
        if (net.stopTraining) return net.stopTraining;
        final NetImage[] trainImages = net.trainImages;
        net.clearGradient();
        for (final NetImage trainImage : trainImages) {
            net.addToGradient(trainImage);
        }
        return net.stopTraining;
    }

    static void prepareGaussianStep(final NeuroNet net) {
        final double[] g = net.dw;
        net.clearGradient();
//        g[rnd.rnd(g.length)] = 1;
        g[1] = 1;
    }

    static void prepareRandomStep(final NeuroNet net) {

    }

    /**
     * Длина шага.
     * Здесь минимизируем целевую функцию, считая ее линейной.
     *
     * @param net нейросеть
     * @return длина шага
     */
    static double getLinearStep(final NeuroNet net) {
        double up = 0;
        double down = 0;
        for (final NetImage trainImage : net.trainImages) {
            net.propagateForStep(trainImage);
            up += net.addStepUp(trainImage);//сумма произведений отклонения на градиент в точке входа
            down += net.addStepDown(trainImage);//сумма квадратов градиента в точке входа
        }
        net.up = up;
        net.down = down;
        return (down == 0) ? up : up / down;// "длина" шага
    }

}
