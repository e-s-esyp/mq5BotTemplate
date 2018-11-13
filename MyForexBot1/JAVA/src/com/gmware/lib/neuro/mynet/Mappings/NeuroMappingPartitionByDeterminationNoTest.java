package com.gmware.lib.neuro.mynet.Mappings;

/**
 * ---
 * Created by Gauss on 13.04.2016.
 */

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.*;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapPartition;
import com.gmware.lib.games.holdem.common.rnd.Rnd517;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * регрессор решающего дерева
 * при помощи увеличени€ определенности
 * <p/>
 * Created by Gauss on 13.04.2016.
 * <p/>
 * ќдна структура.
 * ¬ структуре несколько выходов.
 * <p/>
 * Created by Gauss on 11.04.2016.
 *  опи€ NeuroMappingPartitionByCorrelation3, но в структуре несколько выходов.
 * ѕолучение NeuroMapPartitionByCorrelation по статистическим данным.
 * ќтличие от NeuroMappingPartitionByCorrelation в том, что точка разделени€
 * в NeuroMappingPartitionByCorrelation выбираетс€ просто делением пополам,
 * а в NeuroMappingPartitionByCorrelation3 как среднее по выбранному входу.
 * ƒобавлено обучение без тестовых образов дл€ малого числа образов.
 * <p/>
 * <p/>
 * Created by Gauss on 25.02.2016.
 */
//TODO: классификаци€ k-среднего, параметр - близость к собирательному образу
public class NeuroMappingPartitionByDeterminationNoTest extends NeuroMapping {

    public static int maxLevel = 40;
    public static int minImagesDefault = 0;
    //ѕроцент тренировочных образов.
    //≈сли proportion==1, то тестовое обрезание не проводитс€.
    public static double proportion = 0.5;
    // тип ошибки и тип выходов, которые хотим получить:
    public static NeuroComputationType errorTypeDefault = new NeuroComputationType();

    static {
        errorTypeDefault.setF();
    }

    public static final boolean hasTesting = false;
    public static boolean restrictByLevel = true;
    public static boolean restrictByImages = true;
    public static boolean randomized = false;

    //----------------------------------------------------------

    private static class IndexedDetermination {
        public double value = 0;
        public boolean divided = false;
        public double divisor = 0;
        public int inIndex = -1;
        public int lKlass = -1;
        public int rKlass = -1;
        public ImageDivision train = null;

        public IndexedDetermination(final int i) {
            inIndex = i;
        }

        public void setIfDivided() {
            divided = (train != null);
        }
    }

    private static class ImageDivision {
        NeuroImage[] less = null;
        NeuroImage[] notLess = null;

        ImageDivision(final NeuroImage[] l, final NeuroImage[] n) {
            less = l;
            notLess = n;
        }
    }

    private static class Doubles2 {
        public double value = 0;
        public double divisor = 0;
        public int center = 0;

        public Doubles2(final double v, final int c, final double d) {
            value = v;
            divisor = d;
            center = c;
        }

        @Override
        public String toString() {
            return "(" +
                    String.format("%1.4f", value).replace(',', '.') + "," +
                    center + "," +
                    String.format("%1.4f", divisor).replace(',', '.') + ")";
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
        final NeuroMapPartition map =
                new NeuroMapPartition(images[0].numIn, images[0].numOut, images.length, getErrorType(images));
        final NeuroImage[] mapImages = map.convertImages(images);
        map.noTest = true;
        map.computationType.setWeights(map.getAverageOut(images));
        train(map, mapImages, rnd);
        log.print(map);
        return map;
    }

    /**
     * ќбучалка по готовым NeuroImage[]
     *
     * @param mapImages       - образы
     * @param rnd             - гсч
     * @param log             - лог
     * @param computationType - тип вычислений: ошибка, веса, ...
     * @return решающее дерево
     */
    public static NeuroMap train(
            final NeuroImage[] mapImages,
            final Rnd rnd,
            final PrintStream log,
            final NeuroComputationType computationType) {
        if (mapImages == null) return null;
        if (mapImages.length == 0) return null;
        final NeuroMapPartition map =
                new NeuroMapPartition(mapImages[0].numIn, mapImages[0].numOut, mapImages.length, computationType);
        map.noTest = true;
        map.computationType.setWeights(map.getKlassesProbability(mapImages));
        train(map, mapImages, rnd);
        log.print(map);
        return map;
    }

