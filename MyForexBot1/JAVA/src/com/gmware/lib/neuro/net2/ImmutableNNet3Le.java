package com.gmware.lib.neuro.net2;

import java.io.DataInputStream;

/**
 * Created by Æåíÿ on 25.03.2016.
 */
public class ImmutableNNet3Le {
    private NetStats netStats;

    public ImmutableNNet3Le(final DataInputStream netDis, final boolean b) {

    }

    public ImmutableNNet3Le(final DataInputStream netDis, final boolean b, final boolean b1) {

    }

    public double[] propagateMulty(double[] in) {
        return new double[0];
    }

    public NetStats getNetStats() {
        return netStats;
    }

    public class NetStats {
        public double[] trainErrorsRatio;
        public double[] testErrorsRatio;
        public int trainImages;
    }
}
