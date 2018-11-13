package com.gmware.lib.neuro.mynet.F;

import com.gmware.lib.neuro.NetImage;

import java.util.Arrays;
import java.util.Comparator;

/**
 * ---
 * Created by Æåíÿ on 03.04.2016.
 */
public class NeuroNormalization {

    public static void normalizeImages(final NetImage[] images, final int ind, final int numClusters) {
        Arrays.sort(images, new Comparator<NetImage>() {
            @Override
            public int compare(final NetImage i1, final NetImage i2) {
                if (i1.in[ind] < i2.in[ind]) return -1;
                if (i1.in[ind] > i2.in[ind]) return 1;
                final double o1 = i1.tag;
                final double o2 = i2.tag;
                if (o1 < o2) return -1;
                if (o1 > o2) return 1;
                return 0;
            }
        });
//        int numClasses = 1;
//        for (int i = 1; i < images.length; ++i) {
//            if (images[i].in[ind] != images[i - 1].in[ind]) ++numClasses;
//        }
        for (int i = 0; i < images.length; i++) {
            images[i].in[ind] = (i * numClusters) / images.length;
        }
    }
}
