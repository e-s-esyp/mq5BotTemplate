package com.gmware.lib.neuro.mynet.F;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * -
 * Created by Gauss on 15.04.2016.
 */
public class NeuroFSimulator {

    static boolean makeSimulatorLog = false;

    NeuroFSimulatorTiming[] simulate(final NeuroFPredictor predictor,
                                     final TimedDoubles[] data,
                                     final long startTime) {
        final NeuroFSimulatorTiming[] timedBalance = new NeuroFSimulatorTiming[data.length];
        double balance = 0;
        final NeuroFPosition position = new NeuroFPosition(1, 0);
        int startInd = findTime(startTime, data);
        if (startInd < NeuroF.NUM_USED_IN) {
            startInd = NeuroF.NUM_USED_IN;
        }
        predictor.initParameters(data, startInd);
        for (int ind = 0; ind < startInd; ind++) {
            timedBalance[ind] = new NeuroFSimulatorTiming(data, ind, position, balance);
        }
        for (int ind = startInd; ind < data.length - 1; ++ind) {
            final int action = predictor.getDecision(position.order, 1, data, ind);
            balance += position.makeAction(action, data[ind]);
            timedBalance[ind] = new NeuroFSimulatorTiming(data, ind, position, balance);
        }
        balance += position.makeAction(1, data[data.length - 1]);
        timedBalance[timedBalance.length - 1] = new NeuroFSimulatorTiming(data, timedBalance.length - 1, position, balance);
        return timedBalance;
    }

    static int findTime(final long time, final TimedDoubles[] data) {
        for (int i = 0; i < data.length; i++) {
            if (data[i].time >= time) return i;
        }
        return data.length;
    }


