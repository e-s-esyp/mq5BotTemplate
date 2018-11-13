package com.gmware.lib.neuro.mynet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Gauss on 23.02.2016.
 */
public class IndexedDoubles {
    public int index = 0;
    public double value = 0;

    public IndexedDoubles() {
    }

    public IndexedDoubles(final int i, final double v) {
        index = i;
        value = v;
    }

    public IndexedDoubles(final DataInputStream dis) throws IOException {
        index = dis.readInt();
        value = dis.readDouble();
    }

    public void setValue(final int i, final double v) {
        index = i;
        value = v;
    }

    public static int formatOfDouble = 3;

    @Override
    public String toString() {
        return "[" + index + "] = " + t(value);
    }

    static String t(final double x) {
        return String.format("%01." + formatOfDouble + "f", x).replaceAll(",", ".");
    }

    public void save(final DataOutputStream dos) throws IOException {
        dos.writeInt(index);
        dos.writeDouble(value);
    }

    public void updateMaxValue(final int i, final double v) {
        index = i;
        if (value < v) {
            value = v;
        }
    }
}

