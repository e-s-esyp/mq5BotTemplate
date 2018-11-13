package com.gmware.lib.neuro.mynet.Mappings;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.IndexedDoubles;
import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.mynet.NeuroMapping;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapPartitionByCorrelation2;
import com.gmware.lib.neuro.mynet.NeuroMap;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Получение NeuroMapPartitionByCorrelation по статистическим данным.
 * Отличие от NeuroMappingPartitionByCorrelation в том, что точка разделения
 * в NeuroMappingPartitionByCorrelation выбирается просто делением пополам,
 * а в NeuroMappingPartitionByCorrelation2 как среднее по выбранному входу.
 * Добавлено обучение без тестовых образов для малого числа образов.
 * <p/>
 * <p/>
 * Created by Gauss on 25.02.2016.
 */
public class NeuroMappingPartitionByCorrelation2 extends NeuroMapping {

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

    static final int MAX_LEVEL = 20;
    static double trainTestProportion = 0.75;

    public static NeuroMap train(final NetImage[] images,
                                 final NeuroMapType mapType,
                                 final Rnd rnd,
                                 final PrintStream log,
                                 final String netOutPath) {
        if (images == null) return null;
        if (images.length == 0) return null;
        final NeuroMapPartitionByCorrelation2 map =
                new NeuroMapPartitionByCorrelation2(images[0].numIn, images[0].numOut, images.length);
        if (map.numOuts == 3) map.setConvertFCR();
        final int numTrainImages = (int) (images.length * trainTestProportion);
        final int numTestImages = images.length - numTrainImages;
        final NetImage[] trainImages = new NetImage[numTrainImages];
        final NetImage[] testImages = new NetImage[numTestImages];
        selectTrainTestImages(images, trainImages, testImages, rnd);
        train(map, trainImages, testImages, log);
        if (map.rrError >= 1) {
            map.noTest = true;
            log.println("----------------------------------------------------------");
            log.println("TRAIN WITHOUT TEST:");
            train(map, images, images, log);
        }
        return map;
    }

    private static void train(final NeuroMapPartitionByCorrelation2 map,
                              final NetImage[] trainImages,
                              final NetImage[] testImages,
                              final PrintStream log) {
        final NeuroImagePBC[][] trainSelectedImages = convertImages(trainImages, map);
        final NeuroImagePBC[][] testSelectedImages = convertImages(testImages, map);
        for (int i = 0; i < map.numStructures; ++i) {
            int minImages = (int) (trainSelectedImages[i].length * 0.0005);
            if (minImages < 100) {
                minImages = 100;
            }
            map.structure[i] = trainStructure(MAX_LEVEL, minImages, trainSelectedImages[i], testSelectedImages[i], log);
        }
        final IndexedDoubles[] e = new IndexedDoubles[MAX_LEVEL];
        for (int i = 0; i < MAX_LEVEL; ++i) {
            map.level = i;
            map.setErrors(trainImages, testImages);
            e[i] = new IndexedDoubles(i, map.rrError);
        }
        map.errorByLevel = e;
        log.println(map);
        Arrays.sort(e, new Comparator<IndexedDoubles>() {
            @Override
            public int compare(final IndexedDoubles o1, final IndexedDoubles o2) {
                if (o1.value < o2.value) return -1;
                if (o1.value > o2.value) return 1;
                return 0;
            }
        });
        map.level = e[0].index;
        map.restrict();
        map.setErrors(trainImages, testImages);
        log.println("");
        log.println("RESTRICTED BY BEST LEVEL:");
        log.println(map);
    }

    private static NeuroImagePBC[][] convertImages(final NetImage[] images,
                                                   final NeuroMapPartitionByCorrelation2 map) {
        if (map.convertFCR && map.numOuts == 3) {
            final NeuroImagePBC[] convertedImages0 = new NeuroImagePBC[images.length];
            int num = 0;
            int i = 0;
            for (final NetImage image : images) {
                final double[] out = NeuroMapPartitionByCorrelation2.convertFCR(image.out);
                convertedImages0[i++] = new NeuroImagePBC(image.in, out[0]);
                if (!Double.isNaN(out[1])) {
                    ++num;
                }
            }
            final NeuroImagePBC[] convertedImages1 = new NeuroImagePBC[num];
            i = 0;
            for (final NetImage image : images) {
                final double[] out = NeuroMapPartitionByCorrelation2.convertFCR(image.out);
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

    private static NeuroMapPartitionByCorrelation2.Structure trainStructure(
            final int recursionIndex,
            final int minImages,
            final NeuroImagePBC[] trainImages,
            final NeuroImagePBC[] testImages,
            final PrintStream log) {
        final NeuroMapPartitionByCorrelation2.Structure structure = new NeuroMapPartitionByCorrelation2.Structure();
        structure.numOfImages = trainImages.length;
        structure.average = getAverageOut(trainImages);
        if (recursionIndex > 0 && trainImages.length >= 2 * minImages) {
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
            if (trainImages.length < minImages) {
                return structure;
            }
            structure.divided = true;
            structure.correlation = correlations[correlationsIndex].value;
            structure.inInd = bestInIndex;
            structure.divisor = middleValue;
            structure.lessPart = trainStructure(
                    recursionIndex - 1,
                    minImages,
                    trainImagesDivision.less,
                    testImagesDivision.less,
                    log);
            structure.notLessPart = trainStructure(
                    recursionIndex - 1,
                    minImages,
                    trainImagesDivision.notLess,
                    testImagesDivision.notLess,
                    log);
            if (isBetterThenAverage(structure, trainImages, testImages, recursionIndex)) {
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
            final NeuroMapPartitionByCorrelation2.Structure structure,
            final NeuroImagePBC[] trainImages,
            final NeuroImagePBC[] testImages,
            final int level) {
        final double averageOut = getAverageOut(trainImages);
        final double trnVar = getVariance(trainImages);
        final double tstVar = getVariance(testImages);

        final double trnEA = getErrorOfAverage(trainImages, averageOut);
        final double tstEA = getErrorOfAverage(testImages, averageOut);
        final double trnREA = trnVar > 0.0 ? trnEA / trnVar : 1.0;
        final double tstREA = tstVar > 0.0 ? tstEA / tstVar : 1.0;
        final double relativeErrorOfAverage = Math.max(trnREA, tstREA);

        final double trnE = getErrorQuadratic(structure, trainImages, level);
        final double tstE = getErrorQuadratic(structure, testImages, level);
        final double trnRE = trnVar > 0.0 ? trnE / trnVar : 1.0;
        final double tstRE = tstVar > 0.0 ? tstE / tstVar : 1.0;
        final double relativeError = Math.max(trnRE, tstRE);

        return relativeError < relativeErrorOfAverage;
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

    private static double getAverageOut(final NeuroImagePBC[] images) {
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

    private static double getAverageOut(
            final NeuroImagePBC[] trainImages,
            final NeuroImagePBC[] testImages) {
        double m = 0;
        int n = 0;
        if (trainImages != null) {
            for (final NeuroImagePBC image : trainImages) {
                m += image.out;
                ++n;
            }
        }
        if (testImages != null) {
            for (final NeuroImagePBC image : testImages) {
                m += image.out;
                ++n;
            }
        }
        if (n == 0) {
            return 0;
        } else {
            return m / n;
        }
    }

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

    protected static double getErrorOfAverage(
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
            System.out.println("Error: getErrorOfAverage");
        }
        return e / images.length;
    }

    protected static double getErrorQuadratic(
            final NeuroMapPartitionByCorrelation2.Structure structure,
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
            System.out.println("Error: getErrorQuadratic");
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


}
