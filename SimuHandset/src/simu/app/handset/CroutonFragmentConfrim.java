package simu.app.handset;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import simu.avsubobjects.ProductCategory;
import simu.database.AssetsDatabaseManager;

import cilico.tools.I2CTools;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
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

public class CroutonFragmentConfrim extends FragmentActivity implements View.OnClickListener{
	
	private TextView first,second,third;
	private EditText ETconfirm;
	private Button btn_cancel;
	private String tag = "CroutonFragmentConfirm";
	Timer timer;
	int closeTimerTickCount = 0;
	private SQLiteDatabase db;
	private ProductCategory curPC = null;
	private String tempRFIDObjectId;
	private Crouton infiniteCrouton;
	
	
	private static final Style INFINITE = new Style.Builder()
	.setBackgroundColorValue(Style.holoBlueLight).build();
	private static final Configuration CONFIGURATION_INFINITE = new Configuration.Builder()
	.setDuration(Configuration.DURATION_INFINITE).build();

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.d(null, "CroutonFragmentConfirm.this.onBackPressed()");
		Intent it = new Intent();
		it.setClass(CroutonFragmentConfrim.this, CroutonDemo.class);
		finish();
		startActivity(it);
		
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		Log.d(tag, "this.onCreate()");
		setContentView(R.layout.crouton_confirm);
		first = (TextView) findViewById(R.id.et_first);
		second = (TextView) findViewById(R.id.et_second);
		third =   (TextView) findViewById(R.id.et_third);
		ETconfirm = (EditText) findViewById(R.id.et_confirm);
		
		
		Intent it = getIntent();
		first.setText(it.getStringExtra("first"));
		second.setText(it.getStringExtra("second"));
		third.setText(it.getStringExtra("third"));
		
		AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();
		db = mg.getDatabase("SimuLocal.db");
		
		//show the readyDialog
		readyToRead();
		
		//the readyDialog was shown
		inCome();
	}


	Handler handler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {

			Log.d(tag, "this.readyToRead()");
			String cid = ReadCID(true);
			if (8 == cid.length()) {
				timer.cancel();
				PutInStorage(cid);
			}else if (10 < closeTimerTickCount) {
				timer.cancel();
			}
			
		}
		
	};
	

	private void readyToRead() {
		Log.d(tag, "this.readyToRead()");
		LayoutInflater li = getLayoutInflater();
		View readyDialog = li.inflate(R.layout.dialog_ready, (ViewGroup) findViewById(R.id.dialog_ready));
//		AlertDialog.Builder builder =  new AlertDialog.Builder(this);
//		builder.setTitle("准备写入RFID认证").setView(readyDialog).show();
//		
		Dialog dialog = new Dialog(CroutonFragmentConfrim.this);
		dialog.setTitle("准备写入RFID认证");
		dialog.setContentView(readyDialog);
		dialog.show();
		btn_cancel = (Button)dialog.getWindow().findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(this);
		
		
	
		
	}

	@Override
	public void onClick(View v) {
		Log.d(tag, ".onClick()");
		if (R.id.btn_cancel == v.getId()) {
			Intent it = new Intent();
			it.setClass(CroutonFragmentConfrim.this, CroutonDemo.class);
			finish();
			startActivity(it);
		}
		
	}
	
	private void inCome() {
		Log.d(tag, "this.inCome()");
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (null != timer) {
					timer.cancel();
					timer.purge();
				}
				Message message = new Message();
				handler.sendMessage(message);
			}
		};
		timer = new Timer(true);
		timer.schedule(task, 1000, 1500);
	}
	
	private String ReadCID(boolean isSilent) {
		Log.d(tag, "this.ReadCID()");
		
		String t = I2CTools.ReadUID();
		String cid = "";
		if (t.length() == 8) {
			cid = t.substring(6, 8);
			cid = cid + t.substring(4, 6);
			cid = cid + t.substring(2, 4);
			cid = cid + t.substring(0, 2);
			cid = cid.toUpperCase();
		} else if (!isSilent) {
			//TODO 如果没有感应到标签
			String croutonText = "没有感应到思木电子标签\n请将手持机靠近标签重试";
			ShowCrouton(Style.ALERT, croutonText);
		}
		return cid;
	}

	private void PutInStorage(String cid) {
		Log.d(tag, "this.PutInStorage");
		MyApplication myApp = (MyApplication)getApplication();
		curPC = myApp.getCurProductCategory();
		Log.d(tag, "this.PutInStorage().curPC = " + curPC.getFullName());
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
									this);
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
			ShowCrouton(Style.ALERT, croutonText);
		}
		return t == 0;
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
	}
	
	
	private void showCrouton(String croutonText, Style croutonStyle,
			Configuration configuration) {
		final boolean infinite = INFINITE == croutonStyle;

		if (infinite) {
			croutonText = getString(R.string.infinity_text);
		}

		final Crouton crouton;
		crouton = Crouton.makeText(CroutonFragmentConfrim.this, croutonText, croutonStyle);

		if (infinite) {
			infiniteCrouton = crouton;
		}
		crouton.setOnClickListener(this)
				.setConfiguration(
						infinite ? CONFIGURATION_INFINITE : configuration)
				.show();
	}
	
	private void ShowCrouton(final Style croutonStyle, String croutonText) {
		// showCrouton(croutonText, croutonStyle, Configuration.DEFAULT);
		ETconfirm.setText(croutonText);
		if (croutonStyle == Style.CONFIRM || croutonStyle == Style.INFO)
			PlayNotification();
	}

	private void PlayNotification() {
		Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone r = RingtoneManager.getRingtone(CroutonFragmentConfrim.this
				.getApplicationContext(), notification);
		r.play();
	}
}
