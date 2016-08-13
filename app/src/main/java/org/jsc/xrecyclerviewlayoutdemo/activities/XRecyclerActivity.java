package org.jsc.xrecyclerviewlayoutdemo.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.jsc.xrecyclerviewlayout.EmptyView;
import org.jsc.xrecyclerviewlayout.XRecyclerViewLayout;
import org.jsc.xrecyclerviewlayout.XRecyclerView;
import org.jsc.xrecyclerviewlayoutdemo.R;
import org.jsc.xrecyclerviewlayoutdemo.adapter.MyAdapter;

import java.util.ArrayList;
import java.util.List;

public class XRecyclerActivity extends AppCompatActivity {

    private XRecyclerView mXRecyclerView;
    private TextView tvClearData;

    private MyAdapter mAdapter;

    private List<String> listData;
    private int refreshTime;
    private int times;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xrecycler);

        tvClearData = (TextView) findViewById(R.id.btn_clear_data);
        tvClearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listData.clear();
                mAdapter.notifyDataSetChanged();
                mXRecyclerView.isHaveMore(false);
                mXRecyclerView.showEmptyView(EmptyView.EmptyStatus.EMPTY_ERROR, "Clear data！");
            }
        });

        XRecyclerViewLayout recyclerViewLayout = (XRecyclerViewLayout) findViewById(R.id.recycler_view_layout);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mXRecyclerView = recyclerViewLayout.getRecyclerView();
        mXRecyclerView.setLayoutManager(layoutManager);
        mXRecyclerView.setHeadView(getLayoutInflater().inflate(R.layout.head_view_layout, null));
        mXRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                refreshTime ++;
                times = 0;
                new Handler().postDelayed(new Runnable(){
                    public void run() {

                        listData.clear();
                        for(int i = 0; i < 15 ;i++){
                            listData.add("item" + i + "after " + refreshTime + " times of refresh");
                        }
                        mXRecyclerView.refreshComplete();
                        mAdapter.notifyDataSetChanged();
                        mXRecyclerView.isHaveMore(15, 15);
                    }

                }, 2000);            //refresh data here
            }

            @Override
            public void onLoadMore() {
                if(times < 2){
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            for(int i = 0; i < 15 ;i++){
                                listData.add("item" + (1 + listData.size() ) );
                            }
                            mXRecyclerView.loadMoreComplete();
                            mAdapter.notifyDataSetChanged();
                            mXRecyclerView.isHaveMore(9, 9);
                        }
                    }, 1000);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            for(int i = 0; i < 8 ;i++){
                                listData.add("item" + (1 + listData.size() ) );
                            }
                            mXRecyclerView.loadMoreComplete();
                            mAdapter.notifyDataSetChanged();
                            mXRecyclerView.isHaveMore(8, 9);
                        }
                    }, 1000);
                }
                times ++;
            }
        });
        mXRecyclerView.setItemClickListener(new XRecyclerView.ItemClickListener() {
            @Override
            public void onRecyclerViewItemClick(View view, int LayoutPosition, int DataPosition) {
                String data = mAdapter.getItem(DataPosition);
                showShortToast("item click: " + DataPosition + " --> " + data);
            }
        });
        mXRecyclerView.setItemLongClickListener(new XRecyclerView.ItemLongClickListener() {
            @Override
            public void onRecyclerViewItemLongClick(View view, int LayoutPosition, int DataPosition) {
                String data = mAdapter.getItem(DataPosition);
                showShortToast("item long click: " + DataPosition + " --> " + data);
            }
        });
        listData = new ArrayList<>();
        mAdapter = new MyAdapter(listData);

        mXRecyclerView.setAdapter(mAdapter);
        mXRecyclerView.setRefreshing(true);
    }


    private void showShortToast(String txt){
        Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();
    }
}
