package com.tgalinis.openweatherclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    // Following some conventions set by doing HW6.
    public static final String EXTRA_FORECAST = "com.tgalinis.openweatherclient.FORECAST";

    // Using the same endpoint and API key from HW4.
    public static final String FORECAST_ENDPOINT = "https://api.openweathermap.org/data/2.5/weather";

    // This corresponds to a meta-data entry in AndroidManifest.xml
    private static final String OPEN_WEATHER_API_KEY = "com.tgalinis.openweatherclient.API_KEY";

    // Carry-over from the GPS tutorial. Seems it is required for requesting
    // permissions.
    private static final int REQUEST_CODE_PERMISSION = 2;

    private EditText locationTxt;
    private RequestQueue requestQueue;
    private FusedLocationProviderClient locationClient;


    //--------------------------------------------------------------------------
    // Callback classes
    //
    // These private classes handle asynchronous events, such as fetching
    // JSON from a remote resource or getting the user's current location
    // via GPS.
    //--------------------------------------------------------------------------

    /**
     * If OpenWeather successfully responds to a request, deserialize returned
     * JSON to an instance of the Forecast class and pass it to the
     * ForecastActivity via an intent.
     */
    private class OpenWeatherResponse implements Response.Listener<JSONObject> {

        @Override
        public void onResponse(@NonNull JSONObject res) {
            Intent intent = new Intent(MainActivity.this, ForecastActivity.class);

            try {

                // Pluck first result from weather array.
                JSONObject weather = res.getJSONArray("weather").getJSONObject(0);
                JSONObject coords = res.getJSONObject("coord");
                JSONObject main = res.getJSONObject("main");
                JSONObject wind = res.getJSONObject("wind");
                JSONObject sys = res.getJSONObject("sys");

                // Forward the forecast data to the ForecastActivity via
                // an Intent's extra.
                intent.putExtra(EXTRA_FORECAST, new Forecast(
                        main.getDouble("temp"),
                        main.getDouble("feels_like"),
                        main.getDouble("temp_min"),
                        main.getDouble("temp_max"),
                        main.getInt("pressure"),
                        main.getInt("humidity"),
                        weather.getString("description"),
                        weather.getString("icon"),
                        wind.getDouble("speed"),
                        wind.getDouble("deg"),
                        res.getString("name"),
                        sys.getString("country"),
                        coords.getDouble("lat"),
                        coords.getDouble("lon")));

                startActivity(intent);

            } catch (JSONException e) {

                Toast.makeText(MainActivity.this,
                        "An error occurred while attempting to parse " +
                        "data returned by API.",
                        Toast.LENGTH_SHORT).show();

                e.printStackTrace();

            }

        }
    }

    private class OpenWeatherError implements Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(MainActivity.this,
                    "Weather forecast services are unavailable at " +
                            "this time. Please try again later.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class OnGeoSuccessListener implements OnSuccessListener<Location> {

        @Override
        public void onSuccess(Location location) {
            ArrayList<String> params;

            if (location == null) {
                Toast.makeText(MainActivity.this,
                        "Failed to pinpoint your location.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            params = new ArrayList<String>();

            params.add(String.format(Locale.US, "lat=%.2f", location.getLatitude()));
            params.add(String.format(Locale.US, "lon=%.2f", location.getLongitude()));

            fetchForecast(params);
        }
    }

    private class OnGeoFailureListener implements OnFailureListener {

        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(MainActivity.this,
                    "Failed to pinpoint your location.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private class OnSearchBtnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            ArrayList<String> params = new ArrayList<String>();
            String location = locationTxt.getText().toString();
            String locType;

            try {
                Integer.parseInt(location);

                // If the above statement did not fail, the provided location
                // can be assumed to be a ZIP code.
                locType = "zip";

            } catch (Exception e) {

                // Otherwise, it is probably a city since it contains alphabetic
                // characters, commas, whitespace, etc.
                locType = "q";
            }

            params.add(locType + "=" + location);

            fetchForecast(params);
        }
    }

    /**
     * Attempts to use Location services to initialize a request to OpenWeather.
     */
    private class OnGeoBtnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int accessFineLocationPerm = ActivityCompat.checkSelfPermission(
                    MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
            int accessCoarseLocationPerm = ActivityCompat.checkSelfPermission(
                    MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);

            // If the app doesn't have explicit permission from the user to
            // access their location, ask them for permissions.
            if (accessFineLocationPerm != PackageManager.PERMISSION_GRANTED &&
                    accessCoarseLocationPerm != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        MainActivity.this,

                        new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },

                        REQUEST_CODE_PERMISSION);
            }

            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnGeoSuccessListener())
                    .addOnFailureListener(new OnGeoFailureListener());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Cache cache = new DiskBasedCache(getCacheDir(), 1 << 20); // 1 MB cap
        Network network = new BasicNetwork(new HurlStack());
        Button searchBtn = findViewById(R.id.searchBtn);
        Button geoBtn = findViewById(R.id.geoBtn);

        requestQueue = new RequestQueue(cache, network);
        locationTxt = findViewById(R.id.locationTxt);
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        searchBtn.setOnClickListener(new OnSearchBtnClickListener());
        geoBtn.setOnClickListener(new OnGeoBtnClickListener());
        requestQueue.start();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (requestQueue != null) {
            requestQueue.cancelAll(EXTRA_FORECAST);
        }
    }

    /**
     * Makes a request to OpenWeather's API. Parameters must be set
     * in the params ArrayList beforehand.
     *
     * Plugs in API key and desired unit type before sending request.
     *
     * Instead of hard-coding the API key in this class, I opted to put it
     * in the manifest in the form of a meta-data tag. The following
     * StackOverflow post helped me find a way to retrieve it so I can use it
     * to make requests:
     *
     * https://stackoverflow.com/questions/19379349/android-get-manifest-meta-
     * data-out-of-activity
     *
     * @param params Query parameters in key=value form
     */
    protected void fetchForecast(ArrayList<String> params) {
        try {

            Bundle meta = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA).metaData;
            params.add("apikey=" + meta.getString(OPEN_WEATHER_API_KEY));
            params.add("units=imperial");

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.GET,
                    FORECAST_ENDPOINT + "?" + TextUtils.join("&", params),
                    null, // Note to self: this param is for POST data
                    new OpenWeatherResponse(),
                    new OpenWeatherError());

            req.setTag(EXTRA_FORECAST);

            requestQueue.add(req);

        } catch (Exception e) {

            // This shouldn't happen if I didn't misspell the meta-data name above.
            e.printStackTrace();

        }
    }
}