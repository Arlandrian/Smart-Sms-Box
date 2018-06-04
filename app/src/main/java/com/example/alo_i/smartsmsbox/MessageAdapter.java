package com.example.alo_i.smartsmsbox;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private static final int CHAT_END = 1;
    private static final int CHAT_START = 2;

    Context context;

    private List<Message> mDataSet;
    public List<Message> getlist(){
        return mDataSet;
    }
    private String mId;
    MessageAdapter (List<Message> dataSet, String id, Context context) {
        mDataSet = dataSet;
        mId = id;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;

        if (viewType == CHAT_END) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_chat_end, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_chat_start, parent, false);
        }


        return new MyViewHolder(v);
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataSet.get(position).getIsItMe() != CHAT_START) {
            return CHAT_START;
        }else{
            return CHAT_END;
        }
    }
    final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Message message = mDataSet.get(position);
        holder.mTextView.setText(message.getText());
        holder.mTextViewDate.setText(dateFormat.format(message.getDate()));

        holder.itemView.setLongClickable(true);
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                if(isLongClick){

                    String id = mDataSet.get(position).getId();
                    DBMessage mes = getDBMessage(id);
                    if(mes == null){
                        Log.e("Longclick","mes null");
                        Toast.makeText(context,"No location data",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(-1 == mes.getAltitude() || -1 == mes.longitude){
                        Log.d("No location info found",mDataSet.get(position).toString());
                        return;
                    }

                    String latitude = String.valueOf(mes.getLatitude());
                    String longitude = String.valueOf(mes.getLongitude());


                    String label = "Message";
                    String uriBegin = "geo:" + latitude + "," + longitude;
                    String query = latitude + "," + longitude + "(" + label + ")";
                    String encodedQuery = Uri.encode(query);
                    String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                    Uri uri = Uri.parse(uriString);
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);


                }else{
                    String id = mDataSet.get(position).getId();
                    DBMessage mes = getDBMessage(id);
                    if(mes == null){
                        Log.e("Longclick","mes null");
                        return;
                    }
                    Toast.makeText(context,mes.categoryToString(),Toast.LENGTH_SHORT).show();
                    String txt = mes.getText();
                    if(txt != null)
                        Log.d("Message Click",mes.getText());

                }
            }
        });
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

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener ,View.OnLongClickListener{
        TextView mTextView,mTextViewDate;
        ItemClickListener itemClickListener;
        MyViewHolder(View v) {
            super(v);
            mTextView = (TextView) itemView.findViewById(R.id.tvMessage);
            mTextViewDate = (TextView) itemView.findViewById(R.id.tvMessageDate);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener){
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onClick(view,getAdapterPosition(),false);
        }

        @Override
        public boolean onLongClick(View view) {
            itemClickListener.onClick(view,getAdapterPosition(),true);
            return true;
        }


    }

}
