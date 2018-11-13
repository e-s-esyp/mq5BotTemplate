package com.gmware.lib.neuro.mynet.F;

import java.io.*;

/**
 * Created by Gauss on 21.03.2016.
 */
public class NeuroFLoader {

    static TimedDoubles[] loadBinData(final File file) {
        final DataInputStream dis;
        TimedDoubles[] d = null;
        try {
            dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            final int length = (int) dis.readLong();
            d = new TimedDoubles[length];
            for (int i = 0; i < length; ++i) {
                d[i] = new TimedDoubles(dis);
            }
        } catch (final IOException ignored) {
        }
        return d;
    }

    static class FileInt {
        int[] data = null;
        int head = 0;

        FileInt(final int length) {
            data = new int[length];
        }

        public boolean eof() {
            return head >= data.length;
        }

        public void reset() {
            head = 0;
        }

        public void write(final int c) {
            data[head++] = c;
        }

        public int read() {
            return data[head++];
        }
    }

    static TimedDoubles[] buffer = new TimedDoubles[20000000];

    static TimedDoubles[] loadData(final File file) {
        final DataInputStream dis;
        int i = 0;
        try {
            dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            while (true) {
                if ((buffer[i] = loadRow(dis)) == null) break;
                ++i;
            }
        } catch (final IOException ignored) {
        }
        final TimedDoubles[] d = new TimedDoubles[i];
        System.arraycopy(buffer, 0, d, 0, i);
        return d;
    }

    static TimedDoubles[] loadData(final File[] files) {
        DataInputStream dis;
        int i = 0;
        for (final File file : files) {
            try {
                dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                while (true) {
                    buffer[i] = loadRow(dis);
                    if ((buffer[i]) == null) break;
                    ++i;
                }
            } catch (final IOException ignored) {
            }
        }
        final TimedDoubles[] d = new TimedDoubles[i];
        System.arraycopy(buffer, 0, d, 0, i);
        return d;
    }

    public static TimedDoubles loadRow(final DataInputStream dis) throws IOException {
        final FileInt buf = new FileInt(100);
        int c;
        loop:
        while (true) {
            switch (c = dis.read()) {
                case -1:
                    return null;
                case '\n':
                    break loop;
                default:
                    if (!buf.eof()) {
                        buf.write(c);
                    } else {
                        break loop;
                    }
                    break;
            }
        }
        buf.reset();
        final int year = loadInt(buf, '.', '-');
        final int month = loadInt(buf, '.', '-');
        final int day = loadInt(buf, ',', ' ');
        final int hour = loadInt(buf, ':');
        final int minute = loadInt(buf, ',');
        final double f1 = loadFloat(buf, '.', ',');
        final double f2 = loadFloat(buf, '.', ',');
        final double f3 = loadFloat(buf, '.', ',');
        final double f4 = loadFloat(buf, '.', ',');
        final int f5 = loadInt(buf);
        return new TimedDoubles(year, month, day, hour, minute, f1, f2, f3, f4, f5);
    }

    private static double loadFloat(final FileInt buf, final char c1, final char c2) {
        final double v1 = loadInt(buf, c1);
        int i = buf.head;
        final double v2 = loadInt(buf, c2);
        i = buf.head - i - 1;
        if (i > 0) {
            return v1 + v2 / Math.pow(10, i);
        } else {
            return v1;
        }
    }

    static int loadInt(final FileInt buf, final int c) {
        int v = 0;
        while (!buf.eof()) {
            final int b = buf.read();
            if (b == c || b == '\n') {
                break;
            }
            if (b >= '0' && b <= '9') {
                v = v * 10 + b - '0';
            }
        }
        return v;
    }

    static int loadInt(final FileInt buf, final int c1, final int c2) {
        int v = 0;
        while (!buf.eof()) {
            final int b = buf.read();
            if (b == c1 || b == c2 || b == '\n') {
                break;
            }
            if (b >= '0' && b <= '9') {
                v = v * 10 + b - '0';
            }
        }
        return v;
    }

    static int loadInt(final FileInt buf) {
        int v = 0;
        while (!buf.eof()) {
            final int b = buf.read();
            if (b == '\r' || b == '\n') {
                break;
            }
            if (b >= '0' && b <= '9') {
                v = v * 10 + b - '0';
            }
        }
        return v;
    }

}
