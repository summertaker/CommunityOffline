package com.summertaker.communityoffline.article;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.bluzwong.swipeback.SwipeBackActivityHelper;
import com.summertaker.communityoffline.R;
import com.summertaker.communityoffline.common.BaseApplication;
import com.summertaker.communityoffline.data.ArticleListData;
import com.summertaker.communityoffline.data.SiteData;
import com.summertaker.communityoffline.parser.RuliwebParser;
import com.summertaker.communityoffline.parser.TodayhumorParser;
import com.summertaker.communityoffline.util.EndlessScrollListener;
import com.summertaker.communityoffline.util.Util;

import java.util.ArrayList;

public class ArticleListFragment extends Fragment implements ArticleListInterface {

    private String mTag = "== " + this.getClass().getSimpleName();
    //private String mVolleyTag = mTag;

    private ArticleListFragmentListener mListener;

    //private LinearLayout mLoLoading;
    //private ProgressBar mPbLoading;
    //private LinearLayout mLoLoadMore;

    //private String mUserAgent;
    //private String mRequestUrl;

    private SiteData mSiteData;
    private int mCurrentPage = 1;
    private boolean mIsLoading = false;
    private boolean mIsDataExists = true;

    private ArrayList<ArticleListData> mArticleList;
    private ArticleListAdapter mAdapter;
    private ListView mListView;
    private EndlessScrollListener mEndlessScrollListener;

    private boolean mIsReloadMode = false;

    // Container Activity must implement this interface
    public interface ArticleListFragmentListener {
        public void onArticleListFragmentEvent(String event);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            Activity activity = (Activity) context;

            // This makes sure that the container activity has implemented
            // the callback interface. If not, it throws an exception
            try {
                mListener = (ArticleListFragmentListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
            }
        }
    }

    public ArticleListFragment() {
    }

    public static ArticleListFragment newInstance(int position) {
        ArticleListFragment fragment = new ArticleListFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        //args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.article_list_fragment, container, false);

        //mLoLoading = rootView.findViewById(R.id.loLoading);
        //mPbLoading = rootView.findViewById(R.id.pbLoading);
        //mLoLoadMore = rootView.findViewById(R.id.loLoadMore);

        mArticleList = new ArrayList<>();
        mAdapter = new ArticleListAdapter(getContext(), mArticleList);

        mListView = rootView.findViewById(R.id.listView);
        mListView.setAdapter(mAdapter);

        /*
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            int currentScrollState;
            int currentFirstVisibleItem;
            int currentVisibleItemCount;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                this.currentScrollState = scrollState;
                this.isScrollCompleted();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                this.currentFirstVisibleItem = firstVisibleItem;
                this.currentVisibleItemCount = visibleItemCount;
            }

            private void isScrollCompleted() {
                if (this.currentVisibleItemCount > 0 && this.currentScrollState == SCROLL_STATE_IDLE) {
                    //Log.d(mTag, "**********************************");
                    if (mCurrentPage <= mSiteData.getMaxDownloadPage()) {
                        loadData();
                    }
                }
            }
        });
        */

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ArticleListData data = (ArticleListData) adapterView.getItemAtPosition(i);

                String title = data.getTitle();
                String url = data.getUrl();

                //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                //startActivity(intent);

                //Intent intent = new Intent(getActivity(), WebViewActivity.class);
                //Intent intent = new Intent(getActivity(), WebActivity.class);

                //-------------------------------------------------------------------------
                // "Empty Activity" 템플릿 사용 시 툴바에 프로그레스바 표시할 때 사용하는
                // setSupportProgressBarIndeterminateVisibility(true);가 Deprecated 됨
                //-------------------------------------------------------------------------
                //Intent intent = new Intent(getActivity(), ArticleDetailActivity.class);
                // 그래서 "Basic Activity" 템플릿 사용해서 직접 프로그레스바를 추가함
                // https://stackoverflow.com/questions/27788195/setprogressbarindeterminatevisibilitytrue-not-working
                Intent intent = new Intent(getActivity(), ArticleViewActivity.class);

                intent.putExtra("title", title);
                intent.putExtra("url", url);
                startActivity(intent);
                //startActivityForResult(intent, 100);
                //getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        mEndlessScrollListener = new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                if (mIsDataExists) {
                    loadData();
                    return true; // ONLY if more data is actually being loaded; false otherwise.
                } else {
                    return false;
                }
            }
        };

        mListView.setOnScrollListener(mEndlessScrollListener);

        int position = getArguments().getInt("position");

        mSiteData = BaseApplication.getInstance().getSiteList().get(position);
        //mUserAgent = mSiteData.getUserAgent();
        //mRequestUrl = mSiteData.getUrl();

        loadData();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * 데이터 로드하기
     */
    private void loadData() {
        if (!mIsLoading) {
            mIsLoading = true;
            mListener.onArticleListFragmentEvent("onLoadDataStarted");

            requestData();
        }
    }

    private void requestData() {
        String url = mSiteData.getUrl();

        if (mCurrentPage > 1) {
            url += mSiteData.getPageParam() + mCurrentPage;
            //mLoLoadMore.setVisibility(View.VISIBLE);
        }
        //Log.e(mTag, "url: " + url);

        String fileName = Util.getUrlToFileName(url, ".html");
        String html = Util.readFromFile(fileName);

        if (!html.isEmpty()) {
            mIsDataExists = true;
            parseData(html);
        } else {
            mIsDataExists = false;
            renderData();
            /*
            StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //Log.d(mTag, response.toString());
                    parseData(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    parseData("");
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
            */
        }
    }

    private void parseData(String response) {
        if (mSiteData.getUrl().contains("todayhumor")) {
            TodayhumorParser todayhumorParser = new TodayhumorParser();
            todayhumorParser.parseList(response, mArticleList);
        } else if (mSiteData.getUrl().contains("ruliweb")) {
            RuliwebParser ruliwebParser = new RuliwebParser();
            ruliwebParser.parseList(response, mArticleList);
        }

        renderData();
    }

    private void renderData() {
        //Log.d(mTag, "mMemberList.size(): " + mMemberList.size());

        if (mCurrentPage == 1) {
            //mLoLoading.setVisibility(View.GONE);
            //mPbLoading.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }

        if (mIsDataExists) {
            mAdapter.notifyDataSetChanged();
            mCurrentPage++;
        }

        if (mIsReloadMode) {
            goTop();
            mIsReloadMode = false;
        }

        mIsLoading = false;
        mListener.onArticleListFragmentEvent("onLoadDataFinished");
    }

    public boolean goBack() {
        return false;
    }

    public void goTop() {
        //mListView.smoothScrollToPosition(0);
        //mListView.setSelection(0);
        mListView.setSelectionAfterHeaderView();
    }

    public void refresh() {
        //Log.e(mTag, "refresh()......");

        mArticleList.clear();
        mAdapter.notifyDataSetChanged();

        mCurrentPage = 1;

        mEndlessScrollListener.reset();
        mIsReloadMode = true;

        loadData();
    }

    public void openInNew() {
        //String url = mWebView.getUrl();
        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        //startActivity(intent);
    }

    public void share() {

    }

    @Override
    public void onPictureClick(int position, String imageUrl) {
        //Log.d(mTag, imageUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
        startActivity(intent);
    }

    @Override
    public void onTitleClick(int position) {

    }

    @Override
    public void onCloseClick(int position) {
        mArticleList.remove(position);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        //BaseApplication.getInstance().cancelPendingRequests(mVolleyTag);
    }
}
