package net.simonvt.menudrawer.samples;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class MenuListView extends ListView {

    public interface OnScrollChangedListener {

        void onScrollChanged();
    }

    private OnScrollChangedListener mOnScrollChangedListener;

    public MenuListView(Context context) {
        super(context);
    }

    public MenuListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MenuListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (mOnScrollChangedListener != null) mOnScrollChangedListener.onScrollChanged();
    }

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        mOnScrollChangedListener = listener;
    }
}
