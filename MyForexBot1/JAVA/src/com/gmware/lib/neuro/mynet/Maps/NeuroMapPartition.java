package com.gmware.lib.neuro.mynet.Maps;

import com.gmware.lib.neuro.mynet.*;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.net2.Predictor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Одна структура.
 * В структуре несколько выходов.
 * <p/>
 * Created by Gauss on 11.04.2016.
 * Копия NeuroMapPartitionByCorrelation3, но в структуре несколько выходов.
 * Приближение статистических данных путем разбиения пространства
 * входов. Разбиение производится последовательно по какому-то параметру.
 * Выбор параметра производится по принципу максимальной корреляции с выходом.
 * <p/>
 * Created by Gauss on 25.02.2016.
 */
public class NeuroMapPartition extends NeuroMap {
    public int numKlasses = -1;
    public boolean noTest = false;
    double[] lastIn = null;
    double[] lastNetOut = null;
    public int levelLimit = -1;
    public int imagesLimit = -1;
    public NeuroMapPartition continuous = null;

    public double trnVar = Double.MAX_VALUE;
    public double tstVar = Double.MAX_VALUE;

    public double trnEA = Double.MAX_VALUE;
    public double tstEA = Double.MAX_VALUE;
    public double trnREA = Double.MAX_VALUE;
    public double tstREA = Double.MAX_VALUE;
    public double relativeErrorOfAverage = Double.MAX_VALUE;

    public double trnE = Double.MAX_VALUE;
    public double tstE = Double.MAX_VALUE;
    public double trnRE = Double.MAX_VALUE;
    public double tstRE = Double.MAX_VALUE;
    public double relativeError = Double.MAX_VALUE;
    public IndexedDoubles[] errorByLevel = null;
    public IndexedDoubles[] errorByImages = null;

    public NeuroComputationType computationType = null; //quadratic error

    public void setErrors(final NetImage[] trainImages, final NetImage[] testImages) {
        averageOut = getAverageOut(trainImages);
        trnVar = getVariance(trainImages);
        tstVar = getVariance(testImages);
        trnEA = getError(structure, trainImages, 0);
        tstEA = getError(structure, testImages, 0);
        trnREA = trnVar > 0.0 ? trnEA / trnVar : 1.0;
        tstREA = tstVar > 0.0 ? tstEA / tstVar : 1.0;
        relativeErrorOfAverage = Math.max(trnREA, tstREA);
        trnE = getError(structure, trainImages, levelLimit);
        tstE = getError(structure, trainImages, levelLimit);
        trnRE = trnVar > 0.0 ? trnE / trnVar : 1.0;
        tstRE = tstVar > 0.0 ? tstE / tstVar : 1.0;
        relativeError = Math.max(trnRE, tstRE);
        if (relativeErrorOfAverage == 0) {
            if (relativeError == 0) {
                rrError = 0;
            } else {
                rrError = 10;
            }
        } else {
            rrError = relativeError / relativeErrorOfAverage;
        }
    }

    public double[] getAverageOut(final NetImage[] images) {
        if (images == null) {
            return null;
        }
        if (images.length == 0) return null;
        final double[] m = new double[numOuts];
        for (int i = 0; i < numOuts; ++i) {
            for (final NetImage image : images) {
                m[i] += image.out[i];
            }
            m[i] /= images.length;
        }
        if (computationType.isContinuousOut) {
            // возвращает вероятность класса
            return m;
        } else {
            // возвращает наилучший класс
            double max = Double.MIN_VALUE;
            int maxInd = 0;
            for (int i = 0; i < numOuts; ++i) {
                if (m[i] > max) {
                    max = m[i];
                    maxInd = i;
                }
                m[i] = 0;
            }
            m[maxInd] = 1;
            return m;
        }
    }

    public double[] getKlassesProbability(final NeuroImage[] images) {
        if (images == null) {
            return null;
        }
        if (images.length == 0) return null;
        final int numKlasses = images[0].numKlasses;
        final double[] m = new double[numKlasses];
        for (final NeuroImage image : images) {
            m[image.klass]++;
        }
        for (int i = 0; i < numKlasses; i++) {
            m[i] /= images.length;
        }
        // возвращает вероятность класса
        return m;
    }

