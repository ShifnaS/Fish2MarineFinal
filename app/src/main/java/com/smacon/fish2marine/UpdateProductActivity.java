package com.smacon.fish2marine;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.smacon.f2mlibrary.Badge;
import com.smacon.f2mlibrary.Button.FloatingActionButton;
import com.smacon.f2mlibrary.CustomToast;
import com.smacon.f2mlibrary.Progress.AVLoadingIndicatorView;
import com.smacon.f2mlibrary.Switcher.Switcher;
import com.smacon.fish2marine.AdapterClass.HomeViewPagerAdapter;
import com.smacon.fish2marine.AdapterClass.SpinnerAdapter;
import com.smacon.fish2marine.HelperClass.ProductListItem;
import com.smacon.fish2marine.HelperClass.SqliteHelper;
import com.smacon.fish2marine.Util.Config;
import com.smacon.fish2marine.Util.HttpOperations;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by smacon on 3/1/18.
 */

public class UpdateProductActivity extends AppCompatActivity implements View.OnClickListener{
    private String mProductId,mProductName,mItemID,mQty;
    Toolbar toolbar;
    private Switcher switcher;
    private SqliteHelper helper;
    private Config mConfig;
    List<HashMap<String, String>> SQLData_Item ;
    String CustomerID = "";
    private SharedPreferences sPreferences;

    private TextView error_label_retry, empty_label_retry, mTitle,
            txt_product_name,txt_product_code,txt_product_stock,txt_product_price,
            txt_product_special_price,txt_ordered_qty,txt_cleaned_qty,txt_product_description,txt_sold_by;
    private ImageView opendrawer;
    Button addtocart;
    FrameLayout maincontent,subcontent;
    LinearLayout layout_specialPrice,layout_spinner;
    String CuttypeApplicable="";
    private Badge mBadge;
    ArrayList<ProductListItem> dataItem,spinnerdataItem;
    private Intent intent;
    FloatingActionButton share;

