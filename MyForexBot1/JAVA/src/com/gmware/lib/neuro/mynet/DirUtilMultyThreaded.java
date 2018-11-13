package com.gmware.lib.neuro.mynet;


import com.gmware.lib.neuro.NetImage;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Единнообразное действие над файлами в указанной директории.
 * Мультипоточное исполнение.
 * Created by Gauss on 08.12.2015.
 */
public abstract class DirUtilMultyThreaded {

    /**
     * Количество потоков.
     */
    protected int numThreads = Runtime.getRuntime().availableProcessors();

    /**
     * Папка, в которой лежат обучающие образы.
     */
    private final File inDir;//Z:\DATA\neuro-test\in\

    public File getInDir() {
        return inDir;
    }

    /**
     * Папка, в которой лежат обучающие образы.
     */
    private final File dir3;//Z:\DATA\neuro-test\in\

    public File getDir3() {
        return dir3;
    }

    /**
     * Именованная папка вычислителя.
     */
    private final File outDir;

    public File getOutDir() {
        return outDir;
    }

    /**
     * Папка, в которую складываются результаты.
     */
    public File resultDir = null;

    public File getResultDir() {
        return resultDir;
    }

    /**
     * Папка, в которую складываются логи.
     */
    public File logDir = null;

    public File getLogDir() {
        return logDir;
    }

    protected PrintStream summary = null;
    private File[] files = null;
    private int currentFileIndex = 0;

    protected DirUtilMultyThreaded(final String solverId,
                                   final int numThreads,
                                   final File inDir,
                                   final File outDir) {
        this.inDir = inDir;
        dir3 = null;
        this.outDir = new File(outDir, solverId);
        this.outDir.mkdirs();
        resultDir = new File(this.outDir, "RESULT");
        resultDir.mkdirs();
        logDir = new File(this.outDir, "LOGS");
        logDir.mkdirs();
        this.numThreads = numThreads;
        try {
            summary = new PrintStream(new FileOutputStream(new File(this.outDir, "_summary.log"), true));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    DirUtilMultyThreaded(final String solverId,
                         final int numThreads,
                         final File inDir,
                         final File dir3,
                         final File outDir) {
        this.inDir = inDir;
        this.dir3 = dir3;
        this.outDir = new File(outDir, solverId);
        this.outDir.mkdirs();
        resultDir = new File(this.outDir, "RESULT");
        resultDir.mkdirs();
        logDir = new File(this.outDir, "LOGS");
        logDir.mkdirs();
        this.numThreads = numThreads;
        try {
            summary = new PrintStream(new FileOutputStream(new File(this.outDir, "_summary.log"), true));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    DirUtilMultyThreaded(final int numThreads,
                         final File inDir,
                         final File dir3,
                         final File outDir,
                         final String summary) {
        this.inDir = inDir;
        this.dir3 = dir3;
        this.dir3.mkdirs();
        this.outDir = outDir;
        this.outDir.mkdirs();
        this.numThreads = numThreads;
        try {
            this.summary = new PrintStream(new FileOutputStream(new File(summary), true));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Запустить потоки.<br>
     *
     * @throws InterruptedException  если один из потоков прерывается внешним потоком.
     * @throws FileNotFoundException если не удалось открыть файл outDir/_summary.log.
     */
    public void start() throws InterruptedException, FileNotFoundException {
        try {
            summary.println(" Директория: " + inDir.getAbsolutePath());
            final FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(final File file) {
                    return !file.isDirectory();
                }
            };
            files = inDir.listFiles(filter);
            if (files == null) return;

            // большие файлы пойдут первыми:
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(final File f1, final File f2) {
                    return (int) Math.signum(f2.length() - f1.length());
                }
            });

            // выводим список файлов в лог:
            for (final File file : files) {
                summary.println(file.getName() + " \t" + format(file.length()) + " \t" + getFileDescription(file));
            }
            currentFileIndex = 0;

            // стартуем потоки
            final Thread[] threads = new Thread[numThreads];
            for (int id = 0; id < threads.length; id++) {
                threads[id] = new ComputingThread(id);
                threads[id].start();
                Thread.sleep(1000);
            }
            for (final Thread thread : threads) {
                thread.join();
            }
            summary.println();
        } finally {
            summary.close();
        }
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

    protected abstract String getFileDescription(File file);

    synchronized private File getWorkFile(final int id) {
        if (currentFileIndex < files.length) {
            final int index = currentFileIndex++;
            final File file = files[index];
            System.out.println(String.format("ThreadId = %2d CurrentFileIndex = %7d FileName = %s (%.1fMb)", id, index, file.getName(), file.length() / 1024.0 / 1024));

            return file;
        } else {
            return null;
        }
    }

    private final class ComputingThread extends Thread {
        private int id;

        private ComputingThread(final int id) {
            super("Computing thread - " + id);
            this.id = id;
        }

        @Override
        public void run() {
            preRun(id);
            File f;
            while ((f = getWorkFile(id)) != null) {
                action(id, f);
            }
        }
    }

    void fileCopy(final File src, final File dst) {
        try (final DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(src)));
             final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dst)))
        ) {
            final byte[] b = new byte[(int) src.length()];
            dis.read(b);
            dos.write(b);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void preRun(final int id);

    protected abstract void action(final int id, final File workFile);

    protected NetImage[] imagesBuffer = null;

    synchronized public NetImage[] assignImages(final DataInputStream dis) {
        final ArrayList<NetImage> netImages = new ArrayList<>(imagesBuffer.length / 16);
        try {
            while (true) {
                netImages.add(new NetImage(dis));
            }
        } catch (final EOFException ignored) {
        } catch (final IOException e) {
            e.printStackTrace();
        }
        final NetImage[] images = netImages.toArray(new NetImage[netImages.size()]);
        return images;
    }

    //веса образов
    int w1 = 0;
    int w2 = 0;

    synchronized public NetImage[] mergeImages(final DataInputStream dis1, final DataInputStream dis2) {
        int numImages = 0;
        int index = 0;
        for (int i = 0; i < w1; ++i) {
            try {
                while (true) {
                    imagesBuffer[index] = new NetImage(dis1);
                    numImages++;
                    index = numImages % imagesBuffer.length;
                }
            } catch (final EOFException ignored) {
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < w2; ++i) {
            try {
                while (true) {
                    imagesBuffer[index] = new NetImage(dis2);
                    numImages++;
                    index = numImages % imagesBuffer.length;
                }
            } catch (final EOFException ignored) {
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        numImages = (numImages < imagesBuffer.length) ? numImages : imagesBuffer.length;
        final NetImage[] images = new NetImage[numImages];
        System.arraycopy(imagesBuffer, 0, images, 0, numImages);
        return images;
    }

    protected static String t(final double x) {
        return String.format("%01.5f", x);
    }
}

