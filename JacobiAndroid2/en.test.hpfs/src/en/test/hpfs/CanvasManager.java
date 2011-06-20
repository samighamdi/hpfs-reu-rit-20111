package en.test.hpfs;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

public class CanvasManager implements OnTouchListener {

	private ImageView view;
	private String rawURL;
	private String cordURL;
	private TextView text;
	private int width;
	private int height;

	public CanvasManager(ImageView view, String rawURL, String cordURL, TextView text) {
		this.text = text;
		this.view = view;
		view.setOnTouchListener(this);
		this.rawURL = rawURL;
		this.cordURL = cordURL;
	}
	
	public void setURL(String rawURL) {
		this.rawURL = rawURL;
	}

	public void updateCanvas() {
		view.setImageDrawable(requestImage());
	}

	private synchronized Drawable requestImage() {
		Drawable image = null;
		try {
			/*URL url = new URL(rawURL);

			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setReadTimeout(10000);
			connection.setConnectTimeout(15000);
			connection.setRequestMethod("GET");
			//connection.setDoInput(true);
			connection.connect();

			InputStream input = connection.getInputStream();*/
			URL url = new URL(rawURL);
			InputStream input = (InputStream)url.getContent();
			//Drawable d = Drawable.createFromStream(is, "src");

			//BufferedInputStream inputBuf = new BufferedInputStream(input);
			image = Drawable.createFromStream(input, "src");
			
			width = image.getIntrinsicWidth();
			height = image.getIntrinsicWidth();
			//inputBuf.close();
			input.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			return requestImage();
		}

		return image;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	private void sendPosition(int x, int y) {
		URL url;
		try {
			String cord = cordURL + "?x=" + x + "&y=" + y;
			text.setText("Touch Positions   X : " + x + " , Y : " + y);
			url = new URL(cord);
			url.getContent();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean onTouch(View v, MotionEvent e) {
		sendPosition((int)e.getX(), (int)e.getY());
		return true;
	}
	
	
}
