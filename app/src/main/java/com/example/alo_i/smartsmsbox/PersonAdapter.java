package com.example.alo_i.smartsmsbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.MyViewHolder>{
    private final SimpleDateFormat sm = new SimpleDateFormat("dd/MM/yyyy");
    private List<Person> personList;
    private Context context;
    private int  selectedCategory = -1;

    AlertDialog.Builder alertDialog;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{

        private TextView name;
        private TextView lastMessage;
        private TextView date;

        public MyViewHolder(View itemView) {
            super(itemView);

            name = (TextView)itemView.findViewById(R.id.m_name);
            lastMessage = (TextView)itemView.findViewById(R.id.m_lastmessage);
            date = (TextView)itemView.findViewById(R.id.m_date);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        ItemClickListener itemClickListener;
        public void setItemClickListener(ItemClickListener itemClickListener){
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v,getAdapterPosition(),false);

        }

        @Override
        public boolean onLongClick(View v) {
            itemClickListener.onClick(v,getAdapterPosition(),true);

            return false;
        }
    }

    public PersonAdapter(List<Person> personList, Context context) {
        this.personList = personList;
        this.context = context;
    }
    public PersonAdapter(Context context) {
        this.personList = new ArrayList<>();
        this.context = context;
    }

    public int listContainsAddress(String address){

        for(int i = 0 ; i < personList.size() ; i++){
            if(personList.get(i).getNumber().equals(address)){
                return i;
            }
        }
        return -1;

    }

    public List<Person> getList() {
        return personList;
    }

    public void setList(List<Person> personList) {
        this.personList = personList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_row,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Person person = personList.get(position);

        holder.itemView.setLongClickable(true);

        holder.name.setText(person.getName());
        holder.lastMessage.setText(person.getLastMessage());

        holder.date.setText(sm.format(person.getLastMessageDate()));

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, final int position, boolean isLongClick) {
                if(isLongClick){
                    if(MainActivity.selectedTab == 0){
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        addToBlackList(OneFragment.adapter.getList().get(position).getNumber(),MainActivity.blackList);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Mark as SPAM").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();

                    }else{
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        removeFromBlackList(TwoFragment.adapter.getList().get(position).getNumber(),MainActivity.blackList,position);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Mark as NOT SPAM").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();

                    }

                }else{

                    String personAddress = personList.get(position).getNumber();
                    String personName = personList.get(position).getName();
                    Intent intent =  new Intent(context,ChatActivity.class);
                    intent.putExtra("address",personAddress);
                    intent.putExtra("name",personName);
                    intent.putExtra("pos",position);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });

    }

    public void addToBlackList(String address, List<String> blacklist){
        //blacklist e bak bu adress içinde bulunmuyosa ekle adapter a notify et
        if(blacklist.contains(address)){
            Toast.makeText(context,address+" already exist!",Toast.LENGTH_SHORT).show();
            return;
        }else{
            blacklist.add(address);
            int pos = OneFragment.adapter.listContainsAddress(address);
            if(pos == -1){
                Person newPerson = new Person(address,"",new Date(456));

                this.getList().add(newPerson);
                this.notifyItemInserted(this.getItemCount());
            }else{

                Person newPerson = OneFragment.adapter.getList().get(pos);

                OneFragment.adapter.getList().remove(pos);
                OneFragment.adapter.notifyItemRemoved(pos);

                TwoFragment.adapter.getList().add(newPerson);
                TwoFragment.adapter.notifyItemInserted(this.getItemCount());

            }

        }
    }

    public void removeFromBlackList(String address, List<String> blacklist,int pos){
        if(!blacklist.contains(address)){
            Toast.makeText(context,address+" could not be found!",Toast.LENGTH_SHORT).show();
            this.getList().remove(pos);
            this.notifyItemRemoved(pos);
            return;
        }else{
            blacklist.remove(address);

            Person newPerson = this.getList().get(pos).clone();

            this.getList().remove(pos);
            this.notifyItemRemoved(pos);

            OneFragment.adapter.getList().add(newPerson);
            OneFragment.adapter.notifyItemInserted(OneFragment.adapter.getItemCount());
        }
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }
}
