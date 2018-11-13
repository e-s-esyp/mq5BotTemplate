package com.gmware.lib.neuro.mynet;

import com.gmware.lib.neuro.NetImage;

import java.io.*;

/**
 * Собираем образы из двух источников.
 * Created by Gauss on 14.03.2016.
 */
public class NeuroImageMerging extends DirUtilMultyThreaded {

    static public void main(final String[] args) {
        for (int i = 0; i < args.length; ++i) {
            System.out.println("args[" + i + "]=" + args[i]);
        }
        if (args.length == 6) {
            final NeuroImageMerging neuroImageMerging = new NeuroImageMerging(
                    Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]),
                    Integer.parseInt(args[2]),
                    new File(args[3]),
                    new File(args[4]),
                    new File(args[5])
            );
            try {
                neuroImageMerging.start();
            } catch (InterruptedException | FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Пример аргументов: ");
            System.out.print("2000000 ");
            System.out.print("1 ");
            System.out.print("2 ");
            System.out.print("Z:\\Gauss\\RESULT\\PLO\\images ");
            System.out.print("Z:\\Gauss\\RESULT\\NLO\\images ");
            System.out.println("Z:\\Gauss\\RESULT\\PLO+NLO\\images ");
            System.out.println("2000000 - максимальное число используемых образов ");
            System.out.println("1 - вес первого набора образов ");
            System.out.println("2 - вес второго набора образов ");
            System.out.println("Z:\\Gauss\\RESULT\\PLO\\images - первый набор образов ");
            System.out.println("Z:\\Gauss\\RESULT\\NLO\\images - второй набор образов ");
            System.out.println("Z:\\Gauss\\RESULT\\PLO+NLO\\images - результат ");

        }
    }

    NeuroImageMerging(final int bufSize,
                      final int w1,
                      final int w2,
                      final File inDir,
                      final File inDir2,
                      final File outDir) {
        super("images", 1, inDir, inDir2, outDir);
        imagesBuffer = new NetImage[bufSize];
        this.w1 = w1;
        this.w2 = w2;
    }

    @Override
    protected String getFileDescription(final File file) {
        return "";
    }

    @Override
    protected void preRun(final int id) {

    }

    @Override
    protected void action(final int id, final File workFile) {
        try {
            final File dst = new File(getOutDir().getAbsolutePath() + "\\" + workFile.getName());
            final File src2 = new File(getDir3().getAbsolutePath() + "\\" + workFile.getName());
            final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dst)));
            final DataInputStream dis1 = new DataInputStream(new BufferedInputStream(new FileInputStream(workFile)));
            final DataInputStream dis2 = new DataInputStream(new BufferedInputStream(new FileInputStream(src2)));
            final NetImage[] images = mergeImages(dis1, dis2);
            for (final NetImage image : images) {
                image.writeImage(dos);
            }
            dos.flush();
            dos.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }
}
