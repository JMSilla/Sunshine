package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    private Boolean showDifferentTodayElement;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public void setShowDifferentTodayElement(Boolean showDifferentTodayElement) {
        this.showDifferentTodayElement = showDifferentTodayElement;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private static class ViewHolder {
        final ImageView iconView;
        final TextView textViewDate;
        final TextView textViewForecast;
        final TextView textViewHigh;
        final TextView textViewLow;

        ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            textViewDate = (TextView) view.findViewById(R.id.list_item_date_textview);
            textViewForecast = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            textViewHigh = (TextView) view.findViewById(R.id.list_item_high_textview);
            textViewLow = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());

        int layoutId = R.layout.list_item_forecast;
        if (showDifferentTodayElement && viewType == VIEW_TYPE_TODAY)
            layoutId = R.layout.list_item_forecast_today;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        boolean isMetric = Utility.isMetric(context);
        int iconResource;
        String dateText;

        if (showDifferentTodayElement && getItemViewType(cursor.getPosition()) == VIEW_TYPE_TODAY)
            iconResource = Utility.getArtResourceForWeatherCondition(weatherId);
        else
            iconResource = Utility.getIconResourceForWeatherCondition(weatherId);

        holder.iconView.setImageResource(iconResource);

        if (showDifferentTodayElement) {
            dateText = Utility.getFriendlyDayString(context,
                    cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
        }
        else {
            dateText = Utility.getDayName(context, cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
        }

        holder.textViewDate.setText(dateText);

        holder.textViewForecast.setText(cursor.getString(ForecastFragment.COL_WEATHER_DESC));
        holder.textViewHigh.setText(Utility.formatTemperature(context, cursor.getDouble(
                ForecastFragment.COL_WEATHER_MAX_TEMP), isMetric));
        holder.textViewLow.setText(Utility.formatTemperature(context, cursor.getDouble(
                ForecastFragment.COL_WEATHER_MIN_TEMP), isMetric));
    }
}