package com.gmware.lib.neuro.mynet;

import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.net2.ImmutableNNet3Le;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapPartitionByCorrelation;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapPartitionByCorrelation2;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapPartitionByCorrelation3;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Анализирует отображения.
 * Created by Gauss on 24.02.2016.
 * TODO: собирательный образ
 */
public class NeuroCheckMapsOnImages {

    private boolean f = false;//общий файл образов
    static final double[][] GRAD = new double[][]{
            {0.0, 0.5}, //F
            {0.0, 0.02, 0.05, 0.1, 0.15}, //C
            {0.0, 0.02, 0.05, 0.1, 0.15}};//R
    static final String[] OUT_DESCRIPTION = new String[]{
            "(+1,-1,-1)",
            "(-1,+1,-1)",
            "(-1,-1,+1)"};
    //----------------------------------------------------------
    private PrintStream summary;
    private File mapDir = null;
    private File netDir = null;
    private File imageDir = null;
    private String netExtention = null;
    private String imageExtention = null;
    private String summaryName = null;
    private File commonImageFile = null;

    private class MapNetDescription {
        String name;

        File mapFile;
        double mapError = 0;
        double mapError2 = 0;
        int[] mapDiscrete = new int[9];
        int[] mapD2 = new int[2];

        File netFile = null;
        double netError = 0;

        File imageFile;
        int numImages = 0;
        double imagesVar = 0;
        double imagesErrorOfAverage = 0;
        double imagesErrorOfAverage2 = 0;

        String description = null;
        String description2 = "";
        public int numOuts = 0;

        public MapNetDescription(final File mapFile) {
            name = mapFile.getName();
            final int i = name.lastIndexOf('.');
            name = name.substring(0, i);
            this.mapFile = mapFile;
            if (f) {
                imageFile = commonImageFile;
            } else {
                imageFile = new File(imageDir.getPath() + "\\" + name + imageExtention);// + ".image"
                if (netDir != null) {
                    netFile = new File(netDir.getPath() + "\\" + name + netExtention);
                } else {
                    netFile = null;
                }
            }
        }

        @Override
        public String toString() {
            return name;
        }

        public void make() throws IOException {
            if (mapFile.exists()) {
                final DataInputStream mapDis = new DataInputStream(new BufferedInputStream(new FileInputStream(mapFile)));
                final NeuroMap map = NeuroMap.load(mapDis);
                description = format(name, 20) + ".map";
                description += " type=" + getClassIndex(map);
                if (getClassIndex(map) == 0) return;
                description += getMapDescription(map);
                if (imageFile.exists()) {
                    final NetImage[] images = assignImages(
                            new DataInputStream(new BufferedInputStream(new FileInputStream(imageFile))));
                    setErrorOfAverage(images);
                    description += " images:";
                    description += " s=" + format(numImages = images.length);
                    description += " v=" + format(imagesVar);
                    description += " EOA=" + format(imagesErrorOfAverage);
                    description += " EOA2=" + format(imagesErrorOfAverage2);
                    description += " map(images):";
                    mapError = getError(map, images);
                    description += " e=" + format(mapError);
                    description += " rel=" + format((imagesErrorOfAverage == 0) ? 0 : mapError / imagesErrorOfAverage);
                    if (f) {
                        for (int i = 1; i < 24; ++i) {
                            setLimit(map, 1 << i);
                            description += "\n il=" + format(i, 2);
                            description += " (" + format(1 << i, 7) + ")";
//                            mapError2 = getError2(map, images);
//                            description += " e2=" + format(mapError2);
//                            description += " rel2=" + format(mapError2 / imagesErrorOfAverage2);
                            getDiscrete(map, images, mapDiscrete, mapD2);
                            description += " p=[" +
                                    "++" + format(mapDiscrete[0], 6) + ", " +
                                    "+o" + format(mapDiscrete[1], 6) + ", " +
                                    "+-" + format(mapDiscrete[2], 6) + "| " +
                                    "o+" + format(mapDiscrete[3], 6) + ", " +
                                    "oo" + format(mapDiscrete[4], 6) + ", " +
                                    "o-" + format(mapDiscrete[5], 6) + "| " +
                                    "-+" + format(mapDiscrete[6], 6) + ", " +
                                    "-o" + format(mapDiscrete[7], 6) + ", " +
                                    "--" + format(mapDiscrete[8], 6) + "]" +
                                    "[" + String.format("%6d", mapD2[0]) + ", " +
                                    "" + String.format("%6d", mapD2[1]) + "]" +
                                    String.format("%6d", mapD2[0] + mapD2[1]);
                        }
                    }
                    if (!f) {
                        description2 += "--- S-TREE ---------------------------------------\n";
                        getClusterError(map, images, null);
                    }
                    if (netFile == null) {
                        return;
                    }
                    if (netFile.exists()) {
                        final DataInputStream netDis = new DataInputStream(new BufferedInputStream(new FileInputStream(netFile)));
                        final ImmutableNNet3Le net = new ImmutableNNet3Le(netDis, true, true);
                        description += " net description.";
                        description += " net(images):";
                        netError = getError(net, images);
                        description += " e=" + format(netError);
                        description += " rel=" + format((imagesErrorOfAverage == 0) ? 0 : netError / imagesErrorOfAverage);
                        if (!f) {
                            description2 += "--- NEURO NET --------------------------------------\n";
                            getClusterError(net, images, null);
                        }
                    }
                } else {
                    description += " imageFile not exists.";
                    if (netFile == null) {
                        return;
                    }
                    if (netFile.exists()) {
                        final DataInputStream netDis = new DataInputStream(new BufferedInputStream(new FileInputStream(netFile)));
                        final ImmutableNNet3Le net = new ImmutableNNet3Le(netDis, true, true);
                        description += " net description.";
                    } else {
                        description += " netFile not exists.";
                    }
                }
            } else {
                description = "mapFile not exists.";
            }
        }

