package com.smacon.fish2marine;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smacon.f2mlibrary.Progress.AVLoadingIndicatorView;
import com.smacon.f2mlibrary.Switcher.Switcher;
import com.smacon.fish2marine.AdapterClass.PlaceOrderItemAdapter;
import com.smacon.fish2marine.CCAvenuPay.utility.AvenuesParams;
import com.smacon.fish2marine.CCAvenuPay.activity.WebViewActivity;
import com.smacon.fish2marine.Constants.OrdersConstants;
import com.smacon.fish2marine.HelperClass.AddressListItem;
import com.smacon.fish2marine.HelperClass.CartListItem;
import com.smacon.fish2marine.HelperClass.SqliteHelper;
import com.smacon.fish2marine.Util.Config;
import com.smacon.fish2marine.Util.HttpOperations;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class OrderSummaryActivity extends AppCompatActivity implements View.OnClickListener{
    private SharedPreferences sPreferences;
    private SqliteHelper helper;
    private Config mConfig;
    List<HashMap<String, String>> SQLData_Item ;
    String CustomerID = "",QuoteID;
    String increment_id="";

    int RewardPoints;
    private Switcher switcher;
    private RecyclerView mrecyclerview;
    private TextView mTitle,error_label_retry, empty_label_retry,
            carttotal,itemcount,ordertotal,shipping,tax,discount,
            shippingmethod,deliverydate,deliveryslot,shipaddress;
    ArrayList<CartListItem> dataItem;
    ImageView back;
    Button placeorder;
    LinearLayout sub_layout,main_layout;
    PlaceOrderItemAdapter myCartAdapter;
    private Intent mIntent;
    String address_name="",address="",city="",state="",pincode="",country="",phone="";
    Dialog progressdialog;
    AVLoadingIndicatorView loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordersummary);
        helper = new SqliteHelper(getApplicationContext(), "Fish2Marine", null, 5);
        sPreferences = getApplicationContext().getSharedPreferences("Fish2Marine", MODE_PRIVATE);
        mConfig = new Config(getApplicationContext());
        progressdialog = new Dialog(OrderSummaryActivity.this);
        progressdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressdialog.setContentView(R.layout.progress_layout);
        progressdialog.setCanceledOnTouchOutside(false);
        loading = (AVLoadingIndicatorView) progressdialog.findViewById(R.id.indicator);

        InitIdView();
    }
    private void InitIdView(){


        SQLData_Item = helper.getadmindetails();
        CustomerID=SQLData_Item.get(0).get("admin_id");
        Log.d("1111221", "Customer ID "+CustomerID);

        mTitle=(TextView)findViewById(R.id.mTitle);
        mTitle.setText("Order Summary");

        switcher = new Switcher.Builder(getApplicationContext())
                .addContentView(findViewById(R.id.main_layout))
                .addErrorView( findViewById(R.id.error_view))
                .setErrorLabel((TextView) findViewById(R.id.error_label))
                .setEmptyLabel((TextView) findViewById(R.id.empty_label))
                .addEmptyView( findViewById(R.id.empty_view))
                .build();
        sub_layout=((LinearLayout)findViewById(R.id.sub_layout));
        main_layout=((LinearLayout)findViewById(R.id.main_layout));
        mrecyclerview = ((RecyclerView) findViewById(R.id.mrecyclerview));
        mrecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        carttotal = ((TextView)  findViewById(R.id.CartTotal));
        shipping = ((TextView)  findViewById(R.id.Shipping));
        tax = ((TextView)  findViewById(R.id.Taxamount));
        discount = ((TextView) findViewById(R.id.Discount));
        ordertotal = ((TextView) findViewById(R.id.OrderTotal));
        itemcount = ((TextView)  findViewById(R.id.itemcount));

        shippingmethod = ((TextView) findViewById(R.id.shippingmethod));
        deliverydate = ((TextView) findViewById(R.id.deliverydate));
        deliveryslot=((TextView) findViewById(R.id.deliveryslot));
        shipaddress=((TextView) findViewById(R.id.shipaddress));

        mIntent = getIntent();
        QuoteID=mIntent.getExtras().getString("PLACEORDER_ID");

        carttotal.setText("Rs. "+mIntent.getExtras().getString("SUBTOTAL"));
        shipping.setText("Rs. "+mIntent.getExtras().getString("SHIPPING"));
        tax.setText("Rs. "+mIntent.getExtras().getString("TAX"));
        discount.setText("Rs. "+mIntent.getExtras().getString("DISCOUNT"));
        ordertotal.setText("Rs. "+mIntent.getExtras().getString("GRANDTOTAL"));
        itemcount.setText(mIntent.getExtras().getString("ITEMCOUNT")+" items in the cart");
        shippingmethod.setText("Shipping Method "+mIntent.getExtras().getString("SHIPPINGMETHOD"));
        deliverydate.setText("Delivery Date: "+mIntent.getExtras().getString("DELIVERYDATE"));
        deliveryslot.setText("Delivery Slot: "+mIntent.getExtras().getString("DELIVERYSLOT"));
        address_name=mIntent.getExtras().getString("FULLNAME");
        address=mIntent.getExtras().getString("ADDRESS");
        city=mIntent.getExtras().getString("CITY");
        state=mIntent.getExtras().getString("STATE");
        country=mIntent.getExtras().getString("COUNTRY");
        pincode=mIntent.getExtras().getString("PINCODE");
        phone=mIntent.getExtras().getString("PHONE");
        shipaddress.setText(address_name+",\n"+address+",\n"+city+",\n"+state+","+pincode+",\n"+country+",\n"+phone);
        placeorder=((Button) findViewById(R.id.placeorder));

        error_label_retry = ((TextView)  findViewById(R.id.error_label_retry));
        empty_label_retry = ((TextView) findViewById(R.id.empty_label_retry));
        error_label_retry.setOnClickListener(this);
        empty_label_retry.setOnClickListener(this);
        placeorder.setOnClickListener(this);
        back=(ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);

        dataItem = new ArrayList<>();
        InitGetCartData();
    }

    @Override
    public void onClick(View view) {
        int buttonId = view.getId();
        switch (buttonId) {
            case R.id.error_label_retry:
                InitGetCartData();
                break;
            case R.id.empty_label_retry:
                InitGetCartData();
                break;
            case R.id.back:
                onBackPressed();
                break;
            case R.id.placeorder:
                    InitPlaceOrder();
                break;
        }
    }

    private void InitGetCartData(){
        Config mConfig = new Config(getApplicationContext());
        if(mConfig.isOnline(getApplicationContext())){
            LoadMyCartInitiate mLoadMyCartInitiate = new LoadMyCartInitiate(CustomerID);
            mLoadMyCartInitiate.execute((Void) null);
        }else {
            switcher.showErrorView("No Internet Connection");
        }
    }

    private void InitPlaceOrder(){
        Config mConfig = new Config(getApplicationContext());
        if(mConfig.isOnline(getApplicationContext())){
            PlaceOrderInitiate mPlaceOrderInitiate = new PlaceOrderInitiate(QuoteID);
            mPlaceOrderInitiate.execute((Void) null);
        }else {
            switcher.showErrorView("No Internet Connection");
        }
    }

    public class PlaceOrderInitiate extends AsyncTask<Void, StringBuilder, StringBuilder> {

        private String mQuoteId;
        PlaceOrderInitiate(String ID) {
            mQuoteId=ID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressdialog.show();
           // switcher.showProgressView();
            //helper.Delete_timeslot_details();
        }
        @Override
        protected StringBuilder doInBackground(Void... params) {
            int id=Integer.parseInt(mQuoteId);
            HttpOperations httpOperations = new HttpOperations(getApplicationContext());
            StringBuilder result = httpOperations.doPlaceOrder(id);
            Log.d("111111","PASSING VALUE: QUOTE ID "+id);
            Log.d("111111", "RESULT "+result);
            return result;
        }

        @Override
        protected void onPostExecute(StringBuilder result) {
            super.onPostExecute(result);
            Log.d("111111", "Result from web api "+result);
            progressdialog.cancel();
                if(result.toString().contains("message")){
                    switcher.showErrorView("Could not place order");
                }
                else {
                    try {
                        String id="",cid="";
                        JSONObject jsonObj0 = new JSONObject(result.toString());
                       // JSONArray jo=new JSONArray(result.toString());
                      //  JSONObject feedObj1;


                        if (jsonObj0.has("data")) {
                            JSONArray feedArray1 = jsonObj0.getJSONArray("data");
                            Log.d("111111", "API jsonObj1" + feedArray1);
                            for (int i = 0; i < feedArray1.length(); i++) {
                               // JSONObject feedObj1=new JSONObject();
                                JSONObject  feedObj1 = feedArray1.getJSONObject(i);
                                id = feedObj1.getString("increment_id");
                                cid=feedObj1.getString("order_id");
                                Log.d("111111", "JSONONJECT " + feedObj1.toString());
                               // id = jo.getString("increment_id");
                                Log.d("111111", "ID  " + id);

                                if (increment_id.equals("")) {
                                    increment_id = id;
                                } else {
                                    increment_id = increment_id + "/" + id;
                                }
                                //  increment_id=id+","+increment_id;

                            }
                        }
                      //  String OrderID = result.toString().replaceAll("\\W", "");
                        // OrderID = OrderID.replaceAll("\\s","");
                        Log.d("111111", "ORDER ID "+increment_id);
                        Log.d("111111", "ORDER ID "+cid);
                        InitDeliveryOptions(cid);

                    }
                    catch (Exception e)
                    {
                        Log.d("111111", "EXCEPTION "+e.getMessage());

                    }
                }
        }
    }

    private void InitDeliveryOptions(String ID){
        Config mConfig = new Config(getApplicationContext());
        if(mConfig.isOnline(getApplicationContext())){
            DeliveryOptionsInitiate mDeliveryOptionsInitiate = new DeliveryOptionsInitiate(ID);
            mDeliveryOptionsInitiate.execute((Void) null);
        }else {
            switcher.showErrorView("No Internet Connection");
        }
    }

    public class DeliveryOptionsInitiate extends AsyncTask<Void, StringBuilder, StringBuilder> {

        private String mId;
        DeliveryOptionsInitiate(String ID) {
            mId=ID;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           // switcher.showProgressView();
            progressdialog.show();

        }
        @Override
        protected StringBuilder doInBackground(Void... params) {

            HttpOperations httpOperations = new HttpOperations(getApplicationContext());
            StringBuilder result = httpOperations.doGoToPayment(mId);
            Log.d("111111111","PASSING VALUE: CUSTOMER ID "+mId);
            Log.d("111111111", "RESULT "+result);
            return result;
        }

        @Override
        protected void onPostExecute(StringBuilder result) {
            super.onPostExecute(result);
            progressdialog.cancel();
            try {
                JSONObject jsonObj0 = new JSONObject(result.toString());
              //  Toast.makeText(OrderSummaryActivity.this, ""+increment_id, Toast.LENGTH_SHORT).show();

                Log.d("111111111", "RESULT1 "+result);

                if (jsonObj0.has("status")) {
                    if (jsonObj0.getInt("status")==1) {
                       // switcher.showContentView();
                       // sub_layout.setVisibility(View.VISIBLE);
                        Log.d("111111", "here0 ");
                        JSONObject jsonObj1 = jsonObj0.getJSONObject("data");
                        Log.e("111111","DATA "+jsonObj1.toString());
                        if (jsonObj0.getString("method").equals("cashondelivery")){
                            Log.d("111111", "here1 ");
                            String orderid=jsonObj1.getString("orderid");
                            String ordernumber=jsonObj1.getString("order_number");
                            Intent intent = new Intent(OrderSummaryActivity.this,SuccessActivity.class);
                            intent.putExtra("PLACEORDER_ID",orderid);
                            intent.putExtra("ORDER_NUMBER",increment_id);
                            startActivity(intent);
                            finish();
                        }else if (jsonObj0.getString("method").equals("ccavenuepay")){


                            Log.d("Access Code","/////////////////////////////////// "+jsonObj1.getString("access_code"));
                            Intent i = new Intent(OrderSummaryActivity.this,WebViewActivity.class);
                            i.putExtra(AvenuesParams.ORDER_ID, jsonObj1.getString("orderid"));
                            i.putExtra(AvenuesParams.ORDER_NUMBER, increment_id);
                            i.putExtra(AvenuesParams.ACCESS_CODE,jsonObj1.getString("access_code"));
                            i.putExtra(AvenuesParams.MERCHANT_ID,jsonObj1.getString("merchant_id"));
                            i.putExtra(AvenuesParams.CURRENCY, jsonObj1.getString("currency"));
                            i.putExtra(AvenuesParams.AMOUNT, jsonObj1.getString("order_amount"));
                            i.putExtra(AvenuesParams.RSA_KEY_URL, jsonObj1.getString("rsa_url"));
                            i.putExtra(AvenuesParams.REDIRECT_URL, jsonObj1.getString("success_url"));
                            i.putExtra(AvenuesParams.CANCEL_URL, jsonObj1.getString("cancel_url"));
                            i.putExtra(AvenuesParams.BILLING_NAME,address_name);
                            i.putExtra(AvenuesParams.BILLING_ADDRESS,address);
                            i.putExtra(AvenuesParams.BILLING_ZIP,pincode);
                            i.putExtra(AvenuesParams.BILLING_CITY,city);
                            i.putExtra(AvenuesParams.BILLING_STATE,state);
                            i.putExtra(AvenuesParams.BILLING_COUNTRY,country);
                            i.putExtra(AvenuesParams.BILLING_TEL,phone);
                            startActivity(i);
                            finish();
                        }else if (jsonObj0.getString("method").equals("banktransfer")){
                            String orderid=jsonObj1.getString("orderid");
                            String ordernumber=jsonObj1.getString("order_number");
                            Intent intent = new Intent(OrderSummaryActivity.this,SuccessActivity.class);
                            intent.putExtra("PLACEORDER_ID",orderid);
                            intent.putExtra("ORDER_NUMBER",increment_id);
                            startActivity(intent);
                           // Log.d("111111", "here3 ");
                            finish();
                        }

                    }else {
                        switcher.showEmptyView();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("111111","ERRROR1 "+e.getMessage());
                switcher.showErrorView("Please Try Again");
            } catch (NullPointerException e) {
                Log.d("111111","ERRROR2 "+e.getMessage());

                switcher.showErrorView("No Internet Connection");
            } catch (Exception e) {
                Log.d("111111","ERRROR3 "+e.getMessage());

                switcher.showErrorView("Please Try Again");
            }
        }

    }

    public class LoadMyCartInitiate extends AsyncTask<Void, StringBuilder, StringBuilder> {

        private String mId;
        LoadMyCartInitiate(String ID) {
            mId=ID;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressdialog.show();
           // switcher.showProgressView();
            //helper.Delete_timeslot_details();
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {

            HttpOperations httpOperations = new HttpOperations(getApplicationContext());
            StringBuilder result = httpOperations.doMyCartList(mId);
            Log.d("111111111","PASSING VALUE: CUSTOMER ID "+mId);
            Log.d("111111111", "RESULT "+result);
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
                        sub_layout.setVisibility(View.VISIBLE);
                        Log.d("111111", "here0 ");
                        JSONObject jsonObj1 = jsonObj0.getJSONObject("data");
                        if(jsonObj1.has("items")) {
                            JSONArray feedArray1 = jsonObj1.getJSONArray("items");
                            Log.d("1111221", "API jsonObj1" + feedArray1);
                            for (int i = 0; i < feedArray1.length(); i++) {
                                CartListItem item = new CartListItem();
                                JSONObject feedObj1 = (JSONObject) feedArray1.get(i);
                                item.setProductId(feedObj1.getString("productId"));
                                item.setProductImage(feedObj1.getString("imagepath"));
                                item.setProductName(feedObj1.getString("productname")+"/"+feedObj1.getString("itemId"));
                                Log.d("111111", "here3 "+feedObj1.getString("productname"));
                                item.setOtherName(feedObj1.getString("nameInMalayalam"));
                                item.setQuantity(feedObj1.getString("qty"));
                                item.setNetQty(feedObj1.getDouble("netQty"));
                                item.setPrice(feedObj1.getString("price"));
                                item.setItemsTotal(feedObj1.getString("itemsTotal"));
                                item.setItemId(feedObj1.getString("itemId"));
                                item.setCutTypeApplicable(feedObj1.getString("cutTypeApplicable"));
                                item.setCutType(feedObj1.getString("cutType"));
                                item.setBeforeCleaning(feedObj1.getDouble("beforeCleaning"));
                                item.setAfterCleaning(feedObj1.getDouble("afterCleaning"));
                                item.setSoldBy(feedObj1.getString("soldby"));
                                dataItem.add(item);
                            }
                            myCartAdapter = new PlaceOrderItemAdapter(OrderSummaryActivity.this, dataItem);
                            mrecyclerview.setAdapter(myCartAdapter);
                        }
                    }else {
                        switcher.showEmptyView();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                switcher.showErrorView("Please Try Again");
            } catch (NullPointerException e) {
                switcher.showErrorView("No Internet Connection");
            } catch (Exception e) {
                switcher.showErrorView("Please Try Again");
            }
        }

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}