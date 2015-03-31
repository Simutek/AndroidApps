package simu.app.handset;

import java.util.Timer;
import java.util.TimerTask;

import simu.database.AssetsDatabaseManager;

import cilico.tools.I2CTools;
import de.keyboardsurfer.android.widget.crouton.Style;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Administrator
 * 
 */
public class VerifyRFID extends Activity {

	Timer timer;
	private SQLiteDatabase db;
	int closeTimerTickCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rfid_verify);
		AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();
		db = mg.getDatabase("SimuLocal.db");
	}

	@Override
	protected void onResume() {
		super.onResume();
		beginToverity();
	}

	private void beginToverity() {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				Message message = new Message();
				handler.sendMessage(message);
			}
		};
		if (null != timer) {
			timer.cancel();
			timer.purge();
		}
		timer = new Timer(true);
		timer.schedule(task, 1500, 2000);
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			String cid = ReadCID(true);
			if (cid.length() == 8) {
				timer.cancel();
				Verify(cid);
			} else {
				if (closeTimerTickCount++ > 10) {
					timer.cancel();
					Toast.makeText(VerifyRFID.this, "操作超时，请重新验证 !!!",
							Toast.LENGTH_SHORT).show();
					PlayNotification();
					Intent it = new Intent(VerifyRFID.this, CroutonDemo.class);
					finish();
					startActivity(it);
				}
			}
		}
	};

	private String ReadCID(boolean isSilent) {
		String t = I2CTools.ReadUID();
		String cid = "";
		if (t.length() == 8) {
			cid = t.substring(6, 8);
			cid = cid + t.substring(4, 6);
			cid = cid + t.substring(2, 4);
			cid = cid + t.substring(0, 2);
			cid = cid.toUpperCase();
		} else if (!isSilent) {
			String croutonText = "没有感应到思木电子标签\n请将手持机靠近标签重试";
			// ShowCrouton(Style.ALERT, croutonText);
		}
		Log.d(null, "the cid has been returned !! cid.length = " + cid.length());
		return cid;
	}

	private void Verify(String cid) {
		if (VerifyTag()) {
			Cursor c = db.rawQuery("SELECT * FROM RFID WHERE cid=?",
					new String[] { String.valueOf(cid) });
			if (c.moveToNext()) {
				String rfidObjectId = c.getString(c.getColumnIndex("objectId"));
				Cursor c1 = db.rawQuery("SELECT * FROM Product WHERE rfid=?",
						new String[] { String.valueOf(rfidObjectId) });
				if (c1.getCount() <= 0) {
					// ShowCrouton(Style.INFO, "电子标签验证成功：\n厂家：四川中兴机械\n该标签尚未登记");
					Toast.makeText(VerifyRFID.this, "电子标签验证成功",
							Toast.LENGTH_SHORT).show();
					PlayNotification();
					sendResult("电子标签验证成功：\n厂家：四川中兴机械\n该标签尚未登记");
				} else {
					if (c1.moveToNext()) {
						String pDate = c1.getString(c1
								.getColumnIndex("productDate"));
						String eDate = c1.getString(c1
								.getColumnIndex("factoryDate"));
						String pType = c1.getString(c1
								.getColumnIndex("categoryName"));
						String pDateDes = (pDate == null
								|| pDate.equals("NULL") || pDate.equals("null")) ? "尚未登记入库"
								: pDate;
						String eDateDes = (eDate == null
								|| eDate.equals("NULL") || eDate.equals("null")) ? "尚未登记出库"
								: eDate;
						String pTypeDes = (pType == null
								|| pType.equals("NULL") || pType.equals("null")) ? "尚未登记类别"
								: pType;
						// ShowCrouton(Style.INFO,
						// String.format("电子标签验证成功：\n厂家：四川中兴机械\n生产日期：%s\n出厂日期：%s\n产品类别：%s",
						// new String[]{String.valueOf(pDateDes),
						// String.valueOf(eDateDes),
						// String.valueOf(pTypeDes)}));
						Toast.makeText(VerifyRFID.this, "电子标签验证成功",
								Toast.LENGTH_SHORT).show();
						PlayNotification();
						sendResult(String
								.format("电子标签验证成功：\n厂家：四川中兴机械\n生产日期：%s\n出厂日期：%s\n产品类别：%s",
										new String[] {
												String.valueOf(pDateDes),
												String.valueOf(eDateDes),
												String.valueOf(pTypeDes) }));
					}
				}
			}
		}
	}

	private boolean VerifyTag() {
		String mimaStr = "CC13250B1222";
		byte[] passw = I2CTools.stringToBytes(mimaStr);
		String address = "60";

		byte[] buffer = new byte[16];
		int add = Integer.valueOf(address);
		int t;

		t = I2CTools.ReadBlock(buffer, passw, (byte) 0x60, (byte) add);
		if (t != 0) {
			String croutonText = "警告：该标签不是思木科技认证的合法防伪标签，谨防伪造！";
			PlayNotification();
//			sendResult(croutonText);
			// ShowCrouton(Style.ALERT, croutonText);
		} else {
			String croutonText = "警告：该标签虽然加密信息吻合，但无法查找到认证数据，谨防伪造！";
			PlayNotification();
//			sendResult(croutonText);
			// ShowCrouton(Style.ALERT, croutonText);
		}
		return t == 0;
	}

	private void sendResult(String message) {
		Intent it = new Intent(VerifyRFID.this, VerifyRFIDResult.class);
		it.putExtra("message", message);
		finish();
		startActivity(it);
	}
	
	private void PlayNotification() {
	  	Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);  
	    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);  
	    r.play(); 
	  }
}