        void setErrorOfAverage(final NetImage[] images) {
            if (images == null) return;
            if (images.length == 0) return;
            imagesVar = NeuroMap.getVariance(images);
            final double[] averageOut = new double[images[0].out.length];
            NeuroMap.setAverageOut(averageOut, images);
            imagesErrorOfAverage = NeuroMap.getErrorOfAverage(averageOut, images);
//            imagesErrorOfAverage = (imagesVar > 0.0) ? imagesErrorOfAverage / imagesVar : imagesErrorOfAverage;
            imagesErrorOfAverage2 = NeuroMap.getErrorOfAverage2(averageOut, images);
        }

        double getError(final Object func, final NetImage[] images) {
            if (images == null) return 0;
            if (images.length == 0) return 0;
            double error = 0;
            for (final NetImage image : images) {
                final double[] out = propagate(func, image.in);
                double d;
                if (out == null) {
                    summary.println("error: out == null in file " + name);
                    continue;
                }
                for (int i = 0; i < image.out.length; ++i) {
                    d = out[i] - image.out[i];
                    error += d * d;
                }
            }
            error = (images.length > 0) ? error / images.length : error;
            return error;
//            return imagesVar > 0.0 ? error / imagesVar : error;
        }

        double getError2(final Object func, final NetImage[] images) {
            if (images == null) return 0;
            if (images.length == 0) return 0;
            double error = 0;
            for (final NetImage image : images) {
                final double[] out = propagate(func, image.in);
                double d;
                if (out == null) {
                    summary.println("error: out == null in file " + name);
                    continue;
                }
                for (int i = 0; i < image.out.length; ++i) {
                    d = NeuroCommon.signum(out[i]) - image.out[i];
                    error += d * d;
                }
            }
            error = (images.length > 0) ? error / images.length : error;
            return error;
        }

        void setLimit(final Object func, final int limit) {
            if (func.getClass() == NeuroMapPartitionByCorrelation2.class) {
                return;
            }
            if (func.getClass() == NeuroMapPartitionByCorrelation3.class) {
                ((NeuroMapPartitionByCorrelation3) func).imagesLimit = limit;
                return;
            }
            if (func.getClass() == ImmutableNNet3Le.class) {
                return;
            }
        }

