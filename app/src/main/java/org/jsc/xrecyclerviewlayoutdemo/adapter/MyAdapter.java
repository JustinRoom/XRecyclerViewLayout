package org.jsc.xrecyclerviewlayoutdemo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jsc.xrecyclerviewlayoutdemo.R;

import java.util.List;

/**
 * Created by jianghejie on 15/11/26.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.DataViewHolder> {

    public List<String> data = null;
    public MyAdapter(List<String> data) {
        this.data = data;
    }

    public String getItem(int position){
        return data.get(position);
    }

    //创建新View，被LayoutManager所调用
    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_layout,viewGroup,false);
        return new DataViewHolder(view);
    }

    //将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(DataViewHolder viewHolder, int position) {
        viewHolder.mTextView.setText(data.get(position));
    }

    //获取数据的数量
    @Override
    public int getItemCount() {
        return data.size();
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class DataViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public DataViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.text);
        }
    }
}
