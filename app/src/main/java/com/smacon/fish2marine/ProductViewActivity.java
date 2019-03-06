package com.smacon.fish2marine;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smacon.f2mlibrary.Badge;
import com.smacon.f2mlibrary.Progress.AVLoadingIndicatorView;
import com.smacon.f2mlibrary.Switcher.Switcher;
import com.smacon.fish2marine.AdapterClass.AllProductsGridAdapter;
import com.smacon.fish2marine.Fragment.HomeFragment;
import com.smacon.fish2marine.HelperClass.ProductListItem;
import com.smacon.fish2marine.HelperClass.SqliteHelper;
import com.smacon.fish2marine.Interface.RecycleviewInterface;
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

public class ProductViewActivity extends AppCompatActivity implements View.OnClickListener{
    private SharedPreferences sPreferences;
    private Config mConfig;

    Toolbar toolbar;
    private SqliteHelper helper;
    private Switcher switcher;
    private RecyclerView mrecyclerview;
    private TextView error_label_retry, empty_label_retry, mTitle;
    private ImageView opendrawer;
    GridLayoutManager mLayoutManager;
    AllProductsGridAdapter mAllProductAdapter;
    List<HashMap<String, String>> SQLData_Item,CartData_Item;
    FrameLayout container;
    Dialog progressdialog;
    AVLoadingIndicatorView loading;
    SharedPreferences sp;
    //ArrayList<ProductListItem> searchitem;
    ArrayList<ProductListItem> dataItem;
    UpdateListner updateListner;
    String CustomerID = "";
    private Intent intent;
    private String mCategoryName,mCategoryID,mPage;
    ImageView mycart;
    Badge cartbadge;
    LinearLayout progress_view;
    RecycleviewInterface recycleviewInterface;


    private static ProductViewActivity checkoutActivity;
    String centerid="";
    public static ProductViewActivity getInstance(){
        return checkoutActivity;
    }

    public static ProductViewActivity newInstance(){
        return new ProductViewActivity();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_gridview);
       // recycleviewInterface=(RecycleviewInterface)getApplicationContext();

        //  container=findViewById(R.id.container);
      //  progress_view=findViewById(R.id.progress_view);
            //  addProgressView(findViewById(R.id.progress_view))

        helper = new SqliteHelper(getApplicationContext(), "Fish2Marine", null, 5);
        sPreferences = getSharedPreferences("Fish2Marine", MODE_PRIVATE);




        mConfig = new Config(getApplicationContext());
        IntentFilter inF = new IntentFilter("data_changed");
        LocalBroadcastManager.getInstance(this).registerReceiver(dataChangeReceiver,inF);
      //  Toast.makeText(getApplicationContext(), "hiiiiii", Toast.LENGTH_SHORT).show();
        View inflatedView = getLayoutInflater().inflate(R.layout.view_progress, null);
       // progress_view =  inflatedView.findViewById(R.id.progress_view);
       // progress_view.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        progressdialog = new Dialog(ProductViewActivity.this);
        progressdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressdialog.setContentView(R.layout.progress_layout);
        progressdialog.setCanceledOnTouchOutside(false);
        loading = (AVLoadingIndicatorView) progressdialog.findViewById(R.id.indicator);
        //sp= PreferenceManager.getDefaultSharedPreferences(th
        centerid=sPreferences.getString("DeliveryCenter_ID","");
        if(centerid.equals("")){
            centerid="0";
        }

        updateListner=new UpdateListner() {
            @Override
            public void onClick(int count) {
               // Toast.makeText(ProductViewActivity.this, "count "+count, Toast.LENGTH_SHORT).show();
               // recycleviewInterface.userItemClick(count);
               // InitIdView();
            }
        };

