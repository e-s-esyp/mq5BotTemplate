package com.gmware.lib.neuro.mynet.Maps;

import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.mynet.NeuroMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Gauss on 24.02.2016.
 */
public class NeuroMapUnknown extends NeuroMap {

    public NeuroMapUnknown() {
        setType();
    }

    @Override
    public final void setType() {
        type = NeuroMapType.Unknown;
    }

    @Override
    public void save(final DataOutputStream dos) {
        try {
            dos.writeLong(type.getFormatCode());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadBody(final DataInputStream dis) throws IOException {

    }

    @Override
    public double[] propagate(final double[] doubles) {
        return null;
    }

    public static NeuroMap loadNewMap(final DataInputStream dis) {
        return new NeuroMapUnknown();
    }
}
