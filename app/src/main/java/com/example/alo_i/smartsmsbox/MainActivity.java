package com.example.alo_i.smartsmsbox;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity {

    private int[] tabIcons = {
            R.drawable.ic_action_name,R.drawable.ic_action_blacklist
    };

    private ViewPager viewPager;

    private TabLayout tabLayout;

    ImageButton popUpButton;

    public static int lastID;
    public static int selectedTab;

    public static Hashtable<String,Message> messageDictionary ;

    public static final String[] otp_key_list = new String[]{"one time","password","tek sefer","şifre","veritification","code","tek","onay"};


    //region Adapters
    public PersonAdapter personAdapter;
    public void setPersonAdapter(PersonAdapter personAdapter){
        this.personAdapter = personAdapter;
    }

    public PersonAdapter getPersonAdapter(){
        return personAdapter;
    }

    PersonAdapter blacklistAdapter;
    public void setBlacklistAdapter(PersonAdapter blacklistAdapter){
        this.blacklistAdapter = blacklistAdapter;
    }

    public PersonAdapter getBlackListAdapter(){
        return blacklistAdapter;
    }
    //endregion

    final String [] permissions = new String[]{
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};


    Context context;
    public static SQLiteDatabase database;
    public final String databaseName = "smartsmsboxdb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deleteFile(messagesFilename);
        database = this.openOrCreateDatabase(databaseName, MODE_PRIVATE, null);

        //database.execSQL("DROP TABLE sms");

        database.execSQL("CREATE TABLE IF NOT EXISTS "
                + "sms"
                + " (id VARCHAR Primary KEY,category int,latitude REAL, longitude REAL,altitude REAL,text VARCHAR);");


        context = getApplicationContext();
        //messageDictionary = new Hashtable<>();
        blackList = new ArrayList<String>() ;

        if(checkFile(context, blacklistFilename)){
            getBlackListFromFile();

        }else{

            saveBlackList();
        }
        //controls if there are messages received or sent in phone sms db
        controlNewMessages();

        lastID = Integer.parseInt(getLastMessageId());

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        //region PopUp Menu Button
        popUpButton = (ImageButton)findViewById(R.id.popupButton);
        popUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this,popUpButton);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getTitle().toString()){
                            case "Clear Sms":
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Are you sure?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            case DialogInterface.BUTTON_POSITIVE:
                                                MainActivity.database.execSQL("DELETE FROM sms WHERE id > 0");
                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //No button clicked
                                                break;
                                        }
                                    }
                                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        }).show();
                                break;
                            case "Clear BlackList":

                                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                                builder2.setMessage("Are you sure?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            case DialogInterface.BUTTON_POSITIVE:
                                                MainActivity.blackList.clear();
                                                personAdapter.getList().clear();
                                                blacklistAdapter.getList().clear();
                                                getAllSmsForMainScreen();

                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //No button clicked
                                                break;
                                        }
                                    }
                                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        }).show();
                                break;
                            case "Categories":
                                showRadioButtonDialog();
                                break;
                            default:


                        }
                        return false;
                    }
                });
                popupMenu.show();

            }
        });
        //endregion

        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab=tabLayout.getSelectedTabPosition();

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        setupTabIcons();

        for(String permission : permissions){
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(permissions,2);

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
        }

    }

    //region Control New Messages For Initializiation
    private void controlNewMessages(){
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(message, new String[]{"address","body","date","person","_id"}, null, null,null);//address,body,date,person,_id

        int onPhone=0;
        if (c.moveToFirst()) {
            onPhone=c.getCount();
        }
        c.close();

        c = MainActivity.database.query("sms",new String[]{"id,category,latitude,longitude,altitude,text"},null,null,null,null,null);
        int onDB=0 ;

        if (c.moveToFirst()) {
            onDB=c.getCount();
        }
        c.close();
        if(onDB >= onPhone){
            c.close();
            return;
        }
        Cursor ph = cr.query(message, new String[]{"_id","body","address"}, null, null,null);
        ph.moveToFirst();

        while(!ph.isAfterLast()){


            String _id = ph.getString(0);

            if(isItOnMyDB(_id)){
                ph.moveToNext();
                continue;
            }


            String body = ph.getString(1);

            int category = categorizeSms(body,ph.getString(2));

            DBMessage mes = new DBMessage(_id,category,body);

            pushMessageToDB(mes,_id);

            ph.moveToNext();
        }

        ph.close();
    }

    boolean isItOnMyDB(String id){
        ContentResolver cr = getContentResolver();
        Cursor c = MainActivity.database.query("sms",new String[]{"id"},"id = '"+id+"'",null,null,null,null);

        boolean res = false;
        if( c.getCount() > 0 ){
            res = true;
        }
        c.close();
        return res;
    }

    int categorizeSms(String body,String address){

        int len = body.length();
        if(MainActivity.blackList.contains(address)){
            return DBMessage.SPAM;
        }

        if(len < 4){
            return DBMessage.PERSONAL;
        }
        String Bcode = body.substring(len-4,len);
        if( Bcode.charAt(0) == 'B' ){
            int code ;
            try{
                code = Integer.parseInt(Bcode.substring(1,4));
            }catch(Exception ex){
                code = 1200;
            }
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
        values.append( mes.getText().replace("'","''") );
        values.append("');");
        String query = "INSERT INTO sms (id,category,latitude,longitude,altitude,text) VALUES "+values.toString();
        MainActivity.database.execSQL(query);
    }
    //endregion

    private void showRadioButtonDialog() {

        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.radio_group_dialog);
        List<String> stringList=new ArrayList<>();  // here is list

        stringList.add("Personal");
        stringList.add("Commercial");
        stringList.add("Spam");
        stringList.add("OTP");
        stringList.add("All Messages");

        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);

        for(int i=0;i<stringList.size();i++){
            RadioButton rb=new RadioButton(this); // dynamically creating RadioButton and adding to RadioGroup.
            rb.setText(stringList.get(i));
            rg.addView(rb);
        }
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        Log.e("selected RadioButton->",btn.getText().toString());

                        personAdapter.getList().clear();
                        blacklistAdapter.getList().clear();
                        if(x == 4){
                            getAllSmsForMainScreen();
                        }else{
                            getAllSmsForMainScreenWithCategory( x);

                        }
                        dialog.cancel();
                        break;
                    }
                }
            }
        });
        dialog.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        MainActivity.lastID = Integer.valueOf(getLastMessageId());
        if(!database.isOpen())
            database= this.openOrCreateDatabase(databaseName, MODE_PRIVATE, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.lastID = Integer.valueOf(getLastMessageId());
        if(!database.isOpen())
            database= this.openOrCreateDatabase(databaseName, MODE_PRIVATE, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveBlackList();
        //saveMessagesFile();
        if(database.isOpen())
            database.close();
    }

    public static List<String> blackList;
    void getBlackListFromFile(){
        readBlackListFromFile();
    }

    //region Setup Fragments
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());


        personAdapter= new PersonAdapter(this);
        OneFragment.adapter = personAdapter;

        blacklistAdapter = new PersonAdapter(this);
        TwoFragment.adapter = blacklistAdapter;

        adapter.addFragment(new OneFragment(), "Messages");
        adapter.addFragment(new TwoFragment(), "Black Lıst");


        viewPager.setAdapter(adapter);

        getAllSmsForMainScreen();

    }

    private void setupTabIcons() {
        //tabLayout.getTabAt(0).get
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter{
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment,String Title){
            mFragmentList.add(fragment);
            mFragmentTitleList.add(Title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }
    //endregion
    public void getAllSmsForMainScreenWithCategory(int category){
        final Dictionary<String,Person> contacts = new Hashtable<>();

        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(message, new String[]{"address","body","date","person","_id"}, null, null,null);//address,body,date,person

        int i=0;
        if (c.moveToFirst()) {
            while(! c.isAfterLast()) {
                String id = c.getString(4);
                DBMessage db_message = getDBMessage(id);
                if(db_message == null || db_message.getCategory() != category){
                    c.moveToNext();
                    continue;
                }
                i++;
                String personAddress = c.getString(0);//address
                if(personAddress != null){
                    Person person = contacts.get(personAddress);
                    if(person == null){

                        String personName = getContactName(context,personAddress);//getPersonName(c,personAddress);

                        long lastDate =  c.getLong(2);

                        String lastMessage = c.getString(1);

                        Person newPerson = new Person(personName,lastMessage,new Date(lastDate));

                        newPerson.setNumber(personAddress);
                        contacts.put(personAddress,newPerson);

                        if(blackList.contains(personAddress)){
                            blacklistAdapter.getList().add(newPerson);
                        }else{
                            personAdapter.getList().add(newPerson);
                        }

                    }else{

                        long lastDate =  c.getLong(2);//date

                        if(lastDate > person.getLastMessageDate().getTime() ){
                            person.setLastMessageDate(new Date(lastDate));
                            String lastMessage = c.getString(1);
                            person.setLastMessage(lastMessage);
                        }
                    }
                }


                c.moveToNext();
            }
            i++;
            boolean flag ;
            for(String str : blackList){
                flag = false;
                for(Person prs : blacklistAdapter.getList()){

                    if( str.equals(prs.getNumber()) ){
                        flag = true;
                        break;
                    }

                }
                if(!flag){
                    blacklistAdapter.getList().add(new Person(str,"",new Date(456)));
                }
            }

            personAdapter.notifyDataSetChanged();
            blacklistAdapter.notifyDataSetChanged();

        }
        c.close();
    }

    private DBMessage getDBMessage(String id){
        Cursor cc;
        try{
            cc = MainActivity.database.query("sms",new String[]{"id,category,latitude,longitude,altitude,text"},"id = '"+id+"'",null,null,null,null);

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
            String text = cc.getString(5);
            cc.close();
            return new DBMessage(tid,lat,log,alt,cat,text);
        }catch (Exception ex){
            ex.printStackTrace();

            return null;
        }

    }


    public void getAllSmsForMainScreen() {
        final Dictionary<String,Person> contacts = new Hashtable<>();

        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(message, new String[]{"address","body","date","person"}, null, null,null);//address,body,date,person

        int i=0;
        if (c.moveToFirst()) {
            while(! c.isAfterLast()) {

                i++;
                String personAddress = c.getString(0);//address
                if(personAddress != null){
                    Person person = contacts.get(personAddress);
                    if(person == null){

                        String personName = getContactName(context,personAddress);//getPersonName(c,personAddress);

                        long lastDate =  c.getLong(2);

                        String lastMessage = c.getString(1);

                        Person newPerson = new Person(personName,lastMessage,new Date(lastDate));

                        newPerson.setNumber(personAddress);
                        contacts.put(personAddress,newPerson);

                        if(blackList.contains(personAddress)){
                            blacklistAdapter.getList().add(newPerson);
                        }else{
                            personAdapter.getList().add(newPerson);
                        }

                    }else{

                        long lastDate =  c.getLong(2);//date

                        if(lastDate > person.getLastMessageDate().getTime() ){
                            person.setLastMessageDate(new Date(lastDate));
                            String lastMessage = c.getString(1);
                            person.setLastMessage(lastMessage);
                        }
                    }
                }


                c.moveToNext();
            }
            i++;
            boolean flag ;
            for(String str : blackList){
                flag = false;
                for(Person prs : blacklistAdapter.getList()){

                    if( str.equals(prs.getNumber()) ){
                        flag = true;
                        break;
                    }

                }
                if(!flag){
                    blacklistAdapter.getList().add(new Person(str,"",new Date(456)));
                }
            }

            personAdapter.notifyDataSetChanged();
            blacklistAdapter.notifyDataSetChanged();

        }
        c.close();

    }

    public String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri,
                new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);
        if (cursor == null) {
            return phoneNumber;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }else{
            return phoneNumber;
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }

    //region file
    String blacklistFilename = "blacklist.dat";
    String messagesFilename = "messages.dat";

    void saveBlackList(){

        try{
            FileOutputStream f = openFileOutput(blacklistFilename,Context.MODE_PRIVATE);
            ObjectOutputStream o = new ObjectOutputStream(f);

            int size = MainActivity.blackList.size();
            o.writeInt(size);
            for (int i = 0; i < size ; i++) {
                String cat = MainActivity.blackList.get(i);
                o.writeObject(cat);
            }
            o.close();
            f.close();
        }catch (Exception e){
            e.printStackTrace();
            Log.e("FILE",e.getMessage());
        }
    }

    void readBlackListFromFile(){
        try{
            FileInputStream fi = openFileInput(blacklistFilename);
            ObjectInputStream oi = new ObjectInputStream(fi);

            int size = oi.readInt();
            if(size == MainActivity.blackList.size()){
                return;
            }
            for(int i = 0; i < size ; i++){
                String newC = (String)oi.readObject();
                MainActivity.blackList.add(newC);
            }
            oi.close();
            fi.close();
        }catch (Exception e){

            try {
                getApplicationContext().deleteFile(blacklistFilename);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }

    }

    void saveMessagesFile(){

        try{
            FileOutputStream f = openFileOutput(messagesFilename,Context.MODE_PRIVATE);
            ObjectOutputStream o = new ObjectOutputStream(f);

            int size = MainActivity.messageDictionary.size();
            o.writeInt(size);

            Enumeration e = MainActivity.messageDictionary.keys();
            while(e.hasMoreElements()){
                Message message = MainActivity.messageDictionary.get(e.nextElement());
                o.writeObject(message);
            }
            //LOCATION NOT SERIALIZABLE
            o.close();
            f.close();
        }catch (Exception e){
            e.printStackTrace();
            Log.e("Save Messages to File",e.getMessage());
        }
    }

    void readMessagesFromFile(){
        try{
            FileInputStream fi = openFileInput(messagesFilename);
            ObjectInputStream oi = new ObjectInputStream(fi);

            int size = oi.readInt();
            if(size == MainActivity.messageDictionary.size()){
                return;
            }
            for(int i = 0; i < size ; i++){
                Message newC = (Message) oi.readObject();
                MainActivity.messageDictionary.put(newC.getId(),newC);
            }

            oi.close();
            fi.close();
        }catch (Exception e){

            try {
                getApplicationContext().deleteFile(messagesFilename);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            Log.e("ReadMessagesFromFile",e.getMessage());
        }

    }

    public boolean checkFile(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }

    String getLastMessageId(){
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = context.getContentResolver();

        Cursor c = cr.query(message, new String[]{"max(_id)"},
                null, null, "date desc");

        String id;
        if (c.moveToFirst()) {
            id = c.getString(0);
            c.close();
            return id;
        }


        return null;
    }
    //endregion

}
