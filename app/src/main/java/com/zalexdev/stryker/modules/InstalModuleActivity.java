package com.zalexdev.stryker.modules;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.modules.utils.RunModule;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.CustomCommand;
import java.util.concurrent.ExecutionException;

/**
 * Installs a module from a given path
 */
public class InstalModuleActivity extends AppCompatActivity {

    public String path;
    public TextView log;
    public Activity activity;
    public Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        path = getIntent().getExtras().getString("path");
        String name = getIntent().getExtras().getString("name");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instal_module);
        ExtendedFloatingActionButton relaunch = findViewById(R.id.relauch_button);
        log = findViewById(R.id.logview);
        activity = this;
        context = this;
        relaunch.shrink();
        relaunch.hide();
        new Thread(() -> {
            try {
                boolean module = new RunModule(path,new Core(context),log,activity,getIntent().getExtras().getBoolean("install"),name).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                runOnUiThread(relaunch::show);
                runOnUiThread(relaunch::extend);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        relaunch.setOnClickListener(view -> {
            new CustomCommand("am start -n com.zalexdev.stryker/.MainActivity",new Core(context)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            finishAffinity();
        });



    }
    @Override
    public void onBackPressed() {
        new Core(context).toaster("Relaunch app!");
    }
}