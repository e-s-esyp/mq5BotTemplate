package com.gmware.lib.neuro.mynet.Maps;

/**
 * Двухслойные нейросети без выходного сигмоида.
 * Created by Gauss on 25.01.2016.
 */

import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.mynet.NeuroNetStructure;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.neuro.mynet.NeuroNet;

import java.io.*;


public class NeuroNet1p5N extends NeuroNet {

    @Override
    public final void setType() {
        type = NeuroMapType.SumOfSigmoids;
    }

    public NeuroNet1p5N() {
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
     * число весов нейросети по слоям с накоплением
     */
    private int nb1 = 0, nb2 = 0;
    /**
     * Выходы из нейронов первого слоя.
     */
    private double[] y1 = null;
    /**
     * Выходы из нейронов первого слоя без сигмоида.
     */
    private double[] y1x = null;
    /**
     * Выходы из нейронов первого слоя с производной сигмоида.
     */
    private double[] y1d = null;

    private void init() {
        nb1 = (n0 + 3) * n1;
        nb2 = nb1 + (n1 + 1) * n2;
        y1 = new double[n1];
        lastNetOut = new double[n2];
        lastNetOutD = new double[n2];
        y1x = new double[n1];
        y1d = new double[n1];
        w = new double[nb2];
        dw = new double[nb2];
    }


    double[] getSigmoidParameters() {
        final double[] v = new double[n1 * 2];
        int k = 0;
        for (int i = 0; i < n1; ++i) {
            final int w1N0 = (i + 1) * (n0 + 3) - 3;
            v[k++] = w[w1N0 + 1];
            v[k++] = w[w1N0 + 2];
        }
        return v;
    }

    public String getSigmoidParametersAsSting() {
        String s = "";
        for (int i = 0; i < n1; ++i) {
            final int w1N0 = (i + 1) * (n0 + 3) - 3;
            s += "[ " + t(w[w1N0 + 1]) + ", " + t(w[w1N0 + 2]) + "] ";
        }
        return s;
    }

    void setSigmoidParameters(final double[] v) {
        int k = 0;
        for (int i = 0; i < n1; ++i) {
            final int w1N0 = (i + 1) * (n0 + 3) - 3;
            w[w1N0 + 1] = v[k++];
            w[w1N0 + 2] = v[k++];
        }
    }

    public NeuroNet1p5N(final NeuroNetStructure structure) {
        super(structure);
        setType();
        this.structure = structure;
        n0 = structure.valence[0][0];
        n1 = structure.valence[1][0];
        n2 = structure.valence[2][0];
        init();
    }

    public static NeuroNet1p5N loadNewNet(final DataInputStream dis) throws IOException {
        final NeuroNet1p5N net = new NeuroNet1p5N();
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
        init();
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

    @Override
    public void makeSpecialRandomParameters() {
        for (int i = 0; i < n1; ++i) {
            final int w1N0 = (i + 1) * (n0 + 3) - 3;
            w[w1N0 + 1] = StrictMath.exp(w[w1N0 + 1] * 5);
            w[w1N0 + 2] = StrictMath.exp(w[w1N0 + 2] * 5);
        }

    }

    /**
     * Конструктор копирования.
     *
     * @param net нейросеть.
     */
    public NeuroNet1p5N(final NeuroNet1p5N net) {
        type = net.type;
        n0 = net.n0;
        n1 = net.n1;
        n2 = net.n2;
        nb1 = net.nb1;
        nb2 = net.nb2;
        y1 = new double[n1];
        System.arraycopy(net.y1, 0, y1, 0, y1.length);
        lastNetOut = new double[n2];
        System.arraycopy(net.lastNetOut, 0, lastNetOut, 0, lastNetOut.length);
        lastNetOutD = new double[n2];
        System.arraycopy(net.lastNetOutD, 0, lastNetOutD, 0, lastNetOutD.length);
        y1x = new double[n1];
        System.arraycopy(net.y1x, 0, y1x, 0, y1x.length);
        y1d = new double[n1];
        System.arraycopy(net.y1d, 0, y1d, 0, y1d.length);
        w = new double[nb2];
        System.arraycopy(net.w, 0, w, 0, w.length);
        dw = new double[nb2];
        System.arraycopy(net.dw, 0, dw, 0, dw.length);
    }

    /**
     * Создать копию.
     *
     * @return копия этого объекта.
     */
    public NeuroNet1p5N getCopy() {
        return new NeuroNet1p5N(this);
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
            s += weights[wi++];
            final double a = wi++;
            final double b = wi++;
            y1[i] = f(a, b, s);
        }
        for (int i = 0; i < n2; i++) {
            double s = 0;
            for (int j = 0; j < n1; j++) {
                s += weights[wi++] * y1[j];
            }
            out[i] = (s + weights[wi++]);
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
            s += w[wi++];
            final double a = wi++;
            final double b = wi++;
            out1[i] = f(a, b, s);
        }
        final double[] out2 = new double[n2];
        for (int i = 0; i < n2; i++) {
            double s = 0;
            for (int j = 0; j < n1; j++) {
                s += w[wi++] * out1[j];
            }
            out2[i] = (s + w[wi++]);
        }
        return out2;
    }


    private void propagateForGradient(final double[] in) {
        int wi = 0;
        double v;
        for (int i = 0; i < n1; i++) {
            double s = 0;
            for (int j = 0; j < n0; j++) {
                s += w[wi++] * in[j];
            }
            v = s + w[wi++];
            final double a = wi++;
            final double b = wi++;
            y1x[i] = v;
            y1[i] = f(a, b, v);
            y1d[i] = dfx(a, b, v);
        } // wi += n1*(n0+1)
        for (int i = 0; i < n2; i++) {
            double s = 0;
            for (int j = 0; j < n1; j++) {
                s += w[wi++] * y1[j];
            }
            v = s + w[wi++];
            lastNetOut[i] = v;
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
            // вычисляем (F(in)-profits)
            d2 = (lastNetOut[k] - trainImage.out[k]);
            //
            w2N0 = nb1 + (k + 1) * (n1 + 1) - 1;
            dw[w2N0] += d2;
            for (int i = 0; i < n1; ++i) {
                w2N = nb1 + k * (n1 + 1) + i;
                dw[w2N] += d2 * y1[i];
                d1 = d2 * w[w2N] * y1d[i];
                w1N0 = (i + 1) * (n0 + 3) - 3;
                dw[w1N0] += d1;
                if (y1x[i] < 0) {
                    dw[w1N0 + 1] += d2 * w[w2N] * dfa(w[w1N0 + 1], 0, y1x[i]);
                } else {
                    dw[w1N0 + 2] += d2 * w[w2N] * dfa(0, w[w1N0 + 2], y1x[i]);
                }
                for (int j = 0; j < n0; ++j) {
                    w1N = i * (n0 + 3) + j;
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
            s += t(w[wi++]) + ", [" + t(w[wi++]) + ", " + t(w[wi++]) + "])\n";
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

    /**
     * Прибавить веса градиента к весам одного нейрона нейросетей с коэффициентом step.
     */
    @Override
    public void addGradientToWeights(final int indNeuron) {
        if (indNeuron < n1) {
            //--- Layer 1 ---
            final int first = (n0 + 3) * indNeuron;
            final int next = first + n0 + 3;
            for (int i = first; i < next; i++) {
                w[i] += step * dw[i];
            }
            //--- Layer 2 ---
            w[nb1 + indNeuron] += step * dw[nb1 + indNeuron];
            w[nb2 - 1] += step * dw[nb2 - 1];
        } else {
            for (int i = nb1; i < nb2; i++) {
                w[i] += step * dw[i];
            }
        }
    }

    /**
     * Вычесть веса градиента из весов нейросетей с коэффициентом step.
     */
    @Override
    public void makeBackStep(final int indNeuron) {
        if (indNeuron < n1) {
            //--- Layer 1 ---
            final int first = (n0 + 3) * indNeuron;
            final int next = first + n0 + 3;
            for (int i = first; i < next; i++) {
                w[i] -= step * dw[i];
            }
            //--- Layer 2 ---
            w[nb1 + indNeuron] += step * dw[nb1 + indNeuron];
            w[nb2 - 1] -= step * dw[nb2 - 1];
        } else {
            for (int i = nb1; i < nb2; i++) {
                w[i] -= step * dw[i];
            }
        }
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
                (rrError < 10000) ? rrError : Double.POSITIVE_INFINITY);
    }

}

