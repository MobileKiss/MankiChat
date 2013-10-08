package by.mobilekiss.mankichat;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class Redraw extends View{

	int tmp;
	int height = 0;
	static int width = 0; 
	static Bitmap bm;
	
	byte[] BackgroudnArr = null;
	
	static String text = "";
	int y = -1;
	static int ty = -1;
	boolean slide = false;
	static Paint mPaint;
	
	boolean front = false;
	
	public Redraw(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
		mPaint.setTextAlign(Align.CENTER);
		mPaint.setTextSize(35);
		mPaint.setTypeface(FontFactory.getIntro(context));
		mPaint.setColor(Color.parseColor("#FFFFFF"));		
	}

	public void setFront(boolean f)	{
		front = f;
	}
	
	protected void onDraw(Canvas c){
		super.onDraw(c);	
		
		if ((bm == null)||(CameraActivity.newPicture)){
			
 			height = this.getMeasuredHeight();
			width = this.getMeasuredWidth();
			float aRatio = (float)width/height;
			
			bm = BitmapFactory.decodeByteArray(CameraActivity.GetByteArr(), 0, CameraActivity.GetByteArr().length);
			
			int bWidth = bm.getWidth();
			int bHeight = bm.getHeight();
			float baRatio = (float)bHeight/bWidth;
			
			int rHeight = bHeight;
			int rWidth = (int)(bHeight/aRatio);
			
			if (baRatio>aRatio)
			{
				rWidth = bWidth;
				rHeight = (int)(bWidth*aRatio);
			}
			
			int rX = (bWidth-rWidth)/2;
			int rY = (bHeight-rHeight)/2;

			Matrix m = new Matrix();
			//m.setScale(0.1f, 0.1f);
			m.setRotate(front?(-90):90);
			
			m.preScale((float)height / rWidth, (float)width / rHeight);
			
			if (front)
				m.postScale(-1, 1);
			
			//m.setScale(height, width);
			
			bm = Bitmap.createBitmap(bm, rX, rY, rWidth, rHeight, m, false);
			//bm = bm.copy(Bitmap.Config.ARGB_8888, true);
			CameraActivity.newPicture = false;
			y = ty = -1;
			text = "";
		}
		c.drawBitmap(bm, 0, 0, null);		
		if (y>-1){
			//c.drawCircle(100, y, 4, mPaint);
			c.drawText(text, (width/2), y, mPaint);
		}
		else if (ty>-1){
			//c.drawCircle(100, y, 4, mPaint);
			c.drawText(text, (width/2), ty, mPaint);
		}
	}
	
	public Canvas GetCanvas(){
		
		return new Canvas(bm);
	}
	
	public static boolean getPicture(ByteArrayOutputStream strm)
	{
		if (ty>-1)
		{
			Canvas c = new Canvas(bm);
			c.drawText(text, (width/2), ty, mPaint);
			Rect r = new Rect();
			
		}
		
		return bm.compress(CompressFormat.JPEG, 100, strm);
	}
	
	
			
}
	
	