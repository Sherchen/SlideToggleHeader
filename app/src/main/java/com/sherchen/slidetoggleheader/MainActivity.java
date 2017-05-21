package com.sherchen.slidetoggleheader;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sherchen.slidetoggleheader.adapter.ListBaseAdapter;
import com.sherchen.slidetoggleheader.views.ObservableScrollViewCallbacks;
import com.sherchen.slidetoggleheader.views.ObservableXListView;
import com.sherchen.slidetoggleheader.views.ScrollState;
import com.sherchen.slidetoggleheader.views.ScrollUtils;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

public class MainActivity extends AppCompatActivity {

    ObservableXListView slideListView;
    PtrClassicFrameLayout pcflContent;
    /**滑动隐藏或者显示文字*/
    TextView mTvHeaderToggle;
    /**能够滑动的header*/
    private View mVHeader;

    /**layout已经结束*/
    private boolean mLayoutFinished;
    /**背景图片高度*/
    private int mBgHeight;
    /**固定栏高度*/
    private int mStickHeight;
    /**能够滑动的最大距离*/
    private int maxScrollY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        slideListView = (ObservableXListView) findViewById(android.R.id.list);
        pcflContent = (PtrClassicFrameLayout) findViewById(R.id.pcfl_main_content);
        mVHeader = findViewById(R.id.rl_header);
        mTvHeaderToggle = (TextView) findViewById(R.id.tv_header_toggle);
        mBgHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
        mStickHeight = getResources().getDimensionPixelSize(R.dimen.header_sticky_height);
        initPcfl();
        initSlide();
        showContents();
    }

    private void initSlide(){
        slideListView.setPullRefreshEnable(false);
        ScrollUtils.addOnGlobalLayoutListener(slideListView, new Runnable() {
            @Override
            public void run() {
                mLayoutFinished = true;
                updateScroll(slideListView.getCurrentScrollY());
            }
        });
        slideListView.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
            @Override
            public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
                if(!mLayoutFinished) return;
                updateScroll(scrollY);
            }

            @Override
            public void onDownMotionEvent() {

            }

            @Override
            public void onUpOrCancelMotionEvent(ScrollState scrollState) {
            }
        });

        //为ListView添加看不见的header，这个多出的地方是用来给滑动使用的。
        View headerView = new View(this);
        headerView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mBgHeight));
        headerView.setMinimumHeight(mBgHeight);
        // This is required to disable header's list selector effect
        headerView.setClickable(true);
        slideListView.addHeaderView(headerView);
    }

    private void updateScroll(int scrollY){
        if(maxScrollY == 0) {
            maxScrollY = mBgHeight - mStickHeight;
        }
        if(scrollY < 0){
            scrollY = 0;
        }
        int newScroll = scrollY;
        if(newScroll > maxScrollY) {
            newScroll = maxScrollY;
        }
        float alpha = ScrollUtils.getFloat((float) newScroll / maxScrollY, 0, 1);
        mTvHeaderToggle.setBackgroundColor(ScrollUtils.getColorWithAlpha(alpha, Color.parseColor("#FF61A0FF")));
        mTvHeaderToggle.setTextColor(ScrollUtils.getColorWithAlpha(alpha, Color.parseColor("#000000")));
        ViewCompat.setTranslationY(mVHeader, -newScroll);
    }

    private void initPcfl(){
        pcflContent.disableWhenHorizontalMove(true);
        pcflContent.setLastUpdateTimeRelateObject(this);
        pcflContent.setDurationToCloseHeader(1500);
        pcflContent.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, slideListView, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                Toast.makeText(MainActivity.this, "刷新中...", Toast.LENGTH_SHORT).show();
                if(pcflContent != null){
                    pcflContent.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(pcflContent != null){
                                pcflContent.refreshComplete();
                            }
                        }
                    }, 300);
                }
            }
        });
    }

    private void showContents(){
        List<Integer> pics = new ArrayList<>();
        pics.add(R.mipmap.p1);
        pics.add(R.mipmap.p2);
        pics.add(R.mipmap.p3);
        pics.add(R.mipmap.p4);
        pics.add(R.mipmap.p5);
        pics.add(R.mipmap.p6);
        pics.add(R.mipmap.p7);
        pics.add(R.mipmap.p8);
        slideListView.setAdapter(new TravelAdapter(this, pics));
    }

    private class TravelAdapter extends ListBaseAdapter<Integer, Holder> {

        public TravelAdapter(Context context, List<Integer> list) {
            super(context, R.layout.item_travels, list);
        }

        @Override
        public void bindView(Holder holder, View convertView) {
            holder.tvName  = (TextView) convertView.findViewById(R.id.tv_name);
            holder.ivProfile = (ImageView) convertView.findViewById(R.id.iv_profile);
        }

        @Override
        public Holder getViewHolder(View content) {
            return new Holder();
        }

        @Override
        public void setViewContent(Holder holder, Integer integer, View convertView, int position) {
            holder.tvName.setText("第" + (position + 1) + "个景点");
            holder.ivProfile.setImageResource(integer);
        }
    }

    class Holder {
        TextView tvName;
        ImageView ivProfile;
    }
}
