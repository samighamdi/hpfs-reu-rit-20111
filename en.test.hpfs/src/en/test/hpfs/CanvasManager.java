package en.test.hpfs;

import java.io.IOException;
import java.net.UnknownHostException;

import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

public class CanvasManager implements OnTouchListener {

	private ImageView view;
	private TextView text;
	ConnectionManager man;

	public CanvasManager(ImageView view, String numPoints, String numClusters,
			String numDimensions, String seed) {
		this.view = view;
		try {
			man = new ConnectionManager("reu1.cs.rit.edu", 9996);
			man.sendText(numPoints);
			man.sendText(numClusters);
			man.sendText(numDimensions);
			man.sendText(seed);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		view.setOnTouchListener(this);
	}

	public boolean updateCanvas() {
		try {
			byte[] h = man.receiveBytes(9);
			String sizeString = new String(h);
			Log.v("TAG", "Char: " + (int)sizeString.charAt(0));
			int size = Integer.parseInt(sizeString);
			if (size > 0) {
				man.receiveBytes(1);
				byte[] imageBytes = man.receiveBytes(size);
				view.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0,
						imageBytes.length));
				view.postInvalidate();
				return true;
			}
			else return false;
		} catch (IOException e) {
			return false;
		}

	}

	private void sendPosition(int x, int y) {

	}

	public boolean onTouch(View v, MotionEvent e) {
		sendPosition((int) e.getX(), (int) e.getY());
		return true;
	}

}
