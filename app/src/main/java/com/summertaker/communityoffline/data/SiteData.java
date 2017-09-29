package com.summertaker.communityoffline.data;

public class SiteData {
    private String title;
    private String userAgent;
    private String url;
    private String pageParam;
    private int maxDownloadPage;

    public SiteData(String title, String userAgent, String url, String pageParam, int maxDownloadPage) {
        this.title = title;
        this.userAgent = userAgent;
        this.url = url;
        this.pageParam = pageParam;
        this.maxDownloadPage = maxDownloadPage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPageParam() {
        return pageParam;
    }

    public void setPageParam(String pageParam) {
        this.pageParam = pageParam;
    }

    public int getMaxDownloadPage() {
        return maxDownloadPage;
    }

    public void setMaxDownloadPage(int maxDownloadPage) {
        this.maxDownloadPage = maxDownloadPage;
    }
}