        void getDiscrete(final Object func, final NetImage[] images, final int[] discr, final int[] discr2) {
            discr[0] = 0;
            discr[1] = 0;
            if (images == null) return;
            if (images.length == 0) return;
            int p0 = 0;
            int p1 = 0;
            int p2 = 0;
            int p3 = 0;
            int p4 = 0;
            int p5 = 0;
            int p6 = 0;
            int p7 = 0;
            int p8 = 0;
            for (final NetImage image : images) {
                final double[] out = propagate(func, image.in);
                if (out == null) {
                    summary.println("error: out == null in file " + name);
                    continue;
                }
                if (out[0] > 0) {
                    if (image.out[0] > 0) {
                        ++p0;
                        continue;
                    }
                    if (image.out[0] < 0) {
                        ++p2;
                        continue;
                    }
                    ++p1;
                    continue;
                }
                if (out[0] < 0) {
                    if (image.out[0] > 0) {
                        ++p6;
                        continue;
                    }
                    if (image.out[0] < 0) {
                        ++p8;
                        continue;
                    }
                    ++p7;
                    continue;
                }
                if (image.out[0] > 0) {
                    ++p3;
                    continue;
                }
                if (image.out[0] < 0) {
                    ++p5;
                    continue;
                }
                ++p4;
            }
            discr[0] = p0;
            discr[1] = p1;
            discr[2] = p2;
            discr[3] = p3;
            discr[4] = p4;
            discr[5] = p5;
            discr[6] = p6;
            discr[7] = p7;
            discr[8] = p8;
            discr2[0] = (p0 - p2);
            discr2[1] = (p8 - p6);
        }

        double[][][] getClusterError(final Object func, final NetImage[] images, final double[][][] cm) {
            if (images == null) return cm;
            if (images.length == 0) return cm;
            numOuts = images[0].out.length;
            if (numOuts < 2) return cm;
            final double[][][] cm2;
            if (cm == null) {
                cm2 = new double[numOuts][][];
                for (int i = 0; i < cm2.length; i++) {
                    cm2[i] = new double[numOuts][];
                    for (int j = 0; j < cm2[i].length; ++j) {
                        cm2[i][j] = new double[GRAD[j].length];
                    }
                }
            } else {
                cm2 = cm;
            }
            for (final NetImage image : images) {
                final double[] out = propagate(func, image.in);
                if (out == null) {
                    summary.println("error: out == null in file " + name);
                    continue;
                }
                int ind = 0;
                if (numOuts > 1) {
                    ind = -1;
                    for (int i = 0; i < image.out.length && ind < 0; ++i) {
                        if (image.out[i] > 0) {
                            ind = i;
                        }
                    }
                    if (ind < 0) {
                        System.out.println("Error: NeuroCheckMapsOnImages.getClusterError");
                    }
                }
                final double[][] a = cm2[ind];
                for (int i = 0; i < out.length; ++i) {
                    final double o = out[i] + 0.5;
                    final double[] b = a[i];
                    for (int j = b.length - 1; j >= 0; --j) {
                        if (o >= GRAD[i][j]) {
                            b[j] += o;
                            break;
                        }
                    }
                }
            }
            description2 += "     GRAD ";
            for (final double[] b : GRAD) {
                for (final double c : b) {
                    description2 += String.format("\t%5.10f", c);
                }
                description2 += "\t|";
            }
            description2 += "\n";
            for (int i = 0; i < cm2.length; ++i) {
                final double[][] a = cm2[i];
                description2 += OUT_DESCRIPTION[i];
                for (final double[] b : a) {
                    for (final double c : b) {
                        description2 += String.format("\t%5.10f", c / images.length);
                    }
                    description2 += "\t|";
                }
                description2 += "\n";
            }
            return cm2;
        }
    }

    double[] propagate(final Object func, final double[] in) {
        if (func.getClass() == NeuroMapPartitionByCorrelation2.class) {
            return ((NeuroMapPartitionByCorrelation2) func).propagate(in);
        }
        if (func.getClass() == NeuroMapPartitionByCorrelation3.class) {
            return ((NeuroMapPartitionByCorrelation3) func).propagate(in);
        }
        if (func.getClass() == ImmutableNNet3Le.class) {
            return ((ImmutableNNet3Le) func).propagateMulty(in);
        }
        return null;
    }

