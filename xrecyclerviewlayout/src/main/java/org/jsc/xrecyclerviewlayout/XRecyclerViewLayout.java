package org.jsc.xrecyclerviewlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by jsc on 2016/8/11.
 */
public class XRecyclerViewLayout extends RelativeLayout implements XRecyclerView.ActionCallBack{

    private XRecyclerView recyclerView;
    private LoadingMoreFooter loadingMoreFooter;

    public XRecyclerViewLayout(Context context) {
        this(context, null);
    }

    public XRecyclerViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XRecyclerViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init(){
        inflate(getContext(), R.layout.recycler_view_layout, this);
        recyclerView = (XRecyclerView) findViewById(R.id.recycler_view);
        loadingMoreFooter = (LoadingMoreFooter) findViewById(R.id.loading_more_footer);
        loadingMoreFooter.setProgressStyle(ProgressStyle.BallBeat);

        loadingMoreFooter.setVisibility(GONE);

        recyclerView.setCallBack(this);
    }

    public XRecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    public void onActionCallBack(XRecyclerView.ActionType type) {
        if (loadingMoreFooter.getVisibility() == GONE)
            loadingMoreFooter.setVisibility(VISIBLE);
        switch (type){
            case ACTION_LOADING:
                removeCallbacks(runnable);
                loadingMoreFooter.setState(LoadingMoreFooter.STATE_LOADING);
                break;
            case ACTION_COMPLETE:
                removeCallbacks(runnable);
                loadingMoreFooter.setState(LoadingMoreFooter.STATE_COMPLETE);
                postDelayed(runnable, 500);
                break;
            case ACTION_NO_MORE:
                removeCallbacks(runnable);
                loadingMoreFooter.setState(LoadingMoreFooter.STATE_NO_MORE);
                postDelayed(runnable, 500);
                break;
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (loadingMoreFooter != null && loadingMoreFooter.getVisibility() == VISIBLE)
                loadingMoreFooter.setVisibility(GONE);
        }
    };
}
