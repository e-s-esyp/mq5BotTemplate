package com.gmware.lib.neuro.mynet.F;

/**
 * ---
 * Created by Gauss on 14.06.2016.
 */
class NeuroFSimulatorTiming {
    long time;
    TimedDoubles ask;
    double value;
    NeuroFPosition position;

    public NeuroFSimulatorTiming(
            final TimedDoubles[] ask,
            final int time,
            final NeuroFPosition position,
            final double balance) {
        this.time = time;
        this.ask = ask[time];
        value = balance;
        this.position = position.copy();
    }

    @Override
    public String toString() {
        return time + ": " + ask + " " + position + " " + String.format("%7.5f", value);
    }

}