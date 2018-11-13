package com.gmware.lib.neuro.mynet;

import com.gmware.lib.neuro.NetImage;

import java.io.*;

/**
 * Created by Gauss on 24.03.2016.
 */
public class NeuroSplitImages extends DirUtilMultyThreaded {

    long prop1 = 1;
    long prop2 = 1;

    NeuroSplitImages(final int numThreads,
                     final File inDir,
                     final File dir3,
                     final File outDir,
                     final String summary,
                     final int prop1,
                     final int prop2) {
        super(numThreads, inDir, dir3, outDir, summary);
        imagesBuffer = new NetImage[2000000];
    }

    @Override
    protected String getFileDescription(final File file) {
        return null;
    }

    @Override
    protected void preRun(final int id) {

    }

    @Override
    protected void action(final int id, final File workFile) {
        try {
            final File src1 = new File(getOutDir().getAbsolutePath() + "\\" + workFile.getName());
            final File src2 = new File(getDir3().getAbsolutePath() + "\\" + workFile.getName());
            final DataOutputStream dos1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(src1)));
            final DataOutputStream dos2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(src2)));
            final DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(workFile)));
            final NetImage[] images = assignImages(dis);
            final long size1 = ((long) images.length * prop1) / (prop1 + prop2);
            summary.println(workFile.getName() + "  " + images.length + "  " + size1);
            for (int i = 0; i < size1; ++i) {
                images[i].writeImage(dos1);
            }
            dos1.flush();
            dos1.close();
            for (int i = (int) size1; i < images.length; ++i) {
                images[i].writeImage(dos2);
            }
            dos2.flush();
            dos2.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(final String[] args) {
        for (int i = 0; i < args.length; ++i) {
            System.out.println("args[" + i + "]=" + args[i]);
        }
        if (args.length == 7) {
            final NeuroSplitImages splitter = new NeuroSplitImages(
                    Integer.parseInt(args[0]),
                    new File(args[1]),
                    new File(args[2]),
                    new File(args[3]),
                    args[4],
                    Integer.parseInt(args[5]),
                    Integer.parseInt(args[6]));
            try {
                splitter.start();
            } catch (InterruptedException | FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Пример аргументов: ");
            System.out.print("4 ");
            System.out.print("z:\\DATA\\images ");
            System.out.print("z:\\DATA\\imagesOut1 ");
            System.out.print("z:\\DATA\\imagesOut2 ");
            System.out.print("summary.log ");
            System.out.print("5 ");
            System.out.println("6 ");
            System.out.println("4 - число потоков при вычислении ");
            System.out.println("5 - пропорция образов на выходе1 ");
            System.out.println("6 - пропорция образов на выходе2 ");
        }
    }

}
