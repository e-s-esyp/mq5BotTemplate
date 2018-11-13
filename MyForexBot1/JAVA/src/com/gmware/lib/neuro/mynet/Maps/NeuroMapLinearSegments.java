package com.gmware.lib.neuro.mynet.Maps;

import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.neuro.mynet.NeuroMapType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Кусочно-линейное отображение.
 * Created by Gauss on 10.02.2016.
 */
public class NeuroMapLinearSegments extends NeuroMap {

    public double minX = 0;
    public double minY = 0;
    public double maxX = 0;
    public double maxY = 0;
    public SegmentStructure structure = null;

    public static class SegmentStructure {
        public boolean divided = false;
        public double divisor = 0;
        public double p0 = 0;
        public double p1 = 0;
        public SegmentStructure upPart = null;
        public SegmentStructure downPart = null;

        public SegmentStructure() {
        }

        SegmentStructure(final DataInputStream dis) throws IOException {
            divided = dis.readBoolean();
            if (divided) {
                divisor = dis.readDouble();
                upPart = new SegmentStructure(dis);
                downPart = new SegmentStructure(dis);
                p0 = dis.readDouble();
            } else {
                p0 = dis.readDouble();
                p1 = dis.readDouble();
            }
        }

        void save(final DataOutputStream dos) throws IOException {
            dos.writeBoolean(divided);
            if (divided) {
                dos.writeDouble(divisor);
                upPart.save(dos);
                downPart.save(dos);
                dos.writeDouble(p0);
            } else {
                dos.writeDouble(p0);
                dos.writeDouble(p1);
            }
        }

        public double propagate(final double x) {
            if (divided) {
                if (x == divisor) {
                    return p0;
                }
                if (x < divisor) {
                    return downPart.propagate(x);
                } else {
                    return upPart.propagate(x);
                }
            } else {
                return p1 * x + p0;
            }
        }

        public String toString(final int n) {
            if (divided) {
                return downPart.toString(n + 1) +
                        n + ": " + t(divisor) + " : [ " + t(p0) + " ]\n" +
                        upPart.toString(n + 1);
            } else {
                return "[ " + t(p0) + " " + t(p1) + " ]\n";
            }
        }

        String t(final double x) {
            return String.format("%01.5f", x);
        }

    }

    @Override
    public final void setType() {
        type = NeuroMapType.LinearSegments;
    }

    public NeuroMapLinearSegments() {
        setType();
    }

    @Override
    public void save(final DataOutputStream dos) {
        try {
            dos.writeLong(type.getFormatCode());
            dos.writeDouble(minX);
            dos.writeDouble(minY);
            dos.writeDouble(maxX);
            dos.writeDouble(maxY);
            structure.save(dos);
        } catch (final IOException e) {
            System.out.println("type = " + type);
            e.printStackTrace();
        }
    }

    public static NeuroMapLinearSegments loadNewMap(final DataInputStream dis) throws IOException{
        final NeuroMapLinearSegments net = new NeuroMapLinearSegments();
        net.loadBody(dis);
        return net;
    }

    @Override
    public void loadBody(final DataInputStream dis) throws IOException {
        minX = dis.readDouble();
        minY = dis.readDouble();
        maxX = dis.readDouble();
        maxY = dis.readDouble();
        structure = new SegmentStructure(dis);
    }

    @Override
    public double[] propagate(final double[] in) {
        final double x = in[0];
        if (x <= minX) {
            return new double[]{minY};
        } else {
            if (x >= maxX) {
                return new double[]{maxY};
            } else {
                return new double[]{structure.propagate(x)};
            }
        }
    }

    @Override
    public String toString() {
        return "map[" + minX + "]= " + minY + "\n" +
                "map[" + maxX + "]= " + maxY + "\n" +
                structure.toString(1);
    }
}
