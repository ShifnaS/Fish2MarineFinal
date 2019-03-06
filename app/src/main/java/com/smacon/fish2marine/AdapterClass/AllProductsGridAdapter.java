package com.smacon.fish2marine.AdapterClass;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.smacon.f2mlibrary.Button.LiquidRadioButton.LiquidRadioButton;
import com.smacon.f2mlibrary.CustomToast;
import com.smacon.f2mlibrary.Progress.AVLoadingIndicatorView;
import com.smacon.fish2marine.HelperClass.ProductListItem;
import com.smacon.fish2marine.HelperClass.SqliteHelper;
import com.smacon.fish2marine.NavigationDrawerActivity;
import com.smacon.fish2marine.ProductDescriptionActivity;
import com.smacon.fish2marine.ProductViewActivity;
import com.smacon.fish2marine.R;
import com.smacon.fish2marine.Util.Config;
import com.smacon.fish2marine.Util.HttpOperations;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Kris on 12/21/2016.
 */
public class AllProductsGridAdapter extends RecyclerView.Adapter {
    private SharedPreferences sPreferences;
    private SqliteHelper helper;
    private Config mConfig;
    private List<ProductListItem> mitems;
    public Activity mContext;
    private String Customer_ID;
    /*private String Product_ID;
    String mCuttypeValue = "";
    ListView mlistview;
    AVLoadingIndicatorView indicator;
    ArrayList<ProductListItem> cuttype_dataItem;*/
    String Status;
    ProductViewActivity.UpdateListner updateListner;


    public AllProductsGridAdapter(Activity mContext, List<ProductListItem> items, String CustomerID, ProductViewActivity.UpdateListner updateListner) {
        this.mitems = items;
        this.mContext = mContext;
        this.Customer_ID=CustomerID;
        this.updateListner=updateListner;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View rootView = LayoutInflater.
                from(mContext).inflate(R.layout.allproductview_itemlayout, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rootView.setLayoutParams(lp);
        MyViewHolder dataObjectHolder = new MyViewHolder(rootView);
        helper = new SqliteHelper(mContext, "Fish2Marine", null, 5);
        mConfig = new Config(mContext);
        sPreferences = mContext.getSharedPreferences("Fish2Marine", MODE_PRIVATE);

        return new MyViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder,final int position) {
        final ProductListItem dataItem = mitems.get(position);
        final MyViewHolder myViewHolder = (MyViewHolder) viewHolder;
        myViewHolder.txt_product_name.setText(dataItem.getAllproduct_name());

        myViewHolder.OrderQty=mitems.get(position).getneworder_qty();
        myViewHolder.AfterQty=mitems.get(position).getnewcleaned_qty();

        myViewHolder.group.removeAllViews();
        LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
        if (mitems.get(position).getcuttype_applicable().equals("1")) {
            Log.d("111111111","size "+ mitems.get(position).getallCuttype_valuelist().size());
            for (int i = 0; i < mitems.get(position).getallCuttype_valuelist().size(); i++) {
                Log.d("11111111","value "+mitems.get(position).getallCuttype_valuelist().get(i));
                LiquidRadioButton rb = (LiquidRadioButton) inflater.inflate(R.layout.template_radiobutton, null);
                //  LiquidRadioButton rb = new LiquidRadioButton(mContext);
                rb.setPadding(5,5,5,5);
                rb.setText(mitems.get(position).getallCuttype_valuelist().get(i));
                myViewHolder.group.addView(rb);
            }
        }
        myViewHolder.group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        myViewHolder.mCuttypeValue=btn.getText().toString();
                     //   Toast.makeText(mContext, btn.getText().toString()+""+myViewHolder.txt_product_name.getText(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        myViewHolder.txt_OtherName.setText(dataItem.getAllproduct_Othername());
        myViewHolder.txt_ordered_qty.setText("Order Qty: "+dataItem.getallorder_qty());
        myViewHolder.txt_cleaned_qty.setText("After Cleaning: "+dataItem.getallcleaned_qty());
        myViewHolder.txt_product_price.setText(dataItem.getAllproduct_price()+"/0.5kg");
        if (dataItem.getAllproduct_specialprice().equals("")||dataItem.getAllproduct_specialprice().equals("0")){
            myViewHolder.txt_SpecialPrice.setVisibility(View.GONE);
        }
        else {
            myViewHolder.txt_product_price.setPaintFlags(
                    myViewHolder.txt_product_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            myViewHolder.txt_SpecialPrice.setText(dataItem.getAllproduct_specialprice()+"/0.5Kg");
        }
        //Product_ID=dataItem.getAllproduct_id();

        if(dataItem.getAllproduct_image().equals("false")||dataItem.getAllproduct_image().equals("")){

            myViewHolder.img_product.setBackgroundResource(R.drawable.ic_dummy);

        } else {
            try {
                Picasso.with(mContext)
                        .load(mitems.get(position).getAllproduct_image().replaceAll(" ","%20"))
                        .placeholder(R.drawable.ic_dummy)
                        .error(R.drawable.ic_dummy)
                        .into(myViewHolder.img_product);
            }catch (Exception e){
            }
        }

        //myViewHolder.img_product.setImageResource(R.drawable.ic_dummy);

        myViewHolder.img_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*CustomToast.success(mContext, "This is: "+dataItem.getAllproduct_name()
                        +" "+dataItem.getAllproduct_price()).show();*/
                Intent intent = new Intent(mContext,ProductDescriptionActivity.class);
                intent.putExtra("ID",dataItem.getAllproduct_id());
                intent.putExtra("NAME",dataItem.getAllproduct_name());
                mContext.startActivity(intent);
            }
        });
        myViewHolder.img_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number = Integer.parseInt(myViewHolder.txt_quantity.getText().toString()) + 1;
                myViewHolder.txt_quantity.setText(String.valueOf(number));
                Double orderqty=Double.parseDouble(myViewHolder.OrderQty)*Double.parseDouble(myViewHolder.txt_quantity.getText().toString());

