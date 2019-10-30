package co.acaia.android.acaiasdksampleapp;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Dennis on 2019-10-30
 */
public class Brew {
    private String roastery;
    private String coffee_name;
    private String brewer;
    private String origin;
    private String roast_level;
    private String website;
    private float coffee_amount;
    private float water_amount;
    private int serving_people = -9999;
    private float serving_weight_g = -9999;
    private String grind_settings;
    private String grinder1;
    private String grinder2;
    private float temperature;
    private List<Step> steps = new ArrayList<>();
    private String brewStepsJson;
    private boolean isTest = true;
    private String objectId;//parseObjectID
    private String userID;
    private byte[] qrCode;
    private int recommended;
    private String wordsFromRoastery;
    private boolean isPublic;
    private String uuid;
    private Date updatedAt;
    private boolean isDeleted;

    public String getRoastery() {
        return roastery;
    }

    public void setRoastery(String roastery) {
        this.roastery = roastery;
    }

    public String getCoffeeName() {
        return coffee_name;
    }

    public void setCoffeeName(String coffee_name) {
        this.coffee_name = coffee_name;
    }

    public String getBrewer() {
        return brewer;
    }

    public void setBrewer(String brewer) {
        this.brewer = brewer;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getRoastLevel() {
        return roast_level;
    }

    public void setRoastLevel(String roast_level) {
        this.roast_level = roast_level;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public float getCoffeeAmount() {
        return coffee_amount;
    }

    public void setCoffeeAmount(float coffee_amount) {
        this.coffee_amount = coffee_amount;
    }

    public float getWaterAmount() {
        return water_amount;
    }

    public void setWaterAmount(float water_amount) {
        this.water_amount = water_amount;
    }

    public int getServingPeople() {
        return serving_people;
    }

    public void setServingPeople(int serving_people) {
        this.serving_people = serving_people;
    }

    public float getServingWeightGram() {
        return serving_weight_g;
    }

    public void setServingWeightGram(float serving_weight_g) {
        this.serving_weight_g = serving_weight_g;
    }

    public String getGrindSettings() {
        return grind_settings;
    }

    public void setGrindSettings(String grind_settings) {
        this.grind_settings = grind_settings;
    }

    public String getGrinder1() {
        return grinder1;
    }

    public void setGrinder1(String grinder1) {
        this.grinder1 = grinder1;
    }

    public String getGrinder2() {
        return grinder2;
    }

    public void setGrinder2(String grinder2) {
        this.grinder2 = grinder2;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public List<Step> getStepItems() {
        if(!isNullOrEmpty(brewStepsJson)){
            steps.clear();
            Type listType = new TypeToken<List<Step>>(){}.getType();
            List<Step> list = new Gson().fromJson(brewStepsJson, listType);
            if(list!=null){
                steps.addAll(list);
            }
        }
        return steps;
    }

    public void setStepItems(List<Step> steps) {
        this.brewStepsJson = new Gson().toJson(steps);
    }

    public String getBrewStepsJson(){
        return brewStepsJson;
    }

    public void setBrewStepsJson(String brewStepsJson) {
        this.brewStepsJson = brewStepsJson;
    }

    public byte[] getQrCode() {
        return qrCode;
    }

    public void setQrCode(byte[] qrCode) {
        this.qrCode = qrCode;
    }

    public int getRecommended() {
        return recommended;
    }

    public void setRecommended(int recommended) {
        this.recommended = recommended;
    }

    public String getWordsFromRoastery() {
        return wordsFromRoastery;
    }

    public void setWordsFromRoastery(String wordsFromRoastery) {
        this.wordsFromRoastery = wordsFromRoastery;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setTest(boolean test) {
        isTest = test;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    private boolean isNullOrEmpty(String txt){
        return txt==null || TextUtils.isEmpty(txt.trim());
    }

    public ParseObject createParse(Context context){
        ParseObject parsebrew = new ParseObject("Brewguide");
        parsebrew.put(ParseBrew.PHOTO1, JSONObject.NULL);
        parsebrew.put(ParseBrew.PHOTO2, JSONObject.NULL);
        parsebrew.put(ParseBrew.PHOTO3, JSONObject.NULL);
        if(!isNullOrEmpty(grind_settings)){
            parsebrew.put(ParseBrew.GRINDER, grind_settings);
        }else {
            parsebrew.remove(ParseBrew.GRINDER);
        }
        if(!isNullOrEmpty(grinder1)){
            parsebrew.put(ParseBrew.GRINDER1, grinder1);
        }else {
            parsebrew.remove(ParseBrew.GRINDER1);
        }
        if(!isNullOrEmpty(grinder2)){
            parsebrew.put(ParseBrew.GRINDER2, grinder2);
        }else {
            parsebrew.remove(ParseBrew.GRINDER2);
        }
        parsebrew.put(ParseBrew.USERID, userID);
        parsebrew.put(ParseBrew.ORIGIN, origin);
        parsebrew.put(ParseBrew.ROASTERY, roastery);
        if(!isNullOrEmpty(brewStepsJson)){
            parsebrew.put(ParseBrew.BREW_STEPS_JSON, brewStepsJson);
        }else{
            parsebrew.remove(ParseBrew.BREW_STEPS_JSON);
        }
        parsebrew.put(ParseBrew.BREWER, brewer);
        parsebrew.put(ParseBrew.TEMPERATURE, temperature);
        parsebrew.put(ParseBrew.ROAST_WEBSITE, website);
        parsebrew.remove(ParseBrew.SERVINGS);
        parsebrew.put(ParseBrew.SERVINGS_PEOPLE, serving_people);
        parsebrew.put(ParseBrew.SERVINGS_WEIGHT_GRAM, serving_weight_g);
        parsebrew.put(ParseBrew.COFFEE_NAME, coffee_name);
        if(!isNullOrEmpty(wordsFromRoastery)){
            parsebrew.put(ParseBrew.WORDS_FROM_ROASTERY, wordsFromRoastery);
        }else{
            parsebrew.remove(ParseBrew.WORDS_FROM_ROASTERY);
        }
        parsebrew.put(ParseBrew.WATER_WEIGHT_GRAM, water_amount);
        parsebrew.put(ParseBrew.COFFEE_WEIGHT_GRAM, coffee_amount);
        parsebrew.put(ParseBrew.RECOMMENDED, recommended);
        parsebrew.put(ParseBrew.UUID, uuid);
        parsebrew.put(ParseBrew.IS_TEST, isTest);
        parsebrew.put(ParseBrew.ROAST_LEVEL, roast_level);
        parsebrew.put(ParseBrew.IS_PUBLIC, isPublic);
        if(!isNullOrEmpty(objectId)){
            parsebrew.setObjectId(objectId);
        }
        parsebrew.put(ParseBrew.IS_DELETED, isDeleted);
        return parsebrew;
    }

    public Brew(ParseObject parseBrew) {
        setUuid((String) parseBrew.get(ParseBrew.UUID));
        setCoffeeName((String) parseBrew.get(ParseBrew.COFFEE_NAME));
        setRoastery((String) parseBrew.get(ParseBrew.ROASTERY));
        setOrigin((String) parseBrew.get(ParseBrew.ORIGIN));
        setRoastLevel((String) parseBrew.get(ParseBrew.ROAST_LEVEL));
        setWebsite((String) parseBrew.get(ParseBrew.ROAST_WEBSITE));
        setBrewer((String) parseBrew.get(ParseBrew.BREWER));
        setCoffeeAmount(Float.parseFloat(String.valueOf(parseBrew.get(ParseBrew.COFFEE_WEIGHT_GRAM))));
        setWaterAmount(Float.parseFloat(String.valueOf(parseBrew.get(ParseBrew.WATER_WEIGHT_GRAM))));
        if(parseBrew.get(ParseBrew.SERVINGS_PEOPLE)!=null){
            setServingPeople((Integer) parseBrew.get(ParseBrew.SERVINGS_PEOPLE));
        }
        if(parseBrew.get(ParseBrew.SERVINGS_WEIGHT_GRAM)!=null){
            setServingWeightGram(Float.parseFloat(String.valueOf(parseBrew.get(ParseBrew.SERVINGS_WEIGHT_GRAM))));
        }
        setGrindSettings((String) parseBrew.get(ParseBrew.GRINDER));
        setGrinder1((String) parseBrew.get(ParseBrew.GRINDER1));
        setGrinder2((String) parseBrew.get(ParseBrew.GRINDER2));
        setTemperature(Float.parseFloat(String.valueOf(parseBrew.get(ParseBrew.TEMPERATURE))));
        setObjectId(parseBrew.getObjectId());
        setUserID((String) parseBrew.get(ParseBrew.USERID));
        setBrewStepsJson((String) parseBrew.get(ParseBrew.BREW_STEPS_JSON));
        if(parseBrew.get(ParseBrew.RECOMMENDED) != null){
            setRecommended((Integer) parseBrew.get(ParseBrew.RECOMMENDED));
        }
        if(parseBrew.get(ParseBrew.WORDS_FROM_ROASTERY) != null){
            setWordsFromRoastery((String) parseBrew.get(ParseBrew.WORDS_FROM_ROASTERY));
        }
        if(parseBrew.getParseFile(ParseBrew.QRCODE) != null){
            parseBrew.getParseFile(ParseBrew.QRCODE).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    setQrCode(data);
                }
            });
        }
        if(parseBrew.get(ParseBrew.IS_PUBLIC)!=null){
            setPublic((Boolean) parseBrew.get(ParseBrew.IS_PUBLIC));
        }
        Date date = parseBrew.getUpdatedAt();
        setUpdatedAt(date);
        setDeleted((Boolean) parseBrew.get(ParseBrew.IS_DELETED));
    }
}
