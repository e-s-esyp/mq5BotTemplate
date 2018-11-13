package com.gmware.lib.neuro.mynet;

/**
 * Created by Gauss on 31.03.2016.
 */
public class NeuroCommon {

    public static double signum(final double x) {
        if (x > 0.0) return 1.0D;
        if (x < -0.0) return -1.0D;
        return 0.0D;
    }

}
