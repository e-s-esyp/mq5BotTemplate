package com.gmware.lib.neuro.mynet.F;

import com.gmware.lib.games.holdem.common.Rnd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * ---
 * Created by Gauss on 17.03.2016.
 */

public class TimedDoubles {
    long time = 0;
    double open = 0;
    double max = 0;
    double min = 0;
    double close = 0;
    int spread = 0;

    TimedDoubles(final int year,
                 final int month,
                 final int day,
                 final int hour,
                 final int minute,
                 final double f1,
                 final double f2,
                 final double f3,
                 final double f4,
                 final int f5) {
        time = getMinute(year, month, day, hour, minute);
        open = f1;
        max = f2;
        min = f3;
        close = f4;
        spread = f5;
    }

    TimedDoubles(final DataInputStream dis) throws IOException {
        time = dis.readLong();
        open = dis.readDouble();
        max = dis.readDouble();
        min = dis.readDouble();
        close = dis.readDouble();
        spread = dis.readInt();
    }

    TimedDoubles(final long time,
                 final double open,
                 final double max,
                 final double min,
                 final double close,
                 final int spread) {
        this.time = time;
        this.open = open;
        this.max = max;
        this.min = min;
        this.close = close;
        this.spread = spread;
    }

    @Override
    public String toString() {
        return time + ": " +
                String.format("%7.5f", open) + " " +
                String.format("%7.5f", max) + " " +
                String.format("%7.5f", min) + " " +
                String.format("%7.5f", close) + " " +
                String.format("%7.5f", spread * 0.00001);
    }

    public String toStringFull() {
        return time + "," +
                open + "," +
                max + "," +
                min + "," +
                close + "," +
                spread;
    }

    public void set(final long time,
                    final double open,
                    final double max,
                    final double min,
                    final double close,
                    final int spread) {
        this.time = time;
        this.open = open;
        this.max = max;
        this.min = min;
        this.close = close;
        this.spread = spread;
    }

    public void save(final DataOutputStream dos) throws IOException {
        dos.writeLong(time);
        dos.writeDouble(open);
        dos.writeDouble(max);
        dos.writeDouble(min);
        dos.writeDouble(close);
        dos.writeInt(spread);
    }

    static final long S2000 = (new GregorianCalendar(2000, 1, 1, 0, 0)).getTimeInMillis() / 60000;

    public static long getMinute(final int year, final int month, final int day, final int hour, final int minute) {
        final Calendar c = new GregorianCalendar(year, month, day, hour, minute);
        return (c.getTimeInMillis() / 60000) - S2000;
    }

    public static void randomMix(final TimedDoubles[] trainData, final Rnd rnd) {
        final TimedDoubles[] trainData1 = new TimedDoubles[trainData.length - 1];
        for (int i = 0; i < trainData1.length; ++i) {
            trainData1[i] = new TimedDoubles(
                    0,
                    trainData[i + 1].open - trainData[i + 1].close,
                    trainData[i + 1].max - trainData[i + 1].close,
                    trainData[i + 1].min - trainData[i + 1].close,
                    trainData[i + 1].close - trainData[i].close,
                    trainData[i + 1].spread);
        }
        final int[] subst = getSubst(trainData1.length, rnd);
        for (int i = 0; i < trainData1.length; ++i) {
            final int j = subst[i];
            trainData[i + 1].set(
                    trainData[i + 1].time,
                    trainData[i].close + trainData1[j].close + trainData1[j].open,
                    trainData[i].close + trainData1[j].close + trainData1[j].max,
                    trainData[i].close + trainData1[j].close + trainData1[j].min,
                    trainData[i].close + trainData1[j].close,
                    trainData1[j].spread);
        }
    }

    static int[] getSubst(final int i, final Rnd rnd) {
        final int[] result = new int[i];
        for (int j = 0; j < i; j++) {
            result[j] = j;
        }
        for (int j = 0; j < i * 10; j++) {
            final int i1 = rnd.rnd(i);
            final int i2 = rnd.rnd(i);
            final int i3 = result[i1];
            result[i1] = result[i2];
            result[i2] = i3;
        }
        return result;
    }
}
