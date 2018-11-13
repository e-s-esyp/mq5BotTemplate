package com.gmware.lib.neuro.mynet.Mappings;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.*;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapPartitionByCorrelation3;
import com.gmware.lib.neuro.NetImage;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Получение NeuroMapPartitionByCorrelation по статистическим данным.
 * Отличие от NeuroMappingPartitionByCorrelation в том, что точка разделения
 * в NeuroMappingPartitionByCorrelation выбирается просто делением пополам,
 * а в NeuroMappingPartitionByCorrelation3 как среднее по выбранному входу.
 * Добавлено обучение без тестовых образов для малого числа образов.
 * <p/>
 * <p/>
 * Created by Gauss on 25.02.2016.
 */
//TODO: классификация k-среднего, параметр - близость к собирательному образу
public class NeuroMappingPartitionByCorrelation3 extends NeuroMapping {

    public static int maxLevel = 20;
    public static double proportion = 0.50;
    public static int errorType = 3;
    public static boolean hasTesting = true;
    public static boolean restrictByLevel = true;
    public static boolean restrictByImages = false;
    public static boolean balanceImages = false;
    public static boolean randomized = false;

    //----------------------------------------------------------
    @Override
    protected NeuroMap trainMap(final NetImage[] images,
                                final NeuroMapType mapType,
                                final Rnd rnd,
                                final PrintStream log,
                                final String netOutPath) {
        return train(images, mapType, rnd, log, netOutPath);
    }

    static class NeuroImagePBC {
        double[] in = null;
        double out = 0.0;

        public NeuroImagePBC(final double[] in, final double out) {
            this.in = in;
            this.out = out;
        }

        @Override
        public String toString() {
            String s = "";
            for (final double x : in) {
                s += t(x) + " ";
            }
            return "(" + s + "| " + t(out) + ")";
        }
    }

    private static class ImageDivision {
        NeuroImagePBC[] less = null;
        NeuroImagePBC[] notLess = null;

        ImageDivision(final NeuroImagePBC[] l, final NeuroImagePBC[] n) {
            less = l;
            notLess = n;
        }
    }

    public static NeuroMap train(final NetImage[] images,
                                 final NeuroMapType mapType,
                                 final Rnd rnd,
                                 final PrintStream log,
                                 final String netOutPath) {
        if (images == null) return null;
        if (images.length == 0) return null;
        if (balanceImages) {
            doBalanceImages(images, log);
        }
        final NeuroMapPartitionByCorrelation3 map =
                new NeuroMapPartitionByCorrelation3(images[0].numIn, images[0].numOut, images.length);
        map.errorType = errorType;
        if (map.numOuts == 3) map.setConvertFCR();
        if (hasTesting) {
            final int numTrainImages = (int) (images.length * proportion);
            final int numTestImages = images.length - numTrainImages;
            final NetImage[] trainImages = new NetImage[numTrainImages];
            final NetImage[] testImages = new NetImage[numTestImages];
            selectTrainTestImages(images, trainImages, testImages, rnd);
            train(map, trainImages, testImages, rnd);
            if (map.rrError >= 1) {
                map.noTest = true;
                train(map, images, images, rnd);
            }
        } else {
            map.noTest = true;
            train(map, images, images, rnd);
        }
        log.print(map);
        return map;
    }

    private static void doBalanceImages(final NetImage[] images,
                                        final PrintStream log) {
        final int numOut = images[0].numOut;
        for (int i = 0; i < numOut; ++i) {
            int numPos = 0;
            for (final NetImage image : images) {
                if (image.out[i] > 0) {
                    ++numPos;
                }
            }
            final double negWeight = (double) numPos / images.length;
            final double posWeight = negWeight - 1;
            log.println("negWeight = " + negWeight + "  posWeight = " + posWeight);
            for (final NetImage image : images) {
                if (image.out[i] > 0) {
                    image.out[i] = posWeight;
                } else {
                    image.out[i] = negWeight;
                }
            }
        }
    }

