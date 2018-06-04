package com.example.alo_i.smartsmsbox;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

public class SmsReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    LocationManager myLocManager;


    public static ChatActivity chatActivity;
    final SmsManager sms = SmsManager.getDefault();
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(SMS_RECEIVED)) {

            try {
                if(myLocManager == null){
                    myLocManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

                }

                if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
                    SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

                    if(ChatActivity.isItOpened){
                        for (SmsMessage message : smsMessages) {


                            String address = message.getDisplayOriginatingAddress();
                            String body = message.getMessageBody();
                            int category = categorizeSms(body,address);
                            Date date = new Date(Calendar.getInstance().getTimeInMillis());
                            int isItMe = 1;

                            Cursor cc;

                            MainActivity.lastID++;
                            String tid = String.valueOf(MainActivity.lastID);

                            String id = tid;

                            Location location = getLastKnownLocation(context);

                            Message newMessage = new Message(id,address,body,isItMe,date);


                            DBMessage mes;
                            double latitude,longitude,altitude;
                            if(location != null){
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                altitude = location.getAltitude();
                                mes = new DBMessage(tid,category,latitude,longitude,altitude);
                                mes.setText(body);

                            }else{
                                Toast.makeText(context,"No Location provider found!",Toast.LENGTH_SHORT).show();
                                mes = new DBMessage(tid,category,-1,-1,-1);
                                mes.setText(body);
                            }

                            pushMessageToDB(mes,tid);

                            List<Message> mList = ChatActivity.adapter.getlist();
                            mList.add(newMessage);
                            ChatActivity.adapter.notifyItemInserted(mList.size());
                            chatActivity.scrollToBottom();

                            controlMainScreen(address,newMessage);
                        }

                    }else{//chat activity is not opened

                        for (SmsMessage message : smsMessages) {

                            String address = message.getDisplayOriginatingAddress();
                            String body = message.getMessageBody();
                            int category = categorizeSms(body,address);

                            MainActivity.lastID++;
                            String tid = String.valueOf(MainActivity.lastID);

                            int isItMe = 1;

                            Location location = getLastKnownLocation(context);
                            Date date = new Date(Calendar.getInstance().getTimeInMillis());

                            Message newMessage = new Message(tid,address,body,isItMe,date);

                            DBMessage mes;
                            double latitude,longitude,altitude;
                            if(location != null){
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                altitude = location.getAltitude();
                                mes = new DBMessage(tid,category,latitude,longitude,altitude);
                                mes.setText(body);

                            }else{
                                Toast.makeText(context,"No Location provider found!",Toast.LENGTH_SHORT).show();
                                mes = new DBMessage(tid,category,-1,-1,-1);
                                mes.setText(body);
                            }

                            pushMessageToDB(mes,tid);

                            controlMainScreen(address,newMessage);

                        }

                    }

                }
            }catch (Exception ex){
                Log.e("onReceive",ex.getMessage());
            }

        }
    }

    private Location getLastKnownLocation(Context context) {
        myLocManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        List<String> providers = myLocManager.getProviders(true);
        Location bestLocation = null;

        if (checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,"No location permission",Toast.LENGTH_SHORT).show();
            return null;
        }

        for (String provider : providers) {

            Location l = myLocManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    void pushMessageToDB(DBMessage mes,String tid){
        StringBuilder values = new StringBuilder(75);

        values.append("('");
        values.append(tid);
        values.append("','");
        values.append(mes.getCategory());
        values.append("','");
        values.append( String.valueOf(mes.getLatitude()) );
        values.append("','");
        values.append( String.valueOf(mes.getLongitude()) );
        values.append("','");
        values.append( String.valueOf(mes.getAltitude()) );
        values.append("','");
        values.append( mes.getText());
        values.append("');");

        MainActivity.database.execSQL("INSERT INTO sms(id,category,latitude,longitude,altitude,text) VALUES "+values.toString());
    }

    int categorizeSms(String body,String address){
        final String[] otp_key_list = new String[]{"one time","password","tek sefer","şifre"};

        int len = body.length();
        if(MainActivity.blackList.contains(address)){
            return DBMessage.SPAM;
        }

        if(len < 4){
            return DBMessage.PERSONAL;
        }
        String Bcode = body.substring(len-4,len);
        if( Bcode.charAt(0) == 'B' ){
            int code = Integer.parseInt(Bcode.substring(1,4));
            if(code <1000){
                for(String str : otp_key_list){
                    if(body.toLowerCase().contains(str)){
                        return DBMessage.OTP;
                    }

                }
                return DBMessage.COMMERCIAL;
            }
        }

        return DBMessage.PERSONAL;
    }

    void controlMainScreen(String address,Message newMessage){
        if(MainActivity.blackList.contains(address)){
            int pos = TwoFragment.adapter.listContainsAddress(address);
            if(pos != -1){
                Person p = TwoFragment.adapter.getList().get(pos);
                p.setLastMessage(newMessage.getText());
                p.setLastMessageDate(newMessage.getDate());

                movePersonAtPosToTheTop(TwoFragment.adapter.getList(),pos);
                TwoFragment.adapter.notifyDataSetChanged();
            }else{
                MainActivity.blackList.add(address);
                TwoFragment.adapter.getList().add(new Person(address,newMessage.getText(),newMessage.getDate()));
                movePersonAtPosToTheTop(TwoFragment.adapter.getList(),TwoFragment.adapter.getItemCount());
                TwoFragment.adapter.notifyDataSetChanged();
            }

        }else{
            int pos = OneFragment.adapter.listContainsAddress(address);
            if(pos != -1){
                Person p = OneFragment.adapter.getList().get(pos);
                p.setLastMessage(newMessage.getText());
                p.setLastMessageDate(newMessage.getDate());

                movePersonAtPosToTheTop(OneFragment.adapter.getList(),pos);
                OneFragment.adapter.notifyDataSetChanged();
            }else{
                MainActivity.blackList.add(address);
                OneFragment.adapter.getList().add(new Person(address,newMessage.getText(),newMessage.getDate()));
                movePersonAtPosToTheTop(OneFragment.adapter.getList(),OneFragment.adapter.getItemCount());
                OneFragment.adapter.notifyDataSetChanged();
            }

        }
    }

    void movePersonAtPosToTheTop(List<Person> list , int pos){
        Person p = list.get(pos).clone();

        for(int i = pos ; i > 0 ; i--){
            list.set(i,list.get(i-1));
        }
        list.set(0,p);
    }

}