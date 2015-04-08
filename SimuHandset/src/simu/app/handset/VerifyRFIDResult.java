package simu.app.handset;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * * @author 作者 E-mail: * @date 创建时间：2015-3-31 上午11:10:21 *
 * 
 * @version 1.0 * @parameter * @since * @return
 * */
public class VerifyRFIDResult extends SherlockActivity {
	EditText etResult = null;
	Button verifyAgain = null;
	MenuItem back = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.verify_rfid_result);
		etResult = (EditText) findViewById(R.id.result);
		findViewById(R.id.verifyagain).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent it = new Intent(VerifyRFIDResult.this, VerifyRFID.class);
				finish();
				startActivity(it);
			}
		});
		

		showResult();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent it = new Intent(VerifyRFIDResult.this, CroutonDemo.class);
		startActivity(it);
		finish();
	}

	private void showResult() {
		etResult.setText(getIntent().getStringExtra("message"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.verify_menu, menu);
		findViewById(R.menu.verify_menu).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Toast.makeText(VerifyRFIDResult.this,
								"the back button has been clickid !!",
								Toast.LENGTH_SHORT).show();

					}
				});
		return super.onCreateOptionsMenu(menu);
	}
}