    static void trainNsimulateDYCELLS(final String[] args) throws IOException {
        final long startNanoTime = System.nanoTime();
        final NeuroFPredictor predictor = new NeuroFPredictorForest();
        int startPar = 1;
        int finishPar = 21;
        String path = "G:\\F";
        if (args.length == 2) {
            startPar = Integer.parseInt(args[0]);
            finishPar = startPar;
            path = args[1];
        }
        final String trainName = "EU1-gkfx-2015.bin";
        final String simulateName = "EU1-gkfx-2012-2016.bin";
        final String version =
                "single" +
                        NeuroF.MAC[0] + "." +
                        NeuroF.NUM_IN;
        final String pathResult = path + "\\test\\t8\\" + predictor.getShortDescription() + "-" + version + "-" + trainName + "-" + simulateName;
        final String trainNamePath = path + "\\test\\t4\\log-bin\\" + trainName;
        final String simulateNamePath = path + "\\test\\t4\\log-bin\\" + simulateName;
        final long startTime = TimedDoubles.getMinute(2012, 1, 1, 0, 0);
        final long middleTime = TimedDoubles.getMinute(2016, 1, 1, 0, 0);
        final String predictorsPath = pathResult + "\\PREDICTOR";
        final String simulatorPath = pathResult + "\\SIMULATOR";
        (new File(simulatorPath)).mkdirs();
        //--------------------------------------------------------------------------------------------------------------
        final TimedDoubles[] trainData = NeuroFLoader.loadBinData(new File(trainNamePath));
//TODO: test randomMix
//        TimedDoubles.randomMix(trainData, new Rnd517(0));
        final PrintStream summary = new PrintStream(simulatorPath + "\\summary" + String.format("-%02d", startPar) + ".log");
        for (int i1 = startPar; i1 <= finishPar; i1++) {//1..34
            for (int i2 = 0; i2 < i1; i2++) {
                final int[] par0 = new int[]{i2, i1};
                for (int j = 8; j <= 14; j++) {
                    final int[] par1 = new int[j];
                    for (int i = 0; i < par1.length; i++) {
                        par1[i] = par0[i % 2];
                    }
                    final int[][] pars = new int[][]{par1};
                    String parss1 = "";//{*,*}
                    String parss2 = "";//-*-*
                    for (final int[] par : pars) {
                        parss1 += String.format("{%d,%d,%d} ", j, par[0], par[1]);
                        parss2 += String.format("-%02d-%02d-%02d", j, par[0], par[1]);
                    }
                    int numChanges = 0;
                    final String predictorPath = predictorsPath + "\\" + parss2;
                    (new File(predictorPath)).mkdirs();
                    predictor.log = new PrintStream(predictorPath + "\\predictor.log");
                    predictor.train2(pars, trainData, predictorPath);
                    final NeuroFSimulator simulator = new NeuroFSimulator();
                    final TimedDoubles[] simulateData = NeuroFLoader.loadBinData(new File(simulateNamePath));
                    final NeuroFSimulatorTiming[] result = simulator.simulate(predictor, simulateData, startTime);
                    final File simulatorDir = new File(simulatorPath);
                    simulatorDir.mkdirs();
                    int startInd = findTime(startTime, simulateData);
                    if (startInd < NeuroF.NUM_USED_IN1) {
                        startInd = NeuroF.NUM_USED_IN1;
                    }
                    final PrintStream simulatorLog;
                    if (makeSimulatorLog) {
                        simulatorLog = new PrintStream(new File(
                                simulatorDir + "\\summary" + parss2 + ".log"));
                        simulatorLog.println("startTime " + startTime);
                        simulatorLog.println("startInd " + startInd);
                    } else {
                        simulatorLog = null;
                    }
                    final double rf = result[result.length - 1].value;
                    final int middleIndex = findTime(middleTime, simulateData);
                    final double r15 = result[middleIndex - 1].value;
                    final double r16 = rf - r15;
                    double maxrout = 0;
                    double minrout = 0;
                    for (int i = 0; i < result.length; i++) {
                        if (makeSimulatorLog) {
                            simulatorLog.println(result[i]);
                        }
                        if (i > 0) {
                            if (result[i - 1].value != result[i].value) {
                                numChanges++;
                            }
                            final double rout = result[i].value - (rf * (i + 1)) / result.length;
                            if (rout > maxrout) {
                                maxrout = rout;
                            }
                            if (rout < minrout) {
                                minrout = rout;
                            }
                        }
                    }
                    if (r16 > 0 && r15 > 0) {
                        NeuroFPNG.printPNGtimedDouble(
                                result,
                                simulatorDir + "\\graph" + parss2 + ".png",
                                getWeeks(simulateData),
                                middleIndex);
                    }
                    summary.printf("%s %4d %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f\n",
                            parss1,
                            numChanges,
                            r15,
                            r16,
                            minrout,//просадка
                            maxrout,//пик
                            (r16 + minrout) / Math.abs(r16),//относительная просадка. цель - максимальное положительное значение
                            r16 / (maxrout - minrout));//относительное колебание. цель - минимальное положительное значение
                }
            }
        }
        System.out.println("elapsed time = " + (System.nanoTime() - startNanoTime) / 1000000 + "ms");
    }

