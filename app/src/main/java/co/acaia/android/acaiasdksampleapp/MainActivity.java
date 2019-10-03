package co.acaia.android.acaiasdksampleapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.List;
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
    private UPLOAD_MODE uploadMode;
    private ScaleCommunicationService mCommunicationService;
    private BluetoothAdapter blueAdapter;
    private boolean isConnected = false;
    private Button btnConnect;
    private TextView tvWeigh, tvDeviceName, tvDeviceInfo, tvBattery, tvCapacity, tvKeyDisable;
    private ModeAdapter modeAdapter;
    private List<ModeAdapter.Mode> modeList = new ArrayList<>();
    private RecyclerView rcMode;
    private SwitchCompat switchBeepSound;
    private RadioGroup rGroupUnit, rGroupAutoOffTime;
    private RadioButton
            rbtnG, rbtnOz,
            rbtn0Min, rbtn5Min, rbtn10Min, rbtn20Min, rbtn30Min, rbtn60Min;
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
            btnConnect.setClickable(true);
            btnConnect.setText("Connect");
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
        blueAdapter = BluetoothAdapter.getDefaultAdapter();
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
        btnConnect = findViewById(R.id.button);
        tvWeigh = findViewById(R.id.tv_weigh);
        tvDeviceName = findViewById(R.id.tv_device_name);
        tvDeviceInfo = findViewById(R.id.tv_device_info);
        tvBattery = findViewById(R.id.tv_battery);
        tvCapacity = findViewById(R.id.tv_capacity);
        tvKeyDisable = findViewById(R.id.tv_key_disable);
        rcMode = findViewById(R.id.rc_mode_list);
        switchBeepSound = findViewById(R.id.switch_beep_sound);
        rGroupUnit = findViewById(R.id.r_group_unit);
        rbtnG = findViewById(R.id.rbtn_g);
        rbtnOz = findViewById(R.id.rbtn_oz);
        rGroupAutoOffTime = findViewById(R.id.r_group_auto_off_time);
        rbtn0Min = findViewById(R.id.rbtn_0_min);
        rbtn5Min = findViewById(R.id.rbtn_5_min);
        rbtn10Min = findViewById(R.id.rbtn_10_min);
        rbtn20Min = findViewById(R.id.rbtn_20_min);
        rbtn30Min = findViewById(R.id.rbtn_30_min);
        rbtn60Min = findViewById(R.id.rbtn_60_min);

        //set listeners
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected){
                    disconnectDevice();
                }else {
                    scanAndConnectDevice();
                }
            }
        });
        switchBeepSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        rbtnG.setOnCheckedChangeListener(onUnitCheckChangeListener);
        rbtnOz.setOnCheckedChangeListener(onUnitCheckChangeListener);
        rbtn0Min.setOnCheckedChangeListener(onAutoOffTimeCheckChangeListener);
        rbtn5Min.setOnCheckedChangeListener(onAutoOffTimeCheckChangeListener);
        rbtn10Min.setOnCheckedChangeListener(onAutoOffTimeCheckChangeListener);
        rbtn20Min.setOnCheckedChangeListener(onAutoOffTimeCheckChangeListener);
        rbtn30Min.setOnCheckedChangeListener(onAutoOffTimeCheckChangeListener);
        rbtn60Min.setOnCheckedChangeListener(onAutoOffTimeCheckChangeListener);

        modeAdapter = new ModeAdapter(modeList);
        rcMode.setLayoutManager(new LinearLayoutManager(this));
        rcMode.setAdapter(modeAdapter);
    }

    private void disconnectDevice(){
        mCommunicationService.manualDisconnect();
        tvWeigh.setText("0.0 g");
        tvDeviceName.setText("Device Name");
        tvDeviceInfo.setText("Device Info");
        tvBattery.setText("Battery:");
        tvCapacity.setText("Capacity:");
        tvKeyDisable.setText("Key Disable:");
    }

    private void scanAndConnectDevice(){
        if (blueAdapter != null && !blueAdapter.isEnabled()) {
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
            btnConnect.setClickable(false);
            btnConnect.setText("Connecting...");
            mCommunicationService.distanceConnect();
        }
    }

    private void showSettingItems(){
        switchBeepSound.setVisibility(View.VISIBLE);
        rGroupUnit.setVisibility(View.VISIBLE);
        rGroupAutoOffTime.setVisibility(View.VISIBLE);
        rcMode.setVisibility(View.VISIBLE);
    }

    private void hideSettingItems(){
        switchBeepSound.setVisibility(View.GONE);
        rGroupUnit.setVisibility(View.GONE);
        rGroupAutoOffTime.setVisibility(View.GONE);
        rcMode.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScaleConnectStateEvent(ScaleConnectStateEvent event){
        stopScanTimer.cancel();
        isConnected = event.isConnected;
        if(isConnected){
            if (event.device!=null)
                tvDeviceName.setText(event.device.getName());
            btnConnect.setText("Disconnect");
            showSettingItems();
        }else {
            tvDeviceName.setText("Device Name");
            btnConnect.setText("Connect");
            hideSettingItems();
        }
        btnConnect.setClickable(true);
        if(loadingDialog!=null && loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPearlSStatusEvent(PearlSStatusEvent event) {
        tvDeviceInfo.setText("");
        //Beep sound
        switch (event.beep){
            case 0:
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "Sound: OFF");
                switchBeepSound.setChecked(false);
                break;
            case 1:
                //beep sound on
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "Sound: ON");
                switchBeepSound.setChecked(true);
                break;
        }
        //Auto off time
        switch (event.autoOff){
            case 0:
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Auto off: Disabled");
                rbtn0Min.setChecked(true);
                break;
            case 1:
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Auto off: 5 minutes");
                rbtn5Min.setChecked(true);
                break;
            case 2:
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Auto off: 10 minutes");
                rbtn10Min.setChecked(true);
                break;
            case 3:
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Auto off: 20 minutes");
                rbtn20Min.setChecked(true);
                break;
            case 4:
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Auto off: 30 minutes");
                rbtn30Min.setChecked(true);
                break;
            case 5:
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Auto off: 60 minutes");
                rbtn60Min.setChecked(true);
                break;
        }
        //Weigh unit
        switch (event.unit){
            case 2:
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Weigh Unit: Gram");
                rbtnG.setChecked(true);
                break;
            case 5:
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Weigh Unit: Ounce");
                rbtnOz.setChecked(true);
                break;
        }
        //Mode
        tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Mode: Brewguide Mode");
        if(event.weighingMode==1){
            tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Mode: Weighing Mode");
        }
        if(event.dualDispMode==1){
            tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Mode: Dual Display Mode");
        }
        if(event.pourOverAutoStartMode==1){
            tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Mode: Pour Over Auto Start Mode");
        }
        if(event.protaMode==1){
            tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Mode: Protafilter Mode");
        }
        if(event.espressoMode==1){
            tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Mode: Espresso Mode");
        }
        if(event.pourOverMode==1){
            tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Mode: Flowrate Mode");
        }
        if(event.flowRatemode==1){
            tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Mode: Flowrate Practice Mode");
        }
        showMode(event);
    }

    private void showMode(PearlSStatusEvent event){
        if (modeList.size()<=0){
            modeList.add(new ModeAdapter.Mode(true, "Brewguide Mode"));
            modeList.add(new ModeAdapter.Mode(event.weighingMode==1, "Weighing Mode"));
            modeList.add(new ModeAdapter.Mode(event.dualDispMode==1, "Dual Display Mode"));
            modeList.add(new ModeAdapter.Mode(event.pourOverAutoStartMode==1, "Pour Over Auto Start Mode"));
            modeList.add(new ModeAdapter.Mode(event.protaMode==1, "Protafilter Mode"));
            modeList.add(new ModeAdapter.Mode(event.espressoMode==1, "Espresso Mode"));
            modeList.add(new ModeAdapter.Mode(event.pourOverMode==1, "Flowrate Mode"));
            modeList.add(new ModeAdapter.Mode(event.flowRatemode==1, "Flowrate Practice Mode"));
            modeAdapter.notifyItemRangeInserted(0, modeList.size());
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
                tvWeigh.setText(weight_str + " " + event.weight.getUnitText());
                break;
            case "g":
                weight_str = String.format(Locale.US, "%.1f", weight_value);
                tvWeigh.setText(weight_str + " " + event.weight.getUnitText());
                break;
        }
        unit_string = event.weight.getUnitText();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScaleSettingUpdateEvent(ScaleSettingUpdateEvent event){
        if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_BATTERY.ordinal()){
            tvBattery.setText("battery: " + event.get_val() + "%");
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
            tvCapacity.setText("Capacity: " + event.get_val() + " g");
        }else if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_KEY_DISABLED_ELAPSED_TIME.ordinal()){
            tvKeyDisable.setText("Key disable: " + event.get_val());
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
