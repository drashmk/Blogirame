package com.datanasov.custom;

import com.medo.blogirame.Reader;
import com.medo.blogirame.utils.Constants;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;

public class ZeWebView extends WebView {
	
	private boolean sendGesture = false;
	private float x, y, startX, startY = 0;
	private Context context;
	
	public ZeWebView(Context context) {
	    super(context);
	    this.context = context;
	}

	public ZeWebView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    this.context = context;
	}

	public ZeWebView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    this.context = context;
	}

	@Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean consumed = super.onTouchEvent(event);
        int action = event.getAction();	
        switch (action) {
		case MotionEvent.ACTION_MOVE:		
			x = event.getX();
			y = event.getY();
			//if two fingers are in last move then we have two finger gesture
			sendGesture = event.getPointerCount() > 1  ? true : false;
			break;
		case MotionEvent.ACTION_DOWN:
			startX = event.getX();
			startY = event.getY();
			break;
		case MotionEvent.ACTION_UP:
			if(sendGesture)
				fling(startX, x, startY, y);
			break;
		default: break;
		}

        return consumed || isClickable();
    }

   
    
    /*
	 * Gesture detection
	 */
	public void fling(float startX, float x, float startY, float y) {
		
		    //Down swipe  											left/right off tolerance
			if (y - startY > Constants.SWIPE_MIN_DISTANCE && Math.abs(startX - x) < Constants.SWIPE_MAX_OFF_PATH ) {
	           ((Reader)this.context).finish();
	        }
	}

}
