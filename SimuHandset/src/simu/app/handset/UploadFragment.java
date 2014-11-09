/*
 * Copyright 2012 - 2013 Benjamin Weiss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package simu.app.handset;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;


import simu.app.handset.R;
import simu.database.AssetsDatabaseManager;
import cilico.tools.I2CTools;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * @author keyboardsurfer
 * @since 14.12.12
 */
public class UploadFragment extends Fragment implements View.OnClickListener {

	private static final Style INFINITE = new Style.Builder()
			.setBackgroundColorValue(Style.holoBlueLight).build();
	private static final Configuration CONFIGURATION_INFINITE = new Configuration.Builder()
			.setDuration(Configuration.DURATION_INFINITE).build();

	private EditText croutonTextEdit, hintEdit;
	private Crouton infiniteCrouton;
	private SQLiteDatabase db;
	private Thread newThread;
	
	public AVObject TempVendroObj = null;
	public AVObject TempRfidObj = null;
	public  AVObject TempProCgy = null;
	public Date date;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.upload, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.button_upload).setOnClickListener(this);
		view.findViewById(R.id.button_download).setOnClickListener(this);

		hintEdit = (EditText) view.findViewById(R.id.edit_hint);

		AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();
		db = mg.getDatabase("SimuLocal.db");
	}

	private String tag;

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.button_upload: {
				Log.d(tag, "点击了upload按钮");
				UpLoad();
				break;
			}
			default: {
				if (infiniteCrouton != null) {
					Crouton.hide(infiniteCrouton);
					infiniteCrouton = null;
				}
				break;
			}
		}
	}

	private void UpLoad() {
		
		newThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				AVQuery<AVObject> QueryForVendor = new AVQuery<AVObject>("Vendor");
				QueryForVendor.whereEqualTo("vendorName", "四川中兴机械制造有限公司");
				try {
					List<AVObject> TempVendors = QueryForVendor.find();
					TempVendroObj = TempVendors.get(0);
				} catch (AVException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
				Cursor c = db.rawQuery("select * from smallTags where " +
						"ProductType is not '' and ProductType is not null", null);
				
				while (c.moveToNext()) {
					
					String spd = c.getString(c.getColumnIndex("ProductionDate"));
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
					try {
						date = format.parse(spd);
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					String sCID = c.getString(c.getColumnIndex("CID"));
					String sProductType = c.getString(c.getColumnIndex("ProductType"));
					Log.d(tag, "smallTags表的的ProductType为:" + sProductType);
					
					AVQuery<AVObject> QueryForRfid = new AVQuery<AVObject>("RFID");
					QueryForRfid.whereEqualTo("cid", sCID);
					try {
						List<AVObject> TempRfidObjs = QueryForRfid.find();
						TempRfidObj = TempRfidObjs.get(0);
						AVQuery<AVObject> QueryForProduct = new AVQuery<AVObject>("Product");
						QueryForProduct.whereEqualTo("rfid", TempRfidObj);
						List<AVObject> TempProObjs = QueryForProduct.find();
						if (TempProObjs.size() >= 1) {
							Log.d(tag, "Product表中有对应的记录");
						}else {
							Log.d(tag, "Product表中木有对应的记录");
							AVQuery<AVObject> QueryForProCgy = new AVQuery<AVObject>("ProductCategory");
							QueryForProCgy.whereEqualTo("tempSN", sProductType);
							List<AVObject> TempProCgys = QueryForProCgy.find();
							if (TempProCgys.size() >= 1) {
								TempProCgy = TempProCgys.get(0);
							}
							
							
							AVObject NewObject = new AVObject("TestToSaveObj");
							NewObject.put("statu", 1);
							NewObject.put("rfid", TempRfidObj);
							NewObject.put("vendor", TempVendroObj);
							NewObject.put("category", TempProCgy);
							NewObject.put("ProductDate", date);
							NewObject.save();
							Log.d(tag, "构造数据保存完毕");
						}
					} catch (AVException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				c.close();
//				db.close();
			}
		});
		newThread.start();
		
		
		
//		// TODO Auto-generated method stub
//		Cursor c = db.rawQuery("select * from smallTags where " +
//				"ProductType is not '' and ProductType is not null", null);
//		Log.d(tag, "在smallTags表中查询到的ProductType不为空的记录有: " + c.getCount() + "条");
//		
//		while (c.moveToNext()) {
//			String sCID = c.getString(c.getColumnIndex("CID"));
//			String sProductType = c.getString(c.getColumnIndex("ProductType"));
//			
//			Log.d(tag, "在smallTags中查询出的CID为:" + sCID);
//			
//			AVQuery<AVObject> QueryForRfid = new AVQuery<AVObject>("RFID");
//			QueryForRfid.whereEqualTo("cid", sCID);
//			QueryForRfid.findInBackground(new FindCallback<AVObject>() {
//				
//				@Override
//				public void done(List<AVObject> arg0, AVException arg1) {
//					// TODO Auto-generated method stub
//					if (null == arg1) {
//						TempRfidObj = (AVObject)arg0.get(0);
//						String Rfidcid = TempRfidObj.getString("cid");
//						String RfidObjectid = TempRfidObj.getString("objectId");
//						Log.d(tag, "LeanCloud中的RFID对应的objectid和cid分别为:" + 
//								RfidObjectid +" " + Rfidcid);
//						AVQuery<AVObject> QueryForProduct = new AVQuery<AVObject>("Product");
//						QueryForProduct.whereEqualTo("rfid", TempRfidObj);
//						QueryForProduct.findInBackground(new FindCallback<AVObject>() {
//							
//							@Override
//							public void done(List<AVObject> arg0, AVException arg1) {
//								// TODO Auto-generated method stub
//								if (arg0.size() >= 1) {
//									Log.d(tag, "Product表中存在对应的rfid对象");
//								}else{
//									AVQuery<AVObject> QueryForProCgy = new AVQuery<AVObject>("ProductCategory");
//									QueryForProCgy.whereEqualTo("tempSN", "sProductType");
//									QueryForProCgy.findInBackground(new FindCallback<AVObject>() {
//										
//										@Override
//										public void done(List<AVObject> arg0, AVException arg1) {
//											// TODO Auto-generated method stub
//											AVObject TempProCgy = (AVObject)arg0.get(0);
//											
//											AVObject NewObject = new AVObject("TestToSaveObj");
//											NewObject.put("statu", 1);
//											NewObject.put("rfid", TempRfidObj);
//											NewObject.put("vendor", TempVendroObj);
//											NewObject.put("category", TempProCgy);
//											NewObject.saveInBackground();
//											
//										}
//									});
//									
//								}
//							}
//						});
//					}else {
//						Log.d(tag, "在LeanCloud中的RFID没有查询到对应的记录");
//					}
//				}
//			});			
//		c.close();
//		db.close();
//	}
	}
	private void GenerateTypeID() {
		Intent intent = new Intent();
		intent.setClass(getActivity(), SpeedActivity.class);
		startActivity(intent);
	}

	private void ShowCrouton(final Style croutonStyle, String croutonText) {
		showCrouton(croutonText, croutonStyle, Configuration.DEFAULT);
		hintEdit.setText(croutonText);
		if (croutonStyle == Style.CONFIRM || croutonStyle == Style.INFO)
			PlayNotification();
	}

	private void PlayNotification() {
		Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone r = RingtoneManager.getRingtone(getActivity()
				.getApplicationContext(), notification);
		r.play();
	}

	private void showCrouton(String croutonText, Style croutonStyle,
			Configuration configuration) {
		final boolean infinite = INFINITE == croutonStyle;

		if (infinite) {
			croutonText = getString(R.string.infinity_text);
		}

		final Crouton crouton;
		crouton = Crouton.makeText(getActivity(), croutonText, croutonStyle);

		if (infinite) {
			infiniteCrouton = crouton;
		}
		crouton.setOnClickListener(this)
				.setConfiguration(
						infinite ? CONFIGURATION_INFINITE : configuration)
				.show();
	}
}
