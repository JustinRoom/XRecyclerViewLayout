package org.jsc.xrecyclerviewlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EmptyView extends LinearLayout{

    private ImageView ivIcon;
    private TextView tvMessage;

    public EmptyView(Context context) {
        this(context, null);
    }

    public EmptyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void initView(Context context) {
        setOrientation(VERTICAL);
        View rootView = LayoutInflater.from(context).inflate(R.layout.empty_view_layout, null);
        ivIcon = (ImageView) rootView.findViewById(R.id.iv_icon);
        tvMessage = (TextView) rootView.findViewById(R.id.tv_message);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(lp);

        addView(rootView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public void setState(EmptyStatus status, String txt){
        tvMessage.setText(txt);
        switch (status){
            case EMPTY_LOADING:
                ivIcon.setVisibility(INVISIBLE);
                break;
            case EMPTY_LOADED:
            case EMPTY_ERROR:
                ivIcon.setVisibility(VISIBLE);
                break;
        }
    }

    public enum EmptyStatus{
        EMPTY_LOADING, EMPTY_LOADED, EMPTY_ERROR
    }
}
