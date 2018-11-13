package com.gmware.lib.neuro.mynet.Maps;

import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.neuro.mynet.NeuroMapType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Gauss on 23.02.2016.
 */
public class NeuroMapEmpty extends NeuroMap {

    public NeuroMapEmpty() {
        setType();
    }

    @Override
    public final void setType() {
        type = NeuroMapType.Empty;
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
        return new NeuroMapEmpty();
    }
}
