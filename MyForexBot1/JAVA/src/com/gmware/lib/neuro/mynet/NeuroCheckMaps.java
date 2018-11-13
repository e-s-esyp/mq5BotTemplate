package com.gmware.lib.neuro.mynet;

import com.gmware.lib.neuro.net2.ImmutableNNet3Le;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapPartitionByCorrelation;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapPartitionByCorrelation2;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Анализирует отображения.
 * Created by Gauss on 24.02.2016.
 */
public class NeuroCheckMaps {
    static final int IMAGES_LIMIT_FOR_SEARCHING = 10000;

    private final PrintStream summary;
    private File inDir = null;//Z:\Gauss\RESULT\PLO\DOLLARO\PartitionByCorrelation\nets\SOLVER\RESULT
    private File outDir = null;//Z:\Gauss\RESULT\PLO\DOLLARO\PartitionByCorrelation\nets\SOLVER
    private File fileMap = null;
    private File fileNet = null;

    public class Division {
        public int numOfImages = 0;
        public int inInd = 0;
        public double divisor = 0;
        String fileName = null;

        Division(final String fileName) {
            this.fileName = fileName;
        }

        @Override
        public String toString() {
            return format(numOfImages, 7) + " \t[" +
                    format(inInd, 3) + "]=" + format(divisor) +
                    " \t" + fileName;
        }
    }

    private class FileDescription {
        double rrError;
        int numImages;
        double numDivisions;
        String descriptionShort;
        String descriptionAdditional1;
        String descriptionAdditional2;
        Division[] divisions;

        public FileDescription(final double p1,
                               final int p2,
                               final double p3,
                               final String s1,
                               final String s2,
                               final String s3,
                               final Division[] divisions) {
            rrError = p1;
            numImages = p2;
            numDivisions = p3;
            descriptionShort = s1;
            descriptionAdditional1 = s2;
            descriptionAdditional2 = s3;
            this.divisions = divisions;
        }

        @Override
        public String toString() {
            return "rrError = " + rrError;
        }
    }

    private class MapNetDescription {
        double rrError;
        int numImages;
        double numDivisions;
        String descriptionShort;

        public MapNetDescription(final double p1,
                                 final int p2,
                                 final double p3,
                                 final String s1) {
            rrError = p1;
            numImages = p2;
            numDivisions = p3;
            descriptionShort = s1;
        }

        @Override
        public String toString() {
            return "rrError = " + rrError;
        }
    }

