package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    static final String DETAIL_URI = "uri";
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY ,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    static final int COL_WEATHER_DATE = 0;
    static final int COL_WEATHER_DESC = 1;
    static final int COL_WEATHER_MAX_TEMP = 2;
    static final int COL_WEATHER_MIN_TEMP = 3;
    static final int COL_WEATHER_HUMIDITY = 4;
    static final int COL_WEATHER_WIND_SPEED = 5;
    static final int COL_WEATHER_DEGREES = 6;
    static final int COL_WEATHER_PRESSURE = 7;
    static final int COL_WEATHER_ID = 8;

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private String forecastString;
    private static final String APP_HASHTAG = " #sunshineapp";
    private static final Integer DETAIL_LOADER_ID = 2;
    private ShareActionProvider shareProvider;

    private TextView detailDayTextview;
    private TextView detailDateTextview;
    private TextView highTextview;
    private TextView lowTextview;
    private ImageView icon;
    private TextView forecastTextview;
    private TextView humidityTextview;
    private TextView windTextview;
    private TextView pressureTextview;

    private Uri uri;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        detailDayTextview = (TextView) rootView.findViewById(R.id.detail_day_textview);
        detailDateTextview = (TextView) rootView.findViewById(R.id.detail_date_textview);
        highTextview = (TextView) rootView.findViewById(R.id.high_textview);
        lowTextview = (TextView) rootView.findViewById(R.id.low_textview);
        icon = (ImageView) rootView.findViewById(R.id.icon);
        forecastTextview = (TextView) rootView.findViewById(R.id.forecast_textview);
        humidityTextview = (TextView) rootView.findViewById(R.id.humidity_textview);
        windTextview = (TextView) rootView.findViewById(R.id.wind_textview);
        pressureTextview = (TextView) rootView.findViewById(R.id.pressure_textview);

        Bundle arguments = getArguments();
        if (arguments != null)
            uri = arguments.getParcelable(DETAIL_URI);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem item = menu.findItem(R.id.action_share);

        shareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (forecastString != null)
            shareProvider.setShareIntent(createShareIntent());
        else
            Log.d(LOG_TAG, "Share Action Provider is null?");
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, forecastString + APP_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;

        if (uri != null)
            loader = new CursorLoader(getActivity(), uri, FORECAST_COLUMNS, null, null, null);

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) return;

        String dayString = Utility.getDayName(this.getActivity(),
                data.getLong(COL_WEATHER_DATE));
        String dateString = Utility.getFormattedMonthDay(this.getActivity(),
                data.getLong(COL_WEATHER_DATE));
        String weatherDescription = data.getString(COL_WEATHER_DESC);
        boolean isMetric = Utility.isMetric(getActivity());
        String high = Utility.formatTemperature(this.getActivity(),
                data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(this.getActivity(),
                data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        Integer weatherId = data.getInt(COL_WEATHER_ID);

        detailDayTextview.setText(dayString);
        detailDateTextview.setText(dateString);
        highTextview.setText(high);
        lowTextview.setText(low);
        icon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        forecastTextview.setText(weatherDescription);
        humidityTextview.setText(this.getString(R.string.format_humidity,
                data.getFloat(COL_WEATHER_HUMIDITY)));
        windTextview.setText(Utility.getFormattedWind(this.getActivity(),
                data.getFloat(COL_WEATHER_WIND_SPEED), data.getFloat(COL_WEATHER_DEGREES)));
        pressureTextview.setText(this.getString(R.string.format_pressure,
                data.getFloat(COL_WEATHER_PRESSURE)));

        forecastString = String.format("%s - %s - %s/%s", dayString, weatherDescription, high, low);

        if (shareProvider != null)
            shareProvider.setShareIntent(createShareIntent());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri replacedUri = uri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(replacedUri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            uri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER_ID, null, this);
        }
    }
}