    private static void train(final NeuroMapPartitionByCorrelation3 map,
                              final NetImage[] trainImages,
                              final NetImage[] testImages,
                              final Rnd rnd) {
        final NeuroImagePBC[][] trainSelectedImages = convertImages(trainImages, map);
        final NeuroImagePBC[][] testSelectedImages = convertImages(testImages, map);
        for (int i = 0; i < map.numStructures; ++i) {
            map.structure[i] = (randomized) ?
                    trainStructureR(maxLevel, trainSelectedImages[i], testSelectedImages[i], rnd) :
                    trainStructure(maxLevel, trainSelectedImages[i], testSelectedImages[i]);
        }
        map.levelLimit = maxLevel;
        if (restrictByLevel) {
            final IndexedDoubles[] e = new IndexedDoubles[maxLevel];
            for (int i = 0; i < maxLevel; ++i) {
                map.levelLimit = i;
                map.setErrors(trainImages, testImages);
                e[i] = new IndexedDoubles(i, map.rrError);
            }
            map.errorByLevel = e;
            setMinMax(map, trainImages, testImages);
            Arrays.sort(e, new Comparator<IndexedDoubles>() {
                @Override
                public int compare(final IndexedDoubles o1, final IndexedDoubles o2) {
                    if (o1.value < o2.value) return -1;
                    if (o1.value > o2.value) return 1;
                    return 0;
                }
            });
            map.levelLimit = e[0].index;
            map.restrictByLevel();
            map.setErrors(trainImages, testImages);
        }
        if (restrictByImages) {
            final IndexedDoubles[] e = new IndexedDoubles[21];
            for (int i = 0; i < e.length; ++i) {
                map.imagesLimit = 1 << i;
                map.setErrors(trainImages, testImages);
                e[i] = new IndexedDoubles(i, map.rrError);
            }
            map.errorByImages = e;
            map.imagesLimit = 0;
            map.setErrors(trainImages, testImages);
            setMinMax(map, trainImages, testImages);
//            Arrays.sort(e, new Comparator<IndexedDoubles>() {
//                @Override
//                public int compare(final IndexedDoubles o1, final IndexedDoubles o2) {
//                    if (o1.value < o2.value) return -1;
//                    if (o1.value > o2.value) return 1;
//                    return 0;
//                }
//            });
        }
    }

    private static void setMinMax(final NeuroMapPartitionByCorrelation3 map,
                                  final NetImage[] trainImages,
                                  final NetImage[] testImages) {
        for (final NetImage image : trainImages) {
            map.updateMinMax(image.in, image.out);
        }
        for (final NetImage image : testImages) {
            map.updateMinMax(image.in, image.out);
        }
    }

    private static NeuroImagePBC[][] convertImages(final NetImage[] images,
                                                   final NeuroMapPartitionByCorrelation3 map) {
        if (map.convertFCR && map.numOuts == 3) {
            final NeuroImagePBC[] convertedImages0 = new NeuroImagePBC[images.length];
            int num = 0;
            int i = 0;
            for (final NetImage image : images) {
                final double[] out = NeuroMapPartitionByCorrelation3.convertFCR(image.out);
                convertedImages0[i++] = new NeuroImagePBC(image.in, out[0]);
                if (!Double.isNaN(out[1])) {
                    ++num;
                }
            }
            final NeuroImagePBC[] convertedImages1 = new NeuroImagePBC[num];
            i = 0;
            for (final NetImage image : images) {
                final double[] out = NeuroMapPartitionByCorrelation3.convertFCR(image.out);
                if (!Double.isNaN(out[1])) {
                    convertedImages1[i++] = new NeuroImagePBC(image.in, out[1]);
                }
            }
            return new NeuroImagePBC[][]{convertedImages0, convertedImages1};
        } else {
            final NeuroImagePBC[][] imagesPBC = new NeuroImagePBC[map.numOuts][];
            for (int i = 0; i < imagesPBC.length; ++i) {
                imagesPBC[i] = new NeuroImagePBC[images.length];
            }
            for (int i = 0; i < images.length; ++i) {
                for (int j = 0; j < imagesPBC.length; ++j) {
                    imagesPBC[j][i] = new NeuroImagePBC(images[i].in, images[i].out[j]);
                }
            }
            return imagesPBC;
        }
    }

