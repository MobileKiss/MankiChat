package by.mobilekiss.mankichat;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

//
public class CameraActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener, Camera.PictureCallback, Camera.PreviewCallback, Camera.AutoFocusCallback {

    public static boolean newPicture = false;
    static byte[] Background = null;

    SharedPreferences PInfo;
    ImageView shotBtn, btnMessages, btnDoubleSelection, switchCam;
    boolean PictureTaken = false;
    private Camera camera = null;
    private boolean hasSurface;

    private int cameraId = 0;
    
    public static byte[] GetByteArr() {
        return Background;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        /*Display display = getWindowManager().getDefaultDisplay(); 
		int width = display.getWidth();  // deprecated
		int height = display.getHeight();  // deprecated
		*/
        
        PInfo = getSharedPreferences("PrivateInfo", MODE_PRIVATE);
        String FirstStart = PInfo.getString("first_start", "");
        String owner_id = PInfo.getString("user_id", "");

        shotBtn = (ImageView) findViewById(R.id.btnTakePicture);
        //shotBtn.getLayoutParams().height = Math.round(height/5);
        //shotBtn.getLayoutParams().width = Math.round(width/3);
        shotBtn.setOnClickListener(this);

        btnMessages = (ImageView) findViewById(R.id.btnMessages);
        //btnMessages.getLayoutParams().height = Math.round(height/(float)8.2);
        //btnMessages.getLayoutParams().width = Math.round(width/5);
        btnMessages.setOnClickListener(this);
        
        btnDoubleSelection = (ImageView) findViewById(R.id.btnDoubleSelection); 
        //btnDoubleSelection.getLayoutParams().height = Math.round(height/15);
        //btnDoubleSelection.getLayoutParams().width = Math.round(width/9);
        btnDoubleSelection.setOnClickListener(this);

        ImageView btnFriends = (ImageView) findViewById(R.id.btnFriendsContactList);
        //btnFriends.getLayoutParams().height = Math.round(height/12);
        //btnFriends.getLayoutParams().width = Math.round(width/7);
		
        btnFriends.setOnClickListener(this);

        switchCam = (ImageView) findViewById(R.id.btnCamChange);
        //switchCam.getLayoutParams().height = Math.round(height/12);
        //switchCam.getLayoutParams().width = Math.round(width/7);
		
        switchCam.setOnClickListener(this);
        if (Camera.getNumberOfCameras()<2)
        	switchCam.setVisibility(View.INVISIBLE);
        else
        	switchCam.setVisibility(View.VISIBLE);
        
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

    }

	public void CameraSettings(){
	    Camera.Parameters CamParameters = camera.getParameters();
		List<Camera.Size> ListSizes = CamParameters.getSupportedPictureSizes();
		Size previewSize = CamParameters.getPreviewSize();
        
		int previewSurfaceWidth = previewSize.width;
        int previewSurfaceHeight = previewSize.height;
        Size optimalCamSize = null;
        int MinimalDistance = -1;
        
        for (Camera.Size ListSize : ListSizes){
        	
        	int PictureHeight = ListSize.height;
        	int PictureWidth = ListSize.width;
        	if (PictureWidth >= previewSurfaceWidth && PictureHeight >= previewSurfaceHeight){
        		
        		int HamingsDistance = Math.abs((PictureWidth - previewSurfaceWidth)+(PictureHeight - previewSurfaceHeight));
        		if (MinimalDistance == -1 || HamingsDistance < MinimalDistance){
        			MinimalDistance = HamingsDistance;
        			optimalCamSize = ListSize;
        		}
        	}
        }
        if (MinimalDistance > -1){
    		CamParameters.setPictureSize(optimalCamSize.width, optimalCamSize.height);
    		camera.setParameters(CamParameters);
    	}	
	}    
    