    static void trainNsimulateCELLS(final String[] args) throws IOException {
        final long startNanoTime = System.nanoTime();
        final NeuroFPredictor predictor = new NeuroFPredictorCells();
        int startPar = 1;
        int finishPar = 21;
        String path = "G:\\F";
        if (args.length == 2) {
            startPar = Integer.parseInt(args[0]);
            finishPar = startPar;
            path = args[1];
        }
        final String trainName = "F-2016-1-4.bin";
        final String simulateName = "F-2016-1-7.bin";
        final String version =
                "single" +
                        NeuroF.MAC[0] + "." +
                        NeuroF.NUM_IN + "-1";
        final String pathResult = path + "\\test\\t8\\" + predictor.getShortDescription() + "-" + version + "-" + trainName + "-" + simulateName;
        final String trainNamePath = path + "\\test\\t4\\log-bin\\" + trainName;
        final String simulateNamePath = path + "\\test\\t4\\log-bin\\" + simulateName;
        final long startTime = TimedDoubles.getMinute(2012, 1, 1, 0, 0);
        final long middleTime = TimedDoubles.getMinute(2016, 4, 23, 0, 0);
        final String predictorsPath = pathResult + "\\PREDICTOR";
        final String simulatorPath = pathResult + "\\SIMULATOR";
        (new File(simulatorPath)).mkdirs();
        //--------------------------------------------------------------------------------------------------------------
        final TimedDoubles[] trainData = NeuroFLoader.loadBinData(new File(trainNamePath));
//TODO: test randomMix
//        TimedDoubles.randomMix(trainData, new Rnd517(0));
        final PrintStream summary = new PrintStream(simulatorPath + "\\summary" + String.format("-%02d", startPar) + ".log");
        for (int i1 = startPar; i1 <= finishPar; i1++) {//1..34
            for (int i2 = 0; i2 < i1; i2++) {
                for (int j = 10; j <= 40; j++) {
                    final int[][] pars = new int[][]{{j, i2, i1}};
                    String parss1 = "";//{*,*}
                    String parss2 = "";//-*-*
                    for (final int[] par : pars) {
                        parss1 += String.format("{%d,%d,%d} ", par[0], par[1], par[2]);
                        parss2 += String.format("-%02d-%02d-%02d", par[0], par[1], par[2]);
                    }
                    int numChanges = 0;
                    final String predictorPath = predictorsPath + "\\" + parss2;
                    (new File(predictorPath)).mkdirs();
                    predictor.log = new PrintStream(predictorPath + "\\predictor.log");
                    predictor.train2(pars, trainData, predictorPath);
                    final NeuroFSimulator simulator = new NeuroFSimulator();
                    final TimedDoubles[] simulateData = NeuroFLoader.loadBinData(new File(simulateNamePath));
                    final NeuroFSimulatorTiming[] result = simulator.simulate(predictor, simulateData, startTime);
                    final File simulatorDir = new File(simulatorPath);
                    simulatorDir.mkdirs();
                    int startInd = findTime(startTime, simulateData);
                    if (startInd < NeuroF.NUM_USED_IN1) {
                        startInd = NeuroF.NUM_USED_IN1;
                    }
                    final PrintStream simulatorLog;
                    if (makeSimulatorLog) {
                        simulatorLog = new PrintStream(new File(
                                simulatorDir + "\\summary" + parss2 + ".log"));
                        simulatorLog.println("startTime " + startTime);
                        simulatorLog.println("startInd " + startInd);
                    } else {
                        simulatorLog = null;
                    }
                    final double rf = result[result.length - 1].value;
                    final int middleIndex = findTime(middleTime, simulateData);
                    final double r15 = result[middleIndex - 1].value;
                    final double r16 = rf - r15;
                    double maxrout = 0;
                    double minrout = 0;
                    if (r16 > 0 && r15 > 0) {
                        for (int i = 0; i < result.length; i++) {
                            if (makeSimulatorLog) {
                                simulatorLog.println(result[i]);
                            }
                            if (i > 0) {
                                if (result[i - 1].value != result[i].value) {
                                    numChanges++;
                                }
                                final double rout = result[i].value - (rf * (i + 1)) / result.length;
                                if (rout > maxrout) {
                                    maxrout = rout;
                                }
                                if (rout < minrout) {
                                    minrout = rout;
                                }
                            }
                        }
                        NeuroFPNG.printPNGtimedDouble(
                                result,
                                simulatorDir + "\\graph" + parss2 + ".png",
                                getWeeks(simulateData), middleIndex);
                    }
                    summary.printf("%s %4d %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f\n",
                            parss1,
                            numChanges,
                            r15,
                            r16,
                            minrout,//просадка
                            maxrout,//пик
                            (r16 + minrout) / Math.abs(r16),//относительная просадка. цель - максимальное положительное значение
                            r16 / (maxrout - minrout));//относительное колебание. цель - минимальное положительное значение
                }
            }
        }
        System.out.println("elapsed time = " + (System.nanoTime() - startNanoTime) / 1000000 + "ms");
    }

