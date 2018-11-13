package com.gmware.lib.neuro.mynet;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.games.holdem.common.rnd.Rnd517;

import java.io.PrintStream;

/**
 * нейросеть
 * Created by Gauss on 10.12.2015.
 */
public abstract class NeuroNet extends NeuroMap {

    public int numErrors = 0;
    /**
     * Массив весов.<br>
     * Вес связи между j-м нейроном первого слоя и k-м нейроном входного слоя имеет индекс j * (n0 + 1) + k.<br>
     * Вес связи между i-м нейроном второго слоя и j-м нейроном внутреннего слоя имеет индекс (n0 + 1) * n1 + i * (n1 + 1) + j.
     */
    public NeuroNetStructure structure = null;
    public NeuroMap[] normalization = null;

    /**
     * Градиент.
     */
    protected double[] dw = null;
    /**
     * последний шаг градиента.
     */
    public double step = 0;
    public double up = 0;       //TODO:!neuro! отладочная информация
    public double down = 0;     //TODO:!neuro! отладочная информация

    public NetImage[] trainImages = null;
    public NetImage[] testImages = null;
    protected double relativeError = Double.MAX_VALUE;
    protected double relativeErrorOfAverage = Double.MAX_VALUE;
    public boolean stopTraining = false;
    public double strength = 0;

    Rnd rnd = null;

    public NeuroNet() {

    }

    public NeuroNet(final NeuroNetStructure structure) {
        numIns = 0;
        int[] s = structure.valence[0];
        for (final int n : s) {
            numIns += n;
        }
        numOuts = 0;
        s = structure.valence[structure.valence.length - 1];
        for (final int n : s) {
            numOuts += n;
        }
    }

    public void setNumInOut(final int numIns, final int numOuts) {
        this.numIns = numIns;
        this.numOuts = numOuts;
    }

    /**
     * @return Число входов.
     */
    public int getNumInputs() {
        return numIns;
    }

    /**
     * @return число выходов.
     */
    public final int getNumOutputs() {
        return numOuts;
    }

    public void makeAverageNet() {
        for (int i = 0; i < w.length; ++i) {
            w[i] = 0;
        }
//        final int average = w.length - averageOut.length;
        final int[] ns = structure.valence[structure.valence.length - 2];
        int n = 0; //число входов в последний слой
        for (final int ni : ns) {
            n += ni;
        }
        n++;
        final int n1 = w.length - numOuts * n + n - 1;
        for (int i = 0; i < numOuts; ++i) {
            w[n1 + i * n] = finv(averageOut[i]);
        }
    }

    protected abstract void makeSampleNet(double a);

    protected void makeSpecialRandomParameters() {
    }

    public static void randomize(final NeuroNet net, final Rnd rnd) {
        net.relativeError = Double.MAX_VALUE;
        net.relativeErrorOfAverage = Double.MAX_VALUE;
        net.stopTraining = false;
        while (net.relativeErrorOfAverage > 1000000) {
            for (int i = 0; i < net.w.length; ++i) {
                net.w[i] = rnd.rnd() * 2 - 1;
            }
            net.makeSpecialRandomParameters();
            net.setStaticParameters();
        }
    }

    protected abstract double[] propagate(final double[] weights, final double[] in, final double[] out);

    @Override
    public double[] propagate(final double[] in) {
        final double[] inShift;
        if (shift == null) {
            inShift = in;
        } else {
            inShift = shift.map(in);
        }
        if (normalization == null) {
            return propagate(w, inShift, lastNetOut);
        } else {
            final double[] inNorm = new double[inShift.length];
            for (int i = 0; i < inShift.length; ++i) {
                inNorm[i] = normalization[i].propagate(new double[]{inShift[i]})[0];
            }
            return propagate(w, inNorm, lastNetOut);
        }
    }

    public abstract double[] addToGradient(final NetImage trainImage);

    double trnVar = Double.MAX_VALUE;
    double tstVar = Double.MAX_VALUE;
    double trnEA = Double.MAX_VALUE;
    double tstEA = Double.MAX_VALUE;
    double trnREA = Double.MAX_VALUE;
    double tstREA = Double.MAX_VALUE;

