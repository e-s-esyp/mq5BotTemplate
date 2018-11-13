package com.gmware.lib.neuro.mynet.F;

/**
 * ---
 * Created by Gauss on 14.06.2016.
 */

class NeuroFPosition {
    int order = 1; //0-up,2-down
    double price = 0;

    //      0   1       2   <- action
    //  0   -   close   2
    //  1   1   -       2
    //  2   1   close   -
    public double makeAction(final int action, final TimedDoubles data) {
        if (order == action) {
            return 0;
        }
        if (order == 0) {//открыта up
            order = action;
//                final double v = data.close - price - data.spread * POINT;
            final double v = data.close - price - NeuroF.SPREAD;
            price = (action == 1) ? 0 : data.close;
            return v;
        }
        if (order == 1) { //открываем позицию
            order = action;
            price = data.close;
            return -data.spread * NeuroF.POINT;
        }
        if (order == 2) {//открыта down
            order = action;
//                final double v = price - data.close - data.spread * POINT;
            final double v = price - data.close - NeuroF.SPREAD;
            price = (action == 1) ? 0 : data.close;
            return v;
        }
        return 0;
    }

    NeuroFPosition(final int order, final double price) {
        this.order = order;
        this.price = price;
    }

    NeuroFPosition copy() {
        return new NeuroFPosition(order, price);
    }

    @Override
    public String toString() {
        return order + " " + String.format("%7.5f", price);
    }
}
