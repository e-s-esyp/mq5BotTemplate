package com.gmware.lib.neuro.mynet.Maps;

import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.neuro.mynet.NeuroNet;
import com.gmware.lib.neuro.mynet.NeuroNetStructure;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.mynet.NeuroMap;

import java.io.*;

/**
 * Created by Gauss on 01.12.2015.
 * <p/>
 * Класс, реализующий нейросеть с четыремя слоями узлов и выделенными общими входами.
 */
public class NeuroNet4N extends NeuroNet {

    @Override
    public final void setType() {
        type = NeuroMapType.FourNodeLayers;
    }

    public NeuroNet4N() {
        setType();
    }

    public int getN01() {
        return n01;
    }

    public int getN02() {
        return n02;
    }

    public int getN1() {
        return n1;
    }

    public int getN2() {
        return n2;
    }

    public int getN3() {
        return n3;
    }

    public int getN4() {
        return n4;
    }

    /**
     * Число общих входов.
     */
    private int n01 = 0;
    /**
     * Число входов ситуации.
     */
    private int n02 = 0;
    /**
     * Число нейронов на первом слое.
     */
    private int n1 = 0;
    /**
     * Число нейронов на втором слое.
     */
    private int n2 = 0;
    /**
     * Число нейронов на третьем слое.
     */
    private int n3 = 0;
    /**
     * Число выходов (число нейронов на четвертом слое).
     */
    private int n4 = 0;
    /**
     * для вычисления градиента
     */
    private int nb1 = 0;
    private int nb2 = 0;
    private int nb3 = 0;
    private int nb4 = 0;
    /**
     * Выходы из нейронов первого слоя.
     */
    private double[] y1 = null;
    /**
     * Выходы из нейронов второго слоя.
     */
    private double[] y2 = null;
    /**
     * Выходы из нейронов третьего слоя.
     */
    private double[] y3 = null;
    /**
     * Выходы из нейронов четвертого (внешнего) слоя.
     */
//    private final double[] y4;
    /**
     * Выходы из нейронов первого слоя c производной сигмоида.
     */
    private double[] y1d = null;
    /**
     * Выходы из нейронов второго слоя c производной сигмоида.
     */
    private double[] y2d = null;
    /**
     * Выходы из нейронов третьего слоя c производной сигмоида.
     */
    private double[] y3d = null;
    /**
     * Выходы из нейронов четвертого (внешнего) слоя c производной сигмоида.
     */
    private double[] y4d = null;

    public NeuroNet4N(final NeuroNetStructure structure) {
        super(structure);
        setType();
        this.structure = structure;
        n01 = structure.valence[0][0];
        n02 = structure.valence[0][1];
        n1 = structure.valence[1][0];
        n2 = structure.valence[2][0];
        n3 = structure.valence[3][0];
        n4 = structure.valence[4][0];
        nb1 = (n01 + 1) * n1;
        nb2 = nb1 + (n1 + 1) * n2;
        nb3 = nb2 + (n2 + n02 + 1) * n3;
        nb4 = nb3 + (n3 + 1) * n4;
        y1 = new double[n1];
        y2 = new double[n2];
        y3 = new double[n3];
        lastNetOut = new double[n4];
        lastNetOutD = new double[n4];
        y1d = new double[n1];
        y2d = new double[n2];
        y3d = new double[n3];
        y4d = new double[n4];
        w = new double[getNumWeightsTotal()];
        dw = new double[getNumWeightsTotal()];
    }

