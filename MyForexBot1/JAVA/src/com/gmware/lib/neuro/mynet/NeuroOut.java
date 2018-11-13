package com.gmware.lib.neuro.mynet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Gauss on 13.06.2016.
 */

public class NeuroOut {
    public int numKlasses = 0;
    public int numImages = 0;
    public int klass = -1;
    public double[] profits = null;

    public NeuroOut(final int numKlasses) {
        this.numKlasses = numKlasses;
        profits = new double[numKlasses];
    }

    public NeuroOut(final DataInputStream dis, final int numKlasses) throws IOException {
        this.numKlasses = numKlasses;
        numImages = dis.readInt();
        klass = dis.readInt();
        profits = new double[numKlasses];
        for (int i = 0; i < numKlasses; ++i) {
            profits[i] = dis.readDouble();
        }
    }

    public void save(final DataOutputStream dos) throws IOException {
        dos.writeInt(numImages);
        dos.writeInt(klass);
        for (int i = 0; i < numKlasses; ++i) {
            dos.writeDouble(profits[i]);
        }
    }

    public void addImage(final NeuroImage image) {
        numImages++;
        for (int i = 0; i < profits.length; ++i) {
            profits[i] += image.out[i];
        }
    }

    public void setAverageOut() {
        double max = -Double.MAX_VALUE;
        for (int i = 0; i < profits.length; ++i) {
            profits[i] /= numImages;
            if (max < profits[i]) {
                max = profits[i];
                klass = i;
            }
        }
    }
}

