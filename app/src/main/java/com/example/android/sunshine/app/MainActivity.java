package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.sunshine.app.data.WeatherContract;

public class MainActivity extends AppCompatActivity implements ForecastFragment.ForecastItemCallback {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String FORECASTFRAGMENT_TAG = "FFTAG";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private String location;
    private Boolean twoPanes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        location = Utility.getPreferredLocation(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.weather_detail_container) != null) {
            twoPanes = true;
            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(),
                                DETAILFRAGMENT_TAG)
                        .commit();
            }
        }
        else {
            twoPanes = false;
            getSupportActionBar().setElevation(0.0f);
        }

        ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast);
        forecastFragment.setForecastItemCallback(this);
        forecastFragment.setShowDifferentTodayElement(!twoPanes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String newLocation = Utility.getPreferredLocation(this);
        if (location != null && !location.equals(newLocation)) {
            ForecastFragment forecastFragment = (ForecastFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_forecast);
            if (forecastFragment != null)
                forecastFragment.onLocationChanged();

            DetailFragment df = (DetailFragment)getSupportFragmentManager()
                    .findFragmentByTag(DETAILFRAGMENT_TAG);
            if (df != null )
                df.onLocationChanged(newLocation);

            location = newLocation;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap() {
        String location = Utility.getPreferredLocation(this);

        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        Uri viewMapUri =  Uri.parse("geo:0,0").buildUpon()
                .appendQueryParameter("q", location).build();
        viewIntent.setData(viewMapUri);

        if (viewIntent.resolveActivity(getPackageManager()) != null)
            startActivity(viewIntent);
        else
            Toast.makeText(this, R.string.maps_app_not_found, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onForecastItemClick(Uri uri) {
        if (!twoPanes) {
            Intent intent = new Intent(this, DetailActivity.class).setData(uri);
            startActivity(intent);
        }
        else {
            Bundle detailArguments = new Bundle();
            detailArguments.putParcelable(DetailFragment.DETAIL_URI, uri);

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(detailArguments);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailFragment, DETAILFRAGMENT_TAG)
                    .commit();
        }
    }
}
