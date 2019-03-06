package com.smacon.fish2marine;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.smacon.f2mlibrary.CustomToast;
import com.smacon.f2mlibrary.Edittext.MaterialEditText;
import com.smacon.f2mlibrary.Progress.AVLoadingIndicatorView;
import com.smacon.fish2marine.HelperClass.SqliteHelper;
import com.smacon.fish2marine.Util.Config;
import com.smacon.fish2marine.Util.HttpOperations;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private MaterialEditText mFirstname,mLastname,mMobile,mEmail,mNewPassword,mConfirmpassword;
    TextView login;
    CheckBox subscribe;
    private Button mRegister;
    private ImageView mClose;
    ToggleButton mpwdtoggle1,mpwdtoggle2;
    private Config mConfig;
    Handler mHandler;
    private ProgressDialog progress;
    private AVLoadingIndicatorView loading;
    int subchecked=0;

    private SharedPreferences sPreferences;
    private SqliteHelper helper;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private static final String PREF_NAME = "prefs";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASS = "password";
    //google
    private static final int RC_SIGN_IN = 234; //a constant for detecting the login intent result
    private static final String TAG = "1111111"; //Tag for the logs optional
    GoogleSignInClient mGoogleSignInClient; //creating a GoogleSignInClient object
    FirebaseAuth mAuth;     //And also a Firebase Auth object

    //facebook
    private LoginButton loginButton;
    private CallbackManager callbackManager,mCallbackManager;
    private FirebaseAuth.AuthStateListener firebaseAuthListner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();

        helper = new SqliteHelper(getApplicationContext(), "Fish2Marine", null, 5);
        sPreferences = getSharedPreferences("Fish2Marine", MODE_PRIVATE);
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mHandler = new Handler();
        mConfig = new Config(getApplicationContext());
        FacebookSdk.sdkInitialize(this.getApplicationContext());

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                Toast.makeText(getApplicationContext(),"Cancel",Toast.LENGTH_SHORT).show();
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                CustomToast.error(SignUpActivity.this,""+error.getMessage());

                // Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
                // ...
            }
        });

        //Then we need a GoogleSignInOptions object
        //And we need to build it as below
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //Then we will get the GoogleSignInClient object from GoogleSignIn class
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


        InitIdView();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        //if the requestCode is the Google Sign In code that we defined at starting
        if (requestCode == RC_SIGN_IN) {

            //Getting the GoogleSignIn Task
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                //authenticating with firebase
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.d("111111",task.getException().getMessage());
                Log.d("111111",e.getMessage());

                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void handleFacebookAccessToken(AccessToken token) {
        GraphRequest request = GraphRequest.newMeRequest(token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.v("LoginActivity", response.toString());
                        try {

                            String fullname = object.getString("name");
                            String[] nameparts = fullname.split("\\s+");
                            String firstname = nameparts[0];
                            String lastname = nameparts[1];

                            if (mConfig.isOnline(SignUpActivity.this)) {
                                showProgress();

                                UserSocialLoginTask userSocialLoginTask = new UserSocialLoginTask(object.getString("email"),"", firstname, lastname, object.getString("id"),"facebook");
                                userSocialLoginTask.execute((Void) null);
                                editor.putString(KEY_USERNAME, object.getString("email"));
                                editor.apply();

                            } else {
                                CustomToast.error(getApplicationContext(),"No Internet Connection.").show();
                            }

                            Toast.makeText(SignUpActivity.this, "User Signed In " +fullname, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        LoginManager.getInstance().logOut();
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }





    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        //getting the auth credential
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        //Now using firebase we are signing in the user here
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            showProgress();
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            String fullname=user.getDisplayName();
                            String[] nameparts = fullname.split("\\s+");
                            String firstname = nameparts[0];
                            String lastname = nameparts[1];

                            if (mConfig.isOnline(SignUpActivity.this)) {

                                UserSocialLoginTask userSocialLoginTask = new UserSocialLoginTask(user.getEmail(),"",firstname,lastname,user.getUid(),"google");
                                userSocialLoginTask.execute((Void) null);
                                editor.putString(KEY_USERNAME, user.getEmail());
                                editor.apply();

                            } else {

                                CustomToast.error(getApplicationContext(),"No Internet Connection.").show();
                            }

                            Toast.makeText(SignUpActivity.this, "User Signed In"+user.getDisplayName(), Toast.LENGTH_SHORT).show();
                            //  startActivity(new Intent(LoginActivity.this, GoogleLoginProfileActivity.class));

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //   CustomToast.error(LoginActivity.this,""+task.getException());
                            CustomToast.error(SignUpActivity.this, "Authentication failed.").show();

                        }

                    }
                });
    }

    //this method is called on click
    private void signIn() {
        //getting the google signin intent
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        //starting the activity for result
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void showProgress(){
        progress=new ProgressDialog(SignUpActivity.this);
        progress.setMessage("Loading..");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
    }


    private void InitIdView(){
        subscribe=findViewById(R.id.subscribe);
        mpwdtoggle1=(ToggleButton)findViewById(R.id.mpwdtoggle1);
        mpwdtoggle2=(ToggleButton)findViewById(R.id.mpwdtoggle2);

        login=findViewById(R.id.login);
        mFirstname = (MaterialEditText)findViewById(R.id.firstname);
        mLastname = (MaterialEditText)findViewById(R.id.lastname);
        mMobile = (MaterialEditText)findViewById(R.id.mobile);
        mEmail = (MaterialEditText)findViewById(R.id.email);
        mNewPassword = (MaterialEditText)findViewById(R.id.newpassword);
        mConfirmpassword = (MaterialEditText)findViewById(R.id.confirmpassword);
        mRegister = (Button) findViewById(R.id.register);
        mClose = (ImageView) findViewById(R.id.close);
        loading = (AVLoadingIndicatorView) findViewById(R.id.indicator);
        loading.setVisibility(View.GONE);
        mRegister.setOnClickListener(this);
        mClose.setOnClickListener(this);
        login.setOnClickListener(this);
        mpwdtoggle1.setChecked(true);
        mpwdtoggle2.setChecked(true);

        mpwdtoggle1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    mNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else{
                    mNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                }
            }
        });   mpwdtoggle2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    mConfirmpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else{
                    mConfirmpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int buttonId=v.getId();
        switch (buttonId){
            case R.id.close:
                if (loading.getVisibility() == View.VISIBLE) {
                    CustomToast.info(getApplicationContext(), "Please wait while we process your request").show();
                } else {
                    //close
                    onBackPressed();
                }
                break;
            case R.id.register:
                if(subscribe.isChecked())
                {
                    subchecked=1;
                }
                else
                {
                    subchecked=0;
                }
                if (loading.getVisibility() == View.VISIBLE) {
                    CustomToast.info(getApplicationContext(), "Please wait while we process your request").show();
                } else {
                    if (mFirstname.getText().toString().trim().equals("")) {

                        mFirstname.setError("Field cannot be blank");
                    } else if (mLastname.getText().toString().trim().equals("")) {

                        mLastname.setError("Field cannot be blank");
                    } else if (mMobile.getText().toString().trim().equals("")) {

                        mMobile.setError("Field cannot be blank");
                    } else if (mEmail.getText().toString().trim().equals("")) {

                        mEmail.setError("Field cannot be blank");
                    } else if (mNewPassword.getText().toString().trim().equals("")) {

                        mNewPassword.setError("Field cannot be blank");
                    } else if (mConfirmpassword.getText().toString().trim().equals("")) {
                        mConfirmpassword.setError("Field cannot be blank");

                    } else if (!mConfirmpassword.getText().toString().trim().equals(mNewPassword.getText().toString().trim())) {
                        mConfirmpassword.setError("Password do not match");
                    } else {
                        UserRegistration();
                    }
                }
                break;
            case R.id.login:
               Intent i=new Intent(getApplicationContext(),LoginActivity.class);
               startActivity(i);
               finish();
                break;

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SignUpActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void UserRegistration(){
        Config mConfig = new Config(getApplicationContext());
        if(mConfig.isOnline(getApplicationContext())){
            LoadUserRegistrationInitiate mLoadUserRegistrationInitiate = new LoadUserRegistrationInitiate(subchecked,
                    mFirstname.getText().toString().trim(),
                    mLastname.getText().toString().trim(),
                    mMobile.getText().toString().trim(),
                    mEmail.getText().toString().trim(),
                    mNewPassword.getText().toString().trim());
            mLoadUserRegistrationInitiate.execute((Void) null);
        }else {
            CustomToast.error(getApplicationContext(),"No Internet Connection.").show();
        }
    }
    public class LoadUserRegistrationInitiate extends AsyncTask<Void, StringBuilder, StringBuilder> {

        private String mfirstname,mlastname,mmobile,memail,mnewpassword;

        LoadUserRegistrationInitiate(int subchecked, String firstname, String lastname, String mobile, String email, String password) {
            mfirstname = firstname;
            mlastname = lastname;
            mmobile = mobile;
            memail = email;
            mnewpassword = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setVisibility(View.VISIBLE);
            mRegister.setVisibility(View.INVISIBLE);
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {

            HttpOperations httpOperations = new HttpOperations(getApplicationContext());
            StringBuilder result = httpOperations.doUserRegistration(subchecked,mfirstname, mlastname, mmobile,memail,mnewpassword);
            return result;
        }

        @Override
        protected void onPostExecute(StringBuilder result) {
            super.onPostExecute(result);
            loading.setVisibility(View.GONE);
            mRegister.setVisibility(View.VISIBLE);
            try {
                JSONObject jsonObj = new JSONObject(result.toString());
                Log.d("111111111",result.toString());
                if (jsonObj.has("status")) {
                    if (jsonObj.getString("status").equals(String.valueOf(3))) {
                        mEmail.setText("");
                        CustomToast.error(getApplicationContext(),jsonObj.getString("message")).show();
                    }else if (jsonObj.getString("status").equals(String.valueOf(2))) {
                        mNewPassword.setText("");
                        mConfirmpassword.setText("");
                        mNewPassword.setError(jsonObj.getString("message"));
                        //CustomToast.error(getApplicationContext(),jsonObj.getString("message")).show();
                    } else if (jsonObj.getString("status").equals(String.valueOf(1))) {
                        mFirstname.setText("");
                        mLastname.setText("");
                        mMobile.setText("");
                        mEmail.setText("");
                        mNewPassword.setText("");
                        mConfirmpassword.setText("");
                        CustomToast.success(getApplicationContext(),"You have successfully registered").show();
                        onBackPressed();
                        /*Intent intent = new Intent(SignUpActivity.this,EditProfileActivity.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in,
                                R.anim.bottom_down);
                        finish();*/
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


    public class UserSocialLoginTask extends AsyncTask<Void, String, String> {

        JSONObject jsondata;
        private String mEmail,mMobile,mFirstname,mLastname,mUserid,mMode;

        UserSocialLoginTask(String email, String mobile,String firstname, String lastmane,
                            String userid, String mode) {
            mEmail = email;
            mMobile = mobile;
            mFirstname = firstname;
            mLastname = lastmane;
            mUserid = userid;
            mMode = mode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            helper.Delete_admin_details();
        }

        @Override
        protected String doInBackground(Void... params) {

            String Status = "";
            HttpOperations httpOperations = new HttpOperations(getBaseContext());
            StringBuilder result = httpOperations.doSocialLogin(mEmail,mMobile,mFirstname,mLastname,mUserid,mMode);
            Log.d("111111", "API__SOCIAL_LOGIN " + result);
            try {

                JSONObject jsonObj = new JSONObject(result.toString());
                if (jsonObj.has("status")) {
                    if (jsonObj.getString("status").equals(String.valueOf(2))) {
                        Status = "404";
                    }else if (jsonObj.getString("status").equals(String.valueOf(1))) {
                        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
                        JSONObject jsonObj1 = jsonObj.getJSONObject("data");
                        for (int i = 0; i < jsonObj1.length(); i++) {

                            HashMap<String, String> map;
                            map = new HashMap<String, String>();
                            map.put("customerId", jsonObj1.getString("customerId").trim());
                            map.put("customerName", jsonObj1.getString("customerName").trim());
                            map.put("mobile", jsonObj1.getString("mobile").trim());
                            map.put("email", jsonObj1.getString("email").trim());
                            fillMaps.add(map);

                        }
                        helper.Insert_admin_details(fillMaps);
                        final List<HashMap<String, String>> Data_Item;
                        Data_Item = helper.getadmindetails();
                        mConfig.savePreferences(getApplicationContext(),"ID",Data_Item.get(0).get("admin_id"));
                        mConfig.savePreferences(getApplicationContext(),"NAME",Data_Item.get(0).get("admin_name"));
                        Log.d("1111111",Data_Item.get(0).get("admin_id"));
                        Log.d("1111111",Data_Item.get(0).get("admin_name"));
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
                progress.dismiss();
                CustomToast.error(getApplicationContext(),"Invalid Username or Password").show();
                //buttonVisible();
            }else if(result.equals("401")){
                //buttonVisible();
                progress.dismiss();
                CustomToast.error(getApplicationContext(),"No Internet Connection.").show();
            }else if(result.equals("400")){
                //buttonVisible();
                progress.dismiss();
                CustomToast.info(getApplicationContext(),"Please try again").show();
            }else if(result.equals("200")){
                progress.dismiss();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences.Editor editor = sPreferences.edit();
                                editor.remove("DeliveryCenter_ID");
                                editor.remove("CartCount");
                                editor.remove("Location");
                                editor.apply();
                                Intent intent = new Intent(SignUpActivity.this,NavigationDrawerActivity.class);
                                intent.putExtra("PAGE","LOGIN");
                                startActivity(intent);
                                /*overridePendingTransition(R.anim.bottom_up,
                                        android.R.anim.fade_out);*/
                                finish();
                            }
                        }, 1000);
                    }
                }, 1000);
            }
        }


    }


  /*  private void managePrefs(){
            editor.putString(KEY_USERNAME, mUsename.getText().toString().trim());
            editor.putString(KEY_PASS, mPassword.getText().toString().trim());
            editor.putBoolean(KEY_REMEMBER, true);
            editor.apply();

    }*/
}