    public double getError(
            final NeuroBranch structure,
            final NetImage[] images,
            final int level) {
        if (images == null) new Exception(" Empty set of images. ");
        if (images.length == 0) return 0;
        double e = 0;
        try {
            double d;
            if (computationType.isWeighted) {
                if (computationType.isQuadraticError)
                    //среднеквадратичная ошибка
                    for (final NetImage image : images) {
                        final double[] out = structure.propagate(image.in, level);
                        for (int i = 0; i < numOuts; i++) {
                            d = out[i] - image.out[i];
                            e += computationType.weight[i] * d * d;
                        }
                    }
                if (computationType.isAbsError)
                    //abs-ошибка
                    for (final NetImage image : images) {
                        final double[] out = structure.propagate(image.in, level);
                        for (int i = 0; i < numOuts; i++) {
                            e += computationType.weight[i] * Math.abs(out[i] - image.out[i]);
                        }
                    }
                if (computationType.isComplexError)
                    //коплексная ошибка
                    for (final NetImage image : images) {
                        final double[] out = structure.propagate(image.in, level);
                        for (int i = 0; i < numOuts; i++) {


                            e += Math.abs(out[i] - image.out[i]);
                        }
                    }
            } else {
                if (computationType.isQuadraticError)
                    //среднеквадратичная ошибка
                    for (final NetImage image : images) {
                        final double[] out = structure.propagate(image.in, level);
                        for (int i = 0; i < numOuts; i++) {
                            d = out[i] - image.out[i];
                            e += d * d;
                        }
                    }
                if (computationType.isAbsError)
                    //abs-ошибка
                    for (final NetImage image : images) {
                        final double[] out = structure.propagate(image.in, level);
                        for (int i = 0; i < numOuts; i++) {
                            e += Math.abs(out[i] - image.out[i]);
                        }
                    }
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

    //------------------------------------------------------------

    public NeuroImage[] convertImages(final NetImage[] images) {
        final NeuroImage[] result = new NeuroImage[images.length];
        if (computationType.isFCR) {
            for (int i = 0; i < images.length; ++i) {
                final NetImage image1 = images[i];
                final NeuroImage image2 = new NeuroImage();
                image2.numIn = image1.getNumIn();
                image2.in = image1.in;
                image2.numOut = image1.numOut;
                image2.out = image1.out;
                image2.numKlasses = image1.getNumOut();
                image2.klass = image1.out.length;
                for (int j = 0; j < image1.out.length; ++j) {
                    if (image1.out[j] > 0) {
                        image2.klass = j;
                        break;
                    }
                }
                result[i] = image2;
            }
            return result;
        } else {
            int maxKlass = 0;
            for (int i = 0; i < images.length; ++i) {
                final NetImage image1 = images[i];
                final NeuroImage image2 = new NeuroImage();
                image2.numIn = image1.numIn;
                image2.in = image1.in;
                image2.numOut = image1.numOut - 1;
                image2.out = new double[image2.numOut];
                System.arraycopy(image1.out, 1, image2.out, 0, image2.numOut);
                image2.klass = (int) image1.out[0];
                if (maxKlass < image2.klass) maxKlass = image2.klass;
                result[i] = image2;
            }
            for (final NeuroImage image : result) {
                image.numKlasses = maxKlass + 1;
            }
            return result;
        }
    }

    public void setErrors(final NeuroImage[] trainImages, final NeuroImage[] testImages) {
        averageOut = getAverageOut(getKlassesSize(trainImages), trainImages.length);
        trnVar = 1;
        tstVar = 1;
        trnEA = NeuroError.errors[computationType.errorTypeNum].get(structure, trainImages, 0);
        tstEA = NeuroError.errors[computationType.errorTypeNum].get(structure, testImages, 0);
        trnREA = trnVar > 0.0 ? trnEA / trnVar : 1.0;
        tstREA = tstVar > 0.0 ? tstEA / tstVar : 1.0;
        relativeErrorOfAverage = Math.max(trnREA, tstREA);
        trnE = NeuroError.errors[computationType.errorTypeNum].get(structure, trainImages, levelLimit, imagesLimit);
        tstE = NeuroError.errors[computationType.errorTypeNum].get(structure, testImages, levelLimit, imagesLimit);
        trnRE = trnVar > 0.0 ? trnE / trnVar : 1.0;
        tstRE = tstVar > 0.0 ? tstE / tstVar : 1.0;
        relativeError = Math.max(trnRE, tstRE);
        if (relativeErrorOfAverage == 0) {
            if (relativeError == 0) {
                rrError = 0;
            } else {
                rrError = 10;
            }
        } else {
            rrError = relativeError / relativeErrorOfAverage;
        }
    }


    public void setErrors(final NeuroImage[] trainImages) {
        averageOut = getAverageOut(getKlassesSize(trainImages), trainImages.length);
        trnVar = 1;
        tstVar = 0;
        trnEA = NeuroError.errors[computationType.errorTypeNum].get(structure, trainImages, 0);
        tstEA = 0;
        trnREA = trnVar > 0.0 ? trnEA / trnVar : 1.0;
        tstREA = 0;
        relativeErrorOfAverage = trnREA;
        trnE = NeuroError.errors[computationType.errorTypeNum].get(structure, trainImages, levelLimit, imagesLimit);
        tstE = 0;
        trnRE = trnVar > 0.0 ? trnE / trnVar : 1.0;
        tstRE = 0;
        relativeError = trnRE;
        if (relativeErrorOfAverage == 0) {
            if (relativeError == 0) {
                rrError = 0;
            } else {
                rrError = 10;
            }
        } else {
            rrError = relativeError / relativeErrorOfAverage;
        }
    }

    public boolean isBetterThenAverage(
            final NeuroBranch structure,
            final NeuroImage[] trainImages,
            final NeuroImage[] testImages,
            final int level) {
        structure.divided = false;
        final double trnEAs = NeuroError.errors[computationType.errorTypeNum].get(structure, trainImages, level);
        final double tstEAs = NeuroError.errors[computationType.errorTypeNum].get(structure, testImages, level);
        final double relativeErrorOfAverages = Math.max(trnEAs, tstEAs);
        structure.error = relativeErrorOfAverages;
        structure.divided = true;
        final double trnEs = NeuroError.errors[computationType.errorTypeNum].get(structure, trainImages, level);
        final double tstEs = NeuroError.errors[computationType.errorTypeNum].get(structure, testImages, level);
        final double relativeErrors = Math.max(trnEs, tstEs);
        return relativeErrors < relativeErrorOfAverages;
    }

    public boolean isBetterThenAverage(
            final NeuroBranch structure,
            final NeuroImage[] trainImages,
            final int level) {
        structure.divided = false;
        structure.error = NeuroError.errors[computationType.errorTypeNum].get(structure, trainImages, level);
        structure.divided = true;
        final double trnEs = NeuroError.errors[computationType.errorTypeNum].get(structure, trainImages, level);
        return trnEs < structure.error;
    }

    public void setBestKlass(
            final NeuroBranch structure,
            final NeuroImage[] trainImages,
            final NeuroImage[] testImages) {
        structure.divided = false;
        double min = Double.MAX_VALUE;
        int bestKlass = -1;
        for (int i = 0; i < numKlasses; ++i) {
            if (structure.average[i] == 0) continue; //ошибку считаем только по непустым классам
            structure.out.klass = i;
            final double trnEAs = NeuroError.errors[computationType.errorTypeNum].get(structure, trainImages, 0);
            final double tstEAs = NeuroError.errors[computationType.errorTypeNum].get(structure, testImages, 0);
            final double errorOfAverages = Math.max(trnEAs, tstEAs);
            if (min >= errorOfAverages) {
                min = errorOfAverages;
                bestKlass = i;
            }
        }
        structure.out.klass = bestKlass;
    }

    public void setBestKlass(
            final NeuroBranch structure,
            final NeuroImage[] trainImages) {
        structure.divided = false;
        double min = Double.MAX_VALUE;
        int bestKlass = -1;
        for (int i = 0; i < numKlasses; ++i) {
            if (structure.average[i] == 0) continue; //ошибку считаем только по непустым классам
            structure.out.klass = i;
            final double trnEAs = NeuroError.errors[computationType.errorTypeNum].get(structure, trainImages, 0);
            if (min >= trnEAs) {
                min = trnEAs;
                bestKlass = i;
            }
        }
        structure.out.klass = bestKlass;
    }

    public long[] getKlassesSize(final NeuroImage[] images) {
        if (images == null) return null;
        if (images.length == 0) return null;
        final long[] a = new long[images[0].numKlasses];
        for (final NeuroImage image : images) {
            a[image.klass]++;
        }
        return a;
    }

    public double[] getAverageOut(final long[] a, final long total) {
        if (a == null) return null;
        final double[] av = new double[a.length];
        for (int i = 0; i < a.length; ++i) {
            av[i] = ((double) a[i]) / total;
        }
        return av;
    }

    public double[] getProfits(final NeuroImage[] images) {
        if (images == null) return null;
        if (images.length == 0) return null;
        final double[] a = new double[images[0].numKlasses];
        for (final NeuroImage image : images) {
            for (int i = 0; i < a.length; ++i) {
                a[i] += image.out[i];
            }
        }
        return a;
    }

    //------------------------------------------------------------
    public NeuroMapPartition() {
        setType();
    }

    public NeuroMapPartition(final int numIns, final int numOuts, final int l, final NeuroComputationType errorType) {
        setType();
        this.numIns = numIns;
        this.numOuts = numOuts;
        numImages = l;
        computationType = errorType;
        numKlasses = numOuts;
    }

    @Override
    public final void setType() {
        type = NeuroMapType.Partition;
    }

    public NeuroBranch structure = null;

    @Override
    public void save(final DataOutputStream dos) {
        try {
            dos.writeLong(type.getFormatCode());
            dos.writeLong(tag);
            dos.writeDouble(rrError);
            dos.writeBoolean(noTest);
            computationType.write(dos);
            dos.writeInt(levelLimit);
            dos.writeInt(imagesLimit);
            dos.writeDouble(trnVar);
            dos.writeDouble(tstVar);
            dos.writeDouble(trnEA);
            dos.writeDouble(tstEA);
            dos.writeDouble(trnREA);
            dos.writeDouble(tstREA);
            dos.writeDouble(relativeErrorOfAverage);
            dos.writeDouble(trnE);
            dos.writeDouble(tstE);
            dos.writeDouble(trnRE);
            dos.writeDouble(tstRE);
            dos.writeDouble(relativeError);
            if (averageOut == null) {
                dos.writeInt(0);
            } else {
                dos.writeInt(averageOut.length);
                for (final double a : averageOut) {
                    dos.writeDouble(a);
                }
            }
            if (errorByLevel == null) {
                dos.writeInt(0);
            } else {
                dos.writeInt(errorByLevel.length);
                for (final IndexedDoubles a : errorByLevel) {
                    a.save(dos);
                }
            }
            if (errorByImages == null) {
                dos.writeInt(0);
            } else {
                dos.writeInt(errorByImages.length);
                for (final IndexedDoubles a : errorByImages) {
                    a.save(dos);
                }
            }
            dos.writeInt(numImages);
            dos.writeInt(numIns);
            dos.writeInt(numOuts);
            dos.writeInt(numKlasses);
            structure.save(dos);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static NeuroMapPartition loadNewMap(final DataInputStream dis) throws IOException {
        final NeuroMapPartition net = new NeuroMapPartition();
        net.loadBody(dis);
        return net;
    }

    @Override
    public void loadBody(final DataInputStream dis) throws IOException {
        tag = dis.readLong();
        rrError = dis.readDouble();
        noTest = dis.readBoolean();
        computationType = new NeuroComputationType(dis);
        levelLimit = dis.readInt();
        imagesLimit = dis.readInt();
        trnVar = dis.readDouble();
        tstVar = dis.readDouble();
        trnEA = dis.readDouble();
        tstEA = dis.readDouble();
        trnREA = dis.readDouble();
        tstREA = dis.readDouble();
        relativeErrorOfAverage = dis.readDouble();
        trnE = dis.readDouble();
        tstE = dis.readDouble();
        trnRE = dis.readDouble();
        tstRE = dis.readDouble();
        relativeError = dis.readDouble();
        final int sizeA = dis.readInt();
        if (sizeA > 0) {
            averageOut = new double[sizeA];
            for (int i = 0; i < sizeA; ++i) {
                averageOut[i] = dis.readDouble();
            }
        } else {
            averageOut = null;
        }
        final int sizeL = dis.readInt();
        if (sizeL > 0) {
            errorByLevel = new IndexedDoubles[sizeL];
            for (int i = 0; i < sizeL; ++i) {
                errorByLevel[i] = new IndexedDoubles(dis);
            }
        } else {
            errorByLevel = null;
        }
        final int sizeI = dis.readInt();
        if (sizeI > 0) {
            errorByImages = new IndexedDoubles[sizeI];
            for (int i = 0; i < sizeI; ++i) {
                errorByImages[i] = new IndexedDoubles(dis);
            }
        } else {
            errorByImages = null;
        }
        numImages = dis.readInt();
        numIns = dis.readInt();
        numOuts = dis.readInt();
        numKlasses = dis.readInt();
        structure = new NeuroBranch(dis, numKlasses);
    }

    @Override
    public double[] propagate(final double[] in) {
        if (lastNetOut == null) {
            lastNetOut = new double[numOuts];
        }
        final double[] out;
        if (imagesLimit > 0) {
            out = structure.propagate(in, imagesLimit, levelLimit);
        } else {
            out = structure.propagate(in, levelLimit);
        }
        System.arraycopy(out, 0, lastNetOut, 0, out.length);
        return lastNetOut;
    }

    public NeuroBranch propagateStructure(final double[] in) {
        if (lastNetOut == null) {
            lastNetOut = new double[numOuts];
        }
        final double[] out;
        if (imagesLimit > 0) {
            return structure.propagateStructure(in, imagesLimit, levelLimit);
        } else {
            return structure.propagateStructure(in, levelLimit);
        }
    }

    public NeuroOut getOut(final double[] in) {
        return structure.getOut(in);
    }

    @Override
    public String[] getPropagation(final double[] in) {
        return new String[]{structure.getPropagation(in, levelLimit)};
    }

    public void restrictByLevel() {
        structure.restrictByLevel(levelLimit);
    }

    public void restrictByNumImages() {

    }

    @Override
    public String toString() {
        String s = "FormatCode = " + type.getFormatCode() + "\n";
        s += "type      = " + type.getDescription() + " \n";
        s += "tag       = " + tag + "\n";
        s += "numUsedIns    = " + numIns + "\n";
        s += "numOuts   = " + numOuts + "\n";
        s += "numImages = " + numImages + "\n";
        s += "rrError   = " + rrError + "\n";
        s += "noTest    = " + noTest + "\n";
        s += "computationType     = " + computationType + "\n";
        s += "levelLimit    = " + levelLimit + "\n";
        s += "imagesLimit   = " + imagesLimit + "\n";
        s += "trnVar = " + trnVar + "\n";
        s += "tstVar = " + tstVar + "\n";
        s += "trnEA  = " + trnEA + "\n";
        s += "tstEA  = " + tstEA + "\n";
        s += "trnREA = " + trnREA + "\n";
        s += "tstREA = " + tstREA + "\n";
        s += "relativeErrorOfAverage = " + relativeErrorOfAverage + "\n";
        s += "trnE   = " + trnE + "\n";
        s += "tstE   = " + tstE + "\n";
        s += "trnRE  = " + trnRE + "\n";
        s += "tstRE  = " + tstRE + "\n";
        s += "relativeError          = " + relativeError + "\n";
        s += "- end of parameters -\n";
        s += "Average Out:\n";
        if (averageOut != null) {
            for (int i = 0; i < averageOut.length; ++i) {
                s += "[" + i + "] = " + averageOut[i] + "\n";
            }
        }
        s += "- end of list -\n";
        IndexedDoubles.formatOfDouble = 12;
        s += "Errors By Level:\n";
        if (errorByLevel != null) {
            for (final IndexedDoubles a : errorByLevel) {
                if (a != null) s += a + "\n";
            }
        }
        s += "- end of list -\n";
        s += "Errors By Images:\n";
        if (errorByImages != null) {
            for (final IndexedDoubles a : errorByImages) {
                if (a != null) s += a + "\n";
            }
        }
        s += "- end of list -\n";
        s += "STRUCTURE\n" + structure.toString(0);
        s += "- end of structure -\n";
        return s;
    }

    public String getShortDescription() {
        String s = " RRE = " + format(rrError);
        s += " numOfImages = " + format(numImages, 7);
        s += " levelLimit = " + format(levelLimit, 2);
        if (noTest) {
            s += " noTest";
        } else {
            s += "       ";
        }
        s += " ins = " + format(numIns, 3);
        s += " outs = " + numOuts;
        s += " REOA = " + format(relativeErrorOfAverage);
        s += " trnRE  = " + format(trnRE);
        s += " tstRE  = " + format(tstRE);
        s += " RE = " + format(relativeError);
        s += " Errors By Level: < ";
        if (errorByLevel != null) {
            for (final IndexedDoubles a : errorByLevel) {
                s += a + "  ";
            }
        }
        s += " >";
        return s;
    }

    public String getSmartDescription() {
        String s = " RRE=" + format(rrError);
        s += " i=" + format(numImages, 7);
        s += " l=" + format(levelLimit, 2);
        return s;
    }

    public String getStructureDescription1() {
        String s = "";
        s += "STRUCTURE numOfImages = " + structure.out.numImages + " {";
        final int[] inds = new int[numIns];
        for (int j = 0; j < inds.length; ++j) {
            inds[j] = -1;
        }
        structure.getIndexes(inds, 0);
        for (int j = 0; j < inds.length; ++j) {
            if (inds[j] != -1) {
                s += j + "(" + inds[j] + ") ";
            }
        }
        s += " }  ";
        return s;
    }

    public String getStructureDescription2() {
        String s = "";
        s += "STRUCTURE {";
        final String[] inds = new String[numIns];
        for (int j = 0; j < inds.length; ++j) {
            inds[j] = "";
        }
        structure.getIndexes2(inds, 0);
        for (int j = 0; j < 4; ++j) {
            s += inds[j] + ((j < 3) ? ", " : "");
        }
        s += " }  ";
        return s;
    }

    public int getNumDivisions(final int numOfImagesLimit) {
        return structure.getNumDivisions(numOfImagesLimit);
    }

    public void setDivisions(final int numOfImagesLimit,
                             final NeuroCheckMaps.Division[] divisions) {
        structure.setDivisions(numOfImagesLimit, divisions, 0);
    }

    static String format(final double x) {
        return String.format("%10." + 3 + "f", x);
    }

    static String format(final double[] x) {
        String s = "";
        for (final double v : x) {
            s += String.format(" %01." + 3 + "f", v);
        }
        return s;
    }

    static String format(long x, final int l) {
        String s = "";
        int i = l;
        if (x > 0) {
            while (x > 0) {
                s = x % 10 + s;
                x /= 10;
                i--;
            }
        } else {
            s = "0";
            i--;
        }
        while (i > 0) {
            s += " ";
            i--;
        }
        return s;
    }

//--------------------------------------------------------

    @Override
    public double[] propagateMulty(final double[] in) {
        final double[] out0;
        if (imagesLimit > 0) {
            out0 = structure.propagate(in, imagesLimit, levelLimit);
        } else {
            out0 = structure.propagate(in, levelLimit);
        }
        final double[] out = new double[numOuts];
        System.arraycopy(out0, 0, out, 0, out.length);
        return out;
    }

    /**
     * Расширенная статистика нейросети.
     */
    public class NetStats implements Predictor.Stats {
        public int numImages = 0;
        public boolean noTest = false;
        public double[] lastNetOut = null;
        public int numStructures = 0;
        public NeuroBranch structure = null;
        public int level = -1;
        public double trnVar = Double.MAX_VALUE;
        public double tstVar = Double.MAX_VALUE;

        public double trnEA = Double.MAX_VALUE;
        public double tstEA = Double.MAX_VALUE;
        public double trnREA = Double.MAX_VALUE;
        public double tstREA = Double.MAX_VALUE;
        public double relativeErrorOfAverage = Double.MAX_VALUE;

        public double trnE = Double.MAX_VALUE;
        public double tstE = Double.MAX_VALUE;
        public double trnRE = Double.MAX_VALUE;
        public double tstRE = Double.MAX_VALUE;
        public double relativeError = Double.MAX_VALUE;
        public double rrError = Double.MAX_VALUE;
        public IndexedDoubles[] errorByLevel = null;
        public String[] lastPropagation = null;

        @Override
        public int getNumImages() {
            return numImages;
        }
    }

    @Override
    public Predictor.Stats getNetStats() {
        final NetStats netStats = new NetStats();

        netStats.numImages = numImages;
        netStats.noTest = noTest;
        netStats.lastNetOut = lastNetOut;
        netStats.structure = structure;
        netStats.level = levelLimit;

        netStats.trnVar = trnVar;
        netStats.tstVar = tstVar;

        netStats.trnEA = trnEA;
        netStats.tstEA = tstEA;
        netStats.trnREA = trnREA;
        netStats.tstREA = tstREA;
        netStats.relativeErrorOfAverage = relativeErrorOfAverage;

        netStats.trnE = trnE;
        netStats.tstE = tstE;
        netStats.trnRE = trnRE;
        netStats.tstRE = tstRE;
        netStats.relativeError = relativeError;
        netStats.errorByLevel = errorByLevel;
        netStats.rrError = rrError;

        netStats.lastPropagation = getPropagation(lastIn);
        return netStats;
    }

}
