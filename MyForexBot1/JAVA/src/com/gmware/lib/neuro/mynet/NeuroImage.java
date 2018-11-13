package com.gmware.lib.neuro.mynet;

/**
 * ---
 * Created by Gauss on 19.04.2016.
 */
public class NeuroImage {
    public double[] in = null;
    public int numIn = 0;
    public double[] out = null;
    public int numOut = 0;
    public int klass = 0;
    public int numKlasses = 0;

    public NeuroImage() {
    }

    public NeuroImage(final int numIn, final int numOut, final int numKlasses) {
        this.numIn = numIn;
        this.numOut = numOut;
        this.numKlasses = numKlasses;
    }

    @Override
    public String toString() {
        String s = klass + " |";
        for (int i = 0; i < numOut; ++i) {
            s += String.format(" %5.2f", out[i]);
        }
        s += "|";
        for (int i = 0; i < numIn; ++i) {
            s += String.format(" %5.2f", in[i]);
        }
        return s;
    }
}
