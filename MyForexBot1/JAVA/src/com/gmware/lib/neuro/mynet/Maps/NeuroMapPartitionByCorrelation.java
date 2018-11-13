package com.gmware.lib.neuro.mynet.Maps;

import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.mynet.IndexedDoubles;
import com.gmware.lib.neuro.mynet.NeuroMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * ѕриближение статистических данных путем разбиени€ пространства
 * входов. –азбиение производитс€ последовательно по какому-то параметру.
 * ¬ыбор параметра производитс€ по принципу максимальной коррел€ции с выходом.
 * <p/>
 * Created by Gauss on 19.02.2016.
 */
public class NeuroMapPartitionByCorrelation extends NeuroMap {
    public boolean convertFCR = false;
    NeuroMap[] normalization = null;
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
        public Structure upPart = null;
        public Structure middlePart = null;
        public Structure downPart = null;

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
                upPart = new Structure(dis);
                middlePart = new Structure(dis);
                downPart = new Structure(dis);
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
                upPart.save(dos);
                middlePart.save(dos);
                downPart.save(dos);
            }
        }

        public double propagate(final double[] x, final int l) {
            if (divided && l > 0) {
                if (x[inInd] == divisor) {
                    return middlePart.propagate(x, l - 1);
                }
                if (x[inInd] < divisor) {
                    return downPart.propagate(x, l - 1);
                } else {
                    return upPart.propagate(x, l - 1);
                }
            } else {
                return average;
            }
        }

        public void restrict(final int l) {
            if (divided && l > 0) {
                middlePart.restrict(l - 1);
                downPart.restrict(l - 1);
                upPart.restrict(l - 1);
            } else {
                divided = false;
            }
        }

        public String toString(final int n) {
            if (divided) {
                return "{" + numOfImages + "} [" + inInd + "]:" + t(divisor) + ", corr = " + t(correlation) + ", average = " + t(average) + " \n" +
                        tab(n) + "upPart     " + upPart.toString(n + 1) +
                        tab(n) + "middlePart " + middlePart.toString(n + 1) +
                        tab(n) + "downPart   " + downPart.toString(n + 1);
            } else {
                return "{" + numOfImages + "} [ " + t(average) + " ]\n";
            }
        }

        public void getIndexes(final int[] i, final int l) {
            if (divided) {
                if (i[inInd] == -1) {
                    i[inInd] = l;
                }
                upPart.getIndexes(i, l + 1);
                middlePart.getIndexes(i, l + 1);
                downPart.getIndexes(i, l + 1);
            }
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

    NeuroMapPartitionByCorrelation() {
        setType();
    }

    public NeuroMapPartitionByCorrelation(final int numIns, final int numOuts, final int l) {
        setType();
        this.numIns = numIns;
        this.numOuts = numOuts;
        numImages = l;
        numStructures = numOuts;
        structure = new Structure[numStructures];
    }

    @Override
    public final void setType() {
        type = NeuroMapType.PartitionByCorrelation;
    }

    @Override
    public void save(final DataOutputStream dos) {
        try {
            dos.writeLong(type.getFormatCode());
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
            if (normalization == null) {
                dos.writeBoolean(false);
            } else {
                dos.writeBoolean(true);
                for (final NeuroMap net : normalization) {
                    net.save(dos);
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static NeuroMapPartitionByCorrelation loadNewMap(final DataInputStream dis) throws IOException {
        final NeuroMapPartitionByCorrelation net = new NeuroMapPartitionByCorrelation();
        net.loadBody(dis);
        return net;
    }

    @Override
    public void loadBody(final DataInputStream dis) throws IOException {
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
        if (dis.readBoolean()) {
            normalization = new NeuroMap[numIns];
            for (int i = 0; i < numIns; ++i) {
                normalization[i] = NeuroMap.load(dis);
            }
        }
    }

    @Override
    public double[] propagate(final double[] in) {
        if (lastNetOut == null) {
            lastNetOut = new double[numStructures];
        }
        final double[] out;
        if (normalization == null) {
            out = propagate(structure, in, lastNetOut);
        } else {
            final double[] inNorm = new double[in.length];
            for (int i = 0; i < in.length; ++i) {
                inNorm[i] = normalization[i].propagate(new double[]{in[i]})[0];
            }
            out = propagate(structure, inNorm, lastNetOut);
        }
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
        String s = "Partition By Correlation, levelLimit = " + level + ", number of structures = " + numStructures + "\n";
        s += "FormatCode =" + type.getFormatCode() + "\n";
        for (int i = 0; i < numStructures; ++i) {
            s += "STRUCTURE[" + i + "]\n" + structure[i].toString(0);
        }

        s += "numOfImages = " + numImages + "\n";
        s += "\nnumber of structures = " + numStructures + "\n";
        if (convertFCR) s += "FCR-converted\n";
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

    public String getStructureDescription() {
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

}
