package com.example.alo_i.smartsmsbox;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Provider;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class ChatActivity extends AppCompatActivity{

    String currentAddress,currentName;
    int currentPosition;

    ImageButton sendButton;

    EditText inputText;

    TextView personName;

    List<Message> messageList;

    RecyclerView recyclerView;
    public static MessageAdapter adapter;

    int sentSMS = 0;
    int receivedSMS = 0;

    Context context;

    LocationManager myLocManager;


    public static boolean isItOpened = false;//when this activity is opened true


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        context =getApplicationContext();
        SmsReceiver.chatActivity = this;

        currentAddress = getIntent().getStringExtra("address");
        currentName = getIntent().getStringExtra("name");
        currentPosition = getIntent().getIntExtra("pos",-1);

        personName = (TextView)findViewById(R.id.selectedPerson_TV) ;
        personName.setText(currentName);

        sendButton = (ImageButton) findViewById(R.id.imageButtonSend);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!inputText.getText().toString().isEmpty()){
                    sendSMS(currentAddress,inputText.getText().toString());
                    inputText.setText("");
                }

            }
        });

        inputText = (EditText)findViewById(R.id.inputTextET);

        messageList = new ArrayList<Message>() ;

        recyclerView = (RecyclerView)findViewById(R.id.chat_recView);

        adapter = new MessageAdapter(messageList,currentAddress,context);

        RecyclerView.LayoutManager lm = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);

        getAllSmsWithX();
        recyclerView.scrollToPosition(adapter.getItemCount()-1);
        isMessageListChanged = false;

        sentSMS = 0;
        receivedSMS = 0;

        int x= getLastMessageId();
        if(x != MainActivity.lastID){
            Log.e("LAST ID ERROR","last id is not true");
        }

        myLocManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        isItOpened = true;
    }
    //PhoneNumberFormattingTextWatcher phoneFormat = new PhoneNumberFormattingTextWatcher("+90");
    //private final SimpleDateFormat sm = new SimpleDateFormat("dd/MM/yyyy");

    public void getAllSmsWithX() {

        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(message, new String[]{"_id","address","body","type","date"},
                "address = '" + currentAddress+"'", null, "date ASC");
        Toast.makeText(context,currentAddress,Toast.LENGTH_SHORT).show();



        if (c.moveToFirst()) {
            do {

                String address = c.getString(1);
                String text = c.getString(2);
                int type = c.getInt(3);
                long date = c.getLong(4);
                String lastId = c.getString(0);
                Message message2 = new Message(lastId, address, text, type, new Date(date));

                messageList.add(message2);

                c.moveToNext();

            }while( !c.isAfterLast());
            adapter.notifyDataSetChanged();

        }
        c.close();

    }

    private DBMessage getDBMessage(String id){
        Cursor cc;

        cc = MainActivity.database.query("sms",new String[]{"id,category,latitude,longitude,altitude"},"id = '"+id+"'",null,null,null,null);

        cc.moveToFirst();
        if(cc.isAfterLast()){
            cc.close();
            return null;
        }

        String tid = cc.getString(0);
        int cat = cc.getInt(1);
        double lat = cc.getDouble(2);
        double log = cc.getDouble(3);
        double alt = cc.getDouble(4);
        cc.close();
        return new DBMessage(tid,cat,lat,log,alt);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        isItOpened = false;
    }


    public void onPause(){
        super.onPause();
        isItOpened = false;
    }

    public void onResume(){
        super.onResume();
        int temp = getLastMessageId();
        if( MainActivity.lastID != temp ){
            MainActivity.lastID = temp;
            Log.e("LAST ID ERROR","last id is not true");
        }

        isItOpened = true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isItOpened = false;
        if(isMessageListChanged ){
            isMessageListChanged = false;
            if(currentPosition != -1) {
                if(!MainActivity.blackList.contains(currentAddress)){
                    String lastMes = messageList.get(messageList.size()-1).getText();
                    Date lastDate = messageList.get(messageList.size()-1).getDate();

                    OneFragment.adapter.getList().get(currentPosition).setLastMessage(lastMes);
                    OneFragment.adapter.getList().get(currentPosition).setLastMessageDate(lastDate);

                    movePersonAtPosToTheTop(OneFragment.adapter.getList(),currentPosition);

                    OneFragment.adapter.notifyDataSetChanged();
                }else{
                    String lastMes = messageList.get(messageList.size()-1).getText();
                    Date lastDate = messageList.get(messageList.size()-1).getDate();

                    TwoFragment.adapter.getList().get(currentPosition).setLastMessage(lastMes);
                    TwoFragment.adapter.getList().get(currentPosition).setLastMessageDate(lastDate);

                    movePersonAtPosToTheTop(TwoFragment.adapter.getList(),currentPosition);

                    TwoFragment.adapter.notifyDataSetChanged();
                }

            }
        }

    }


    boolean isMessageListChanged = false;

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);

            MainActivity.lastID++;
            String tid = String.valueOf(MainActivity.lastID);


            Location location ;

            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission( android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context,"No location permission",Toast.LENGTH_SHORT).show();
                location = null;
            }else{
                location=getLastKnownLocation();
            }
            DBMessage mes;
            double latitude,longitude,altitude;
            if(location != null){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                altitude = location.getAltitude();
                mes = new DBMessage(tid,DBMessage.PERSONAL,latitude,longitude,altitude);
                mes.setText(msg);
            }else{
                Toast.makeText(context,"No Location provider found!",Toast.LENGTH_SHORT).show();
                mes = new DBMessage(tid,DBMessage.PERSONAL,-1,-1,-1);
                mes.setText(msg);
            }

            pushMessageToDB(mes,tid);

            //Message message = getMessageFromDB(tid);
            Message message = new Message(msg,2,new Date(Calendar.getInstance().getTimeInMillis()));
            message.setId(tid);
            messageList.add(message);
            adapter.notifyItemInserted(adapter.getItemCount());
            scrollToBottom();

            isMessageListChanged = true;

        } catch (Exception ex) {

            ex.printStackTrace();
        }


    }

    private Message getMessageFromDB(String id){
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = context.getContentResolver();
        Cursor c=null;
        try{

            do{
                c = cr.query(message, new String[]{"_id","address","body","type","date"}, "_id='"+id+"'", null, "date desc");
            }while(c.getCount() != 1);
            c.moveToFirst();
            String addr = c.getString(1);
            String txt = c.getString(2);
            int type = c.getInt(3);
            long dateL = c.getLong(4);
            Date date = new Date(dateL);
            Message newMessage = new Message(id,addr,txt,type,date);
            c.close();
            return  newMessage;
        }catch (Exception ex){
            if(c != null)
                c.close();
            Log.e("getMessageFromDB:",ex.getMessage());
            ex.printStackTrace();
            return null;

        }

    }

    private Location getLastKnownLocation() {
        myLocManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);

        List<String> providers = myLocManager.getProviders(true);
        Location bestLocation = null;

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission( android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    int getLastMessageId(){
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = context.getContentResolver();

        Cursor c = cr.query(message, new String[]{"max(_id)"},
                null, null, "date desc");

        String id;
        if (c.moveToFirst()) {
            id = c.getString(0);
            c.close();
            return Integer.valueOf(id);
        }

        c.close();
        return -1;
    }

    void pushMessageToDB(DBMessage mes,String tid){
        StringBuilder values = new StringBuilder(150);

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
        values.append( mes.getText() );
        values.append("');");
        String query = "INSERT INTO sms (id,category,latitude,longitude,altitude,text) VALUES "+values.toString();
        MainActivity.database.execSQL(query);
    }


    void movePersonAtPosToTheTop(List<Person> list , int pos){
        Person p = list.get(pos).clone();

        for(int i = pos ; i > 0 ; i--){
            list.set(i,list.get(i-1));
        }
        list.set(0,p);
    }

    void scrollToBottom(){
        recyclerView.scrollToPosition(adapter.getItemCount()-1);
    }

}
