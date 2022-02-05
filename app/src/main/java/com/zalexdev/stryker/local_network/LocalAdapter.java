package com.zalexdev.stryker.local_network;


import static com.airbnb.lottie.L.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.zalexdev.stryker.R;
import com.zalexdev.stryker.custom.Device;
import com.zalexdev.stryker.custom.Exploit;
import com.zalexdev.stryker.custom.Router;
import com.zalexdev.stryker.exploit_hub.utils.BasicExploitLaunch;
import com.zalexdev.stryker.local_network.exploits.CheckBlueKeep;
import com.zalexdev.stryker.local_network.exploits.CheckEternalBlue;
import com.zalexdev.stryker.local_network.exploits.CheckSmbGhost;
import com.zalexdev.stryker.local_network.exploits.RouterScan;
import com.zalexdev.stryker.local_network.utils.CutNetwork;
import com.zalexdev.stryker.utils.Core;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class LocalAdapter extends RecyclerView.Adapter<LocalAdapter.ViewHolder> {
    public Context context;
    public Activity activity;
    public String port;
    public String portcustom = "";
    public ArrayList<Device> devices;
    public Core core;
    public CutNetwork cut = null;
    public  BottomSheetDialog localdialog = null;
    public String dialogip = "";


    public LocalAdapter(Context context2, Activity mActivity, ArrayList<Device> devs) {
        context = context2;
        activity = mActivity;
        devices = devs;
        core = new Core(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.local_item, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
        //Setting up got info
        String ip = devices.get(position).getIp();
        String mac = devices.get(position).getMac();
        adapter.local_ip.setText(ip);
        if (!new Core(context).getBoolean("hide")) {
            adapter.local_mac.setText(mac);
        } else {
            adapter.local_mac.setText("XX:XX:XX:XX:XX");
        }
        if (devices.get(position).isShim()) {
            adapter.shim.startShimmerAnimation();
        }else if (!devices.get(position).isShim() && localdialog!=null&& localdialog.isShowing()&&devices.get(position).getIp().equals(dialogip)){
            localdialog.dismiss();
            LocalDialog(devices.get(position),position);
        }
        if (devices.get(position).getPorts().contains("80")) {
            adapter.img_1.setVisibility(View.VISIBLE);
            adapter.img_1.setImageDrawable(context.getDrawable(R.drawable.web));
            adapter.img_1.setOnClickListener(view -> openlink("http://" + devices.get(position).getIp()));
        } else if (devices.get(position).getPorts().contains("8080")) {
            adapter.img_1.setVisibility(View.VISIBLE);
            adapter.img_1.setImageDrawable(context.getDrawable(R.drawable.web));
            adapter.img_1.setOnClickListener(view -> openlink("http://" + devices.get(position).getIp() + ":8080"));
        } else if (devices.get(position).getPorts().contains("8000")) {
            adapter.img_1.setVisibility(View.VISIBLE);
            adapter.img_1.setImageDrawable(context.getDrawable(R.drawable.web));
            adapter.img_1.setOnClickListener(view -> openlink("http://" + devices.get(position).getIp() + ":8000"));
        } else if (devices.get(position).getPorts().contains("443")) {
            adapter.img_1.setVisibility(View.VISIBLE);
            adapter.img_1.setImageDrawable(context.getDrawable(R.drawable.web));
            adapter.img_1.setOnClickListener(view -> openlink("https://" + devices.get(position).getIp()));
        }
        if (devices.get(position).getPorts().contains("445") || devices.get(position).getPorts().contains("21")) {
            adapter.img_2.setVisibility(View.VISIBLE);
            adapter.img_2.setImageDrawable(context.getDrawable(R.drawable.folder));
        }
        if (devices.get(position).getPorts().contains("22")) {
            adapter.img_3.setVisibility(View.VISIBLE);
            adapter.img_3.setImageDrawable(context.getDrawable(R.drawable.terminal));
        }
        if (devices.get(position).isIscutted()) {
            adapter.local_img.setColorFilter(ContextCompat.getColor(context, R.color.red));
        } else {
            adapter.local_img.setColorFilter(Color.parseColor("#606060"));
        }
        if (devices.get(position).getSubname() != null) {
            if (!devices.get(position).getSubname().equals(devices.get(position).getIp())) {
                adapter.local_ip.setText(devices.get(position).getIp() + " (" + devices.get(position).getSubname() + ")");
            }
        }
        adapter.local_manufacture.setText(devices.get(position).getVendor());
        adapter.local_ports.setText("Ports: " + devices.get(position).getPorts().size());
        if (devices.get(position).getOs().equals("Unknown")){devices.get(position).guessos();}
        adapter.local_img.setImageDrawable(context.getDrawable(devices.get(position).getImage()));
        if (position == 0) {
            adapter.local_img.setImageDrawable(context.getDrawable(R.drawable.router));
        }

        adapter.card.setOnLongClickListener(view -> {
            netcutdialog(devices.get(position), position);
            return false;
        });
        adapter.card.setOnClickListener(view -> LocalDialog(devices.get(position),position));
        adapter.ports.setOnClickListener(view -> showPorts(devices.get(position).getPorts(), devices.get(position).getServices()));

    }

    public void netcutdialog(Device d, int pos) {
        if (!d.isIscutted()) {
            String[] types = new String[3];
            types[0] = core.str("cut_dev");
            types[1] = core.str("perm_cut");
            types[2] = core.str("cut20");
            new MaterialAlertDialogBuilder(context)
                    .setTitle(core.str("choose_opt"))
                    .setItems(types, (dialogInterface, i) -> {
                        cut = new CutNetwork(core, d.getIp(), devices.get(0).getIp(), i);
                        cut.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        d.setIscutted(true);
                        notifyItemChanged(pos);
                        if (i == 2) {
                            Timer timer = new Timer();
                            timer.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    d.setIscutted(false);
                                    activity.runOnUiThread(() -> notifyItemChanged(pos));
                                    timer.cancel();
                                }
                            }, 20000, 20000);

                        }
                    })
                    .show();
        } else {
            cut.kill();
            d.setIscutted(false);
            notifyItemChanged(pos);
        }
    }

    private void LocalDialog(Device device,int pos) {
        String ip = device.getIp();
        String os = device.getOs();
        String mac = device.getMac();
        ArrayList<String> port = device.getPorts();
        ArrayList<String> services = device.getServices();
        Drawable img = context.getDrawable(device.getImage());
        boolean isrouter = pos == 0;
        localdialog = new BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme);
        localdialog.setContentView(R.layout.local_bottom);
        dialogip = ip;
        ImageView device_img = localdialog.findViewById(R.id.device_img);
        TextView ip_view = localdialog.findViewById(R.id.device_ip);
        TextView mac_view = localdialog.findViewById(R.id.device_mac);

        ShimmerFrameLayout shim = localdialog.findViewById(R.id.shimmer);
        LinearLayout smb = localdialog.findViewById(R.id.check_smb);
        LinearLayout rdp = localdialog.findViewById(R.id.check_rdp);
        LinearLayout admin = localdialog.findViewById(R.id.check_admin_panel);
        LinearLayout run_exploit = localdialog.findViewById(R.id.run_exploit);
        if (!device.isShim()){
            shim.stopShimmerAnimation();
        }else{
            shim.startShimmerAnimation();
        }
        if (core.is64Bit() && core.checkmod("Router Scan")){
        admin.setOnClickListener(view -> getPort("Admin", port, ip));}
        else{
            admin.setVisibility(View.GONE);
        }
        TextView port_count = localdialog.findViewById(R.id.port_count);
        ip_view.setText(ip);

        if (!new Core(context).getBoolean("hide")) {
            mac_view.setText(mac + " "+"OS: " + os);
        } else {
            mac_view.setText("XX:XX:XX:XX:XX"+ " "+"OS: " + os);
        }
        port_count.setText(String.valueOf(port.size()));
        device_img.setImageDrawable(img);
        if (isrouter) {
            device_img.setImageDrawable(context.getDrawable(R.drawable.router));
        }
        device_img.setOnClickListener(view -> {
            showPorts(port, services);
        });
        smb.setOnClickListener(view -> {
            ArrayList<Exploit> smb1 = new ArrayList<>();
            smb1.add(core.getExploitbyTitle("Eternalblue"));
            smb1.add(core.getExploitbyTitle("SMBGhost"));
            smb1.get(0).setIp(ip);
            smb1.get(1).setIp(ip);
            runexploits(smb1);
        });
        rdp.setOnClickListener(view -> {
            ArrayList<Exploit> rdp1 = new ArrayList<>();
            rdp1.add(core.getExploitbyTitle("Bluekeep"));
            rdp1.get(0).setIp(ip);
            runexploits(rdp1);
        });
        run_exploit.setOnClickListener(view -> {
            String[] exploit_list = new String[core.getExploits().size()];
            for (int i = 0; i < core.getExploits().size(); i++) {exploit_list[i] = core.getExploits().get(i).getTitle(); }
            new MaterialAlertDialogBuilder(context)
                    .setTitle(core.str("sel_exploit"))
                    .setItems(exploit_list, (dialogInterface, i) -> {
                        new Thread(() -> {
                            Exploit exploit = core.getExploits().get(i);
                            ArrayList<String> ar = exploit.getRequireArgs();
                            if (ar.contains("IP")){exploit.setIp(ip);ar.remove("IP");}
                            if (ar.contains("MAC")){exploit.setMac(mac);ar.remove("MAC");}
                            if (ar.contains("GW")){exploit.setGw(devices.get(0).getIp());ar.remove("GW");}
                            if (ar.contains("PORT")){
                                activity.runOnUiThread(() -> selectPort(port));
                                while (portcustom.equals("")){Log.d(TAG, "run: test");}
                                exploit.setPort(portcustom);
                                ar.remove("PORT");portcustom="";
                            }
                            String cmd = exploit.genereteLaunchCommand();
                            for (String s : ar){
                                final String[] temp = {""};
                                activity.runOnUiThread(() -> {
                                    final Dialog valuedialog = new Dialog(context);
                                    valuedialog.setContentView(R.layout.input_dialog);
                                    valuedialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    TextView title = valuedialog.findViewById(R.id.input_title);
                                    TextInputEditText valueedit = valuedialog.findViewById(R.id.getvalue);
                                    TextView ok = valuedialog.findViewById(R.id.ok_button);
                                    title.setText("Enter "+s+" value");
                                    ok.setOnClickListener(view1 -> {
                                        temp[0] = valueedit.getText().toString();
                                        valuedialog.dismiss();
                                    });
                                    valuedialog.show();
                                });
                                while (temp[0].equals("")){
                                    Log.d(TAG,"test");
                                }
                                cmd = cmd.replace("{"+s+"}",temp[0]);

                            }
                            String finalCmd1 = cmd;
                            activity.runOnUiThread(() -> {
                                final Dialog resdialog = new Dialog(context);
                                resdialog.setContentView(R.layout.exploit_progress);
                                resdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                LinearProgressIndicator prog = resdialog.findViewById(R.id.exploit_prog);
                                LottieAnimationView image = resdialog.findViewById(R.id.exploit_img);
                                image.setAnimation(R.raw.scan);
                                TextView title = resdialog.findViewById(R.id.exploit_title);
                                TextView progress = resdialog.findViewById(R.id.exploit_progress_text);
                                TextView cancel = resdialog.findViewById(R.id.exploit_cancel);
                                cancel.setOnClickListener(view2 -> {
                                    resdialog.dismiss();
                                });
                                progress.setText(core.str("wait_res"));
                                resdialog.setCanceledOnTouchOutside(false);
                                StringBuilder t = new StringBuilder();
                                t.append(core.str("run")).append(" ").append(exploit.getTitle());
                                title.setText(t);
                                int padd = 100;
                                final int[] progper = {0};
                                final int[] wait = {0};
                                final int[] success = {0};
                                StringBuilder res = new StringBuilder();
                                res.append(" ");
                                new Thread(() -> {
                                    boolean result = false;
                                    try {result = new BasicExploitLaunch(exploit.getSuccesspatern(), finalCmd1,core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get(); } catch (ExecutionException | InterruptedException executionException) {executionException.printStackTrace();}
                                    if (result){
                                        res.append(exploit.getTitle()).append(";");
                                        success[0]++;}
                                    wait[0]++;
                                    progper[0] += padd;
                                    setProg(prog,progper[0]);
                                }).start();
                                new Thread(() -> {
                                    while (wait[0] != 1){Log.d(TAG,"waiting...");}
                                    if (success[0] == 0){setText(progress,core.str("sorry_not_vuln"));setProgColor(prog,image,1);}
                                    else {setText(progress,core.str("vuln_for")+res.toString());setProgColor(prog,image,2); }
                                    setText(cancel,"OK");
                                }).start();
                                resdialog.show();
                            });


                        }).start();
                    })
                    .show();
        });
        localdialog.show();
    }

    private void showPorts(ArrayList<String> ports, ArrayList<String> services) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.list_of_ports);
        dialog.setTitle(core.str("list_of_ports"));
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView text = dialog.findViewById(R.id.list_ports);
        for (int i = 0; i < ports.size(); i++) {
            text.append(ports.get(i) + " (" + services.get(i) + ")\n");
        }
        if (ports.size() == 0) {
            text.setText(R.string.no_avaible);
        }
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void runexploits(ArrayList<Exploit> exploits)  {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.exploit_progress);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        LinearProgressIndicator prog = dialog.findViewById(R.id.exploit_prog);
        LottieAnimationView image = dialog.findViewById(R.id.exploit_img);
        TextView title = dialog.findViewById(R.id.exploit_title);
        TextView progress = dialog.findViewById(R.id.exploit_progress_text);
        TextView cancel = dialog.findViewById(R.id.exploit_cancel);
        cancel.setOnClickListener(view -> dialog.dismiss());
        if (exploits.get(0).getTitle().contains("Bluekeep")){image.setAnimation(R.raw.rdp);}
        progress.setText(R.string.waiting_res);
        dialog.setCanceledOnTouchOutside(false);
        StringBuilder t = new StringBuilder();
        t.append(core.str("run")).append(" ").append(exploits.size()).append(" ").append(core.str("exs"));
        title.setText(t);
        int padd = 100/exploits.size();
        final int[] progper = {0};
        final int[] wait = {0};
        final int[] success = {0};
        StringBuilder res = new StringBuilder();
        res.append(" ");
        for (Exploit e : exploits){
            new Thread(() -> {
                boolean result = false;
                try {result = new BasicExploitLaunch(e.getSuccesspatern(),e.genereteLaunchCommand(),core).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get(); } catch (ExecutionException | InterruptedException executionException) {executionException.printStackTrace();}
                if (result){res.append(e.getTitle()+";");
                    success[0]++;}
                wait[0]++;
                progper[0] += padd;
                setProg(prog,progper[0]);
            }).start();}
        new Thread(() -> {
            while (wait[0] != exploits.size()){Log.d(TAG,"waiting...");}
            if (success[0] == 0){setText(progress,core.str("not_vuln_local"));setProgColor(prog,image,1);}
            else {setText(progress,core.str("vuln_for")+res.toString());setProgColor(prog,image,2); }
            setText(cancel,"OK");
        }).start();
        dialog.show();
    }
    private void testexploit(String type, String port, String ip) throws ExecutionException, InterruptedException {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.exploit_progress);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        LinearProgressIndicator prog = dialog.findViewById(R.id.exploit_prog);
        LottieAnimationView image = dialog.findViewById(R.id.exploit_img);
        TextView title = dialog.findViewById(R.id.exploit_title);
        TextView progress = dialog.findViewById(R.id.exploit_progress_text);
        TextView cancel = dialog.findViewById(R.id.exploit_cancel);
        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
        dialog.setCanceledOnTouchOutside(false);
        if (!core.is64Bit()){
            bitdialog();
        }else{
        if (type.equals("Admin")) {
            image.setAnimation(R.raw.router);
            title.setText(R.string.rs);
            progress.setText(R.string.start_core);
            prog.setVisibility(View.INVISIBLE);
            prog.setIndeterminate(false);
            prog.setVisibility(View.VISIBLE);
            setProg(prog, 20);
            dialog.show();
            Thread t3 = new Thread(() -> {

                try {
                    Router router;
                    router = new RouterScan(activity, context, progress, prog, ip, port).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                    setProg(prog, 100);
                    setText(cancel, "OK");
                    if (!router.getSuccess()) {
                        setText(progress, core.str("failed_info"));
                        setProgColor(prog, image, 1);
                    } else {
                        setText(progress,
                                core.str("web_auth") + router.getAuth() + core.str("ssid") + router.getSsid() + core.str("psk") + router.getPsk() + core.str("wps") + router.getWps());
                        setProgColor(prog, image, 2);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            t3.start();
        }
        }


    }
    public void bitdialog() {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Your device is 32bit!")
                .setMessage(core.str("bit"))
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .show();
    }
    public void getPort(String type, ArrayList<String> ports, String ip) {
        port = "";
        if (!ports.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.coose_port);
            String[] port_list = new String[ports.size()];
            for (int i = 0; i < ports.size(); i++) {
                port_list[i] = ports.get(i);
            }
            int checkedItem = 0;
            builder.setSingleChoiceItems(port_list, checkedItem, (dialog, which) -> {
                port = port_list[which];
                dialog.dismiss();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.setOnDismissListener(dialogInterface -> {
                try {
                    if (!port.equals("")) {
                        testexploit(type, port, ip);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    public void selectPort(ArrayList<String> ports) {
        port = "";
        if (!ports.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.coose_port);
            String[] port_list = new String[ports.size()];
            for (int i = 0; i < ports.size(); i++) {
                port_list[i] = ports.get(i);
            }
            int checkedItem = 0;
            builder.setSingleChoiceItems(port_list, checkedItem, (dialog, which) -> {
                portcustom = port_list[which];
                dialog.dismiss();
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }
    @Override
    public int getItemCount() {

        return devices.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setText(TextView textView, String text) {
        activity.runOnUiThread(() -> textView.setText(text));
    }

    public void setProg(LinearProgressIndicator progressIndicator, int prog) {
        activity.runOnUiThread(() -> {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(false);
            progressIndicator.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressIndicator.setProgress(prog, true);
            }
        });

    }

    public void setProgColor(LinearProgressIndicator progressIndicator, LottieAnimationView img, int color) {
        activity.runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                progressIndicator.setVisibility(View.INVISIBLE);
                progressIndicator.setIndeterminate(false);
                progressIndicator.setVisibility(View.VISIBLE);
                img.setVisibility(View.VISIBLE);
                img.cancelAnimation();
                if (color == 1) {
                    progressIndicator.setIndicatorColor(context.getColor(R.color.red));

                } else if (color == 2) {
                    progressIndicator.setIndicatorColor(context.getColor(R.color.green));

                } else if (color == 3) {
                    progressIndicator.setIndicatorColor(context.getColor(R.color.yellow));
                }
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void toaster(String msg) {
        activity.runOnUiThread(() -> {
            Toast toast = Toast.makeText(context,
                    msg, Toast.LENGTH_SHORT);
            toast.show();
        });

    }

    public void changeitem(int i, Device d) {
        activity.runOnUiThread(() -> {
            devices.set(i, d);
            notifyItemChanged(i);
        });

    }

    public void openlink(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(browserIntent);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //Init
        public TextView local_ip;
        public TextView local_mac;
        public TextView local_manufacture;
        public ImageView local_img;
        public ImageView img_1;
        public ImageView img_2;
        public ImageView img_3;
        public ImageView img_4;
        public ImageView img_5;
        public TextView local_ports;
        public LinearLayout ports;
        public ShimmerFrameLayout shim;
        public View card;

        public ViewHolder(View v) {
            super(v);
            local_ip = v.findViewById(R.id.local_ip);
            local_mac = v.findViewById(R.id.local_mac);
            local_img = v.findViewById(R.id.local_icon);
            local_ports = v.findViewById(R.id.local_ports);
            ports = v.findViewById(R.id.port_layout);
            local_manufacture = v.findViewById(R.id.local_manufacture);
            card = v.findViewById(R.id.local_item);
            shim = v.findViewById(R.id.shimmerFrameLayout);
            img_1 = v.findViewById(R.id.img_1);
            img_2 = v.findViewById(R.id.img_2);
            img_3 = v.findViewById(R.id.img_3);
            img_4 = v.findViewById(R.id.img_4);
            img_5 = v.findViewById(R.id.img_5);

        }

    }
}
