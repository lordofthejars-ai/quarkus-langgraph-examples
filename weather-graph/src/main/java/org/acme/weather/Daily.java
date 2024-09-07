package org.acme.weather;

import java.util.Arrays;

public record Daily(double[] temperature_2m_max,
                    double[] temperature_2m_min,
                    double[] precipitation_sum,
                    double[] wind_speed_10m_max,
                    int[] weather_code) {

    public DailyWeatherData getFirstDay() {
        return new DailyWeatherData(temperature_2m_max[0],
            temperature_2m_min[0],
            precipitation_sum[0],
            wind_speed_10m_max[0],
            weather_code[0]);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Daily{");
        sb.append("temperature_2m_max=").append(Arrays.toString(temperature_2m_max));
        sb.append(", temperature_2m_min=").append(Arrays.toString(temperature_2m_min));
        sb.append(", precipitation_sum=").append(Arrays.toString(precipitation_sum));
        sb.append(", wind_speed_10m_max=").append(Arrays.toString(wind_speed_10m_max));
        sb.append(", weather_code=").append(Arrays.toString(weather_code));
        sb.append('}');
        return sb.toString();
    }

}
