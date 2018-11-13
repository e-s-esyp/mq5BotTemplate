package com.gmware.lib.neuro.mynet.F;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by Æåíÿ on 24.06.2016.
 */
public abstract class NeuroFPredictor {
    public PrintStream log;

    public abstract void train2(int[][] pars, TimedDoubles[] trainData, String predictorPath) throws IOException;

    public abstract void initParameters(TimedDoubles[] data, int startInd);

    public abstract int getDecision(int order, int i, TimedDoubles[] data, int ind);

    public abstract String getShortDescription();

}
