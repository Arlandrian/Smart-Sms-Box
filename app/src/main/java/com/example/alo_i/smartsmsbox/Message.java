package com.example.alo_i.smartsmsbox;

import java.io.Serializable;
import java.sql.Date;

public class Message implements Serializable{
    private String id;
    private String address;
    private String text;
    private int isItMe;//1: them -- 2: me
    private Location location;
    private Date date;

    public Message(String id, String address, String text, int isItMe, Location location,Date date) {
        this.id = id;
        this.address = address;
        this.text = text;
        this.isItMe = isItMe;
        this.date = date;
        this.location = location;
    }

    public Message(String id, String address, String text, int isItMe, Date date) {
        this.id = id;
        this.address = address;
        this.text = text;
        this.isItMe = isItMe;
        this.date = date;
    }
    public Message(String id, String address, String text, int isItMe) {
        this.id = id;
        this.address = address;
        this.text = text;
        this.isItMe = isItMe;
        this.date = date;
    }

    public Message(String text, int isItMe, Location location) {

        this.text = text;
        this.isItMe = isItMe;
        this.location = location;
    }
    public Message(String text, int isItMe,Date date) {

        this.text = text;
        this.isItMe = isItMe;
        this.date =date;
    }
    public Message(String id,String text, int isItMe,Date date) {

        this.text = text;
        this.isItMe = isItMe;
        this.date =date;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        if(this.id != null){
            builder.append("ID:");
            builder.append(id);
            builder.append(" ");
        }
        if(this.address != null){
            builder.append("Address:");
            builder.append(address);
            builder.append(" ");
        }
        if(this.text != null){
            builder.append("Text:");
            builder.append(text);
            builder.append(" ");
        }
        if(this.isItMe == 1 || this.isItMe == 2){
            builder.append("Me:");
            builder.append(isItMe);
            builder.append(" ");
        }
        if(this.date != null){
            builder.append("Date:");
            builder.append(date);
            builder.append(" ");
        }
        if(this.location != null){
            builder.append("Loc:");
            builder.append(location);
            builder.append(" ");
        }

        return  builder.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getIsItMe() {
        return isItMe;
    }

    public void setIsItMe(int isItMe) {
        this.isItMe = isItMe;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
