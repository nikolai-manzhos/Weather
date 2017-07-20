package ru.yamblz.weather.ui.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.gcm.GcmNetworkManager;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;
import ru.yamblz.weather.R;
import ru.yamblz.weather.data.local.AppPreferenceManager;
import ru.yamblz.weather.ui.about.AboutViewImpl;
import ru.yamblz.weather.ui.base.BaseActivity;
import ru.yamblz.weather.ui.overview.OverviewViewImpl;
import ru.yamblz.weather.ui.settings.SettingsViewImpl;
import ru.yamblz.weather.utils.UpdateTaskService;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @Inject
    AppPreferenceManager preferenceManager;

    @State
    int fragmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActivityComponent().inject(this);
        Icepick.restoreInstanceState(this, savedInstanceState);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        if (savedInstanceState == null) {
            checkFirstTimeUser();
            fragmentId = R.id.nav_overview;
            selectItem(fragmentId);
            navigationView.setCheckedItem(fragmentId);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id != fragmentId) {
            fragmentId = id;
            selectItem(id);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void selectItem(int id) {
        switch (id) {
            case R.id.nav_overview:
                replaceFragment(R.id.contentFrame, new OverviewViewImpl());
                break;
            case R.id.nav_settings:
                replaceFragment(R.id.contentFrame, new SettingsViewImpl());
                break;
            case R.id.nav_about:
                replaceFragment(R.id.contentFrame, new AboutViewImpl());
                break;
            default:
                replaceFragment(R.id.contentFrame, new OverviewViewImpl());
                break;
        }
    }

    private void checkFirstTimeUser() {
        if (preferenceManager.getFirstTimeUser()) {
            GcmNetworkManager gcmNetworkManager = GcmNetworkManager.getInstance(getApplicationContext());
            UpdateTaskService.startUpdateTask(gcmNetworkManager, preferenceManager.getCurrentUpdateInterval());
            preferenceManager.setFirstTimeUser(false);
        }
    }
}