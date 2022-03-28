package com.zalexdev.stryker.appintro;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.utils.CheckDir;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomCommand;

import java.util.concurrent.ExecutionException;

/**
 * This class is used to show the slide update screen.
 */
public class SlideUpdate extends Fragment {


    public String chroot;
    public Core core;
    public Context context;
    public Activity activity;
    public ViewPager mPager;
    public int click = 0;
    public SlideUpdate(ViewPager p){
        mPager = p;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slide5update, container, false);
        context = getContext();
        activity = getActivity();
        core = new Core(context);
        ImageView img = view.findViewById(R.id.slide_img);
        TextView title = view.findViewById(R.id.slide_title);
        TextView desc = view.findViewById(R.id.slide_description);
        MaterialButton button = view.findViewById(R.id.slide_button);
        img.setOnClickListener(view12 -> {
            click++;
            if (click>5){
                click = 0;
                core.putBoolean("debug",true);
                img.setImageDrawable(context.getDrawable(R.drawable.debug));
                core.toaster(getString(R.string.debug_mode_on));
                title.setText(R.string.debug_mode_title);
                desc.setText(R.string.debug_desv);
            }
        });
        button.setOnClickListener(view1 -> {
            button.setVisibility(View.GONE);
            try {
                boolean kill = new CustomCommand("/data/data/com.zalexdev.stryker/files/killrootold", new Core(context)).execute().get();
                if (!new CheckDir("/data/local/stryker/beta/sdcard/Stryker").execute().get()){
                    boolean rm = new CustomCommand("rm -rf /data/local/stryker/beta", new Core(context)).execute().get();
                    core.MoveNext(mPager);
                }else{
                    desc.setText(R.string.reboot_update);
                    button.setVisibility(View.VISIBLE);
                    button.setText(R.string.reboot);
                    button.setIconResource(R.drawable.update);
                    button.setOnClickListener(view2 -> {
                        try {
                            boolean reboot = new CustomCommand("reboot", new Core(context)).execute().get();
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                }
                }catch (Exception ignored){

            }
        });
        return view;
    }


}