package com.cch.scrollviewhelper;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by ³¿êÍ on 2015-09-02.
 */
public class MainActivity extends FragmentActivity {
    MyScrollView svmain;
    TextView tvtitlecontent;
    LinearLayout lltitle;
    Button btn_1;
    View imgv_anchor;
    ViewPager vp_test;

    FragmentManager fragmentManager;
    FragmentText fragmentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initView();
        fragmentManager = getSupportFragmentManager();
        fragmentText = new FragmentText();

    }

    private void initView() {
        btn_1 = (Button) findViewById(R.id.btn_1);
        svmain = (MyScrollView) findViewById(R.id.sv_main);
        tvtitlecontent = (TextView) findViewById(R.id.tv_title_content);
        imgv_anchor = (View) findViewById(R.id.anchor);
        lltitle = (LinearLayout) findViewById(R.id.ll_title);
        vp_test = (ViewPager) findViewById(R.id.vp_test);

        vp_test.setAdapter(new ViewpagerAdapter(this));
        svmain.setAnchorView(imgv_anchor);
        svmain.setStrinkableHeaderView(imgv_anchor);
        svmain.addControlView(lltitle, new UpdateControler());
        svmain.addControlView(btn_1, new MyScrollView.ViewAnimationControl() {
            @Override
            public void ofFloat(View view, float param) {
                if(param == 1){
                    btn_1.setVisibility(View.VISIBLE);
                }else{
                    btn_1.setVisibility(View.GONE);
                }
            }
        });
        svmain.setOnExpandListener(new OnExpandListener());
    }

    class UpdateControler implements MyScrollView.ViewAnimationControl {

        @Override
        public void ofFloat(View view, float param) {
            view.setAlpha(param);
        }
    }

    class OnExpandListener implements MyScrollView.OnScrollViewExpendListener {

        @Override
        public void onStateChange(MyScrollView.ScrollState scrollViewState) {
            if(scrollViewState == MyScrollView.ScrollState.STATE_FULL_SCREEN){
                vp_test.setVisibility(View.GONE);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if(!fragmentText.isAdded()){
                    fragmentTransaction.add(R.id.anchor, fragmentText);
                    fragmentTransaction.show(fragmentText);
                }else{
                    fragmentTransaction.show(fragmentText);
                }
                fragmentTransaction.commit();
            }else if(scrollViewState == MyScrollView.ScrollState.STATE_EXPANDING){
                vp_test.setVisibility(View.VISIBLE);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.hide( fragmentText);
                fragmentTransaction.commit();
            }
        }

        @Override
        public void onExpanding(View headerView, float rate) {

        }
    }
}
