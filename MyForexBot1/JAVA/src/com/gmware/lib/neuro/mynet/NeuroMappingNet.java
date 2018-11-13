package com.gmware.lib.neuro.mynet;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.mynet.Mappings.NeuroTrainerStandart;
import com.gmware.lib.games.holdem.common.rnd.Rnd517;

import java.io.PrintStream;

/**
 * Created by Gauss on 12.02.2016.
 */
public class NeuroMappingNet extends NeuroMapping{

    @Override
    protected NeuroMap trainMap(final NetImage[] images,
                                final NeuroMapType mapType,
                                final Rnd rnd,
                                final PrintStream log,
                                final String netOutPath) {
        return NeuroTrainerStandart.train(images, NeuroMapType.Normalizer, new Rnd517(), log, null);
    }
}
