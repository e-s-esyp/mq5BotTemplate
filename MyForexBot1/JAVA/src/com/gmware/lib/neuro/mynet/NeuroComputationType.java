package com.gmware.lib.neuro.mynet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Gauss on 11.04.2016.
 */
public class NeuroComputationType {
    public boolean isQuadraticError = false;

    public boolean isAbsError = false;

    public boolean isComplexError = false;

    //выход - не класс
    public boolean isContinuousOut = false;

    public boolean isWeighted = false;

    public boolean isFCR = false;

    public boolean isAlphas = false;

    public boolean isF = false;

    //разбиение по коррелляции
    public boolean isPBC = false;

    //точное разбиение
    public boolean isPresize = false;

    public byte numIn = 0;

    public byte numOut = 0;

    public byte numKlasses = 0;

    //фиксированный тип
    public byte errorTypeNum = 0;

    public double[] weight = null;

    public NeuroComputationType() {

    }

    public NeuroComputationType(final DataInputStream dis) throws IOException {
        setParameters(dis.readLong());
        numIn = dis.readByte();
        numOut = dis.readByte();
        numKlasses = dis.readByte();
        if (isWeighted) {
            weight = new double[dis.readInt()];
            for (int i = 0; i < weight.length; ++i) {
                weight[i] = dis.readDouble();
            }
        }
    }

    public void write(final DataOutputStream dos) throws IOException {
        dos.writeLong(getParameters());
        dos.writeByte(numIn);
        dos.writeByte(numOut);
        dos.writeByte(numKlasses);
        if (isWeighted) {
            dos.writeInt(weight.length);
            for (final double v : weight) {
                dos.writeDouble(v);
            }
        }
    }

    private long getParameters() {
        long l = 0;
        l |= isQuadraticError ? 0x0001 : 0;
        l |= isAbsError ? 0x0002 : 0;
        l |= isComplexError ? 0x0004 : 0;
        l |= isContinuousOut ? 0x0008 : 0;
        l |= isWeighted ? 0x0010 : 0;
        l |= isFCR ? 0x0020 : 0;
        l |= isAlphas ? 0x0040 : 0;
        l |= isF ? 0x0080 : 0;
        l |= isPBC ? 0x0100 : 0;
        l |= isPresize ? 0x0200 : 0;
        return 0;
    }

    private void setParameters(final long l) {
        isQuadraticError = (l & 0x0001) != 0;
        isAbsError = (l & 0x0002) != 0;
        isComplexError = (l & 0x0004) != 0;
        isContinuousOut = (l & 0x0008) != 0;
        isWeighted = (l & 0x0010) != 0;
        isFCR = (l & 0x0020) != 0;
        isAlphas = (l & 0x0040) != 0;
        isF = (l & 0x0080) != 0;
        isPBC = (l & 0x0100) != 0;
        isPresize = (l & 0x0200) != 0;
    }

    public void setFCR() {
        isQuadraticError = true;
        isAbsError = false;
        isComplexError = false;
        isContinuousOut = true;
        isWeighted = true;
        isFCR = true;
        isAlphas = false;
        isF = false;
        isPBC = true;
        isPresize = false;
    }

    public void setFCR2() {
        setFCR();
        numOut = 1;
        numKlasses = 2;
        errorTypeNum = 0;
    }

    public void setFCR3() {
        setFCR();
        numOut = 3;
        numKlasses = 3;
        errorTypeNum = 1;
    }

    public void setAlphas() {
        isQuadraticError = true;
        isAbsError = false;
        isComplexError = false;
        isContinuousOut = true;
        isWeighted = true;
        isFCR = false;
        isAlphas = true;
        isF = false;
        isPBC = true;
        isPresize = false;
        numOut = 1;
        numKlasses = 0;
        errorTypeNum = 2;
    }

    public void setF() {
        isQuadraticError = false;
        isAbsError = false;
        isComplexError = true;
        isContinuousOut = false;
        isWeighted = false;
        isFCR = false;
        isAlphas = false;
        isF = true;
        isPBC = false;
        isPresize = true;
        numOut = 3;
        numKlasses = 3;
        errorTypeNum = 3;
    }

    //!!! out[i] >= 0
    public void setWeights(final double[] averageOut) {
        if (!isWeighted) return;
        weight = new double[averageOut.length];
        for (int i = 0; i < weight.length; ++i) {
            if (averageOut[i] > 0) weight[i] = 1 / averageOut[i];
            else weight[i] = 1;
        }
        double sum = 0;
        for (final double v : weight) {
            sum += v;
        }
        for (int i = 0; i < weight.length; ++i) {
            weight[i] /= sum;
        }
    }

}

