package com.smacon.fish2marine.AdapterClass;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.smacon.f2mlibrary.Button.LiquidRadioButton.LiquidRadioButton;
import com.smacon.f2mlibrary.CustomToast;
import com.smacon.f2mlibrary.Progress.AVLoadingIndicatorView;
import com.smacon.fish2marine.Fragment.HomeFragment;
import com.smacon.fish2marine.HelperClass.ProductListItem;
import com.smacon.fish2marine.HelperClass.SqliteHelper;
import com.smacon.fish2marine.Interface.RecycleviewInterface;
import com.smacon.fish2marine.ProductDescriptionActivity;
import com.smacon.fish2marine.R;
import com.smacon.fish2marine.Util.Config;
import com.smacon.fish2marine.Util.HttpOperations;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Aiswarya on 2017/04/19.
 */

public class NewProductSnapperAdapter extends RecyclerView.Adapter<NewProductSnapperAdapter.ViewHolder>  {
    private SharedPreferences sPreferences;
    private SqliteHelper helper;
    private Config mConfig;
    private Activity mContext;
    private ArrayList<ProductListItem> mListItem;
    private ProductListItem item;
    HomeFragment.UpdateListner updateListner;
    ListView mlistview;
    private String Customer_ID;
    AVLoadingIndicatorView indicator;
    ArrayList<ProductListItem> cuttype_dataItem;
    private static RecycleviewInterface itemListener;
    HomeFragment fragment;

    int clickedPos = -1;



    public NewProductSnapperAdapter(Activity context, ArrayList<ProductListItem> listitem, String CustomerID, HomeFragment fragment, HomeFragment.UpdateListner updateListner) {
        this.mContext = context;
        this.mListItem = listitem;
        this.Customer_ID=CustomerID;
        this.fragment=fragment;
        this.updateListner=updateListner;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.homeproductview_itemlayout, viewGroup, false);
        helper = new SqliteHelper(mContext, "Fish2Marine", null, 5);
        mConfig = new Config(mContext);
        sPreferences = mContext.getSharedPreferences("Fish2Marine", MODE_PRIVATE);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mListItem.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView ProductName,OtherName, ProductPrice,SpecialPrice, Quantity,txt_ordered_qty,txt_cleaned_qty,text;
        private ImageView ProductImage, Plus, Minus;
        private LinearLayout Addtocart,layout;
        FrameLayout layout_indicator;
        AVLoadingIndicatorView indicator;
        RadioGroup group;
        ImageView close;
        Button btn_ok;
        LinearLayout layout_cutype;
        String mCuttypeValue = "";
        String OrderQty,AfterQty;

        ViewHolder(final View itemView) {
            super(itemView);
            this.layout = itemView.findViewById(R.id.layout);
            this.layout_cutype = itemView.findViewById(R.id.layout_cutype);
            this.close = itemView.findViewById(R.id.close);
            this.btn_ok = itemView.findViewById(R.id.btn_ok);
            this.layout_indicator = itemView.findViewById(R.id.layout_indicator);
            this.indicator = itemView.findViewById(R.id.indicator);
            this.ProductName = itemView.findViewById(R.id.txt_product_name);
            this.ProductPrice = itemView.findViewById(R.id.txt_product_price);
            this.OtherName = itemView.findViewById(R.id.txt_product_othername);
            this.SpecialPrice = itemView.findViewById(R.id.txt_product_specialprice);
            this.txt_ordered_qty = itemView.findViewById(R.id.txt_ordered_qty);
            this.txt_cleaned_qty = itemView.findViewById(R.id.txt_cleaned_qty);
            this.ProductImage = itemView.findViewById(R.id.img_product);
            this.Plus = itemView.findViewById(R.id.img_plus);
            this.Minus = itemView.findViewById(R.id.img_minus);
            this.Quantity = itemView.findViewById(R.id.txt_quantity);
            this.Addtocart = itemView.findViewById(R.id.btn_addtocart);
            this.group= itemView.findViewById(R.id.radiogroup);
        }
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        item = mListItem.get(position);
        holder.ProductName.setText(item.getnewproduct_name());
        holder.OtherName.setText(item.getnewproduct_Othername());
        holder.ProductPrice.setText(item.getnewproduct_price()+"/0.5Kg");

