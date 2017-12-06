package com.udacity.stockhawk.ui.stock_detail;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.common.collect.Lists;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static String SELECTED_PERIOD = "SELECTED_PERIOD";
    public static String SELECTED_SYMBOL = "SELECTED_SYMBOL";

    private int LOADER_ID = 0;

    @BindView(R.id.lc_chart)
    LineChart lc_chart;

    private Context context;
    private Unbinder unbinder;

    private Long datePeriod;
    private String symbol;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        setupEnv(view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                Contract.Quote.makeUriForStock(symbol),
                Contract.Quote.QUOTE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        generateChart(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    private void setupEnv(View view) {
        context = view.getContext();
        unbinder = ButterKnife.bind(this, view);

        datePeriod = getArguments().getLong(SELECTED_PERIOD);
        symbol = getArguments().getString(SELECTED_SYMBOL);
    }

    private void generateChart(Cursor data) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<ILineDataSet> iLineDataSets = new ArrayList<>();
        Date historyTime = new Date();
        Date periodTime = new Date(datePeriod);

        while (data.moveToNext()) {
            List<Entry> values = new ArrayList<>();
            String[] history = data.getString(Contract.Quote.POSITION_HISTORY).split("\n");

            for (int i = 0; historyTime.getTime() > periodTime.getTime(); i++) {
                String s = history[i];
                String[] value = s.split(",");

                Entry entry = null;
                try {
                    historyTime = simpleDateFormat.parse(value[0]);
                    entry = new Entry(historyTime.getTime(), Float.parseFloat(value[1]));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                values.add(entry);
            }

            LineDataSet lineDataSet = new LineDataSet(Lists.reverse(values), symbol);
            lineDataSet.setValueTextColor(Color.GREEN);
            lineDataSet.setValueTextSize(10f);
            iLineDataSets.add(lineDataSet);
        }

        LineData lineData = new LineData(iLineDataSets);
        lc_chart.setData(lineData);
        lc_chart.invalidate();

        // setup chart ui
        lc_chart.getDescription().setEnabled(false);
        lc_chart.getLegend().setEnabled(false);
        lc_chart.getXAxis().setDrawLabels(false);
        lc_chart.getAxisRight().setDrawLabels(false);
        lc_chart.getAxisLeft().setTextColor(Color.WHITE);
    }
}