    private static NeuroMapPartitionByCorrelation3.Structure trainStructure(
            final int recursionIndex,
            final NeuroImagePBC[] trainImages,
            final NeuroImagePBC[] testImages) {
        final NeuroMapPartitionByCorrelation3.Structure structure = new NeuroMapPartitionByCorrelation3.Structure();
        structure.numOfImages = trainImages.length;
        if (errorType == 1) {
            structure.average = getAverageOutContinuous(trainImages);
        }
        if (errorType == 2) {
            structure.average = getAverageOutDiscrete(trainImages);
        }
        if (errorType == 3) {
            structure.average = getAverageOutContinuous(trainImages);
        }
        if (recursionIndex > 0 && trainImages.length >= 2) {
            final IndexedDoubles[] correlations = getCorrelationPBC(null, trainImages);
            Arrays.sort(correlations, new Comparator<IndexedDoubles>() {
                @Override
                public int compare(final IndexedDoubles o1, final IndexedDoubles o2) {
                    if (o1.value < o2.value) return 1;
                    if (o1.value > o2.value) return -1;
                    return 0;
                }
            });
            ImageDivision trainImagesDivision = null;
            ImageDivision testImagesDivision = null;
            int correlationsIndex = 0;
            int bestInIndex = 0;
            double middleValue = 0;
            while (correlationsIndex < correlations.length) {
                //ищем минимальный индекс, по которому можно разделить train и test образы
                bestInIndex = correlations[correlationsIndex].index;
                final int bestInIndex1 = bestInIndex;
                Arrays.sort(trainImages, new Comparator<NeuroImagePBC>() {
                    @Override
                    public int compare(final NeuroImagePBC o1, final NeuroImagePBC o2) {
                        if (o1.in[bestInIndex1] < o2.in[bestInIndex1]) return 1;
                        if (o1.in[bestInIndex1] > o2.in[bestInIndex1]) return -1;
                        return 0;
                    }
                });
                middleValue = getAverageIn(trainImages, bestInIndex);
                trainImagesDivision = divideImages(trainImages, bestInIndex, middleValue);
                testImagesDivision = divideImages(testImages, bestInIndex, middleValue);
                if (trainImagesDivision == null) {
                    correlationsIndex++;
                } else {
                    break;
                }
            }
            if ((trainImagesDivision == null) || (testImagesDivision == null)) {
                return structure;
            }
            structure.divided = true;
            structure.correlation = correlations[correlationsIndex].value;
            structure.inInd = bestInIndex;
            structure.divisor = middleValue;
            structure.lessPart = trainStructure(
                    recursionIndex - 1,
                    trainImagesDivision.less,
                    testImagesDivision.less);
            structure.notLessPart = trainStructure(
                    recursionIndex - 1,
                    trainImagesDivision.notLess,
                    testImagesDivision.notLess);
            boolean isBetter = false;
            if (errorType == 1) {
                isBetter = isBetterThenAverageContinuous(structure, trainImages, testImages, recursionIndex);
            }
            if (errorType == 2) {
                isBetter = isBetterThenAverageDiscrete(structure, trainImages, testImages, recursionIndex);
            }
            if (errorType == 3) {
                isBetter = isBetterThenAverageAbsolute(structure, trainImages, testImages, recursionIndex);
            }
            if (isBetter) {
                return structure;
            } else {
                structure.divided = false;
                return structure;
            }
        } else {
            return structure;
        }
    }

