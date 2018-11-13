package com.gmware.lib.neuro.mynet.Mappings;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.IndexedDoubles;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapPartitionByCorrelation;
import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.mynet.NeuroMapping;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.mynet.NeuroMap;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Получение NeuroMapPartitionByCorrelation по статистическим данным.
 * <p/>
 * Created by Gauss on 19.02.2016.
 */
public class NeuroMappingPartitionByCorrelation extends NeuroMapping {

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
        NeuroImagePBC[] up = null;
        NeuroImagePBC[] middle = null;
        NeuroImagePBC[] down = null;

        ImageDivision(final NeuroImagePBC[] u, final NeuroImagePBC[] m, final NeuroImagePBC[] d) {
            up = u;
            middle = m;
            down = d;
        }
    }

    static final int MAX_LEVEL = 12;

    public static NeuroMap train(final NetImage[] images,
                                 final NeuroMapType mapType,
                                 final Rnd rnd,
                                 final PrintStream log,
                                 final String netOutPath) {
        if (images == null) return null;
        if (images.length == 0) return null;
        final NeuroMapPartitionByCorrelation map =
                new NeuroMapPartitionByCorrelation(images[0].numIn, images[0].numOut, images.length);
        if (map.numOuts == 3) map.setConvertFCR();
        final int numTrainImages = ((images.length * 3) + 2) / 4;
        final int numTestImages = images.length - numTrainImages;
        final NetImage[] trainImages = new NetImage[numTrainImages];
        final NetImage[] testImages = new NetImage[numTestImages];
        selectTrainTestImages(images, trainImages, testImages, rnd);
        final NeuroImagePBC[][] selectedImages = convertImages(trainImages, map);
        for (int i = 0; i < map.numStructures; ++i) {
            map.structure[i] = trainStructure(MAX_LEVEL, selectedImages[i], log);
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

        return map;
    }

    private static NeuroImagePBC[][] convertImages(final NetImage[] images,
                                                   final NeuroMapPartitionByCorrelation map) {
        if (map.convertFCR && map.numOuts == 3) {
            final NeuroImagePBC[] convertedImages0 = new NeuroImagePBC[images.length];
            int num = 0;
            int i = 0;
            for (final NetImage image : images) {
                final double[] out = NeuroMapPartitionByCorrelation.convertFCR(image.out);
                convertedImages0[i++] = new NeuroImagePBC(image.in, out[0]);
                if (!Double.isNaN(out[1])) {
                    ++num;
                }
            }
            final NeuroImagePBC[] convertedImages1 = new NeuroImagePBC[num];
            i = 0;
            for (final NetImage image : images) {
                final double[] out = NeuroMapPartitionByCorrelation.convertFCR(image.out);
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

    private static NeuroMapPartitionByCorrelation.Structure trainStructure(
            final int recursionIndex,
            final NeuroImagePBC[] trainImages,
            final PrintStream log) {
        final NeuroMapPartitionByCorrelation.Structure structure = new NeuroMapPartitionByCorrelation.Structure();
        structure.numOfImages = trainImages.length;
        structure.average = getAverageOut(trainImages);
        if (recursionIndex > 0 && trainImages.length >= 3) {
            final int middleIndex = trainImages.length / 2;
            final IndexedDoubles[] correlations = getCorrelationPBC(null, trainImages);
            Arrays.sort(correlations, new Comparator<IndexedDoubles>() {
                @Override
                public int compare(final IndexedDoubles o1, final IndexedDoubles o2) {
                    if (o1.value < o2.value) return 1;
                    if (o1.value > o2.value) return -1;
                    return 0;
                }
            });
            ImageDivision imageDivision = null;
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
                middleValue = trainImages[middleIndex].in[bestInIndex];
                imageDivision = divideImages(trainImages, bestInIndex, middleValue);
                if (imageDivision == null) {
                    correlationsIndex++;
                } else {
                    break;
                }
            }
            if (imageDivision == null) {
                return structure;
            }
//            if (trainImages.length > 10000) {
//                System.out.println("levelLimit = " + recursionIndex +
//                        " size = " + trainImages.length +
//                        " corr" + correlations[correlationsIndex]);
//            }
            structure.divided = true;
            structure.correlation = correlations[correlationsIndex].value;
            structure.inInd = bestInIndex;
            structure.divisor = middleValue;
            structure.upPart = trainStructure(recursionIndex - 1, imageDivision.up, log);
            structure.middlePart = trainStructure(recursionIndex - 1, imageDivision.middle, log);
            structure.downPart = trainStructure(recursionIndex - 1, imageDivision.down, log);
            return structure;
        } else {
            return structure;
        }
    }

    private static double getAverageOut(final NeuroImagePBC[] trainImages) {
        if (trainImages.length == 0) return 0;
        double sum = 0;
        for (final NeuroImagePBC image : trainImages) {
            sum += image.out;
        }
        return sum / trainImages.length;
    }

    private static ImageDivision divideImages(final NeuroImagePBC[] trainImages,
                                              final int bestInIndex,
                                              final double middleValue) {
        int numLess = 0;
        int numEqual = 0;
        int numMore = 0;
        for (final NeuroImagePBC image : trainImages) {
            if (image.in[bestInIndex] < middleValue) {
                numLess++;
            } else {
                if (image.in[bestInIndex] > middleValue) {
                    numMore++;
                } else {
                    numEqual++;
                }
            }
        }
        if (numLess == 0 || numEqual == 0 || numMore == 0) {
            return null;
        }
        final NeuroImagePBC[] lessImages = new NeuroImagePBC[numLess];
        final NeuroImagePBC[] equalImages = new NeuroImagePBC[numEqual];
        final NeuroImagePBC[] moreImages = new NeuroImagePBC[numMore];
        int indLess = 0;
        int indEqual = 0;
        int indMore = 0;
        for (final NeuroImagePBC image : trainImages) {
            if (image.in[bestInIndex] < middleValue) {
                lessImages[indLess++] = image;
            } else {
                if (image.in[bestInIndex] > middleValue) {
                    moreImages[indMore++] = image;
                } else {
                    equalImages[indEqual++] = image;
                }
            }
        }
        return new ImageDivision(moreImages, equalImages, lessImages);
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
