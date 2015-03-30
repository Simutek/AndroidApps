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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import simu.app.handset.R;
import simu.app.handset.R.layout;
import simu.avsubobjects.ProductCategory;
import simu.database.AssetsDatabaseManager;
import cilico.tools.I2CTools;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * @author keyboardsurfer
 * @since 14.12.12
 */
public class CroutonFragment extends Fragment implements View.OnClickListener {



	private static final Style INFINITE = new Style.Builder()
			.setBackgroundColorValue(Style.holoBlueLight).build();
	private static final Configuration CONFIGURATION_INFINITE = new Configuration.Builder()
			.setDuration(Configuration.DURATION_INFINITE).build();

	private PopupWindow popupWindow;
	private ListView lv_group;
	private View view;
	private View top_title;
	private TextView tvtitle;
	private List<String> groups;
	private ProductCategory p1 = null;
	private ProductCategory p2 = null;
	private ProductCategory p3 = null;

	private EditText croutonTextEdit, hintEdit;
	private Crouton infiniteCrouton;
	private SQLiteDatabase db;

	private Spinner sp1, sp2, sp3;
	private EditText et1,et2,et3;
	private String first,second,third;

	enum OperType {
		PUTINSTORAGE, VERIFY, EXSTORAGE, NOTHING
	}

	private OperType curOperType = OperType.NOTHING;

	private String tempRFIDObjectId;
	private ProductCategory curPC = null;

	Timer timer;
	int closeTimerTickCount = 0;

	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(null, "CroutonFragment.savedInstanceState == null ? " + savedInstanceState);
		return inflater.inflate(R.layout.crouton_demo, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.button_show).setOnClickListener(this);
		croutonTextEdit = (EditText) view.findViewById(R.id.edit_text_text);
		hintEdit = (EditText) view.findViewById(R.id.edit_hint);
//		sp1 = (Spinner) view.findViewById(R.id.spinner1);
//		sp2 = (Spinner) view.findViewById(R.id.spinner2);
//		sp3 = (Spinner) view.findViewById(R.id.spinner3);
		et1 = (EditText)view.findViewById(R.id.firstLevel);
		et2 = (EditText)view.findViewById(R.id.secondLevel);
		et3 = (EditText)view.findViewById(R.id.thirdLevel);
		
		Log.d(null, "CroutonFragment.this-------");
		

		AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();
		db = mg.getDatabase("SimuLocal.db");

		ArrayList<ProductCategory> cp = FillComponents(0, null);
		ComponentAdapter adp = new ComponentAdapter(getActivity(), cp);
//		sp1.setAdapter(adp);
//		showWindow( adp);
		// new AlertDialog.Builder(getActivity()).setTitle("一级类别")
		// .setAdapter(adp, null).show();
		
		et1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ArrayList<ProductCategory> cp = FillComponents(0, null);
				Log.d(null, "cp.count = " + cp.size());
				ComponentAdapter adp = new ComponentAdapter(getActivity(), cp);
				showWindow( 1,adp);
			}
		});

		et2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ArrayList<ProductCategory> cp = FillComponents(1, p1);
				Log.d(null, "cp.count = " + cp.size());
				ComponentAdapter adp = new ComponentAdapter(getActivity(), cp);
				showWindow( 2,adp);
			}
		});
		
		et3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ArrayList<ProductCategory> cp = FillComponents(2, p2);
				Log.d(null, "cp.count = " + cp.size());
				ComponentAdapter adp = new ComponentAdapter(getActivity(), cp);
				showWindow(3, adp);
			}
		});

