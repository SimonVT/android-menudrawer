package net.simonvt.menudrawer.samples;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SamplesActivity extends ListActivity {

    private SamplesAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SamplesAdapter();

        mAdapter.addHeader("Sliding drawer");
        mAdapter.addSample("Left drawer", "Only the content area is dragged.", LeftDrawerSample.class);
        mAdapter.addSample("Right drawer", "The menu is positioned to the right of the content",
                RightDrawerSample.class);
        mAdapter.addSample("Top drawer", "The menu is positioned above the content", TopDrawerSample.class);
        mAdapter.addSample("Bottom drawer", "The menu is positioned below the content", BottomDrawerSample.class);
        mAdapter.addSample("ListActivity sample", "Shows how to use the drawer with a ListActivity.",
                ListActivitySample.class);
        mAdapter.addSample("Window sample", "The entire window is dragged.", WindowSample.class);
        mAdapter.addSample("ViewPager",
                "A left drawer that can only be dragged open when the ViewPager is on the first page",
                ViewPagerSample.class);
        mAdapter.addSample("Layout xml", "The drawer and its menu and content is defined in XML", LayoutSample.class);
        mAdapter.addSample("Fragments", "Sample that uses fragments as the content", FragmentSample.class);
        mAdapter.addSample("ActionBarSherlock sample", "Showcases the drawer used with ActionBarSherlock.",
                ActionBarSherlockSample.class);

        mAdapter.addHeader("Static drawer");
        mAdapter.addSample("Static drawer", "The drawer is always visible", StaticDrawerSample.class);

        mAdapter.addHeader("Overlay drawer");
        mAdapter.addSample("Left overlay", "The drawer can be dragged in from the left.",
                LeftOverlaySample.class);
        mAdapter.addSample("Top overlay", "The drawer can be dragged in from the top.",
                TopOverlaySample.class);
        mAdapter.addSample("Right overlay", "The drawer can be dragged in from the right.",
                RightOverlaySample.class);
        mAdapter.addSample("Bottom overlay", "The drawer can be dragged in from the bottom.",
                BottomOverlaySample.class);

        setListAdapter(mAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        SampleItem sample = (SampleItem) mAdapter.getItem(position);
        Intent i = new Intent(this, sample.mClazz);
        startActivity(i);
    }

    private static class Header {

        String mTitle;

        public Header(String title) {
            mTitle = title;
        }
    }

    private static class SampleItem {

        String mTitle;
        String mSummary;
        Class mClazz;

        public SampleItem(String title, String summary, Class clazz) {
            mTitle = title;
            mSummary = summary;
            mClazz = clazz;
        }
    }

    public class SamplesAdapter extends BaseAdapter {

        private List<Object> mItems = new ArrayList<Object>();

        public void addHeader(String title) {
            mItems.add(new Header(title));
        }

        public void addSample(String title, String summary, Class clazz) {
            mItems.add(new SampleItem(title, summary, clazz));
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position) instanceof Header ? 0 : 1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (getItemViewType(position) == 0) {
                TextView v = (TextView) convertView;
                if (v == null) {
                    v = (TextView) getLayoutInflater().inflate(R.layout.list_row_sample_header, parent, false);
                }

                v.setText(((Header) getItem(position)).mTitle);

                return v;

            } else {
                SampleItem sample = (SampleItem) getItem(position);

                View v = convertView;
                if (v == null) {
                    v = getLayoutInflater().inflate(R.layout.list_row_sample, parent, false);
                }

                ((TextView) v.findViewById(R.id.title)).setText(sample.mTitle);
                ((TextView) v.findViewById(R.id.summary)).setText(sample.mSummary);

                return v;
            }
        }
    }
}
