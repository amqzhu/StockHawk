package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import timber.log.Timber;

/**
 * Created by Aubrianna on 3/21/2017.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = WidgetRemoteViewsService.class.getSimpleName();

    private static final String[] STOCK_COLUMNS = {
            Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_HISTORY
    };
    // these indices must match the projection
    static final int INDEX_STOCK_ID = 0;
    private static final int INDEX_STOCK_SYMBOL = 1;
    private static final int INDEX_STOCK_PRICE = 2;
    static final int INDEX_STOCK_HISTORY = 3;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Timber.d(LOG_TAG);
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                final long identityToken = Binder.clearCallingIdentity();
                Uri stockInfoUri = Contract.Quote.URI;
                data = getContentResolver().query(stockInfoUri,
                        STOCK_COLUMNS,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Uri stockInfoUri = Contract.Quote.URI;
                data = getContentResolver().query(stockInfoUri,
                        STOCK_COLUMNS,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);

                String stockSymbol = data.getString(INDEX_STOCK_SYMBOL);
                float stockPrice = data.getFloat(INDEX_STOCK_PRICE);

                views.setTextViewText(R.id.widget_list_item_symbol, stockSymbol);
                views.setTextViewText(R.id.widget_list_item_price, getString(R.string.currency)+Float.toString(stockPrice));

                final Intent fillInIntent = new Intent();
                Uri stockUri = Contract.Quote.makeUriForStock(stockSymbol);
                fillInIntent.setData(stockUri);
                fillInIntent.putExtra(getString(R.string.stock_detail_symbol), stockSymbol);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_STOCK_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
