package en.test.hpfs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CanvasShow extends Activity {
	/** Called when the activity is first created. */
	private static int inc = 0;
	private TextView text;
	private ImageView view;
	CanvasManager manager;
	private static final String TAG = "HPFS";

	private final Runnable runMain = new Runnable() {
		public void run() {
			main();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.canvasshow);
		text = (TextView) findViewById(R.id.textview);
		view = (ImageView) findViewById(R.id.canvas);
		Bundle bundle = getIntent().getExtras();
		manager = new CanvasManager(view, bundle.getString("numPoints"),
				bundle.getString("numClusters"),
				bundle.getString("numDimensions"),
				bundle.getString("seed"));
		(new Handler()).post(runMain);

		Button close = (Button) findViewById(R.id.button1);
		close.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				System.exit(0);

			}
		});
	}

	private synchronized void main() {
		
		if (manager.updateCanvas())
 		new Handler().post(runMain);
		else {

		    AlertDialog alertDialog = new AlertDialog.Builder(this).create();  
		    alertDialog.setTitle("Status");  
		    alertDialog.setMessage("Calculations Completed");  
		    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {  
		      public void onClick(DialogInterface dialog, int which) {  
		        return;  
		    } });
		    alertDialog.show();
		}
	}

}
