package com.gmware.lib.neuro.net2;

import com.gmware.lib.games.holdem.gameinfo.GameInfo;

/**
 * Created by Æåíÿ on 25.03.2016.
 */
public interface Predictor {
    double[] getActions(GameInfo gi, PredictorContext predictorContext, int situationCode, double[] features);

    Stats getActionStats(GameInfo gi, PredictorContext predictorContext, int situationCode);

    double[] getAlphas(GameInfo gi, PredictorContext predictorContext, int situationCode, double[] features);

    Stats getAlphaStats(GameInfo gi, PredictorContext predictorContext, int situationCode);

    String getDesctiption(GameInfo gi, PredictorContext predictorContext);

    public interface Stats{
        public int getNumImages();
    }

    public class PredictorContext {
    }
}
