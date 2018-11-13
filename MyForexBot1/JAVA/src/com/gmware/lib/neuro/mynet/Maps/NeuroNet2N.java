package com.gmware.lib.neuro.mynet.Maps;

import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.mynet.NeuroNet;
import com.gmware.lib.neuro.mynet.NeuroNetStructure;
import com.gmware.lib.neuro.NetImage;

import java.io.*;

/**
 * Created by Gauss on 21.01.2016.
 * <p/>
 * Класс, реализующий нейросеть с двумя слоями узлов.
 */

public class NeuroNet2N extends NeuroNet {

    @Override
    public final void setType() {
        type = NeuroMapType.TwoNodeLayers;
    }

    public NeuroNet2N() {
        setType();
    }

    public int getN0() {
        return n0;
    }

    public int getN1() {
        return n1;
    }

    public int getN2() {
        return n2;
    }

    /**
     * Число входов.
     */
    private int n0 = 0;
    /**
     * Число нейронов на первом слое.
     */
    private int n1 = 0;
    /**
     * Число выходов (число нейронов на втором слое).
     */
    private int n2 = 0;
    /**
     * для вычисления градиента
     */
    private int nb1 = 0, nb2 = 0;
    /**
     * Выходы из нейронов первого слоя.
     */
    private double[] y1 = null;
    /**
     * Выходы из нейронов первого слоя c производной сигмоида.
     */
    private double[] y1d = null;
    /**
     * Выходы из нейронов второго (внешнего) слоя c производной сигмоида.
     */
    private double[] y2d = null;

    public NeuroNet2N(final NeuroNetStructure structure) {
        super(structure);
        setType();
        this.structure = structure;
        n0 = structure.valence[0][0];
        n1 = structure.valence[1][0];
        n2 = structure.valence[2][0];
        nb1 = (n0 + 1) * n1;
        nb2 = nb1 + (n1 + 1) * n2;
        y1 = new double[n1];
        lastNetOut = new double[n2];
        lastNetOutD = new double[n2];
        y1d = new double[n1];
        y2d = new double[n2];
        w = new double[getNumWeightsTotal()];
        dw = new double[getNumWeightsTotal()];
    }

    public static NeuroNet2N loadNewNet(final DataInputStream dis) throws IOException {
        final NeuroNet2N net = new NeuroNet2N();
        net.loadBody(dis);
        return net;
    }

    @Override
    public final void loadBody(final DataInputStream dis) throws IOException {
        n0 = dis.readInt();
        n1 = dis.readInt();
        n2 = dis.readInt();
        setNumInOut(n0, n2);
        structure = new NeuroNetStructure(new int[][]
                {{n0}, {n1}, {n2}});
        nb1 = (n0 + 1) * n1;
        nb2 = nb1 + (n1 + 1) * n2;
        y1 = new double[n1];
        lastNetOut = new double[n2];
        lastNetOutD = new double[n2];
        y1d = new double[n1];
        y2d = new double[n2];
        w = new double[getNumWeightsTotal()];
        dw = new double[getNumWeightsTotal()];
        for (int i = 0; i < w.length; i++) {
            final double wi = dis.readDouble();
            if (Double.isNaN(wi)) {
                exception("NaN weight.");
            }
            w[i] = wi;
        }
        if (dis.readInt() == 1) {
            final int numInns = dis.readInt();
            normalization = new NeuroNet[numInns];
            for (int i = 0; i < numInns; ++i) {
                normalization[i] = load(dis);
            }
        }
    }

    @Override
    public void makeSampleNet(final double a) {//TODO: !neuro!test
        w = new double[]{
                1, a * 0.1 + 0.5,
                1, 0,
                1, -1, 0,
                1, 1, 0,
                0, 1, 0,
                1, 1, 1, 0,};
    }

    /**
     * Конструктор копирования.
     *
     * @param net нейросеть.
     */
    public NeuroNet2N(final NeuroNet2N net) {
        type = net.type;
        n0 = net.n0;
        n1 = net.n1;
        n2 = net.n2;
        nb1 = (n0 + 1) * n1;
        nb2 = nb1 + (n1 + 1) * n2;
        y1 = new double[n1];
        lastNetOut = new double[n2];
        lastNetOutD = new double[n2];
        y1d = new double[n1];
        y2d = new double[n2];
        w = net.w;
        dw = net.dw;
    }

    /**
     * Создать копию.
     *
     * @return копия этого объекта.
     */
    public NeuroNet2N getCopy() {
        return new NeuroNet2N(this);
    }

    /**
     * @return общее количество весов.
     */
    public final int getNumWeightsTotal() {
        return nb2;
    }

    /**
     * По входному массиву данных получить массив значений на выходах нейросети.
     *
     * @param in массив входных данных длины не меньше, чем {@link #getNumInputs()}.
     * @return массив длины {@link #getNumOutputs()} с предсказанными значениями.
     */
    @Override
    public double[] propagate(final double[] weights, final double[] in, final double[] out) {
        int wi = 0;
        for (int i = 0; i < n1; i++) {
            double s = 0;
            for (int j = 0; j < n0; j++) {
                s += weights[wi++] * in[j];
            }
            y1[i] = f(s + weights[wi++]);
        }
        for (int i = 0; i < n2; i++) {
            double s = 0;
            for (int j = 0; j < n1; j++) {
                s += weights[wi++] * y1[j];
            }
            out[i] = f(s + weights[wi++]);
        }
        return out;
    }