    String getMapDescription(final Object func) {
        if (func.getClass() == NeuroMapPartitionByCorrelation2.class) {
            final NeuroMapPartitionByCorrelation2 map = (NeuroMapPartitionByCorrelation2) func;
            return map.getSmartDescription();
        }
        if (func.getClass() == NeuroMapPartitionByCorrelation3.class) {
            final NeuroMapPartitionByCorrelation3 map = (NeuroMapPartitionByCorrelation3) func;
            return map.getSmartDescription();
        }
        if (func.getClass() == ImmutableNNet3Le.class) {
            return "4";
        }
        if (func.getClass() == NeuroMapPartitionByCorrelation.class) {
            return "1";
        }
        return "unknown";
    }

    int getClassIndex(final Object func) {
        if (func.getClass() == NeuroMapPartitionByCorrelation2.class) return 2;
        if (func.getClass() == NeuroMapPartitionByCorrelation3.class) return 3;
        if (func.getClass() == ImmutableNNet3Le.class) return 4;
        if (func.getClass() == NeuroMapPartitionByCorrelation.class) return 1;
        return 0;
    }

    NetImage[] imagesBuffer = new NetImage[2000000];

    synchronized public NetImage[] assignImages(final DataInputStream dis) {
        int numImages = 0;
        int index = 0;
        try {
            while (true) {
                imagesBuffer[index] = new NetImage(dis);
                numImages++;
                index = numImages % imagesBuffer.length;
            }
        } catch (final EOFException ignored) {
        } catch (final IOException e) {
            e.printStackTrace();
        }
        numImages = (numImages < imagesBuffer.length) ? numImages : imagesBuffer.length;
        final NetImage[] images = new NetImage[numImages];
        System.arraycopy(imagesBuffer, 0, images, 0, numImages);
        return images;
    }

