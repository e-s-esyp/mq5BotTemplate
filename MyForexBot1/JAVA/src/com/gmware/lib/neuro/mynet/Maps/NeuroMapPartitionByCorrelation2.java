package com.gmware.lib.neuro.mynet.Maps;

import com.gmware.lib.neuro.mynet.IndexedDoubles;
import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.net2.Predictor;
import com.gmware.lib.neuro.mynet.NeuroCheckMaps;
import com.gmware.lib.neuro.mynet.NeuroMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * ѕриближение статистических данных путем разбиени€ пространства
 * входов. –азбиение производитс€ последовательно по какому-то параметру.
 * ¬ыбор параметра производитс€ по принципу максимальной коррел€ции с выходом.
 * <p/>
 * Created by Gauss on 25.02.2016.
 */
public class NeuroMapPartitionByCorrelation2 extends NeuroMap {
    public boolean noTest = false;
    public boolean convertFCR = false;
    double[] lastIn = null;
    double[] lastNetOut = null;
    public int numStructures = 0;
    public Structure[] structure = null;
    public int level = -1;

    double trnVar = Double.MAX_VALUE;
    double tstVar = Double.MAX_VALUE;

    double trnEA = Double.MAX_VALUE;
    double tstEA = Double.MAX_VALUE;
    double trnREA = Double.MAX_VALUE;
    double tstREA = Double.MAX_VALUE;
    double relativeErrorOfAverage = Double.MAX_VALUE;

    double trnE = Double.MAX_VALUE;
    double tstE = Double.MAX_VALUE;
    double trnRE = Double.MAX_VALUE;
    double tstRE = Double.MAX_VALUE;
    double relativeError = Double.MAX_VALUE;
    public IndexedDoubles[] errorByLevel = null;

    public NeuroMapPartitionByCorrelation2(DataInputStream dis, boolean b, boolean b1) {

    }

    public void setConvertFCR() {
        convertFCR = true;
        numStructures = 2;
        structure = new Structure[numStructures];
    }

