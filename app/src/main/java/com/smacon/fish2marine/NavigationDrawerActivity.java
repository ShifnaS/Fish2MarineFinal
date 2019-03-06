package com.smacon.fish2marine;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.smacon.f2mlibrary.Badge;
import com.smacon.f2mlibrary.CustomEditText;
import com.smacon.f2mlibrary.CustomToast;
import com.smacon.f2mlibrary.SlidingDrawer.SlidingRootNav;
import com.smacon.f2mlibrary.SlidingDrawer.SlidingRootNavBuilder;
import com.smacon.fish2marine.AdapterClass.places.placecomplete.PlaceAutocompleteAdapter;
import com.smacon.fish2marine.DrawerMenu.DrawerAdapter;
import com.smacon.fish2marine.DrawerMenu.DrawerItem;
import com.smacon.fish2marine.DrawerMenu.SimpleItem;
import com.smacon.fish2marine.Fragment.AboutUsFragment;
import com.smacon.fish2marine.Fragment.ContactUsFragment;
import com.smacon.fish2marine.Fragment.HomeFragment;
import com.smacon.fish2marine.Fragment.MyAddressFragment;
import com.smacon.fish2marine.Fragment.MyOrdersListFragment;
import com.smacon.fish2marine.Fragment.MyProfileFragment;
import com.smacon.fish2marine.Fragment.MyReferalFragment;
import com.smacon.fish2marine.Fragment.MyRewardsFragment;
import com.smacon.fish2marine.HelperClass.Listener;
import com.smacon.fish2marine.HelperClass.SqliteHelper;
import com.smacon.fish2marine.Interface.RecycleviewInterface;
import com.smacon.fish2marine.Util.Config;
import com.smacon.fish2marine.Util.HttpOperations;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yarolegovich on 25.03.2017.
 */

public class NavigationDrawerActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener,Listener,RecycleviewInterface {
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;
    private SharedPreferences sPreferences;
    private SqliteHelper helper;
    private Config mConfig;
    private static final int POS_HOME = 0;
    private static final int POS_ORDERHISTORY = 1;
    private static final int POS_MYADDRESS = 2;
    private static final int POS_MYREWARDS= 3;
    private static final int POS_MYREFFERALS = 4;
    private static final int POS_MYPROFILE = 5;
    private static final int POS_CONTACTUS = 6;
    private static final int POS_ABOUT = 7;
    private static final int POS_LOGOUT = 8;
    private static final String KEY_REMEMBER = "remember";
    private int subscreensOnTheStack=0;
    private static final String PREF_NAME = "prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASS = "password";
    private String[] screenTitles;
    private Drawable[] screenIcons;
    LinearLayout layout_drawermenu;
    Toolbar toolbar;
    private SlidingRootNav slidingRootNav;
    TextView fg_title;
    ImageView mycart,location;
    Badge cartbadge;
    FrameLayout layout_indicator;
    Button btn_ok;
    Dialog dialog;
    private Intent intent;
    private String mAction="",sLoc="",sLat,sLng;
    String CustomerID = "",CustomerName="";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;



    //  AddressUpdateListner addressUpdateListner;
    List<HashMap<String, String>> SQLData_Item,SQLData_Item_customer ;

    private static NavigationDrawerActivity mainActivity;
    public static NavigationDrawerActivity getInstance(){
        return mainActivity;
    }

    FirebaseAuth mAuth;


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
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        setContentView(R.layout.activity_drawer);
        mainActivity=this;
        helper = new SqliteHelper(getApplicationContext(), "Fish2Marine", null, 5);
        sPreferences = getApplicationContext().getSharedPreferences("Fish2Marine", MODE_PRIVATE);
        mConfig = new Config(getApplicationContext());
        IntentFilter inF1 = new IntentFilter("data_changed");
        LocalBroadcastManager.getInstance(this).registerReceiver(dataChangeReceiver,inF1);
        mAuth = FirebaseAuth.getInstance();

        SQLData_Item_customer = helper.getadmindetails();
        CustomerID=SQLData_Item_customer.get(0).get("admin_id");
        CustomerName=SQLData_Item_customer.get(0).get("admin_name");

        String user=sharedPreferences.getString(KEY_USERNAME,"");
        String pass=sharedPreferences.getString(KEY_PASS,"");
       // Toast.makeText(mainActivity, "User "+user, Toast.LENGTH_SHORT).show();
      ///  Toast.makeText(mainActivity, "Pass "+pass, Toast.LENGTH_SHORT).show();

