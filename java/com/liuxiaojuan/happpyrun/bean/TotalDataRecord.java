package com.liuxiaojuan.happpyrun.bean;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * 存储String类型的首页总数据：
 */
public class TotalDataRecord implements Parcelable {
    private String total_distance;
    private String total_minitues;
    private String total_count;

    TotalDataRecord (){

    }

    protected TotalDataRecord(Parcel in) {
        total_distance = in.readString();
        total_minitues = in.readString();
        total_count = in.readString();
    }

    public static final Creator<TotalDataRecord> CREATOR = new Creator<TotalDataRecord>() {
        @Override
        public TotalDataRecord createFromParcel(Parcel in) {
            return new TotalDataRecord(in);
        }

        @Override
        public TotalDataRecord[] newArray(int size) {
            return new TotalDataRecord[size];
        }
    };

    public String getTotal_distance() {
         return total_distance ;
    }

    public String getTotal_minitues() {
         return total_minitues ;
    }

    public String getTotal_count() {
         return total_count ;
    }

    public void setTotal_count(String toal_count) {
         this.total_count = toal_count ;
    }

    public void setTotal_distance(String total_distance) {
         this.total_distance = total_distance ;
    }

    public void setTotal_minitues(String total_minitues) {
         this .total_minitues = total_minitues ;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(total_distance);
        parcel.writeString(total_minitues);
        parcel.writeString(total_count);
    }
}
