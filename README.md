# XRecyclerViewLayout

## Screenshots<br>
###1、Refreshing<br>
![](https://github.com/JustinRoom/XRecyclerViewLayout/blob/master/screenshots/refreshing.gif)<br>

###2、Pull down to refresh<br>
![](https://github.com/JustinRoom/XRecyclerViewLayout/blob/master/screenshots/pull_refreshing.gif)

###3、Loading more<br>
![](https://github.com/JustinRoom/XRecyclerViewLayout/blob/master/screenshots/loading_more.gif)

###4、No more<br>
![](https://github.com/JustinRoom/XRecyclerViewLayout/blob/master/screenshots/no_more.gif)

## Useage<br>
###1、activity_xrecycler.xml<br>
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical" >

    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:background="#2cb1e1">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:text="XRcyclerView"
            android:textColor="@android:color/white"
            android:textSize="18sp"/>

        <TextView
            android:id="@+id/btn_clear_data"
            android:layout_width="wrap_content"
            android:layout_height="48dp"
            android:layout_alignParentRight="true"
            android:layout_centerVertical="true"
            android:layout_marginRight="10dp"
            android:gravity="center_vertical"
            android:text="Clear Data"
            android:textColor="@android:color/white"
            android:textSize="14sp"/>

    </RelativeLayout>

    <org.jsc.xrecyclerviewlayout.XRecyclerViewLayout
        android:id="@+id/recycler_view_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

</LinearLayout>

###2、XRecyclerActivity<br>
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

###3、Important methods of XRecyclerView<br>
//Set listener (pull down to refresh and pull up to load more).
####public void setLoadingListener(LoadingListener listener)
//item click listener
####public void setItemClickListener(ItemClickListener itemClickListener)
//item long click listener
####public void setItemLongClickListener(ItemLongClickListener itemLongClickListener)
//Is loading data.
####public boolean isLoadingData()
####public void setItemClickable(boolean itemClickable)
####public void setItemLongClickable(boolean itemLongClickable)
####public void setRefreshing(boolean refreshing)
####public void refreshComplete()
####public void loadMoreComplete()
####public void isHaveMore(int count, int total)
####public void isHaveMore(boolean haveMore)
####public void setPullRefreshEnabled(boolean enabled)
####public void setLoadingMoreEnabled(boolean enabled)
