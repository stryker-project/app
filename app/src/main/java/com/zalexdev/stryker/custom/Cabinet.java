package com.zalexdev.stryker.custom;

import android.content.Context;

import com.zalexdev.stryker.utils.Core;

/**
 * The Cabinet class is used to store the API keys
 */
public class Cabinet {
    public Context context;
    public String keyView;
    public String keyWrite;
    public Core core;
    public boolean ok = false;

    public Cabinet(Context cont) {
        context = cont;
        core = new Core(context);
    }

    public String getKeyView() {
        return keyView;
    }

    public void setKeyView(String keyView) {
        core.putString("api_view", keyView);
        this.keyView = keyView;
    }

    public String getKeyWrite() {
        return keyWrite;
    }

    public void setKeyWrite(String keyWrite) {
        core.putString("api_write", keyWrite);
        this.keyWrite = keyWrite;
    }

    public void getStored() {
        keyView = core.getString("api_view");
        keyWrite = core.getString("api_write");
    }

    public boolean getOk() {
        return this.ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }
}
