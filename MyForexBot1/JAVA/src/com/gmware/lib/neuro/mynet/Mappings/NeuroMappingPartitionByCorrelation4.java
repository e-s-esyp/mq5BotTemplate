package com.gmware.lib.neuro.mynet.Mappings;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.*;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapPartition;
import com.gmware.lib.neuro.NetImage;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Одна структура.
 * В структуре несколько выходов.
 * <p/>
 * Created by Gauss on 11.04.2016.
 * Копия NeuroMappingPartitionByCorrelation3, но в структуре несколько выходов.
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
public class NeuroMappingPartitionByCorrelation4 extends NeuroMapping {

    public static int maxLevel = 20;
    public static double proportion = 0.50;
    // тип ошибки и тип выходов, которые хотим получить:
    // 0x10 - weighted
    // 0x20 - continuous out
    // 0x01 - quadratic error
    // 0x02 - abs error
    public static NeuroComputationType errorType = new NeuroComputationType();
    public static boolean hasTesting = true;
    public static boolean restrictByLevel = true;
    public static boolean restrictByImages = false;
    public static boolean balanceImages = false;
    public static boolean randomized = false;

    //----------------------------------------------------------

    private static class ImageDivision {
        NetImage[] less = null;
        NetImage[] notLess = null;

        ImageDivision(final NetImage[] l, final NetImage[] n) {
            less = l;
            notLess = n;
        }
    }

