package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.sunshine.app.data.WeatherContract;

import org.w3c.dom.Text;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    private Context mContext;

    // Decides whether the special today list item should be shown
    private boolean mShowSpecialTodayItem;

    // View type constants
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;

    public ForecastAdapter(Context context, Cursor c, int flags) {

        super(context, c, flags);
        mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mShowSpecialTodayItem) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public void setShowSpecialTodayItem(boolean value) {
        mShowSpecialTodayItem = value;
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(mContext, high, isMetric) + "/" + Utility.formatTemperature(mContext, low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {

        // Use Integer constants to get data from the right columns
        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        if(viewType == VIEW_TYPE_TODAY)
            layoutId = R.layout.list_item_forecast_today;
        else if(viewType == VIEW_TYPE_FUTURE_DAY)
            layoutId = R.layout.list_item_forecast;

        View view =  LayoutInflater.from(context).inflate(layoutId, parent, false);

        // Create a ViewHolder from the view just inflated
        ViewHolder viewHolder = new ViewHolder(view);
        // Set the tag of the View to be able to refer back to it later
        view.setTag(viewHolder);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Read the tag of the passed view to access the ViewHolder
        // Now, the views can be accessed from within the ViewHolder,
        // no need to find them everytime they get recycled
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // The today row uses a colored image, whereas, the future day
        // uses a gray image. Therefore, we need to figure out which
        // position we're at and then set the correct image to the fallback
        // image if the Glide fails to download the image

        // Get weather id and icon
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int fallbackIcon;

        // Get the view type
        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_TODAY:
                // set fallback to colored icon
                fallbackIcon = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            default:
                // set fallback to gray icon
                fallbackIcon = Utility.getIconResourceForWeatherCondition(weatherId);
        }

        // Get image using Glide
        Glide.with(context).
                load(Utility.getArtUrlForWeatherCondition(context, weatherId)).
                error(fallbackIcon).
                crossFade().
                into(viewHolder.iconView);

        // Read date from cursor
        long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        // Assign date to TextView
        viewHolder.dateView.setText(Utility.getFriendlyDayString(mContext, date));

        // Read weather forecast from cursor
        String weatherDesc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        // Assign description to TextView
        viewHolder.descriptionView.setText(weatherDesc);

        // For talk back accessibility, assign a content descritpion to the icon
        viewHolder.iconView.setContentDescription(weatherDesc);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemperature(mContext, high, isMetric));

        // Read low temperature from cursor
        double low  = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(mContext, low, isMetric));
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}