    private static boolean isBetterThenAverageContinuous(
            final NeuroMapPartitionByCorrelation3.Structure structure,
            final NeuroImagePBC[] trainImages,
            final NeuroImagePBC[] testImages,
            final int level) {
        final double averageOut = getAverageOutContinuous(trainImages);
        final double trnVar = getVariance(trainImages);
        final double tstVar = getVariance(testImages);

        final double trnEA = getErrorOfAverageContinuous(trainImages, averageOut);
        final double tstEA = getErrorOfAverageContinuous(testImages, averageOut);
        final double trnREA = trnVar > 0.0 ? trnEA / trnVar : 1.0;
        final double tstREA = tstVar > 0.0 ? tstEA / tstVar : 1.0;
        final double relativeErrorOfAverage = Math.max(trnREA, tstREA);

        final double trnE = getErrorQuadraticContinuous(structure, trainImages, level);
        final double tstE = getErrorQuadraticContinuous(structure, testImages, level);
        final double trnRE = trnVar > 0.0 ? trnE / trnVar : 1.0;
        final double tstRE = tstVar > 0.0 ? tstE / tstVar : 1.0;
        final double relativeError = Math.max(trnRE, tstRE);

        return relativeError < relativeErrorOfAverage;
    }

    private static boolean isBetterThenAverageDiscrete(
            final NeuroMapPartitionByCorrelation3.Structure structure,
            final NeuroImagePBC[] trainImages,
            final NeuroImagePBC[] testImages,
            final int level) {
        final double averageOut = getAverageOutDiscrete(trainImages);
        final double trnVar = getVariance(trainImages);
        final double tstVar = getVariance(testImages);

        final double trnEA = getErrorOfAverageDiscrete(trainImages, averageOut);
        final double tstEA = getErrorOfAverageDiscrete(testImages, averageOut);
        final double trnREA = trnVar > 0.0 ? trnEA / trnVar : 1.0;
        final double tstREA = tstVar > 0.0 ? tstEA / tstVar : 1.0;
        final double relativeErrorOfAverage = Math.max(trnREA, tstREA);

        final double trnE = getErrorDiscrete(structure, trainImages, level);
        final double tstE = getErrorDiscrete(structure, testImages, level);
        final double trnRE = trnVar > 0.0 ? trnE / trnVar : 1.0;
        final double tstRE = tstVar > 0.0 ? tstE / tstVar : 1.0;
        final double relativeError = Math.max(trnRE, tstRE);

        return relativeError < relativeErrorOfAverage;
    }

    private static boolean isBetterThenAverageAbsolute(
            final NeuroMapPartitionByCorrelation3.Structure structure,
            final NeuroImagePBC[] trainImages,
            final NeuroImagePBC[] testImages,
            final int level) {
        final double averageOut = getAverageOutContinuous(trainImages);
        final double trnVar = getVariance(trainImages);
        final double tstVar = getVariance(testImages);

        final double trnEA = getErrorOfAverageAbsolute(trainImages, averageOut);
        final double tstEA = getErrorOfAverageAbsolute(testImages, averageOut);
        final double trnREA = trnVar > 0.0 ? trnEA / trnVar : 1.0;
        final double tstREA = tstVar > 0.0 ? tstEA / tstVar : 1.0;
        final double relativeErrorOfAverage = Math.max(trnREA, tstREA);

        final double trnE = getErrorAbsolute(structure, trainImages, level);
        final double tstE = getErrorAbsolute(structure, testImages, level);
        final double trnRE = trnVar > 0.0 ? trnE / trnVar : 1.0;
        final double tstRE = tstVar > 0.0 ? tstE / tstVar : 1.0;
        final double relativeError = Math.max(trnRE, tstRE);

        return relativeError < relativeErrorOfAverage;
    }