    public void setErrors(final NetImage[] trainImages, final NetImage[] testImages) {
        setAverageOut(trainImages, testImages);
        trnVar = getVariance(trainImages);
        tstVar = getVariance(testImages);
        trnEA = getErrorOfAverage(averageOut, trainImages);
        tstEA = getErrorOfAverage(averageOut, testImages);
        trnREA = trnVar > 0.0 ? trnEA / trnVar : 1.0;
        tstREA = tstVar > 0.0 ? tstEA / tstVar : 1.0;
        relativeErrorOfAverage = Math.max(trnREA, tstREA);

        trnE = getErrorQuadratic(trainImages);
        tstE = getErrorQuadratic(testImages);
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

    private double getErrorOfAverageConverted(final NetImage[] images) {
        if (images == null) new Exception(" Empty set of images. ");
        if (images.length == 0) return 0;
        double e = 0;
        try {
            double d;
            final double[] averageOutFCR = getFCR2(averageOut);
            for (final NetImage image : images) {
                for (int i = 0; i < image.out.length; ++i) {
                    d = averageOutFCR[i] - image.out[i];
                    e += d * d;
                }
            }
        } catch (final Exception ex) {
            System.out.println("Error: getErrorOfAverageConverted");
        }
        return e / images.length;
    }

    private void setAverageOut(final NetImage[] trainImages, final NetImage[] testImages) {
        averageOut = new double[numOuts];
        double d;
        for (int i = 0; i < numOuts; ++i) {
            d = 0;
            for (final NetImage image : trainImages) {
                d += image.out[i];
            }
            for (final NetImage image : testImages) {
                d += image.out[i];
            }
            averageOut[i] = d / (trainImages.length + testImages.length);
        }
    }

    public static class Structure {
        public double correlation = 0;
        public int numOfImages = 0;
        public boolean divided = false;
        public int inInd = 0;
        public double divisor = 0;
        public double average = 0;
        public Structure lessPart = null;
        public Structure notLessPart = null;

        public Structure() {
        }

        Structure(final DataInputStream dis) throws IOException {
            numOfImages = dis.readInt();
            average = dis.readDouble();
            divided = dis.readBoolean();
            if (divided) {
                correlation = dis.readDouble();
                inInd = dis.readInt();
                divisor = dis.readDouble();
                lessPart = new Structure(dis);
                notLessPart = new Structure(dis);
            }
        }

        void save(final DataOutputStream dos) throws IOException {
            dos.writeInt(numOfImages);
            dos.writeDouble(average);
            dos.writeBoolean(divided);
            if (divided) {
                dos.writeDouble(correlation);
                dos.writeInt(inInd);
                dos.writeDouble(divisor);
                lessPart.save(dos);
                notLessPart.save(dos);
            }
        }

        public double propagate(final double[] x, final int l) {
//            if (divided && l > 0 && numOfImages >= 1000) { //TODO: вынести ограничение в тренер
            if (divided && l > 0) {
                if (x[inInd] < divisor) {
                    return lessPart.propagate(x, l - 1);
                } else {
                    return notLessPart.propagate(x, l - 1);
                }
            } else {
                return average;
            }
        }

        public String getPropagation(final double[] x, final int l) {
            if (divided && l > 0) {
                String s = "";
//                if (numOfImages < 100) {
//                    s += " " + format(average) + " NotUsed:";
//                }
                s += " x(" + format(average) + "){" + numOfImages + "}[" + inInd + "]=" + format(x[inInd]);
                if (x[inInd] < divisor) {
                    return s + "<" + format(divisor) + lessPart.getPropagation(x, l - 1);
                } else {
                    return s + ">=" + format(divisor) + notLessPart.getPropagation(x, l - 1);
                }
            } else {
                return " " + format(average);
            }

        }

        public void restrict(final int l) {
            if (divided && l > 0) {
                lessPart.restrict(l - 1);
                notLessPart.restrict(l - 1);
            } else {
                divided = false;
            }
        }

        public String toString(final int n) {
            if (divided) {
                return "{" + numOfImages + "} [" + inInd + "]:" + t(divisor) + ", corr = " + t(correlation) + ", average = " + t(average) + " \n" +
                        tab(n) + "lessPart    " + lessPart.toString(n + 1) +
                        tab(n) + "notLessPart " + notLessPart.toString(n + 1);
            } else {
                return "{" + numOfImages + "} [ " + t(average) + " ]\n";
            }
        }

        public String toString(final int n, final int num) {
            if (divided && numOfImages >= num) {
                return "{" + numOfImages + "} [" + inInd + "]:" + t(divisor) + ", corr = " + t(correlation) + ", average = " + t(average) + " \n" +
                        tab(n) + "lessPart    " + lessPart.toString(n + 1, num) +
                        tab(n) + "notLessPart " + notLessPart.toString(n + 1, num);
            } else {
                return "{" + numOfImages + "} [ " + t(average) + " ]\n";
            }
        }

        public void getIndexes(final int[] i, final int l) {
            if (divided) {
                if (i[inInd] == -1) {
                    i[inInd] = l;
                }
                lessPart.getIndexes(i, l + 1);
                notLessPart.getIndexes(i, l + 1);
            }
        }

        public void getIndexes2(final String[] i, final int l) {
            if (divided) {
                i[l] += " " + inInd + "(" + numOfImages + "|" + t(divisor) + ")";
                lessPart.getIndexes2(i, l + 1);
                notLessPart.getIndexes2(i, l + 1);
            }
        }

        public int getNumDivisions(final int numOfImagesLimit) {
            int sum = 0;
            if (divided) {
                if (numOfImages > numOfImagesLimit) ++sum;
                sum += lessPart.getNumDivisions(numOfImagesLimit);
                sum += notLessPart.getNumDivisions(numOfImagesLimit);
            }
            return sum;
        }

        public int setDivisions(final int numOfImagesLimit,
                                final NeuroCheckMaps.Division[] divisions,
                                int currentIndex) {
            if (divided) {
                if (numOfImages > numOfImagesLimit) {
                    divisions[currentIndex].divisor = divisor;
                    divisions[currentIndex].inInd = inInd;
                    divisions[currentIndex].numOfImages = numOfImages;
                    ++currentIndex;
                }
                currentIndex = lessPart.setDivisions(numOfImagesLimit, divisions, currentIndex);
                currentIndex = notLessPart.setDivisions(numOfImagesLimit, divisions, currentIndex);
            }
            return currentIndex;
        }

        String t(final double x) {
            return String.format("%01.5f", x);
        }

        String tab(final int n) {
            String s = "";
            for (int i = 0; i < n; ++i) {
                s += "\t";
            }
            return s;
        }

    }

    NeuroMapPartitionByCorrelation2() {
        setType();
    }

    public NeuroMapPartitionByCorrelation2(final int numIns, final int numOuts, final int l) {
        setType();
        this.numIns = numIns;
        this.numOuts = numOuts;
        numImages = l;
        numStructures = numOuts;
        structure = new Structure[numStructures];
    }

    @Override
    public final void setType() {
        type = NeuroMapType.PartitionByCorrelation2;
    }

    @Override
    public void save(final DataOutputStream dos) {
        try {
            dos.writeLong(type.getFormatCode());
            dos.writeBoolean(noTest);
            dos.writeBoolean(convertFCR);
            dos.writeInt(numIns);
            dos.writeInt(numOuts);
            dos.writeInt(numImages);
            dos.writeDouble(relativeErrorOfAverage);
            dos.writeDouble(trnRE);
            dos.writeDouble(tstRE);
            dos.writeDouble(relativeError);
            dos.writeDouble(rrError);
            dos.writeInt(level);
            dos.writeInt(errorByLevel.length);
            for (final IndexedDoubles a : errorByLevel) {
                a.save(dos);
            }
            dos.writeInt(numStructures);
            for (int i = 0; i < numStructures; ++i) {
                structure[i].save(dos);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static NeuroMapPartitionByCorrelation2 loadNewMap(final DataInputStream dis) throws IOException {
        final NeuroMapPartitionByCorrelation2 net = new NeuroMapPartitionByCorrelation2();
        net.loadBody(dis);
        return net;
    }

    @Override
    public void loadBody(final DataInputStream dis) throws IOException {
        noTest = dis.readBoolean();
        convertFCR = dis.readBoolean();
        numIns = dis.readInt();
        numOuts = dis.readInt();
        numImages = dis.readInt();
        relativeErrorOfAverage = dis.readDouble();
        trnRE = dis.readDouble();
        tstRE = dis.readDouble();
        relativeError = dis.readDouble();
        rrError = dis.readDouble();
        level = dis.readInt();
        final int size = dis.readInt();
        errorByLevel = new IndexedDoubles[size];
        for (int i = 0; i < size; ++i) {
            errorByLevel[i] = new IndexedDoubles(dis);
        }
        numStructures = dis.readInt();
        structure = new Structure[numStructures];
        for (int i = 0; i < numStructures; ++i) {
            structure[i] = new Structure(dis);
        }
    }

    @Override
    public double[] propagate(final double[] in) {
        if (lastNetOut == null) {
            lastNetOut = new double[numStructures];
        }
        final double[] out = propagate(structure, in, lastNetOut);
        if (convertFCR) {
            return getFCR2(out);
        }
        return out;
    }

    private double[] propagate(final Structure[] s, final double[] in, final double[] out) {
        for (int i = 0; i < numStructures; ++i) {
            out[i] = structure[i].propagate(in, level);
        }
        return out;
    }

    @Override
    public String[] getPropagation(final double[] in) {
        final String[] out = new String[structure.length];
        for (int i = 0; i < numStructures; ++i) {
            out[i] = structure[i].getPropagation(in, level);
        }
        return out;
    }

    public void restrict() {
        for (int i = 0; i < numStructures; ++i) {
            structure[i].restrict(level);
        }
    }

    public static double[] convertFCR(final double[] fcr) {
        if (fcr[0] > 0) {
            return new double[]{0, Double.NaN};
        } else {
            if (fcr[1] > 0) {
                return new double[]{1, 0};
            } else {
                if (fcr[2] > 0) {
                    return new double[]{1, 1};
                } else {
                    return null;
                }
            }
        }
    }

    public static double[] getFCR1(final double[] x) { //(1,0,0)
        return new double[]{1 - x[0], x[0] * (1 - x[1]), x[0] * x[1]};
    }

    public static double[] getFCR2(final double[] x) { //(0.5,-0.5,-0.5)
        return new double[]{0.5 - x[0], x[0] * (1 - x[1]) - 0.5, x[0] * x[1] - 0.5};
    }

    @Override
    public String toString() {
        String s = "Partition By Correlation 2, levelLimit = " + level + ", number of structures = " + numStructures + "\n";
        s += "FormatCode =" + type.getFormatCode() + "\n";
        for (int i = 0; i < numStructures; ++i) {
            s += "STRUCTURE[" + i + "]\n" + structure[i].toString(0, 10000);
        }

        s += "numOfImages = " + numImages + "\n";
        s += "\nnumber of structures = " + numStructures + "\n";
        if (convertFCR) s += "FCR-converted\n";
        if (noTest) s += "!!! noTest !!!\n";
        s += "number ins = " + numIns + "\n";
        s += "number of outs = " + numOuts + "\n\n";

        s += "trnVar  = " + trnVar + "\n";
        s += "tstVar  = " + tstVar + "\n";

        s += "trnEA   = " + trnEA + "\n";
        s += "tstEA   = " + tstEA + "\n";
        s += "trnREA  = " + trnREA + "\n";
        s += "tstREA  = " + tstREA + "\n";
        s += "relativeErrorOfAverage = " + relativeErrorOfAverage + "\n";

        s += "trnE   = " + trnE + "\n";
        s += "tstE   = " + tstE + "\n";
        s += "trnRE  = " + trnRE + "\n";
        s += "tstRE  = " + tstRE + "\n";
        s += "relativeError = " + relativeError + "\n";

        s += "rRError = " + rrError + "\n";

        s += "\nlevelLimit = " + level + "\n";
        s += "Errors By Level:\n";
        if (errorByLevel != null) {
            for (final IndexedDoubles a : errorByLevel) {
                s += a + "\n";
            }
        }
        return s;
    }

    public String getShortDescription() {
        String s = " RRE = " + format(rrError);
        s += " numOfImages = " + format(numImages, 7);
        s += " levelLimit = " + format(level, 2);
        if (noTest) {
            s += " noTest";
        } else {
            s += "       ";
        }
        if (convertFCR) {
            s += " FCR-converted";
        } else {
            s += "              ";
        }
        s += " structures = " + numStructures;
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
        s += " l=" + format(level, 2);
        return s;
    }

    public String getStructureDescription1() {
        String s = "";
        for (int i = 0; i < numStructures; ++i) {
            s += "STRUCTURE[" + i + "] numOfImages = " + structure[i].numOfImages + " {";
            final int[] inds = new int[numIns];
            for (int j = 0; j < inds.length; ++j) {
                inds[j] = -1;
            }
            structure[i].getIndexes(inds, 0);
            for (int j = 0; j < inds.length; ++j) {
                if (inds[j] != -1) {
                    s += j + "(" + inds[j] + ") ";
                }
            }
            s += " }  ";
        }
        return s;
    }

    public String getStructureDescription2() {
        String s = "";
        for (int i = 0; i < numStructures; ++i) {
            s += "STRUCTURE[" + i + "] {";
            final String[] inds = new String[numIns];
            for (int j = 0; j < inds.length; ++j) {
                inds[j] = "";
            }
            structure[i].getIndexes2(inds, 0);
            for (int j = 0; j < 4; ++j) {
                s += inds[j] + ((j < 3) ? ", " : "");
            }
            s += " }  ";
        }
        return s;
    }

    public int getNumDivisions(final int numOfImagesLimit) {
        int sum = 0;
        for (int i = 0; i < numStructures; ++i) {
            sum += structure[i].getNumDivisions(numOfImagesLimit);
        }
        return sum;
    }

    public void setDivisions(final int numOfImagesLimit,
                             final NeuroCheckMaps.Division[] divisions) {
        int currentIndex = 0;
        for (int i = 0; i < numStructures; ++i) {
            currentIndex = structure[i].setDivisions(numOfImagesLimit, divisions, currentIndex);
        }
    }

    static String format(final double x) {
        return String.format("%01." + 3 + "f", x);
    }

    static String format(long x, final int l) {
        String s = "";
        int i = l;
        while (x > 0) {
            s = x % 10 + s;
            x /= 10;
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
        lastIn = in;
        final double[] out = new double[numOuts];
        propagate(structure, in, out);
        if (convertFCR) {
            return getFCR2(out);
        }
        return out;
    }

    /**
     * –асширенна€ статистика нейросети.
     */
    public class NetStats implements Predictor.Stats {
        public int numImages = 0;
        public boolean noTest = false;
        public boolean convertFCR = false;
        public double[] lastNetOut = null;
        public int numStructures = 0;
        public Structure[] structure = null;
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
        netStats.convertFCR = convertFCR;
        netStats.lastNetOut = lastNetOut;
        netStats.numStructures = numStructures;
        netStats.structure = structure;
        netStats.level = level;

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
