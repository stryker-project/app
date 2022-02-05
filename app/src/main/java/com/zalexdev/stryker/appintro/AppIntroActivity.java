package com.zalexdev.stryker.appintro;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.zalexdev.stryker.R;

public class AppIntroActivity extends FragmentActivity {

    private static final int NUM_PAGES = 10;
    public LinearProgressIndicator prog;
    private ViewPager mPager;
    public boolean update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_intro);
        getWindow().setStatusBarColor(Color.parseColor("#3F51B5"));
        getWindow().setNavigationBarColor(Color.parseColor("#3F51B5"));
        update = getIntent().getExtras().getBoolean("update");
        mPager = findViewById(R.id.intro_pager);
        prog = findViewById(R.id.slider_prog);
        PagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        mPager.setOnTouchListener((v, event) -> true);
    }

    @Override
    public void onBackPressed() {

    }


    private  class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0){
                setProg(prog,10);
                if (!update){
                return new Slide1(getmPager());
                }else{
                return new SlideUpdate(getmPager());
                }
            }else if (position == 1){
                setProg(prog,20);
                return new Slide2(getmPager());
            }
            else if (position == 2){
                setProg(prog,30);
                return new Slide3(getmPager());
            }else if (position == 3){
                setProg(prog,40);
                return new Slide4(getmPager());
            }
            else if (position == 4){
                setProg(prog,50);
                return new SlideFinnal(getmPager());
            }
            else{
                return new Slide1(getmPager());
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    public ViewPager getmPager() {
        return mPager;
    }
    public void setProg(LinearProgressIndicator progressIndicator, int prog) {
        runOnUiThread(() -> {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(false);
            progressIndicator.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressIndicator.setProgress(prog, true);
            }
        });
    }
}