    private static NeuroMapPartitionByCorrelation3.Structure trainStructureR(
            final int recursionIndex,
            final NeuroImagePBC[] trainImages,
            final NeuroImagePBC[] testImages,
            final Rnd rnd) {
        final int numOfImages = trainImages.length;
        final NeuroMapPartitionByCorrelation3.Structure averageStructure = new NeuroMapPartitionByCorrelation3.Structure();
        averageStructure.numOfImages = numOfImages;
        if (errorType == 1) {
            averageStructure.average = getAverageOutContinuous(trainImages);
        }
        if (errorType == 2) {
            averageStructure.average = getAverageOutDiscrete(trainImages);
        }
        if (errorType == 3) {
            averageStructure.average = getAverageOutContinuous(trainImages);
        }
        if (recursionIndex > 0 && trainImages.length >= 2) {
            final IndexedDoubles[] correlations = getCorrelationPBC(null, trainImages);
            final IndexedDoubles[] randoms = getRandoms(null, trainImages, rnd);
            final NeuroMapPartitionByCorrelation3.Structure[] structures = new NeuroMapPartitionByCorrelation3.Structure[]{
                    averageStructure,
                    trainStructureByOrder(correlations, averageStructure, recursionIndex, trainImages, testImages, rnd),
                    trainStructureByOrder(randoms, averageStructure, recursionIndex, trainImages, testImages, rnd)};
            for (final NeuroMapPartitionByCorrelation3.Structure structure : structures) {
                double trainError = 0;
                double testError = 0;
                if (errorType == 1) {
                    trainError = getErrorQuadraticContinuous(structure, trainImages, recursionIndex);
                    testError = getErrorQuadraticContinuous(structure, testImages, recursionIndex);
                }
                if (errorType == 2) {
                    trainError = getErrorDiscrete(structure, trainImages, recursionIndex);
                    testError = getErrorDiscrete(structure, testImages, recursionIndex);
                }
                if (errorType == 3) {
                    trainError = getErrorAbsolute(structure, trainImages, recursionIndex);
                    testError = getErrorAbsolute(structure, testImages, recursionIndex);
                }
                structure.error = (trainError > testError) ? trainError : testError;
            }
            Arrays.sort(structures, new Comparator<NeuroMapPartitionByCorrelation3.Structure>() {
                @Override
                public int compare(final NeuroMapPartitionByCorrelation3.Structure o1, final NeuroMapPartitionByCorrelation3.Structure o2) {
                    if (o1.error < o2.error) return -1;
                    if (o1.error > o2.error) return 1;
                    return 0;
                }
            });
            return structures[0];
        } else {
            return averageStructure;
        }
    }

    private static NeuroMapPartitionByCorrelation3.Structure trainStructureByOrder(
            final IndexedDoubles[] order,
            final NeuroMapPartitionByCorrelation3.Structure averageStructure,
            final int recursionIndex,
            final NeuroImagePBC[] trainImages,
            final NeuroImagePBC[] testImages,
            final Rnd rnd) {
        Arrays.sort(order, new Comparator<IndexedDoubles>() {
            @Override
            public int compare(final IndexedDoubles o1, final IndexedDoubles o2) {
                if (o1.value < o2.value) return 1;
                if (o1.value > o2.value) return -1;
                return 0;
            }
        });
        ImageDivision trainImagesDivision = null;
        ImageDivision testImagesDivision = null;
        int orderIndex = 0;
        int bestInIndex = 0;
        double middleValue = 0;
        while (orderIndex < order.length) {
            //ищем минимальный индекс, по которому можно разделить train и test образы
            bestInIndex = order[orderIndex].index;
            final int bestInIndex1 = bestInIndex;
            Arrays.sort(trainImages, new Comparator<NeuroImagePBC>() {
                @Override
                public int compare(final NeuroImagePBC o1, final NeuroImagePBC o2) {
                    if (o1.in[bestInIndex1] < o2.in[bestInIndex1]) return 1;
                    if (o1.in[bestInIndex1] > o2.in[bestInIndex1]) return -1;
                    return 0;
                }
            });
            middleValue = getAverageIn(trainImages, bestInIndex);
            trainImagesDivision = divideImages(trainImages, bestInIndex, middleValue);
            testImagesDivision = divideImages(testImages, bestInIndex, middleValue);
            if (trainImagesDivision == null) {
                orderIndex++;
            } else {
                break;
            }
        }
        if ((trainImagesDivision == null) || (testImagesDivision == null)) {
            return averageStructure;
        }
        final NeuroMapPartitionByCorrelation3.Structure structure =
                new NeuroMapPartitionByCorrelation3.Structure();
        structure.numOfImages = averageStructure.numOfImages;
        structure.average = averageStructure.average;
        structure.divided = true;
        structure.correlation = order[orderIndex].value;
        structure.inInd = bestInIndex;
        structure.divisor = middleValue;
        structure.lessPart = trainStructureR(
                recursionIndex - 1,
                trainImagesDivision.less,
                testImagesDivision.less,
                rnd);
        structure.notLessPart = trainStructureR(
                recursionIndex - 1,
                trainImagesDivision.notLess,
                testImagesDivision.notLess,
                rnd);
        return structure;
    }


