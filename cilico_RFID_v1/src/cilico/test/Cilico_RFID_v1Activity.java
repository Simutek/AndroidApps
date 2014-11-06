package cilico.test;

import cilico.tools.I2CTools;
import android.app.Activity;
import android.os.Bundle;

import java.text.SimpleDateFormat; 
//import android.os.Bundle;
//import android.os.SystemClock;

import java.util.Date;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import simu.database.AssetsDatabaseManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;  

public class Cilico_RFID_v1Activity extends Activity {
	/** Called when the activity is first created. */
	String strUI = "";
	TextView resultView_MF1;
	Button findCard, clear, verify;
	SQLiteDatabase db;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		setContentView(R.layout.main);
		findCard = (Button) findViewById(R.id.findCard);
		resultView_MF1 = (TextView) findViewById(R.id.resultView_MF1);
		clear = (Button) findViewById(R.id.clear);
		verify = (Button) findViewById(R.id.verify);
		*/
		
		AssetsDatabaseManager.initManager(getApplication());
		AssetsDatabaseManager mg = AssetsDatabaseManager.getManager(); 
		db = mg.getDatabase("SimuLocal.db");

		showToast(String.valueOf(I2CTools.readVersion()));
		showToast(I2CTools.ReadUID());
	}
	
	@Override
	public void onDestroy()
	{
		AssetsDatabaseManager.closeAllDatabase();
		super.onDestroy();
	}
	
	public void onFindCardClick(View v) {
		// TODO Auto-generated method stub
		if (VerifyTag()){
			String cid = ReadCID();

			if (cid.length() == 8) {
				db.execSQL("UPDATE smallTags SET productionDate=Date() WHERE CID=?", new String[]{String.valueOf(cid)});
				
				strUI += ("登记成功，UID : " + cid + "\n\n");
			} else {
				strUI += ("登记失败，请重试！\n\n");
			}
		}
		else
		{
			strUI += ("感应失败，或者为非法标签，请重试！\n\n");
		}
		
		resultView_MF1.setText(strUI);
	}
	
	public void onVerifyClick(View v) {
		// TODO Auto-generated method stub
		if (VerifyTag()){
			String cid = ReadCID();
			Cursor c = db.rawQuery("SELECT * FROM smallTags WHERE CID=?", new String[]{String.valueOf(cid)});
			while(c.moveToNext())
			{
				String lDate = c.getString(c.getColumnIndex("ProductionDate"));
				if (lDate.length() == 4)
					lDate = "尚未登记入库";
				//Date productDate = new Date(lDate);
				//SimpleDateFormat formatter = new SimpleDateFormat ("yyyy年MM月dd日");	    
				//String strDate = formatter.format(productDate);  
			       
				//Date curDate = new Date(System.currentTimeMillis());//获取当前时间   
				strUI += ("电子标签验证成功：\n" + "厂家：四川中兴机械\n" + "生产日期：" + lDate + "\n\n");
				break;
			}
		} else {
			strUI += ("警告：感应失败，或该标签不是思木科技认证的合法防伪标签，请留意伪造风险！" + "\n\n");
		}
		
		resultView_MF1.setText(strUI);
	}
	
	public void onClearClick(View v) {
		// TODO Auto-generated method stub
		strUI = "";

		resultView_MF1.setText("");
	}
	
	private boolean VerifyTag()
	{
		String mimaStr = "CC13250B1222";
		byte[] passw = I2CTools.stringToBytes(mimaStr);
		String address = "60";

		byte[] buffer = new byte[16];
		int add = Integer.valueOf(address);
		int t;

		t = I2CTools.ReadBlock(buffer, passw, (byte) 0x60,
				(byte) add);
		
		return t==0;
	}
	
	private String ReadCID()
	{
		String t = I2CTools.ReadUID();
		String cid = "";
		if (t.length() == 8) {
			cid = t.substring(6, 8);
			cid = cid + t.substring(4, 6);
			cid = cid + t.substring(2, 4);
			cid = cid + t.substring(0, 2);
			cid = cid.toUpperCase();
		}
		return cid;
	}

	private void showToast(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}

}