package com.zalexdev.stryker.geomac;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.zalexdev.stryker.PlsInstallModule;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.Sploit;
import com.zalexdev.stryker.geomac.utils.GetGeoByMac;
import com.zalexdev.stryker.searchsploit.SploitAdapter;
import com.zalexdev.stryker.searchsploit.utils.GetSploit;
import com.zalexdev.stryker.utils.CheckFile;
import com.zalexdev.stryker.utils.CheckInet;
import com.zalexdev.stryker.utils.Core;
import com.zalexdev.stryker.utils.OnSwipeListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class GeoMac extends Fragment {
    public ImageButton search;
    public Core core;
    private IMapController mapController;
    public Context context;
    public Activity activity;
    public MapView map;
    public GeoMac() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Initalizing

        View view = inflater.inflate(R.layout.geomac_fragment, container, false);
        context = getContext();
        activity = getActivity();
        search = view.findViewById(R.id.search);
        core = new Core(context);
        ExpandableLayout menu = activity.findViewById(R.id.menu_expand);
        view.setOnTouchListener(new OnSwipeListener(context) {
            public void onSwipeTop() {core.closemenu(menu); }
            @SuppressLint("ClickableViewAccessibility")
            public void onSwipeRight() { }
            public void onSwipeLeft() { }
            public void onSwipeBottom() { core.openmenu(menu); }
        });
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        map = view.findViewById(R.id.geomap);
        map.setLayerType(View.LAYER_TYPE_HARDWARE, null );
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        fixinet();
        try {
            if (!new CheckFile("/data/local/stryker/release/modules/GeoMac/geomac").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get()){
                getParentFragmentManager().beginTransaction().replace(R.id.flContent, new PlsInstallModule(true,"GeoMac")).commit();
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        mapController = map.getController();
        mapController.setZoom(19f);
        TextInputEditText getquery = view.findViewById(R.id.getsearch);
        search.setOnClickListener(view1 -> {
            Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
            String q = String.valueOf(getquery.getText());
            new Thread(() -> {

                try {
                    // This is getting the coordinates of the AP
                    String coords = new GetGeoByMac(q,core).execute().get();
                    activity.runOnUiThread(() -> {
                        if (coords.isEmpty()) {
                            core.toaster(core.str("no_results"));
                        } else {
                            String lat = coords.replace(" ","").split(",")[0];
                            String lon = coords.replace(" ","").split(",")[1];
                            ArrayList<OverlayItem> items = new ArrayList<>();
                            // This is creating a new OverlayItem object.
                            OverlayItem point = new OverlayItem(q, coords, new GeoPoint(Double.parseDouble(lat),Double.parseDouble(lon)));
                            Drawable wifipoint = context.getDrawable(R.drawable.wifi);
                            wifipoint.setTint(getResources().getColor(R.color.blue));
                            point.setMarker(wifipoint);
                            items.add(point); // Lat/Lon decimal degrees
                            ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
                                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                                        @Override
                                        public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                                            setClipboard(context,coords);
                                            return true;
                                        }
                                        @Override
                                        public boolean onItemLongPress(final int index, final OverlayItem item) {
                                            setClipboard(context,coords);
                                            return false;
                                        }
                                    }, context);
                            mOverlay.setFocusItemsOnTap(true);
                            map.getOverlays().add(mOverlay);
                            mapController.animateTo(new GeoPoint(Double.parseDouble(lat),Double.parseDouble(lon)));
                        }
                    });
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });

        return view;
    }
    /**
     * It copies the text to the clipboard.
     *
     * @param context The context of the activity (this)
     * @param text The text to be copied to the clipboard.
     */
    private void setClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.copied), text);
        clipboard.setPrimaryClip(clip);
        core.toaster("Copied! "+text);
    }
    public void fixinet(){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.exploit_progress);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        LinearProgressIndicator prog = dialog.findViewById(R.id.exploit_prog);
        LottieAnimationView image = dialog.findViewById(R.id.exploit_img);
        TextView title = dialog.findViewById(R.id.exploit_title);
        TextView progress = dialog.findViewById(R.id.exploit_progress_text);
        TextView cancel = dialog.findViewById(R.id.exploit_cancel);
        title.setText(R.string.checking_inet);
        progress.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
        dialog.setCancelable(false);
        image.setAnimation(R.raw.check_net);
        prog.setVisibility(View.GONE);
        cancel.setOnClickListener(view -> dialog.dismiss());
        new Thread(() -> {
            try {
                Boolean inet  = new CheckInet().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                if (inet){
                    activity.runOnUiThread(dialog::dismiss);
                }else{
                    activity.runOnUiThread(dialog::show);
                    boolean o = core.remountcore();
                    inet  = new CheckInet().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                    if (inet){
                        activity.runOnUiThread(dialog::dismiss);
                    }else {
                        activity.runOnUiThread(() -> {
                            cancel.setVisibility(View.VISIBLE);
                            cancel.setText("OK");
                            progress.setVisibility(View.VISIBLE);
                            progress.setText(R.string.no_inet_chroot);
                        });}


                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }
}