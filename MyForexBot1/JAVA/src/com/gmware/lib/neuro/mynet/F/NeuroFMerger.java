package com.gmware.lib.neuro.mynet.F;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Собираем данные из нескольких фалов в один бинарный файл.
 * <p/>
 * Created by Gauss on 21.03.2016.
 */
public class NeuroFMerger {

    static PrintStream summary = null;

    static File[] getFiles(final File inDir) {
        summary.println(" Директория: " + inDir.getAbsolutePath());
        final FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(final File file) {
                return !file.isDirectory();
            }
        };
        final File[] files = inDir.listFiles(filter);
        if (files == null) return files;

        // большие файлы пойдут первыми:
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(final File f1, final File f2) {
                return (int) Math.signum(f2.length() - f1.length());
            }
        });
        // выводим список файлов в лог:
        for (final File file : files) {
            summary.println(file.getName() + " \t" + format(file.length()));
        }
        return files;
    }

    static String format(long x) {
        String s = "";
        int i = 10;
        while (x > 0) {
            s = x % 10 + s;
            x /= 10;
            i--;
        }
        while (i > 0) {
            s += " ";
            i--;
        }
        return s;
    }

    static void mergeFiles(final File inDir, final File result) throws IOException {
        final File[] files = getFiles(inDir);
        final TimedDoubles[] s = NeuroFLoader.loadData(files);
        if (s == null) return;
        if (s.length == 0) return;
        Arrays.sort(s, new Comparator<TimedDoubles>() {
            @Override
            public int compare(final TimedDoubles f1, final TimedDoubles f2) {
                return (int) Math.signum(f1.time - f2.time);
            }
        });
        final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(result)));
        TimedDoubles d;
        d = s[0];
        long j = 1;
        for (int i = 1; i < s.length; ++i) {
            if (d.time < s[i].time) {
                d = s[i];
                ++j;
            }
        }
        dos.writeLong(j);
        summary.println("total size = " + j);
        d = s[0];
        d.save(dos);
        for (int i = 1; i < s.length; ++i) {
            if (d.time < s[i].time) {
                d = s[i];
                d.save(dos);
            }
        }
        dos.close();
    }

    public static void main(final String[] args) throws IOException {
        if (args.length == 3) {
            summary = new PrintStream(new File(args[0]));
            mergeFiles(new File(args[1]), new File(args[2]));
        } else {
            final String a0 = "G:\\F\\test\\t4\\merger.log";
            summary = new PrintStream(new File(a0));
            final String name = "F-2016-1-4";
            final String a1 = "G:\\F\\SUMMON\\" + name;
            final String a2 = "G:\\F\\test\\t4\\log-bin\\" + name + ".bin";
            final File file1 = new File(a1);
            if (file1.exists()) {
                mergeFiles(file1, new File(a2));
            }
/*
            for (int j = 2013; j <= 2016; ++j) {
                for (int i = 1; i <= 4; ++i) {
                    final String period = j + "-" + i;
                    final String a1 = "G:\\F\\EU1-gkfx-Q-" + period;
                    final String a2 = "G:\\F\\test\\t4\\log-bin\\EU1-gkfx-Q-" + period;
                    final File file1 = new File(a1);
                    if (file1.exists()) {
                        mergeFiles(file1, new File(a2));
                    }
                }
            }
*/
        }
    }

}