        InitIdView();

    }
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataChangeReceiver);
    }
    private void InitIdView(){

        switcher = new Switcher.Builder(getApplicationContext())
                .addContentView(findViewById(R.id.mrecyclerview))
                .addErrorView(findViewById(R.id.error_view))
                .setErrorLabel((TextView)findViewById(R.id.error_label))
                .setEmptyLabel((TextView) findViewById(R.id.empty_label))
                .addEmptyView(findViewById(R.id.empty_view))
                .build();
      //  mListener=(Listener)ProductViewActivity.this;

        SQLData_Item = helper.getadmindetails();
        CartData_Item=helper.getCount();

        intent = getIntent();
        if (intent.getExtras().getString("PAGE").toString().equals("FROM_HOME")) {
            mConfig.savePreferences(getApplicationContext(),"CategoryName", intent.getExtras().getString("NAME"));
            mConfig.savePreferences(getApplicationContext(),"CategoryID", intent.getExtras().getString("ID"));
            mCategoryName = intent.getExtras().getString("NAME");
            mCategoryID = intent.getExtras().getString("ID");
        }else if (intent.getExtras().getString("PAGE").toString().equals("FROM_CUTTYPE")) {
            mCategoryName = intent.getExtras().getString("NAME");
            mCategoryID = intent.getExtras().getString("ID");
        }
        else if (intent.getExtras().getString("PAGE").toString().equals("SUBPAGE")) {
            mCategoryName = intent.getExtras().getString("NAME");
            mCategoryID = intent.getExtras().getString("ID");
        }

        mTitle = ((TextView) findViewById(R.id.mTitle));
        mTitle.setText(mCategoryName);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
       // ((AppCompatActivity) getApplicationContext()).setSupportActionBar(toolbar);

        mycart=(ImageView) findViewById(R.id.mycart);
        cartbadge = (Badge)findViewById(R.id.cartbadge);
        String id=sPreferences.getString("CartCount","");
       // Toast.makeText(getApplicationContext(), "cartcount "+id, Toast.LENGTH_SHORT).show();
        if(id.equals("0"))
        {
            cartbadge.clear();
        }
        else
        {
            cartbadge.setText(sPreferences.getString("CartCount",""));

        }
        mycart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductViewActivity.this,MyCartActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.bottom_up,
                        android.R.anim.fade_out);
                finish();
            }
        });

        opendrawer = ((ImageView) findViewById(R.id.opendrawer));
        mrecyclerview = ((RecyclerView)findViewById(R.id.mrecyclerview));
        error_label_retry = ((TextView) findViewById(R.id.error_label_retry));
        empty_label_retry = ((TextView)findViewById(R.id.empty_label_retry));

        error_label_retry.setOnClickListener(this);
        empty_label_retry.setOnClickListener(this);
        opendrawer.setOnClickListener(this);

        initRecyclerView();
        dataItem = new ArrayList<>();


        InitGetData();

    }

    private void initRecyclerView() {
        mrecyclerview.setHasFixedSize(true);

        /*float scalefactor = getResources().getDisplayMetrics().density * 100;
        int width = getWindowManager().getDefaultDisplay().getWidth();
        int columns = (int) ((float)width / (float) scalefactor);
        mrecyclerview.setNumColumns(columns);*/

        if (isTablet(getApplicationContext())) {
            mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        } else {
            mLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        }
        mrecyclerview.setLayoutManager(mLayoutManager);
    }

    private void InitGetData(){
        Config mConfig = new Config(getApplicationContext());
        if(mConfig.isOnline(getApplicationContext())){
            LoadProductListInitiate mLoadProductListInitiate = new LoadProductListInitiate(mCategoryID);
            mLoadProductListInitiate.execute((Void) null);
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
                // mListener.OpenDrawer();
               // back();
                break;
        }
    }



    public class LoadProductListInitiate extends AsyncTask<Void, StringBuilder, StringBuilder> {

        private String mId;
        LoadProductListInitiate(String id) {
            mId = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressdialog.show();
           // switcher.showProgressView();
            //AVLoadingIndicatorView loadingIndicatorView=findViewById(R.id.indicator);
          //  loadingIndicatorView.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Toast.makeText(ProductViewActivity.this, "hellooooo", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {

            HttpOperations httpOperations = new HttpOperations(getApplicationContext());
            StringBuilder result = httpOperations.doProductList(mId,centerid);
            Log.d("111111111", "PASSING_DATA " + centerid);

            Log.d("111111111", "RESULT " + result);
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
                        JSONObject jsonObj1 = jsonObj0.getJSONObject("data");
                        if(jsonObj1.has("productList")) {
                            JSONArray feedArray1 = jsonObj1.getJSONArray("productList");
                            dataItem.clear();
                            Log.d("1111221", "API jsonObj1"+feedArray1);
                            for (int i = 0; i < feedArray1.length(); i++) {
                                ProductListItem item = new ProductListItem();
                                JSONObject feedObj1 = (JSONObject) feedArray1.get(i);
                                //item.setneworder_qty();
                                item.setneworder_qty(feedObj1.getString("beforeCleaning"));
                                item.setnewcleaned_qty(feedObj1.getString("afterCleaning"));
                                item.setAllproduct_id(feedObj1.getString("productId"));
                                item.setAllproduct_name(feedObj1.getString("name"));
                                item.setAllproduct_Othername(feedObj1.getString("NameInMalayalam"));
                                item.setAllproduct_price(feedObj1.getString("price"));
                                item.setAllproduct_specialprice(feedObj1.getString("specialPrice"));
                                item.setAllproduct_image(feedObj1.getString("imagepath"));
                                item.setallorder_qty(feedObj1.getString("beforeCleaning"));
                                item.setallcleaned_qty(feedObj1.getString("afterCleaning"));
                                item.setcuttype_applicable(feedObj1.getString("cutTypeApplicable"));
                                List<String> cuttype_dataItem= new ArrayList<>();
                                if(feedObj1.getString("cutTypeApplicable").equals("1")){
                                    if (feedObj1.has("cutTypes")){
                                        JSONArray feedArray2 = feedObj1.getJSONArray("cutTypes");
                                        for (int k = 0; k < feedArray2.length(); k++) {
                                            JSONObject feedObj2 = (JSONObject) feedArray2.get(k);
                                            String cutype=feedObj2.getString("value");
                                            cuttype_dataItem.add(cutype);
                                           // cuttype_dataItem.add(feedObj2.getString("value"));
                                            Log.d("111111111","value: "+cuttype_dataItem.toString());
                                        }
                                       // Log.d("1111111","after loop size: "+cuttype_dataItem.toString());
                                    }

                                }else {
                                    cuttype_dataItem.clear();
                                }
                                Log.d("11111111221", "Data Itemss "+cuttype_dataItem.toString());

                                item.setallCuttype_valuelist(cuttype_dataItem);
                                dataItem.add(item);

                                for (int s=0;s<dataItem.size();s++){
                                    Log.d("11111111","my"+dataItem.get(s).getallCuttype_valuelist());
                                }
                            }
                            CustomerID=SQLData_Item.get(0).get("admin_id");
                            mAllProductAdapter = new AllProductsGridAdapter(ProductViewActivity.this, dataItem,CustomerID,updateListner);
                            mrecyclerview.setAdapter(mAllProductAdapter);
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
    public boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
    private BroadcastReceiver dataChangeReceiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // update your listview

            String id=sPreferences.getString("CartCount","");
           // Toast.makeText(getApplicationContext(), "cartcount1 "+id, Toast.LENGTH_SHORT).show();
            if(id.equals("0"))
            {
                cartbadge.clear();
            }
            else
            {
                cartbadge.setText(sPreferences.getString("CartCount",""));

            }
        }
    };
    private void back(){
        Intent intent = new Intent(ProductViewActivity.this,NavigationDrawerActivity.class);
        intent.putExtra("PAGE","PRODUCT");
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in,
                R.anim.bottom_down);
        finish();
    }
    @Override
    public void onBackPressed() {

        super.onBackPressed();

    }
    public interface UpdateListner {
        void onClick(int count);


    }
}
