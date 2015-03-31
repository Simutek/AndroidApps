package simu.app.handset;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
/** * @author  作者 E-mail:
 *  * @date 创建时间：2015-3-31 上午11:10:21 * 
 *  @version 1.0 
 *  * @parameter  
 *  * @since  
 *  * @return 
 *   */
public class VerifyRFIDResult extends SherlockActivity {
	EditText etResult;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.verify_rfid_result);
		etResult = (EditText) findViewById(R.id.result);
		
		showResult();
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		Intent it = new Intent(VerifyRFIDResult.this, CroutonDemo.class);
		startActivity(it);
	}
	private void showResult() {
		etResult.setText(getIntent().getStringExtra("message"));
	}

}
