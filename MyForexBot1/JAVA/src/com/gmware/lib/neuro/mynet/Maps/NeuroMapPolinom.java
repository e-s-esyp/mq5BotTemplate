package com.gmware.lib.neuro.mynet.Maps;

import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.mynet.ShiftMapping;
import com.gmware.lib.neuro.NetImage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Полиномиальное отображение.
 * Created by Gauss on 11.02.2016.
 */
public class NeuroMapPolinom extends NeuroMap {

    // Общая дисперсия выходов
    double var = Double.MAX_VALUE;
    // Ошибка среднего
    double eA = Double.MAX_VALUE;
    // Относительная ошибка среднего
    double rEA = Double.MAX_VALUE;

    @Override
    public final void setType() {
        type = NeuroMapType.Polinom;
    }

    public NeuroMapPolinom() {
        setType();
    }

    public static NeuroMapPolinom loadNewMap(final DataInputStream dis) throws IOException {
        final NeuroMapPolinom map = new NeuroMapPolinom();
        map.loadBody(dis);
        return map;
    }

    @Override
    public void save(final DataOutputStream dos) {
        try {
            dos.writeLong(type.getFormatCode());
            ShiftMapping.save(shift, dos);
            dos.writeInt(w.length);
            for (final double a : w) {
                dos.writeDouble(a);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadBody(final DataInputStream dis) throws IOException {
        shift = ShiftMapping.load(dis);
        final int l = dis.readInt();
        w = new double[l];
        for (int i = 0; i < w.length; i++) {
            final double wi = dis.readDouble();
            if (Double.isNaN(wi)) {
                exception("NaN weight.");
            }
            w[i] = wi;
        }
    }

    @Override
    public double[] propagate(final double[] in) {
        final double x;
        if (shift == null) {
            x = in[0];
        } else {
            x = shift.map(in)[0];
        }
        double y = 0;
        for (int i = w.length - 1; i >= 0; --i) {
            y = y * x + w[i];
        }
        return new double[]{y};
    }

    public void setErrorOfAverage(final NetImage[] images) {
        setNumInOuts(images);
        if (numIns != 1 || numOuts != 1) {
            exception("Only 1 dimention supported.");
        }
        setAverageOut(images);
        var = getVariance(images);
        eA = getErrorOfAverage(averageOut, images);
        rEA = var > 0.0 ? eA / var : 1.0;
    }

    public double getRelativeErrors(final NetImage[] images) {
        final double e = getErrorQuadratic(images);
        final double rE = var > 0.0 ? e / var : 1.0;
        rrError = (rEA > 0) ? rE / rEA : rE;
        return rrError;
    }

    private void setNumInOuts(final NetImage[] images) {
        if (images == null) return;
        if (images.length < 1) return;
        numIns = images[0].getNumIn();
        numOuts = images[0].getNumOut();
    }

    private void setAverageOut(final NetImage[] images) {
        averageOut = new double[numOuts];
        double d;
        for (int i = 0; i < numOuts; ++i) {
            d = 0;
            for (final NetImage image : images) {
                d += image.out[i];
            }
            averageOut[i] = d / images.length;
        }
    }

}
