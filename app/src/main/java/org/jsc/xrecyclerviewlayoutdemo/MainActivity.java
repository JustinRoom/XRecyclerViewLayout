package org.jsc.xrecyclerviewlayoutdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import org.jsc.xrecyclerviewlayoutdemo.activities.XRecyclerActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private LinearLayout lyXRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView(){
        lyXRecyclerView = (LinearLayout) findViewById(R.id.x_recycler_view);

        lyXRecyclerView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == lyXRecyclerView)
            startActivity(new Intent(MainActivity.this, XRecyclerActivity.class));
    }
}
