package org.jsc.xrecyclerviewlayout;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public class XRecyclerView extends RecyclerView {

    private final String TAG = "XRecyclerView";
    private static final float DRAG_RATE = 3;

    private boolean isLoadingData = false;
    private boolean haveMore = false;
    private float mLastY = -1;
    private WrapAdapter mWrapAdapter;

    private boolean pullRefreshEnabled = true;
    private boolean loadingMoreEnabled = true;
    private boolean isHeadViewAdded = false;
    private boolean isEmptyViewAdded = false;
    private boolean itemClickable = true;
    private boolean itemLongClickable = true;

    //下面的ItemViewType是保留值(ReservedItemViewType),如果用户的adapter与它们重复将会强制抛出异常。不过为了简化,我们检测到重复时对用户的提示是ItemViewType必须小于10000
    private static final int TYPE_REFRESH_HEADER = 10000;//设置一个很大的数字,尽可能避免和用户的adapter冲突
    private static final int TYPE_HEAD = 10001;
    private static final int TYPE_EMPTY = 10002;

    private ArrowRefreshHeader mRefreshHeaderView;
    private View mHeadView;
    private EmptyView mEmptyView;

    //adapter没有数据的时候显示,类似于listView的emptyView

    private final AdapterDataObserver mDataObserver = new DataObserver();
    private AppBarStateChangeListener.State appbarState = AppBarStateChangeListener.State.EXPANDED;

    private ItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;
    private LoadingListener mLoadingListener;
    private ActionCallBack mCallBack;
    public interface LoadingListener {

        void onRefresh();

        void onLoadMore();
    }

    public interface ActionCallBack{
        public void onActionCallBack(ActionType type);
    }

    public enum ActionType{
        ACTION_LOADING, ACTION_COMPLETE, ACTION_NO_MORE
    }

    public interface ItemClickListener {
        public void onRecyclerViewItemClick(View view, int LayoutPosition, int DataPosition);
    }

    public interface ItemLongClickListener {
        public void onRecyclerViewItemLongClick(View view, int LayoutPosition, int DataPosition);
    }

    public XRecyclerView(Context context) {
        this(context, null);
    }

    public XRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mRefreshHeaderView = new ArrowRefreshHeader(getContext());
        mEmptyView = new EmptyView(getContext());
    }

    public void setHeadView(View view) {
        mHeadView = view;
        isHeadViewAdded = true;
    }

    private void addEmptyView(){
        if (isEmptyViewAdded)
            return;
        isEmptyViewAdded = true;
    }

    private void removeEmptyView(){
        if (!isEmptyViewAdded)
            return;
        isEmptyViewAdded = false;
    }

    public void showEmptyView(EmptyView.EmptyStatus status, String txt){
        mEmptyView.setState(status, txt);
    }

    //判断是否是XRecyclerView保留的itemViewType
    private boolean isReservedItemViewType(int itemViewType) {
        if(itemViewType == TYPE_REFRESH_HEADER || itemViewType == TYPE_HEAD || itemViewType == TYPE_EMPTY) {
            return true;
        } else {
            return false;
        }
    }

    public void setLoadingListener(LoadingListener listener) {
        mLoadingListener = listener;
    }

    public void setCallBack(ActionCallBack mCallBack) {
        this.mCallBack = mCallBack;
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setItemLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    public boolean isLoadingData() {
        return isLoadingData;
    }

    public void setItemClickable(boolean itemClickable) {
        this.itemClickable = itemClickable;
    }

    public void setItemLongClickable(boolean itemLongClickable) {
        this.itemLongClickable = itemLongClickable;
    }

    /**
     * Start refreshing.
     * @param refreshing
     */
    public void setRefreshing(boolean refreshing) {
        if (refreshing && pullRefreshEnabled && mLoadingListener != null) {
            scrollToPosition(0);
            int height = mRefreshHeaderView.getMeasuredHeight();
            if (height == 0)
                height = getResources().getDimensionPixelSize(R.dimen.head_view_max_height);
            mRefreshHeaderView.onMove(height);
            mRefreshHeaderView.setState(ArrowRefreshHeader.STATE_REFRESHING);
            isLoadingData = true;
            mLoadingListener.onRefresh();
        }
    }

    /**
     * Refresh completed.
     */
    public void refreshComplete() {
        mRefreshHeaderView.refreshComplete();
        isLoadingData = false;
    }

    /**
     * Load more completed.
     */
    public void loadMoreComplete() {
        isLoadingData = false;
        if (mCallBack != null)
            mCallBack.onActionCallBack(ActionType.ACTION_COMPLETE);
    }

    /**
     * Have more date?
     * @param count
     * @param total
     */
    public void isHaveMore(int count, int total){
        if (total > 0 && count >= total)
            isHaveMore(true);
        else
            isHaveMore(false);
    }

    public void isHaveMore(boolean haveMore){
        this.haveMore = haveMore;
    }

    /**
     * Enable or disable refresh action.
     * @param enabled
     */
    public void setPullRefreshEnabled(boolean enabled) {
        pullRefreshEnabled = enabled;
    }

    /**
     * Enable or disable loading more action.
     * @param enabled
     */
    public void setLoadingMoreEnabled(boolean enabled) {
        loadingMoreEnabled = enabled;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mWrapAdapter = new WrapAdapter(adapter);
        super.setAdapter(mWrapAdapter);
        adapter.registerAdapterDataObserver(mDataObserver);
        mDataObserver.onChanged();
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE && mLoadingListener != null && !isLoadingData && loadingMoreEnabled) {
            LayoutManager layoutManager = getLayoutManager();
            int lastVisibleItemPosition;
            if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                lastVisibleItemPosition = findMax(into);
            } else {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }

            if (layoutManager.getChildCount() > 0
                    && lastVisibleItemPosition >= layoutManager.getItemCount() - 1
                    && layoutManager.getItemCount() > layoutManager.getChildCount()
                    && mRefreshHeaderView.getState() < ArrowRefreshHeader.STATE_REFRESHING) {

                if (mCallBack != null && haveMore){
                    isLoadingData = true;
                    mCallBack.onActionCallBack(ActionType.ACTION_LOADING);
                    mLoadingListener.onLoadMore();
                    return;
                }

                if (mCallBack != null && !haveMore && !isEmptyViewAdded){
                    mCallBack.onActionCallBack(ActionType.ACTION_NO_MORE);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                if (isOnTop() && pullRefreshEnabled && appbarState == AppBarStateChangeListener.State.EXPANDED) {
                    mRefreshHeaderView.onMove(deltaY / DRAG_RATE);
                    if (mRefreshHeaderView.getVisibleHeight() > 0 && mRefreshHeaderView.getState() < ArrowRefreshHeader.STATE_REFRESHING) {
                        return false;
                    }
                }
                break;
            default:
                mLastY = -1; // reset
                if (isOnTop() && pullRefreshEnabled && appbarState == AppBarStateChangeListener.State.EXPANDED) {
                    if (mRefreshHeaderView.releaseAction()) {
                        if (mLoadingListener != null) {
                            isLoadingData = true;
                            mLoadingListener.onRefresh();
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private boolean isOnTop() {
        if (mRefreshHeaderView.getParent() != null) {
            return true;
        } else {
            return false;
        }
    }

    private class DataObserver extends AdapterDataObserver {
        @Override
        public void onChanged() {
            if (mWrapAdapter == null)
                return;

            Adapter<?> adapter = getAdapter();
            if (adapter != null) {
                int count = adapter.getItemCount();
                if (count == mWrapAdapter.getExtraViewCount()) {
                    addEmptyView();
                } else {
                    removeEmptyView();
                }
            }
            mWrapAdapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mWrapAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    };

    public class WrapAdapter extends Adapter<ViewHolder> implements View.OnClickListener,View.OnLongClickListener {

        private Adapter adapter;

        public WrapAdapter(Adapter adapter) {
            this.adapter = adapter;
        }

        public int getExtraViewCount(){
            //refresh_header
            int extraViewCount = 1;

            if (isHeadViewAdded)
                extraViewCount ++;

            if (isEmptyViewAdded)
                extraViewCount ++;

            return extraViewCount;
        }

        public boolean isRefreshHeader(int position) {
            return position == 0;
        }

        public boolean isHeader(int position) {
            return isHeadViewAdded && position == 1;
        }

        public boolean isEmpty(int position){
            int realPos = 1;
            if (isHeadViewAdded)
                realPos ++;

            return isEmptyViewAdded && position == realPos;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_REFRESH_HEADER) {
                return new SimpleViewHolder(mRefreshHeaderView);
            } else if (viewType == TYPE_HEAD) {
                return new SimpleViewHolder(mHeadView);
            } else if (viewType == TYPE_EMPTY) {
                return new SimpleViewHolder(mEmptyView);
            }
            ViewHolder holder = adapter.onCreateViewHolder(parent, viewType);
            if (itemClickable)
                holder.itemView.setOnClickListener(this);
            if (itemLongClickable)
                holder.itemView.setOnLongClickListener(this);
            return holder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            if (isRefreshHeader(position)
                    || isHeader(position)
                    || isEmpty(position)) {
                return;
            }
            int adjPosition = position - getExtraViewCount();
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                if (adjPosition < adapterCount) {
                    adapter.onBindViewHolder(holder, adjPosition);
                    return;
                }
            }
        }

        @Override
        public int getItemCount() {
            int extraViewCount = getExtraViewCount();
            extraViewCount += adapter == null? 0 : adapter.getItemCount();

            return extraViewCount;
        }

        @Override
        public int getItemViewType(int position) {
            if (isRefreshHeader(position)) {
                return TYPE_REFRESH_HEADER;
            }
            if (isHeader(position)) {
                return TYPE_HEAD;
            }
            if (isEmpty(position)) {
                return TYPE_EMPTY;
            }

            int adjPosition = position - getExtraViewCount();
            if(isReservedItemViewType(adapter.getItemViewType(adjPosition))) {
                throw new IllegalStateException("XRecyclerView require itemViewType in adapter should be less than 10000 " );
            }


            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                if (adjPosition < adapterCount) {
                    return adapter.getItemViewType(adjPosition);
                }
            }
            return 0;
        }

        @Override
        public long getItemId(int position) {
            if (adapter != null && position >= getExtraViewCount()) {
                int adjPosition = position - getExtraViewCount();
                if (adjPosition < adapter.getItemCount()) {
                    return adapter.getItemId(adjPosition);
                }
            }
            return -1;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) manager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (isRefreshHeader(position) || isHeader(position) || isEmpty(position))
                                ? gridManager.getSpanCount() : 1;
                    }
                });
            }
            adapter.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            adapter.onDetachedFromRecyclerView(recyclerView);
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null
                    && lp instanceof StaggeredGridLayoutManager.LayoutParams
                    && (isRefreshHeader(holder.getLayoutPosition()) ||isHeader(holder.getLayoutPosition()) || isEmpty(holder.getLayoutPosition()))) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
            adapter.onViewAttachedToWindow(holder);
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            adapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            adapter.onViewRecycled(holder);
        }

        @Override
        public boolean onFailedToRecycleView(ViewHolder holder) {
            return adapter.onFailedToRecycleView(holder);
        }

        @Override
        public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            adapter.unregisterAdapterDataObserver(observer);
        }

        @Override
        public void registerAdapterDataObserver(AdapterDataObserver observer) {
            adapter.registerAdapterDataObserver(observer);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null){
                int position = getChildLayoutPosition(v);
                itemClickListener.onRecyclerViewItemClick(v, position, position - getExtraViewCount());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (itemLongClickListener != null){
                int position = getChildLayoutPosition(v);
                itemLongClickListener.onRecyclerViewItemLongClick(v, position, position - getExtraViewCount());
                return true;
            }
            return false;
        }

        private class SimpleViewHolder extends ViewHolder {
            public SimpleViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //解决和CollapsingToolbarLayout冲突的问题
        AppBarLayout appBarLayout = null;
        ViewParent p = getParent();
        while (p != null) {
            if (p instanceof CoordinatorLayout) {
                break;
            }
            p = p.getParent();
        }
        if(p instanceof CoordinatorLayout) {
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout)p;
            final int childCount = coordinatorLayout.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                final View child = coordinatorLayout.getChildAt(i);
                if(child instanceof AppBarLayout) {
                    appBarLayout = (AppBarLayout)child;
                    break;
                }
            }
            if(appBarLayout != null) {
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, State state) {
                        appbarState = state;
                    }
                });
            }
        }
    }
}