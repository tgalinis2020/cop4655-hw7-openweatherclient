package com.tgalinis.openweatherclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class MapsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Instead of rendering the map when the view is created, I've moved the logic
     * here so I can update the map on-the-fly from ForecastActivity.
     *
     * Not sure if this is the proper way to approach this problem but... it
     * works!
     *
     * @param loc city name
     * @param lat Latitude
     * @param lon Longitude
     */
    public void update(final String loc, final double lat, final double lon) {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        // Make sure the fragment exists before trying to do anything with it.
        if (mapFragment == null) {
            Toast.makeText(this.getActivity(),
                    "Map is not available! This should not happen. :(",
                    Toast.LENGTH_LONG).show();
            return;
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                LatLng pos = new LatLng(lat, lon);
                MarkerOptions marker = new MarkerOptions();
                map.addMarker(marker.position(pos).title(loc));
                map.moveCamera(CameraUpdateFactory.newLatLng(pos));
            }
        });
    }
}