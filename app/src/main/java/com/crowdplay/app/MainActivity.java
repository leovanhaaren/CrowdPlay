package com.crowdplay.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class MainActivity extends ActionBarActivity implements ServiceConnection {

    private static final String TAG = "MainActivity";

    private String[]     mMenuItems;
    private DrawerLayout mDrawerLayout;
    private ListView     mDrawerList;

    private CharSequence          mTitle;
    private ActionBarDrawerToggle mDrawerToggle;

    private MusicService musicService;
    private Intent       musicServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get default title
        mTitle = getString(R.string.room_overview_title);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList   = (ListView)     findViewById(R.id.left_drawer);

        // Load the drawer items from xml
        mMenuItems = getResources().getStringArray(R.array.drawer_menu);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mMenuItems));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_navigation_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(R.string.drawer_title);
                invalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportFragmentManager().addOnBackStackChangedListener(new android.support.v4.app.FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                setActionBarArrowDependingOnFragmentsBackStack();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Disable logo in action bar
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Init imageloader lib
        initImageLoader(getApplicationContext());
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MusicBinder binder = (MusicService.MusicBinder) service;

        musicService = binder.getService();
        Log.v(TAG, "Music service connected ");

        // update the main content by loading the default fragment
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new RoomOverviewFragment())
                .commit();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.v(TAG, "Music service disconnected");
    }

    @Override
    public void onStart() {
        super.onStart();

        if(musicServiceIntent == null){
            musicServiceIntent = new Intent(this, MusicService.class);
            this.bindService(musicServiceIntent, this, Context.BIND_AUTO_CREATE);
            this.startService(musicServiceIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        this.stopService(musicServiceIntent);
        musicService = null;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.isDrawerIndicatorEnabled() && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == android.R.id.home && getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);

        // update the main content by replacing fragments
        Fragment fragment = null;
        FragmentManager fragmentManager = getFragmentManager();
        switch(position) {
            case 0:
                fragment = new RoomOverviewFragment();
                mTitle   = getString(R.string.room_overview_title);
                break;
            case 1:
                fragment = new MusicOverviewFragment();
                mTitle   = getString(R.string.music_overview_title);
                break;
            case 2:
                fragment = new AboutFragment();
                mTitle   = getString(R.string.about_title);
                break;
        }
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void setActionBarArrowDependingOnFragmentsBackStack() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        boolean shouldEnableDrawerIndicator = backStackEntryCount == 0;
        mDrawerToggle.setDrawerIndicatorEnabled(shouldEnableDrawerIndicator);
    }

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                //.writeDebugLogs() // Remove for release app
                .build();

        ImageLoader.getInstance().init(config);
    }

    public MusicService getMusicService() {
        return musicService;
    }

}