        holder.OrderQty=mListItem.get(position).getneworder_qty();
        holder.AfterQty=mListItem.get(position).getnewcleaned_qty();

        holder.txt_ordered_qty.setText("Order Qty: "+holder.OrderQty+"gm");
        holder.txt_cleaned_qty.setText("After Cleaning: "+holder.AfterQty+"gm");

        holder.group.removeAllViews();
        LayoutInflater inflater = mContext.getLayoutInflater();
        if (mListItem.get(position).getnewcuttype_applicable().equals("1")) {
           // Log.d("111111111","here"+ mListItem.get(position).getCuttype_valuelist().size());
            for (int i = 0; i < mListItem.get(position).getCuttype_valuelist().size(); i++) {
               // Log.d("11111111","hiiinew"+mListItem.get(position).getCuttype_valuelist().get(i));
                LiquidRadioButton rb = (LiquidRadioButton) inflater.inflate(R.layout.template_radiobutton, null);
              //  LiquidRadioButton rb = new LiquidRadioButton(mContext);
                rb.setPadding(5,5,5,5);
                rb.setText(mListItem.get(position).getCuttype_valuelist().get(i));
                holder.group.addView(rb);
            }
        }
        holder.group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        holder.mCuttypeValue=btn.getText().toString();
                       // Toast.makeText(mContext, btn.getText().toString()+""+holder.ProductName.getText(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        if (item.getnewproduct_specialprice().equals("")||item.getnewproduct_specialprice().equals("0")){
            holder.SpecialPrice.setVisibility(View.GONE);
        }
        else {
            holder.ProductPrice.setPaintFlags(
                    holder.ProductPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.SpecialPrice.setText(item.getnewproduct_specialprice()+"/0.5Kg");
        }


        if(!item.getnewproduct_image().equals("")){



            try {
                Picasso.get()
                        .load(mListItem.get(position).getnewproduct_image().replace("https",
                                "http").replaceAll(" ","%20"))
                        .placeholder(R.drawable.ic_dummy)
                        .error(R.drawable.ic_dummy)
                        .into(holder.ProductImage);
            }catch (Exception e){
            }
        } else {
            holder.ProductImage.setBackgroundResource(R.drawable.ic_dummy);
        }

        holder.ProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // fragment.userItemClick(position);
                //CustomToast.error(v.getContext(), "This is: "+mListItem.get(position).getnewproduct_name()).show();
                Intent intent = new Intent(mContext, ProductDescriptionActivity.class);
                intent.putExtra("ID",mListItem.get(position).getnewproduct_id());
                intent.putExtra("NAME",mListItem.get(position).getnewproduct_name());
                mContext.startActivity(intent);
            }
        });

        holder.Plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number = Integer.parseInt(holder.Quantity.getText().toString()) + 1;
                holder.Quantity.setText(String.valueOf(number));
                Double orderqty=Double.parseDouble(holder.OrderQty)*Double.parseDouble(holder.Quantity.getText().toString());
                if(orderqty>=1000){
                    Double value=orderqty/1000;
                    holder.txt_ordered_qty.setText("Order Qty: "+value+"kg");
                }
                else {
                    holder.txt_ordered_qty.setText("Order Qty: "+orderqty+"gm");
                }

                Double afterqty=Double.parseDouble(holder.AfterQty)*Double.parseDouble(holder.Quantity.getText().toString());
              //  if(afterqty<=500){
               //     holder.txt_cleaned_qty.setText("After Cleaning: "+mListItem.get(position).getnewcleaned_qty()+"gm");
              //  }
                if(afterqty>=1000){
                    Double value=afterqty/1000;
                    holder.txt_cleaned_qty.setText("After Cleaning: "+value+"kg");
                }
                else {
                    holder.txt_cleaned_qty.setText("After Cleaning: "+afterqty+"gm");
                }

            }
        });

        holder.Minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Integer.parseInt(holder.Quantity.getText().toString()) >= 2) {
                    int number = Integer.parseInt(holder.Quantity.getText().toString()) - 1;
                    holder.Quantity.setText(String.valueOf(number));
                }
                Double orderqty=Double.parseDouble(holder.OrderQty)*Double.parseDouble(holder.Quantity.getText().toString());
                if(orderqty>=1000){
                    Double value=orderqty/1000;
                    holder.txt_ordered_qty.setText("Order Qty: "+value+"kg");
                }
                else {
                    holder.txt_ordered_qty.setText("Order Qty: "+orderqty+"gm");
                }

                Double afterqty=Double.parseDouble(holder.AfterQty)*Double.parseDouble(holder.Quantity.getText().toString());
                //  if(afterqty<=500){
                //     holder.txt_cleaned_qty.setText("After Cleaning: "+mListItem.get(position).getnewcleaned_qty()+"gm");
                //  }
                if(afterqty>=1000){
                    Double value=afterqty/1000;
                    holder.txt_cleaned_qty.setText("After Cleaning: "+value+"kg");
                }
                else {
                    holder.txt_cleaned_qty.setText("After Cleaning: "+afterqty+"gm");
                }

            }
        });

        holder.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.layout.setVisibility(View.VISIBLE);
                holder.layout_cutype.setVisibility(View.GONE);
            }
        });

        holder.btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.mCuttypeValue.equals("")){
                    CustomToast.error(mContext,"Please choose a cut type").show();
                }
                else {
                    holder.layout.setVisibility(View.VISIBLE);
                    holder.layout_cutype.setVisibility(View.GONE);
                   // Toast.makeText(mContext, "hiii", Toast.LENGTH_SHORT).show();
                  //  Toast.makeText(mContext, "quantity "+holder.Quantity.getText().toString(), Toast.LENGTH_SHORT).show();
                    AddToCart(holder.Quantity.getText().toString(),mListItem.get(position).getnewproduct_id(),holder.mCuttypeValue,holder);
                }
            }
        });

        holder.Addtocart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Toast.makeText(mContext, "hihihihi", Toast.LENGTH_SHORT).show();
               // Log.d("111111111","Cart centerID "+sPreferences.getString("DeliveryCenter_ID",""));
                //Log.d("111111111","cuttypeapplicable "+mListItem.get(position).getnewcuttype_applicable());

                try
                {
                    if(sPreferences.getString("DeliveryCenter_ID","").equals("")){
                        CustomToast.error(v.getContext(), "Please Choose ur location"+sPreferences.getString("DeliveryCenter_ID","")).show();
                    }
                    else {
                        //   Toast.makeText(mContext, "hiii "+mListItem.get(position).getnewcuttype_applicable().equals("1"), Toast.LENGTH_SHORT).show();

                        if(mListItem.get(position).getnewcuttype_applicable().equals("1")){
                            holder.layout.setVisibility(View.INVISIBLE);
                            holder.layout_cutype.setVisibility(View.VISIBLE);
                      /*  //CutypeDialog();
                        CutypeDialog(mListItem.get(position).getnewproduct_id(),holder.Quantity.getText().toString(),holder);
                        CutType(mListItem.get(position).getnewproduct_id(),holder.Quantity.getText().toString());
                    */
                        }
                        else {
                            AddToCart(holder.Quantity.getText().toString(),mListItem.get(position).getnewproduct_id(),"",holder);
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.d("11111111","Exception"+e.getMessage());
                }

            }
        });
    }
    private void AddToCart(String Quantity,String ProductID,String Cuttype,ViewHolder holder){
        Config mConfig = new Config(mContext);
        if(mConfig.isOnline(mContext)){
            LoadAddToCartInitiate mLoadAddToCartInitiate = new LoadAddToCartInitiate(
                    Customer_ID,ProductID,Cuttype,Quantity,holder);

            mLoadAddToCartInitiate.execute((Void) null);
        }else {
            CustomToast.error(mContext,"No Internet Connection.").show();
        }
    }

    public class LoadAddToCartInitiate extends AsyncTask<Void, StringBuilder, StringBuilder> {

        private String mCustomerID,mProductID,mCutype,mQty;
        ViewHolder mholder;


        LoadAddToCartInitiate(String Customer_ID, String Product_ID, String CutType,String Qty,ViewHolder holder) {
            mCustomerID = Customer_ID;
            mProductID = Product_ID;
            mCutype = CutType;
            mQty = Qty;
            mholder=holder;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mholder.layout.setVisibility(View.INVISIBLE);
            mholder.layout_indicator.setVisibility(View.VISIBLE);
            helper.Delete_cartcount();
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {

            HttpOperations httpOperations = new HttpOperations(mContext);
            Log.d("111111", "PASSING VALUE " + mCustomerID+" "+mProductID+" "+mCutype+" "+mQty);
            StringBuilder result = httpOperations.doAddToCart(mCustomerID, mProductID, mCutype,mQty);
            Log.d("111111111",result.toString());
            return result;

        }

        @Override
        protected void onPostExecute(StringBuilder result) {
            super.onPostExecute(result);

            try {
                Log.d("111","//"+result.toString());
                JSONObject jsonObj = new JSONObject(result.toString());
                if (jsonObj.has("status")) {
                    if (jsonObj.getString("status").equals(String.valueOf(2))) {
                        mholder.layout.setVisibility(View.VISIBLE);
                        mholder.layout_indicator.setVisibility(View.GONE);
                        CustomToast.info(mContext, jsonObj.getString("message")).show();
                    }else if (jsonObj.getString("status").equals(String.valueOf(1))) {
                        Log.e("data/////////////","//////////////20"+jsonObj.toString());
                      //  Toast.makeText(mContext, "hiiiii "+jsonObj.getJSONObject("data").toString(), Toast.LENGTH_SHORT).show();

                        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
                        JSONObject jsonObj1 = jsonObj.getJSONObject("data");
                       /// Toast.makeText(mContext, "cart id ****"+jsonObj1.getString("id").trim(), Toast.LENGTH_SHORT).show();
                        for (int i = 0; i < jsonObj1.length(); i++) {
                            HashMap<String, String> map;
                            map = new HashMap<String, String>();
                            map.put("itemsCount", jsonObj1.getString("itemsCount").trim());
                            fillMaps.add(map);
                        }
                        mConfig.savePreferences(mContext,"CartID",jsonObj1.getString("id").trim());
                        Log.d("111111111",sPreferences.getString("CartID",""));
                        helper.Insert_Count(fillMaps);
                        final List<HashMap<String, String>> Data_Item;
                        Data_Item = helper.getCount();
                        Log.d("1111111112","Cart Count "+Data_Item.get(0).get("cartcount"));
                        mConfig.savePreferences(mContext,"CartCount",Data_Item.get(0).get("cartcount"));
                     //   Toast.makeText(mContext, "cart count in adapter "+jsonObj1.getInt("itemsCount"), Toast.LENGTH_SHORT).show();
                        updateListner.onClick(jsonObj1.getInt("itemsCount"));
                        updatecartcount(mholder);
                        Log.d("111111111",sPreferences.getString("CartCount",""));

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("carterror1","cart"+e.getMessage());
                CustomToast.info(mContext,"Please Try Again").show();

            } catch (NullPointerException e) {
                CustomToast.error(mContext,"No Internet Connection.").show();
            } catch (Exception e) {
                Log.e("carterror2","cart"+e.getMessage());
                CustomToast.info(mContext,"Please Try Again").show();
            }
        }
    }
    private void updatecartcount(ViewHolder holder){
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("data_changed"));

        holder.layout.setVisibility(View.VISIBLE);
        holder.layout_indicator.setVisibility(View.GONE);
        CustomToast.success(mContext, "Item successfully added to cart").show();
    }
}
