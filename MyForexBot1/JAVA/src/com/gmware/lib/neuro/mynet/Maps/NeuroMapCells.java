package com.gmware.lib.neuro.mynet.Maps;

import com.gmware.lib.neuro.mynet.NeuroImage;
import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.neuro.mynet.NeuroMapType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * -
 * Created by Gauss on 27.05.2016.
 */
public class NeuroMapCells extends NeuroMap {
    int[] usedIns = null;
    double[][] allEdges = null;
    int numKlasses = -1;

    public NeuroMapCells() {
        setType();
    }

    public NeuroMapCells(
            final NeuroImage[] images,
            final int[] usedIns,
            final double[][] allEdges,
            final int numDivisions) {
        setType();
        numImages = images.length;
        final NeuroImage image = images[0];
        numIns = image.numIn;
        numOuts = image.numOut;
        numKlasses = image.numKlasses;
        this.usedIns = usedIns;
        this.allEdges = allEdges;
        structure = new Structure(0, numDivisions);
    }

    @Override
    public final void setType() {
        type = NeuroMapType.Cells;
    }

    public class Out {
        public int numImages = 0;
        public int klass = -1;
        public double[] profits = null;

        Out() {
            profits = new double[numKlasses];
        }

        public Out(final DataInputStream dis) throws IOException {
            numImages = dis.readInt();
            klass = dis.readInt();
            profits = new double[numKlasses];
            for (int i = 0; i < numKlasses; ++i) {
                profits[i] = dis.readDouble();
            }
        }

        void save(final DataOutputStream dos) throws IOException {
            dos.writeInt(numImages);
            dos.writeInt(klass);
            for (int i = 0; i < numKlasses; ++i) {
                dos.writeDouble(profits[i]);
            }
        }

        public void addImage(final NeuroImage image) {
            numImages++;
            for (int i = 0; i < profits.length; ++i) {
                profits[i] += image.out[i];
            }
        }

        public void setAverageOut() {
            double max = -Double.MAX_VALUE;
            for (int i = 0; i < profits.length; ++i) {
                profits[i] /= numImages;
                if (max < profits[i]) {
                    max = profits[i];
                    klass = i;
                }
            }
        }
    }

    class Structure {
        //division
        Structure[] d = null;
        double[] edges = null;
        int inInd = -1;
        Out out = null;

        public Structure(final int numDivisions) {
            d = new Structure[numDivisions];
        }

        public Structure(final int i, final int numDivisions) {
            if (i < usedIns.length) {
                d = new Structure[numDivisions];
                edges = allEdges[i];
                inInd = usedIns[i];
                for (int j = 0; j < d.length; ++j) {
                    d[j] = new Structure(i + 1, numDivisions);
                }
            } else {
                out = new Out();
            }
        }

        public Structure(final DataInputStream dis, final int j) throws IOException {
            final int numd = dis.readInt();
            if (numd >= 0) {
                d = new Structure[numd];
                for (int i = 0; i < numd; ++i) {
                    d[i] = new Structure(dis, j + 1);
                }
                edges = allEdges[j];
                inInd = usedIns[j];
            }
            if (dis.readBoolean()) {
                out = new Out(dis);
            }

        }

        public void save(final DataOutputStream dos) throws IOException {
            if (d == null) {
                dos.writeInt(-1);
            } else {
                dos.writeInt(d.length);
                for (final Structure a : d) {
                    a.save(dos);
                }
            }
            if (out != null) {
                dos.writeBoolean(true);
                out.save(dos);
            } else {
                dos.writeBoolean(false);
            }
        }

        int find(final double in) {
            int i = 0;
            for (; i < edges.length; ++i) {
                if (in < edges[i]) {
                    return i;
                }
            }
            return edges.length;
        }

        Out getOut(final double[] in) {
            if (d != null) {
                return d[find(in[inInd])].getOut(in);
            } else {
                return out;
            }
        }

        public void addImage(final NeuroImage image) {
            if (d != null) {
                d[find(image.in[inInd])].addImage(image);
            } else {
                out.addImage(image);
            }
        }

        public void setAverageOut() {
            if (d != null) {
                for (final Structure a : d) {
                    a.setAverageOut();
                }
            } else {
                out.setAverageOut();
            }

        }
    }

    Structure structure = null;

    @Override
    public void save(final DataOutputStream dos) {
        try {
            dos.writeLong(type.getFormatCode());
            dos.writeInt(numImages);
            dos.writeInt(numIns);
            dos.writeInt(numOuts);
            dos.writeInt(numKlasses);
            if (usedIns != null) {
                dos.writeInt(usedIns.length);
                for (final int usedIn : usedIns) {
                    dos.writeInt(usedIn);
                }
                dos.writeInt(allEdges[0].length);
                for (final double[] edges : allEdges) {
                    for (final double edge : edges) {
                        dos.writeDouble(edge);
                    }
                }
            } else {
                dos.writeInt(-1);
            }
            if (structure != null) {
                dos.writeBoolean(true);
                structure.save(dos);
            } else {
                dos.writeBoolean(false);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadBody(final DataInputStream dis) throws IOException {
        numImages = dis.readInt();
        numIns = dis.readInt();
        numOuts = dis.readInt();
        numKlasses = dis.readInt();
        final int numUsedIns = dis.readInt();
        if (numUsedIns >= 0) {
            usedIns = new int[numUsedIns];
            for (int i = 0; i < numUsedIns; i++) {
                usedIns[i] = dis.readInt();
            }
            allEdges = new double[numUsedIns][];
            final int numEdges = dis.readInt();
            for (int i = 0; i < numUsedIns; ++i) {
                allEdges[i] = new double[numEdges];
                for (int j = 0; j < numEdges; ++j) {
                    allEdges[i][j] = dis.readDouble();
                }
            }
        }
        if (dis.readBoolean()) {
            structure = new Structure(dis, 0);
        }
    }

    public static NeuroMapCells loadNewMap(final DataInputStream dis) throws IOException {
        final NeuroMapCells map = new NeuroMapCells();
        map.loadBody(dis);
        return map;
    }

    @Override
    public double[] propagate(final double[] in) {
        return structure.getOut(in).profits;
    }

    public Out getOut(final double[] in) {
        return structure.getOut(in);
    }

    public void addImage(final NeuroImage image) {
        structure.addImage(image);
    }

    public void setAverageOut() {
        structure.setAverageOut();
    }

}
