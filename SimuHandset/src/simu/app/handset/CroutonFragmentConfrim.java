package simu.app.handset;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.actionbarsherlock.internal.view.View_HasStateListenerSupport;

import simu.avsubobjects.ProductCategory;
import simu.database.AssetsDatabaseManager;

import cilico.tools.I2CTools;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CroutonFragmentConfrim extends Activity {

	Timer timer;
	private SQLiteDatabase db;
	int closeTimerTickCount = 0;
	private ProductCategory curPC = null;
	private String tempRFIDObjectId;
	EditText hintEdit;
	private AlertDialog dialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.crouton_confirm);
		AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();
		db = mg.getDatabase("SimuLocal.db");
		Log.d(null, "this.onCreate()");

		TextView et1 = (TextView) findViewById(R.id.et_first);
		et1.setText(getIntent().getStringExtra("first"));
		TextView et2 = (TextView) findViewById(R.id.et_second);
		et2.setText(getIntent().getStringExtra("second"));
		TextView et3 = (TextView) findViewById(R.id.et_third);
		et3.setText(getIntent().getStringExtra("third"));
		hintEdit = (EditText) findViewById(R.id.et_confirm);

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(null, "this.onResume()");
		showReadyDialog();
		beginToCrouton();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent it = new Intent(CroutonFragmentConfrim.this, CroutonDemo.class);
		finish();
		startActivity(it);
	}
	
	private void showReadyDialog() {
		LayoutInflater li = LayoutInflater.from(CroutonFragmentConfrim.this);
		View dialogView = li.inflate(R.layout.dialog_ready, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(
				CroutonFragmentConfrim.this);
		builder.setTitle("准备写入RFID认证").setView(dialogView)
				.setNegativeButton("取消操作", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent it = new Intent(CroutonFragmentConfrim.this,
								CroutonDemo.class);
						finish();
						startActivity(it);
					}
				});
		dialog = builder.create();
		dialog.show();
	}

	private void beginToCrouton() {
		Log.d(null, "this.beginToCrouton()");
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				Message message = new Message();
				handler.sendMessage(message);
				Log.d(null, "has sent the message !!");
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
			Log.d(null, "has received the message !!");
			String cid = ReadCID(true);
			if (cid.length() == 8) {
				timer.cancel();
				PutInStorage(cid);
			} else {
				if (closeTimerTickCount++ > 10) {
					timer.cancel();
					Toast.makeText(CroutonFragmentConfrim.this,
							"操作超时，请重新验证 !!!", Toast.LENGTH_SHORT).show();
					PlayNotification();
					Intent it = new Intent(CroutonFragmentConfrim.this,
							CroutonDemo.class);
					finish();
					startActivity(it);
				}
			}
		}
	};

	private String ReadCID(boolean isSilent) {
		Log.d(null, "begin to ReadCID()");
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
			ShowCrouton(Style.ALERT, croutonText);
		}
		Log.d(null, "the cid has been returned !! cid.length = " + cid.length());
		return cid;
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
			// sendResult(croutonText);
			ShowCrouton(Style.ALERT, croutonText);
		} else {
			String croutonText = "警告：该标签虽然加密信息吻合，但无法查找到认证数据，谨防伪造！";
			PlayNotification();
			// sendResult(croutonText);
			ShowCrouton(Style.ALERT, croutonText);
		}
		return t == 0;
	}

	private void PlayNotification() {
		Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
				notification);
		r.play();
	}

	private void PutInStorage(String cid) {
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
		MyApplication myApp = (MyApplication) getApplication();
		curPC = myApp.getCurProductCategory();
		Log.d(null, "CrouConfirm.curPC = " + curPC.getTempSN());
		if (VerifyTag()) {
			Cursor c = db.rawQuery("SELECT * FROM RFID WHERE cid=?",
					new String[] { String.valueOf(cid) });
			while (c.moveToNext()) {
				String rfidObjectId = c.getString(c.getColumnIndex("objectId"));
				Cursor c1 = db.rawQuery("SELECT * FROM Product WHERE rfid=?",
						new String[] { String.valueOf(rfidObjectId) });
				if (c1.getCount() <= 0) {
					AddNModifyProduct(rfidObjectId, curPC, true);
				} else {
					if (c1.moveToFirst()) {
						String eDate = c1.getString(c1
								.getColumnIndex("factoryDate"));

						if (!(eDate == null || eDate.equals("NULL") || eDate
								.equals("null"))) {
							ShowCrouton(Style.ALERT, "产品已登记出库，无法重复入库");
							return;
						} else {
							tempRFIDObjectId = rfidObjectId;
							AlertDialog.Builder builder = new AlertDialog.Builder(
									CroutonFragmentConfrim.this);
							builder.setTitle("警告")
									.setIcon(R.drawable.ic_launcher)
									.setCancelable(false)
									.setMessage("产品已登记入库，是否要再次登记并覆盖之前的记录？")
									.setPositiveButton(
											"确定",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													AddNModifyProduct(
															tempRFIDObjectId,
															curPC, false);
												}
											}).setNegativeButton("取消", null);
							AlertDialog dlg = builder.create();
							dlg.show();
						}
					}
				}
			}
		}
	}

	private void AddNModifyProduct(String rfid, ProductCategory pc,
			Boolean isAdd) {
		if (isAdd) {
			ContentValues values = new ContentValues();
			values.put("category", pc.getObjectId());
			values.put("rfid", rfid);
			values.put("vendor", String.valueOf("GhRj3SamEr"));
			values.put("categoryName", pc.getFullName());// 将categoryName改赋成Fullname
			db.insert("Product", null, values);
		}
		try {

			db.execSQL(
					"UPDATE Product SET productDate=Date(), category=?, categoryName=? WHERE rfid=?",
					new String[] { String.valueOf(pc.getObjectId()),
							String.valueOf(pc.getCategoryName()),
							String.valueOf(rfid) });
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}

		ShowCrouton(Style.INFO, "登记成功");
		showCroutonSuccess();
	}

	private void showCroutonSuccess() {
		AlertDialog.Builder succ = new AlertDialog.Builder(
				CroutonFragmentConfrim.this);
		succ.setTitle("写入成功").setMessage(
				"设备类别：" + getIntent().getStringExtra("first") + "-"
						+ getIntent().getStringExtra("second") + "-"
						+ getIntent().getStringExtra("third"));
		succ.setNegativeButton("绑定二维码", null).setNegativeButton("取消", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent it = new Intent(CroutonFragmentConfrim.this, CroutonDemo.class);
				finish();
				startActivity(it);
			}
		});
		succ.create().show();
	}

	private void ShowCrouton(final Style croutonStyle, String croutonText) {
		// showCrouton(croutonText, croutonStyle, Configuration.DEFAULT);
		hintEdit.setText(croutonText);
		if (croutonStyle == Style.CONFIRM || croutonStyle == Style.INFO)
			PlayNotification();
	}



}