    private static double getAverageIn(final NeuroImagePBC[] images,
                                       final int index) {
        if (images == null) {
            return 0;
        }
        if (images.length == 0) {
            return 0;
        }
        double m = 0;
        for (final NeuroImagePBC image : images) {
            m += image.in[index];
        }
        return m / images.length;
    }

    private static double getAverageOutContinuous(final NeuroImagePBC[] images) {
        if (images == null) {
            return 0;
        }
        if (images.length == 0) return 0;
        double m = 0;
        for (final NeuroImagePBC image : images) {
            m += image.out;
        }
        return m / images.length;
    }

    private static double getAverageOutDiscrete(final NeuroImagePBC[] images) {
        if (images == null) {
            return 0;
        }
        if (images.length == 0) return 0;
        Arrays.sort(images, new Comparator<NeuroImagePBC>() {
            @Override
            public int compare(final NeuroImagePBC o1, final NeuroImagePBC o2) {
                if (o1.out < o2.out) return -1;
                if (o1.out > o2.out) return 1;
                return 0;
            }
        });
        double last = images[0].out;
        int maxNum = 0;
        double maxOut = last;
        int num = 0;
        for (final NeuroImagePBC image : images) {
            if (image.out != last) {
                if (num > maxNum) {
                    maxNum = num;
                    maxOut = last;
                }
                num = 0;
                last = image.out;
            }
            num++;
        }
        if (num > maxNum) {
            maxOut = last;
        }
        return maxOut;
    }

//    public static void main(final String[] args) {
//        final NeuroImagePBC[] images = new NeuroImagePBC[]{
//                new NeuroImagePBC(null, 0),
//                new NeuroImagePBC(null, 1),
//                new NeuroImagePBC(null, 2),
//                new NeuroImagePBC(null, 1),
//                new NeuroImagePBC(null, 2),
//                new NeuroImagePBC(null, 2)};
//        System.out.println(getAverageOutDiscrete(images));
//    }

//    private static double getAverageOutContinuous(
//            final NeuroImagePBC[] trainImages,
//            final NeuroImagePBC[] testImages) {
//        double m = 0;
//        int n = 0;
//        if (trainImages != null) {
//            for (final NeuroImagePBC image : trainImages) {
//                m += image.out;
//                ++n;
//            }
//        }
//        if (testImages != null) {
//            for (final NeuroImagePBC image : testImages) {
//                m += image.out;
//                ++n;
//            }
//        }
//        if (n == 0) {
//            return 0;
//        } else {
//            return m / n;
//        }
//    }

    /**
     * Вычислить общую дисперсию обучающих выходов.
     *
     * @param images массив образов
     * @return общая дисперсия обучающих выходов
     */
    protected static double getVariance(final NeuroImagePBC[] images) {
        if (images.length == 0) return 0;
        final double sum = images.length;
        double sum1 = 0;
        double sum2 = 0;
        for (final NeuroImagePBC image : images) {
            final double out = image.out;
            final double d = out / sum;
            sum1 += d;
            sum2 += out * d;
        }
        return sum2 - sum1 * sum1;
    }

