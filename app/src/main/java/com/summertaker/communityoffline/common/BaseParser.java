package com.summertaker.communityoffline.common;

public class BaseParser {

    protected String mTag;

    public BaseParser() {
        mTag = "========== " + this.getClass().getSimpleName();
    }
}