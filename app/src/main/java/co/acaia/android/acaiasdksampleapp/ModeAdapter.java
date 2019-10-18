package co.acaia.android.acaiasdksampleapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import co.acaia.brewguide.events.PearlSModeEvent;
import co.acaia.communications.protocol.ver20.pearls.ScaleProtocol;

/**
 * Created by Dennis on 2019-10-03
 */
public class ModeAdapter extends RecyclerView.Adapter<ModeAdapter.ViewHolder> {
    private List<Mode> list;

    public ModeAdapter(List<Mode> list){
        this.list = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox_selected);
        }
    }

    @NonNull
    @Override
    public ModeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mode, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ModeAdapter.ViewHolder holder, final int position) {
        if (list!=null){
            final Mode mode = list.get(position);
            holder.checkBox.setChecked(mode.isSelected);
            holder.checkBox.setText(mode.modeName);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mode.modeName.equals("Brewguide Mode")){
                        return;
                    }
                    mode.isSelected = !mode.isSelected;
                    if(mode.modeName.equals("Weighing Mode")){
                        EventBus.getDefault().post(new PearlSModeEvent(ScaleProtocol.ESETTING_ITEM.e_setting_weighingmode, mode.isSelected?1:0));
                    }
                    if(mode.modeName.equals("Dual Display Mode")){
                        EventBus.getDefault().post(new PearlSModeEvent(ScaleProtocol.ESETTING_ITEM.e_setting_dual_displaymode, mode.isSelected?1:0));
                    }
                    if(mode.modeName.equals("Pour Over Auto Start Mode")){
                        EventBus.getDefault().post(new PearlSModeEvent(ScaleProtocol.ESETTING_ITEM.e_setting_pourover_autostart_mode, mode.isSelected?1:0));
                    }
                    if(mode.modeName.equals("Protafilter Mode")){
                        EventBus.getDefault().post(new PearlSModeEvent(ScaleProtocol.ESETTING_ITEM.e_setting_protafilter_mode, mode.isSelected?1:0));
                    }
                    if(mode.modeName.equals("Espresso Mode")){
                        EventBus.getDefault().post(new PearlSModeEvent(ScaleProtocol.ESETTING_ITEM.e_setting_espresso_mode, mode.isSelected?1:0));
                    }
                    if(mode.modeName.equals("Flowrate Mode")){
                        EventBus.getDefault().post(new PearlSModeEvent(ScaleProtocol.ESETTING_ITEM.e_setting_pourover_mode, mode.isSelected?1:0));
                    }
                    if(mode.modeName.equals("Flowrate Practice Mode")){
                        EventBus.getDefault().post(new PearlSModeEvent(ScaleProtocol.ESETTING_ITEM.e_setting_flowrate_mode, mode.isSelected?1:0));
                    }
                    notifyItemChanged(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Mode{
        boolean isSelected;
        String modeName;
        public Mode(boolean isSelected, String modeName){
            this.isSelected = isSelected;
            this.modeName = modeName;
        }
    }
}
