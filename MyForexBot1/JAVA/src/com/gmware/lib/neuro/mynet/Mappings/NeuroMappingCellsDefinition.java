package com.gmware.lib.neuro.mynet.Mappings;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.NeuroImage;
import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.mynet.NeuroMapping;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapCells;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

/**
 * -
 * Created by Gauss on 27.05.2016.
 */
public class NeuroMappingCellsDefinition extends NeuroMapping {

    @Override
    protected NeuroMap trainMap(
            final NetImage[] images,
            final NeuroMapType mapType,
            final Rnd rnd,
            final PrintStream log,
            final String netOutPath) {
        return null;
    }

    public static NeuroMap train(
            final NetImage[] images,
            final NeuroMapType mapType,
            final Rnd rnd,
            final PrintStream log,
            final String netOutPath) {
        return null;
    }

    public static NeuroMapCells train(
            final NeuroImage[] images,
            final int[] inInds,
            final int numDivisions,
            final Rnd rnd,
            final PrintStream log,
            final String netOutPath) {
        final double[][] edges = new double[inInds.length][];
        for (int i = 0; i < inInds.length; ++i) {
            edges[i] = findEdges(images, inInds[i], numDivisions);
        }
        final NeuroMapCells map = new NeuroMapCells(images, inInds, edges, numDivisions);
        for (final NeuroImage image : images) {
            map.addImage(image);
        }
        map.setAverageOut();
        return map;
    }

    private static double[] findEdges(
            final NeuroImage[] images,
            final int inInd,
            final int numDivisions) {
        final double[] result = new double[numDivisions - 1];
        Arrays.sort(images, new Comparator<NeuroImage>() {
            @Override
            public int compare(final NeuroImage o1, final NeuroImage o2) {
                if (o1.in[inInd] < o2.in[inInd]) return -1;
                if (o1.in[inInd] > o2.in[inInd]) return 1;
                return 0;
            }
        });
        for (int i = 1; i < numDivisions; i++) {
            result[i - 1] = images[(images.length * i) / numDivisions].in[inInd];
        }
        return result;
    }
}
