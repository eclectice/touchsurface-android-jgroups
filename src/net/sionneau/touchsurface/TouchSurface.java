/* This is a port of the JGroups Draw demo
 * by Yann Sionneau <yann.sionneau@telecom-sudparis.eu>
 */

package net.sionneau.touchsurface;

//import org.jgroups.*;
//import org.jgroups.util.Util;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
//import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
//import android.view.View;

public class TouchSurface extends Activity {

	/** Called when the activity is first created. */

	Bitmap buffer;
	Canvas surface;
	Paint	paint;
	MySurface view;
	int	tool;
	MulticastLock lock;

	//static private Channel channel = null;
	static private String groupname = "draw-cluster";
	static private String name = null;
	static boolean no_channel = false;
	static boolean jmx = false;
	private static boolean use_state = false;
	//private static long state_timeout = 5000;
	private static boolean use_unicasts = false;
	private Draw draw = null;
	private String bind_addr_str = "null";
	String group_name=TouchSurface.groupname;

	static {
		System.setProperty("java.net.preferIPv4Stack" , "true");
		System.setProperty("java.net.preferIPv6Stack", "false");
		System.setProperty("java.net.preferIPv4Addresses", "true");
		System.setProperty("java.net.preferIPv6Addresses ", "false");

	}         
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		Log.i("Touchsurface", "onCreate()");
		
		String os = System.getProperty("java.vm.name");
		Log.i("Touchsurface", os);

		//if (draw == null)
		//Log.e("TouchSurface", "draw == null");
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		//get wifi bind ip address to avoid confusion
		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifi.getConnectionInfo();
		int bind_addr = wifiInfo.getIpAddress();
		Locale locale = Locale.getDefault();
		bind_addr_str = String.format(locale,"%d.%d.%d.%d", 
				(bind_addr & 0xff), 
				(bind_addr >> 8 & 0xff),
				(bind_addr >> 16 & 0xff),
				(bind_addr >> 24 & 0xff)
				);
		Log.i("TouchSurface", bind_addr_str);
		System.setProperty("jgroups.bind_addr", bind_addr_str);
		
		super.onCreate(savedInstanceState);
		
		view = new MySurface(this);
		setContentView(view);
	}		

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i("touchsurface", "onKeyDown()");
		Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		vib.vibrate(50);

		if (keyCode == KeyEvent.KEYCODE_MENU) {
			paint.setColor(0xFFFFFFFF);
			surface.drawPaint(paint);
			view.invalidate();
			draw.sendClearPanelMsg();
		}
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		
		//this.finish();
		return true;
	}

	public class MySurface extends SurfaceView implements SurfaceHolder.Callback {

		SurfaceHolder holder;

		MySurface(Context context) {
			super(context);
			holder = getHolder();
			holder.addCallback(this);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float cx = event.getX();
			float cy = event.getY();

			switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:

				Paint p = new Paint();
				p.setColor(0xFF0000FF);
				
				if (draw == null)
					Log.e("TouchSurface", "draw == null");
				else
					draw.TouchEvent(cx, cy);
				
				surface.drawPoint(cx, cy, p);
			}


			view.invalidate();
			return true;
		}

		@Override
		public void invalidate() {
			if(holder!=null){
				Canvas c = holder.lockCanvas();	
				if(c!=null){	
					c.drawBitmap(buffer,0,0,null);
					holder.unlockCanvasAndPost(c);	
				}
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (draw != null) setTitle(draw.setTitle("Draw", bind_addr_str));
		}

		public void surfaceCreated(SurfaceHolder holder) {
			String props="assets/udp.xml";
			boolean no_channel=TouchSurface.no_channel;
			boolean jmx=TouchSurface.jmx;
			boolean use_state=TouchSurface.use_state;
			boolean use_unicasts=TouchSurface.use_unicasts;
			String name=TouchSurface.name;
			long state_timeout=5000;

			buffer = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
			surface = new Canvas(buffer);
			paint = new Paint();


			WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			lock = wifi.createMulticastLock("mylock");
			lock.setReferenceCounted(true);
			lock.acquire();

			paint.setColor(0xFFFFFFFF);
			surface.drawPaint(paint);
			view.invalidate();
			
			//since we want to create the surface, make sure draw is created to open the channel!
			//no attempt is made to save the last draw surface session
			Log.i("TouchSurface", "surfaceCreated()");
			try {
				draw = new Draw(props, no_channel, jmx, use_state, state_timeout, use_unicasts, name, surface, view);
				draw.setGroupName(group_name);
				draw.go();
				String title = draw.setTitle("Draw", bind_addr_str); 
				setTitle(title);
			}
			catch(Throwable e) {
				Log.e("TouchSurface", "draw.go()");
				draw.stop();
				e.printStackTrace();
				System.exit(0);
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			//since we want to destroy this surface, make sure draw is released too to close the channel!
			//if not, the unreleased idle channel will merged into the new draw surface session!
			//no attempt is made to save the last draw surface session
			Log.i("TouchSurface", "surfaceDestroyed()");
			draw.stop();
		}

	}

}