    //TODO
    public static NeuroMapPartition train(final NeuroImage[] images,
                                          final int[] par,
                                          final Rnd rnd,
                                          final PrintStream log,
                                          final NeuroComputationType computationType) {
        if (images == null) return null;
        if (images.length == 0) return null;
        final NeuroMapPartition map =
                new NeuroMapPartition(images[0].numIn, images[0].numOut, images.length, computationType);
        map.noTest = true;
        map.computationType.setWeights(map.getKlassesProbability(images));
        final NeuroBranch branch = new NeuroBranch(map.numKlasses);
        map.structure = branch;
        trainBranchByPar(map, branch, par, 0, images);
        log.print(map);
        return map;
    }

    //TODO
    static void trainBranchByPar(final NeuroMapPartition map,
                                 final NeuroBranch branch,
                                 final int[] par,
                                 final int recursionIndex,
                                 final NeuroImage[] images) {
        if (images.length == 0) return;
        branch.out.numImages = images.length;
        final long[] a = map.getKlassesSize(images);
        branch.average = map.getAverageOut(a, images.length);
        branch.out.profits = map.getProfits(images);
        map.setBestKlass(branch, images);
        branch.divided = false;
        if (recursionIndex < par.length) {
            final IndexedDetermination determination =
                    getDeterminationByPar(images, par[recursionIndex]);
            if (determination == null) {
                return;
            }
//-----------------------------------
            branch.divided = true;
            branch.determination = determination.value;
            branch.inInd = determination.inIndex;
            branch.divisor = determination.divisor;
            branch.lessPart = new NeuroBranch(map.numKlasses);
            branch.notLessPart = new NeuroBranch(map.numKlasses);
            trainBranchByPar(
                    map,
                    branch.lessPart,
                    par,
                    recursionIndex + 1,
                    determination.train.less);
            trainBranchByPar(
                    map,
                    branch.notLessPart,
                    par,
                    recursionIndex + 1,
                    determination.train.notLess);
//---------------------------------
        }
    }

    //TODO
    private static IndexedDetermination getDeterminationByPar(
            final NeuroImage[] images,
            final int i) {
        if (images == null) return null;
        if (images.length < 2) return null;
        sortImages(images, i);
        final IndexedDetermination determination = new IndexedDetermination(i);
        determination.divisor = images[(images.length + 1) / 2].in[i];
        determination.train = divideImages(images, i, determination.divisor);
        determination.setIfDivided();
        if (determination.divided) {
            return determination;
        }
        return null;
    }

    /**
     * ќбучалка по готовым NeuroImage[]
     *
     * @param images          - образы
     * @param rnd             - гсч
     * @param log             - лог
     * @param computationType - тип вычислений: ошибка, веса, ...
     * @return решающее дерево
     */
    public static NeuroMapPartition trainContinuous(
            final NeuroImage[] images,
            final Rnd rnd,
            final PrintStream log,
            final NeuroComputationType computationType) {
        if (images == null) return null;
        if (images.length == 0) return null;
        final int numTrainImages = (int) (images.length * proportion);
        final int numTestImages = images.length - numTrainImages;
//        System.out.println("numTrainImages = " + numTrainImages);
//        System.out.println("numTestImages = " + numTestImages);
        final NeuroImage[] trainImages = new NeuroImage[numTrainImages];
        final NeuroImage[] testImages = new NeuroImage[numTestImages];
        selectTrainTestOur(images, trainImages, testImages, rnd);
        final NeuroMapPartition map =
                new NeuroMapPartition(images[0].numIn, images[0].numOut, images.length, computationType);
        map.noTest = true;
        map.computationType.setWeights(map.getKlassesProbability(trainImages));
        train(map, trainImages, rnd);
        restrict(map.computationType.errorTypeNum, map.structure, testImages);
        map.structure.setError(map.computationType.errorTypeNum, testImages, maxLevel);
        log.print(map);
        return map;
    }

    protected static void selectTrainTestOur(final NeuroImage[] images,
                                             final NeuroImage[] trainImages,
                                             final NeuroImage[] testImages,
                                             final Rnd rnd) {
        int trainNum = 0;
        int testNum = 0;
        for (final NeuroImage image : images) {
            final int trainLack = trainImages.length - trainNum;
            final int testLack = testImages.length - testNum;
            if (rnd.rnd(trainLack + testLack) < trainLack) {
                trainImages[trainNum++] = image;
            } else {
                testImages[testNum++] = image;
            }
        }
    }