    //----------------------------------------------------------
    @Override
    protected NeuroMap trainMap(final NetImage[] images,
                                final NeuroMapType mapType,
                                final Rnd rnd,
                                final PrintStream log,
                                final String netOutPath) {
        return train(images, mapType, rnd, log, netOutPath);
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
        final NeuroMapPartition map =
                new NeuroMapPartition(images[0].numIn, images[0].numOut, images.length,errorType);
        if (hasTesting) {
            final int numTrainImages = (int) (images.length * proportion);
            final int numTestImages = images.length - numTrainImages;
            final NetImage[] trainImages = new NetImage[numTrainImages];
            final NetImage[] testImages = new NetImage[numTestImages];
            selectTrainTestImages(images, trainImages, testImages, rnd);
            map.computationType.setWeights(map.getAverageOut(trainImages));
            train(map, trainImages, testImages, rnd);
            if (map.rrError >= 1) {
                map.noTest = true;
                map.computationType.setWeights(map.getAverageOut(images));
                train(map, images, images, rnd);
            }
        } else {
            map.noTest = true;
            map.computationType.setWeights(map.getAverageOut(images));
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

    private static void train(final NeuroMapPartition map,
                              final NetImage[] trainImages,
                              final NetImage[] testImages,
                              final Rnd rnd) {
        map.structure = (randomized) ?
                trainStructureR(map, maxLevel, trainImages, testImages, rnd) :
                trainStructure(map, maxLevel, trainImages, testImages);
        map.levelLimit = maxLevel;
        if (restrictByLevel) {
            final IndexedDoubles[] e = new IndexedDoubles[maxLevel];
            for (int i = 0; i < maxLevel; ++i) {
                map.levelLimit = i;
                map.setErrors(trainImages, testImages);
                e[i] = new IndexedDoubles(i, map.rrError);
            }
            map.errorByLevel = e;
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

    private static NeuroBranch trainStructure(
            final NeuroMapPartition map,
            final int recursionIndex,
            final NetImage[] trainImages,
            final NetImage[] testImages) {
        final NeuroBranch structure = new NeuroBranch(map.numKlasses);
        structure.out.numImages = trainImages.length;
        structure.average = map.getAverageOut(trainImages);
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
                Arrays.sort(trainImages, new Comparator<NetImage>() {
                    @Override
                    public int compare(final NetImage o1, final NetImage o2) {
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
            structure.determination = correlations[correlationsIndex].value;
            structure.inInd = bestInIndex;
            structure.divisor = middleValue;
            structure.lessPart = trainStructure(
                    map,
                    recursionIndex - 1,
                    trainImagesDivision.less,
                    testImagesDivision.less);
            structure.notLessPart = trainStructure(
                    map,
                    recursionIndex - 1,
                    trainImagesDivision.notLess,
                    testImagesDivision.notLess);
            if (isBetterThenAverage(map, structure, trainImages, testImages, recursionIndex)) {
                return structure;
            } else {
                structure.divided = false;
                return structure;
            }
        } else {
            return structure;
        }
    }

    private static boolean isBetterThenAverage(
            final NeuroMapPartition map,
            final NeuroBranch structure,
            final NetImage[] trainImages,
            final NetImage[] testImages,
            final int level) {
        structure.divided = false;
        final double trnEA = map.getError(structure, trainImages, level);
        final double tstEA = map.getError(structure, testImages, level);
        final double relativeErrorOfAverage = Math.max(trnEA, tstEA);
        structure.divided = true;
        final double trnE = map.getError(structure, trainImages, level);
        final double tstE = map.getError(structure, testImages, level);
        final double relativeError = Math.max(trnE, tstE);

        return relativeError < relativeErrorOfAverage;
    }

    private static NeuroBranch trainStructureR(
            final NeuroMapPartition map,
            final int recursionIndex,
            final NetImage[] trainImages,
            final NetImage[] testImages,
            final Rnd rnd) {
        final int numOfImages = trainImages.length;
        final NeuroBranch averageStructure = new NeuroBranch(map.numKlasses);
        averageStructure.out.numImages = numOfImages;
        averageStructure.average = map.getAverageOut(trainImages);
        if (recursionIndex > 0 && trainImages.length >= 2) {
            final IndexedDoubles[] correlations = getCorrelationPBC(null, trainImages);
            final IndexedDoubles[] randoms = getRandoms(null, trainImages, rnd);
            final NeuroBranch[] structures = new NeuroBranch[]{
                    averageStructure,
                    trainStructureByOrder(map, correlations, averageStructure, recursionIndex, trainImages, testImages, rnd),
                    trainStructureByOrder(map, randoms, averageStructure, recursionIndex, trainImages, testImages, rnd)};
            for (final NeuroBranch structure : structures) {
                final double trainError = map.getError(structure, trainImages, recursionIndex);
                final double testError = map.getError(structure, testImages, recursionIndex);
                structure.error = (trainError > testError) ? trainError : testError;
            }
            Arrays.sort(structures, new Comparator<NeuroBranch>() {
                @Override
                public int compare(final NeuroBranch o1, final NeuroBranch o2) {
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

    private static NeuroBranch trainStructureByOrder(
            final NeuroMapPartition map,
            final IndexedDoubles[] order,
            final NeuroBranch averageStructure,
            final int recursionIndex,
            final NetImage[] trainImages,
            final NetImage[] testImages,
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
            Arrays.sort(trainImages, new Comparator<NetImage>() {
                @Override
                public int compare(final NetImage o1, final NetImage o2) {
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
        final NeuroBranch structure =
                new NeuroBranch(map.numKlasses);
        structure.out.numImages = averageStructure.out.numImages;
        structure.average = averageStructure.average;
        structure.divided = true;
        structure.determination = order[orderIndex].value;
        structure.inInd = bestInIndex;
        structure.divisor = middleValue;
        structure.lessPart = trainStructureR(
                map,
                recursionIndex - 1,
                trainImagesDivision.less,
                testImagesDivision.less,
                rnd);
        structure.notLessPart = trainStructureR(
                map,
                recursionIndex - 1,
                trainImagesDivision.notLess,
                testImagesDivision.notLess,
                rnd);
        return structure;
    }


    private static double getAverageIn(final NetImage[] images,
                                       final int index) {
        if (images == null) {
            return 0;
        }
        if (images.length == 0) {
            return 0;
        }
        double m = 0;
        for (final NetImage image : images) {
            m += image.in[index];
        }
        return m / images.length;
    }

    private static ImageDivision divideImages(final NetImage[] trainImages,
                                              final int bestInIndex,
                                              final double middleValue) {
        int numLess = 0;
        int numNotLess = 0;
        for (final NetImage image : trainImages) {
            if (image.in[bestInIndex] < middleValue) {
                numLess++;
            } else {
                numNotLess++;
            }
        }
        if (numLess == 0 || numNotLess == 0) {
            return null;
        }
        final NetImage[] lessImages = new NetImage[numLess];
        final NetImage[] notLessImages = new NetImage[numNotLess];
        int indLess = 0;
        int indNotLess = 0;
        for (final NetImage image : trainImages) {
            if (image.in[bestInIndex] < middleValue) {
                lessImages[indLess++] = image;
            } else {
                notLessImages[indNotLess++] = image;
            }
        }
        return new ImageDivision(lessImages, notLessImages);
    }

    private static IndexedDoubles[] getCorrelationPBC(IndexedDoubles[] result,
                                                      final NetImage[] images) {
        if (images == null) return result;
        if (images.length == 0) return result;
        final int numIn = images[0].numIn;
        final int numOut = images[0].numOut;
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
        for (int i = 0; i < result.length; ++i) {
            result[i].setValue(i, 0);
        }
        for (int outInd = 0; outInd < numOut; outInd++) {
            double m = 0; //M(out[outInd])
            for (final NetImage image : images) {
                final double y = image.out[outInd];
                m += y;
            }
            m /= images.length;
            double a = 0; //D(out
            for (final NetImage image : images) {
                final double y = image.out[outInd];
                a += (y - m) * (y - m);
            }
            a = StrictMath.sqrt(a);
            for (int inInd = 0; inInd < numIn; ++inInd) {
                double mi = 0;
                for (final NetImage image : images) {
                    final double x = image.in[inInd];
                    mi += x;
                }
                mi /= images.length;
                double b = 0;
                double c = 0;
                for (final NetImage image : images) {
                    final double x = image.in[inInd];
                    final double y = image.out[outInd];
                    b += (x - mi) * (y - m);
                    c += (x - mi) * (x - mi);
                }
                c = StrictMath.sqrt(c);
                final double v = (a == 0 || c == 0) ? 0 : Math.abs((b / a) / c);
                if (v > result[inInd].value) result[inInd].value = v;
            }
        }
        return result;
    }

    private static IndexedDoubles[] getRandoms(IndexedDoubles[] result,
                                               final NetImage[] images,
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