    public static NeuroNet4N loadNewNet(final DataInputStream dis) throws IOException {
        final NeuroNet4N net = new NeuroNet4N();
        net.loadBody(dis);
        return net;
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
    public NeuroNet4N(final NeuroNet4N net) {
        type = net.type;
        n01 = net.n01;
        n02 = net.n02;
        n1 = net.n1;
        n2 = net.n2;
        n3 = net.n3;
        n4 = net.n4;
        nb1 = (n01 + 1) * n1;
        nb2 = nb1 + (n1 + 1) * n2;
        nb3 = nb2 + (n2 + n02 + 1) * n3;
        nb4 = nb3 + (n3 + 1) * n4;
        y1 = new double[n1];
        y2 = new double[n2];
        y3 = new double[n3];
        lastNetOut = new double[n4];
        lastNetOutD = new double[n4];
        y1d = new double[n1];
        y2d = new double[n2];
        y3d = new double[n3];
        y4d = new double[n4];
        w = net.w;
        dw = net.dw;
    }

    /**
     * Создать копию.
     *
     * @return копия этого объекта.
     */
    public NeuroNet4N getCopy() {
        return new NeuroNet4N(this);
    }

    /**
     * @return Число общих входов.
     */
    public final int getNumCommonInputs() {
        return n01;
    }

    /**
     * @return Количество весов общей части.
     */
    public final int getNumWeightsCommon() {
        return nb2;
    }

    /**
     * @return общее количество весов.
     */
    public final int getNumWeightsTotal() {
        return nb4;
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
            for (int j = 0; j < n01; j++) {
                s += weights[wi++] * in[j];
            }
            y1[i] = f(s + weights[wi++]);
        }
        for (int i = 0; i < n2; i++) {
            double s = 0;
            for (int j = 0; j < n1; j++) {
                s += weights[wi++] * y1[j];
            }
            y2[i] = f(s + weights[wi++]);
        }
        for (int i = 0; i < n3; i++) {
            double s = 0;
            for (int j = 0; j < n2; j++) {
                s += weights[wi++] * y2[j];
            }
            int k = n01;
            for (int j = 0; j < n02; j++) {
                s += weights[wi++] * in[k++];
            }
            y3[i] = f(s + weights[wi++]);
        }
        for (int i = 0; i < n4; i++) {
            double s = 0;
            for (int j = 0; j < n3; j++) {
                s += weights[wi++] * y3[j];
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
            for (int j = 0; j < n01; j++) {
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
        final double[] out3 = new double[n3];
        for (int i = 0; i < n3; i++) {
            double s = 0;
            for (int j = 0; j < n2; j++) {
                s += w[wi++] * out2[j];
            }
            int k = n01;
            for (int j = 0; j < n02; j++) {
                s += w[wi++] * in[k++];
            }
            out3[i] = f(s + w[wi++]);
        }
        final double[] out4 = new double[n4];
        for (int i = 0; i < n4; i++) {
            double s = 0;
            for (int j = 0; j < n3; j++) {
                s += w[wi++] * out3[j];
            }
            out4[i] = f(s + w[wi++]);
        }
        return out4;
    }


    private void propagateForGradient(final double[] in) {
        int wi = 0;
        double a;
        for (int i = 0; i < n1; i++) {
            double s = 0;
            for (int j = 0; j < n01; j++) {
                s += w[wi++] * in[j];
            }
            a = s + w[wi++];
            y1[i] = f(a);
            y1d[i] = df(a);
        } // wi += n1*(n01+1)

        for (int i = 0; i < n2; i++) {
            double s = 0;
            for (int j = 0; j < n1; j++) {
                s += w[wi++] * y1[j];
            }
            a = s + w[wi++];
            y2[i] = f(a);
            y2d[i] = df(a);
        } // wi += n2*(n2+1)
        for (int i = 0; i < n3; i++) {
            double s = 0;
            for (int j = 0; j < n2; j++) {
                s += w[wi++] * y2[j];
            }
            int k = n01;
            for (int j = 0; j < n02; j++) {
                s += w[wi++] * in[k++];
            }
            a = s + w[wi++];
            y3[i] = f(a);
            y3d[i] = df(a);
        } // wi += n3*(n2+n02+1)
        for (int i = 0; i < n4; i++) {
            double s = 0;
            for (int j = 0; j < n3; j++) {
                s += w[wi++] * y3[j];
            }
            a = s + w[wi++];
            lastNetOut[i] = f(a);
            y4d[i] = df(a);
        } // wi += n4*(n3+1)
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
        double d4, d3, d2, d1;
        int w4N0, w4N, w3N0, w31N, w2N0, w2N, w1N0, w1N, w32N;
        for (int m = 0; m < n4; ++m) {
            // вычисляем (F(in)-profits)*df(Fs(in)), где Fs - выход нейросети без выходного сигмоида
            d4 = (lastNetOut[m] - trainImage.out[m]) * y4d[m];
            //
            w4N0 = nb3 + (m + 1) * (n3 + 1) - 1;
            dw[w4N0] += d4;
            for (int l = 0; l < n3; ++l) {
                w4N = nb3 + m * (n3 + 1) + l;
                dw[w4N] += d4 * y3[l];
                d3 = d4 * w[w4N] * y3d[l];
                w3N0 = nb2 + (l + 1) * (n2 + n02 + 1) - 1;
                dw[w3N0] += d3;
                for (int k = 0; k < n2; ++k) {
                    w31N = nb2 + l * (n2 + n02 + 1) + k;
                    dw[w31N] += d3 * y2[k];
                    d2 = d3 * w[w31N] * y2d[k];
                    w2N0 = nb1 + (k + 1) * (n1 + 1) - 1;
                    dw[w2N0] += d2;
                    for (int i = 0; i < n1; ++i) {
                        w2N = nb1 + k * (n1 + 1) + i;
                        dw[w2N] += d2 * y1[i];
                        d1 = d2 * w[w2N] * y1d[i];
                        w1N0 = (i + 1) * (n01 + 1) - 1;
                        dw[w1N0] += d1;
                        for (int j = 0; j < n01; ++j) {
                            w1N = i * (n01 + 1) + j;
                            dw[w1N] += d1 * trainImage.in[j];
                        }
                    }
                }
                for (int k = 0; k < n02; ++k) {
                    w32N = nb2 + l * (n2 + n02 + 1) + n2 + k;
                    dw[w32N] += d3 * trainImage.in[n01 + k];
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
            dos.writeInt(n01);
            dos.writeInt(n02);
            dos.writeInt(n1);
            dos.writeInt(n2);
            dos.writeInt(n3);
            dos.writeInt(n4);
            for (final double wi : w) dos.writeDouble(wi);
            if (normalization == null) {
                dos.writeBoolean(false);
            } else {
                dos.writeBoolean(true);
                dos.writeInt(normalization.length);
                for (final NeuroMap net : normalization) {
                    net.save(dos);
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public final void loadBody(final DataInputStream dis) throws IOException {
        n01 = dis.readInt();
        n02 = dis.readInt();
        n1 = dis.readInt();
        n2 = dis.readInt();
        n3 = dis.readInt();
        n4 = dis.readInt();
        setNumInOut(n01 + n02, n4);
        structure = new NeuroNetStructure(new int[][]
                {{n01, n02}, {n1}, {n2}, {n3}, {n4}});
        nb1 = (n01 + 1) * n1;
        nb2 = nb1 + (n1 + 1) * n2;
        nb3 = nb2 + (n2 + n02 + 1) * n3;
        nb4 = nb3 + (n3 + 1) * n4;
        y1 = new double[n1];
        y2 = new double[n2];
        y3 = new double[n3];
        lastNetOut = new double[n4];
        lastNetOutD = new double[n4];
        y1d = new double[n1];
        y2d = new double[n2];
        y3d = new double[n3];
        y4d = new double[n4];
        w = new double[getNumWeightsTotal()];
        dw = new double[getNumWeightsTotal()];
        for (int i = 0; i < w.length; i++) {
            final double wi = dis.readDouble();
            if (Double.isNaN(wi)) {
                exception("NaN weight.");
            }
            w[i] = wi;
        }
        if (dis.readBoolean()) {
            final int numInns = dis.readInt();
            normalization = new NeuroMap[numInns];
            for (int i = 0; i < numInns; ++i) {
                normalization[i] = load(dis);
            }
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
            for (int j = 0; j < n01; j++) {
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
        s += "\n--- Layer 3 ---\n";
        for (int i = 0; i < n3; i++) {
            s += "y3[" + i + "] = f( ";
            for (int j = 0; j < n2; j++) {
                s += t(w[wi++]) + "*y2[" + j + "] + ";
            }
            for (int j = 0; j < n02; j++) {
                s += t(w[wi++]) + "*in2[" + j + "] + ";
            }
            s += t(w[wi++]) + ")\n";
        }
        s += "\n--- Layer 4 ---\n";
        for (int i = 0; i < n4; i++) {
            s += "profits[" + i + "] = f( ";
            for (int j = 0; j < n3; j++) {
                s += t(w[wi++]) + "*y3[" + j + "] + ";
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
        return String.format("n01 = %d, n02 = %d, n1 = %d, n2 = %d, n3 = %d, n4 = %d, nw = %d, rrError = %2.6f",
                n01, n02, n1, n2, n3, n4, getNumWeightsTotal(), rrError);
    }

}

