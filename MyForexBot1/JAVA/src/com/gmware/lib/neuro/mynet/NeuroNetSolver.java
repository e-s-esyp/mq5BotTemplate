package com.gmware.lib.neuro.mynet;

import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.utils.StrUtils;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapEmpty;
import com.gmware.lib.games.holdem.common.rnd.Rnd517;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * Обучение нейросетей по образам в указанной директории.
 * <p>
 * Пример ключей запуска
 * -Xmx60g -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath="oom_dump.hprof"
 * <p>
 * Конкретно у {@link NeuroMapType#PartitionByCorrelation2}:
 * 1. Оверхед 13% от размера файла при загрузке в память образов
 * 2. Порядка 11% хавается алгоритмом просчета (разделение на два набора через divideImages)
 * 3. Потенциально до 11% может съестся алгоритмом сортировки...
 */
public class NeuroNetSolver extends DirUtilMultyThreaded {
    private final Rnd517[] rnd;
    private final NeuroMapType netType;
    private final NeuroTrainerType trainerType;
    private final boolean needPrintDebugInfo;

    /**
     * @param solverId           имя решателя
     * @param inDir              папка с образами
     * @param outDir             папка с результатом
     * @param bufSize            максимальное число образов
     * @param netType            тип нейросети
     * @param trainerType        тип учителя нейросети
     * @param needPrintDebugInfo надо ли делать отладочные сообщения, которые замедляют работу, но для боевого режима
     *                           излишне
     */
    NeuroNetSolver(final String solverId,
                   final int numThreads,
                   final File inDir,
                   final File outDir,
                   final int bufSize,
                   final NeuroMapType netType,
                   final NeuroTrainerType trainerType, final boolean needPrintDebugInfo) {
        super(solverId, numThreads, inDir, outDir);
        this.needPrintDebugInfo = needPrintDebugInfo;
        rnd = new Rnd517[numThreads];
        imagesBuffer = new NetImage[bufSize];
        this.netType = netType;
        this.trainerType = trainerType;
    }

    /**
     * @param solverId           имя решателя
     * @param inDir              папка с образами
     * @param outDir             папка с результатом
     * @param bufSize            максимальное число образов
     * @param netType            тип нейросети
     * @param needPrintDebugInfo надо ли делать отладочные сообщения, которые замедляют работу, но для боевого режима
     *                           излишне
     */
    NeuroNetSolver(final String solverId,
                   final int numThreads,
                   final File inDir,
                   final File outDir,
                   final int bufSize,
                   final NeuroMapType netType, final boolean needPrintDebugInfo) {
        super(solverId, numThreads, inDir, outDir);
        this.needPrintDebugInfo = needPrintDebugInfo;
        rnd = new Rnd517[numThreads];
        imagesBuffer = new NetImage[bufSize];
        this.netType = netType;
        trainerType = netType.getDefaultTrainer();
    }


    @Override
    synchronized protected String getFileDescription(final File file) {
        if (!needPrintDebugInfo) return "undefined";
        if (file.length() == 0) {
            return "empty";
        }
        int numImages = 0;
        NetImage image = null;
        final double[] averageOut = new double[100];
        for (int i = 0; i < averageOut.length; ++i) {
            averageOut[i] = 0;
        }
        try (final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
             final DataInputStream dis = new DataInputStream(bis)) {
            while (bis.available() > 0) {
                image = new NetImage(dis);
                for (int i = 0; i < image.numOut; ++i) {
                    averageOut[i] += image.out[i];
                }
                numImages++;
            }
        } catch (final EOFException ignored) {
        } catch (final IOException e) {
            e.printStackTrace();
        }
        String s = "num images = " + format(numImages);
        if (image != null) {
            s += " \tnumIns = " + image.numIn + " \tnumOuts = " + image.numOut + " \taverageOut = [";
            for (int i = 0; i < image.numOut; ++i) {
                s += " " + t(averageOut[i] / numImages);
            }
            s += "]";
        }
        return s;
    }

    @Override
    protected void preRun(final int id) {
        rnd[id] = new Rnd517();
    }

    private static String niceSize(final long size) {
        return StrUtils.getPrettySize(size);
    }

    @Override
    protected void action(final int id, final File workFile) {
        final long startTime = System.nanoTime();
        NetImage[] images = null;
        synchronized (NeuroNetSolver.class) {
            long diff;
            final long fileSize = workFile.length();
            final Runtime r = Runtime.getRuntime();
            final long diffToContinue;
            switch (netType) {
                case PartitionByCorrelation2:
                    diffToContinue = (long) (r.maxMemory() * 0.35);
                    break;
                default:
                    diffToContinue = (long) (r.maxMemory() * 0.14);
                    break;
            }
            boolean wasSleeping = false;
            final long startSleep = System.currentTimeMillis();
            do {
                final long freeMemory = r.maxMemory() - r.totalMemory() + r.freeMemory();
                diff = freeMemory - fileSize;
                if (diff < diffToContinue) {
                    if (!wasSleeping) {
                        System.out.println(String.format("   [%2d] not anoth space (%s bound), sleeping %s to %s", id, niceSize(diffToContinue), niceSize(fileSize), niceSize(freeMemory)));
                    }
                    wasSleeping = true;
                    try {
                        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
                        r.gc();
                    } catch (final InterruptedException ignored) {
                    }
                } else {
                    if (wasSleeping) {
                        System.out.println(String.format("   [%2d] sleeped %d sec", id, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startSleep)));
                    }
                    System.out.println(String.format("   [%2d] %s (%s bound) will be free after load %s to %s", id, niceSize(diff), niceSize(diffToContinue), niceSize(fileSize), niceSize(freeMemory)));
                }
            } while (diff < diffToContinue);
            final long start = System.currentTimeMillis();
            try (final DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(workFile)))) {
                images = assignImages(dis);
            } catch (final IOException e) {
                e.printStackTrace();
            }
            final long spend = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start);
            System.out.println(String.format("   [%2d] loaded in %d sec with speed %.1fMb/s", id, spend, fileSize * 1.0 / spend / 1024 / 1024));
        }
        try (final DataOutputStream dosNet = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(getResultDir().getAbsolutePath() + "\\" + workFile.getName() + ".map")));
             final PrintStream log = new PrintStream(getLogDir().getAbsolutePath() + "\\" + workFile.getName() + ".log")
        ) {
            log.println("Start training");
            printImages(images, log);
            if (images.length < 10000 && images.length > 0) {
                final File badFilesDir = new File(getOutDir(), "SMALL_FILES");
                if (!badFilesDir.exists()) {
                    badFilesDir.mkdirs();
                }
                final File dest = new File(badFilesDir.getAbsolutePath() + "\\" + workFile.getName());
                fileCopy(workFile, dest);
            }
            final long startTrainTime;
            final long finishTrianTime;
            if (images.length != 0) {
                startTrainTime = System.nanoTime();

                final NeuroMap net = trainerType.train(
                        images,
                        netType,
                        rnd[id],
                        log,
                        getLogDir().getAbsolutePath() + "\\" + workFile.getName());
                finishTrianTime = System.nanoTime();
                net.save(dosNet);
            } else {
                startTrainTime = System.nanoTime();
                finishTrianTime = startTrainTime;
                (new NeuroMapEmpty()).save(dosNet);
            }
            final long finishTime = System.nanoTime();
            log.println("Train time:" + t((finishTrianTime - startTrainTime) / 1000000000.0) + " s");
            log.println("Total time:" + t((finishTime - startTime) / 1000000000.0) + " s");
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void printImages(final NetImage[] images, final PrintStream dosLog) {
        if (images.length > 100) return;
        dosLog.println("Образы, " + images.length + "шт.");
        if (needPrintDebugInfo) {
            for (final NetImage i : images) {
                dosLog.println(i.toString1());
            }
        }
        dosLog.println("--------------------");
    }

    private static String getArgInfo(final String[] args, final int id, final String description) {
        return "args[" + id + "] (" + description + ") " + (args == null || args.length <= id ? "null" : args[id]);
    }

    static public void main(final String[] args) throws FileNotFoundException {
        final PrintStream ps = new PrintStream(NeuroNetSolver.class.getSimpleName() + ".log");
        //noinspection TryFinallyCanBeTryWithResources
        try {
            System.setErr(ps);
            System.setOut(ps);
            System.out.println(getArgInfo(args, 0, "calc   name"));
            System.out.println(getArgInfo(args, 1, "num threads"));
            System.out.println(getArgInfo(args, 2, "images path"));
            System.out.println(getArgInfo(args, 3, "result path"));
            System.out.println(getArgInfo(args, 4, "buffer size"));
            System.out.println(getArgInfo(args, 5, "solver type"));
            System.out.println(getArgInfo(args, 6, "need  debug"));
            if (args.length == 7) {
                final NeuroNetSolver neuroNetSolver = new NeuroNetSolver(
                        args[0],
                        Integer.parseInt(args[1]),
                        new File(args[2]),
                        new File(args[3]),
                        Integer.parseInt(args[4]),
                        NeuroMapType.getType(args[5]),
                        Boolean.valueOf(args[6]));
                try {
                    neuroNetSolver.start();
                } catch (InterruptedException | FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Пример аргументов: ");
                System.out.print("AnyName ");
                System.out.print("4 ");
                System.out.print("z:\\DATA\\images ");
                System.out.print("z:\\DATA\\PartitionByCorrelation\\nets ");
                System.out.print("2000000 ");
                System.out.println("PBC2");
                System.out.println("false");
                System.out.println("4 - число потоков при вычислении ");
                System.out.println("2000000 - максимальное число используемых образов ");
                System.out.println("PBC2 - тип обучаемой нейросети ");
            }
        } finally {
            ps.close();
        }
    }

}
