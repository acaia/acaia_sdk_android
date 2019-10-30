package co.acaia.android.acaiasdksampleapp;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.parse.Parse;

import org.greenrobot.eventbus.EventBus;

//import co.acaia.brewguide.BrewguideUploader;
import co.acaia.brewguide.BrewguideUploader;
import co.acaia.communications.events.WeightEvent;
import co.acaia.communications.scaleService.AcaiaScaleService;
import co.acaia.communications.scaleService.ScaleCommunicationService;
import co.acaia.communications.scaleevent.ScaleSettingUpdateEvent;
import co.acaia.communications.scaleevent.ScaleSettingUpdateEventType;

/**
 * Created by Dennis on 2019-09-26
 */
public class AcaiaSDKSampleApp extends Application {
    //Test server
    public static final String PARSE_APP_ID = "oLSJZ4Xk6qnXhNtMWPhvFmxviXTE5AzY5B7xRcNF";
    public static final String PARSE_CLIENT_KEY = "EMwXMUeE8b5hA1mcLgTrIukqwDw1BaKLQax5Wh3m";
    public static final String PARSE_SERVER = "https://parseapi.back4app.com/";

    public static BrewguideUploader brewguideUploader;
    public static AcaiaScaleService mAcaiaScaleService;

    @Override
    public void onCreate() {
        super.onCreate();
        brewguideUploader = new BrewguideUploader(BrewguideUploader.UPLOAD_MODE.upload_mode_brewguide);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(PARSE_APP_ID)
                .clientKey(PARSE_CLIENT_KEY)
                .server(PARSE_SERVER)
                .build()
        );
        initAcaiaBt();
        registerUpdateReceiver();
//        EventBus.getDefault().register(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
//        EventBus.getDefault().unregister(this);
    }

    private void initAcaiaBt() {
        mAcaiaScaleService = new AcaiaScaleService();
        mAcaiaScaleService.initialize(this);
    }

    public ScaleCommunicationService getScaleCommunicationService() {
        return (mAcaiaScaleService != null) ? mAcaiaScaleService.getScaleCommunicationService() : null;
    }

    private void registerUpdateReceiver() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ScaleCommunicationService.ACTION_DATA_AVAILABLE);
        //TODO There's no unregister call, dangerous!
        registerReceiver(updateReceiver, intentFilter);
    }

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (ScaleCommunicationService.ACTION_DATA_AVAILABLE.equals(action)) {

                try {
                    int resultType = intent.getExtras().getInt(ScaleCommunicationService.EXTRA_DATA_TYPE);
                    float val;

                    switch (resultType) {
                        case ScaleCommunicationService.DATA_TYPE_WEIGHT:
                            float result = intent.getExtras().getFloat("value");
                            int unit = intent.getExtras().getInt(ScaleCommunicationService.EXTRA_UNIT);

                            // Hanjord todo: migrate from Brewmaster
                            EventBus.getDefault().post(new WeightEvent(result, unit));
                            break;

                        case ScaleCommunicationService.DATA_TYPE_KEY_DISABLED_ELAPSED_TIME:
                            val = intent.getExtras().getFloat(ScaleCommunicationService.EXTRA_DATA);
                            EventBus.getDefault().post(new ScaleSettingUpdateEvent(ScaleSettingUpdateEventType.event_type.EVENT_KEY_DISABLED_ELAPSED_TIME.ordinal(), val));
                            break;

                        case ScaleCommunicationService.DATA_TYPE_BEEP:
                            val = intent.getExtras().getFloat(ScaleCommunicationService.EXTRA_DATA);
                            EventBus.getDefault().post(new ScaleSettingUpdateEvent(ScaleSettingUpdateEventType.event_type.EVENT_BEEP.ordinal(), val));
                            break;

                        case ScaleCommunicationService.DATA_TYPE_AUTO_OFF_TIME:
                            val = intent.getExtras().getFloat(ScaleCommunicationService.EXTRA_DATA);
                            EventBus.getDefault().post(new ScaleSettingUpdateEvent(ScaleSettingUpdateEventType.event_type.EVENT_AUTO_OFF_TIME.ordinal(), val));
                            break;

                        case ScaleCommunicationService.DATA_TYPE_BATTERY:
                            val = intent.getExtras().getFloat(ScaleCommunicationService.EXTRA_DATA);
                            EventBus.getDefault().post(new ScaleSettingUpdateEvent(ScaleSettingUpdateEventType.event_type.EVENT_BATTERY.ordinal(), val));
                            break;

                        case ScaleCommunicationService.DATA_TYPE_CAPACITY:
                            val = intent.getExtras().getFloat(ScaleCommunicationService.EXTRA_DATA);
                            EventBus.getDefault().post(new ScaleSettingUpdateEvent(ScaleSettingUpdateEventType.event_type.EVENT_CAPACITY.ordinal(), val));
                            break;

                        case ScaleCommunicationService.DATA_TYPE_UNIT:
                            val = intent.getExtras().getFloat(ScaleCommunicationService.EXTRA_DATA);
                            EventBus.getDefault().post(new ScaleSettingUpdateEvent(ScaleSettingUpdateEventType.event_type.EVENT_UNIT.ordinal(), val));
                            break;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };
}