    protected static double getErrorOfAverageContinuous(
            final NeuroImagePBC[] images,
            final double averageOut) {
        if (images == null) new Exception(" Empty set of images. ");
        if (images.length == 0) return 0;
        double e = 0;
        try {
            double d;
            for (final NeuroImagePBC image : images) {
                d = averageOut - image.out;
                e += d * d;
            }
        } catch (final Exception ex) {
            System.out.println("Error: getErrorOfAverageContinuous");
        }
        return e / images.length;
    }

    protected static double getErrorQuadraticContinuous(
            final NeuroMapPartitionByCorrelation3.Structure structure,
            final NeuroImagePBC[] images,
            final int level) {
        if (images == null) new Exception(" Empty set of images. ");
        if (images.length == 0) return 0;
        double e = 0;
        try {
            double d;
            for (final NeuroImagePBC image : images) {
                final double out = structure.propagate(image.in, level);
                d = out - image.out;
                e += d * d;
            }
        } catch (final Exception ex) {
            System.out.println("Error: getErrorQuadraticContinuous");
        }
        e /= images.length;
        if (Double.isNaN(e)) {
            e = 1E10;
        }
        return e;
    }

    protected static double getErrorOfAverageDiscrete(
            final NeuroImagePBC[] images,
            final double averageOut) {
        if (images == null) new Exception(" Empty set of images. ");
        if (images.length == 0) return 0;
        double e = 0;
        try {
            double d;
            for (final NeuroImagePBC image : images) {
                final double out = NeuroCommon.signum(averageOut);
                if (out == -1.0D) {
                    if (image.out == -1) {
                        d = 0.0;
                    } else {
                        if (image.out == 0.0D) {
                            d = 1.0;
                        } else {
                            d = 1.1;
                        }
                    }
                } else {
                    if (out == 0.0D) {
                        if (image.out == -1) {
                            d = 1.0;
                        } else {
                            if (image.out == 0.0D) {
                                d = 0.0;
                            } else {
                                d = 1.0;
                            }
                        }
                    } else {
                        if (image.out == -1) {
                            d = 1.1;
                        } else {
                            if (image.out == 0.0D) {
                                d = 1.0;
                            } else {
                                d = 0.0;
                            }
                        }
                    }
                }
                e += d;
            }
        } catch (final Exception ex) {
            System.out.println("Error: getErrorOfAverageDiscrete");
        }
        return e / images.length;
    }

    protected static double getErrorDiscrete(
            final NeuroMapPartitionByCorrelation3.Structure structure,
            final NeuroImagePBC[] images,
            final int level) {
        if (images == null) new Exception(" Empty set of images. ");
        if (images.length == 0) return 0;
        double e = 0;
        try {
            double d;
            for (final NeuroImagePBC image : images) {
                final double out = structure.propagate(image.in, level);
                if (out == -1.0D) {
                    if (image.out == -1) {
                        d = 0.0;
                    } else {
                        if (image.out == 0.0D) {
                            d = 1.0;
                        } else {
                            d = 1.1;
                        }
                    }
                } else {
                    if (out == 0.0D) {
                        if (image.out == -1) {
                            d = 1.0;
                        } else {
                            if (image.out == 0.0D) {
                                d = 0.0;
                            } else {
                                d = 1.0;
                            }
                        }
                    } else {
                        if (image.out == -1) {
                            d = 1.1;
                        } else {
                            if (image.out == 0.0D) {
                                d = 1.0;
                            } else {
                                d = 0.0;
                            }
                        }
                    }
                }
                e += d;
            }
        } catch (final Exception ex) {
            System.out.println("Error: getErrorQuadratic");
        }
        e /= images.length;
        if (Double.isNaN(e)) {
            e = 1E10;
        }
        return e;
    }

