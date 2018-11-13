package com.gmware.lib.neuro.mynet.F;

import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.mynet.Mappings.NeuroMappingPartitionByCorrelation2;
import com.gmware.lib.neuro.mynet.NeuroMap;
import com.gmware.lib.neuro.mynet.NeuroMapType;
import com.gmware.lib.games.holdem.common.rnd.Rnd517;

import java.io.*;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Gauss on 09.03.2016.
 */
public class NeuroImageLoader {

    static final long S2000 = (new GregorianCalendar(2000, 1, 1, 0, 0)).getTimeInMillis() / 60000;
    static final int NUM_USED = 121;

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

    public static class TimedDouble {
        long time = 0;
        double value1 = 0;
        double value2 = 0;

        TimedDouble(final long time, final double value1, final double value2) {
            this.time = time;
            this.value1 = value1;
            this.value2 = value2;
        }

        @Override
        public String toString() {
            return "(" + time + ", " + value1 + ", " + value2 + ")";
        }

    }

    public static TimedDouble loadRow(final DataInputStream dis) throws IOException {
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
        final int year = loadInt(buf, '.');
        final int month = loadInt(buf, '.');
        final int day = loadInt(buf, ',');
        final int hour = loadInt(buf, ':');
        final int minute = loadInt(buf, ',');
        loadFloat(buf, '.', ',');
        final double f2 = loadFloat(buf, '.', ',');
        final double f3 = loadFloat(buf, '.', ',');
        loadFloat(buf, '.', ',');
        return new TimedDouble(getMinute(year, month, day, hour, minute), f2, f3);
    }

    private static long getMinute(final int year, final int month, final int day, final int hour, final int minute) {
        final Calendar c = new GregorianCalendar(year, month, day, hour, minute);
        return (c.getTimeInMillis() / 60000) - S2000;
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

    static TimedDouble[] buffer = new TimedDouble[20000000];

    static TimedDouble[] loadData(final String fileName) {
        final File file = new File(fileName);
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
        final TimedDouble[] d = new TimedDouble[i];
        System.arraycopy(buffer, 0, d, 0, i);
        return d;
    }

    private static int getNumImages(final TimedDouble[] d) {
        int j = 0;
        int n = 2;
        for (int i = 1; i < d.length; ++i) {
            if (d[i - 1].time + 1 != d[i].time) {
                if (n >= NUM_USED) {
                    j += n - NUM_USED;
                }
                n = 1;
            }
            ++n;
        }
        if (n >= NUM_USED) {
            j += n - NUM_USED;
        }
        return j;
    }

    private static void fillImages(final NetImage[] images, final TimedDouble[] d) {
        int n = 0;
        int j = 0;
        while (j + NUM_USED <= d.length) {
            for (int i = 1; i < NUM_USED && i + j < d.length; ++i) {
                if (d[j + i - 1].time + 1 != d[j + i].time) {
                    j += i;
                    i = 0;
                }
            }
            while (j + NUM_USED <= d.length) {
                if (d[j + NUM_USED - 2].time + 1 != d[j + NUM_USED - 1].time) {
                    break;
                }
                final int num2 = NUM_USED / 2;
                final NetImage image = new NetImage(2 * num2, 2);
                int k = 0;
                for (; k < num2; ++k) {
                    image.in[2 * k] = 1000 * Math.log(d[j + k + 1].value1 / d[j + k].value1);
                    image.in[2 * k + 1] = 1000 * Math.log(d[j + k + 1].value2 / d[j + k].value2);
                }
                double max = -1000;
                double min = 1000;
                for (k = num2 + 1; k < NUM_USED; ++k) {
                    final double v1 = 1000 * Math.log(d[j + k].value1 / d[j + num2].value1);
                    final double v2 = 1000 * Math.log(d[j + k].value2 / d[j + num2].value2);
                    if (max < v1) max = v1;
                    if (min > v2) min = v2;
                }
                image.out[0] = max;
                image.out[1] = min;
                images[n++] = image;
                ++j;
            }
        }
    }

    private static NetImage[] getImages(final TimedDouble[] d) {
        final NetImage[] images = new NetImage[getNumImages(d)];
        System.out.println("images.length = " + images.length);
        fillImages(images, d);
        return images;
    }

    public static void main(final String[] args) throws IOException {
        final String workFileName = args[0];
//        final String workFileName = "D:\\F\\архив\\test.csv";
        final File workFile = new File(workFileName);
        final String solver = workFile.getParent() + "\\SOLVER2";
        final File resultDir = new File(solver + "\\RESULT");
        final File logDir = new File(solver + "\\LOG");
        resultDir.mkdirs();
        logDir.mkdirs();
        final TimedDouble[] d = loadData(workFileName);
        final NetImage[] images = getImages(d);
        final NeuroMap map = NeuroMappingPartitionByCorrelation2.train(
                images,
                NeuroMapType.PartitionByCorrelation2,
                new Rnd517(),
                new PrintStream(new File(logDir.getAbsolutePath() + "\\" + workFile.getName() + ".log")),
                "NeuroImageLoader");
        final DataOutputStream dosNet = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(resultDir.getAbsolutePath() + "\\" + workFile.getName() + ".net")));
        map.save(dosNet);
        dosNet.flush();
        dosNet.close();

    }

}
