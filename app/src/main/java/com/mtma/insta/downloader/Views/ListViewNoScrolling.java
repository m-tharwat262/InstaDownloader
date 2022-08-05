package com.mtma.insta.downloader.Views;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ListViewNoScrolling extends ListView {


    public ListViewNoScrolling(Context context) {
        super(context);
    }

    public ListViewNoScrolling(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListViewNoScrolling(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}

