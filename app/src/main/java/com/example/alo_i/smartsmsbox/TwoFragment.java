package com.example.alo_i.smartsmsbox;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

import javax.microedition.khronos.egl.EGLDisplay;

public class TwoFragment extends Fragment {

    List<Person> blacklist;

    List<String> blakList;

    Context context;
    RecyclerView recyclerView;
    public static PersonAdapter adapter;

    FloatingActionButton addToBlacklistButton;
    EditText blacklistInput;

    public TwoFragment(){
        blakList = MainActivity.blackList;

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        context = getContext();


        View view = inflater.inflate(R.layout.fragment_two,container,false);

        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);

        addToBlacklistButton=view.findViewById(R.id.addBlackList);
        blacklistInput = (EditText)(view.findViewById(R.id.blacklistET));


        RecyclerView.LayoutManager lm = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(lm);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        addToBlacklistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(blacklistInput.getText().toString().isEmpty())
                    return;
                addToBlackList(blacklistInput.getText().toString(),MainActivity.blackList);

            }
        });

        return view;
    }

    void openChatActivity(String pickedNumber,String pickedName){
        Intent chatActivityIntent = new Intent(context,ChatActivity.class);
        String personAddress = pickedNumber;
        String personName = pickedName;

        chatActivityIntent.putExtra("address",personAddress);
        chatActivityIntent.putExtra("name",personName);
        chatActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(chatActivityIntent);
    }



    public void addToBlackList(String address, List<String> blacklist){
        //blacklist e bak bu adress içinde bulunmuyosa ekle adapter a notify et
        if(blacklist.contains(address.replace(" ",""))){
            Toast.makeText(context,address+" already exist!",Toast.LENGTH_SHORT).show();
            return;
        }else{
            blacklist.add(address);
            int pos = OneFragment.adapter.listContainsAddress(address);
            if(pos == -1){
                Person newPerson = new Person(address,"",new Date(456));
                adapter.getList().add(newPerson);
                adapter.notifyItemInserted(adapter.getItemCount());
            }else{

                Person newPerson = OneFragment.adapter.getList().get(pos);

                OneFragment.adapter.getList().remove(pos);
                OneFragment.adapter.notifyItemRemoved(pos);

                adapter.getList().add(newPerson);
                adapter.notifyItemInserted(adapter.getItemCount());
                blacklistInput.setText("");
            }

        }

    }

    public void getAllSmsForBlackListScreen() {
        final Dictionary<String,Person> contacts = new Hashtable<>();

        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(message, new String[]{"address","body","date","person"}, null, null,null);//address,body,date,person

        int i=0;
        if (c.moveToFirst()) {
            while(! c.isAfterLast()) {

                i++;
                String personAddress = c.getString(0);//address

                //control blacklist if its in add it to the list else dont

                if(personAddress != null){
                    Person person = contacts.get(personAddress);
                    if(person == null){

                        String personName = getContactName(context,personAddress);//getPersonName(c,personAddress);

                        long lastDate =  c.getLong(2);

                        String lastMessage = c.getString(1);

                        Person newPerson = new Person(personName,lastMessage,new Date(lastDate));

                        newPerson.setNumber(personAddress);
                        contacts.put(personAddress,newPerson);
                        blacklist.add(newPerson);

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

            adapter.notifyDataSetChanged();

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

}
