package com.tgalinis.openweatherclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

/**
 * This activity doesn't have much functionality: its purpose is to render
 * weather data received from an API query response in MainActivity.
 */
public class ForecastActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        final Intent intent = getIntent();
        final Forecast forecast = intent.getParcelableExtra(MainActivity.EXTRA_FORECAST);

        // MainActivity always sends a Forecast instance when instantiating this
        // activity. If for whatever reason there is no forecast to show, stop
        // right here.
        if (forecast == null) {
            Toast.makeText(this, "Well, this is awkward...", Toast.LENGTH_SHORT).show();
            return;
        }

        final TextView location = findViewById(R.id.location);
        final TextView humidity = findViewById(R.id.humidity);
        final TextView pressure = findViewById(R.id.pressure);
        final TextView wind = findViewById(R.id.wind);
        final TextView temperature = findViewById(R.id.temp);
        final TextView feelsLike = findViewById(R.id.tempFeelsLike);
        final TextView lowAndHigh = findViewById(R.id.tempLoHi);
        final TextView coordinates = findViewById(R.id.coordinates);
        final TextView conditionDescription = findViewById((R.id.conditionDescription));
        final ImageView conditionImage = findViewById(R.id.conditionImage);
        final MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapsFragment);

        // OpenWeather provides companion icons. Condition codes are mapped to
        // them.
        final String imgUrl = "https://openweathermap.org/img/wn/" +
                forecast.getConditionCode() +"@2x.png";

        double lat = forecast.getLatitude();
        double lon = forecast.getLongitude();
        String y = "N"; // North (positive) or South (negative)
        String x = "E"; // East (positive) or West (negative)

        // Give the image a nice blue background like the one in Homework 4.
        conditionImage.setBackgroundColor(Color.parseColor("#6AA6D4"));

        // Was getting errors while trying to download the image. The following
        // StackOverflow post pointed me to the right direction:
        //
        // https://stackoverflow.com/questions/22395417/error-
        // strictmodeandroidblockguardpolicy-onnetwork
        //
        // Makes sense I suppose. The image isn't immediately available so the
        // process of fetching it should be in its own asynchronous thread.
        // Volley only supports String and JSON requests as far as I can tell so
        // this is the next best thing. Only downside is that I can't show a
        // Toast message if an error occurs.
        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream stream = (InputStream) new URL(imgUrl).getContent();
                    conditionImage.setImageDrawable(Drawable.createFromStream(stream, null));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })).start();

        location.setText(String.format(Locale.US,
                "%s, %s",
                forecast.getCity(),
                forecast.getCountry()));

        // Unicode value 00B0 is the degree symbol.
        temperature.setText(String.format(Locale.US,
                "%.2f \u00B0F",
                forecast.getTemperature()));

        feelsLike.setText(String.format(Locale.US,
                "Feels like %.2f \u00B0F",
                forecast.getFeelsLike()));

        lowAndHigh.setText(String.format(Locale.US,
                "Lo %.2f \u00B0F / Hi %.2f \u00B0F",
                forecast.getLow(),
                forecast.getHigh()));

        conditionDescription.setText(forecast.getConditionDescription());

        humidity.setText(String.format(Locale.US,
                "%d%%",
                forecast.getHumidity()));

        pressure.setText(String.format(Locale.US,
                "%d mb",
                forecast.getPressure()));

        wind.setText(String.format(Locale.US,
                "%s at %.2f mph",
                forecast.getWindDirection(),
                forecast.getWindSpeed()));

        mapsFragment.update(forecast.getCity(), lat, lon);

        if (lat < 0) {
            lat *= -1;  // Convert to a positive number
            y = "S";    // Change direction
        }

        if (lon < 0) {
            lon *= -1;
            x = "W";
        }

        coordinates.setText(String.format(Locale.US,
                "%.2f\u00B0 %s %.2f\u00B0 %s", lat, y, lon, x));
    }
}