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

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;

import simu.avsubobjects.*;
import simu.app.handset.R;
import simu.database.AssetsDatabaseManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
	
	private String TempCategoryName = null;
	private Number TempcParentNo = null;

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
			case R.id.button_download: {
				DownLoad();
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
		Log.d(tag, "调用了Upload方法");
		newThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				AVQuery<AVObject> QueryForVendor = new AVQuery<AVObject>(
						"Vendor");
				QueryForVendor.whereEqualTo("vendorName", "四川中兴机械制造有限公司");
				try {
					List<AVObject> TempVendors = QueryForVendor.find();
					TempVendroObj = TempVendors.get(0);
				} catch (AVException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				Cursor c = db.rawQuery("select * from smallTags where "
						+ "ProductType is not '' and ProductType is not null",
						null);
				Log.d(tag, "smallTags表中 ProductType 不为空的记录有 " + c.getCount() + "条");
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
					String sProductType = c.getString(c
							.getColumnIndex("ProductType"));
					Log.d(tag, "smallTags表的的ProductType为:" + sProductType);

					AVQuery<AVObject> QueryForRfid = new AVQuery<AVObject>(
							"RFID");
					QueryForRfid.whereEqualTo("cid", sCID);
					try {
						List<AVObject> TempRfidObjs = QueryForRfid.find();
						TempRfidObj = TempRfidObjs.get(0);
						AVQuery<AVObject> QueryForProduct = new AVQuery<AVObject>(
								"Product");
						QueryForProduct.whereEqualTo("rfid", TempRfidObj);
						List<AVObject> TempProObjs = QueryForProduct.find();
						if (TempProObjs.size() >= 1) {
							Log.d(tag, "Product表中有对应的记录");
						} else {
							Log.d(tag, "Product表中木有对应的记录");
							AVQuery<AVObject> QueryForProCgy = new AVQuery<AVObject>(
									"ProductCategory");
							QueryForProCgy.whereEqualTo("tempSN", sProductType);
							List<AVObject> TempProCgys = QueryForProCgy.find();
							if (TempProCgys.size() >= 1) {
								TempProCgy = TempProCgys.get(0);
								// 获取 categoryName (FullName)
								TempCategoryName = TempProCgy
										.getString("categoryName");
								TempcParentNo = TempProCgy
										.getNumber("cParentNo");
								if (TempcParentNo != null) {
									AVQuery<ProductCategory> query2 = AVObject
											.getQuery(ProductCategory.class);
									query2.whereEqualTo("cNo", TempcParentNo);
									AVObject obj1 = query2.getFirst();
									TempCategoryName = obj1
											.getString("categoryName")
											+ "-"
											+ TempCategoryName;
									TempcParentNo = obj1.getNumber("cParentNo");
									if (TempcParentNo != null) {
										AVQuery<ProductCategory> query3 = AVObject
												.getQuery(ProductCategory.class);
										query3.whereEqualTo("cNo",
												TempcParentNo);
										AVObject obj2 = query3.getFirst();
										TempCategoryName = obj2
												.getString("categoryName")
												+ "-" + TempCategoryName;
									}
								}
							}

							AVObject NewObject = new AVObject("TestToSaveObj");
							NewObject.put("statu", 1);
							NewObject.put("rfid", TempRfidObj);
							NewObject.put("vendor", TempVendroObj);
							NewObject.put("category", TempProCgy);
							NewObject.put("ProductDate", date);
							NewObject.put("categoryName", TempCategoryName);
							NewObject.save();
							Log.d(tag, "构造数据保存完毕");
						}
					} catch (AVException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				c.close();
				// db.close();
			}
		});
		newThread.start();
		
	}
	
	private void DownloadVendor(){
		AVQuery<Vendor> query = AVObject.getQuery(Vendor.class);
		query.findInBackground(new FindCallback<Vendor>(){
			@Override
			public void done(List<Vendor> results, AVException e){
				if (e == null)
				{
					try
					{
						db.execSQL("DELETE FROM Vendor");
						for (Vendor vendor : results){
							ContentValues values = new ContentValues();
							values.put("objectId", vendor.getObjectId());
							values.put("vendorName", vendor.getVendorName());
							values.put("vendorCode", vendor.getVendorCode());
							db.insert("Vendor", null, values);
						}
					} catch(Exception se){
						se.printStackTrace();
					}
				}
				else
					e.printStackTrace();
			}
		});
	}
	
	private void DownloadCategory(){
		AVQuery<ProductCategory> query = AVObject.getQuery(ProductCategory.class);
		query.whereEqualTo(ProductCategory.CN_TAG_GENERATION, 2);
		query.limit(1000);
		query.findInBackground(new FindCallback<ProductCategory>(){
			@Override
			public void done(List<ProductCategory> results, AVException e){
				if (e == null)
				{
					try
					{
						db.execSQL("DELETE FROM ProductCategory");
						for (ProductCategory pc : results){
							ContentValues values = new ContentValues();
							values.put("objectId", pc.getObjectId());
							values.put("categoryName", pc.getCategoryName());
							values.put("level", pc.getLevel());
							String parentObjectId = pc.getParent() == null ? "" : pc.getParent().getObjectId();
							values.put("parent", parentObjectId);
							values.put("tagGeneration", pc.getTagGeneration());
							values.put("tempSN", pc.getTempSN());
							values.put("fullName", pc.getFullName());//增加FullName的下载
							db.insert("ProductCategory", null, values);
						}
					} catch(Exception se){
						se.printStackTrace();
					}
				}
				else
					e.printStackTrace();
			}
		});
	}
	
	private void DownloadRFID(){
		while(true){
			Cursor c = db.rawQuery("SELECT MAX(initializeSN) FROM RFID", null);
			int initSn = 0;
			if (c.getCount() > 0) {
				c.moveToFirst();
				initSn = c.getInt(0);
			}
			AVQuery<RFID> query = AVObject.getQuery(RFID.class);
			query.whereEqualTo(RFID.CN_GENERATION, 2);
			query.whereGreaterThan(RFID.CN_INITIALIZE_SN, initSn);
			query.orderByAscending(RFID.CN_INITIALIZE_SN);
			query.limit(500);
			try {
				List<RFID> results = query.find();
				if (results.isEmpty())
					return;
				for (RFID rfid : results){
					ContentValues values = new ContentValues();
					values.put("objectId", rfid.getObjectId());
					values.put("generation", rfid.getGeneration());
					values.put("guid", rfid.getGuid());
					values.put("initializeSN", rfid.getInitializeSN());
					//values.put("initializeDate", rfid.getInitializeDate());
					values.put("printingSN", rfid.getPrintingSN());
					values.put("cid", rfid.getCid());
					values.put("vendor", rfid.getVendor().getObjectId());
					values.put("statu", rfid.getStatu());
					db.insert("RFID", null, values);
				}
			} catch (AVException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	private void DownLoad() {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				//DownloadVendor();
				//DownloadCategory();
				DownloadRFID();
			}
			}).start();
		
		//ShowCrouton(Style.CONFIRM, "下载完毕");
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
