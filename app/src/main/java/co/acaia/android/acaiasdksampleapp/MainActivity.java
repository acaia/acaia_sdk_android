package co.acaia.android.acaiasdksampleapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
    private BluetoothDevice currentDevice;
    private boolean isConnected = false;
    private Button btnConnect;
    private TextView tvWeigh, tvDeviceName, tvDeviceInfo, tvBattery, tvCapacity, tvKeyDisable;
    private ModeAdapter modeAdapter;
    private List<ModeAdapter.Mode> modeList = new ArrayList<>();
    private RecyclerView rcMode;
    private SwitchCompat switchBeepSound;
    private RadioGroup rGroupCapacity, rGroupUnit, rGroupAutoOffTime;
    private RadioButton
            rbtnCapacity1000, rbtnCapacity2000,
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
    private CompoundButton.OnCheckedChangeListener onCapacityChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(b){
                switch (compoundButton.getId()){
                    case R.id.rbtn_capacity_1000:
                        mCommunicationService.setCapacity1000();
                        break;
                    case R.id.rbtn_capacity_2000:
                        mCommunicationService.setCapacity2000();
                        break;
                }
            }
        }
    };
    private CompoundButton.OnCheckedChangeListener onUnitChangeListener = new CompoundButton.OnCheckedChangeListener() {
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
            }
        }
    };
    private CompoundButton.OnCheckedChangeListener onAutoOffTimeChangeListener = new CompoundButton.OnCheckedChangeListener() {
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
        rGroupCapacity = findViewById(R.id.r_group_capacity);
        rbtnCapacity1000 = findViewById(R.id.rbtn_capacity_1000);
        rbtnCapacity2000 = findViewById(R.id.rbtn_capacity_2000);
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
            }
        });
        rbtnCapacity1000.setOnCheckedChangeListener(onCapacityChangeListener);
        rbtnCapacity2000.setOnCheckedChangeListener(onCapacityChangeListener);
        rbtnG.setOnCheckedChangeListener(onUnitChangeListener);
        rbtnOz.setOnCheckedChangeListener(onUnitChangeListener);
        rbtn0Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
        rbtn5Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
        rbtn10Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
        rbtn20Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
        rbtn30Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
        rbtn60Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);

        modeAdapter = new ModeAdapter(modeList);
        rcMode.setLayoutManager(new LinearLayoutManager(this));
        rcMode.setAdapter(modeAdapter);
    }

    private void resetInfo(){
        tvWeigh.setText("0.0 g");
        tvDeviceName.setText("Device Name");
        tvDeviceInfo.setText("Device Info");
        tvBattery.setText("Battery:");
        tvCapacity.setText("Capacity:");
        tvKeyDisable.setText("Key Disable:");
    }

    private void disconnectDevice(){
        mCommunicationService.disconnect();
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
            mCommunicationService.autoConnect(1000*sec);
        }
    }

    private void showSettingItems(){
        switchBeepSound.setVisibility(View.VISIBLE);
        rGroupCapacity.setVisibility(View.VISIBLE);
        rGroupUnit.setVisibility(View.VISIBLE);
        rGroupAutoOffTime.setVisibility(View.VISIBLE);
        rcMode.setVisibility(View.VISIBLE);
        if(currentDevice.getName().contains("PEARLS")){
            rcMode.setVisibility(View.VISIBLE);
        }
    }

    private void hideSettingItems(){
        resetInfo();
        switchBeepSound.setVisibility(View.GONE);
        rGroupCapacity.setVisibility(View.GONE);
        rGroupUnit.setVisibility(View.GONE);
        rGroupAutoOffTime.setVisibility(View.GONE);
        rcMode.setVisibility(View.GONE);
        if(rcMode.getVisibility() == View.VISIBLE){
            rcMode.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScaleConnectStateEvent(ScaleConnectStateEvent event){
        stopScanTimer.cancel();
        isConnected = event.isConnected;
        if(isConnected){
            if (event.device!=null) {
                currentDevice = event.device;
                tvDeviceName.setText(event.device.getName());
            }
//            if(event.device.getName().contains("PROCH")){
//                EventBus.getDefault().post(new ScaleCommandEvent(ScaleCommandType.command_id.GET_SOUND_ONOFF.ordinal()));
//                EventBus.getDefault().post(new ScaleCommandEvent(ScaleCommandType.command_id.GET_AUTO_OFF_TIME.ordinal()));
//                EventBus.getDefault().post(new ScaleCommandEvent(ScaleCommandType.command_id.GET_BATT.ordinal()));
//                EventBus.getDefault().post(new ScaleCommandEvent(ScaleCommandType.command_id.GET_CAPACITY.ordinal()));
//            }
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
        if(currentDevice!=null && currentDevice.getName().contains("PEARLS")){
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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScaleSettingUpdateEvent(ScaleSettingUpdateEvent event){
        if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_BATTERY.ordinal()){
            tvBattery.setText("battery: " + event.get_val() + "%");
        }else if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_BEEP.ordinal()){
            if(event.get_val()==0.0f){
                switchBeepSound.setChecked(false);
            }else {
                //beep sound on
                switchBeepSound.setChecked(true);
            }
        }else if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_AUTO_OFF_TIME.ordinal()){
            if(event.get_val()==0.0f){
                rbtn0Min.setChecked(true);
            }else if(event.get_val()==1.0f){
                rbtn5Min.setChecked(true);
            }else if(event.get_val()==2.0f){
                rbtn10Min.setChecked(true);
            }else if(event.get_val()==3.0f){
                rbtn20Min.setChecked(true);
            }else if(event.get_val()==4.0f){
                rbtn30Min.setChecked(true);
            }else if(event.get_val()==5.0f){
                rbtn60Min.setChecked(true);
            }
        }else if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_CAPACITY.ordinal()){
            String capacity = "";
            if(event.get_val()==1){
                capacity = "2000";
                rbtnCapacity2000.setChecked(true);
            }else {
                capacity = "1000";
                rbtnCapacity1000.setChecked(true);
            }
            tvCapacity.setText("Capacity: " + capacity + " g");
        }else if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_KEY_DISABLED_ELAPSED_TIME.ordinal()){
            tvKeyDisable.setText("Key disable: " + event.get_val());
        }else if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_UNIT.ordinal()){
            if(event.get_val()==0){
                rbtnG.setChecked(true);
            }else if(event.get_val()==1){
                rbtnOz.setChecked(true);
            }
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
