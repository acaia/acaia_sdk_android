package co.acaia.android.acaiasdksampleapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import co.acaia.ble.events.ScaleConnectStateEvent;
import co.acaia.communications.scalecommand.ScaleCommandEvent;
import co.acaia.communications.scalecommand.ScaleCommandType;
import co.acaia.communications.scaleevent.UpdateTimerStartPauseEvent;
import co.acaia.communications.scaleevent.UpdateTimerValueEvent;

public class TimerActivity extends AppCompatActivity {
    private enum TIMER_STATE {START, PAUSE, STOP}
    private TextView tvTimer;
    private Button btnControl;
    private TIMER_STATE timerState = TIMER_STATE.STOP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        EventBus.getDefault().register(this);

        tvTimer = findViewById(R.id.tvTimer);
        btnControl = findViewById(R.id.btnControl);

        updateControlBtn();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private String sec2TimeText(int sec){
        String minute = sec / 60 < 10 ? "0" + sec / 60 : sec / 60 + "";
        String second = sec % 60 < 10 ? "0" + sec % 60 : sec % 60 + "";
        return minute + ":" + second;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateTimerValueEvent(UpdateTimerValueEvent event){
        Log.i("Dennis test", "sec: " + event.sec);
        tvTimer.setText(sec2TimeText(event.sec));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateTimerStartPauseEvent(UpdateTimerStartPauseEvent event){
        Log.i("Dennis test", "if_pause: " + event.if_pause);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScaleConnectStateEvent(ScaleConnectStateEvent event){
        if (!event.isConnected){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Scale disconnected, please connect again.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    resetViews();
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }

    private void resetViews(){
        timerState = TIMER_STATE.STOP;
        tvTimer.setText("00:00");
        updateControlBtn();
    }

    private void updateControlBtn(){
        switch (timerState){
            case STOP:
                btnControl.setText("Start");
                break;
            case START:
                btnControl.setText("Pause");
                break;
            case PAUSE:
                btnControl.setText("Stop");
                break;
        }
    }

    public void controlTimer(View v){
        switch (timerState){
            case STOP:
                timerState = TIMER_STATE.START;
                EventBus.getDefault().post(
                        new ScaleCommandEvent(ScaleCommandType.command_id.SEND_TIMER_COMMAND.ordinal(),
                                ScaleCommandType.set_timer.START.ordinal()));
                break;
            case START:
                timerState = TIMER_STATE.PAUSE;
                EventBus.getDefault().post(
                        new ScaleCommandEvent(ScaleCommandType.command_id.SEND_TIMER_COMMAND.ordinal(),
                                ScaleCommandType.set_timer.PAUSE.ordinal()));
                break;
            case PAUSE:
                timerState = TIMER_STATE.STOP;
                EventBus.getDefault().post(
                        new ScaleCommandEvent(ScaleCommandType.command_id.SEND_TIMER_COMMAND.ordinal(),
                                ScaleCommandType.set_timer.STOP.ordinal()));
                break;
        }
        updateControlBtn();
    }
}