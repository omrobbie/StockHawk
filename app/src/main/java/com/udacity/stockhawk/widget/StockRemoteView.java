package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by omrobbie on 06/12/2017.
 */

public class StockRemoteView extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            private Cursor data = null;
            private DecimalFormat dollarFormat;
            private DecimalFormat dollarFormatWithPlus;

            @Override
            public void onCreate() {
                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus = dollarFormat;
                dollarFormatWithPlus.setPositivePrefix("+$");
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) data.close();

                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(
                        Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL
                );
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
            public RemoteViews getViewAt(int i) {
                if (i == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(i))
                    return null;

                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.list_item_quote);
                remoteViews.setTextViewText(R.id.symbol, data.getString(Contract.Quote.POSITION_SYMBOL));
                remoteViews.setTextViewText(R.id.price, dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));

                float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

                if (rawAbsoluteChange > 0) {
                    remoteViews.setInt(
                            R.id.change,
                            "setBackgroundColor",
                            ContextCompat.getColor(getBaseContext(), R.color.material_green_700)
                    );
                } else {
                    remoteViews.setInt(
                            R.id.change,
                            "setBackgroundColor",
                            ContextCompat.getColor(getBaseContext(), R.color.material_red_700)
                    );
                }

                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                remoteViews.setTextViewText(R.id.change, change);

                final Intent fillInIntent = new Intent();
                final Bundle extras = new Bundle();

                extras.putString(MainActivity.STOCK_SYMBOL, data.getString(Contract.Quote.POSITION_SYMBOL));
                fillInIntent.putExtras(extras);
                remoteViews.setOnClickFillInIntent(R.id.list_item_quote, fillInIntent);

                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getBaseContext().getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
