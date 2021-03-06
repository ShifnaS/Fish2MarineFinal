package com.smacon.fish2marine.AdapterClass;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smacon.fish2marine.HelperClass.AllListItem;
import com.smacon.fish2marine.OrderDetailsActivity;
import com.smacon.fish2marine.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Aiswarya on 10/05/2018.
 */
public class MyOrdersAdapter extends RecyclerView.Adapter {

    private List<AllListItem> mitems;
    public Activity mContext;

    public MyOrdersAdapter(Activity mContext, List<AllListItem> items) {
        this.mitems = items;
        this.mContext = mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View rootView = LayoutInflater.
                from(mContext).inflate(R.layout.list_item_order, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rootView.setLayoutParams(lp);
        return new MyViewHolder(rootView);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {

        final AllListItem dataItem = mitems.get(position);
        MyViewHolder myViewHolder = (MyViewHolder) viewHolder;
        myViewHolder.txt_order_name.setText(dataItem.getCreated());
        myViewHolder.txt_order_id.setText("Order No: "+dataItem.getReward_id());
        String date=dataItem.getComment();
        Log.e("Date",""+date);
        String d[]=date.split("-");
        String newDate=d[2]+"-"+d[1]+"-"+d[0];
        myViewHolder.txt_order_date.setText(""+newDate);

        if(dataItem.getPoints().equals("Pending")){
            myViewHolder.txt_order_status.setTextColor(ContextCompat.getColor(mContext, R.color.color2));
           // myViewHolder.txt_order_status.setTextColor(Color.parseColor("#ff8f00"));
        }
        else  if(dataItem.getPoints().equals("Processing")){
            myViewHolder.txt_order_status.setTextColor(ContextCompat.getColor(mContext, R.color.color4));
            //myViewHolder.txt_order_status.setTextColor(R.color.color4);
        }
        else  if(dataItem.getPoints().equals("Completed")||dataItem.getPoints().equals("Delivered")){
            myViewHolder.txt_order_status.setTextColor(ContextCompat.getColor(mContext, R.color.green));
        }
        else  if(dataItem.getPoints().equals("Rejected")){
            myViewHolder.txt_order_status.setTextColor(ContextCompat.getColor(mContext, R.color.red));
        }
        myViewHolder.txt_order_status.setText(dataItem.getPoints());
        myViewHolder.txt_order_amount.setText("\u20B9 "+dataItem.getExpiry());

        myViewHolder.orderitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CustomToast.success(mContext, "This is: "+mitems.get(position).getReward_id()).show();
                Intent intent = new Intent(mContext,OrderDetailsActivity.class);
                intent.putExtra("ORDER_ID",mitems.get(position).getReward_id());
                mContext.startActivity(intent);
                mContext.overridePendingTransition(R.anim.bottom_up,
                        android.R.anim.fade_out);
                mContext.finish();
            }
        });


    }

    @Override
    public int getItemCount() {

        return mitems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView txt_order_id,txt_order_date,txt_order_status,txt_order_amount,txt_order_name;
        public CardView orderitem;

        public MyViewHolder(View itemView) {
            super(itemView);

            //cardview = (CardView) itemView.findViewById(R.id.cardview);
            txt_order_name = itemView.findViewById(R.id.txt_order_name);

            txt_order_id = itemView.findViewById(R.id.txt_order_id);
            txt_order_date = itemView.findViewById(R.id.txt_order_date);
            txt_order_status = itemView.findViewById(R.id.txt_order_status);
            txt_order_amount = itemView.findViewById(R.id.txt_order_amount);
            orderitem = itemView.findViewById(R.id.orderitem);

        }
    }
}