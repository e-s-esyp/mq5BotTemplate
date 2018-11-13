package com.gmware.lib.neuro.mynet;

/**
 * Created by Gauss on 10.12.2015.
 */
public class NeuroNetStructure {
    public NeuroNetStructure(final int[][] valence) {
        this.valence = valence;
    }

    /**
     * [0][] - ����� ������ ���������, ������ ������ �� ���� ������
     * [1][] - ����� ����� ������� ����
     * ...
     * [length-1][] - ����� �������
     */
    public int[][] valence = null;

    @Override
    public String toString() {
        String s = "";
        if (valence == null) return "null";
        for (final int[] x : valence) {
            s += "[";
            if (x == null) return "error";
            for (final int y : x) {
                s += " " + y;
            }
            s += "]";
        }
        return s;
    }

    public int getNumNeurons() {
        int n = 0;
        for (int i = 1; i < valence.length; ++i) {
            for (int j = 0; j < valence[i].length; ++j) {
                n += valence[i][j];
            }
        }
        return n;
    }

}
