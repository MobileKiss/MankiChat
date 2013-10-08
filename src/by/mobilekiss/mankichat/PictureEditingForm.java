package by.mobilekiss.mankichat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


public class PictureEditingForm extends Activity implements OnTouchListener, OnSeekBarChangeListener, OnClickListener, TextWatcher{
	
		final int DIALOG = 1;
		float screenDencity = 0;
		SeekBar TimerSeekBar;
		RelativeLayout RLPrepost;
		EditText etAddText = null;
		boolean isDrawing = false;
	    private by.mobilekiss.mankichat.Redraw preview;
	    ImageView ivTimer, btnDrawing, btnSending, btnClose;	    
	    TextView tvTimer, tvTimer_dialog, tvTimer_static;
	    Canvas canvas = null;
	    Paint mPaint;
		Rect Textrect = null;
		boolean isMoved = false;
	    int x, y, lx = -1, ly = -1, TimerCount = 10, DialogButtonId = 0;
	    boolean draw = false, etTextIsMoved = false;
	
	    private ColorGenerator generator = new ColorGenerator();
	    
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.prepost);
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = 
			        new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			}
		mPaint = new Paint();
		mPaint.setDither(true);
		mPaint.setAntiAlias(true);
		screenDencity = Resources.getSystem().getDisplayMetrics().density;
		//mPaint.setColor(Color.RED);
		float pencilWidth = (float)(8 * screenDencity + 0.5);
		mPaint.setStrokeWidth(pencilWidth);

		/*Display display = getWindowManager().getDefaultDisplay(); 
		int width = display.getWidth();  // deprecated
		int height = display.getHeight();  // deprecated
		*/
		preview = (by.mobilekiss.mankichat.Redraw) findViewById(R.id.vPrepost);
		preview.setFront(getIntent().getBooleanExtra("front", false));
		preview.setOnTouchListener(this);
		
		TimerSeekBar = (SeekBar) findViewById(R.id.prepost_seekbar);
		//TimerSeekBar.getLayoutParams().height = Math.round(height/15);
		//TimerSeekBar.getLayoutParams().width = Math.round(width/9);
		TimerSeekBar.setOnSeekBarChangeListener(this);
		
		ivTimer = (ImageView) findViewById(R.id.prepost_ivTimer_icon);
		//ivTimer.getLayoutParams().height = Math.round(height/15);
		//ivTimer.getLayoutParams().width = Math.round(width/9);
		ivTimer.setOnTouchListener(this);
		
		btnClose = (ImageView) findViewById(R.id.prepost_btnClose);
		//btnClose.getLayoutParams().height = Math.round(height/15);
		//btnClose.getLayoutParams().width = Math.round(width/9);
		btnClose.setOnClickListener(this);
		
		btnDrawing = (ImageView) findViewById(R.id.btnDrawing);
		//btnDrawing.getLayoutParams().height = Math.round(height/15);
		//btnDrawing.getLayoutParams().width = Math.round(width/9);        
		btnDrawing.setOnClickListener(this);
		
		btnSending = (ImageView) findViewById(R.id.btnSending);
		//btnSending.getLayoutParams().height = Math.round(height/9);
		//btnSending.getLayoutParams().width = Math.round(width/5);
        
		btnSending.setOnClickListener(this);
		
		tvTimer = (TextView) findViewById(R.id.tvTimer);
		tvTimer.setTypeface(FontFactory.getUbuntuBold(this));
		tvTimer.setText(Integer.toString(TimerCount) + " SEC");
		tvTimer.setOnTouchListener(this);
		
		tvTimer_dialog = (TextView) findViewById(R.id.prepost_form_timer_dialog_textView);
		tvTimer_dialog.setTypeface(FontFactory.getUbuntuBold(this));
		tvTimer_dialog.setText("10 SEC");
		
		tvTimer_static = (TextView) findViewById(R.id.prepost_form_timer_tvstatic);
		tvTimer_static.setTypeface(FontFactory.getUbuntuBold(this));
		
		RLPrepost = (RelativeLayout) findViewById(R.id.FLPrepost);
		RLPrepost.setOnClickListener(this);		
	}

	int tly=-1;
	int ny = -1;
	boolean ed = true; 
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		canvas = preview.GetCanvas();
		
		switch (v.getId()){
		
			case R.id.tvTimer:
			
				switch ( event.getAction()){
			
					case MotionEvent.ACTION_DOWN:		
						
						tvTimer_dialog.setVisibility(View.VISIBLE);
						tvTimer_static.setVisibility(View.VISIBLE);
						TimerSeekBar.setVisibility(View.VISIBLE);
						ivTimer.setVisibility(View.INVISIBLE);
						
						return true;
			
					case MotionEvent.ACTION_MOVE:			
										
						return true;
			
					case MotionEvent.ACTION_UP:
						
						return true;			
					
				}
				
				break;
	
		
		case R.id.prepost_ivTimer_icon:
				
				switch ( event.getAction()){
				
				case MotionEvent.ACTION_DOWN:		
					tvTimer_dialog.setVisibility(View.VISIBLE); 
					tvTimer_static.setVisibility(View.VISIBLE);
					TimerSeekBar.setVisibility(View.VISIBLE);
					ivTimer.setVisibility(View.INVISIBLE);
					
					return true;
		
				case MotionEvent.ACTION_MOVE:			
									
					return true;
		
				case MotionEvent.ACTION_UP:
					
					return true;			
				
			}
				
				break;
								
		case R.id.vPrepost:
			
			if (isDrawing == false){

				switch ( event.getAction()){
				
				case MotionEvent.ACTION_DOWN:			
					
					y = (int)event.getY();
					
					tly = y;
					ed = true;
					if (y<preview.ty + 20 && y>preview.ty - 45)
					{	
						preview.y = y;
						preview.slide = true;
						preview.invalidate();
					}
					return true;
		
				case MotionEvent.ACTION_MOVE:
					if (preview.slide)
					{
						y = (int)event.getY();
						if (Math.abs(y-tly)>10)
							ed = false;
						preview.y = y;
						preview.invalidate();
					}
					return true;
		
				case MotionEvent.ACTION_UP:
					y = (int)event.getY();
					if (preview.slide)
					{
						preview.y = -1;
						preview.slide = false;
						if (ed)
						{
							ny = -1;
							CreateEditText(preview.ty-25);
						}
						else
							preview.ty = y;
						preview.invalidate();
					}
					else if (preview.ty==-1)
					{
						int ys = (int)event.getY();
						int hs = (int)preview.height;
						if ((ys<hs*0.3)||(ys>hs*0.8))
							return true;
						ny = y;
						CreateEditText(preview.height/2);
					}
					return true;			
				}				
			}
			if (isDrawing == true){	
			
				switch ( event.getAction()){
			
					case MotionEvent.ACTION_DOWN:			
						
						draw = true;
						x = (int)event.getX(); 
						y = (int)event.getY();
						lx = x;
						ly = y;
						mPaint.setColor(generator.getColor());
						//mPaint.setShader(new LinearGradient(lx, ly, x, y, 0xffffffff, 0xff000000, android.graphics.Shader.TileMode.CLAMP));
						canvas.drawCircle(x, y, (float)(4 * screenDencity + 0.5), mPaint);
						preview.invalidate();
						return true;
			
					case MotionEvent.ACTION_MOVE:			
						x = (int)event.getX(); 
						y = (int)event.getY();
						if (draw == true){
							int scl = generator.getColor();
							generator.addLen((float)Math.sqrt((lx-x)*(lx-x)+(ly-y)*(ly-y)));
							int ecl = generator.getColor();
 						    mPaint.setShader(new LinearGradient(lx, ly, x, y, scl, ecl, android.graphics.Shader.TileMode.CLAMP));
							canvas.drawLine(lx, ly, x, y, mPaint);
							canvas.drawCircle(x, y, (float)(4 * screenDencity + 0.5), mPaint);
							lx = x;
							ly = y;						
						}
						preview.invalidate();
						return true;
			
					case MotionEvent.ACTION_UP:
						draw = false;
						x = (int)event.getX(); 
						y = (int)event.getY();
						int scl = generator.getColor();
						generator.addLen((float)Math.sqrt((lx-x)*(lx-x)+(ly-y)*(ly-y)));
						int ecl = generator.getColor();
						mPaint.setShader(new LinearGradient(lx, ly, x, y, scl, ecl, android.graphics.Shader.TileMode.CLAMP));
						canvas.drawLine(lx, ly, x, y, mPaint);
						canvas.drawCircle(x, y, (float)(4 * screenDencity + 0.5), mPaint);
						lx = -1;
						ly = -1;
						preview.invalidate();
						return true;			
					}
				}
			break;
			}
			return false;
		
	}
		

	@Override
	public void onClick(View v) {
				 
		switch(v.getId()){
		
		case R.id.prepost_btnClose:
			finish();
			break;
		
		case R.id.btnSending:
			
			Intent SendIntent = new Intent(this, PostPhoto.class);
			SendIntent.putExtra("timer", String.valueOf(TimerCount));
			startActivityForResult(SendIntent, 2);
			break;		        	
	    	    	
		case R.id.btnDrawing:
			if (isDrawing == true){
				isDrawing = false;
				btnDrawing.setBackgroundResource(R.drawable.prepost_draw);
				break;
			}
			else{
				isDrawing = true;
				btnDrawing.setBackgroundResource(R.drawable.prepost_draw_ok);
				break;
			}
			
		
		}
		
	}
	
	 @Override
	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		 if (resultCode == RESULT_OK)
			 finish();
				
	  }
	 
	 
	 @Override
	 public void onTextChanged(CharSequence s, int start, int count, int after)
	 {
		 if (after==1)
		 {
			 char ch = s.charAt(start);
			 if (ch=='\n')
			 {
				 String ss = s.toString().replaceAll("\n", "");
				 fix(ss);
			 }
		 }
	 }
	 
	 EditText EdtText = null;
	 
	 boolean opened = false;
	 
	 public void CreateEditText(int MoveY){
		 if (opened)
			return;
 	    opened = true;
		RelativeLayout.LayoutParams LParamsEditText = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		LParamsEditText.leftMargin = 0;
		LParamsEditText.topMargin = MoveY;
		EdtText = new EditText(this);
		EdtText.setText(preview.text);
		EdtText.setGravity(0x11);
		EdtText.setTextColor(Color.parseColor("#7A6ACC"));
		EdtText.setTypeface(FontFactory.getIntro(this));
		//EdtText.setBackgroundResource(R.drawable.prepost_text_background);
		EdtText.setHint("Нажмите Enter что бы сохранить");
		EdtText.addTextChangedListener(this);
		RLPrepost.addView(EdtText, LParamsEditText);
		EdtText.requestFocus();
		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.showSoftInput(EdtText, 0);
	 }

	 public void fix(String s)
	 {
		 InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		 if (imm != null)
			imm.hideSoftInputFromWindow(EdtText.getWindowToken(), 0);
		 RLPrepost.removeView(EdtText);
		 EdtText = null;
		 preview.text = s;
		 if (s=="")
			 preview.ty = -1;
		 else if (ny>-1)
			 preview.ty = ny;
		 preview.invalidate();
		 opened = false;
	 }

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onBackPressed(){
		
		if (EdtText != null){
			
			fix("");			
		}
		
		else{
			
			super.onBackPressed();
			
		}
	}


	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		
		TimerCount = progress + 1;
		
		tvTimer.setText(String.valueOf(TimerCount) + " SEC");
		tvTimer_dialog.setText(String.valueOf(TimerCount) + " SEC");
		
	}


	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
		tvTimer.setText(String.valueOf(TimerCount) + " SEC");
	}


	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
		tvTimer_dialog.setVisibility(View.INVISIBLE);
		tvTimer_static.setVisibility(View.INVISIBLE);
		TimerSeekBar.setVisibility(View.INVISIBLE);
		ivTimer.setVisibility(View.VISIBLE);
		
	}
	
}
