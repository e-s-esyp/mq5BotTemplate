package com.gmware.lib.neuro.mynet.F;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Gauss on 07.07.2016.
 */
public class NeuroFAutoC {
    final static int DRAW_TYPE = 1;
    final static int SIZE = 40;
    final static int FULL_SIZE = 600;
    final static String PATH_RESULT = "G:\\F\\test\\PNG-selected" + DRAW_TYPE + "-" + FULL_SIZE;
    final static String TRAIN_NAME_PATH = "G:\\F\\test\\t4\\log-bin\\" + "F-2016-1-7.bin";

    static class Data {
        int i;
        double x;

        Data(final int i, final double x) {
            this.i = i;
            this.x = x;
        }
    }

    static double getC(final double[] data, final int length, final int pos) {
        final int size0 = data.length;
        double s0 = 0;
        for (int j = 0; j < length; ++j) {
            s0 += data[j + pos];
        }
        double s1 = 0;
        int i = 0;
        for (; i < length; i++) {
            s1 += (data[i] * (i + 1)) / length;
        }
        for (i = length; i < size0 - length; i++) {
            s1 += data[i];
        }
        for (i = size0 - length; i < size0; i++) {
            s1 += (data[i] * (size0 - i)) / length;
        }
        final double s2 = s0 * s1;
        double s = 0;
        final int size = data.length - length;
        for (i = 0; i < size; i++) {
            for (int j = 0; j < length; ++j) {
                s += (data[j + i] * data[j + pos]) / s2;
            }
        }
        return s;
    }

    static void auto() {
        final long startNanoTime = System.nanoTime();
        final String pathResult = PATH_RESULT + "-" + SIZE;
        final String trainNamePath = TRAIN_NAME_PATH;
        //--------------------------------------------------------------------------------------------------------------
        final File resultDir = new File(pathResult);
        resultDir.mkdirs();
        final double[] data = loadData(trainNamePath);
        final int length = 30;
        final int size = data.length - length;
        System.out.printf("length=%d size=%d\n", length, size);
        final Data[] res = new Data[size];
        for (int i = 0; i < size; i++) {
            res[i] = new Data(i, getC(data, length, i));
            if (i % 1000 == 0)
                System.out.print("*");
        }
        System.out.println();
        Arrays.sort(res, new Comparator<Data>() {
            @Override
            public int compare(final Data o1, final Data o2) {
                if (o1.x < o2.x) return -1;
                if (o1.x > o2.x) return 1;
                return 0;
            }
        });
        for (int i = 0; i < 20; i++) {
            System.out.printf("pos=%d val=%12.10f\n", res[i].i, res[i].x);
        }
        System.out.println("...");
        for (int i = size - 20; i < size; i++) {
            System.out.printf("pos=%d val=%12.10f\n", res[i].i, res[i].x);
        }
    }

    static void printData(final int start, final int length) {
        final String trainNamePath = TRAIN_NAME_PATH;
        final double[] data = loadData(trainNamePath);
        for (int i = start; i < start + length; ++i) {
            System.out.printf("pos=%d val=%9.7f\n", i, data[i]);
        }
    }

    static double[] loadData(final String trainNamePath) {
        final TimedDoubles[] trainData = NeuroFLoader.loadBinData(new File(trainNamePath));
        final double[] data = new double[trainData.length];
        for (int i = 0; i < trainData.length; ++i) {
            data[i] = ((trainData[i].max + trainData[i].min) / 2);
        }
        return data;
    }

    public static void main(final String[] args) {
//        auto();
        printData(168530 , 30);
    }
}