//		sp1.setOnItemSelectedListener(new OnItemSelectedListener() {
//			@Override
//			public void onItemSelected(AdapterView<?> parent, View view,
//					int position, long id) {
//				ProductCategory c = (ProductCategory) parent
//						.getItemAtPosition(position);
//				ArrayList<ProductCategory> cp = FillComponents(1, c);
//				ComponentAdapter adp = new ComponentAdapter(getActivity(), cp);
//				sp2.setAdapter(adp);
//				croutonTextEdit.setText(c.getTempSN());
//				curPC = c;
//
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> parent) {
//				// TODO Auto-generated method stub
//				ArrayList<ProductCategory> cp = new ArrayList<ProductCategory>();
//				ComponentAdapter adp = new ComponentAdapter(getActivity(), cp);
//				sp2.setAdapter(adp);
//				sp3.setAdapter(adp);
//			}
//		});

//		sp2.setOnItemSelectedListener(new OnItemSelectedListener() {
//			@Override
//			public void onItemSelected(AdapterView<?> parent, View view,
//					int position, long id) {
//				ProductCategory c = (ProductCategory) parent
//						.getItemAtPosition(position);
//				ArrayList<ProductCategory> cp = FillComponents(2, c);
//				ComponentAdapter adp = new ComponentAdapter(getActivity(), cp);
//				sp3.setAdapter(adp);
//				croutonTextEdit.setText(c.getTempSN());
//				curPC = c;
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> parent) {
//				// TODO Auto-generated method stub
//				ArrayList<ProductCategory> cp = new ArrayList<ProductCategory>();
//				ComponentAdapter adp = new ComponentAdapter(getActivity(), cp);
//				sp3.setAdapter(adp);
//			}
//		});

//		sp3.setOnItemSelectedListener(new OnItemSelectedListener() {
//			@Override
//			public void onItemSelected(AdapterView<?> parent, View view,
//					int position, long id) {
//				ProductCategory c = (ProductCategory) parent
//						.getItemAtPosition(position);
//				croutonTextEdit.setText(c.getTempSN());
//				curPC = c;
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> parent) {
//				// TODO Auto-generated method stub
//			}
//		});

		croutonTextEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
	}

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			String cid = ReadCID(true);
			if (cid.length() == 8) {
				timer.cancel();
				switch (curOperType) {
				case PUTINSTORAGE: {
					PutInStorage(cid);
					break;
				}
				default:
					break;
				}
			} else {
				if (closeTimerTickCount++ > 10)
					timer.cancel();
			}
		}
	};

	@Override
	public void onClick(View view) {
		TimerTask task = new TimerTask() {
			public void run() {
				Message message = new Message();
				handler.sendMessage(message);
			}
		};
		closeTimerTickCount = 0;
		// 按钮被按下时，将之前可能正在运行的timer任务终止
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
		switch (view.getId()) {
		case R.id.button_show: {
			if (null != curPC) {
				MyApplication app = (MyApplication)getActivity().getApplication();
				Log.d("CroutonFragment.this", "CroutonFragment.curPC = " + curPC.getTempSN());
				app.setCurProductCategory(curPC);
				Log.d(null, "MyApplication.curProductCategory.SN = " + app.getCurProductCategory().getTempSN());
			}else {
				Toast.makeText(getActivity(), "还没选择任何条目", Toast.LENGTH_SHORT).show();
				break;
			}
			Intent it = new Intent();
			it.putExtra("first", first);
			it.putExtra("second", second);
			it.putExtra("third", third);
			it.setClass(getActivity().getApplicationContext(), CroutonFragmentConfrim.class);
			startActivity(it);
			getActivity().finish();
			curOperType = OperType.PUTINSTORAGE;
			timer = new Timer(true);
			timer.schedule(task, 1000, 1500);
			break;
		}
		case R.id.button_verify: {
			curOperType = OperType.VERIFY;
			timer = new Timer(true);
			timer.schedule(task, 1000, 1000);
			break;
		}
		case R.id.button_exstorage: {
			curOperType = OperType.EXSTORAGE;
			timer = new Timer(true);
			timer.schedule(task, 1000, 1000);
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

	private void PutInStorage(String cid) {
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
									getActivity());
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

	private ArrayList<ProductCategory> FillComponents(int level,
			ProductCategory parent) {
		ArrayList<ProductCategory> result = new ArrayList<ProductCategory>();
		String parentObjectId = parent == null ? "" : parent.getObjectId();
		Cursor c = db.rawQuery(
				"SELECT * FROM ProductCategory WHERE level=? and parent=?",
				new String[] { String.valueOf(level), parentObjectId });
		while (c.moveToNext()) {
			ProductCategory pc = new ProductCategory();
			pc.setObjectId(c.getString(c.getColumnIndex("objectId")));
			pc.setCategoryName(c.getString(c.getColumnIndex("categoryName")));
			pc.setLevel(c.getInt(c.getColumnIndex("level")));
			pc.setParent(parent);
			pc.setTagGenetation(c.getInt(c.getColumnIndex("tagGeneration")));
			pc.setTempSN(c.getString(c.getColumnIndex("tempSN")));
			result.add(pc);
		}
		return result;
	}

	private void ShowCrouton(final Style croutonStyle, String croutonText) {
		// showCrouton(croutonText, croutonStyle, Configuration.DEFAULT);
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
			ShowCrouton(Style.ALERT, croutonText);
		}
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
			ShowCrouton(Style.ALERT, croutonText);
		}
		return t == 0;
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

	private void showWindow( final int level,ComponentAdapter adp) {
		
		if (popupWindow == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getActivity()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			view = layoutInflater.inflate(R.layout.group_list, null);

			lv_group = (ListView) view.findViewById(R.id.lvGroup);

			
			// 创建一个PopuWidow对象
			popupWindow = new PopupWindow(view, view.getWidth(), 400);
		}
		
		lv_group.setAdapter(adp);
		
		// 使其聚集
		popupWindow.setFocusable(true);
		// 设置允许在外点击消失
		popupWindow.setOutsideTouchable(true);

		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		WindowManager windowManager = (WindowManager) getActivity()
				.getSystemService(Context.WINDOW_SERVICE);

		// 控制popupwindow的宽度和高度自适应
//		lv_group.measure(View.MeasureSpec.EXACTLY,
//				View.MeasureSpec.EXACTLY );
		
//		popupWindow.setWidth(lv_group.getMeasuredWidth());
//		popupWindow.setHeight((lv_group.getMeasuredHeight())
//				* lv_group.getCount());
		popupWindow.setWidth(120);
		popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
		
		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService
			      (Context.LAYOUT_INFLATER_SERVICE);
		View crouton = inflater.inflate(R.layout.crouton_demo, null);
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;//宽度
		int height = dm.heightPixels ;//高度
		int height1 = getActivity().getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight();
		Log.d(null, "listview : width = " + lv_group.getWidth() + " height = " + lv_group.getHeight());
		Log.d(null, "屏幕大小 : width = " + width + " height = " + height + "view 区域  height = " + height1);
		Log.d(null, "croutonlayout.width = " + crouton.getWidth() + " height = " + crouton.getHeight());
		Log.d(null, "powindow.width : " + popupWindow.getWidth() + " powindow.height : " + popupWindow.getHeight());
		popupWindow.showAsDropDown(
				 crouton, width - popupWindow.getWidth(), height - height1);
		
		lv_group.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				ProductCategory c = (ProductCategory) adapterView
						.getItemAtPosition(position);
				curPC = c;
				Toast.makeText(getActivity(), "level : " + level + "c.TempSN = " + c.getTempSN(), Toast.LENGTH_SHORT).show();
				switch (level) {
				case 1:
					et1.setText(c.getTempSN() + " " + c.getCategoryName());
					p1 = c;
					first = c.getTempSN() + " " + c.getCategoryName();
					croutonTextEdit.setText(c.getTempSN() + " " + c.getCategoryName());
					et2.setText(null);
					et3.setText(null);
					break;
				case 2:
					et2.setText(c.getTempSN() + " " + c.getCategoryName());
					p2 = c;
					second = c.getTempSN() + " " + c.getCategoryName();
					croutonTextEdit.setText(c.getTempSN() + " " + c.getCategoryName());
					et3.setText(null);
					break;
				case 3:
					et3.setText(c.getTempSN() + " " + c.getCategoryName());
//					p3 = c;
					third = c.getTempSN() + " " + c.getCategoryName();
					croutonTextEdit.setText(c.getTempSN() + " " + c.getCategoryName());
					break;
				default:
					break;
				}

				if (popupWindow != null) {
					popupWindow.dismiss();
				}
			}
		});
	}
}