    @Override
    public void onStart() {
        super.onStart();

        String FirstStart = PInfo.getString("first_start", "");
        
        FlurryAgent.onStartSession(this, "QPHX3MTZZ4CZ3HRWJZJW");
        
        if (!isMessangerServiceRunning() && FirstStart.contentEquals("NO")) {

            startService(new Intent(this, MessangerService.class));

        }
    }

    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    public void onDestroy() {
    	
        super.onDestroy();
        if (isMessangerServiceRunning())
        	stopService(new Intent(this, MessangerService.class));;
       
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnMessages:
                Intent intentMessages = new Intent(this, MessagesForm.class);
                startActivity(intentMessages);
                break;
                
            case R.id.btnDoubleSelection:
            	btnDoubleSelection.setVisibility(View.INVISIBLE);
            	SharedPreferences PInfo = getSharedPreferences("PrivateInfo",MODE_PRIVATE);
    		    PInfo.edit().putString("double_click_selId", "").commit();	
    		    PInfo = null;
    		    btnDoubleSelection.requestLayout();
            	btnMessages.setVisibility(View.VISIBLE);
            	break;
                
            case R.id.btnTakePicture:
              
               String focusMode = camera.getParameters().getFocusMode();
               if (!PictureTaken)
            	   if (focusMode.contentEquals(Parameters.FOCUS_MODE_AUTO)||focusMode.contentEquals(Parameters.FOCUS_MODE_MACRO))
            	   {
            		   shotBtn.setEnabled(false);
            		   camera.autoFocus(this);
            	   }
            	   else
            		   onAutoFocus(true, camera);
                break;
            case R.id.btnFriendsContactList:
                Intent FriendContactList = new Intent(this, MyFriends.class);
                startActivity(FriendContactList);
                break;
            case R.id.btnCamChange:
            	cameraId++;
            	switchCam.setClickable(false);
            	if (cameraId>=Camera.getNumberOfCameras())
            		cameraId = 0;
                SurfaceView preview = (SurfaceView) findViewById(R.id.SurfaceViewMain);
                SurfaceHolder surfaceHolder = preview.getHolder();
            	initCamera(surfaceHolder);
            	switchCam.setClickable(true);
            	break;
        }
    }    
    
    protected void onResume() {
        super.onResume();
        
        getResources().flushLayoutCache();

        if (PInfo.getString("double_click_selId", "").contentEquals("")){
        
        	btnDoubleSelection.setVisibility(View.INVISIBLE);
        	btnMessages.setVisibility(View.VISIBLE);
        	
	        DBHelper dbHelper = new DBHelper(this);		
	        SQLiteDatabase db = dbHelper.getReadableDatabase(); 
	        String[] owner_id = { PInfo.getString("user_id", "") };
	        
			String tri = PInfo.getString("triangle", "0");
	        Cursor c = db.rawQuery("SELECT * FROM messages_table WHERE is_view=0 AND from_id!=? AND (id>(SELECT MAX(id) FROM messages_table)-20)", owner_id);
			if (tri.contentEquals("1")||(c.getCount() != 0))
				btnMessages.setBackgroundResource(R.drawable.photo_form_message_new);
				
			else
				btnMessages.setBackgroundResource(R.drawable.photo_form_messages);
			
			c.close();
			db.close();
        }
        else {
        	btnMessages.setVisibility(View.INVISIBLE);
        	btnDoubleSelection.setVisibility(View.VISIBLE);
        }
        //camera and surface launch
        SurfaceView preview = (SurfaceView) findViewById(R.id.SurfaceViewMain);
        SurfaceHolder surfaceHolder = preview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        PictureTaken = false;
        String FirstStart = PInfo.getString("first_start", "");

        //server connection
        String api_key = PInfo.getString("api_key", "");
        String user_id = PInfo.getString("user_id", "");
        String api_key_access = PInfo.getString("api_key_access", "");

        if (api_key.contentEquals("") || user_id.contentEquals("") || api_key_access.contentEquals("")) {
        	if (FirstStart.contentEquals("NO"))
        		Toast.makeText(this, "Пожалуйста, войдите в приложение еще раз", Toast.LENGTH_SHORT).show();

        	Intent intensRegistration = new Intent(this, LoginingForm.class);
            startActivityForResult(intensRegistration, 0);
        }

        

        if (!isMessangerServiceRunning() && FirstStart.contentEquals("NO")) {
            startService(new Intent(this, MessangerService.class));
        }

    }

    private void initCamera(SurfaceHolder holder) {
        try {
        	if (camera!=null)
        	{
        		camera.stopPreview();
        		camera.release();
        	}
            camera = Camera.open(cameraId);
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            CameraConfiguration.initFromCameraParameters(camera, this);
            CameraSettings();
            camera.startPreview();
        } catch (IOException e) {
            //TODO something here
        }
    }

    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void onAutoFocus(boolean paramBoolean, Camera paramCamera) {
    	
    	if (paramBoolean)
        {
    		camera.takePicture(null, null, null, this);
            PictureTaken = true;
            shotBtn.setEnabled(true);
        }
    	else 
    		shotBtn.setEnabled(true);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    public void onPictureTaken(byte[] paramArrayOfByte, Camera paramCamera) {

        Intent intent = new Intent(this, PictureEditingForm.class);
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        intent.putExtra("front", cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT);
        Background = paramArrayOfByte;
        newPicture = true;
        startActivity(intent);

    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        hasSurface = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) {
            finish();
        }
        
       
    }

    private boolean isMessangerServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MessangerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
