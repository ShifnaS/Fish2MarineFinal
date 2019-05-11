package com.smacon.fish2marine.CCAvenuPay.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.smacon.fish2marine.MyCartActivity;
import com.smacon.fish2marine.R;


public class StatusActivity extends AppCompatActivity {
	private SharedPreferences sPreferences;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_status);

		Intent mainIntent = getIntent();
		TextView tv4 = findViewById(R.id.textView1);
		sPreferences = getSharedPreferences("Fish2Marine", MODE_PRIVATE);

		tv4.setText(mainIntent.getStringExtra("transStatus"));
		Button bt_ok=findViewById(R.id.ok);
		bt_ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				SharedPreferences.Editor editor = sPreferences.edit();
				editor.remove("CartCount");
				editor.apply();

				Intent i=new Intent(getApplicationContext(), MyCartActivity.class);
				startActivity(i);
				finish();

			}
		});


	}
	
	public void showToast(String msg) {
		Toast.makeText(this, "Toast: " + msg, Toast.LENGTH_LONG).show();
	}

} 