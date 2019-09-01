package com.smacon.fish2marine;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.smacon.f2mlibrary.Badge;
import com.smacon.f2mlibrary.Button.FloatingActionButton;
import com.smacon.f2mlibrary.CustomToast;
import com.smacon.f2mlibrary.Progress.AVLoadingIndicatorView;
import com.smacon.f2mlibrary.Switcher.Switcher;
import com.smacon.fish2marine.AdapterClass.HomeViewPagerAdapter;
import com.smacon.fish2marine.AdapterClass.SpinnerAdapter;
import com.smacon.fish2marine.AdapterClass.places.placecomplete.PlaceAutocompleteAdapter;
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

public class ProductDescriptionActivity extends AppCompatActivity implements View.OnClickListener{
    private String mProductId,mProductName;
    Toolbar toolbar;
    private Switcher switcher;
    private SqliteHelper helper;
    private Config mConfig;
    List<HashMap<String, String>> SQLData_Item ;
    String CustomerID = "";
    private SharedPreferences sPreferences;
    private String mAction="",sLoc="",sLat,sLng;

    private TextView error_label_retry, empty_label_retry, mTitle,
            txt_product_name,txt_product_code,txt_product_stock,txt_product_price,
            txt_product_special_price,txt_ordered_qty,txt_cleaned_qty,txt_product_description,txt_sold_by,txt_product_size;
    private ImageView opendrawer,icon_cart,location;
    Button addtocart;
    FrameLayout maincontent,subcontent;
    LinearLayout layout_specialPrice,layout_spinner;
    String CuttypeApplicable="";
    private Badge mBadge;
    ArrayList<ProductListItem> dataItem,spinnerdataItem;
    private Intent intent;
    FloatingActionButton share;
    Badge cartbadge;
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
    Dialog dialog;
    TextView tv_currLocation;
    LinearLayout loc;
    FrameLayout layout_indicator;
    Button btn_ok;
    AutoCompleteTextView searched_address;
    //New places code
    protected GeoDataClient mGeoDataClient;
    private PlaceAutocompleteAdapter mAdapter;
    //private AutoCompleteTextView mAutocompleteView;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(9.999620, 76.314297), new LatLng(10.066215, 76.350793));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_description);
        helper = new SqliteHelper(getApplicationContext(), "Fish2Marine", null, 5);
        sPreferences = getApplicationContext().getSharedPreferences("Fish2Marine", MODE_PRIVATE);
        mConfig = new Config(getApplicationContext());
        intent = getIntent();
        mProductName = intent.getExtras().getString("NAME");
        mProductId=intent.getExtras().getString("ID");
        progressdialog = new Dialog(ProductDescriptionActivity.this);
        progressdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressdialog.setContentView(R.layout.progress_layout);
        progressdialog.setCanceledOnTouchOutside(false);
        loading = (AVLoadingIndicatorView) progressdialog.findViewById(R.id.indicator);
        location= findViewById(R.id.location);