    static void trainNsimulateCELLS1(final String[] args) throws IOException {
        final long startNanoTime = System.nanoTime();
        int startPar = 1;
        int finishPar = 34;
        String path = "G:\\F";
        if (args.length == 2) {
            startPar = Integer.parseInt(args[0]);
            finishPar = startPar;
            path = args[1];
        }
        final String trainName = "EU1-gkfx-2015.bin";
//        final String simulateName = "EU-2012-2016-NEW.bin";
        final String simulateName = "EU1-gkfx-2012-2016.bin";
        final String version = "set-3-" +
                NeuroF.MAC[0] + "." +
                NeuroF.NUM_IN;
        final String pathResult = path + "\\test\\t8\\" + startNanoTime + "CELLS-" + version + "-" + trainName + "-" + simulateName;
        final String trainNamePath = path + "\\test\\t4\\log-bin\\" + trainName;
        final String simulateNamePath = path + "\\test\\t4\\log-bin\\" + simulateName;
        final long startTime = TimedDoubles.getMinute(2012, 1, 1, 0, 0);
        final long middleTime = TimedDoubles.getMinute(2016, 1, 1, 0, 0);
        final String predictorsPath = pathResult + "\\PREDICTOR";
        final String simulatorPath = pathResult + "\\SIMULATOR";
        (new File(simulatorPath)).mkdirs();
        //--------------------------------------------------------------------------------------------------------------
        final TimedDoubles[] trainData = NeuroFLoader.loadBinData(new File(trainNamePath));
        final NeuroFPredictorCells predictor = new NeuroFPredictorCells();
        final PrintStream summary = new PrintStream(simulatorPath + "\\summary" + String.format("-%02d", startPar) + ".log");
        final int i1 = 4;
        final int i2 = 5;
        final int r = 41;
        final int[][] pars = new int[][]{{r, i2, i1}};
        String parss1 = "";//{*,*}
        String parss2 = "";//-*-*
        for (final int[] par : pars) {
            parss1 = String.format("{%2d", par[0]);
            parss2 = String.format("-%02d", par[0]);
            for (int i = 1; i < par.length; i++) {
                parss1 += String.format(",%2d", par[i]);
                parss2 += String.format("-%02d", par[i]);
            }
            parss1 += "} ";
        }
        int numChanges = 0;
        final String predictorPath = predictorsPath + "\\" + parss2;
        (new File(predictorPath)).mkdirs();
        predictor.log = new PrintStream(predictorPath + "\\predictor.log");
        predictor.train2(pars, trainData, predictorPath);
        final NeuroFSimulator simulator = new NeuroFSimulator();
        final TimedDoubles[] simulateData = NeuroFLoader.loadBinData(new File(simulateNamePath));
        final NeuroFSimulatorTiming[] result = simulator.simulate(predictor, simulateData, startTime);
        final File simulatorDir = new File(simulatorPath);
        simulatorDir.mkdirs();
        int startInd = findTime(startTime, simulateData);
        if (startInd < NeuroF.NUM_USED_IN1) {
            startInd = NeuroF.NUM_USED_IN1;
        }
        final PrintStream simulatorLog;
        if (makeSimulatorLog) {
            simulatorLog = new PrintStream(new File(
                    simulatorDir + "\\summary" + parss2 + ".log"));
            simulatorLog.println("startTime " + startTime);
            simulatorLog.println("startInd " + startInd);
        } else {
            simulatorLog = null;
        }
        final double rf = result[result.length - 1].value;
        final int middleIndex = findTime(middleTime, simulateData);
        final double r15 = result[middleIndex - 1].value;
        final double r16 = rf - r15;
        double maxrout = 0;
        double minrout = 0;
        for (int i = 0; i < result.length; i++) {
            if (makeSimulatorLog) {
                simulatorLog.println(result[i]);
            }
            if (i > 0) {
                if (result[i - 1].value != result[i].value) {
                    numChanges++;
                }
                final double rout = result[i].value - (rf * (i + 1)) / result.length;
                if (rout > maxrout) {
                    maxrout = rout;
                }
                if (rout < minrout) {
                    minrout = rout;
                }
            }
        }
        NeuroFPNG.printPNGtimedDouble(
                result,
                simulatorDir + "\\graph" + parss2 + ".png",
                getWeeks(simulateData),
                middleIndex);
        summary.printf("%s %4d %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f\n",
                parss1,
                numChanges,
                r15,
                r16,
                minrout,//просадка
                maxrout,//пик
                (r16 + minrout) / Math.abs(r16),//относительная просадка. цель - максимальное положительное значение
                r16 / (maxrout - minrout));//относительное колебание. цель - минимальное положительное значение
        System.out.println("elapsed time = " + (System.nanoTime() - startNanoTime) / 1000000 + "ms");
    }

