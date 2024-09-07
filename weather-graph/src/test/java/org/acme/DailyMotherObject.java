package org.acme;

import org.acme.weather.Daily;

public class DailyMotherObject {


    public static Daily createDaily() {
        return new Daily(array(30.5), array(16.1), array(0.0), array(20.8), array(3));
    }

    private static int[] array(int x) {
        return new int[]{x};
    }

    private static double[] array(double x) {
        return new double[]{x};
    }

}
