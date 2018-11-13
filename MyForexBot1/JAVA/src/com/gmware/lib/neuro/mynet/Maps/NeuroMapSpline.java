package com.gmware.lib.neuro.mynet.Maps;

import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.neuro.mynet.ShiftMapping;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Spline.
 * 1-dimentional only !!!
 * Created by Gauss on 16.02.2016.
 */
public class NeuroMapSpline extends NeuroMap {

    //(степень полинома + 1)
    public static final int SPLINE_POWER = 3;

    public static class SplineStructure {
        public boolean divided = false;
        public double divisor = 0;
        public double[] p = null;

        public SplineStructure upPart = null;
        public SplineStructure downPart = null;

        public SplineStructure() {
        }

        SplineStructure(final DataInputStream dis) throws IOException {
            divided = dis.readBoolean();
            if (divided) {
                divisor = dis.readDouble();
                upPart = new SplineStructure(dis);
                downPart = new SplineStructure(dis);
            } else {
                p = new double[SPLINE_POWER];
                for (int i = 0; i < SPLINE_POWER; ++i) {
                    p[i] = dis.readDouble();
                }
            }
        }

        void save(final DataOutputStream dos) throws IOException {
            dos.writeBoolean(divided);
            if (divided) {
                dos.writeDouble(divisor);
                upPart.save(dos);
                downPart.save(dos);
            } else {
                for (int i = 0; i < SPLINE_POWER; ++i) {
                    dos.writeDouble(p[i]);
                }
            }
        }

        public double propagate(final double v) {
            if (divided) {
                if (v < divisor) {
                    return downPart.propagate(v);
                } else {
                    return upPart.propagate(v);
                }
            } else {
                double y = 0;
                for (int i = p.length - 1; i >= 0; --i) {
                    y = y * v + p[i];
                }
                return y;
            }
        }

        public String toString(final int n) {
            if (divided) {
                return downPart.toString(n + 1) +
                        ":" + t(divisor) + ":\n" +
                        upPart.toString(n + 1);
            } else {
                String s = "";
                for (final double a : p) {
                    s += " " + t(a) + " ";
                }
                return "[" + s + "]\n";
            }
        }

        String t(final double x) {
            return String.format("%01.5f", x);
        }

        String tabs(final int n) {
            String s = "";
            for (int i = 0; i < n; ++i) {
                s += "\t";
            }
            return s;
        }
    }

    public SplineStructure structure = null;

    @Override
    public final void setType() {
        type = NeuroMapType.Spline;
    }

    public NeuroMapSpline() {
        setType();
    }

    public static NeuroMapSpline loadNewMap(final DataInputStream dis) throws IOException {
        final NeuroMapSpline map = new NeuroMapSpline();
        map.loadBody(dis);
        return map;
    }

    @Override
    public void save(final DataOutputStream dos) {
        try {
            dos.writeLong(type.getFormatCode());
            ShiftMapping.save(shift, dos);
            structure.save(dos);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadBody(final DataInputStream dis) throws IOException {
        shift = ShiftMapping.load(dis);
        structure = new SplineStructure(dis);
    }

    @Override
    public double[] propagate(final double[] in) {
        if (shift == null) {
            return new double[]{structure.propagate(in[0])};
        } else {
            return new double[]{structure.propagate(shift.map(in)[0])};
        }
    }
}
