package com.gmware.lib.neuro.mynet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Gauss on 13.06.2016.
 */

public class NeuroBranch {
    public int numKlasses = 0;

    public double[] average = null;
    public double error = 0;
    //-------------------------
    public boolean divided = false;
    public int inInd = 0;//индекс, по которому делим
    public double divisor = 0;//величина, по которой делим
    public NeuroBranch lessPart = null;
    public NeuroBranch notLessPart = null;
    public double determination = 0;
    public NeuroOut out = null;

    public NeuroBranch(final int numKlasses) {
        this.numKlasses = numKlasses;
        out = new NeuroOut(numKlasses);
    }

    public NeuroBranch(final DataInputStream dis, final int numKlasses) throws IOException {
        this.numKlasses = numKlasses;
        divided = dis.readBoolean();
        if (divided) {
            determination = dis.readDouble();
            inInd = dis.readInt();
            divisor = dis.readDouble();
            lessPart = new NeuroBranch(dis, numKlasses);
            notLessPart = new NeuroBranch(dis, numKlasses);
        }
        if (dis.readBoolean()) {
            out = new NeuroOut(dis, numKlasses);
        }
        final int numOuts = dis.readInt();
        average = new double[numOuts];
        for (int i = 0; i < numOuts; ++i) {
            average[i] = dis.readDouble();
        }
        error = dis.readDouble();
    }

    public void save(final DataOutputStream dos) throws IOException {
        dos.writeBoolean(divided);
        if (divided) {
            dos.writeDouble(determination);
            dos.writeInt(inInd);
            dos.writeDouble(divisor);
            lessPart.save(dos);
            notLessPart.save(dos);
        }
        if (out != null) {
            dos.writeBoolean(true);
            out.save(dos);
        } else {
            dos.writeBoolean(false);
        }
        dos.writeInt(average.length);
        for (final double anAverage : average) {
            dos.writeDouble(anAverage);
        }
        dos.writeDouble(error);
    }

    public NeuroOut getOut(final double[] x) {
        if (divided) {
            if (x[inInd] < divisor) {
                return lessPart.getOut(x);
            } else {
                return notLessPart.getOut(x);
            }
        } else {
            return out;
        }
    }

    public double[] propagate(final double[] x, final int l) {
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

    public double[] propagate(final double[] x, final int l, final int imagesLimit) {
        if (divided && l > 0 && out.numImages >= imagesLimit) {
            if (x[inInd] < divisor) {
                return lessPart.propagate(x, l - 1, imagesLimit);
            } else {
                return notLessPart.propagate(x, l - 1, imagesLimit);
            }
        } else {
            return average;
        }
    }

    public NeuroBranch propagateStructure(final double[] x, final int l) {
        if (divided && l > 0) {
            if (x[inInd] < divisor) {
                return lessPart.propagateStructure(x, l - 1);
            } else {
                return notLessPart.propagateStructure(x, l - 1);
            }
        } else {
            return this;
        }
    }

    public NeuroBranch propagateStructure(final double[] x, final int l, final int imagesLimit) {
        if (divided && l > 0 && out.numImages >= imagesLimit) {
            if (x[inInd] < divisor) {
                return lessPart.propagateStructure(x, l - 1, imagesLimit);
            } else {
                return notLessPart.propagateStructure(x, l - 1, imagesLimit);
            }
        } else {
            return this;
        }
    }

    public String getPropagation(final double[] x, final int l) {
        if (divided && l > 0) {
            String s = "";
            s += " x(" + out.klass + "|" + format(average) + "){" + format(out.numImages, 6) + "}["
                    + format(inInd, 2) + "]=" + format(x[inInd]);
            if (x[inInd] < divisor) {
                return s + "< " + format(divisor) + lessPart.getPropagation(x, l - 1);
            } else {
                return s + ">=" + format(divisor) + notLessPart.getPropagation(x, l - 1);
            }
        } else {
            return " " + format(average);
        }

    }

    public void restrictByLevel(final int l) {
        if (divided && l > 0) {
            lessPart.restrictByLevel(l - 1);
            notLessPart.restrictByLevel(l - 1);
        } else {
            divided = false;
        }
    }

    public String toString(final int n) {
        if (divided) {
            return "{" + out.numImages + "} " + out.klass + " [" + t(average) + " ] [" + inInd + "]:" + t(divisor) +
                    " ce = (" + t(determination) + " " + t(error) + ") \n" +
                    tab(n) + "lessPart    " + lessPart.toString(n + 1) +
                    tab(n) + "notLessPart " + notLessPart.toString(n + 1);
        } else {
            return "{" + out.numImages + "} " + out.klass + " [" + t(average) + " ]\n";
        }
    }

    public String toString(final int n, final int num) {
        if (divided && out.numImages >= num) {
            return "{" + out.numImages + "} " + out.klass + " [" + t(average) + " ] [" + inInd + "]:" + t(divisor) +
                    " ce = (" + t(determination) + " " + t(error) + ") \n" +
                    tab(n) + "lessPart    " + lessPart.toString(n + 1, num) +
                    tab(n) + "notLessPart " + notLessPart.toString(n + 1, num);
        } else {
            return "{" + out.numImages + "} " + out.klass + " [" + t(average) + " ]\n";
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
            i[l] += " " + inInd + "(" + out.numImages + "|" + t(divisor) + ")";
            lessPart.getIndexes2(i, l + 1);
            notLessPart.getIndexes2(i, l + 1);
        }
    }

    public int getNumDivisions(final int numOfImagesLimit) {
        int sum = 0;
        if (divided) {
            if (out.numImages > numOfImagesLimit) ++sum;
            sum += lessPart.getNumDivisions(numOfImagesLimit);
            sum += notLessPart.getNumDivisions(numOfImagesLimit);
        }
        return sum;
    }

    public int setDivisions(final int numOfImagesLimit,
                            final NeuroCheckMaps.Division[] divisions,
                            int currentIndex) {
        if (divided) {
            if (out.numImages > numOfImagesLimit) {
                divisions[currentIndex].divisor = divisor;
                divisions[currentIndex].inInd = inInd;
                divisions[currentIndex].numOfImages = out.numImages;
                ++currentIndex;
            }
            currentIndex = lessPart.setDivisions(numOfImagesLimit, divisions, currentIndex);
            currentIndex = notLessPart.setDivisions(numOfImagesLimit, divisions, currentIndex);
        }
        return currentIndex;
    }

    String t(final double x) {
        return String.format("%01.5f", x).replaceAll(",", ".");
    }

    String t(final double x[]) {
        String s = "";
        for (final double v : x) {
            s += " " + String.format("%01.5f", v).replaceAll(",", ".");
        }
        return s;
    }

    String tab(final int n) {
        String s = "";
        for (int i = 0; i < n; ++i) {
            s += "\t";
        }
        return s;
    }

    public void setError(final int errorTypeNum,
                         final NeuroImage[] trainImages,
                         final NeuroImage[] testImages,
                         final int level) {
        final double trainError = NeuroError.errors[errorTypeNum].get(this, trainImages, level);
        final double testError = NeuroError.errors[errorTypeNum].get(this, testImages, level);
        error = (trainError > testError) ? trainError : testError;
    }

    public void setError(final int errorTypeNum,
                         final NeuroImage[] images,
                         final int level) {
        error = NeuroError.errors[errorTypeNum].get(this, images, level);
    }

    public double getError(final int errorTypeNum,
                           final NeuroImage[] images,
                           final int level) {
        return NeuroError.errors[errorTypeNum].get(this, images, level);
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

}

