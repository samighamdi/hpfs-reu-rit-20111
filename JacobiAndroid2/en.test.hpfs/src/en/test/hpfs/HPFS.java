package en.test.hpfs;

import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Rect;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class HPFS extends Activity {
   /** Called when the activity is first created. */
	private static int inc = 0;
	private TextView text;
	private ImageView view;
	CanvasManager manager;
   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.main);
		text = (TextView)findViewById(R.id.textview);
		((TextView)findViewById(R.id.textview1)).setText("Touch Location");
		ImageView view = (ImageView)findViewById(R.id.canvas);
	       manager = new CanvasManager(view,
		   "http://server.daquanne.com/images.php", "http://server.daquanne.com/", (TextView)findViewById(R.id.textview1));
	    (new Handler()).postDelayed(runMain, 20);
       
       
       
//       try
//	   	{
//			String str;
//	   		HttpClient hc = new DefaultHttpClient();
//	   		HttpPost post = new HttpPost("http://www.syngames.net");
//	
//	   		HttpResponse rp = hc.execute(post);
//	   		
//	
//			//text.setText(rp.getStatusLine().getStatusCode());
//	   		
//	   		if(rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
//	   		{
//	   			str = EntityUtils.toString(rp.getEntity());
//	   			text.setText(str);
//	   		}
//	   	}
//			catch(IOException e){
//	   		e.printStackTrace();
//	   	}
       
   }
   
   private final Runnable runMain = new Runnable() {
	    public void run() {
	        main();
//			try {
//				Thread.sleep(16);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

	    }
	};
	   
//	   private final Runnable runCount = new Runnable() {
//		    public void run() {
////				try {
////					Thread.sleep(16);
////				} catch (InterruptedException e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				}
//					if (inc < 300) {
//					(new Handler()).postDelayed(runMain, 16);
//					}
//
//		    }
//		};
	
	private synchronized void main() {
	       text.setText("Frame Number " + ++inc);
	       manager.updateCanvas();
	       //text.setText("" + manager.getWidth());

			if (inc < 300) {
				(new Handler()).postDelayed(runMain, 16);
			}
	}
   
   
}