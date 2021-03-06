package com.example.haruka.rescue_aid.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Tomoya on 9/5/2017 AD.
 */

public class MedicalCertification implements Serializable {

    private static final long serialVersionUID = Utils.serialVersionUID_MedicalCertification;

    public ArrayList<Record> records;
    //Location location;
    //TODO location should be back to private
    public double[] location;
    private boolean isLocationSet;
    private final int LONGITUDE = 0, LATITUDE = 1;
    private final String LOCATION_TAG = "loc";
    private final String SCENARIO_TAG = "sce";;
    //private final String DEFAULT_ADDRESS = " 徳島県徳島市寺島本町西１丁目６１";
    public final static String DEFAULT_ADDRESS = " ー ";
    Date startAt;
    int scenarioID;
    //List<Address> addresses;
    //Address address;
    String addressString;

    public MedicalCertification(){
        startAt = QADateFormat.getDate(QADateFormat.getInstance());
        records = new ArrayList<>();
        Record r = new Record("start", "");
        records.add(r);
        location = new double[2];
        isLocationSet = false;
        /*
        times = new String[1];
        times[0] = r.time;
        tags = new String[1];
        tags[0] = r.tag;
        values = new String[];
        */
    }

    public MedicalCertification(String code){
        //TODO test data using QA reader
        records = new ArrayList<>();
        startAt = null;
        location = new double[2];
        isLocationSet = false;

        String[] array = code.split("\n");
        for (String line : array){
            Log.d("constructor MC", line);
            Record r;
            if (null == startAt) {
                r = new Record(line);
                startAt = QADateFormat.getDate(r.getTime());
            } else {
                r = new Record(startAt, line);
            }
            if (LOCATION_TAG.equals(r.getTag())){
                try {
                    String[] loc = r.getValue().split(":");
                    location = new double[2];
                    location[LONGITUDE] = Double.parseDouble(loc[LONGITUDE]);
                    location[LATITUDE] = Double.parseDouble(loc[LATITUDE]);
                    isLocationSet = true;
                }catch (Exception e){
                    Log.e("records location", e.toString());
                }
            } else if (SCENARIO_TAG.equals(r.getTag())){
                setScenario(Integer.parseInt(r.getValue()));
            } else{
                records.add(r);
            }
        }


        showLocation();

    }

    public void addRecord(Record r){
        records.add(r);
    }
    public void remove(Record r) {}

    public void updateLocation(Location location, Context context){
        //this.location = location;
        isLocationSet = true;
        this.location[LONGITUDE] = location.getLongitude();
        this.location[LATITUDE] = location.getLatitude();

        if(addressString == null) {
            try {
                Geocoder coder = new Geocoder(context);
                List<Address> addresses = coder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                Address address = addresses.get(0);
                Log.d("Geocoder location", address.getLocality());
                setAddressString(getAddressLine(address));
            } catch (Exception e) {
                Log.e("Geocoder location", e.toString());
            }
        }
    }

    public Location getLocation(){
        Location l = new Location("");
        if (isLocationSet) {
            l.setLongitude(location[LONGITUDE]);
            l.setLatitude(location[LATITUDE]);
        }
        return l;
    }

    public void showLocation(){
        if (!isLocationSet){
            Log.i("medical certification", "location is not set");
        } else {
            Log.i("medical certification", "location : "+ location[LONGITUDE] + ", " + location[LATITUDE]);
        }
    }

    public void showRecords(){
        showRecords("");
    }

    public void showRecords(String activity){
        Log.d("records", activity);
        for(Record r : records){
            Log.d("records", r.toString());
        }
        if (isLocationSet){
            Log.d("records", "location : " + Double.toString(location[LONGITUDE]) + ", " + Double.toString(location[LATITUDE]));
        }
    }

    public String toString(){
        showRecords();
        String res = "";
        Date startAt = null;

        for(Record r : records){
            if ("".equals(res)){
                res += r.toString();
                startAt = QADateFormat.getDate(r.getTime());
            }else{
                Date d = QADateFormat.getDate(r.getTime());
                String time = Long.toString(((d.getTime() - startAt.getTime())/1000));
                res += time + "," + r.getTagValue();
            }
            res += "\n";
        }
        if (isLocationSet){
            Record r = new Record(LOCATION_TAG, Double.toString(location[LONGITUDE]) + ":" + Double.toString(location[LATITUDE]));
            res += "0," + r.getTagValue();
        }

        return res;
    }

    public String getStartAtJap(){
        return QADateFormat.getStringDateJapanese(startAt);
    }

    public void setAddressString(String addressString){
        this.addressString = addressString;
    }

    public String getAddressLine(Address address) {
        String adr = "";
        adr += address.getAdminArea();
        if (address.getSubAdminArea() != null) {
            adr += address.getSubAdminArea();
        }
        adr += address.getLocality();
        adr += address.getSubLocality();

        if (address.getThoroughfare() != null) {
            adr += address.getThoroughfare();
        }
        if (address.getSubThoroughfare() != null) {
            adr += address.getSubThoroughfare();
        }
        return adr;
    }

    public String getAddress(Context context){
        if(addressString == null) {
            try {
                Geocoder coder = new Geocoder(context);
                List<Address> addresses = coder.getFromLocation(location[LATITUDE], location[LONGITUDE], 1);
                Address address = addresses.get(0);
                setAddressString(getAddressLine(address));
                Log.d("Geocoder location", addresses.get(0).getLocality());
                return getAddressLine(address);
            } catch (Exception e) {
                Log.e("Geocoder location", e.toString());
                return DEFAULT_ADDRESS;
            }
        } else{
            return addressString;
        }
    }

    public String getAddress(){
        if (addressString != null){
            return addressString;
        } else{
            return DEFAULT_ADDRESS;
        }
    }

    public void setScenario(int scenarioID){
        this.scenarioID = scenarioID;
        addRecord(new Record(SCENARIO_TAG, Integer.toString(scenarioID)));
    }

    public int getScenarioID(){
        return scenarioID;
    }

}