    HomeViewPagerAdapter mviewPagerAdapter;
    ViewPager viewPager;
    LinearLayout sliderDotspanel;
    private int dotscount;
    private ImageView[] dots;
    ImageView Plus,Minus;
    TextView Quantity,choose;
    String mSpinnerItem = "",ShareURL="";
    private Spinner spinner;
    String OrderQty,AfterQty;
    Dialog progressdialog;
    AVLoadingIndicatorView loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_product);
        helper = new SqliteHelper(getApplicationContext(), "Fish2Marine", null, 5);
        sPreferences = getApplicationContext().getSharedPreferences("Fish2Marine", MODE_PRIVATE);
        mConfig = new Config(getApplicationContext());
        intent = getIntent();
        mProductName = intent.getExtras().getString("NAME");
        mProductId=intent.getExtras().getString("ID");
        mQty=intent.getExtras().getString("QTY");
        mItemID=intent.getExtras().getString("ITEM_ID");
        progressdialog = new Dialog(UpdateProductActivity.this);
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

        SQLData_Item = helper.getadmindetails();
        CustomerID=SQLData_Item.get(0).get("admin_id");
        Log.d("1111221", "Customer ID "+CustomerID);

        mTitle = findViewById(R.id.mTitle);
        mTitle.setText(mProductName);

        toolbar = findViewById(R.id.toolbar);
        // ((AppCompatActivity) getApplicationContext()).setSupportActionBar(toolbar);

        opendrawer = findViewById(R.id.opendrawer);
        maincontent = findViewById(R.id.main_content);
        subcontent= findViewById(R.id.sub_layout);

        viewPager = findViewById(R.id.viewPager);
        sliderDotspanel = findViewById(R.id.SliderDots);

        txt_product_name = findViewById(R.id.txt_product_name);
        choose = findViewById(R.id.choose);
        txt_product_code = findViewById(R.id.txt_product_code);
        txt_product_stock = findViewById(R.id.txt_product_stock);
        txt_product_price = findViewById(R.id.txt_product_price);
        layout_specialPrice= findViewById(R.id.layout_specialPrice);
        txt_product_special_price = findViewById(R.id.txt_product_special_price);
        txt_ordered_qty= findViewById(R.id.txt_ordered_qty);
        txt_cleaned_qty= findViewById(R.id.txt_cleaned_qty);
        txt_product_description= findViewById(R.id.txt_product_description);
        txt_sold_by= findViewById(R.id.txt_sold_by);
        addtocart= findViewById(R.id.addtocart);
        Plus = findViewById(R.id.img_plus);
        Minus = findViewById(R.id.img_minus);
        Quantity = findViewById(R.id.txt_quantity);
        share= findViewById(R.id.share);

        layout_spinner= findViewById(R.id.layout_spinner);
        spinner = findViewById(R.id.spinner);

        error_label_retry = findViewById(R.id.error_label_retry);
        empty_label_retry = findViewById(R.id.empty_label_retry);

        error_label_retry.setOnClickListener(this);
        empty_label_retry.setOnClickListener(this);
        addtocart.setOnClickListener(this);
        opendrawer.setOnClickListener(this);
        InitGetData();
        Quantity.setText(mQty);
       /* Double orderqty=Double.parseDouble(OrderQty)*Double.parseDouble(Quantity.getText().toString());
        if(orderqty>=1000){
            Double value=orderqty/1000;
            txt_ordered_qty.setText("Order Qty: "+value+"kg");
        }
        else {
            txt_ordered_qty.setText("Order Qty: "+orderqty+"gm");
        }

        Double afterqty=Double.parseDouble(AfterQty)*Double.parseDouble(Quantity.getText().toString());
        if(afterqty>=1000){
            Double value=afterqty/1000;
            txt_cleaned_qty.setText("After Cleaning: "+value+"kg");
        }
        else {
            txt_cleaned_qty.setText("After Cleaning: "+afterqty+"gm");
        }*/

        Plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number = Integer.parseInt(Quantity.getText().toString()) + 1;
                Quantity.setText(String.valueOf(number));
                Double orderqty=Double.parseDouble(OrderQty)*Double.parseDouble(Quantity.getText().toString());
                if(orderqty>=1000){
                    Double value=orderqty/1000;
                    txt_ordered_qty.setText("Order Qty: "+value+"kg");
                }
                else {
                    txt_ordered_qty.setText("Order Qty: "+orderqty+"gm");
                }

                Double afterqty=Double.parseDouble(AfterQty)*Double.parseDouble(Quantity.getText().toString());
                if(afterqty>=1000){
                    Double value=afterqty/1000;
                    txt_cleaned_qty.setText("After Cleaning: "+value+"kg");
                }
                else {
                    txt_cleaned_qty.setText("After Cleaning: "+afterqty+"gm");
                }

            }
        });

        Minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Integer.parseInt(Quantity.getText().toString()) >= 2) {
                    int number = Integer.parseInt(Quantity.getText().toString()) - 1;
                    Quantity.setText(String.valueOf(number));
                }
                Double orderqty=Double.parseDouble(OrderQty)*Double.parseDouble(Quantity.getText().toString());
                if(orderqty>=1000){
                    Double value=orderqty/1000;
                    txt_ordered_qty.setText("Order Qty: "+value+"kg");
                }
                else {
                    txt_ordered_qty.setText("Order Qty: "+orderqty+"gm");
                }

                Double afterqty=Double.parseDouble(AfterQty)*Double.parseDouble(Quantity.getText().toString());
                if(afterqty>=1000){
                    Double value=afterqty/1000;
                    txt_cleaned_qty.setText("After Cleaning: "+value+"kg");
                }
                else {
                    txt_cleaned_qty.setText("After Cleaning: "+afterqty+"gm");
                }

            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareTextUrl();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                mSpinnerItem = spinnerdataItem.get(i).getcuttype_value();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        dataItem = new ArrayList<>();
    }

    public void setTextvalue(){
        Double orderqty=Double.parseDouble(OrderQty)*Double.parseDouble(Quantity.getText().toString());
        if(orderqty>=1000){
            Double value=orderqty/1000;
            txt_ordered_qty.setText("Order Qty: "+value+"kg");
        }
        else {
            txt_ordered_qty.setText("Order Qty: "+orderqty+"gm");
        }

        Double afterqty=Double.parseDouble(AfterQty)*Double.parseDouble(Quantity.getText().toString());
        if(afterqty>=1000){
            Double value=afterqty/1000;
            txt_cleaned_qty.setText("After Cleaning: "+value+"kg");
        }
        else {
            txt_cleaned_qty.setText("After Cleaning: "+afterqty+"gm");
        }
    }

    private void InitGetData(){
        Config mConfig = new Config(getApplicationContext());
        if(mConfig.isOnline(getApplicationContext())){
            LoadProductDescriptionInitiate mLoadProductDescriptionInitiate = new LoadProductDescriptionInitiate(mProductId);
            mLoadProductDescriptionInitiate.execute((Void) null);
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
                onBackPressed();
                break;
            case R.id.addtocart:
                UpdateCart(Quantity.getText().toString(), mItemID, mSpinnerItem);
                break;

        }
    }
    private void shareTextUrl() {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        share.putExtra(Intent.EXTRA_SUBJECT, "Fish2Marine");
        share.putExtra(Intent.EXTRA_TEXT, "Get more at Fish2Marine! Register with Fish2Marine and explore more fish.\n"+ShareURL);
        startActivity(Intent.createChooser(share, "Share link!"));

    }
    public class LoadProductDescriptionInitiate extends AsyncTask<Void, StringBuilder, StringBuilder> {
        private String mId;
        LoadProductDescriptionInitiate(String id) {
            mId = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // switcher.showProgressView();
            progressdialog.show();
            spinnerdataItem=new ArrayList<>();

        }

        @Override
        protected StringBuilder doInBackground(Void... params) {

            HttpOperations httpOperations = new HttpOperations(getApplicationContext());
            StringBuilder result = httpOperations.doProductDescription(mId);
            Log.d("1111111", "API_PRODUCT_DESC " + result);
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
                        if(jsonObj1.has("productData")) {
                            JSONArray feedArray1 = jsonObj1.getJSONArray("productData");
                            for (int i = 0; i < feedArray1.length(); i++) {
                                JSONObject feedObj1 = (JSONObject) feedArray1.get(i);
                                ShareURL=feedObj1.getString("urlkey");
                                txt_product_name.setText(feedObj1.getString("name")+" - "+feedObj1.getString("nameInMalayalam"));
                                txt_product_code.setText("SKU# : "+feedObj1.getString("sku"));
                                txt_product_stock.setVisibility(View.GONE);
                                if (feedObj1.getString("inStock").equals("")||feedObj1.getString("inStock").equals("0")){
                                    txt_product_stock.setText("Out of Stock");
                                    txt_product_stock.setTextColor(getResources().getColor(R.color.red));
                                }
                                else {
                                    txt_product_stock.setText("In Stock: "+feedObj1.getString("inStock"));
                                    txt_product_stock.setTextColor(getResources().getColor(R.color.colorAccentsecond));
                                }
                                txt_product_price.setText(""+feedObj1.getString("price"));
                                if (feedObj1.getString("specialPrice").equals("")||feedObj1.getString("specialPrice").equals("0")){
                                    layout_specialPrice.setVisibility(View.GONE);
                                }
                                else {
                                    txt_product_price.setPaintFlags(
                                            txt_product_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                    txt_product_special_price.setText("Rs. "+feedObj1.getString("specialPrice"));
                                }
                                OrderQty=feedObj1.getString("beforeCleaning");
                                AfterQty=feedObj1.getString("afterCleaning");
                                setTextvalue();
                                txt_product_description.setText(feedObj1.getString("description"));
                                if (feedObj1.getString("seller").equals("")){
                                    txt_sold_by.setVisibility(View.GONE);
                                }
                                else {
                                    txt_sold_by.setText(feedObj1.getString("seller"));
                                }
                                if (feedObj1.has("galleryImages")){
                                    JSONArray feedArray2 = feedObj1.getJSONArray("galleryImages");
                                    for (int j = 0; j < feedArray2.length(); j++) {
                                        ProductListItem item = new ProductListItem();
                                        JSONObject feedObj2 = (JSONObject) feedArray2.get(j);
                                        item.set_sliderimage(feedObj2.getString("imageurl"));
                                        dataItem.add(item);
                                    }
                                    mviewPagerAdapter = new HomeViewPagerAdapter(getApplicationContext(),dataItem);
                                    viewPager.setAdapter(mviewPagerAdapter);
                                    initviewpager();
                                }
                                CuttypeApplicable= feedObj1.getString("cutTypeApplicable");
                                if(feedObj1.getString("cutTypeApplicable").equals("2")){
                                    layout_spinner.setVisibility(View.GONE);
                                    choose.setVisibility(View.GONE);
                                }
                                else {
                                    if (feedObj1.has("cutTypes")){
                                        JSONArray feedArray2 = feedObj1.getJSONArray("cutTypes");
                                        for (int k = 0; k < feedArray2.length(); k++) {
                                            ProductListItem item1 = new ProductListItem();
                                            JSONObject feedObj2 = (JSONObject) feedArray2.get(k);
                                            Log.d("1111221", "API ");
                                            item1.setcuttype_label(feedObj2.getString("label"));
                                            item1.setcuttype_value(feedObj2.getString("value"));
                                            item1.setcuttype_imageurl(feedObj2.getString("ImageUrl"));
                                            spinnerdataItem.add(item1);
                                        }
                                        SpinnerAdapter mSpinnerAdapter = new SpinnerAdapter(getApplicationContext(),
                                                spinnerdataItem);
                                        spinner.setAdapter(mSpinnerAdapter);
                                    }
                                }
                            }
                            //Log.d("1111221", "API "+dataItem.size());
                            //mTitle.setText("");
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
    private void initviewpager(){
        dotscount = mviewPagerAdapter.getCount();
        dots = new ImageView[dotscount];

        for(int i = 0; i < dotscount; i++){

            dots[i] = new ImageView(getApplicationContext());
            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.nonactive_dot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);

            sliderDotspanel.addView(dots[i], params);

        }
        dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                for(int i = 0; i< dotscount; i++){
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.nonactive_dot));
                }

                dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void UpdateCart(String Quantity,String ProductID,String Cuttype){
        Config mConfig = new Config(getApplicationContext());
        if(mConfig.isOnline(getApplicationContext())){
            LoadUpdateCartInitiate mLoadUpdateCartInitiate = new LoadUpdateCartInitiate(
                    CustomerID,ProductID,Cuttype,Quantity);

            mLoadUpdateCartInitiate.execute((Void) null);
        }else {
            CustomToast.error(getApplicationContext(),"No Internet Connection.").show();
        }
    }

    public class LoadUpdateCartInitiate extends AsyncTask<Void, StringBuilder, StringBuilder> {

        private String mCustomerID,mitemID,mCutype,mQty;



        LoadUpdateCartInitiate(String Customer_ID, String Product_ID, String CutType,String Qty) {
            mCustomerID = Customer_ID;
            mitemID = Product_ID;
            mCutype = CutType;
            mQty = Qty;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            helper.Delete_cartcount();
            //   switcher.showProgressView();
            progressdialog.show();

        }

        @Override
        protected StringBuilder doInBackground(Void... params) {

            HttpOperations httpOperations = new HttpOperations(getApplicationContext());
            Log.d("111111", "PASSING VALUE " + mCustomerID+" "+mitemID+" "+mCutype+" "+mQty);
            StringBuilder result = httpOperations.doUpdateCart(mCustomerID, mitemID, mCutype,mQty);
            Log.d("111111111",result.toString());
            return result;

        }

        @Override
        protected void onPostExecute(StringBuilder result) {
            super.onPostExecute(result);
            progressdialog.cancel();
            try {
                JSONObject jsonObj = new JSONObject(result.toString());
                if (jsonObj.has("status")) {
                    if (jsonObj.getString("status").equals(String.valueOf(2))) {
                        switcher.showContentView();
                        Log.d("11111111","here0");
                        CustomToast.info(getApplicationContext(), jsonObj.getString("message")).show();
                    }else if (jsonObj.getString("status").equals(String.valueOf(1))) {
                        CustomToast.success(getApplicationContext(),"Updated").show();
                        onBackPressed();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                CustomToast.info(getApplicationContext(),"Please Try Again").show();
            } catch (NullPointerException e) {
                CustomToast.error(getApplicationContext(),"No Internet Connection.").show();
            } catch (Exception e) {
                CustomToast.info(getApplicationContext(),"Please Try Again").show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(UpdateProductActivity.this,MyCartActivity.class);
        intent.putExtra("PAGE","HOME");
        startActivity(intent);
        finish();
    }
}
