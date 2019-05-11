package com.smacon.fish2marine;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smacon.f2mlibrary.Progress.AVLoadingIndicatorView;
import com.smacon.f2mlibrary.Switcher.Switcher;
import com.smacon.fish2marine.AdapterClass.OrderItemDetailsAdapter;
import com.smacon.fish2marine.HelperClass.ProductListItem;
import com.smacon.fish2marine.Util.Config;
import com.smacon.fish2marine.Util.HttpOperations;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by smacon on 3/1/18.
 */

public class OrderDetailsActivity extends AppCompatActivity implements View.OnClickListener{
    private String mOrderId;
    Toolbar toolbar;
    private Switcher switcher;
    private TextView error_label_retry, empty_label_retry, mTitle,
            txt_order_date,txt_order_item_count,txt_order_grand_total,txt_address_name,
            txt_order_deliverydate,txt_order_slot,txt_full_address,txt_order_number,
            txt_order_status,txt_order_shipping,txt_order_payment,txt_thankyou,txt_order_sub_total, txt_order_tax ,txt_order_discount;
    private ImageView opendrawer;
    LinearLayout maincontent,subcontent;
    ArrayList<ProductListItem> dataItem;
    private Intent intent;
    RecyclerView mrecyclerview;
    OrderItemDetailsAdapter mOrderItemDetailsAdapter;
    Dialog progressdialog;
    AVLoadingIndicatorView loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_details);
        intent = getIntent();
        mOrderId=intent.getExtras().getString("ORDER_ID");
        progressdialog = new Dialog(OrderDetailsActivity.this);
        progressdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressdialog.setContentView(R.layout.progress_layout);
        progressdialog.setCanceledOnTouchOutside(false);
        loading = progressdialog.findViewById(R.id.indicator);
        InitIdView();
    }
    private void InitIdView(){
        switcher = new Switcher.Builder(getApplicationContext())
                .addErrorView(findViewById(R.id.error_view))
                .addContentView(findViewById(R.id.main_content))
                .setErrorLabel((TextView)findViewById(R.id.error_label))
                .setEmptyLabel((TextView) findViewById(R.id.empty_label))
                .addEmptyView(findViewById(R.id.empty_view))
                .build();

        opendrawer = findViewById(R.id.opendrawer);
        maincontent = findViewById(R.id.main_content);
        subcontent= findViewById(R.id.sub_layout);

        mTitle = findViewById(R.id.mTitle);
        mTitle.setText("ORDER No #"+mOrderId);

        txt_order_date = findViewById(R.id.txt_order_date);

      //  txt_order_tax = ((TextView) findViewById(R.id.txt_order_tax));
      //  txt_order_discount = ((TextView) findViewById(R.id.txt_order_discount));
        txt_order_sub_total = findViewById(R.id.txt_order_sub_total);

      //  txt_order_item_count = ((TextView) findViewById(R.id.txt_order_item_count));
        txt_order_grand_total = findViewById(R.id.txt_order_grand_total);
        txt_address_name = findViewById(R.id.txt_address_name);
        txt_full_address = findViewById(R.id.txt_full_address);
        txt_order_number= findViewById(R.id.txt_order_number);
        txt_order_number.setText("ORDER # "+mOrderId);
        txt_order_status= findViewById(R.id.txt_order_status);
        txt_order_shipping= findViewById(R.id.txt_order_shipping);
        txt_order_payment= findViewById(R.id.txt_order_payment);
        txt_order_deliverydate= findViewById(R.id.txt_order_deliverydate);
        txt_order_slot= findViewById(R.id.txt_order_slot);
        txt_thankyou= findViewById(R.id.txt_thankyou);
        mrecyclerview = findViewById(R.id.mrecyclerview);
        
        error_label_retry = findViewById(R.id.error_label_retry);
        empty_label_retry = findViewById(R.id.empty_label_retry);
        error_label_retry.setOnClickListener(this);
        empty_label_retry.setOnClickListener(this);
        opendrawer.setOnClickListener(this);
        
        dataItem = new ArrayList<>();
        InitGetData();
        mrecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

    }

    private void InitGetData(){

        Config mConfig = new Config(getApplicationContext());
        if(mConfig.isOnline(getApplicationContext())){
            LoadOrderDetailsInitiate mLoadOrderDetailsInitiate = new LoadOrderDetailsInitiate(mOrderId);
            mLoadOrderDetailsInitiate.execute((Void) null);
        }else {
            switcher.showErrorView("No Internet Connection");
        }
    }

    @Override
    public void onClick(View v) {
        int buttonId = v.getId();
        switch (buttonId){

            case R.id.error_label_retry:
                InitGetData();
                break;
            case R.id.empty_label_retry:
                InitGetData();
                break;
            case R.id.opendrawer:
                // mListener.OpenDrawer();
                back();
                break;
        }
    }
    public class LoadOrderDetailsInitiate extends AsyncTask<Void, StringBuilder, StringBuilder> {

        private String mId;
        LoadOrderDetailsInitiate(String id) {
            mId = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          //  switcher.showProgressView();
            progressdialog.show();
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {

            HttpOperations httpOperations = new HttpOperations(getApplicationContext());
            StringBuilder result = httpOperations.doOrderDetails(mId);
            Log.d("1111111", "API_ORDER_DETAILS " + result);
            return result;
        }

        @Override
        protected void onPostExecute(StringBuilder result) {
            super.onPostExecute(result);
            progressdialog.cancel();
            try {
                JSONObject jsonObj0 = new JSONObject(result.toString());
                if (jsonObj0.has("status")) {
                    if (jsonObj0.getString("status").equals(String.valueOf(1))) {
                        switcher.showContentView();
                        subcontent.setVisibility(View.VISIBLE);
                        JSONObject jsonObj1 = jsonObj0.getJSONObject("data");
                        if(jsonObj1.has("items")) {
                            JSONArray feedArray1 = jsonObj1.getJSONArray("items");
                            for (int i = 0; i < feedArray1.length(); i++) {
                                ProductListItem item = new ProductListItem();
                                JSONObject feedObj1 = (JSONObject) feedArray1.get(i);
                              //  String subtotal=feedObj1.getString("subtotal");
                                //String tax=feedObj1.getString("tax");
                                //String grandtotal=feedObj1.getString("grand_total");
                                //String discount=feedObj1.getString("discount");
                                item.setAllproduct_name(feedObj1.getString("name"));
                                item.setAllproduct_Othername(feedObj1.getString("sku"));
                                item.setAllproduct_price(feedObj1.getString("price"));
                                item.setallorder_qty(feedObj1.getString("qty"));
                                item.setallcleaned_qty(feedObj1.getString("grossWeight"));
                                item.setAllproduct_image(feedObj1.getString("imagepath"));
                                if (feedObj1.has("itemOptions")){
                                    JSONArray feedArray2 = feedObj1.getJSONArray("itemOptions");
                                    for (int j = 0; j < feedArray2.length(); j++) {

                                        JSONObject feedObj2 = (JSONObject) feedArray2.get(j);
                                        item.setAllproduct_id(feedObj2.getString("value"));
                                        dataItem.add(item);
                                    }
                                }
                                mOrderItemDetailsAdapter = new OrderItemDetailsAdapter(getApplicationContext(),dataItem);
                                mrecyclerview.setAdapter(mOrderItemDetailsAdapter);
                            }
                        }

                        //txt_order_discount.setText("Rs."+jsonObj1.getString("discount")+"/-");
                       // txt_order_tax.setText("Rs."+jsonObj1.getString("tax")+"/-");
                        txt_order_sub_total.setText("\u20B9 "+jsonObj1.getString("subtotal"));
                        txt_order_status.setText(jsonObj1.getString("orderStatus"));
                        txt_order_date.setText(jsonObj1.getString("orderDate"));
                        txt_order_shipping.setText(jsonObj1.getString("shippingMethod"));
                        txt_order_payment.setText(jsonObj1.getString("paymentMethod"));
                     //   txt_order_item_count.setText(jsonObj1.getString("totalqty"));
                        txt_order_grand_total.setText("\u20B9 "+jsonObj1.getString("grand_total"));


                        Log.d("1111111","here10");

                        if(jsonObj1.has("delivaryInfo")){
                            JSONObject jsonObj2 = jsonObj1.getJSONObject("delivaryInfo");
                            txt_order_deliverydate.setText(jsonObj2.getString("delivaryDate"));
                            txt_order_slot.setText(jsonObj2.getString("delivaryTime"));
                        }
                        else
                        {
                            Toast.makeText(OrderDetailsActivity.this, "no delivery info", Toast.LENGTH_SHORT).show();
                        }
                        Log.d("1111111","here");
                        if(jsonObj1.has("shippingAddress")){
                            JSONObject jsonObj2 = jsonObj1.getJSONObject("shippingAddress");
                            txt_address_name.setText(jsonObj2.getString("name"));
                            Log.d("1111111","here1");
                            String street=jsonObj2.getString("street");
                            street=street.replaceAll("\"","")
                                    .replaceAll("\\[","").replaceAll("\\]","");
                            Log.d("11111111","street"+street);
                            String address=street+",\n"+jsonObj2.getString("city")+",\n"+
                            jsonObj2.getString("region")+","+jsonObj2.getString("postcode")+",\n"+"India"+",\n"+
                            jsonObj2.getString("phone");
                            txt_full_address.setText(address);

                        }

                    }else {
                        switcher.showEmptyView();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                switcher.showErrorView("Please Try Again");
            } catch (NullPointerException e) {
                Log.e("1111111","ERROR "+e);
                switcher.showErrorView("No Internet Connection");
            } catch (Exception e) {
                switcher.showErrorView("Please Try Again");
            }
        }
    }
    public void back(){
        Intent intent = new Intent(getApplicationContext(),NavigationDrawerActivity.class);
        intent.putExtra("PAGE","HOME");
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),NavigationDrawerActivity.class);
        intent.putExtra("PAGE","HOME");
        startActivity(intent);
        finish();
    }
}
