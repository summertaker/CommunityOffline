package com.summertaker.communityoffline.common;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class BaseDataAdapter extends BaseAdapter {

    protected String mTag;

    public BaseDataAdapter() {
        this.mTag = "===== " + this.getClass().getSimpleName();
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}