        icon_cart = ((ImageView) findViewById(R.id.icon));
        cartbadge = findViewById(R.id.cartbadge);
        cartbadge.setText(sPreferences.getString("CartCount",""));
        icon_cart.setOnClickListener(this);
        InitIdView();


        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog=new Dialog(ProductDescriptionActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setContentView(R.layout.dialog_location);
                dialog.show();
                ImageView gifImageView= dialog.findViewById(R.id.gifImageView);
                tv_currLocation= dialog.findViewById(R.id.current_loc);
                loc=dialog.findViewById(R.id.loc);

                btn_ok= dialog.findViewById(R.id.btn_ok);
                layout_indicator= dialog.findViewById(R.id.layout_indicator);

                mGeoDataClient = Places.getGeoDataClient(ProductDescriptionActivity.this, null);
                searched_address= dialog.findViewById(R.id.searched_address);

                try
                {
                    loc.setVisibility(View.VISIBLE);
                    String loc=sPreferences.getString("Location","");
                    String location[]=loc.split(" ");
                    String sec[]=location[1].split(",");
                    String cur_loc=location[0]+" "+sec[0];
                    tv_currLocation.setText(cur_loc);
                }
                catch (Exception e)
                {
                    Log.e("1111location error",""+e);
                }
                searched_address.setOnItemClickListener(mAutocompleteClickListener);

                mAdapter = new PlaceAutocompleteAdapter(ProductDescriptionActivity.this, mGeoDataClient, BOUNDS_GREATER_SYDNEY,null);
                searched_address.setThreshold(1);
                searched_address.setAdapter(mAdapter);
                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //  InitGetLocation(sLat,sLng,sLoc);
                    }
                });
            }
        });

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

        mTitle = ((TextView) findViewById(R.id.mTitle));
        mTitle.setText(mProductName);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // ((AppCompatActivity) getApplicationContext()).setSupportActionBar(toolbar);

        opendrawer = ((ImageView) findViewById(R.id.opendrawer));
        maincontent = ((FrameLayout)findViewById(R.id.main_content));
        subcontent=((FrameLayout)findViewById(R.id.sub_layout));

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        sliderDotspanel = (LinearLayout) findViewById(R.id.SliderDots);

        txt_product_name = ((TextView) findViewById(R.id.txt_product_name));
        choose = ((TextView) findViewById(R.id.choose));
        txt_product_code = ((TextView) findViewById(R.id.txt_product_code));
        txt_product_stock = ((TextView) findViewById(R.id.txt_product_stock));
        txt_product_price = ((TextView) findViewById(R.id.txt_product_price));
        layout_specialPrice=(LinearLayout)findViewById(R.id.layout_specialPrice);
        txt_product_special_price = ((TextView) findViewById(R.id.txt_product_special_price));
        txt_ordered_qty= ((TextView) findViewById(R.id.txt_ordered_qty));
        txt_cleaned_qty= ((TextView) findViewById(R.id.txt_cleaned_qty));
        txt_product_description= ((TextView) findViewById(R.id.txt_product_description));
        txt_sold_by=((TextView) findViewById(R.id.txt_sold_by));
        addtocart=(Button)findViewById(R.id.addtocart);
        Plus = (ImageView) findViewById(R.id.img_plus);
        Minus = (ImageView) findViewById(R.id.img_minus);
        Quantity = (TextView) findViewById(R.id.txt_quantity);
        share=(FloatingActionButton)findViewById(R.id.share);
        txt_product_size= findViewById(R.id.txt_product_size);

        layout_spinner=(LinearLayout)findViewById(R.id.layout_spinner);
        spinner = (Spinner)findViewById(R.id.spinner);

        error_label_retry = ((TextView) findViewById(R.id.error_label_retry));
        empty_label_retry = ((TextView)findViewById(R.id.empty_label_retry));

        error_label_retry.setOnClickListener(this);
        empty_label_retry.setOnClickListener(this);
        addtocart.setOnClickListener(this);
        opendrawer.setOnClickListener(this);


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


        InitGetData();

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
            case R.id.icon:
                Intent i=new Intent(getApplicationContext(),MyCartActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.addtocart:
                if(sPreferences.getString("DeliveryCenter_ID","").equals("")){
                    CustomToast.error(v.getContext(), "Please Choose ur location"+sPreferences.getString("DeliveryCenter_ID","")).show();
                }
                else {
                    AddToCart(Quantity.getText().toString(), mProductId, mSpinnerItem);
                }
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
            //  switcher.showProgressView();
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
                                txt_product_size.setText("Fish Size :"+feedObj1.getString("fish_size"));
                                if (feedObj1.getString("inStock").equals("")||feedObj1.getString("inStock").equals("0")){
                                    txt_product_stock.setText("Out of Stock");
                                    txt_product_stock.setTextColor(getResources().getColor(R.color.red));
                                }
                                else {
                                    txt_product_stock.setText("In Stock: "+feedObj1.getString("inStock"));
                                    txt_product_stock.setTextColor(getResources().getColor(R.color.colorAccentsecond));
                                }
                                txt_product_price.setText(""+feedObj1.getString("price")+"/0.5kg");
                                if (feedObj1.getString("specialPrice").equals("")||feedObj1.getString("specialPrice").equals("0")){
                                    layout_specialPrice.setVisibility(View.GONE);
                                }
                                else {
                                    txt_product_price.setPaintFlags(
                                            txt_product_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                    txt_product_special_price.setText("Rs. "+feedObj1.getString("specialPrice"));
                                }
                                txt_ordered_qty.setText("Ordered Qty: "+feedObj1.getString("beforeCleaning")+"g");
                                txt_cleaned_qty.setText("After Cleaning: "+feedObj1.getString("afterCleaning")+"g");
                                OrderQty=feedObj1.getString("beforeCleaning");
                                AfterQty=feedObj1.getString("afterCleaning");
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
                                CuttypeApplicable=feedObj1.getString("cutTypeApplicable").toString();
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
    public boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private void AddToCart(String Quantity,String ProductID,String Cuttype){
        Config mConfig = new Config(getApplicationContext());
        if(mConfig.isOnline(getApplicationContext())){
            LoadAddToCartInitiate mLoadAddToCartInitiate = new LoadAddToCartInitiate(
                    CustomerID,ProductID,Cuttype,Quantity);

            mLoadAddToCartInitiate.execute((Void) null);
        }else {
            CustomToast.error(getApplicationContext(),"No Internet Connection.").show();
        }
    }

    public class LoadAddToCartInitiate extends AsyncTask<Void, StringBuilder, StringBuilder> {

        private String mCustomerID,mProductID,mCutype,mQty;



        LoadAddToCartInitiate(String Customer_ID, String Product_ID, String CutType,String Qty) {
            mCustomerID = Customer_ID;
            mProductID = Product_ID;
            mCutype = CutType;
            mQty = Qty;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            helper.Delete_cartcount();
            switcher.showProgressView();
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {

            HttpOperations httpOperations = new HttpOperations(getApplicationContext());
            Log.d("111111", "PASSING VALUE " + mCustomerID+" "+mProductID+" "+mCutype+" "+mQty);
            StringBuilder result = httpOperations.doAddToCart(mCustomerID, mProductID, mCutype,mQty);
            Log.d("111111111",result.toString());
            return result;

        }

        @Override
        protected void onPostExecute(StringBuilder result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObj = new JSONObject(result.toString());
                if (jsonObj.has("status")) {
                    if (jsonObj.getString("status").equals(String.valueOf(2))) {
                        switcher.showContentView();
                        Log.d("11111111","here0");
                        CustomToast.info(getApplicationContext(),jsonObj.getString("message").toString()).show();
                    }else if (jsonObj.getString("status").equals(String.valueOf(1))) {
                        switcher.showContentView();
                        Log.d("11111111","here1");

                        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
                        JSONObject jsonObj1 = jsonObj.getJSONObject("data");
                        for (int i = 0; i < jsonObj1.length(); i++) {
                            Log.d("11111111","here2");

                            HashMap<String, String> map;
                            map = new HashMap<String, String>();
                            map.put("itemsCount", jsonObj1.getString("itemsCount").trim());
                            fillMaps.add(map);
                        }
                        Log.d("11111111","here3");

                        mConfig.savePreferences(getApplicationContext(),"CartID",jsonObj1.getString("id").trim());
                        Log.d("111111111",sPreferences.getString("CartID",""));
                        helper.Insert_Count(fillMaps);
                        final List<HashMap<String, String>> Data_Item;;
                        Data_Item = helper.getCount();
                        Log.d("1111111112","Cart Count "+Data_Item.get(0).get("cartcount"));
                        mConfig.savePreferences(getApplicationContext(),"CartCount",Data_Item.get(0).get("cartcount"));
                        updatecartcount(Data_Item.get(0).get("cartcount"));
                        Log.d("111111111",sPreferences.getString("CartCount",""));

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
    private void updatecartcount(String cartcount){
        cartbadge.setText(cartcount);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("data_changed"));
        NavigationDrawerActivity.getInstance().updateCartCount(cartcount);
//        icon_cart.set
        CustomToast.success(getApplicationContext(), "Item successfully added to cart").show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ProductDescriptionActivity.this,NavigationDrawerActivity.class);
        intent.putExtra("PAGE","HOME");
        startActivity(intent);
        finish();
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getFullText(null);

            Log.d("111111Place1", "Autocomplete item selected: " + primaryText);
            /*
             Issue a request to the Places Geo Data Client to retrieve a Place object with
             additional details about the place.
              */
            Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);

            // Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
            // Toast.LENGTH_SHORT).show();
            Log.d("111111Place2", "Called getPlaceById to get Place details for " + placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data Client query that shows the first place result in
     * the details view on screen.
     */
    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                // Get the Place object from the buffer.
                final Place place = places.get(0);
                mConfig.savePreferences(getApplicationContext(),"Location",place.getAddress().toString());

                Log.d("11111111shared","Location "+sPreferences.getString("Location",""));
                Log.d("1111111",place.getLatLng().toString());
                Log.d("1111111",place.getAddress().toString());

                String s = place.getLatLng().toString();
                String[] latLng = s.substring(10, s.length() - 1).split(",");
                sLat = latLng[0];
                sLng = latLng[1];
                sLoc=place.getAddress().toString();
                Log.d("111111111", "Latitude is: "+sLat+", Longtitude is: "+sLng);
                InitGetLocation(sLat,sLng,sLoc);

                final CharSequence thirdPartyAttribution = places.getAttributions();
                Log.d("111111Place3", "Place details received: " + place.getName());

                places.release();
            } catch (RuntimeRemoteException e) {
                // Request did not complete successfully
                Log.d("111111Place4", "Place query did not complete.", e);
                return;
            }
        }
    };
    private void InitGetLocation(String Lat,String Long,String Location){
        Config mConfig = new Config(getApplicationContext());
        if(mConfig.isOnline(getApplicationContext())){
            LoadLocationInitiate mLoadLocationInitiate = new LoadLocationInitiate(Lat,Long,CustomerID,Location);
            mLoadLocationInitiate.execute((Void) null);
        }else {
            CustomToast.error(getApplicationContext(),"No Internet Connection.").show();
        }
    }
    public class LoadLocationInitiate extends AsyncTask<Void, StringBuilder, StringBuilder> {

        private String mLatitude,mLongitude,mCustomerId,mLocation;
        LoadLocationInitiate(String latitude,String longitude,String customerid,String location) {
            mLatitude=latitude;
            mLongitude=longitude;
            mCustomerId=customerid;
            mLocation=location;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btn_ok.setVisibility(View.GONE);
            layout_indicator.setVisibility(View.VISIBLE);
        }
        @Override
        protected StringBuilder doInBackground(Void... params) {
            HttpOperations httpOperations = new HttpOperations(getApplicationContext());
            StringBuilder result = httpOperations.doLocation(mLatitude,mLongitude,mCustomerId,mLocation);
            Log.d("111111", "API_LOCATION_RESPONSE " + result);
            return result;
        }
        @Override
        protected void onPostExecute(StringBuilder result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObj0 = new JSONObject(result.toString());
                if (jsonObj0.has("status")){
                    if (jsonObj0.getString("status").equals(String.valueOf(1))) {
                        btn_ok.setVisibility(View.VISIBLE);
                        layout_indicator.setVisibility(View.GONE);
                        JSONObject jsonObj1 = jsonObj0.getJSONObject("data");
                        mConfig.savePreferences(getApplicationContext(),"DeliveryCenter_ID",jsonObj1.getString("delivery_centerid").trim());
                        Log.d("11111111shared","center id "+sPreferences.getString("DeliveryCenter_ID",""));
                        if(jsonObj1.getString("delivery_centerid").trim().equals("")){
                            CustomToast.error(getApplicationContext(),"Please choose another location", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            SharedPreferences.Editor editor = sPreferences.edit();
                            editor.remove("CartCount");
                            editor.apply();
                            dialog.dismiss();
                            finish();
                            startActivity(getIntent());
                            //  InitGetData(jsonObj1.getString("delivery_centerid").trim());
                        }
                    }else {
                        btn_ok.setVisibility(View.VISIBLE);
                        layout_indicator.setVisibility(View.GONE);
                        searched_address.setText("");
                        CustomToast.error(getApplicationContext(),"Please choose another location",Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                CustomToast.error(getApplicationContext(),"Please Try Again",Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                CustomToast.error(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                CustomToast.error(getApplicationContext(),"Please Try Again",Toast.LENGTH_SHORT).show();
            }
        }
    }
}