    private static double restrict(final int ct, final NeuroBranch structure, final NeuroImage[] validationImages) {
        if (validationImages == null) return structure.error;
        if (validationImages.length == 0) return structure.error;
        if (structure.divided) {
            int numLeft = 0;
            for (final NeuroImage image : validationImages) {
                if (image.in[structure.inInd] < structure.divisor) {
                    ++numLeft;
                }
            }
            if (numLeft == 0 || numLeft == validationImages.length) {
                structure.setError(ct, validationImages, 20);
                return structure.error;
            }
            final NeuroImage[] leftImages = new NeuroImage[numLeft];
            final NeuroImage[] rightImages = new NeuroImage[validationImages.length - numLeft];
            int l = 0;
            int r = 0;
            for (final NeuroImage image : validationImages) {
                if (image.in[structure.inInd] < structure.divisor) {
                    leftImages[l++] = image;
                } else {
                    rightImages[r++] = image;
                }
            }
            final double el = restrict(ct, structure.lessPart, leftImages) * leftImages.length;
            final double er = restrict(ct, structure.notLessPart, rightImages) * rightImages.length;
            structure.divided = false;
            structure.setError(ct, validationImages, 0);
            if (el + er <= structure.error * validationImages.length) {
                structure.error = (el + er) / validationImages.length;
                structure.divided = true;
            }
            return structure.error;
        }
        structure.setError(ct, validationImages, 0);
        return structure.error;
    }

    private static NeuroComputationType getErrorType(final NetImage[] images) {
        return errorTypeDefault;
    }

    private static void train(final NeuroMapPartition map,
                              final NeuroImage[] trainImages,
                              final Rnd rnd) {
        final NeuroBranch structure = new NeuroBranch(map.numKlasses);
        map.structure = structure;
        final int minImages = getMinImages(trainImages.length);
        if (randomized) {
            trainStructureR(map, structure, maxLevel, minImages, trainImages, rnd);
        } else {
            trainStructure(map, structure, maxLevel, minImages, trainImages);
        }
        map.levelLimit = maxLevel;
        if (restrictByLevel) {
            final IndexedDoubles[] e = new IndexedDoubles[maxLevel];
            for (int i = 0; i < maxLevel; ++i) {
                map.levelLimit = i;
                map.setErrors(trainImages);
                e[i] = new IndexedDoubles(i, map.rrError);
            }
            map.errorByLevel = e;
            map.levelLimit = maxLevel;
            map.setErrors(trainImages);
        }
        if (restrictByImages) {
            final IndexedDoubles[] e = new IndexedDoubles[21];
            for (int i = 0; i < e.length; ++i) {
                map.imagesLimit = 1 << i;
                map.setErrors(trainImages);
                e[i] = new IndexedDoubles(i, map.rrError);
            }
            map.errorByImages = e;
            map.imagesLimit = 0;
            map.setErrors(trainImages);
        }
    }

    private static int getMinImages(final int numImages) {
//        int minImages = (int) (numImages * 0.0005);
//        if (minImages < minImagesDefault) {
//            minImages = minImagesDefault;
//        }
//        return minImages;
        return minImagesDefault;
    }

    private static void trainStructure(
            final NeuroMapPartition map,
            final NeuroBranch structure,
            final int recursionIndex,
            final int minImages,
            final NeuroImage[] trainImages) {
        structure.out.numImages = trainImages.length;
        final long[] a = map.getKlassesSize(trainImages);
        structure.average = map.getAverageOut(a, trainImages.length);
        structure.out.profits = map.getProfits(trainImages);
        map.setBestKlass(structure, trainImages);
        if (recursionIndex > 0 && trainImages.length >= minImages) {
            final IndexedDetermination determination = getBestDetermination(trainImages, a);
            if (determination == null) {
                return;
            }
//-----------------------------------
            structure.divided = true;
            structure.determination = determination.value;
            structure.inInd = determination.inIndex;
            structure.divisor = determination.divisor;
            structure.lessPart = new NeuroBranch(map.numKlasses);
            structure.notLessPart = new NeuroBranch(map.numKlasses);
            trainStructure(
                    map,
                    structure.lessPart,
                    recursionIndex - 1,
                    minImages,
                    determination.train.less);
            trainStructure(
                    map,
                    structure.notLessPart,
                    recursionIndex - 1,
                    minImages,
                    determination.train.notLess);
//---------------------------------
            structure.divided = map.isBetterThenAverage(structure, trainImages, recursionIndex);
        }
    }

