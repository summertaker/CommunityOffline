package com.summertaker.communityoffline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.summertaker.communityoffline.common.BaseActivity;
import com.summertaker.communityoffline.common.BaseApplication;
import com.summertaker.communityoffline.data.ArticleListData;
import com.summertaker.communityoffline.data.SiteData;
import com.summertaker.communityoffline.parser.TodayhumorParser;
import com.summertaker.communityoffline.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadActivity extends BaseActivity {

    private List<SiteData> mSiteDataList;
    private SiteData mSiteData;
    private int mSiteCount = 0;

    private String mUrl;
    private int mPage = 0;
    private boolean mIsLoading = false;

    private ProgressBar mPbSite;
    private ProgressBar mPbPage;
    private TextView mTvSiteProgress;
    private TextView mTvPageProgress;

    private Button mBtnAppy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_activity);

        setBaseStatusBar(); // 상태바 설정
        setBaseToolbar(getString(R.string.download)); // 툴바 설정

        mSiteDataList = new ArrayList<>();
        mSiteDataList = BaseApplication.getInstance().getSiteList();

        mTvPageProgress = findViewById(R.id.tvPageProgress);
        mPbPage = findViewById(R.id.pbLoadingPage);
        mPbPage.setMax(mSiteDataList.get(mSiteCount).getMaxDownloadPage());

        mTvSiteProgress = findViewById(R.id.tvSiteProgress);
        mPbSite = findViewById(R.id.pbLoadingSite);
        mPbSite.setMax(mSiteDataList.size());

        // 다운로드 버튼
        Button btnDownload = findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
                loadData();
            }
        });

        // 적용하기 버튼
        mBtnAppy = findViewById(R.id.btnApply);
        mBtnAppy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doFinish();
            }
        });

        mSiteData = mSiteDataList.get(mSiteCount);

        setProgressBar();

        loadData();
    }

    /**
     * 데이터 로드하기
     */
    private void loadData() {
        if (mIsLoading) {
            return;
        }

        mIsLoading = true;

        requestData();
    }

    /**
     * 데이터 요청하기
     */
    private void requestData() {
        mUrl = mSiteData.getUrl();

        int targetPage = mPage + 1;
        if (targetPage > 1) {
            mUrl += mSiteData.getPageParam() + targetPage;
            //mLoLoadMore.setVisibility(View.VISIBLE);
        }
        //Log.e(mTag, "url: " + mUrl);

        StringRequest strReq = new StringRequest(Request.Method.GET, mUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.d(mTag, response.toString());
                writeData(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                writeData("");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                //headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("User-agent", mSiteData.getUserAgent());
                return headers;
            }
        };

        BaseApplication.getInstance().addToRequestQueue(strReq, mVolleyTag);
    }

    private void writeData(String response) {
        /*Document doc = Jsoup.parse(response);

        Element root = null;
        if (mSiteData.getUrl().contains("todayhumor")) {
            root = doc.select("body").first();
            //Log.d(mTag, "root.html(): " + root.html());
        }

        if (root != null) {
            String fileName = Util.getUrlToFileName(mUrl, ".html");
            Log.d(mTag, "fileName: " + fileName);
            Util.writeToFile(fileName, root.html());
        }
        */

        String fileName = Util.getUrlToFileName(mUrl, ".html");
        //Log.d(mTag, "fileName: " + fileName);
        Util.writeToFile(fileName, response);

        //ArrayList<ArticleListData> articleListData = new ArrayList<>();
        //TodayhumorParser todayhumorParser = new TodayhumorParser();
        //todayhumorParser.parseList(response, articleListData);

        mPage++;

        if ((mPage + 1) > mSiteData.getMaxDownloadPage()) {
            mSiteCount++;
            setProgressBar();
            mPage = 0;
        } else {
            setProgressBar();
        }

        mIsLoading = false;

        if (mSiteCount < mSiteDataList.size()) {
            mSiteData = mSiteDataList.get(mSiteCount);

            if (mPage < mSiteData.getMaxDownloadPage()) {
                loadData();
            }
        } else {
            // 데이터 로드 완료
            //mBtnAppy.setVisibility(View.VISIBLE);

            doFinish();
        }
    }

    public void setProgressBar() {
        String pageProgress = mPage + " / " + mSiteData.getMaxDownloadPage();
        mTvPageProgress.setText(pageProgress);
        mPbPage.setProgress(mPage);

        String siteProgress = mSiteCount + " / " + mSiteDataList.size();
        mTvSiteProgress.setText(siteProgress);
        mPbSite.setProgress(mSiteCount);
    }

    public void doFinish() {
        Intent data = new Intent();
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