        Intent i=getIntent();
        if(i.hasExtra("PAGE"))
        {
            String val=i.getStringExtra("PAGE");
            if(val.equals("LOGIN"))
            {

            }
            else if(val.equals("HOME"))
            {

            }
            else
            {
            }

        }
        else
        {
            clearCart();

        }

        /*Intent mIntent = getIntent();
        if(mIntent.hasExtra("PAGE"))n
        {
            mAction = mIntent.getExtras().getString("PAGE");

        }n
        if(mAction.equals("CART"))
        {
            Intent i=new Intent(getApplicationContext(),MyCartAcfkjksfjksjfkdfjksjfktivity.class);
            startActivity(i);
            finish();skfjkfjkfjkdjfksjfsijkejfksdjfkfjksfjkdfjkjjkjf
        }*/

        SQLData_Item = helper.getCount();
        toolbar=(Toolbar) findViewById(R.id.toolbar);

        fg_title=(TextView) findViewById(R.id.fg_title);
        location=(ImageView) findViewById(R.id.location);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog=new Dialog(NavigationDrawerActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setContentView(R.layout.dialog_location);
                dialog.show();
                ImageView gifImageView=(ImageView)dialog.findViewById(R.id.gifImageView);
                /* Glide.with(getApplicationContext())
                    .load(R.drawable.locationloader)
                    .into(gifImageView);*/

                btn_ok=(Button)dialog.findViewById(R.id.btn_ok);
                layout_indicator=(FrameLayout)dialog.findViewById(R.id.layout_indicator);

                mGeoDataClient = Places.getGeoDataClient(NavigationDrawerActivity.this, null);

                searched_address=(AutoCompleteTextView)dialog.findViewById(R.id.searched_address);
                //   searched_address.setText(sPreferences.getString("Location",""));
                searched_address.setOnItemClickListener(mAutocompleteClickListener);

                mAdapter = new PlaceAutocompleteAdapter(NavigationDrawerActivity.this, mGeoDataClient, BOUNDS_GREATER_SYDNEY,null);
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
        cartbadge = (Badge)findViewById(R.id.cartbadge);



      //  Toast.makeText(mainActivity, "CArt Count "+sPreferences.getString("CartCount",""), Toast.LENGTH_SHORT).show();
        mycart=(ImageView) findViewById(R.id.mycart);
        //cartbadge.setText(SQLData_Item.get(0).get("cartcount"));
        cartbadge.setText(sPreferences.getString("CartCount",""));
        mycart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationDrawerActivity.this,MyCartActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.bottom_up,
                        android.R.anim.fade_out);
                finish();
            }
        });
        fg_title.setText("Fish2Marine");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_action_menu);
        // getSupportActionBar().setIcon(R.mipmap.ic_action_menu);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.drawer_menus)
                .inject();
        layout_drawermenu=(LinearLayout)findViewById(R.id.layout_drawermenu);
        layout_drawermenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(slidingRootNav.isMenuOpened()){
                    slidingRootNav.closeMenu();
                }
                else {
                    slidingRootNav.openMenu();
                }
            }
        });

        fg_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(slidingRootNav.isMenuOpened()){
                    slidingRootNav.closeMenu();
                }
                else {
                    slidingRootNav.openMenu();
                }
            }
        });
        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        DrawerAdapter adapter = new DrawerAdapter(Arrays.asList(
                createItemFor(POS_HOME).setChecked(true),
                createItemFor(POS_ORDERHISTORY),
                createItemFor(POS_MYADDRESS),
                createItemFor(POS_MYREWARDS),
                createItemFor(POS_MYREFFERALS),
                createItemFor(POS_MYPROFILE),
                createItemFor(POS_CONTACTUS),
                createItemFor(POS_ABOUT),
                // new SpaceItem(28),
                createItemFor(POS_LOGOUT)));
        adapter.setListener(this);

        TextView customer_name=(TextView)findViewById(R.id.Customer_Name);
        String str = CustomerName;
        String[] splited = str.split("\\s+");
        customer_name.setText("Hi, "+splited[0]);
        RecyclerView list = (RecyclerView) findViewById(R.id.menulist);
        list.setNestedScrollingEnabled(true);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        adapter.setSelected(POS_HOME);

    }



    private void clearCart() {
        if(mConfig.isOnline(getApplicationContext())){
            UserLoginTask userLoginTask = new UserLoginTask(CustomerID);
            userLoginTask.execute((Void) null);

        }else {
            CustomToast.error(getApplicationContext(),"No Internet Connection.").show();
        }
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

    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.d("111111Place5", res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataChangeReceiver);
    }

    private BroadcastReceiver dataChangeReceiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // update your listview
            cartbadge.setText(sPreferences.getString("CartCount",""));
        }
    };

    @Override
    public void onItemSelected(int position) {
        switch (position){
            case POS_HOME:
                slidingRootNav.closeMenu();
                fg_title.setText("Fish2Marine");
                Fragment dashboard = new HomeFragment();
                showFragment(dashboard,"HOMEFRAGMENT");
                break;
            case POS_MYREWARDS:
                slidingRootNav.closeMenu();
                fg_title.setText("My Rewards");
                Fragment rewards = new MyRewardsFragment();
                showFragment(rewards,"MyRewards");
                break;
            case POS_MYREFFERALS:
                slidingRootNav.closeMenu();
                fg_title.setText("My Referals");
                Fragment referal = new MyReferalFragment();
                showFragment(referal,"MyRefereal");
                break;
            case POS_MYADDRESS:
                slidingRootNav.closeMenu();
                fg_title.setText("My Address");
                Fragment address = new MyAddressFragment();
                showFragment(address,"MyAddress");
                break;
            case POS_ORDERHISTORY:
                slidingRootNav.closeMenu();
                fg_title.setText("My Orders");
                Fragment orders = new MyOrdersListFragment();
                showFragment(orders,"MyOrder");
                break;
            case POS_MYPROFILE:
                slidingRootNav.closeMenu();
                fg_title.setText("My Profile");
                Fragment profile = new MyProfileFragment();
                showFragment(profile,"MyProfile");
                break;
            case POS_ABOUT:
                slidingRootNav.closeMenu();
                fg_title.setText("About Us");
                Fragment aboutus = new AboutUsFragment();
                showFragment(aboutus,"AboutUs");
                break;
            case POS_CONTACTUS:
                slidingRootNav.closeMenu();
                fg_title.setText("Contact Us");
                Fragment contactus = new ContactUsFragment();
                showFragment(contactus,"ContactUs");
                break;
            case POS_LOGOUT:
                logout();

                    break;
        }

    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        GoogleSignInClient mGoogleSignInClient;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getBaseContext(), gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(NavigationDrawerActivity.this,
                new OnCompleteListener<Void>() {  //signout Google
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseAuth.getInstance().signOut(); //signout firebase

                    }
                });

        SharedPreferences preferences =getSharedPreferences("prefs",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();


        Intent setupIntent = new Intent(getBaseContext(), LoginActivity.class);
        Toast.makeText(getBaseContext(), "Logged Out", Toast.LENGTH_LONG).show(); //if u want to show some text
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(setupIntent);
        finish();
    }

    private void showFragment(Fragment fragment,String tag) {


     /*   String backStateName = fragment.getClass().getName();

        boolean fragmentPopped = getSupportFragmentManager().popBackStackImmediate (backStateName, 0);

        if (!fragmentPopped) { //fragment not in back stack, create it.
*/

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, fragment, tag);
            fragmentTransaction.addToBackStack(tag);
            fragmentTransaction.commitAllowingStateLoss();
            subscreensOnTheStack++;
     //   }
       /* getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();*/
    }



    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.textColorPrimary))
                .withTextTint(color(R.color.textColorPrimary))
                .withSelectedIconTint(color(R.color.f2mcolor))
                .withSelectedTextTint(color(R.color.f2mcolor));
    }

    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.ld_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.ld_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @ColorInt
    private int color(@ColorRes int res) {

        return ContextCompat.getColor(this, res);
    }



    @Override
    public void LoadItem(int pos) {

    }

    @Override
    public void OpenDrawer() {
        slidingRootNav.openMenu();

    }


    public void updateCartCount(String count){
        cartbadge.setText(count);
    }

    @Override
    public void onBackPressed() {
        Fragment myFragment= getSupportFragmentManager().findFragmentByTag("HOMEFRAGMENT");
        if (myFragment != null && myFragment.isVisible()) {
            // add your code here
          //  super.onBackPressed();
            finish();
           // Toast.makeText(mainActivity, "current tag", Toast.LENGTH_SHORT).show();
        }
        else
        {
            slidingRootNav.closeMenu();
            fg_title.setText("Fish2Marine");
            Fragment dashboard = new HomeFragment();
            showFragment(dashboard,"HOMEFRAGMENT");
        }
    }

    /*@Override
        public void onBackPressed() {

            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {

                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                GoogleSignInClient mGoogleSignInClient;
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(getBaseContext(), gso);
                mGoogleSignInClient.signOut().addOnCompleteListener(NavigationDrawerActivity.this,
                        new OnCompleteListener<Void>() {  //signout Google
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseAuth.getInstance().signOut(); //signout firebase
                            }
                        });
                finish();
                Intent closepage = new Intent();
                closepage.setAction(Intent.ACTION_MAIN);
                closepage.addCategory(Intent.CATEGORY_HOME);
                startActivity(closepage);


            } else {
                CustomToast.info(getApplicationContext(),"Press again to exit").show();
            }
            mBackPressed = System.currentTimeMillis();
        }*/
    private void InitGetLocation(String Lat,String Long,String Location){
        Config mConfig = new Config(getApplicationContext());
        if(mConfig.isOnline(getApplicationContext())){
            LoadLocationInitiate mLoadLocationInitiate = new LoadLocationInitiate(Lat,Long,CustomerID,Location);
            mLoadLocationInitiate.execute((Void) null);
        }else {
            CustomToast.error(getApplicationContext(),"No Internet Connection.").show();
        }
    }


  /* public boolean onKeyDown(int keyCode, KeyEvent event)
    {

      //  if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            // this.moveTaskToBack(true);
            //return true;
            Fragment myFragment= getSupportFragmentManager().findFragmentByTag("HOMEFRAGMENT");
            if (myFragment != null && myFragment.isVisible()) {
                // add your code here
                Toast.makeText(mainActivity, "current tag", Toast.LENGTH_SHORT).show();
            }
        }

       // } else {
         //   CustomToast.info(getApplicationContext(),"Press again to exit").show();
       // }
      //  mBackPressed = System.currentTimeMillis();
        return super.onKeyDown(keyCode, event);
    }*/

    @Override
    public void userItemClick(int count) {

      //  Toast.makeText(mainActivity, "cart count "+count, Toast.LENGTH_SHORT).show();
        cartbadge.setText(""+count);

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
                            CustomToast.error(getApplicationContext(),"Please choose another location",Toast.LENGTH_SHORT).show();
                        }
                        else {
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

    public class UserLoginTask extends AsyncTask<Void, String, String> {

        JSONObject jsondata;
        private String id;

        UserLoginTask(String id) {
            this.id = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mConfig.savePreferences(getApplicationContext(),"id",
                    id.toString().trim());


        }

        @Override
        protected String doInBackground(Void... params) {

            String Status = "";
            HttpOperations httpOperations = new HttpOperations(getBaseContext());
            StringBuilder result = httpOperations.clearCart(id);
            Log.d("111111", "API_CLEAR_CART_RESPONSE " + result);
            Log.d("111111", "PASIING_VALUE " + id);

            try {

                JSONObject jsonObj = new JSONObject(result.toString());
                if (jsonObj.has("status")) {
                    if (jsonObj.getInt("status")==0) {
                        Status = "404";
                    }else if (jsonObj.getInt("status")==1) {

                       // SharedPreferences.Editor editor = sPreferences.edit();


                        Status="200";
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Status = "400";
            }catch (NullPointerException e) {
                e.printStackTrace();
                Status = "401";
            }catch (Exception e) {
                e.printStackTrace();
                Status = "400";
            }
            return Status;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals("404")){
                CustomToast.error(getApplicationContext(),"Invalid Data").show();
               // buttonVisible();
            }else if(result.equals("401")){
               // buttonVisible();
                CustomToast.error(getApplicationContext(),"No Internet Connection.").show();
            }else if(result.equals("400")){
              //  buttonVisible();
                CustomToast.info(getApplicationContext(),"Please try again").show();
            }else if(result.equals("200")){
                SharedPreferences sPreferences =getSharedPreferences("Fish2Marine",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sPreferences.edit();
                editor.remove("CartCount");
                //editor.putString("CartCount","0");
                editor.apply();
                String id=sPreferences.getString("CartCount","");
                cartbadge.clear();
               // Toast.makeText(NavigationDrawerActivity.this, "Cart cleared ", Toast.LENGTH_SHORT).show();
            }
        }
    }






}
