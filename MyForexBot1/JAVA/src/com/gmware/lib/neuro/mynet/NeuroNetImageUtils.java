package com.gmware.lib.neuro.mynet;

import com.gmware.lib.neuro.NetImage;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * -
 * Created by Gauss on 14.04.2016.
 */
public class NeuroNetImageUtils {

    synchronized public static NetImage[] assignImages(final int bufSize, final DataInputStream dis) {
        final NetImage[] imagesBuffer = new NetImage[bufSize];
        int numImages = 0;
        int index = 0;
        try {
            while (true) {
                imagesBuffer[index] = new NetImage(dis);
                numImages++;
                index = numImages % imagesBuffer.length;
            }
        } catch (final EOFException ignored) {
        } catch (final IOException e) {
            e.printStackTrace();
        }
        numImages = (numImages < imagesBuffer.length) ? numImages : imagesBuffer.length;
        final NetImage[] images = new NetImage[numImages];
        System.arraycopy(imagesBuffer, 0, images, 0, numImages);
        return images;
    }


}
