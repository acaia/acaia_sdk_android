package co.acaia.android.acaiasdksampleapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import co.acaia.ble.events.ScaleConnectStateEvent;
import co.acaia.brewguide.events.PearlSStatusEvent;
import co.acaia.communications.events.WeightEvent;
import co.acaia.communications.scaleService.ScaleCommunicationService;
import co.acaia.communications.scaleevent.ScaleSettingUpdateEvent;
import co.acaia.communications.scaleevent.ScaleSettingUpdateEventType;

public class MainActivity extends AppCompatActivity {
    public enum UPLOAD_MODE {
        MODE_BREWGUIDE,
        MODE_MESSAGE
    }
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private UPLOAD_MODE upload_mode;
    private ScaleCommunicationService mCommunicationService;
    private BluetoothAdapter blueadapter;
    private boolean isConnected = false;
    private Button btn_connect;
    private TextView tv_weigh, tv_device_name, tv_device_info, tv_battery, tv_capacity, tv_key_disable;
    private SwitchCompat switch_beep_sound;
    private RadioGroup r_group_unit, r_group_auto_off_time;
    private RadioButton
            rbtn_g, rbtn_oz,
            rbtn_0_min, rbtn_5_min, rbtn_10_min, rbtn_20_min, rbtn_30_min, rbtn_60_min;
    private LoadingDialog loadingDialog;
    private final int sec = 10;
    private CountDownTimer stopScanTimer = new CountDownTimer(1000*sec, 1000) {
        @Override
        public void onTick(long l) {

        }

        @Override
        public void onFinish() {
            if(loadingDialog!=null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            btn_connect.setClickable(true);
            btn_connect.setText("Connect");
        }
    };
    private CompoundButton.OnCheckedChangeListener onUnitCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(b){
                switch (compoundButton.getId()){
                    case R.id.rbtn_g:
                        mCommunicationService.setUnitGram();
                        break;
                    case R.id.rbtn_oz:
                        mCommunicationService.setUnitOunce();
                        break;
                }
                mCommunicationService.requestPearlsStatus();
            }
        }
    };
    private CompoundButton.OnCheckedChangeListener onAutoOffTimeCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(b){
                switch (compoundButton.getId()){
                    case R.id.rbtn_0_min:
                        mCommunicationService.setAutoOffTimeNone();
                        break;
                    case R.id.rbtn_5_min:
                        mCommunicationService.setAutoOffTime5Min();
                        break;
                    case R.id.rbtn_10_min:
                        mCommunicationService.setAutoOffTime10Min();
                        break;
                    case R.id.rbtn_20_min:
                        mCommunicationService.setAutoOffTime20Min();
                        break;
                    case R.id.rbtn_30_min:
                        mCommunicationService.setAutoOffTime30Min();
                        break;
                    case R.id.rbtn_60_min:
                        mCommunicationService.setAutoOffTime60Min();
                        break;
                }
                mCommunicationService.requestPearlsStatus();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        mCommunicationService = ((AcaiaSDKSampleApp)getApplication()).getScaleCommunicationService();
        isPermissionGranted();
        blueadapter = BluetoothAdapter.getDefaultAdapter();
        iniView();
    }

    @Override
    protected void onDestroy() {
        stopScanTimer.cancel();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void iniView(){
        loadingDialog = new LoadingDialog(this);
        btn_connect = findViewById(R.id.button);
        tv_weigh = findViewById(R.id.tv_weigh);
        tv_device_name = findViewById(R.id.tv_device_name);
        tv_device_info = findViewById(R.id.tv_device_info);
        tv_battery = findViewById(R.id.tv_battery);
        tv_capacity = findViewById(R.id.tv_capacity);
        tv_key_disable = findViewById(R.id.tv_key_disable);
        switch_beep_sound = findViewById(R.id.switch_beep_sound);
        r_group_unit = findViewById(R.id.r_group_unit);
        rbtn_g = findViewById(R.id.rbtn_g);
        rbtn_oz = findViewById(R.id.rbtn_oz);
        r_group_auto_off_time = findViewById(R.id.r_group_auto_off_time);
        rbtn_0_min = findViewById(R.id.rbtn_0_min);
        rbtn_5_min = findViewById(R.id.rbtn_5_min);
        rbtn_10_min = findViewById(R.id.rbtn_10_min);
        rbtn_20_min = findViewById(R.id.rbtn_20_min);
        rbtn_30_min = findViewById(R.id.rbtn_30_min);
        rbtn_60_min = findViewById(R.id.rbtn_60_min);

        //set listeners
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected){
                    disconnectDevice();
                }else {
                    scanAndConnectDevice();
                }
            }
        });
        switch_beep_sound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    mCommunicationService.turnOnVoice();
                }else {
                    mCommunicationService.turnOffVoice();
                }
                mCommunicationService.requestPearlsStatus();
            }
        });
        rbtn_g.setOnCheckedChangeListener(onUnitCheckChangeListener);
        rbtn_oz.setOnCheckedChangeListener(onUnitCheckChangeListener);
        rbtn_0_min.setOnCheckedChangeListener(onAutoOffTimeCheckChangeListener);
        rbtn_5_min.setOnCheckedChangeListener(onAutoOffTimeCheckChangeListener);
        rbtn_10_min.setOnCheckedChangeListener(onAutoOffTimeCheckChangeListener);
        rbtn_20_min.setOnCheckedChangeListener(onAutoOffTimeCheckChangeListener);
        rbtn_30_min.setOnCheckedChangeListener(onAutoOffTimeCheckChangeListener);
        rbtn_60_min.setOnCheckedChangeListener(onAutoOffTimeCheckChangeListener);
    }

    private void disconnectDevice(){
        mCommunicationService.manualDisconnect();
        tv_weigh.setText("0.0 g");
        tv_device_name.setText("Device Name");
        tv_device_info.setText("Device Info");
        tv_battery.setText("Battery:");
        tv_capacity.setText("Capacity:");
        tv_key_disable.setText("Key Disable:");
    }

    private void scanAndConnectDevice(){
        if (blueadapter != null && !blueadapter.isEnabled()) {
            DialogHelper.showSettingBluetoothDialog(
                    this,
                    "Request permission",
                    "Please turn on Bluetooth");
        } else {
            if(mCommunicationService==null){
                mCommunicationService = ((AcaiaSDKSampleApp)getApplication()).getScaleCommunicationService();
            }
            loadingDialog.show();
            stopScanTimer.start();
            btn_connect.setClickable(false);
            btn_connect.setText("Connecting...");
            mCommunicationService.distanceConnect();
        }
    }

    private void showSettingItems(){
        switch_beep_sound.setVisibility(View.VISIBLE);
        r_group_unit.setVisibility(View.VISIBLE);
        r_group_auto_off_time.setVisibility(View.VISIBLE);
    }

    private void hideSettingItems(){
        switch_beep_sound.setVisibility(View.GONE);
        r_group_unit.setVisibility(View.GONE);
        r_group_auto_off_time.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScaleConnectStateEvent(ScaleConnectStateEvent event){
        stopScanTimer.cancel();
        isConnected = event.isConnected;
        if(isConnected){
            if (event.device!=null)
                tv_device_name.setText(event.device.getName());
            btn_connect.setText("Disconnect");
            showSettingItems();
        }else {
            tv_device_name.setText("Device Name");
            btn_connect.setText("Connect");
            hideSettingItems();
        }
        btn_connect.setClickable(true);
        if(loadingDialog!=null && loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPearlSStatusEvent(PearlSStatusEvent event) {
        tv_device_info.setText("");
        //Beep sound
        switch (event.beep){
            case 0:
                tv_device_info.setText(tv_device_info.getText().toString() + "Sound: OFF");
                switch_beep_sound.setChecked(false);
                break;
            case 1:
                //beep sound on
                tv_device_info.setText(tv_device_info.getText().toString() + "Sound: ON");
                switch_beep_sound.setChecked(true);
                break;
        }
        //Auto off time
        switch (event.autoOff){
            case 0:
                tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Auto off: Disabled");
                rbtn_0_min.setChecked(true);
                break;
            case 1:
                tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Auto off: 5 minutes");
                rbtn_5_min.setChecked(true);
                break;
            case 2:
                tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Auto off: 10 minutes");
                rbtn_10_min.setChecked(true);
                break;
            case 3:
                tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Auto off: 20 minutes");
                rbtn_20_min.setChecked(true);
                break;
            case 4:
                tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Auto off: 30 minutes");
                rbtn_30_min.setChecked(true);
                break;
            case 5:
                tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Auto off: 60 minutes");
                rbtn_60_min.setChecked(true);
                break;
        }
        //Weigh unit
        switch (event.unit){
            case 2:
                tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Weigh Unit: Gram");
                rbtn_g.setChecked(true);
                break;
            case 5:
                tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Weigh Unit: Ounce");
                rbtn_oz.setChecked(true);
                break;
        }
        //Mode
        tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Mode: Brewguide Mode");
        if(event.weighingMode==1){
            tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Mode: Weighing Mode");
        }
        if(event.dualDispMode==1){
            tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Mode: Dual Display Mode");
        }
        if(event.pourOverAutoStartMode==1){
            tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Mode: Pour Over Auto Start Mode");
        }
        if(event.protaMode==1){
            tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Mode: Protafilter Mode");
        }
        if(event.espressoMode==1){
            tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Mode: Espresso Mode");
        }
        if(event.pourOverMode==1){
            tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Mode: Flowrate Mode");
        }
        if(event.flowRatemode==1){
            tv_device_info.setText(tv_device_info.getText().toString() + "\n" + "Mode: Flowrate Practice Mode");
        }
    }

    private String unit_string = "";

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWeightEvent(WeightEvent event){
        double weight_value = event.weight.getValue();
        String weight_str;
        switch (event.weight.getUnitText()) {
            case "oz":
                weight_str = String.format(Locale.US, "%.3f", weight_value);
                tv_weigh.setText(weight_str + " " + event.weight.getUnitText());
                break;
            case "g":
                weight_str = String.format(Locale.US, "%.1f", weight_value);
                tv_weigh.setText(weight_str + " " + event.weight.getUnitText());
                break;
        }
        unit_string = event.weight.getUnitText();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScaleSettingUpdateEvent(ScaleSettingUpdateEvent event){
        if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_BATTERY.ordinal()){
            tv_battery.setText("battery: " + event.get_val() + "%");
        }else if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_CAPACITY.ordinal()){
//            double d_capasity = event.get_val();
//            String capacity = "";
//            switch (unit_string) {
//                case "oz":
//                    d_capasity = d_capasity * 0.0352739619;
//                    capacity = String.format(Locale.US, "%.3f", d_capasity);
//                    break;
//                case "g":
//                    capacity = String.format(Locale.US, "%.1f", d_capasity);
//                    break;
//            }
            tv_capacity.setText("Capacity: " + event.get_val() + " g");
        }else if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_KEY_DISABLED_ELAPSED_TIME.ordinal()){
            tv_key_disable.setText("Key disable: " + event.get_val());
        }
    }

    private boolean isPermissionGranted(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        String permission_denied_msg = "Permission denied";
        if (grantResults.length > 0) {
            if (!PermissionUtil.allPermissionsGranted(grantResults)) {
                if (PermissionUtil.somePermissionForeverDenied(this, permissions)) {
                    DialogHelper.showGoSettingDialog(
                            this,
                            "Request permission",
                            "Location permission");
                }else {
                    Toast.makeText(this, permission_denied_msg, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, permission_denied_msg, Toast.LENGTH_SHORT).show();
        }
    }
}