    public void setStaticParameters() {
        setAverageOut();
        trnVar = getVariance(trainImages);
        tstVar = getVariance(testImages);
        trnEA = getErrorOfAverage(averageOut, trainImages);
        tstEA = getErrorOfAverage(averageOut, testImages);
        trnREA = trnVar > 0.0 ? trnEA / trnVar : 1.0;
        tstREA = tstVar > 0.0 ? tstEA / tstVar : 1.0;
        relativeErrorOfAverage = Math.max(trnREA, tstREA);
//        relativeErrorOfAverage = trnEA;//TODO: !neuro!test вернуть относительную ошибку
    }

    private void setAverageOut() {
        averageOut = new double[numOuts];
        double d;
        for (int i = 0; i < numOuts; ++i) {
            d = 0;
            for (final NetImage image : trainImages) {
                d += image.out[i];
            }
            for (final NetImage image : testImages) {
                d += image.out[i];
            }
            averageOut[i] = d / (trainImages.length + testImages.length);
        }
    }

    public void setRelativeErrors() {
        final double trnE = getErrorQuadratic(trainImages);
        final double tstE = getErrorQuadratic(testImages);
        final double trnRE = trnVar > 0.0 ? trnE / trnVar : 1.0;
        final double tstRE = tstVar > 0.0 ? tstE / tstVar : 1.0;
//        final double newRelativeError = trnE;//TODO: !neuro!test вернуть относительную ошибку
        final double newRelativeError = Math.max(trnRE, tstRE);
        final double newRrError = (relativeErrorOfAverage > 0) ? newRelativeError / relativeErrorOfAverage : newRelativeError;
        stopTraining = (newRrError >= rrError);
        if (!stopTraining) {
            relativeError = newRelativeError;
            rrError = newRrError;
        }
    }

    public void setRelativeErrorsAndBack() {
        setRelativeErrors();
        if (stopTraining) {
            makeBackStep();
            step /= 2;
            stopTraining = false;
            numErrors++;
        } else {
            numErrors = 0;
        }
    }

    public void setRelativeErrorsAndBack(final int indNeuron) {
        setRelativeErrors();
        if (stopTraining) {
            makeBackStep(indNeuron);
            step /= 2;
            stopTraining = false;
            numErrors++;
        } else {
            numErrors = 0;
        }
    }

    public double getLastGradientValue() {
        double s = 0;
        for (final double x : dw) {
            s += x * x;
        }
        return Math.sqrt(s);
    }

    /**
     * Запись в текстовом виде.
     *
     * @param log поток
     */
    public abstract void printNetWeights(PrintStream log);

    /**
     * Обнуление градиента.
     */
    public void clearGradient() {
        for (int i = dw.length - 1; i >= 0; i--) {
            dw[i] = 0;
        }
    }

    /**
     * Прибавить веса градиента к весам нейросетей с коэффициентом step.
     */
    public void addGradientToWeights() {
        for (int i = w.length - 1; i >= 0; i--) {
            w[i] += step * dw[i];
        }
    }

    /**
     * Прибавить веса градиента к весам одного нейрона нейросетей с коэффициентом step.
     */
    public void addGradientToWeights(final int indNeuron) {
        for (int i = w.length - 1; i >= 0; i--) {
            w[i] += step * dw[i];
        }
    }

    /**
     * Вычесть веса градиента из весов нейросетей с коэффициентом step.
     */
    public void makeBackStep() {
        for (int i = w.length - 1; i >= 0; i--) {
            w[i] -= step * dw[i];
        }
    }

    /**
     * Вычесть веса градиента из весов нейросетей с коэффициентом step.
     */
    public void makeBackStep(final int indNeuron) {
        for (int i = w.length - 1; i >= 0; i--) {
            w[i] -= step * dw[i];
        }
    }

    public void pushRandomNeuron() {
        if (rnd == null) {
            rnd = new Rnd517();
        }
//...
    }

    public static double A = 1;