    /**
     * По входному массиву данных получить массив значений на выходах нейросети.
     *
     * @param in массив входных данных длины не меньше, чем {@link #getNumInputs()}.
     * @return массив длины {@link #getNumOutputs()} с предсказанными значениями.
     */
    @Override
    public double[] propagateMulty(final double[] in) {
        int wi = 0;
        final double[] out1 = new double[n1];
        for (int i = 0; i < n1; i++) {
            double s = 0;
            for (int j = 0; j < n0; j++) {
                s += w[wi++] * in[j];
            }
            out1[i] = f(s + w[wi++]);
        }
        final double[] out2 = new double[n2];
        for (int i = 0; i < n2; i++) {
            double s = 0;
            for (int j = 0; j < n1; j++) {
                s += w[wi++] * out1[j];
            }
            out2[i] = f(s + w[wi++]);
        }
        return out2;
    }


    private void propagateForGradient(final double[] in) {
        int wi = 0;
        double a;
        for (int i = 0; i < n1; i++) {
            double s = 0;
            for (int j = 0; j < n0; j++) {
                s += w[wi++] * in[j];
            }
            a = s + w[wi++];
            y1[i] = f(a);
            y1d[i] = df(a);
        } // wi += n1*(n0+1)

        for (int i = 0; i < n2; i++) {
            double s = 0;
            for (int j = 0; j < n1; j++) {
                s += w[wi++] * y1[j];
            }
            a = s + w[wi++];
            lastNetOut[i] = f(a);
            y2d[i] = df(a);
        } // wi += n2*(n2+1)
    }

    /**
     * Вычисляем 1/2 градиента.
     *
     * @param trainImage текущий образ
     * @return 1/2 градиента
     */
    @Override
    public double[] addToGradient(final NetImage trainImage) {
        try {
            propagateForGradient(trainImage.in);
        } catch (final Exception e) {
            System.out.println("---");
        }
        double d2, d1;
        int w2N0, w2N, w1N0, w1N;
        for (int k = 0; k < n2; ++k) {
            // вычисляем (F(in)-profits)*df(Fs(in)), где Fs - выход нейросети без выходного сигмоида
            d2 = (lastNetOut[k] - trainImage.out[k]) * y2d[k];
            //
            w2N0 = nb1 + (k + 1) * (n1 + 1) - 1;
            dw[w2N0] += d2;
            for (int i = 0; i < n1; ++i) {
                w2N = nb1 + k * (n1 + 1) + i;
                dw[w2N] += d2 * y1[i];
                d1 = d2 * w[w2N] * y1d[i];
                w1N0 = (i + 1) * (n0 + 1) - 1;
                dw[w1N0] += d1;
                for (int j = 0; j < n0; ++j) {
                    w1N = i * (n0 + 1) + j;
                    dw[w1N] += d1 * trainImage.in[j];
                }
            }
        }
        return dw;
    }

    /**
     * Записать конфигурацию и веса нейросети в двоичный поток.
     *
     * @param dos поток.
     */
    @Override
    public void save(final DataOutputStream dos) {
        try {
            dos.writeLong(type.getFormatCode());
            dos.writeInt(n0);
            dos.writeInt(n1);
            dos.writeInt(n2);
            for (final double wi : w) dos.writeDouble(wi);
            if (normalization == null) {
                dos.writeInt(0);
            } else {
                dos.writeInt(1);
                dos.writeInt(normalization.length);
                for (final NeuroMap net : normalization) {
                    net.save(dos);
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    static String t(final double x) {
        return String.format("% 2.4f", x);
    }

    public String netWeightsToString() {
        String s = "";
        int wi = 0;
        s += "\n--- Layer 1 ---\n";
        for (int i = 0; i < n1; i++) {
            s += "y1[" + i + "] = f( ";
            for (int j = 0; j < n0; j++) {
                s += t(w[wi++]) + "*in1[" + j + "] + ";
            }
            s += t(w[wi++]) + ")\n";
        }
        s += "\n--- Layer 2 ---\n";
        for (int i = 0; i < n2; i++) {
            s += "y2[" + i + "] = f( ";
            for (int j = 0; j < n1; j++) {
                s += t(w[wi++]) + "*y1[" + j + "] + ";
            }
            s += t(w[wi++]) + ")\n";
        }
        return s;
    }

    @Override
    public void printNetWeights(final PrintStream log) {
        log.println("formatCode=" + type.getFormatCode());
        log.println(toString());
        log.println(netWeightsToString());
    }

    @Override
    public String toString() {
        return String.format("n0 = %d, n1 = %d, n2 = %d, nw = %d, error = %2.4f",
                n0, n1, n2, getNumWeightsTotal(),
                (relativeError < 10000) ? relativeError / relativeErrorOfAverage : Double.POSITIVE_INFINITY);
    }

}