    static void simulateCELLS(final String[] args) throws IOException {
//        makeSimulatorLog = true;
        final long startNanoTime = System.nanoTime();
        final String path = "G:\\F";
        final String predictorName = "MIX00";
        final String predictorPath = path + "\\maps\\" + predictorName;
        final String simulateName = "EU-2012-2016-NEW.bin";
        final String pathResult = path + "\\test\\t8\\" + startNanoTime + "-CELLS-" + "-" + predictorName + "-" + simulateName;
        final String simulateNamePath = path + "\\test\\t4\\log-bin\\" + simulateName;
        final long startTime = TimedDoubles.getMinute(2012, 1, 1, 0, 0);
        final long middleTime = TimedDoubles.getMinute(2016, 4, 23, 0, 0);
        final String simulatorPath = pathResult + "\\SIMULATOR";
        (new File(simulatorPath)).mkdirs();
        //--------------------------------------------------------------------------------------------------------------
        final NeuroFPredictorCells predictor = new NeuroFPredictorCells(predictorPath);
        final PrintStream summary = new PrintStream(simulatorPath + "\\summary" + ".log");
        int numChanges = 0;
        (new File(predictorPath)).mkdirs();
        predictor.log = new PrintStream(predictorPath + "\\predictor.log");
        final NeuroFSimulator simulator = new NeuroFSimulator();
        final TimedDoubles[] simulateData = NeuroFLoader.loadBinData(new File(simulateNamePath));
        final NeuroFSimulatorTiming[] result = simulator.simulate(predictor, simulateData, startTime);
        final File simulatorDir = new File(simulatorPath);
        simulatorDir.mkdirs();
        int startInd = findTime(startTime, simulateData);
        if (startInd < NeuroF.NUM_USED_IN1) {
            startInd = NeuroF.NUM_USED_IN1;
        }
        final PrintStream simulatorLog;
        if (makeSimulatorLog) {
            simulatorLog = new PrintStream(new File(
                    simulatorDir + "\\summary" + ".log"));
            simulatorLog.println("startTime " + startTime);
            simulatorLog.println("startInd " + startInd);
        } else {
            simulatorLog = null;
        }
        final double rf = result[result.length - 1].value;
        final int middleIndex = findTime(middleTime, simulateData);
        final double r15 = result[middleIndex - 1].value;
        final double r16 = rf - r15;
        double maxrout = 0;
        double minrout = 0;
        for (int i = 0; i < result.length; i++) {
            if (makeSimulatorLog) {
                if (simulatorLog != null) {
                    simulatorLog.println(result[i]);
                }
            }
            if (i > 0) {
                if (result[i - 1].value != result[i].value) {
                    numChanges++;
                }
                final double rout = result[i].value - (rf * (i + 1)) / result.length;
                if (rout > maxrout) {
                    maxrout = rout;
                }
                if (rout < minrout) {
                    minrout = rout;
                }
            }
        }
        NeuroFPNG.printPNGtimedDouble(
                result,
                simulatorDir + "\\graph" + ".png",
                getWeeks(simulateData),
                middleIndex);
        summary.printf("%4d %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f\n",
                numChanges,
                r15,
                r16,
                minrout,//просадка
                maxrout,//пик
                (r16 + minrout) / Math.abs(r16),//относительная просадка. цель - максимальное положительное значение
                r16 / (maxrout - minrout));//относительное колебание. цель - минимальное положительное значение
        System.out.println("elapsed time = " + (System.nanoTime() - startNanoTime) / 1000000 + "ms");
    }

    static final long WEEK = 7 * 24 * 60;

    private static long[] getWeeks(final TimedDoubles[] timings) {
        final long time0 = TimedDoubles.getMinute(2000, 1, 3, 0, 0);
        final long start = ((timings[0].time / WEEK) + 1) * WEEK;
        final int numweeks = (int) ((timings[timings.length - 1].time - timings[0].time) / WEEK);
        final long[] weeks = new long[numweeks];
        for (int i = 0; i < numweeks; ++i) {
            weeks[i] = findTime(start + i * WEEK, timings);
        }
        return weeks;
    }

    public static void main(final String[] args) throws IOException {
        trainNsimulateCELLS(args);
//        simulateCELLS(args);
    }

}
