package by.mobilekiss.mankichat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class OverscrolledListView extends ListView implements OnScrollListener {

	public View showView = null;
	public MessagesForm form = null;
	
	private int firstView = 0;
	private final static int DOUBLE_TAP = 2;
	private final static int SINGLE_TAP = 1;
	private final static int DELAY = ViewConfiguration.getDoubleTapTimeout();
	private boolean mTookFirstEvent=false;
    private int mPositionHolder=-1;
	private int mPosition=-1;
	private OnItemDoubleTapLister mOnDoubleTapListener = null;
	private AdapterView<?> mParent = null;
    private View mView = null;
    private long mId= 12315;
    private Message mMessage = null;
    private static String TAG = "DoubleTapListView";
    private Handler mHandler = new Handler(){
    	
    	@Override
    	public void handleMessage(Message msg)
    	{
    		super.handleMessage(msg);
    		
    		switch(msg.what)
    		{
    		case SINGLE_TAP:
    			Log.i(TAG, "Single tap entry");
    			 mOnDoubleTapListener.OnSingleTap(mParent, mView, mPosition, mId);
    			 break;
    		case DOUBLE_TAP:
    			Log.i(TAG, "Double tap entry");
    			 mOnDoubleTapListener.OnDoubleTap(mParent, mView, mPosition, mId);
    			break;
    		}
    	}
    	
    };
    
    
  /*  public void MessagesListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		removeSelector();
	}

	public void MessagesListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		removeSelector();
	}

	public void MessagesListView(Context context) {
		super(context);
		removeSelector();//optional
	}*/

    
    public void setOnItemDoubleClickListener(OnItemDoubleTapLister listener )
	{
		mOnDoubleTapListener = listener;
		/*If the listener is null then throw exception*/
		if(mOnDoubleTapListener==null)
			throw new IllegalArgumentException("OnItemDoubleTapListener cannot be null");
		else
		{
			/*If the OnItemDoubleTapListener is not null, 
			 * register the default onItemClickListener to proceed with listening */
		setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	            mParent = parent;
	            mView = view;
	            mPosition = position;
	            mId=id;
	            if(!mTookFirstEvent) /* Testing if first tap occurred */
	            {
	                mPositionHolder=position;
	                /*this will hold the position variable from first event. 
	                 * In case user presses any other item (position)*/
	                mTookFirstEvent=true;
	            	mMessage = mMessage == null? new Message() : mHandler.obtainMessage();
	                /*"Recycling" the message, instead creating new instance we get the old one */
	            	mHandler.removeMessages(SINGLE_TAP);
	                mMessage.what = SINGLE_TAP;
	                mHandler.sendMessageDelayed(mMessage, DELAY);
	            
	            }
	            else
	            { 
		            if(mPositionHolder == position)
		            {
		            	mHandler.removeMessages(SINGLE_TAP);
		            	 
		                mPosition = position;
		                mMessage = mHandler.obtainMessage(); 
		               /*obtaining old message instead creating new one */
		                mMessage.what=DOUBLE_TAP;
		                mHandler.sendMessageAtFrontOfQueue(mMessage);
		                
		                mTookFirstEvent=false;
		               
		              }
			            else
			            {
			            	
			            	mMessage = mHandler.obtainMessage();
			            	mHandler.removeMessages(SINGLE_TAP);
			            	mTookFirstEvent=true;
			            	mMessage.what = SINGLE_TAP;
			            	mPositionHolder = position;
				            mHandler.sendMessageDelayed(mMessage, DELAY);
			            }
	            }
				
			}});
		}
	}
	
	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
			boolean clampedY) {
		if (clampedY&&(firstView==0))
		{
			LinearLayout lo = (LinearLayout)getParent();
			if (lo.getChildCount()==2)
			{
				lo.addView(showView, 1);
				lo.invalidate();
				form.InvokeGetNewMessages();
			}
		}
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
	}

	public OverscrolledListView(Context context) {
		super(context);
		setOnScrollListener(this);
	}

	public OverscrolledListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnScrollListener(this);
	}

	public OverscrolledListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		setOnScrollListener(this);
	}
	
	public void hideUpdater()
	{
		LinearLayout lo = (LinearLayout)getParent();
		if (lo.getChildCount()==3)
		{
			lo.removeViewAt(1);
			lo.invalidate();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		firstView = firstVisibleItem;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
	
	public void removeSelector()
	{
		setSelector(android.R.color.transparent); // optional
		//TODO solution for double tap selector needed.
		
	}
	
	public interface OnItemDoubleTapLister
	{
		public void OnDoubleTap(AdapterView<?> parent, View view, int position,
				long id);
		public void OnSingleTap(AdapterView<?> parent, View view, int position,
				long id);
	}
	
}
