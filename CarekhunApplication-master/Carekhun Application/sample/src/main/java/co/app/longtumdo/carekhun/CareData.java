package co.app.longtumdo.carekhun;

/**
 * Created by suttipong on 10/1/2017.
 */

public class CareData {

    public int height;
    public int weight;
    public int sedentaryRemind;
    public String distance;
    public int calories;
    public int heartrate;
    public int rssi;

    public CareData(int height, int weight, int sedentaryRemind, String distance, int calories, int heartrate, int rssi) {
        this.height = height;
        this.weight = weight;
        this.sedentaryRemind = sedentaryRemind;
        this.distance = distance;
        this.calories = calories;
        this.heartrate = heartrate;
        this.rssi = rssi;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getSedentaryRemind() {
        return sedentaryRemind;
    }

    public void setSedentaryRemind(int sedentaryRemind) {
        this.sedentaryRemind = sedentaryRemind;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getHeartrate() {
        return heartrate;
    }

    public void setHeartrate(int heartrate) {
        this.heartrate = heartrate;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