    static private final int F_NUM_SEG = 10000;
    static private final double F_LIMIT = 25.0;
    static private final double F_MULTIPLIER = F_NUM_SEG / F_LIMIT;
    static private final double[] a0 = new double[F_NUM_SEG];
    static private final double[] a1 = new double[F_NUM_SEG];
    static private final double[] a2 = new double[F_NUM_SEG];

    static private final int DF_NUM_SEG = 10000;
    static private final double DF_LIMIT = 25.0;
    static private final double DF_MULTIPLIER = DF_NUM_SEG / DF_LIMIT;
    static private final double[] ad0 = new double[DF_NUM_SEG];
    static private final double[] ad1 = new double[DF_NUM_SEG];
    static private final double[] ad2 = new double[DF_NUM_SEG];

    static {
        for (int i = 0; i < F_NUM_SEG; ++i) {
            final double x0 = i / F_MULTIPLIER;
            final double b0 = strictF(x0);
            final double b1 = strictDF(x0);
            final double b2 = strictD2F(x0) / 2;
            a0[i] = b0 + (-b1 + b2 * x0) * x0;
            a1[i] = b1 - 2 * b2 * x0;
            a2[i] = b2;
        }
        for (int i = 0; i < DF_NUM_SEG; ++i) {
            final double x0 = i / DF_MULTIPLIER;
            final double b0 = strictDF(x0);
            final double b1 = strictD2F(x0);
            final double b2 = strictD3F(x0) / 2;
            ad0[i] = b0 + (-b1 + b2 * x0) * x0;
            ad1[i] = b1 - 2 * b2 * x0;
            ad2[i] = b2;
        }
    }

    /**
     * Сигмоид.
     * Разложение в ряд тейлора, ограничено 2й степенью.
     *
     * @param x аргумент.
     * @return значение сигмоида.
     */
    public static double f(final double x) {
        final int sign;
        final double x1;
        if (x < 0) {
            x1 = -x;
            sign = -1;
        } else {
            x1 = x;
            sign = 1;
        }
        final int ind = (int) (x1 * F_MULTIPLIER + 0.5d);
        if (ind < F_NUM_SEG) {
            return sign * (a0[ind] + (a1[ind] + (a2[ind] * x1)) * x1);
        } else {
            return (sign == 1) ? 0.5 : -0.5;
        }
    }

    /**
     * Сигмоид.
     *
     * @param x аргумент.
     * @return значение сигмоида.
     */
    protected static double strictF(double x) {
        x *= A;
        return -0.5 + 1.0 / (1.0 + StrictMath.exp(-x));
    }

    static void printF(final NeuroNet net, final PrintStream log) {
        log.println("-- Sigmoid --");
        log.println("x\t f(x)");
        for (int i = 0; i < 100; ++i) {
            double x = i;
            x = x / 10 - 5;
            log.println(t(x) + "  " + t(NeuroNet.f(x)));
        }
    }

    static String t(final double x) {
        return String.format("%01.5f", x);
    }

    /**
     * Функция, обратная к сигмоиду.
     *
     * @param y аргумент
     * @return значение
     */
    protected static double finv(final double y) {
        return Math.log((1 + 2 * y) / (1 - 2 * y)) / A;
    }

    /**
     * Вычислить производную сигмоида.
     * Разложение в ряд тейлора, ограничено 2й степенью.
     *
     * @param x значение сигмоида.
     * @return значение производной сигмоида.
     */
    protected static double df(final double x) {
        final double x1;
        if (x < 0) {
            x1 = -x;
        } else {
            x1 = x;
        }
        final int ind = (int) (x1 * DF_MULTIPLIER + 0.5d);
        if (ind < DF_NUM_SEG) {
            return (ad0[ind] + (ad1[ind] + (ad2[ind] * x1)) * x1);
        } else {
            return 0;
        }
    }

    /**
     * Вычислить производную сигмоида.
     *
     * @param x значение сигмоида.
     * @return значение производной сигмоида.
     */
    protected static double strictDF(double x) {
        x *= A;
        final double y1 = StrictMath.exp(-Math.abs(x));
        final double y2 = 1 + y1;
        return (A * y1 / y2) / y2;
    }