    public NeuroCheckMapsOnImages(final String mapDirName,
                                  final String imageName,
                                  final String imageExtention,
                                  final String summaryName) {
        mapDir = new File(mapDirName);
        if (f) {
            commonImageFile = new File(imageName);
        } else {
            imageDir = new File(imageName);
        }
        this.imageExtention = imageExtention;
        final File fileSummary = new File(summaryName);
        PrintStream summary1 = null;
        try {
            summary1 = new PrintStream(new FileOutputStream(fileSummary, true));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
        summary = summary1;
        summary.println("maps only");
    }

    public NeuroCheckMapsOnImages(final String mapDirName,
                                  final String netDirName,
                                  final String netExtention,
                                  final String imageDirName,
                                  final String imageExtention,
                                  final String summaryName) {
        mapDir = new File(mapDirName);
        netDir = new File(netDirName);
        this.netExtention = netExtention;
        imageDir = new File(imageDirName);
        this.imageExtention = imageExtention;
        this.summaryName = summaryName;
        final File fileSummary = new File(summaryName);
        PrintStream summary1 = null;
        try {
            summary1 = new PrintStream(new FileOutputStream(fileSummary, true));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
        summary = summary1;
        summary.println("maps + nets");
    }

    public void checkMaps() throws IOException {
        summary.println("Директория с предсказателями: " + mapDir.getAbsolutePath());
        if (netDir != null) {
            summary.println("Директория с предсказателями: " + netDir.getAbsolutePath());
        }
        if (f) {
            summary.println("Файл с образами: " + commonImageFile.getAbsolutePath());
        } else {
            summary.println("Директория с образами: " + imageDir.getAbsolutePath());
        }
        final FileFilter mapFilter = new FileFilter() {
            @Override
            public boolean accept(final File file) {
                return !file.isDirectory();
            }
        };
        final File[] mapFiles = mapDir.listFiles(mapFilter);
        if (mapFiles == null) return;

        final MapNetDescription[] descriptions = new MapNetDescription[mapFiles.length];
        // выводим список файлов в лог:
        for (int i = 0; i < mapFiles.length; ++i) {
            descriptions[i] = new MapNetDescription(mapFiles[i]);
            descriptions[i].make();
            if (descriptions[i].numImages > 0) {
                summary.println(format(i + 1) + " " +
                        descriptions[i].description);
            }
        }
        try {
            // сортируем по количеству образов
            final File fileSummary1 = new File(summaryName + "-sort1");
            PrintStream summary1 = null;
            summary1 = new PrintStream(new FileOutputStream(fileSummary1, true));
            final File fileSummary2 = new File(summaryName + "-sort2");
            PrintStream summary2 = null;
            summary2 = new PrintStream(new FileOutputStream(fileSummary2, true));
            final File fileSummary3 = new File(summaryName + "-sort3");
            PrintStream summary3 = null;
            summary3 = new PrintStream(new FileOutputStream(fileSummary3, true));
            Arrays.sort(descriptions, new Comparator<MapNetDescription>() {
                @Override
                public int compare(final MapNetDescription f1, final MapNetDescription f2) {
                    if (f2.numImages < f1.numImages) return -1;
                    if (f2.numImages > f1.numImages) return 1;
                    return 0;
                }
            });
            for (int i = 0; i < descriptions.length; ++i) {
                if (descriptions[i].numOuts > 1) {
                    summary1.println(format(i + 1) + " " +
                            format(descriptions[i].mapError / descriptions[i].netError) +
                            descriptions[i].description);
                    summary3.print(format(i + 1) + " " +
                            format((descriptions[i].netError == 0) ? 0 : descriptions[i].mapError / descriptions[i].netError) +
                            descriptions[i].description + "\n" + descriptions[i].description2);
                } else {
                    summary2.println(format(i + 1) + " " +
                            format(descriptions[i].mapError / descriptions[i].netError) +
                            descriptions[i].description);
                }
            }
            summary1.flush();
            summary2.flush();
            summary3.flush();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }


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
        if (x == 0) {
            s = "0";
            i--;
        } else {
            while (x > 0) {
                s = x % 10 + s;
                x /= 10;
                i--;
            }
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
        while (x > 0) {
            s = x % 10 + s;
            x /= 10;
        }
        s = format(s, l, c);
        return s;
    }

    static String format(final double x) {
        return String.format("%01." + 4 + "f", x);
    }

    public static void main(final String[] args) throws IOException {
        for (int i = 0; i < args.length; ++i) {
            System.out.println("args[" + i + "] = " + args[i]);
        }
        final NeuroCheckMapsOnImages checker;
        if (args.length == 0) {
            final String[] s = new String[]{
                    "\\\\10.10.40.53\\Gauss\\RESULT\\PNL13\\nets\\PBC2\\SOLVER75\\RESULT",
                    "\\\\10.10.40.53\\Gauss\\RESULT\\PNL13\\nets\\net4",
                    ".net4",
                    "\\\\10.10.40.53\\Share\\PNL13\\images2-validation",
                    "D:\\DATA\\NeuroCheckMapsOnImages-PBC2-SOLVER75.log"};
            checker = new NeuroCheckMapsOnImages(s[0], s[1], s[2], s[3], "", s[4]);
            checker.checkMaps();
            return;
        }
        if (args.length == 3) {
            checker = new NeuroCheckMapsOnImages(args[0], args[1], "", args[2]);
            checker.checkMaps();
            return;
        }
        if (args.length == 4) {
            checker = new NeuroCheckMapsOnImages(args[0], args[1], args[2], args[3]);
            checker.checkMaps();
            return;
        }
        if (args.length == 5) {
            checker = new NeuroCheckMapsOnImages(args[0], args[1], args[2], args[3], "", args[4]);
            checker.checkMaps();
            return;
        }
        if (args.length == 6) {
            checker = new NeuroCheckMapsOnImages(args[0], args[1], args[2], args[3], args[4], args[5]);
            checker.checkMaps();
            return;
        }
        System.out.println("args[0] = папка с р-деревьями");
        System.out.println("args[1] = папка с образами");
        System.out.println("args[3] = лог-файл");
        System.out.println("или");
        System.out.println("args[0] = папка с р-деревьями");
        System.out.println("args[1] = папка с образами");
        System.out.println("args[2] = расширение образов");
        System.out.println("args[3] = лог-файл");
        System.out.println("или");
        System.out.println("args[0] = папка с р-деревьями");
        System.out.println("args[1] = папка с нейросетями");
        System.out.println("args[2] = расширение нейросетей");
        System.out.println("args[3] = папка с образами");
        System.out.println("args[4] = лог-файл");
        System.out.println("или");
        System.out.println("args[0] = папка с р-деревьями");
        System.out.println("args[1] = папка с нейросетями");
        System.out.println("args[2] = расширение нейросетей");
        System.out.println("args[3] = папка с образами");
        System.out.println("args[4] = расширение образов");
        System.out.println("args[5] = лог-файл");
    }

}

