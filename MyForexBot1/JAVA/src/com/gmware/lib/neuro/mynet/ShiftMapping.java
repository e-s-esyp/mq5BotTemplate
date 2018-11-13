package com.gmware.lib.neuro.mynet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Gauss on 25.01.2016.
 */
public class ShiftMapping {
    double a = 0;
    double b = 0;

    ShiftMapping() {
    }

    ShiftMapping(final double a, final double b) {
        this.a = a;
        this.b = b;
    }

    public double[] map(final double[] in) {
        final double[] out = new double[in.length];
        for (int i = 0; i < in.length; ++i) {
            out[i] = in[i] * a + b;
        }
        return out;
    }

    public static void save(final ShiftMapping shiftMapping, final DataOutputStream dos) {
        try {
            if (shiftMapping == null) {
                dos.writeBoolean(false);
            } else {
                dos.writeBoolean(true);
                dos.writeDouble(shiftMapping.a);
                dos.writeDouble(shiftMapping.b);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static ShiftMapping load(final DataInputStream dis) throws IOException {
        if (dis.readBoolean()) {
            final ShiftMapping shiftMapping = new ShiftMapping();
            shiftMapping.a = dis.readDouble();
            shiftMapping.b = dis.readDouble();
            return shiftMapping;
        } else {
            return null;
        }
    }

}
