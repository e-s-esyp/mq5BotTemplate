package com.gmware.lib.neuro.mynet.Mappings;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.mynet.NeuroNet;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.mynet.NeuroTrainer;

import java.io.PrintStream;

/**
 * Учитель нейросети с фиксированной частью.
 *
 * Created by Gauss on 11.02.2016.
 */
public class NeuroTrainerStandartWithFixedInns extends NeuroTrainer {

    @Override
    protected NeuroMap trainMap(final NetImage[] images,
                                final NeuroMapType mapType,
                                final Rnd rnd,
                                final PrintStream log,
                                final String netOutPath) {
        return train(images, mapType, rnd, log, netOutPath);
    }

    public static NeuroNet train(NetImage[] images, NeuroMapType netType, Rnd rnd, PrintStream log, String netOutPath) {
        return null;
    }
}
