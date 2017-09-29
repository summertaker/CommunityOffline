package com.summertaker.communityoffline;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.summertaker.communityoffline.article.ArticleListFragment;
import com.summertaker.communityoffline.common.BaseActivity;
import com.summertaker.communityoffline.common.BaseApplication;
import com.summertaker.communityoffline.util.SlidingTabLayout;

import java.io.File;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, ArticleListFragment.ArticleListFragmentListener {

    private static final int REQUEST_PERMISSION_CODE = 100;

    private Toolbar mToolbar;
    private ProgressBar mPbToolbar;

    private ActionBarDrawerToggle mDrawerToggle;

    private SectionsPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    private final static int DOWNLOAD_REQUEST_CODE = 900;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runFragment("goTop");
            }
        });

        //----------------------------------------------------------------------------
        // 런타임에 권한 요청
        // https://developer.android.com/training/permissions/requesting.html?hl=ko
        //----------------------------------------------------------------------------
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //Log.d(mTag, "onRequestPermissionsResult().....");

        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //-------------------
                    // 디렉토리 생성하기
                    //-------------------
                    String path = BaseApplication.getDataPath();
                    //Log.d(mTag, "path: " + path);

                    File dir = new File(path);
                    if (!dir.exists()) {
                        boolean isSuccess = dir.mkdirs();
                        if (isSuccess) {
                            //Log.d(mTag, "created.");
                        } else {
                            //Log.d(mTag, "mkdir failed.");
                        }
                    } else {
                        //Log.d(mTag, "exists.");
                    }
                    initUI();
                } else {
                    onPermissionDenied();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void onPermissionDenied() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("권한이 거부되었습니다.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    private void initUI() {
        mPbToolbar = findViewById(R.id.toolbar_progress_bar);
        //mPbLoading = findViewById(R.id.pbLoading);
        //mLoLoadMore = findViewById(R.id.loLoadMore);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                runFragment("goBack");
            }
        });
        */

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(mPagerAdapter);

        //-------------------------------------------------------------------------------------------------------
        // 뷰페이저 간 이동 시 프레그먼트 자동으로 새로고침 방지
        // https://stackoverflow.com/questions/28494637/android-how-to-stop-refreshing-fragments-on-tab-change
        //-------------------------------------------------------------------------------------------------------
        mViewPager.setOffscreenPageLimit(BaseApplication.getInstance().getSiteList().size());

        SlidingTabLayout slidingTabLayout = findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_download:
                goDownload();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            runFragment("goBack");
        }
    }

    private void goDownload() {
        Intent intent = new Intent(this, DownloadActivity.class);
        //intent.putExtra("url", url);
        startActivityForResult(intent, DOWNLOAD_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DOWNLOAD_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                refreshFragment();
            }
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ArticleListFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return BaseApplication.getInstance().getSiteList().size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return BaseApplication.getInstance().getSiteData(position).getTitle();
        }
    }

    public void runFragment(String command) {
        //--------------------------------------------------------------------------------------------
        // 프레그먼트에 이벤트 전달하기
        // https://stackoverflow.com/questions/34861257/how-can-i-set-a-tag-for-viewpager-fragments
        //--------------------------------------------------------------------------------------------
        Fragment f = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + mViewPager.getCurrentItem());

        // based on the current position you can then cast the page to the correct Fragment class
        // and call some method inside that fragment to reload the data:
        //if (0 == mViewPager.getCurrentItem() && null != f) {
        if (f == null) {
            if ("goBack".equals(command)) {
                super.onBackPressed();
            }
        } else {
            //WebFragment wf = (WebFragment) f;
            ArticleListFragment wf = (ArticleListFragment) f;

            switch (command) {
                case "goBack":
                    boolean canGoBack = wf.goBack();
                    if (!canGoBack) {
                        super.onBackPressed();
                    }
                    break;
                case "goTop":
                    wf.goTop();
                    break;
                case "open_in_new":
                    wf.openInNew();
                    break;
                case "share":
                    wf.share();
                    break;
            }
        }
    }

    public void refreshFragment() {
        //--------------------------------------------------------------------------------------------
        // 프레그먼트에 이벤트 전달하기
        // https://stackoverflow.com/questions/34861257/how-can-i-set-a-tag-for-viewpager-fragments
        //--------------------------------------------------------------------------------------------
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            Fragment f = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + i);
            ((ArticleListFragment) f).refresh();
        }
    }

    @Override
    public void onArticleListFragmentEvent(String event) {
        switch (event) {
            case "onLoadDataStarted":
                //mLoLoadMore.setVisibility(View.VISIBLE);
                break;
            case "onLoadDataFinished":
                //mPbLoading.setVisibility(View.GONE);
                //mLoLoadMore.setVisibility(View.GONE);
                break;
        }
    }
}
