package com.example.alo_i.smartsmsbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.sql.Date;
import java.util.ArrayList;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class OneFragment extends Fragment {

    RecyclerView recyclerView;
    public static PersonAdapter adapter;
    //PersonAdapter blackListAdapter = ((MainActivity)getActivity()).getBlackListAdapter();
    Context context;

    FloatingActionButton newMessageButton;

    View view;

    List<Person> personList;
    public OneFragment() {


    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        view = inflater.inflate(R.layout.fragment_one, container, false);
        recyclerView = (RecyclerView)view.findViewById(R.id.m_messagesRecycler) ;

        context = getContext();


        RecyclerView.LayoutManager lm = new LinearLayoutManager(context);

        recyclerView.setLayoutManager(lm);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);


        newMessageButton = (FloatingActionButton) view.findViewById(R.id.newMessageButton);
        newMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readContact();

            }
        });
        //getAllSmsForMainScreen();
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
    String pickedNumber;
    String pickedName;
    final int PICK_CONTACT_REQUEST = 11;
    public void readContact() {
        try {

            Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts/people"));
            startActivityForResult(intent, PICK_CONTACT_REQUEST);

            onActivityResult(PICK_CONTACT_REQUEST,1,intent);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (PICK_CONTACT_REQUEST != requestCode || RESULT_OK != resultCode) return;

        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(data.getData(), null,
                null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                pickedName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                List<String> allNumbers = new ArrayList<String>();
                // Query phone here. Covered next
                Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
                while (phones.moveToNext()) {
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    allNumbers.add(phoneNumber);
                }
                phones.close();


                final CharSequence[] items = allNumbers.toArray(new String[allNumbers.size()]);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Choose a number");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String selectedNumber = items[item].toString();

                        selectedNumber = selectedNumber.replace("-", "");
                        selectedNumber = selectedNumber.replace("(","");
                        selectedNumber = selectedNumber.replace(")","");
                        selectedNumber = selectedNumber.replace(" ","");
                        pickedNumber = selectedNumber;
                        openChatActivity(pickedNumber,pickedName);

                        //pickedNumber = selectedNumber.replace("-", "");
                        //pickedNumber = selectedNumber.replace("-", "");

                    }
                });
                AlertDialog alert = builder.create();
                if(allNumbers.size() > 1) {
                    alert.show();

                } else if(allNumbers.size() > 0){
                    String selectedNumber = allNumbers.get(0);
                    selectedNumber = selectedNumber.replace("-", "");
                    selectedNumber = selectedNumber.replace("(","");
                    selectedNumber = selectedNumber.replace(")","");
                    selectedNumber = selectedNumber.replace(" ","");

                    pickedNumber =selectedNumber;
                    openChatActivity(pickedNumber,pickedName);

                } else if (allNumbers.size() == 0) {
                    Toast.makeText(context,"No numbers found",Toast.LENGTH_SHORT).show();
                    return;
                }


            }
        }
        cur.close();

    }




}