package com.gmware.lib.neuro.mynet.Mappings;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapLinearSegments;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.mynet.NeuroMapping;

import java.io.PrintStream;

/**
 * Представление возрастающей функции в виде кусочно-линейного преобразования.
 * Created by Gauss on 10.02.2016.
 */
public class NeuroMappingLinearSegments extends NeuroMapping {

    @Override
    protected NeuroMap trainMap(final NetImage[] images,
                                final NeuroMapType mapType,
                                final Rnd rnd,
                                final PrintStream log,
                                final String netOutPath) {
        return train(images, mapType, rnd, log, netOutPath);
    }

    static class NeuroImageLC {
        double in = 0.0;
        double out = 0.0;
        int weight = 0;

        public NeuroImageLC(final double in, final double out, final int num) {
            this.in = in;
            this.out = out;
            weight = num;
        }

        @Override
        public String toString() {
            return "(" + t(in) + " " + t(out) + " " + weight + ")";
        }
    }

    public static NeuroMap train(final NetImage[] images,
                                 final NeuroMapType mapType,
                                 final Rnd rnd,
                                 final PrintStream log,
                                 final String netOutPath) {
        final NeuroMapLinearSegments map = new NeuroMapLinearSegments();
        if (images == null) return null;
        if (images.length == 0) return map;
        int numPlates = 1;
        for (int i = 1; i < images.length; ++i) {
            if (images[i].in[0] != images[i - 1].in[0]) ++numPlates;
        }
        final NeuroImageLC[] neuroWeightedImages = new NeuroImageLC[numPlates];
        int k = 0;
        double sumOut = images[0].out[0];
        int num = 1;
        for (int i = 1; i < images.length; ++i) {
            if (images[i].in[0] != images[i - 1].in[0]) {
                neuroWeightedImages[k++] = new NeuroImageLC(images[i - 1].in[0], sumOut / num, num);
                sumOut = images[i].out[0];
                num = 1;
            } else {
                sumOut += images[i].out[0];
                ++num;
            }
        }
        neuroWeightedImages[k] = new NeuroImageLC(images[images.length - 1].in[0], sumOut / num, num);
        map.minX = neuroWeightedImages[0].in;
        map.minY = neuroWeightedImages[0].out;
        map.maxX = neuroWeightedImages[k].in;
        map.maxY = neuroWeightedImages[k].out;
        map.structure = trainStructure(neuroWeightedImages, 0, numPlates - 1, log);
        log.println(map);
        return map;
    }

    private static NeuroMapLinearSegments.SegmentStructure trainStructure(final NeuroImageLC[] images,
                                                                          final int limit1,
                                                                          final int limit2,
                                                                          final PrintStream log) {
        System.out.println("[" + limit1 + "," + limit2 + "]   " +
                "[" + images[limit1] + "," + images[limit2] + "]   ");
        final NeuroMapLinearSegments.SegmentStructure s = new NeuroMapLinearSegments.SegmentStructure();
        makeSlope(s, images, limit1, limit2);
        if (limit2 - limit1 < 2) return s;
        final double error = getError(s, images, limit1, limit2);
        if (error < 0.001) return s;
        final NeuroMapLinearSegments.SegmentStructure s1 = new NeuroMapLinearSegments.SegmentStructure();
        final NeuroMapLinearSegments.SegmentStructure s2 = new NeuroMapLinearSegments.SegmentStructure();
        int l1 = limit1;
        int l2 = limit2;
        int lastDivisor = (l1 + l2) / 2;
        while (true) {
            makeSlope(s1, images, limit1, limit2);
            final double error1 = getError(s1, images, limit1, lastDivisor);
            makeSlope(s2, images, limit1, limit2);
            final double error2 = getError(s2, images, lastDivisor, limit2);
            if (error1 < 0.001 && error2 < 0.001) {
                s.divided = true;
                s.divisor = images[lastDivisor].in;
                s.p0 = images[lastDivisor].out;
                s.downPart = s1;
                s.upPart = s2;
                return s;
            }
            if (error1 > error2) {
                if (l2 == lastDivisor) break;
                l2 = lastDivisor;
            } else {
                if (l1 == lastDivisor) break;
                l1 = lastDivisor;
            }
            lastDivisor = (l1 + l2) / 2;
        }
        s.divided = true;
        s.divisor = images[lastDivisor].in;
        s.p0 = images[lastDivisor].out;
        s.downPart = trainStructure(images, limit1, lastDivisor, log);
        s.upPart = trainStructure(images, lastDivisor, limit2, log);
        return s;

    }


    static private void makeSlope(final NeuroMapLinearSegments.SegmentStructure s,
                                  final NeuroImageLC[] images,
                                  final int limit1,
                                  final int limit2) {
        if (limit2 - limit1 == 0) {
            s.p0 = images[limit1].out;
            return;
        }
        final double x1 = images[limit1].in;
        final double y1 = images[limit1].out;
        final double x2 = images[limit2].in;
        final double y2 = images[limit2].out;
        final double c = (y2 - y1) / (x2 - x1);
        s.p0 = y1 - c * x1;
        s.p1 = c;
    }

    static private double getError(final NeuroMapLinearSegments.SegmentStructure s,
                                   final NeuroImageLC[] images,
                                   final int limit1,
                                   final int limit2) {
        double d = 0;
        for (int i = limit1; i <= limit2; ++i) {
            final double e = Math.abs(s.propagate(images[i].in) - images[i].out);
            if (d < e) d = e;
        }
        return d;
    }

}
