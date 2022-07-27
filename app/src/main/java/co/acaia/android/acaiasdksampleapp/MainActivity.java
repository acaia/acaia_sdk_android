package co.acaia.android.acaiasdksampleapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
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
import co.acaia.communications.events.ServiceConnectionEvent;
import co.acaia.communications.events.WeightEvent;
import co.acaia.communications.scaleService.ScaleCommunicationService;
import co.acaia.communications.scalecommand.ScaleCommandEvent;
import co.acaia.communications.scalecommand.ScaleCommandType;
import co.acaia.communications.scalecommand.ScaleConnectionCommandEvent;
import co.acaia.communications.scalecommand.ScaleConnectionCommandEventType;
import co.acaia.communications.scaleevent.ScaleSettingUpdateEvent;
import co.acaia.communications.scaleevent.ScaleSettingUpdateEventType;

import static co.acaia.communications.scaleService.ScaleCommunicationService.ACTION_DEVICE_FOUND;
import static co.acaia.communications.scaleService.ScaleCommunicationService.EXTRA_DEVICE;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private ScaleCommunicationService mCommunicationService;
    private BluetoothAdapter blueAdapter;
    private BluetoothDevice currentDevice;
    private boolean isConnected = false;
    private boolean isServiceReady = false;
    private Button btnConnect, btn_tare, btn_go_timer;
    private TextView tvWeigh, tvDeviceName, tvDeviceInfo, tvBattery, tvCapacity;
    private SwitchCompat switchBeepSound;
    private RadioGroup rGroupCapacity, rGroupUnit, rGroupAutoOffTime;
    private RadioButton
            rbtnCapacity1000, rbtnCapacity2000,
            rbtnG, rbtnOz,
            rbtn0Min, rbtn5Min, rbtn10Min, rbtn20Min, rbtn30Min, rbtn60Min;
    private LoadingDialog loadingDialog;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private List<String> deviceNames = new ArrayList<>();
    private final int sec = 3;
    private CountDownTimer stopScanTimer = new CountDownTimer(1000*sec, 1000) {
        @Override
        public void onTick(long l) {

        }

        @Override
        public void onFinish() {
            if(loadingDialog!=null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            if (devices.size()>0){
                showDevices();
            }else {
                Toast.makeText(MainActivity.this, "No device found.", Toast.LENGTH_SHORT).show();
                btnConnect.setClickable(true);
                btnConnect.setText("Connect");
            }
        }
    };
    private CompoundButton.OnCheckedChangeListener onBeepOnOffListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(b){
                mCommunicationService.turnOnVoice();
            }else {
                mCommunicationService.turnOffVoice();
            }
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
        registerDeviceReceiver();
        blueAdapter = BluetoothAdapter.getDefaultAdapter();
        iniView();
    }

    @Override
    protected void onDestroy() {
        stopScanTimer.cancel();
        unregisterReceiver(deviceReceiever);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_more, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logs:
                showLogs();
                return true;
            case R.id.action_clear_logs:
                clearLogs();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void iniView(){
        loadingDialog = new LoadingDialog(this);
        btnConnect = findViewById(R.id.btn_connect);
        btn_tare = findViewById(R.id.btn_tare);
        btn_go_timer = findViewById(R.id.btn_go_timer);
        tvWeigh = findViewById(R.id.tv_weigh);
        tvDeviceName = findViewById(R.id.tv_device_name);
        tvDeviceInfo = findViewById(R.id.tv_device_info);
        tvBattery = findViewById(R.id.tv_battery);
        tvCapacity = findViewById(R.id.tv_capacity);
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
                    scanDevice();
                }
            }
        });
        btn_tare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new ScaleCommandEvent(ScaleCommandType.command_id.SEND_TARE.ordinal()));
            }
        });
        switchBeepSound.setOnCheckedChangeListener(onBeepOnOffListener);
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
    }

    private void resetInfo(){
        tvWeigh.setText("0.0 g");
        tvDeviceName.setText("Device Name");
        tvDeviceInfo.setText("Device Info");
        tvBattery.setText("Battery:");
        tvCapacity.setText("Capacity:");
    }

    private void disconnectDevice(){
        mCommunicationService.disconnect();
    }

    private boolean isBleEnable(){
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth is not supported !!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (blueAdapter != null && !blueAdapter.isEnabled()) {
            DialogHelper.turnOnBluetoothDialog(
                    this,
                    blueAdapter,
                    "Bluetooth requirement",
                    "Would you like to turn on bluetooth?");
            return false;
        }
        return true;
    }

    private boolean isGPSEnable(){
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            DialogHelper.showSettingGPSDialog(
                    this,
                    "GPS requirement",
                    "Please turn on GPS.");
            return false;
        }
        return true;
    }

    private void scanDevice(){
        if (isBleEnable() && isGPSEnable() && isPermissionGranted()){
            if (isServiceReady && !isConnected){
                if(mCommunicationService==null){
                    mCommunicationService = ((AcaiaSDKSampleApp)getApplication()).getScaleCommunicationService();
                }
                loadingDialog.show();
                stopScanTimer.start();
                btnConnect.setClickable(false);
                btnConnect.setText("Connecting...");
                devices.clear();
                deviceNames.clear();
                mCommunicationService.startScan();
            }
        }
    }

    private void showDevices(){
        String[] names = new String[deviceNames.size()];
        names = deviceNames.toArray(names);
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Connect device");
        builder.setItems(names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                connectDevice(devices.get(which));
            }
        });
        builder.show();
    }

    private void connectDevice(BluetoothDevice device){
        EventBus.getDefault().post(new ScaleConnectionCommandEvent(ScaleConnectionCommandEventType.connection_command.CONNECT.ordinal(), device.getAddress()));
    }

    private void showSettingItems(){
        btn_tare.setVisibility(View.VISIBLE);
        btn_go_timer.setVisibility(View.VISIBLE);
        switchBeepSound.setVisibility(View.VISIBLE);
        rGroupCapacity.setVisibility(View.VISIBLE);
        rGroupUnit.setVisibility(View.VISIBLE);
        rGroupAutoOffTime.setVisibility(View.VISIBLE);
        if(currentDevice.getName().contains("PEARLS")){
            rbtnCapacity2000.setText("3000 g");
        }else {
            rbtnCapacity2000.setText("2000 g");
        }
    }

    private void hideSettingItems(){
        resetInfo();
        btn_tare.setVisibility(View.GONE);
        btn_go_timer.setVisibility(View.GONE);
        switchBeepSound.setVisibility(View.GONE);
        rGroupCapacity.setVisibility(View.GONE);
        rGroupUnit.setVisibility(View.GONE);
        rGroupAutoOffTime.setVisibility(View.GONE);
    }

    @Subscribe
    public void onEvent(ServiceConnectionEvent event) {
        isServiceReady = event.isConnected();
        if(event.isConnected()){
            // Auto connect scale if the service is connected.
//            scanAndConnectDevice();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScaleConnectStateEvent(ScaleConnectStateEvent event){
        stopScanTimer.cancel();
        isConnected = event.isConnected;
        if(isConnected){
            if (event.device!=null) {
                clearLogs();
                currentDevice = event.device;
                tvDeviceName.setText(event.device.getName());
            }
            btnConnect.setText("Disconnect");
            showSettingItems();
            Toast.makeText(this, "Scale connected.", Toast.LENGTH_SHORT).show();
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
        //Battery
        tvBattery.setText("Battery: " + event.battery + "%");
        //Beep sound
        switchBeepSound.setOnCheckedChangeListener(null);
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
        switchBeepSound.setOnCheckedChangeListener(onBeepOnOffListener);
        //Auto off time
        switch (event.autoOff){
            case 0:
                rbtn0Min.setOnCheckedChangeListener(null);
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Auto off: Disabled");
                rbtn0Min.setChecked(true);
                rbtn0Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
                break;
            case 1:
                rbtn5Min.setOnCheckedChangeListener(null);
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Auto off: 5 minutes");
                rbtn5Min.setChecked(true);
                rbtn5Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
                break;
            case 2:
                rbtn10Min.setOnCheckedChangeListener(null);
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Auto off: 10 minutes");
                rbtn10Min.setChecked(true);
                rbtn10Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
                break;
            case 3:
                rbtn20Min.setOnCheckedChangeListener(null);
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Auto off: 20 minutes");
                rbtn20Min.setChecked(true);
                rbtn20Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
                break;
            case 4:
                rbtn30Min.setOnCheckedChangeListener(null);
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Auto off: 30 minutes");
                rbtn30Min.setChecked(true);
                rbtn30Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
                break;
            case 5:
                rbtn60Min.setOnCheckedChangeListener(null);
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Auto off: 60 minutes");
                rbtn60Min.setChecked(true);
                rbtn60Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
                break;
        }
        //Weigh unit
        addLogs("BM71 unit event:" + event.unit);
        switch (event.unit){
            case 2:
                rbtnG.setOnCheckedChangeListener(null);
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Weigh Unit: Gram");
                rbtnG.setChecked(true);
                rbtnG.setOnCheckedChangeListener(onUnitChangeListener);
                break;
            case 5:
                rbtnOz.setOnCheckedChangeListener(null);
                tvDeviceInfo.setText(tvDeviceInfo.getText().toString() + "\n" + "Weigh Unit: Ounce");
                rbtnOz.setChecked(true);
                rbtnOz.setOnCheckedChangeListener(onUnitChangeListener);
                break;
        }
        //Capacity
        String capacity = "";
        switch (event.capacity){
            case 0:
                rbtnCapacity1000.setOnCheckedChangeListener(null);
                capacity = "1000";
                rbtnCapacity1000.setChecked(true);
                rbtnCapacity1000.setOnCheckedChangeListener(onCapacityChangeListener);
                break;
            case 1:
                rbtnCapacity2000.setOnCheckedChangeListener(null);
                capacity = "3000";
                rbtnCapacity2000.setChecked(true);
                rbtnCapacity2000.setOnCheckedChangeListener(onCapacityChangeListener);
                break;
        }
        tvCapacity.setText("Capacity: " + capacity + " g");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWeightEvent(WeightEvent event){
        double weight_value = event.weight.getValue();
        String weight_str;
        switch (event.weight.getUnitText()) {
            case "oz":
                weight_str = String.format(Locale.US, "%.3f", weight_value);
                tvWeigh.setText(weight_str + " " + event.weight.getUnitText());
                addLogs("Weight:" + weight_str + " " + event.weight.getUnitText());
                break;
            case "g":
                weight_str = String.format(Locale.US, "%.1f", weight_value);
                tvWeigh.setText(weight_str + " " + event.weight.getUnitText());
                addLogs("Weight:" + weight_str + " " + event.weight.getUnitText());
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScaleSettingUpdateEvent(ScaleSettingUpdateEvent event){
        if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_BATTERY.ordinal()){
            tvBattery.setText("Battery: " + event.get_val() + "%");
        }else if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_BEEP.ordinal()){
            switchBeepSound.setOnCheckedChangeListener(null);
            if(event.get_val()==0.0f){
                switchBeepSound.setChecked(false);
            }else {
                //beep sound on
                switchBeepSound.setChecked(true);
            }
            switchBeepSound.setOnCheckedChangeListener(onBeepOnOffListener);
        }else if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_AUTO_OFF_TIME.ordinal()){
            rbtn0Min.setOnCheckedChangeListener(null);
            rbtn5Min.setOnCheckedChangeListener(null);
            rbtn10Min.setOnCheckedChangeListener(null);
            rbtn20Min.setOnCheckedChangeListener(null);
            rbtn30Min.setOnCheckedChangeListener(null);
            rbtn60Min.setOnCheckedChangeListener(null);
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
            rbtn0Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
            rbtn5Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
            rbtn10Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
            rbtn20Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
            rbtn30Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
            rbtn60Min.setOnCheckedChangeListener(onAutoOffTimeChangeListener);
        }else if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_UNIT.ordinal()){
            addLogs("Unit event:" + event.get_val());
            if(event.get_val()==0){
                rbtnG.setOnCheckedChangeListener(null);
                rbtnG.setChecked(true);
                rbtnG.setOnCheckedChangeListener(onUnitChangeListener);
            }else {
                rbtnOz.setOnCheckedChangeListener(null);
                rbtnOz.setChecked(true);
                rbtnOz.setOnCheckedChangeListener(onUnitChangeListener);
            }
        }else if(event.get_type() == ScaleSettingUpdateEventType.event_type.EVENT_CAPACITY.ordinal()){
            String capacity = "";
            if(event.get_val()==1){
                capacity = "2000";
                rbtnCapacity2000.setOnCheckedChangeListener(null);
                rbtnCapacity2000.setChecked(true);
                rbtnCapacity2000.setOnCheckedChangeListener(onCapacityChangeListener);
            }else {
                capacity = "1000";
                rbtnCapacity1000.setOnCheckedChangeListener(null);
                rbtnCapacity1000.setChecked(true);
                rbtnCapacity1000.setOnCheckedChangeListener(onCapacityChangeListener);
            }
            tvCapacity.setText("Capacity: " + capacity + " g");
        }
    }

    private boolean isPermissionGranted(){
        // Android M Permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Android Q Background Permission check
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    requestPermissions(
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            PERMISSION_REQUEST_COARSE_LOCATION);
                }else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_COARSE_LOCATION);
                }
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
                            "Please allow the location permission then app can start connection. " +
                                    "Would you like to go setting and allow it?");
                }else {
                    Toast.makeText(this, permission_denied_msg, Toast.LENGTH_SHORT).show();
                }
            }else {
                scanDevice();
            }
        } else {
            Toast.makeText(this, "Unexpected result.", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerDeviceReceiver() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ScaleCommunicationService.ACTION_DEVICE_FOUND);
        registerReceiver(deviceReceiever, intentFilter);
    }

    private BroadcastReceiver deviceReceiever = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ACTION_DEVICE_FOUND)){
                BluetoothDevice device = (BluetoothDevice) intent.getExtras().get(EXTRA_DEVICE);
                if (!devices.contains(device)){
                    devices.add(device);
                    deviceNames.add(device.getName());
                }
            }
        }
    };

    public void goTimer(View v){
        startActivity(new Intent(this, TimerActivity.class));
    }

    private String logText = "";
    private void addLogs(String log) {
        logText += log + "\n";
    }

    private void clearLogs() {
        logText = "";
    }

    private void showLogs() {
        if (TextUtils.isEmpty(logText)) logText = "Empty logs.";
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Logs");
        builder.setMessage(logText);
        builder.show();
    }
}
