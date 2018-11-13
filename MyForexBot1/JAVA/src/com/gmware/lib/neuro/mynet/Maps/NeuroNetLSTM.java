package com.gmware.lib.neuro.mynet.Maps;

import com.gmware.lib.neuro.mynet.NeuroNet;
import com.gmware.lib.neuro.NetImage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Нейросети LSTM.
 * Created by Gauss on 11.12.2015.
 */
public class NeuroNetLSTM  extends NeuroNet {

    @Override
    public void setType() {

    }

    @Override
    protected void makeSampleNet(final double a) {

    }

    @Override
    protected double[] propagate(final double[] weights, final double[] in, final double[] out) {
        return new double[0];
    }

    @Override
    public double[] addToGradient(final NetImage trainImage) {
        return new double[0];
    }

    @Override
    public void save(final DataOutputStream dataOutputStream) {

    }

    @Override
    public void loadBody(final DataInputStream dis) throws IOException {

    }

    @Override
    public void printNetWeights(final PrintStream log) {

    }
}