                if(orderqty>=1000){
                    Double value=orderqty/1000;
                    myViewHolder.txt_ordered_qty.setText("Order Qty: "+value+"kg");
                }
                else {
                    myViewHolder.txt_ordered_qty.setText("Order Qty: "+orderqty+"gm");
                }

                Double afterqty=Double.parseDouble(myViewHolder.AfterQty)*Double.parseDouble(myViewHolder.txt_quantity.getText().toString());
                //  if(afterqty<=500){
                //     holder.txt_cleaned_qty.setText("After Cleaning: "+mListItem.get(position).getnewcleaned_qty()+"gm");
                //  }
                if(afterqty>=1000){
                    Double value=afterqty/1000;
                    myViewHolder.txt_cleaned_qty.setText("After Cleaning: "+value+"kg");
                }
                else {
                    myViewHolder.txt_cleaned_qty.setText("After Cleaning: "+afterqty+"gm");
                }



            }
        });

        myViewHolder.img_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Integer.parseInt(myViewHolder.txt_quantity.getText().toString()) >= 2  ){
                    int number = Integer.parseInt(myViewHolder.txt_quantity.getText().toString()) - 1;
                    myViewHolder.txt_quantity.setText(String.valueOf(number));

                }

              /*  if (Integer.parseInt(holder.Quantity.getText().toString()) >= 2) {
                    int number = Integer.parseInt(holder.Quantity.getText().toString()) - 1;
                    holder.Quantity.setText(String.valueOf(number));
                }*/
                Double orderqty=Double.parseDouble(myViewHolder.OrderQty)*Double.parseDouble(myViewHolder.txt_quantity.getText().toString());
                if(orderqty>=1000){
                    Double value=orderqty/1000;
                    myViewHolder.txt_ordered_qty.setText("Order Qty: "+value+"kg");
                }
                else {
                    myViewHolder.txt_ordered_qty.setText("Order Qty: "+orderqty+"gm");
                }

                Double afterqty=Double.parseDouble(myViewHolder.AfterQty)*Double.parseDouble(myViewHolder.txt_quantity.getText().toString());
                //  if(afterqty<=500){
                //     holder.txt_cleaned_qty.setText("After Cleaning: "+mListItem.get(position).getnewcleaned_qty()+"gm");
                //  }
                if(afterqty>=1000){
                    Double value=afterqty/1000;
                    myViewHolder.txt_cleaned_qty.setText("After Cleaning: "+value+"kg");
                }
                else {
                    myViewHolder.txt_cleaned_qty.setText("After Cleaning: "+afterqty+"gm");
                }



            }
        });
        myViewHolder.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myViewHolder.layout.setVisibility(View.VISIBLE);
                myViewHolder.layout_cutype.setVisibility(View.GONE);
            }
        });

        myViewHolder.btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myViewHolder.mCuttypeValue.equals("")){
                    CustomToast.error(mContext,"Please choose a cut type").show();
                }
                else {
                    myViewHolder.layout.setVisibility(View.VISIBLE);
                    myViewHolder.layout_cutype.setVisibility(View.GONE);
                    AddToCart(myViewHolder.txt_quantity.getText().toString(),mitems.get(position).getAllproduct_id(),myViewHolder.mCuttypeValue,myViewHolder);
                }
            }
        });
        myViewHolder.btn_addtocart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("111111111","Cart centerID "+sPreferences.getString("DeliveryCenter_ID",""));
                Log.d("111111111","cuttypeapplicable "+mitems.get(position).getcuttype_applicable());

                if(sPreferences.getString("DeliveryCenter_ID","").equals("")){
                    CustomToast.error(v.getContext(), "Please Choose ur location"+sPreferences.getString("DeliveryCenter_ID","")).show();
                }
                else {
                    if(mitems.get(position).getcuttype_applicable().equals("1")){
                        myViewHolder.layout_cutype.setVisibility(View.VISIBLE);
                        myViewHolder.layout.setVisibility(View.GONE);

                      /*  //CutypeDialog();
                        CutypeDialog(mListItem.get(position).getnewproduct_id(),holder.Quantity.getText().toString(),holder);
                        CutType(mListItem.get(position).getnewproduct_id(),holder.Quantity.getText().toString());
                    */
                    }
                    else {
                        AddToCart(myViewHolder.txt_quantity.getText().toString(),mitems.get(position).getAllproduct_id(),"",myViewHolder);
                    }
                }

            }
        });

    }
    @Override
    public int getItemCount() {

        return mitems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView txt_product_name,txt_OtherName,txt_SpecialPrice,txt_product_price,txt_ordered_qty,txt_cleaned_qty;
        final TextView txt_quantity;
        private ImageView img_product;
        private LinearLayout btn_addtocart,layout;
        final ImageView img_plus,img_minus;
        //CardView cardview;
        FrameLayout layout_indicator;
        AVLoadingIndicatorView indicator;
        RadioGroup group;
        ImageView close;
        TextView btn_ok;
        LinearLayout layout_cutype;
        String mCuttypeValue = "";
        String OrderQty,AfterQty;


        private MyViewHolder(View itemView) {
            super(itemView);

            layout = (LinearLayout) itemView.findViewById(R.id.layout);
            layout_cutype = (LinearLayout) itemView.findViewById(R.id.layout_cutype);
            close = (ImageView) itemView.findViewById(R.id.close);
            btn_ok = (TextView) itemView.findViewById(R.id.btn_ok);
            layout_indicator = (FrameLayout) itemView.findViewById(R.id.layout_indicator);
            indicator = (AVLoadingIndicatorView) itemView.findViewById(R.id.indicator);

            img_product = (ImageView) itemView.findViewById(R.id.img_product);
            txt_product_name = (TextView) itemView.findViewById(R.id.txt_product_name);
            txt_OtherName = (TextView) itemView.findViewById(R.id.txt_product_othername);
            txt_product_price = (TextView) itemView.findViewById(R.id.txt_product_price);
            txt_SpecialPrice = (TextView) itemView.findViewById(R.id.txt_product_specialprice);
            btn_addtocart=(LinearLayout) itemView.findViewById(R.id.btn_addtocart);
            img_plus = (ImageView) itemView.findViewById(R.id.img_plus);
            img_minus = (ImageView) itemView.findViewById(R.id.img_minus);
            txt_quantity = (TextView) itemView.findViewById(R.id.txt_quantity);
            txt_ordered_qty = (TextView) itemView.findViewById(R.id.txt_ordered_qty);
            txt_cleaned_qty = (TextView) itemView.findViewById(R.id.txt_cleaned_qty);
            group=(RadioGroup)itemView.findViewById(R.id.radiogroup);
        }
    }

    private void AddToCart(String Quantity,String ProductID,String Cuttype,MyViewHolder holder){
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
        MyViewHolder mholder;


        LoadAddToCartInitiate(String Customer_ID, String Product_ID, String CutType,String Qty,MyViewHolder holder) {
            mCustomerID = Customer_ID;
            mProductID = Product_ID;
            mCutype = CutType;
            mQty = Qty;
            mholder=holder;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mholder.layout.setVisibility(View.GONE);
            mholder.layout_indicator.setVisibility(View.VISIBLE);
            helper.Delete_cartcount();
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {

            HttpOperations httpOperations = new HttpOperations(mContext);
            Log.d("111111", "PASSING VALUE " + mCustomerID+" "+mProductID+" "+mCutype+" "+mQty);
            StringBuilder result = httpOperations.doAddToCart(mCustomerID, mProductID, mCutype,mQty);
            Log.d("111111111","RESULT VALUE "+result.toString());
            return result;

        }

        @Override
        protected void onPostExecute(StringBuilder result) {
            super.onPostExecute(result);
            try {
                Log.d("111111111","Result// "+result.toString());
                JSONObject jsonObj = new JSONObject(result.toString());
                if (jsonObj.has("status")) {
                    if (jsonObj.getString("status").equals(String.valueOf(2))) {
                        mholder.layout.setVisibility(View.VISIBLE);
                        mholder.layout_indicator.setVisibility(View.GONE);
                        CustomToast.info(mContext,jsonObj.getString("message").toString()).show();
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
                        final List<HashMap<String, String>> Data_Item;;
                        Data_Item = helper.getCount();
                        Log.d("1111111112","Cart Count "+Data_Item.get(0).get("cartcount"));
                        mConfig.savePreferences(mContext,"CartCount",Data_Item.get(0).get("cartcount"));
                        //   Toast.makeText(mContext, "cart count in adapter "+jsonObj1.getInt("itemsCount"), Toast.LENGTH_SHORT).show();
                        updateListner.onClick(jsonObj1.getInt("itemsCount"));
                        updatecartcount(mholder,Data_Item.get(0).get("cartcount"));
                        Log.d("111111111",sPreferences.getString("CartCount",""));

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                CustomToast.info(mContext,"Please Try Again").show();
            } catch (NullPointerException e) {
                CustomToast.error(mContext,"No Internet Connection.").show();
            } catch (Exception e) {
                CustomToast.info(mContext,"Please Try Again").show();
            }
        }
    }

    private void updatecartcount(MyViewHolder holder,String count){
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("data_changed"));
        NavigationDrawerActivity.getInstance().updateCartCount( count);

       /* Intent intent = new Intent(mContext, ProductViewActivity.class);
        Log.d("11111111shared",sPreferences.getString("CategoryName",""));
        Log.d("11111111shared",sPreferences.getString("CategoryID",""));
        intent.putExtra("ID",sPreferences.getString("CategoryID",""));
        intent.putExtra("NAME",sPreferences.getString("CategoryName",""));
        intent.putExtra("PAGE","SUBPAGE");
       // mContext.getWindow().setExitTransition(null);
        mContext.startActivity(intent);*/
        holder.layout.setVisibility(View.VISIBLE);
        holder.layout_indicator.setVisibility(View.GONE);
        CustomToast.success(mContext, "Item successfully added to cart").show();
    }

}