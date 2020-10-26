package com.tgalinis.openweatherclient;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Can't pass around Java objects trivially between Activities. After doing some
 * digging, I found a useful StackOverflow post on Parcelables.
 *
 * https://stackoverflow.com/questions/2139134/how-to-send-an-object-from-one-
 * android-activity-to-another-using-intents
 */
public class Forecast implements Parcelable {
    public static final String[] COMPASS_SECTORS = {
            "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S",
            "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
    };
    private double temperature;
    private double temperatureFeelsLike;
    private double temperatureHi;
    private double temperatureLo;
    private double windDirection;
    private double windSpeed;
    private int humidity;
    private int pressure;
    private double lat;
    private double lon;
    private String conditionDescription;
    private String conditionCode;
    private String country;
    private String city;

    public Forecast(double temperature, double feels_like, double temp_min,
                    double temp_max, int pressure, int humidity,
                    String description, String icon, double speed,
                    double direction, String city, String country,
                    double lat, double lon) {

        this.temperature = temperature;
        this.temperatureFeelsLike = feels_like;
        this.temperatureLo = temp_min;
        this.temperatureHi = temp_max;
        this.conditionDescription = description;
        this.conditionCode = icon;
        this.windSpeed = speed;
        this.windDirection = direction;
        this.pressure = pressure;
        this.humidity = humidity;
        this.city = city;
        this.country = country;
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * Unserialize Forecast data from given Parcel.
     *
     * @param in Source of forecast data.
     */
    protected Forecast(Parcel in) {
        temperature = in.readDouble();
        temperatureFeelsLike = in.readDouble();
        temperatureHi = in.readDouble();
        temperatureLo = in.readDouble();
        windDirection = in.readDouble();
        windSpeed = in.readDouble();
        humidity = in.readInt();
        pressure = in.readInt();
        conditionDescription = in.readString();
        conditionCode = in.readString();
        lat = in.readDouble();
        lon = in.readDouble();
        city = in.readString();
        country = in.readString();
    }

    public static final Creator<Forecast> CREATOR = new Creator<Forecast>() {
        @Override
        public Forecast createFromParcel(Parcel in) {
            return new Forecast(in);
        }

        @Override
        public Forecast[] newArray(int size) {
            return new Forecast[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Serialize the Forecast object into a Parcel. Each write must be parallel
     * with the reads done in the constructor accepting a Parcel.
     *
     * @param dest The destination of the serialized Forecast.
     * @param flags No idea what this is for honestly ¯\_(ツ)_/¯
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(temperature);
        dest.writeDouble(temperatureFeelsLike);
        dest.writeDouble(temperatureHi);
        dest.writeDouble(temperatureLo);
        dest.writeDouble(windDirection);
        dest.writeDouble(windSpeed);
        dest.writeInt(humidity);
        dest.writeInt(pressure);
        dest.writeString(conditionDescription);
        dest.writeString(conditionCode);
        dest.writeDouble(lat);
        dest.writeDouble(lon);
        dest.writeString(city);
        dest.writeString(country);
    }

    //--------------------------------------------------------------------------
    // Accessor methods
    //
    // Other than getting the wind direction, these are very straight-forward.
    //--------------------------------------------------------------------------

    public double getTemperature() {
        return temperature;
    }

    public double getFeelsLike() {
        return temperatureFeelsLike;
    }

    public double getHigh() {
        return temperatureHi;
    }

    public double getLow() {
        return temperatureLo;
    }

    /**
     * Instead of returning the direction in degrees, represent it as a compass
     * sector.
     *
     * Degree-to-direction solution inspired by (aka shamelessly stolen from) a
     * response from this StackOverflow post:
     * https://stackoverflow.com/questions/36475255/i-have-wind-direction-data-
     * coming-from-openweathermap-api-and-the-data-is-repre
     */
    public String getWindDirection() {
        return COMPASS_SECTORS[(int) (windDirection % 360 / 22.5)];
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getPressure() {
        return pressure;
    }

    public String getConditionDescription() {
        return conditionDescription;
    }

    public String getConditionCode() {
        return conditionCode;
    }

    public double getLatitude() {
        return lat;
    }

    public double getLongitude() {
        return lon;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }
}
