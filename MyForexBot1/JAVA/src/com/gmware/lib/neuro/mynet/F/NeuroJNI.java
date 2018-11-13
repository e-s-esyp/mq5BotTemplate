package com.gmware.lib.neuro.mynet.F;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by Gauss on 16.03.2016.
 */
public class NeuroJNI {
    static final String MAP_FILE_NAME = "Z:\\F\\maps\\-19-00-20";
    static NeuroF f = null;
    static PrintStream log = null;

    static {
        try {
            log = new PrintStream("Z:/F/" + System.nanoTime() + "-NeuroJNI.log");
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
        System.setErr(log);
        System.setOut(log);
    }

    public static void init() {
        NeuroF.simulateMode = false;
        f = new NeuroF(MAP_FILE_NAME);
    }

    public static void close() throws IOException {
        NeuroF.printOut("closing");
    }

    /*
    вход:
       a[0] - команда
       a[1] - текущий ордер (0 - sell, 1 - no order, 2 - buy)
       a[2] - размер данных (если a[0]<0)
       a[3].. -данные
    выход:

     */
    public static int getAction(final long[] a) {
        if (a == null) {
            NeuroF.printOut("null data");
            return 0;
        }
        if (a.length == 0) {
            NeuroF.printOut("empty data");
            return 0;
        }
        if (a[0] == -1) {
            NeuroF.printOut("get action " + a[1]);
            try {
                f.propagate(a);
                NeuroF.printOut("f.out = [" + f.out[0] + "," + f.out[1] + "]");
                a[0] = (long) (f.out[0]);
                a[1] = (long) (f.out[1]);
                NeuroF.printOut("a = [" + a[0] + "," + a[1] + "]");
            } catch (final Exception e) {
                System.out.println("Error " + f);
                e.printStackTrace();
            }
            return 2;
        }
        if (a[0] == -2) {
            NeuroF.printOut("set data");
            f.setInitData(a);
            a[0] = 1234;
            a[1] = 2345;
            return 2;
        }
        if (a[0] == -3) {
            NeuroF.printOut("get data");
            return 0;
        }
        if (a[0] == -5) {
            NeuroF.printOut("check data");
            a[0] = 1234;
            a[1] = 2345;
            return 2;
        }
        return 0;
    }

    public static void main(final String[] args) throws IOException {
        init();
        final long[] a = new long[NeuroF.NUM_USED_IN * 5];
        for (int i = 0; i < NeuroF.NUM_USED_IN; ++i) {
            a[5 * i + 2] = 1000010 + i;
            a[5 * i + 3] = 1000000 + i;
        }
        getAction(a);
        close();
    }
}
