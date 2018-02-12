package com.udacity.stockhawk.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Created by Aubrianna on 3/22/2017.
 */

public class TodayWidgetIntentService extends IntentService {
    private static final String[] STOCK_SMALL_COLUMNS = {
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE
    };
    // these indices must match the projection
    private static final int INDEX_STOCK_SYMBOL = 0;
    private static final int INDEX_STOCK_PRICE = 1;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public TodayWidgetIntentService(String name) {
        super(name);
    }

    public TodayWidgetIntentService() {
        super(null);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TodayWidgetProvider.class));
        // Get today's data from the ContentProvider
        Uri stockSmallUri = Contract.Quote.URI;
        Cursor data = getContentResolver().query(stockSmallUri, STOCK_SMALL_COLUMNS, null,
                null, Contract.Quote.COLUMN_SYMBOL + " ASC");
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // Extract the weather data from the Cursor
        String stockSmallSymbol = data.getString(INDEX_STOCK_SYMBOL);
        float stockSmallPrice = data.getFloat(INDEX_STOCK_PRICE);
        data.close();

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_today_small;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);
            // Add the data to the RemoteViews
            views.setTextViewText(R.id.widget_small_symbol, stockSmallSymbol);
            views.setTextViewText(R.id.widget_small_price, Float.toString(stockSmallPrice));

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