    public NeuroCheckMaps(final String dirName) {
        inDir = new File(dirName + "\\RESULT");
        outDir = new File(dirName);
        PrintStream summary1 = null;
        try {
            summary1 = new PrintStream(new FileOutputStream(new File(outDir, "nets_summary.log"), true));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
        summary = summary1;
    }

    public NeuroCheckMaps(final String fileNameMap, final String fileNameNet, final String fileNameResult) {
        fileMap = new File(fileNameMap);
        fileNet = new File(fileNameNet);
        final File fileSummary = new File(fileNameResult);
        PrintStream summary1 = null;
        try {
            summary1 = new PrintStream(new FileOutputStream(fileSummary, true));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
        summary = summary1;
    }

    public NeuroCheckMaps(final PrintStream summary, final File inDir, final File outDir) {
        this.summary = summary;
        this.inDir = inDir;
        this.outDir = outDir;
    }

    public void checkFiles() throws InterruptedException, FileNotFoundException {
        try {
            summary.println(" Директория: " + inDir.getAbsolutePath());
            final FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(final File file) {
                    return !file.isDirectory();
                }
            };
            final File[] files = inDir.listFiles(filter);
            if (files == null) return;

            final FileDescription[] descriptions = new FileDescription[files.length];

            // выводим список файлов в лог:
            for (int i = 0; i < files.length; ++i) {
                descriptions[i] = getFileDescription(files[i]);
            }

            // сортируем по ошибке
            Arrays.sort(descriptions, new Comparator<FileDescription>() {
                @Override
                public int compare(final FileDescription f1, final FileDescription f2) {
                    if (f2.rrError < f1.rrError) return -1;
                    if (f2.rrError > f1.rrError) return 1;
                    return 0;
                }
            });

            int sumDivisions = 0;
            for (final FileDescription a : descriptions) {
                sumDivisions += a.numDivisions;
            }

            summary.println("Total divisions over 10000 images = " + sumDivisions);
            for (final FileDescription a : descriptions) {
                summary.println(a.descriptionShort);
            }
            summary.println("-----------------------------------------------------------------");
            for (final FileDescription a : descriptions) {
                if (a.rrError > 0 && a.rrError < 1.0) {
                    summary.println(a.descriptionAdditional1);
                }
            }
            summary.println("-----------------------------------------------------------------");
            // сортируем по количеству образов
            Arrays.sort(descriptions, new Comparator<FileDescription>() {
                @Override
                public int compare(final FileDescription f1, final FileDescription f2) {
                    if (f2.numImages < f1.numImages) return -1;
                    if (f2.numImages > f1.numImages) return 1;
                    return 0;
                }
            });
            for (final FileDescription a : descriptions) {
                if (a.rrError > 0 && a.rrError < 1.0) {
                    summary.println(a.descriptionAdditional2);
                }
            }
            summary.println("-----------------------------------------------------------------");
            final Division[] totalDivisions = new Division[sumDivisions];
            int index = 0;
            for (final FileDescription a : descriptions) {
                for (int i = 0; i < a.numDivisions; ++i) {
                    totalDivisions[index++] = a.divisions[i];
                }
            }
            // сортируем по по количеству образов
            Arrays.sort(totalDivisions, new Comparator<Division>() {
                @Override
                public int compare(final Division f1, final Division f2) {
                    if (f2.numOfImages < f1.numOfImages) return -1;
                    if (f2.numOfImages > f1.numOfImages) return 1;
                    return 0;
                }
            });
            for (final Division a : totalDivisions) {
                summary.println(a);
            }
            summary.println("-----------------------------------------------------------------");
            summary.println();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            summary.close();
        }
    }

    private void checkNets() {
        try {
            final DataInputStream disMap =
                    new DataInputStream(new BufferedInputStream(new FileInputStream(fileMap)));
            int numMaps = 0;
            try {
                while (true) {
                    NeuroMap.load(disMap);
                    numMaps++;
                }
            } catch (final Exception ignored) {

            }
            disMap.close();

            final NeuroMap[] maps = new NeuroMap[numMaps];
            final DataInputStream disMap2 =
                    new DataInputStream(new BufferedInputStream(new FileInputStream(fileMap)));
            for (int i = 0; i < maps.length; ++i) {
                maps[i] = NeuroMap.load(disMap2);
            }
            disMap2.close();

            final ImmutableNNet3Le[] nets = new ImmutableNNet3Le[numMaps];
            final DataInputStream disNet =
                    new DataInputStream(new BufferedInputStream(new FileInputStream(fileNet)));
            for (int i = 0; i < nets.length; ++i) {
                nets[i] = new ImmutableNNet3Le(disNet, true, true);
            }

            final MapNetDescription[] descriptions = new MapNetDescription[maps.length];

            // выводим список файлов в лог:
            for (int i = 0; i < maps.length; ++i) {
                descriptions[i] = getDescription(i, maps[i], nets[i]);
            }

            // сортируем по ошибке
            Arrays.sort(descriptions, new Comparator<MapNetDescription>() {
                @Override
                public int compare(final MapNetDescription f1, final MapNetDescription f2) {
                    if (f2.rrError < f1.rrError) return -1;
                    if (f2.rrError > f1.rrError) return 1;
                    return 0;
                }
            });

            int sumDivisions = 0;
            for (final MapNetDescription a : descriptions) {
                sumDivisions += a.numDivisions;
            }

            summary.println("Total divisions over 10000 images = " + sumDivisions);
            for (final MapNetDescription a : descriptions) {
                summary.println(a.descriptionShort);
            }
            summary.println("-----------------------------------------------------------------");
            summary.println();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            summary.close();
        }
    }

    private FileDescription getFileDescription(final File file) throws IOException {
        final NeuroMap map = NeuroMap.load(new DataInputStream(
                new BufferedInputStream(new FileInputStream(file))));
        String s1 = format(file.getName(), 15) + " \t" + " \t" + map.type.getShortDescription();
        String s2 = format(file.getName(), 15) + " \t";
        String s3 = format(file.getName(), 15) + " \t";
        int numDivisions = 0;
        Division[] divisions = null;
        if (map.type == NeuroMapType.PartitionByCorrelation) {
            final NeuroMapPartitionByCorrelation mapPBC = (NeuroMapPartitionByCorrelation) map;
            s1 += mapPBC.getShortDescription();
            s2 += mapPBC.getStructureDescription();
        }
        if (map.type == NeuroMapType.PartitionByCorrelation2) {
            final NeuroMapPartitionByCorrelation2 mapPBC2 = (NeuroMapPartitionByCorrelation2) map;
            s1 += mapPBC2.getShortDescription();
            s2 += mapPBC2.getStructureDescription1();
            s3 += mapPBC2.getStructureDescription2();
            numDivisions = mapPBC2.getNumDivisions(IMAGES_LIMIT_FOR_SEARCHING);
            divisions = new Division[numDivisions];
            for (int i = 0; i < numDivisions; ++i) {
                divisions[i] = new Division(file.getName());
            }
            mapPBC2.setDivisions(IMAGES_LIMIT_FOR_SEARCHING, divisions);
        }
        if (Double.isNaN(map.rrError)) {
            map.rrError = 10;
        }
        if (map.rrError > 10) map.rrError = 10;
        return new FileDescription(map.rrError, map.numImages, numDivisions, s1, s2, s3, divisions);
    }

    private MapNetDescription getDescription(final int ind, final NeuroMap map, final ImmutableNNet3Le net) throws IOException {
        String s1 = format(ind, 4, '0') + " ";
        final ImmutableNNet3Le.NetStats netStats = net.getNetStats();
        String se = "";
        for (final double a : netStats.trainErrorsRatio) {
            se += " " + format(a);
        }
        se += ";";
        for (final double a : netStats.testErrorsRatio) {
            se += " " + format(a);
        }
        s1 += " netImages = " + format(netStats.trainImages, 7) + " netErrorsRatios = [" + se + "]";
        s1 = format(100, s1);
        s1 += " " + map.type.getShortDescription();
        s1 = format(106, s1);
        int numDivisions = 0;
        if (map.type == NeuroMapType.PartitionByCorrelation2) {
            final NeuroMapPartitionByCorrelation2 mapPBC2 = (NeuroMapPartitionByCorrelation2) map;
            s1 += mapPBC2.getShortDescription();
            numDivisions = mapPBC2.getNumDivisions(IMAGES_LIMIT_FOR_SEARCHING);
        }
        if (Double.isNaN(map.rrError)) {
            map.rrError = 10;
        }
        if (map.rrError > 10) map.rrError = 10;
        return new MapNetDescription(map.rrError, map.numImages, numDivisions, s1);
    }

    static private String format(String name, final int l) {
        while (name.length() < l) {
            name = " " + name;
        }
        return name;
    }

    static private String format(final int l, String name) {
        while (name.length() < l) {
            name += " ";
        }
        return name;
    }

    static private String format(String name, final int l, final char c) {
        while (name.length() < l) {
            name = c + name;
        }
        return name;
    }

    static private String format(final int l, String name, final char c) {
        while (name.length() < l) {
            name += c;
        }
        return name;
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

    static String format(long x, final int l) {
        String s = "";
        int i = l;
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

    static String format(long x, final int l, final char c) {
        String s = "";
        int i = l;
        while (x > 0) {
            s = x % 10 + s;
            x /= 10;
            i--;
        }
        s = format(s, l, c);
        return s;
    }

    static String format(final double x) {
        return String.format("%01." + 3 + "f", x);
    }

    public static void main(final String[] args) {
        System.out.println("Формат 1:");
        System.out.println("args[0] = путь к SOLVER");
        System.out.println("Формат 2:");
        System.out.println("args[0] = файл старых нейросетей");
        System.out.println("args[1] = файл новых NeuroMaps");
        System.out.println("args[2] = лог-файл");
        NeuroCheckMaps checker = null;
        if (args.length == 1) {
            checker = new NeuroCheckMaps(args[0]);
            try {
                checker.checkFiles();
            } catch (InterruptedException | FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (args.length == 3) {
            //        "nets_summary.log"
            checker = new NeuroCheckMaps(args[0], args[1], args[2]);
            checker.checkNets();
        }
    }

}

