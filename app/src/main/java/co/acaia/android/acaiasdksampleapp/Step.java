package co.acaia.android.acaiasdksampleapp;

import android.content.Context;

/**
 * Created by Dennis on 2019-10-30
 */
public class Step {
    public enum Type {AMOUNT, DURATION, TEXT, AMOUNT_DURATION, FLOWRATE_INDICATOR}

    public enum SubType {WATER, COFFEE, STIR, PRESS, WAIT, POUR}

    private int index;
    private int brewStepType;
    private String stepTitle;
    private String message;
    private int brewStepSubType;
    private float weight;
    private int time;
    private int alertSound0 = 0;
    private int alertSound1 = 0;
    private int autoPause0 = 0;
    private int autoPause1 = 0;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Type getStepType() {
        switch (brewStepType) {
            case 0:
                return Type.AMOUNT;
            case 1:
                return Type.DURATION;
            case 2:
                return Type.TEXT;
            case 3:
                return Type.AMOUNT_DURATION;
            case 4:
                return Type.FLOWRATE_INDICATOR;
            default:
                return null;
        }
    }

    public void setStepType(Type stepType) {
        switch (stepType) {
            case AMOUNT:
                this.brewStepType = 0;
                break;
            case DURATION:
                this.brewStepType = 1;
                break;
            case TEXT:
                this.brewStepType = 2;
                break;
            case AMOUNT_DURATION:
                this.brewStepType = 3;
                break;
            case FLOWRATE_INDICATOR:
                this.brewStepType = 4;
                break;
        }
    }

    public String getStepTitle() {
        return stepTitle;
    }

    public void setStepTitle(String stepTitle) {
        this.stepTitle = stepTitle;
    }

    public String getDescription() {
        return message;
    }

    public void setDescription(String message) {
        this.message = message;
    }

    public SubType getStepSubType() {
        switch (brewStepSubType) {
            case 0:
                return SubType.WATER;
            case 1:
                return SubType.COFFEE;
            case 2:
                return SubType.STIR;
            case 3:
                return SubType.PRESS;
            case 4:
                return SubType.WAIT;
            case 5:
                return SubType.POUR;
            default:
                return null;
        }
    }

    public void setStepSubType(SubType subType) {
        switch (subType) {
            case WATER:
                brewStepSubType = 0;
                break;
            case COFFEE:
                brewStepSubType = 1;
                break;
            case STIR:
                brewStepSubType = 2;
                break;
            case PRESS:
                brewStepSubType = 3;
                break;
            case WAIT:
                brewStepSubType = 4;
                break;
            case POUR:
                brewStepSubType = 5;
                break;
        }
    }

    public float getAmount() {
        return weight;
    }

    public void setAmount(float weight) {
        this.weight = weight;
    }

    public int getDuration() {
        return time;
    }

    public void setDuration(int time) {
        this.time = time;
    }

    public int getAutoPause0() {
        return autoPause0;
    }

    public void setAutoPause0(int autoPause0) {
        this.autoPause0 = autoPause0;
    }

    public int getAutoPause1() {
        return autoPause1;
    }

    public void setAutoPause1(int autoPause1) {
        this.autoPause1 = autoPause1;
    }

    public int getAlertSound0() {
        return alertSound0;
    }

    public String getAlertSound0(Context context) {
        switch (alertSound0) {
            case 1:
                return context.getResources().getString(R.string.low_voice);
            case 2:
                return context.getResources().getString(R.string.high_voice);
            case 3:
                return context.getResources().getString(R.string.high_and_low_voice);
            default:
                return "";
        }
    }

    public void setAlertSound0(int stringId) {
        switch (stringId) {
            case R.string.low_voice:
                alertSound0 = 1;
                break;
            case R.string.high_voice:
                alertSound0 = 2;
                break;
            case R.string.high_and_low_voice:
                alertSound0 = 3;
                break;
        }
    }

    public int getAlertSound1() {
        return alertSound1;
    }

    public String getAlertSound1(Context context) {
        switch (alertSound1) {
            case 1:
                return context.getResources().getString(R.string.low_voice);
            case 2:
                return context.getResources().getString(R.string.high_voice);
            case 3:
                return context.getResources().getString(R.string.high_and_low_voice);
            default:
                return "";
        }
    }

    public void setAlertSound1(int stringId) {
        switch (stringId) {
            case R.string.low_voice:
                alertSound1 = 1;
                break;
            case R.string.high_voice:
                alertSound1 = 2;
                break;
            case R.string.high_and_low_voice:
                alertSound1 = 3;
                break;
        }
    }

    public static int[] getOptionsResourceIdArr() {
        return new int[]{R.string.low_voice, R.string.high_voice, R.string.high_and_low_voice};
    }
}
