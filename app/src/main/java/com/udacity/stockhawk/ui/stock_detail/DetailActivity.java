package com.udacity.stockhawk.ui.stock_detail;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.ui.MainActivity;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.vp_chart)
    ViewPager vp_chart;

    @BindView(R.id.tab_period)
    TabLayout tab_period;

    private String symbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setupEnv();
        setupPager();
    }

    private void setupEnv() {
        ButterKnife.bind(this);

        symbol = getIntent().getStringExtra(MainActivity.STOCK_SYMBOL);
        getSupportActionBar().setTitle(symbol);
    }

    private void setupPager() {
        DetailAdapter detailAdapter = new DetailAdapter(getSupportFragmentManager());

        detailAdapter.addFragment(setFragment(Calendar.DATE, 7), "Week");
        detailAdapter.addFragment(setFragment(Calendar.MONTH, 1), "1 month");
        detailAdapter.addFragment(setFragment(Calendar.MONTH, 3), "3 months");

        tab_period.setupWithViewPager(vp_chart, true);
        vp_chart.setAdapter(detailAdapter);
        vp_chart.setOffscreenPageLimit(2);
    }

    private DetailFragment setFragment(int period, int between) {
        DetailFragment detailFragment = new DetailFragment();
        Bundle bundle = new Bundle();
        Calendar calendar = Calendar.getInstance();

        calendar.add(period, -between);
        bundle.putLong(DetailFragment.SELECTED_PERIOD, calendar.getTimeInMillis());
        bundle.putString(DetailFragment.SELECTED_SYMBOL, symbol);
        detailFragment.setArguments(bundle);

        return detailFragment;
    }
}
