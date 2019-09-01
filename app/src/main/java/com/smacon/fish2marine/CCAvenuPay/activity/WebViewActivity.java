package com.smacon.fish2marine.CCAvenuPay.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.smacon.fish2marine.AdapterClass.places.logger.Log;
import com.smacon.fish2marine.CCAvenuPay.utility.AvenuesParams;
import com.smacon.fish2marine.CCAvenuPay.utility.Constants;
import com.smacon.fish2marine.CCAvenuPay.utility.LoadingDialog;
import com.smacon.fish2marine.CCAvenuPay.utility.RSAUtility;
import com.smacon.fish2marine.CCAvenuPay.utility.ServiceUtility;
import com.smacon.fish2marine.MyCartActivity;
import com.smacon.fish2marine.R;
import com.smacon.fish2marine.SuccessActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class WebViewActivity extends AppCompatActivity {
    Intent mainIntent;
    String encVal;
    String vResponse;
    String order_num="",access_code="",email="";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private static final String PREF_NAME = "prefs";
    private static final String KEY_USERNAME = "username";


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_webview);
        mainIntent = getIntent();
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        order_num=mainIntent.getStringExtra(AvenuesParams.ORDER_NUMBER);
        access_code=mainIntent.getStringExtra(AvenuesParams.ACCESS_CODE);
        email=sharedPreferences.getString(KEY_USERNAME,"");
        //get rsa key method
     //   Toast.makeText(this, "Access_Code "+mainIntent.getStringExtra(AvenuesParams.ACCESS_CODE), Toast.LENGTH_SHORT).show();
       // Toast.makeText(this, "Order_number "+mainIntent.getStringExtra(AvenuesParams.ORDER_NUMBER), Toast.LENGTH_SHORT).show();

        Log.e("11111","ACCESS_CODE "+mainIntent.getStringExtra(AvenuesParams.ACCESS_CODE));
        Log.e("11111","ORDER_NUMBER "+mainIntent.getStringExtra(AvenuesParams.ORDER_NUMBER));

        get_RSA_key(mainIntent.getStringExtra(AvenuesParams.ACCESS_CODE), order_num);
    }



    private class RenderView extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            LoadingDialog.showLoadingDialog(WebViewActivity.this, "Loading...");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            if (!ServiceUtility.chkNull(vResponse).equals("")
                    && ServiceUtility.chkNull(vResponse).toString().indexOf("ERROR") == -1) {
                StringBuffer vEncVal = new StringBuffer();
                vEncVal.append(ServiceUtility.addToPostParams(AvenuesParams.AMOUNT, mainIntent.getStringExtra(AvenuesParams.AMOUNT)));
                vEncVal.append(ServiceUtility.addToPostParams(AvenuesParams.CURRENCY, mainIntent.getStringExtra(AvenuesParams.CURRENCY)));
                encVal = RSAUtility.encrypt(vEncVal.substring(0, vEncVal.length() - 1), vResponse);  //encrypt amount and currency
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            LoadingDialog.cancelLoading();

            @SuppressWarnings("unused")
            class MyJavaScriptInterface {
                @JavascriptInterface
                public void processHTML(String html)
                {
                    // process the html as needed by the app
                    String status = null;
                    if(html.indexOf("Failure")!=-1){
                        status = "Transaction Declined!";
                    }else if(html.indexOf("Success")!=-1){
                        status = "Transaction Successful!";
                    }else if(html.indexOf("Aborted")!=-1){
                        status = "Transaction Cancelled!";
                    }else{
                        status = "Status Not Known!";
                    }
                    //Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(),StatusActivity.class);
                    intent.putExtra("transStatus", status);
                    startActivity(intent);
                    finish();
                }
            }

            final WebView webview = findViewById(R.id.webview);
            webview.getSettings().setJavaScriptEnabled(true);
            webview.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
            webview.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(webview, url);
                    Log.d("111111", "URL " + webview.getUrl());

                    //    Toast.makeText(WebViewActivity.this, "URL "+url+" WEBVIEW "+view.getUrl() , Toast.LENGTH_SHORT).show();
                    if(url.indexOf("payment/Index/success")!=-1) {
                        Log.d("URL", "11111 " + webview.getUrl());
                        Intent i = new Intent(WebViewActivity.this, SuccessActivity.class);
                        i.putExtra("ORDER_NUMBER", order_num);
                        //  i.putExtra("FROM_CHECKOUT", false);
                        startActivity(i);
                        finish();
                    }
                    else if(url.indexOf("payment/Index/cancel")!=-1)
                    {
                        Intent intent = new Intent(getApplicationContext(),StatusActivity.class);
                        intent.putExtra("transStatus", "Payment Cancelled");
                        startActivity(intent);
                        finish();
                    }
                    else if(url.indexOf("payment/Index/failed")!=-1)
                    {
                        Intent intent = new Intent(getApplicationContext(),StatusActivity.class);
                        intent.putExtra("transStatus", "Payment Failed");
                        startActivity(intent);
                        finish();
                    }

                    else if(url.indexOf("/ccavResponseHandler.jsp")!=-1){
                           Toast.makeText(WebViewActivity.this, "here2", Toast.LENGTH_SHORT).show();
                        webview.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                    }


                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Toast.makeText(getApplicationContext(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
                }
            });

            try {


                String postData = AvenuesParams.ACCESS_CODE + "=" + URLEncoder.encode(access_code, "UTF-8") + "&" +
                        AvenuesParams.MERCHANT_ID + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.MERCHANT_ID), "UTF-8") + "&" +
                        AvenuesParams.ORDER_ID + "=" + URLEncoder.encode(order_num, "UTF-8") + "&" +
                        AvenuesParams.ORDER_NUMBER + "=" + URLEncoder.encode(order_num, "UTF-8") + "&" +
                        AvenuesParams.REDIRECT_URL + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.REDIRECT_URL), "UTF-8") + "&" +
                        AvenuesParams.CANCEL_URL + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.CANCEL_URL), "UTF-8") + "&" +
                        AvenuesParams.BILLING_NAME + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.BILLING_NAME), "UTF-8") + "&" +
                        AvenuesParams.BILLING_ADDRESS + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.BILLING_ADDRESS), "UTF-8") + "&" +
                        AvenuesParams.BILLING_ZIP + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.BILLING_ZIP), "UTF-8") + "&" +
                        AvenuesParams.BILLING_CITY + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.BILLING_CITY), "UTF-8") + "&" +
                        AvenuesParams.BILLING_STATE + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.BILLING_STATE), "UTF-8") + "&" +
                        AvenuesParams.BILLING_COUNTRY + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.BILLING_COUNTRY), "UTF-8") + "&" +
                        AvenuesParams.BILLING_TEL + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.BILLING_TEL), "UTF-8") + "&" +
                        AvenuesParams.BILLING_EMAIL + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.BILLING_EMAIL), "UTF-8") + "&" +
                        AvenuesParams.ENC_VAL + "=" + URLEncoder.encode(encVal, "UTF-8");
             //   Log.d("111111","POST_DATA "+postData);
                webview.postUrl(Constants.TRANS_URL, postData.getBytes());
            } catch (UnsupportedEncodingException e) {
                Log.d("111111","POST_DATA_Error "+e.getMessage());

                e.printStackTrace();
            }

        }
    }




    public void get_RSA_key(final String ac, final String od) {


        LoadingDialog.showLoadingDialog(WebViewActivity.this, "Loading...");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, mainIntent.getStringExtra(AvenuesParams.RSA_KEY_URL),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                       // Toast.makeText(WebViewActivity.this,response,Toast.LENGTH_LONG).show();
                        LoadingDialog.cancelLoading();

                        if (response != null && !response.equals("")) {
                            vResponse = response;     ///save retrived rsa key

                            Log.d("111111","RSA_KEY "+response);


                            if (vResponse.contains("!ERROR!")) {
                                show_alert(vResponse);
                            } else {
                                new RenderView().execute();   // Calling async task to get display content
                            }


                        }
                        else
                        {
                            show_alert("No response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("111111","Errror "+error.getMessage());
                        LoadingDialog.cancelLoading();
                        //Toast.makeText(WebViewActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(AvenuesParams.ACCESS_CODE, ac);
                params.put(AvenuesParams.ORDER_NUMBER, od);
                Log.d("111111","PASSING VALUE "+params);
                return params;
            }

        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void show_alert(String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(
                WebViewActivity.this).create();

        alertDialog.setTitle("Error!!!");
        if (msg.contains("\n"))
            msg = msg.replaceAll("\\\n", "");

        alertDialog.setMessage(msg);



        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });


        alertDialog.show();
    }
    @Override
    public void onBackPressed() {
       // super.onBackPressed();
        Intent i=new Intent(getApplicationContext(), MyCartActivity.class);
        startActivity(i);
        finish();
    }
}