    protected static double getErrorOfAverageAbsolute(
            final NeuroImagePBC[] images,
            final double averageOut) {
        if (images == null) new Exception(" Empty set of images. ");
        if (images.length == 0) return 0;
        double e = 0;
        try {
            double d;
            for (final NeuroImagePBC image : images) {
                d = averageOut - image.out;
                e += Math.abs(d);
            }
        } catch (final Exception ex) {
            System.out.println("Error: getErrorOfAverageAbsolute");
        }
        return e / images.length;
    }

    protected static double getErrorAbsolute(
            final NeuroMapPartitionByCorrelation3.Structure structure,
            final NeuroImagePBC[] images,
            final int level) {
        if (images == null) new Exception(" Empty set of images. ");
        if (images.length == 0) return 0;
        double e = 0;
        try {
            double d;
            for (final NeuroImagePBC image : images) {
                final double out = structure.propagate(image.in, level);
                d = out - image.out;
                e += Math.abs(d);
            }
        } catch (final Exception ex) {
            System.out.println("Error: getErrorAbsolute");
        }
        e /= images.length;
        if (Double.isNaN(e)) {
            e = 1E10;
        }
        return e;
    }

    private static ImageDivision divideImages(final NeuroImagePBC[] trainImages,
                                              final int bestInIndex,
                                              final double middleValue) {
        int numLess = 0;
        int numNotLess = 0;
        for (final NeuroImagePBC image : trainImages) {
            if (image.in[bestInIndex] < middleValue) {
                numLess++;
            } else {
                numNotLess++;
            }
        }
        if (numLess == 0 || numNotLess == 0) {
            return null;
        }
        final NeuroImagePBC[] lessImages = new NeuroImagePBC[numLess];
        final NeuroImagePBC[] notLessImages = new NeuroImagePBC[numNotLess];
        int indLess = 0;
        int indNotLess = 0;
        for (final NeuroImagePBC image : trainImages) {
            if (image.in[bestInIndex] < middleValue) {
                lessImages[indLess++] = image;
            } else {
                notLessImages[indNotLess++] = image;
            }
        }
        return new ImageDivision(lessImages, notLessImages);
    }

    private static IndexedDoubles[] getCorrelationPBC(IndexedDoubles[] result,
                                                      final NeuroImagePBC[] images) {
        final int numIn = images[0].in.length;
        if (result == null) {
            result = new IndexedDoubles[numIn];
            for (int i = 0; i < result.length; ++i) {
                result[i] = new IndexedDoubles();
            }
        }
        if (result.length != numIn) {
            result = new IndexedDoubles[numIn];
            for (int i = 0; i < result.length; ++i) {
                result[i] = new IndexedDoubles();
            }
        }
        double m = 0;
        for (final NeuroImagePBC image : images) {
            final double y = image.out;
            m += y;
        }
        m /= images.length;
        double a = 0;
        for (final NeuroImagePBC image : images) {
            final double y = image.out;
            a += (y - m) * (y - m);
        }
        a = StrictMath.sqrt(a);

        for (int i = 0; i < numIn; ++i) {
            double mi = 0;
            for (final NeuroImagePBC image : images) {
                final double x = image.in[i];
                mi += x;
            }
            mi /= images.length;
            double b = 0;
            double c = 0;
            for (final NeuroImagePBC image : images) {
                final double x = image.in[i];
                final double y = image.out;
                b += (x - mi) * (y - m);
                c += (x - mi) * (x - mi);
            }
            c = StrictMath.sqrt(c);
            if (a == 0 || c == 0) {
                result[i].setValue(i, 0);
            } else {
                result[i].setValue(i, Math.abs((b / a) / c));
            }
        }

        return result;

    }

    private static IndexedDoubles[] getRandoms(IndexedDoubles[] result,
                                               final NeuroImagePBC[] images,
                                               final Rnd rnd) {
        final int numIn = images[0].in.length;
        if (result == null) {
            result = new IndexedDoubles[numIn];
        }
        if (result.length != numIn) {
            result = new IndexedDoubles[numIn];
        }
        for (int i = 0; i < result.length; ++i) {
            result[i] = new IndexedDoubles(i, -rnd.rnd());
        }
        return result;
    }
}
