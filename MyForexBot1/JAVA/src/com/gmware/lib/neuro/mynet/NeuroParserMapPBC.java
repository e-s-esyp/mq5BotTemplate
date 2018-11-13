package com.gmware.lib.neuro.mynet;

import com.gmware.lib.neuro.mynet.Maps.NeuroMapPartitionByCorrelation3;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * На вход подаем распечатку р-дерева,
 * на выходе - NeuroMap
 * <p/>
 * Created by Gauss on 24.03.2016.
 */
public class NeuroParserMapPBC {

    public static NeuroMapPartitionByCorrelation3 parsePBC3(final BufferedReader stream) throws IOException {
        final NeuroMapPartitionByCorrelation3 map = new NeuroMapPartitionByCorrelation3();
        parseMapHeader(map, stream);
        map.structure = new NeuroMapPartitionByCorrelation3.Structure[map.numStructures];
        for (int i = 0; i < map.structure.length; ++i) {
            map.structure[i] = parseStructure(i, stream);
        }
        return map;
    }

    private static void parseMapHeader(
            final NeuroMapPartitionByCorrelation3 map,
            final BufferedReader stream) throws IOException {
        final Map<String, String> params = parseParameters(stream);
        map.tag = Long.parseLong(params.get("tag"));
        map.numIns = Integer.parseInt(params.get("numIns"));
        map.numOuts = Integer.parseInt(params.get("numOuts"));
        map.numImages = Integer.parseInt(params.get("numImages"));
        map.rrError = Double.parseDouble(params.get("rrError"));
        map.noTest = Boolean.parseBoolean(params.get("noTest"));
        map.numStructures = Integer.parseInt(params.get("numStructures"));
        map.convertFCR = Boolean.parseBoolean(params.get("convertFCR"));
        map.errorType = Integer.parseInt(params.get("computationType"));
        map.levelLimit = Integer.parseInt(params.get("levelLimit"));
        map.imagesLimit = Integer.parseInt(params.get("imagesLimit"));
        map.trnVar = Double.parseDouble(params.get("trnVar"));
        map.tstVar = Double.parseDouble(params.get("tstVar"));
        map.trnEA = Double.parseDouble(params.get("trnEA"));
        map.tstEA = Double.parseDouble(params.get("tstEA"));
        map.trnREA = Double.parseDouble(params.get("trnREA"));
        map.tstREA = Double.parseDouble(params.get("tstREA"));
        map.relativeErrorOfAverage = Double.parseDouble(params.get("relativeErrorOfAverage"));
        map.trnE = Double.parseDouble(params.get("trnE"));
        map.tstE = Double.parseDouble(params.get("tstE"));
        map.trnRE = Double.parseDouble(params.get("trnRE"));
        map.tstRE = Double.parseDouble(params.get("tstRE"));
        map.relativeError = Double.parseDouble(params.get("relativeError"));
        map.averageOut = parseArrayOfDoubles("Average Out:", stream);
        map.errorByLevel = parseArrayOfIndexedDoubles("Errors By Level:", stream);
        map.errorByImages = parseArrayOfIndexedDoubles("Errors By Images:", stream);
    }

    private static Map<String, String> parseParameters(final BufferedReader stream) throws IOException {
        final ConcurrentHashMap<String, String> params = new ConcurrentHashMap<String, String>();
        while (true) {
            final String line = stream.readLine();
            if (line.startsWith("- end of parameters -")) break;
            final String[] record = line.split(" = ");
            if (record.length == 2) params.putIfAbsent(record[0].replaceAll(" ", ""), record[1]);
        }
        return params;
    }

    private static double[] parseArrayOfDoubles(final String s, final BufferedReader stream) throws IOException {
        while (true) {
            final String line = stream.readLine();
            if (line.startsWith(s)) break;
        }
        final double[] d = new double[100];
        int maxInd = -1;
        while (true) {
            final String line = stream.readLine();
            if (line.startsWith("- end of list -")) break;
            final String[] spl = line.replaceAll("\\[|\\]| ", "").split("=");
            if (spl.length == 2) {
                final int ind = Integer.parseInt(spl[0]);
                if (ind > maxInd) maxInd = ind;
                d[ind] = Double.parseDouble(spl[1]);
            }
        }
        return Arrays.copyOfRange(d, 0, maxInd + 1);
    }

    private static IndexedDoubles[] parseArrayOfIndexedDoubles(final String s, final BufferedReader stream) throws IOException {
        final double[] d = parseArrayOfDoubles(s, stream);
        final IndexedDoubles[] id = new IndexedDoubles[d.length];
        for (int i = 0; i < id.length; i++) {
            id[i] = new IndexedDoubles(i, d[i]);
        }
        return id;
    }

    private static NeuroMapPartitionByCorrelation3.Structure parseStructure(
            final int i, final BufferedReader stream) throws IOException {
        while (true) {
            final String line = stream.readLine();
            if (line.contains("STRUCTURE")) {
                final int n = Integer.parseInt(line.replaceAll("\\[|\\]| |[A-Z,a-z]", ""));
                if (i == n) return parseStructure(stream);
            }
        }
    }

    private static NeuroMapPartitionByCorrelation3.Structure parseStructure(final BufferedReader stream) throws IOException {
        final NeuroMapPartitionByCorrelation3.Structure structure = new NeuroMapPartitionByCorrelation3.Structure();
        String line = stream.readLine();
        String[] sp = line.split("\\{|\\}");
        structure.numOfImages = Integer.parseInt(sp[1]);
        line = sp[2];
        structure.divided = line.contains(":");
        sp = line.split("\\[|\\]");
        structure.average = Double.parseDouble(sp[1].replace(" ", ""));
        if (structure.divided) {
            structure.inInd = Integer.parseInt(sp[3]);
            sp = sp[4].split("\\(|\\)");
            structure.divisor = Double.parseDouble(sp[0].replaceAll(":| |=|ce", ""));
            sp = sp[1].split(" ");
            structure.correlation = Double.parseDouble(sp[0]);
            structure.error = Double.parseDouble(sp[1]);
            structure.lessPart = parseStructure(stream);
            structure.notLessPart = parseStructure(stream);
        }
        return structure;
    }

    public static void main(final String[] args) throws IOException {
        final String fileName = "D:\\F\\test\\t4\\maps\\PBC3=20=0.1=DT-I-- 1 in4 0.69 0.31 0.9989 0.0011 D out1440 " +
                "o_+-_b 0.0895 r18.0\\LOGS\\EU1-2016-03-27.bin.log";
        final NeuroMap map = parsePBC3(new BufferedReader(new FileReader(new File(fileName))));
        final PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(fileName + ".copy")));
        ps.print(map);
        ps.flush();
    }

}