    /**
     * Вычислить вторую производную сигмоида.
     *
     * @param x значение сигмоида.
     * @return значение второй производной сигмоида.
     */
    protected static double strictD2F(double x) {
        x *= A;
        final double y1 = StrictMath.exp(-Math.abs(x));
        final double y2 = y1 - 1;
        final double y3 = y1 + 1;
        return (((A * A * y1 * y2) / y3) / y3) / y3;
    }

    /**
     * Вычислить вторую производную сигмоида.
     *
     * @param x значение сигмоида.
     * @return значение второй производной сигмоида.
     */
    protected static double strictD3F(double x) {
        x *= A;
        final double y1 = StrictMath.exp(-Math.abs(x));
        final double y2 = y1 - 1;
        final double y3 = y1 + 1;
        return ((((A * A * A * y1 * (y2 * y2 - 2 * y1)) / y3) / y3) / y3) / y3;
    }

    //TODO: требует оптимизации
    protected static double f(final double a, final double b, final double x) {
        if (x < 0) {
            if (a == 0) {
                return 0;
            }
            final double c = (a < 0) ? -a : a;
            return -0.5 + 1 / (1 + Math.pow(1 + ((-4 * x) / c), c));
        } else {
            if (b == 0) {
                return 0;
            }
            final double c = (b < 0) ? -b : b;
            return 0.5 - 1 / (1 + Math.pow(1 + ((4 * x) / c), c));
        }
    }

    //TODO: требует оптимизации
    protected static double dfx(final double a, final double b, final double x) {
        final double x1;
        final double c;
        if (x < 0) {
            if (a == 0) return 0;
            x1 = -x;
            c = (a < 0) ? -a : a;
        } else {
            if (b == 0) return 0;
            x1 = x;
            c = (b < 0) ? -b : b;
        }
        final double d = ((4 * x1) / c) + 1;
        final double da = Math.pow(d, c);
        return (4.0 * da) / (Math.pow(1 + da, 2) * d);
    }

    //TODO: требует оптимизации
    protected static double dfa(final double a, final double b, final double x) {
        final double x1;
        final double c;
        final double s;
        if (x < 0) {
            if (a == 0) return 0;
            x1 = -x;
            c = (a < 0) ? -a : a;
            s = -1;
        } else {
            if (b == 0) return 0;
            x1 = x;
            c = (b < 0) ? -b : b;
            s = 1;
        }
        final double d = ((4 * x1) / c) + 1;
        final double da = Math.pow(d, c);
        return (s * da * (Math.log(d) - ((4.0 * x1) / (c * d)))) / Math.pow(1 + da, 2);
    }

    double[] lastImageOut = null;
    protected double[] lastNetOut = null;
    protected double[] lastNetOutD = null;

    /**
     * Формируем lastOut, lastOutD
     *
     * @param trainImage обучающий образ
     */
    public void propagateForStep(final NetImage trainImage) {
        lastImageOut = trainImage.out;
        propagate(w, trainImage.in, lastNetOut);
        propagate(dw, trainImage.in, lastNetOutD);
    }

    /**
     * SUM{ (out-W(in))*DW(in) }
     * перед этой функцией должна быть выполнена propagateForStep(NetImage trainImage);
     *
     * @param trainImage обучающий образ
     * @return сумма по выходам
     */
    public double addStepUp(final NetImage trainImage) {
        double r = 0;
        for (int i = 0; i < lastImageOut.length; ++i) {
            r += (lastImageOut[i] - lastNetOut[i]) * lastNetOutD[i];
        }
        return r;
    }

    /**
     * SUM{ DW(in)*DW(in) }
     * перед этой функцией должна быть выполнена propagateForStep(NetImage trainImage);
     *
     * @param trainImage обучающий образ
     * @return сумма по выходам
     */
    public double addStepDown(final NetImage trainImage) {
        double r = 0;
        for (int i = 0; i < lastImageOut.length; ++i) {
            r += lastNetOutD[i] * lastNetOutD[i];
        }
        return r;
    }

}