    private static NeuroBranch trainStructureR(
            final NeuroMapPartition map,
            final NeuroBranch averageStructure,
            final int recursionIndex,
            final int minImages,
            final NeuroImage[] trainImages,
            final Rnd rnd) {
        averageStructure.out.numImages = trainImages.length;
        final long[] a = map.getKlassesSize(trainImages);
        averageStructure.average = map.getAverageOut(a, trainImages.length);
        map.setBestKlass(averageStructure, trainImages);
        final IndexedDetermination bestDetermination = getBestDetermination(trainImages, a);
        if (recursionIndex > 0 && trainImages.length >= minImages) {
            final NeuroBranch[] structures = new NeuroBranch[]{
                    averageStructure,
                    trainStructureByDetermination(
                            map,
                            bestDetermination,
                            averageStructure,
                            recursionIndex,
                            minImages,
                            trainImages,
                            rnd),
                    trainStructureByDetermination(
                            map,
                            getRandomDetermination(trainImages, rnd),
                            averageStructure,
                            recursionIndex,
                            minImages,
                            trainImages,
                            rnd)};
            for (final NeuroBranch structure : structures) {
                structure.setError(map.computationType.errorTypeNum, trainImages, recursionIndex);
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

    private static NeuroBranch trainStructureByDetermination(
            final NeuroMapPartition map,
            final IndexedDetermination determination,
            final NeuroBranch averageStructure,
            final int recursionIndex,
            final int minImages,
            final NeuroImage[] trainImages,
            final Rnd rnd) {
        if (determination == null) {
            return averageStructure;
        }
        final NeuroBranch structure =
                new NeuroBranch(map.numKlasses);
        structure.out.numImages = averageStructure.out.numImages;
        structure.average = averageStructure.average;
        structure.out.klass = averageStructure.out.klass;
        //-----------------------------------
        structure.divided = true;
        structure.determination = determination.value;
        structure.inInd = determination.inIndex;
        structure.divisor = determination.divisor;
        structure.lessPart = new NeuroBranch(map.numKlasses);
        structure.notLessPart = new NeuroBranch(map.numKlasses);
        trainStructureR(
                map,
                structure.lessPart,
                recursionIndex - 1,
                minImages,
                determination.train.less,
                rnd);
        trainStructureR(
                map,
                structure.notLessPart,
                recursionIndex - 1,
                minImages,
                determination.train.notLess,
                rnd);
        return structure;
    }

    //точное разбиение
    private static IndexedDetermination getBestDetermination1(
            final NeuroImage[] trainImages,
            final long[] a) {
        final int numIn = trainImages[0].numIn;
        final int numKlasses = trainImages[0].numKlasses;
        final IndexedDetermination[] determinations = new IndexedDetermination[numIn];
        if (getNumNonZeroes(a) == 1) return null;
        for (int i = 0; i < numIn; ++i) {
            sortImages(trainImages, i);
            determinations[i] = new IndexedDetermination(i);
            final long[] al = new long[numKlasses];
            final long[] ar = new long[numKlasses];
            final Doubles2[] b = new Doubles2[trainImages.length];
            b[0] = new Doubles2(-1, -1, trainImages[0].in[i]);
            System.arraycopy(a, 0, ar, 0, a.length);
            al[trainImages[0].klass]++;
            ar[trainImages[0].klass]--;
            for (int j = 1; j < trainImages.length; j++) {
                double detl = 0;
                double detr = 0;
                for (int k = 0; k < numKlasses; ++k) {
                    if (a[k] != 0) {
                        final double dl = ((double) al[k]) / (a[k] * j);
                        final double dr = ((double) ar[k]) / (a[k] * (trainImages.length - j));
                        if (detl < dl) detl = dl;
                        if (detr < dr) detr = dr;
                    }
                }
                detl *= trainImages.length;
                detr *= trainImages.length;
                final int center = Math.abs(j - trainImages.length / 2);
                final double det =
                        Math.min(detl, detr);
//                        detl * detr * Math.pow(Math.min(detl, detr), 10);
                if (trainImages[j - 1].in[i] < trainImages[j].in[i]) {
                    b[j] = new Doubles2(det, center, trainImages[j].in[i]);
                } else {
                    b[j] = new Doubles2(-1, -1, trainImages[j].in[i]);
                }
                al[trainImages[j].klass]++;
                ar[trainImages[j].klass]--;
            }
            Arrays.sort(b, new Comparator<Doubles2>() {
                @Override
                public int compare(final Doubles2 o1, final Doubles2 o2) {
                    if (o1.value > o2.value) return -1;
                    if (o1.value < o2.value) return 1;
                    if (o1.center < o2.center) return -1;
                    if (o1.center > o2.center) return 1;
                    return 0;
                }
            });
            determinations[i].value = b[0].value;
            determinations[i].divisor = b[0].divisor;
            determinations[i].divided = true;
        }
        Arrays.sort(determinations, new Comparator<IndexedDetermination>() {
            @Override
            public int compare(final IndexedDetermination o1, final IndexedDetermination o2) {
                if (o1.value > o2.value) return -1;
                if (o1.value < o2.value) return 1;
                return 0;
            }
        });
        int index = 0;
        IndexedDetermination result;
        while (index < determinations.length) {
            result = determinations[index++];
            result.train = divideImages(trainImages, result.inIndex, result.divisor);
            result.setIfDivided();
            if (result.divided) return result;
        }
        return null;
    }

    //приближенное разбиение
    private static IndexedDetermination getBestDetermination2(
            final NeuroImage[] trainImages,
            final long[] a) {
        final int numIn = trainImages[0].numIn;
        final int numKlasses = trainImages[0].numKlasses;
        final IndexedDetermination[] determinations = new IndexedDetermination[numIn];
        if (getNumNonZeroes(a) == 1) return null;
        for (int i = 0; i < numIn; ++i) {
            determinations[i] = new IndexedDetermination(i);
            final double[] v = new double[numKlasses];
            for (final NeuroImage image : trainImages) {
                v[image.klass] += image.in[i];
            }
            double divisor = 0;
            for (int j = 0; j < a.length; j++) {
                divisor += v[j] / a[j];
            }
            determinations[i].divisor = divisor / numKlasses;
            final long[] al = new long[numKlasses];
            final long[] ar = new long[numKlasses];
            int j = 0;
            for (final NeuroImage image : trainImages) {
                if (image.in[i] < divisor) {
                    al[image.klass]++;
                    ++j;
                } else {
                    ar[image.klass]--;
                }
            }
            if (j == 0) {
                determinations[i].value = -1;
                continue;
            }
            double detl = 0;
            double detr = 0;
            for (int k = 0; k < numKlasses; ++k) {
                if (a[k] != 0) {
                    final double dl = ((double) al[k]) / (a[k] * j);
                    final double dr = ((double) ar[k]) / (a[k] * (trainImages.length - j));
                    if (detl < dl) detl = dl;
                    if (detr < dr) detr = dr;
                }
            }
            detl *= trainImages.length;
            detr *= trainImages.length;
            determinations[i].value = Math.min(detl, detr);
        }
        Arrays.sort(determinations, new Comparator<IndexedDetermination>() {
            @Override
            public int compare(final IndexedDetermination o1, final IndexedDetermination o2) {
                if (o1.value > o2.value) return -1;
                if (o1.value < o2.value) return 1;
                return 0;
            }
        });
        int index = 0;
        IndexedDetermination result;
        while (index < determinations.length) {
            result = determinations[index++];
            result.train = divideImages(trainImages, result.inIndex, result.divisor);
            result.setIfDivided();
            if (result.divided) return result;
        }
        return null;
    }

    //разбиение по коррел€ции
    private static IndexedDetermination getBestDetermination3(
            final NeuroImage[] trainImages,
            final long[] a) {
        final IndexedDoubles[] correlations = getCorrelationPBC(null, trainImages);
        Arrays.sort(correlations, new Comparator<IndexedDoubles>() {
            @Override
            public int compare(final IndexedDoubles o1, final IndexedDoubles o2) {
                if (o1.value < o2.value) return 1;
                if (o1.value > o2.value) return -1;
                return 0;
            }
        });
        final IndexedDetermination result = new IndexedDetermination(0);
        for (final IndexedDoubles correlation : correlations) {
            result.inIndex = correlation.index;
            result.value = correlation.value;
            result.divisor = getAverageIn(trainImages, result.inIndex);
            result.train = divideImages(trainImages, result.inIndex, result.divisor);
            result.setIfDivided();
            if (result.divided) return result;
        }
        return null;
    }

    //разбиение по доходности (по ошибке)
    private static IndexedDetermination getBestDetermination(
            final NeuroImage[] trainImages,
            final long[] a) {
        if (trainImages == null) return null;
        if (trainImages.length < 2) return null;
        final IndexedDetermination[] determinations = getCorrelationPBP(trainImages);
        Arrays.sort(determinations, new Comparator<IndexedDetermination>() {
            @Override
            public int compare(final IndexedDetermination o1, final IndexedDetermination o2) {
                if (o1.value < o2.value) return 1;
                if (o1.value > o2.value) return -1;
                return 0;
            }
        });
//        for (final IndexedDetermination result : determinations) {
//            result.train = divideImages(trainImages, result.inIndex, result.divisor);
//            result.setIfDivided();
//        }
        for (final IndexedDetermination result : determinations) {
            result.train = divideImages(trainImages, result.inIndex, result.divisor);
            result.setIfDivided();
            if (result.divided) return result;
        }
        return null;
    }

    //разбиение по доходности (по ошибке) среднее
    private static IndexedDetermination getBestDetermination5(
            final NeuroImage[] trainImages,
            final long[] a) {
        if (trainImages == null) return null;
        if (trainImages.length < 2) return null;
        final IndexedDetermination[] determinations = getCorrelationPBPM(trainImages);
        Arrays.sort(determinations, new Comparator<IndexedDetermination>() {
            @Override
            public int compare(final IndexedDetermination o1, final IndexedDetermination o2) {
                if (o1.value < o2.value) return 1;
                if (o1.value > o2.value) return -1;
                return 0;
            }
        });
        for (final IndexedDetermination result : determinations) {
            result.train = divideImages(trainImages, result.inIndex, result.divisor);
            result.setIfDivided();
            if (result.divided) return result;
        }
        return null;
    }

    private static IndexedDetermination[] getCorrelationPBP(final NeuroImage[] trainImages) {
        final int numIn = trainImages[0].numIn;
        final int numOut = trainImages[0].numOut;
        final IndexedDetermination[] result = new IndexedDetermination[numIn];
        final double[] e = new double[numOut];
        for (final NeuroImage image : trainImages) {
            for (int i = 0; i < numOut; i++) {
                e[i] += image.out[i];
            }
        }
        for (int i = 0; i < numIn; ++i) {
            result[i] = new IndexedDetermination(i);
            sortImages(trainImages, i);
            final double[] el = new double[numOut];
            final double[] er = new double[numOut];
            for (int j = 0; j < numOut; j++) {
                el[j] = trainImages[0].out[j];
                er[j] = e[j] - trainImages[0].out[j];
            }
            //TODO: не пон€тно: ошибочный вариант работает лучше
            double maxProfit = -Double.MAX_VALUE;
//            double maxProfit = Double.MIN_VALUE;
            double maxDivisor = 0;
            int lKlass = -1;
            int rKlass = -1;
            for (int k = 1; k < trainImages.length; ++k) {
                if (k > trainImages.length * 0.25 && k < trainImages.length * 0.75) {
                    double profit = -Double.MAX_VALUE;
                    for (int l = 0; l < numOut; l++) {
                        for (int r = 0; r < numOut; r++) {
                            if (l != r) {
                                final double p = el[l] + er[r];
                                if (p >= profit) {
                                    profit = p;
                                    lKlass = l;
                                    rKlass = r;
                                }
                            }
                        }
                    }
                    if (profit >= maxProfit && trainImages[k - 1].in[i] != trainImages[k].in[i]) {
                        if (profit > maxProfit || k < trainImages.length / 2) {
                            maxProfit = profit;
                            maxDivisor = trainImages[k].in[i];
                        }
                    }
                }
                for (int j = 0; j < numOut; j++) {
                    el[j] += trainImages[k].out[j];
                    er[j] -= trainImages[k].out[j];
                }
            }
            result[i].value = -maxProfit;
            result[i].divisor = maxDivisor;
            result[i].lKlass = lKlass;
            result[i].rKlass = rKlass;
        }
        return result;
    }

    private static IndexedDetermination[] getCorrelationPBPM(final NeuroImage[] trainImages) {
        final int numIn = trainImages[0].numIn;
        final int numOut = trainImages[0].numOut;
        final IndexedDetermination[] result = new IndexedDetermination[numIn];
        final double[] e = new double[numOut];
        int ind = 0;
        for (final NeuroImage image : trainImages) {
            if (image.out.length != numOut) {
                System.out.println("error " + ind);
            }
            for (int i = 0; i < numOut; i++) {
                e[i] += image.out[i];
            }
            ++ind;
        }
        for (int i = 0; i < numIn; ++i) {
            result[i] = new IndexedDetermination(i);
            sortImages(trainImages, i);
            final double[] el = new double[numOut];
            final double[] er = new double[numOut];
            for (int j = 0; j < numOut; j++) {
                el[j] = trainImages[0].out[j];
                er[j] = e[j] - trainImages[0].out[j];
            }
            int lKlass = -1;
            int rKlass = -1;
            final int k = trainImages.length / 2;
            for (int k1 = 1; k1 <= k; k1++) {
                for (int j = 0; j < numOut; j++) {
                    el[j] += trainImages[k1].out[j];
                    er[j] -= trainImages[k1].out[j];
                }
            }
            double profit = -Double.MAX_VALUE;
            for (int l = 0; l < numOut; l++) {
                for (int r = 0; r < numOut; r++) {
                    if (l != r) {
                        final double p = el[l] + er[r];
                        if (p >= profit) {
                            profit = p;
                            lKlass = l;
                            rKlass = r;
                        }
                    }
                }
            }
            result[i].value = -profit;
            result[i].divisor = trainImages[k].in[i];
            result[i].lKlass = lKlass;
            result[i].rKlass = rKlass;
        }
        return result;
    }

    private static IndexedDetermination[] getCorrelationPBPM2(final NeuroImage[] trainImages) {
        final int numIn = trainImages[0].numIn;
        final int numOut = trainImages[0].numOut;
        final IndexedDetermination[] result = new IndexedDetermination[numIn];
        final double[] e = new double[numOut];
        int ind = 0;
        for (final NeuroImage image : trainImages) {
            if (image.out.length != numOut) {
                System.out.println("error " + ind);
            }
            for (int i = 0; i < numOut; i++) {
                e[i] += image.out[i];
            }
            ++ind;
        }
        for (int i = 0; i < numIn; ++i) {
            result[i] = new IndexedDetermination(i);
            final double[] el = new double[numOut];
            final double[] er = new double[numOut];
            for (int j = 0; j < numOut; j++) {
                el[j] = 0;
                er[j] = e[j];
            }
            final double divisor = getAverageIn(trainImages, i);
            for (int k = 0; k < trainImages.length; ++k) {
                if (trainImages[k].in[i] < divisor) {
                    for (int j = 0; j < numOut; j++) {
                        el[j] += trainImages[k].out[j];
                        er[j] -= trainImages[k].out[j];
                    }
                }
            }
            int lKlass = -1;
            int rKlass = -1;
            double profit = -Double.MAX_VALUE;
            for (int l = 0; l < numOut; l++) {
                for (int r = 0; r < numOut; r++) {
                    if (l != r) {
                        final double p = el[l] + er[r];
                        if (p >= profit) {
                            profit = p;
                            lKlass = l;
                            rKlass = r;
                        }
                    }
                }
            }
            result[i].value = -profit;
            result[i].divisor = divisor;
            result[i].lKlass = lKlass;
            result[i].rKlass = rKlass;
        }
        return result;
    }

    private static IndexedDoubles[] getCorrelationPBC(IndexedDoubles[] result,
                                                      final NeuroImage[] images) {
        final int numIn = images[0].in.length;
        final int numOut = images[0].out.length;
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
        for (int j = 0; j < numOut; j++) {
            double m = 0;
            for (final NeuroImage image : images) {
                final double y = image.out[j];
                m += y;
            }
            m /= images.length;
            double a = 0;
            for (final NeuroImage image : images) {
                final double y = image.out[j];
                a += (y - m) * (y - m);
            }
            a = StrictMath.sqrt(a);
            for (int i = 0; i < numIn; ++i) {
                double mi = 0;
                for (final NeuroImage image : images) {
                    final double x = image.in[i];
                    mi += x;
                }
                mi /= images.length;
                double b = 0;
                double c = 0;
                for (final NeuroImage image : images) {
                    final double x = image.in[i];
                    final double y = image.out[j];
                    b += (x - mi) * (y - m);
                    c += (x - mi) * (x - mi);
                }
                c = StrictMath.sqrt(c);
                if (a == 0 || c == 0) {
                    result[i].updateMaxValue(i, 0);
                } else {
                    result[i].updateMaxValue(i, Math.abs((b / a) / c));
                }
            }
        }
        return result;
    }

    private static int getNumNonZeroes(final long[] a) {
        int s = 0;
        for (final double v : a) {
            if (v > 0) ++s;
        }
        return s;
    }

    private static IndexedDetermination getRandomDetermination(
            final NeuroImage[] trainImages,
            final Rnd rnd) {
        final int numIn = trainImages[0].in.length;
        final IndexedDoubles[] order = new IndexedDoubles[numIn];
        for (int i = 0; i < order.length; ++i) {
            order[i] = new IndexedDoubles(i, -rnd.rnd());
        }
        Arrays.sort(order, new Comparator<IndexedDoubles>() {
            @Override
            public int compare(final IndexedDoubles o1, final IndexedDoubles o2) {
                if (o1.index < o2.index) return 1;
                if (o1.index > o2.index) return -1;
                return 0;
            }
        });
        int index = 0;
        final IndexedDetermination result = new IndexedDetermination(index);
        while (index < order.length && result.divided) {
            final int inIndex = order[index++].index;
            result.inIndex = inIndex;
            final double divisor = getAverageIn(trainImages, inIndex);
            result.divisor = divisor;
            result.train = divideImages(trainImages, inIndex, divisor);
            result.setIfDivided();
        }
        if (result.divided) {
            return result;
        } else {
            return null;
        }
    }

    private static double getAverageIn(final NeuroImage[] images,
                                       final int index) {
        if (images == null) {
            return 0;
        }
        if (images.length == 0) {
            return 0;
        }
        double m = 0;
        for (final NeuroImage image : images) {
            m += image.in[index];
        }
        return m / images.length;
    }

    private static ImageDivision divideImages(
            final NeuroImage[] images,
            final int bestInIndex,
            final double middleValue) {
        int numLess = 0;
        int numNotLess = 0;
        for (final NeuroImage image : images) {
            if (image.in[bestInIndex] < middleValue) {
                numLess++;
            } else {
                numNotLess++;
            }
        }
        if (numLess == 0 || numNotLess == 0) {
            return null;
        }
        final NeuroImage[] lessImages = new NeuroImage[numLess];
        final NeuroImage[] notLessImages = new NeuroImage[numNotLess];
        int indLess = 0;
        int indNotLess = 0;
        for (final NeuroImage image : images) {
            if (image.in[bestInIndex] < middleValue) {
                lessImages[indLess++] = image;
            } else {
                notLessImages[indNotLess++] = image;
            }
        }
        return new ImageDivision(lessImages, notLessImages);
    }

    private static void sortImages(
            final NeuroImage[] images,
            final int inIndex) {
        Arrays.sort(images, new Comparator<NeuroImage>() {
            @Override
            public int compare(final NeuroImage o1, final NeuroImage o2) {
                final double i1 = o1.in[inIndex];
                final double i2 = o2.in[inIndex];
                if (i1 < i2) return -1;
                if (i1 > i2) return 1;
                return 0;
            }
        });
    }

    static void simpleTest() throws IOException {
        // тест:
        final double[][] a = new double[][]{
                {1, 1},
                {2, 0},
                {3, 1},
                {4, 1},
                {5, 0},
                {6, 1}};
        final NetImage[] images = new NetImage[a.length];
        for (int i = 0; i < a.length; i++) {
            final double[] b = a[i];
            final int numIn = b.length - 1;
            final double[] in = new double[numIn];
            System.arraycopy(b, 0, in, 0, numIn);
            final int numOut = 1;
            final double[] out = new double[numOut];
            System.arraycopy(b, numIn, out, 0, numOut);
            images[i] = new NetImage(numIn, numOut, in, out);
        }
        final String path = "D:\\DATA";
        final PrintStream summary = new PrintStream(new FileOutputStream(new File(path + "\\simpleTest.summary")));
        errorTypeDefault = new NeuroComputationType();
        train(images, NeuroMapType.Partition, new Rnd517(), summary, path + "\\simpleTest.log").save(path + "\\simpleTest.net");
    }

    static void fcrTest() throws FileNotFoundException {
        // тест:
        final long startLoadingTime = System.nanoTime();
        final String path = "D:\\DATA";
        final String name = "actions-FR0024";
        final NeuroMapType type = NeuroMapType.Partition;
        final String version = type.getShortDescription() + ".2";
        final DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(path + "\\" + name))));
        final NetImage[] images = NeuroNetImageUtils.assignImages(2000000, dis);
        final PrintStream summary = new PrintStream(new FileOutputStream(new File(
                path + "\\" + name + ".FCRtest-" + version + ".summary")));
        errorTypeDefault = new NeuroComputationType();
        final long finishLoadingTime = System.nanoTime();
        summary.println("Loading time:" + t((finishLoadingTime - startLoadingTime) / 1000000000.0) + " s");
        final NeuroMap map = train(images, type, new Rnd517(), summary, path);
        final long finishTime = System.nanoTime();
        summary.println("Train time:" + t((finishTime - finishLoadingTime) / 1000000000.0) + " s");
        summary.flush();
        final DataOutputStream dosMap = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(path + "\\" + name + "-" + version + ".map")));
        map.save(dosMap);
    }

    static void neuroTest() throws IOException {
        final double[][] a = new double[][]{
                {1, 1, 0, -1, 0},
                {2, 3, -1, 0, 1},
                {3, 5, 0, -1, 0},
                {4, 7, -1, 0, 1},
                {5, 8, 0, -1, 0},
                {6, 6, -1, 0, 1},
                {7, 4, 0, -1, 0},
                {8, 2, -1, 0, 1}};
        final NeuroImage[] images = new NeuroImage[a.length];
        for (int i = 0; i < a.length; i++) {
            final double[] b = a[i];
            final int numIn = 2;
            final double[] in = new double[numIn];
            System.arraycopy(b, 0, in, 0, numIn);
            final int numOut = 2;
            final double[] out = new double[numOut];
            System.arraycopy(b, numIn, out, 0, numOut);
            final int numKlasses = 2;
            final int klass = (int) b[4];
            images[i] = new NeuroImage(numIn, numOut, numKlasses);
            images[i].in = in;
            images[i].out = out;
            images[i].klass = klass;
        }
        final String path = "D:\\DATA";
        final PrintStream summary = new PrintStream(new FileOutputStream(new File(path + "\\neuroTest.summary")));
        final NeuroComputationType computationType = new NeuroComputationType();
        computationType.setF();
        train(images, new Rnd517(), summary, computationType).save(path + "\\neuroTest.net");
    }

    static public void main(final String[] args) throws IOException {
        neuroTest();
//        simpleTest();
//        fcrTest();
    }
}
