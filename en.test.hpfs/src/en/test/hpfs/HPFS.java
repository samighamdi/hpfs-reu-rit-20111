package en.test.hpfs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class HPFS extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		final HPFS self = this;
		
		Button cont = (Button) findViewById(R.id.button1);
		cont.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClassName(self, CanvasShow.class.getName());
				Bundle bundle = new Bundle();
				String numPoints = ((EditText)findViewById(R.id.editText1)).getText().toString();
				bundle.putString("numPoints", (numPoints != null)? numPoints : "0");
				String numClusters = ((EditText)findViewById(R.id.editText2)).getText().toString();
				bundle.putString("numClusters", (numClusters != null)? numClusters : "0");
				String numDimensions = ((EditText)findViewById(R.id.editText3)).getText().toString();
				bundle.putString("numDimensions", (numDimensions != null)? numDimensions : "0");
				String seed = ((EditText)findViewById(R.id.editText4)).getText().toString();
				bundle.putString("seed", (seed != null)? seed : "0");
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

}