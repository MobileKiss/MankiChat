package by.mobilekiss.mankichat;

import android.app.Activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;


public class LicenseActivity extends Activity implements OnClickListener{	

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.license_form);
		
		Display display = getWindowManager().getDefaultDisplay(); 
		int width = display.getWidth();  // deprecated
		int height = display.getHeight();  // deprecated
		
		ImageView back = (ImageView) findViewById(R.id.l_form_back);
		//back.getLayoutParams().height = Math.round(height/15);
		//back.getLayoutParams().width = Math.round(width/10);
		back.setOnClickListener(this);
		
		TextView BannerNameLicensing  = (TextView) findViewById(R.id.license_tvBanner);
		BannerNameLicensing.setTypeface(FontFactory.getUbuntuBold(this));
		
		WebView wvLicense = (WebView) findViewById(R.id.license_webView);
        wvLicense.loadUrl("file:///android_asset/License.html");
		
		
	}

	@Override
	public void onClick(View v) {
		
		finish();
		
	}
	
}