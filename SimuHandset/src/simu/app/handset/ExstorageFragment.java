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
public class ExstorageFragment extends Fragment implements View.OnClickListener {

  private static final Style INFINITE = new Style.Builder().
    setBackgroundColorValue(Style.holoBlueLight).build();
  private static final Configuration CONFIGURATION_INFINITE = new Configuration.Builder()
          .setDuration(Configuration.DURATION_INFINITE)
          .build();

  private EditText croutonTextEdit, hintEdit;
  private Crouton infiniteCrouton;
  private SQLiteDatabase db;
  
  private Spinner sp1, sp2, sp3, sp4;
  private String id1, id2, id3;
  
  private List<Component> componentsLevel1;
  private List<Component> componentsLevel2;
  private List<Component> componentsLevel3;
  private List<Component> componentsLevel4;
  
  enum OperType{PUTINSTORAGE, VERIFY, EXSTORAGE, NOTHING}
  private OperType curOperType = OperType.NOTHING; 
  
  private String tempCID;
  
  Timer timer;
  int closeTimerTickCount = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.extorage, null);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
//    view.findViewById(R.id.button_show).setOnClickListener(this);
//    view.findViewById(R.id.button_verify).setOnClickListener(this);
    view.findViewById(R.id.button_exstorage).setOnClickListener(this);
    croutonTextEdit = (EditText) view.findViewById(R.id.edit_text_text);
    hintEdit = (EditText) view.findViewById(R.id.edit_hint);
    sp1 = (Spinner) view.findViewById(R.id.spinner1);
    sp2 = (Spinner) view.findViewById(R.id.spinner2);
    sp3 = (Spinner) view.findViewById(R.id.spinner3);
    
		AssetsDatabaseManager mg = AssetsDatabaseManager.getManager(); 
		db = mg.getDatabase("SimuLocal.db");
		
		ArrayList<Component> cp = FillComponents(0, -1);
		ComponentAdapter adp = new ComponentAdapter(getActivity(), cp);
		sp1.setAdapter(adp);
		
		sp1.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Component c = (Component)parent.getItemAtPosition(position);
				ArrayList<Component> cp = FillComponents(1, c.cNo);
				ComponentAdapter adp = new ComponentAdapter(getActivity(), cp);
				sp2.setAdapter(adp);
				id1 = c.cID;
				croutonTextEdit.setText(id1);
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				ArrayList<Component> cp = new ArrayList<Component>();
				ComponentAdapter adp = new ComponentAdapter(getActivity(), cp);
				sp2.setAdapter(adp);
			}
		});
		
		sp2.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Component c = (Component)parent.getItemAtPosition(position);
				ArrayList<Component> cp = FillComponents(2, c.cNo);
				ComponentAdapter adp = new ComponentAdapter(getActivity(), cp);
				sp3.setAdapter(adp);
				id2 = c.cID;
				croutonTextEdit.setText(id1 + id2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				ArrayList<Component> cp = new ArrayList<Component>();
				ComponentAdapter adp = new ComponentAdapter(getActivity(), cp);
				sp3.setAdapter(adp);
			}
		});
		
		sp3.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Component c = (Component)parent.getItemAtPosition(position);
				id3 = c.cID;
				croutonTextEdit.setText(id1 + id2 + id3);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
		});
		
		croutonTextEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
  }
  
  final Handler handler = new Handler() {
  	public void handleMessage(Message msg) {
  		String cid = ReadCID(true);
  		if (cid.length() == 8) {
  			timer.cancel();
  			switch(curOperType) {
  			case PUTINSTORAGE: {
  				PutInStorage(cid);
  				break;
  			}
  			case VERIFY: {
  				Verify(cid);
  				break;
  			}
  			case EXSTORAGE: {
  				ExStorage(cid);
  				break;
  			}
  			default: 
  				break;
  			}
	  	}
  		else {
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
		//按钮被按下时，将之前可能正在运行的timer任务终止
		if (timer != null)
		{
			timer.cancel();
			timer.purge();
		}
    switch (view.getId()) {
      case R.id.button_show: {
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
  
  private void GenerateTypeID() {
		Intent intent = new Intent();
		intent.setClass(getActivity(), SpeedActivity.class);
		startActivity(intent);
  }

  private void PutInStorage(String cid) {
		if (VerifyTag())
		{
			String typeID = getProductTypeID();
			if (typeID.equals("init"))
			{
				db.execSQL("UPDATE smallTags SET productionDate=NULL, ProductType=NULL, ExStorageDate = NULL WHERE CID=?", new String[]{String.valueOf(cid)});
				ShowCrouton(Style.INFO, "初始化成功，产品类别 : " + typeID); 				
			}
			else
			{
			
				Cursor c = db.rawQuery("SELECT * FROM smallTags WHERE CID=?", new String[]{String.valueOf(cid)});
				while(c.moveToNext())
				{
					String pDate = c.getString(c.getColumnIndex("ProductionDate"));
					String eDate = c.getString(c.getColumnIndex("ExStorageDate"));
					
					if (!(eDate == null || eDate.equals("NULL") || eDate.equals("null")))
					{
						ShowCrouton(Style.ALERT, "产品已登记出库，无法重复入库");
						return;
					}
					else 
					{
						if (!(pDate == null || pDate.equals("NULL") || pDate.equals("null")))
						{
							AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							builder.setTitle("警告")
							.setIcon(R.drawable.ic_launcher)
							.setCancelable(false)
							.setMessage("产品已登记入库，是否要再次登记并覆盖之前的记录？")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {						
								@Override
								public void onClick(DialogInterface dialog, int which) {
					  			String typeID = getProductTypeID();
					  			if (!TextUtils.isEmpty(typeID))
					  			{
							  		db.execSQL("UPDATE smallTags SET productionDate=Date(), ProductType=? WHERE CID=?", new String[]{String.valueOf(typeID), String.valueOf(tempCID)});
							  		ShowCrouton(Style.CONFIRM, "入库登记成功，产品类别 : " + typeID);
					  			}
								}
							})
							.setNegativeButton("取消", null);
							AlertDialog dlg = builder.create();
							tempCID = cid;
							dlg.show();
						}
						else
						{
							if (!TextUtils.isEmpty(typeID))
			  			{
					  		db.execSQL("UPDATE smallTags SET productionDate=Date(), ProductType=? WHERE CID=?", new String[]{String.valueOf(typeID), String.valueOf(cid)});
					  		ShowCrouton(Style.CONFIRM, "入库登记成功，产品类别 : " + typeID);	
			  			}
						}
					}
				}
			}
  	}
	}
  
  private void ExStorage(String cid) {
		if (VerifyTag())
		{
			Cursor c = db.rawQuery("SELECT * FROM smallTags WHERE CID=?", new String[]{String.valueOf(cid)});
			while(c.moveToNext())
			{
				String pDate = c.getString(c.getColumnIndex("ProductionDate"));
				String pType = c.getString(c.getColumnIndex("ProductType"));
				String pTypeDes = (pType == null || pType.equals("NULL") || pType.equals("null")) ? "尚未登记类别" : pType;
				
				if (pDate == null || pDate.equals("NULL") || pDate.equals("null"))
				{
					ShowCrouton(Style.ALERT, "产品尚未登记入库，无法出库");
					return;
				}
				else
				{
		  		db.execSQL("UPDATE smallTags SET ExStorageDate=Date() WHERE CID=?", new String[]{String.valueOf(cid)});
		  		ShowCrouton(Style.CONFIRM, "出库登记成功，产品类别 : " + pTypeDes);
				}
			}
  	}
	}
  
  private void Verify(String cid) {
		if (VerifyTag())
		{			
			Cursor c = db.rawQuery("SELECT * FROM smallTags WHERE CID=?", new String[]{String.valueOf(cid)});
			while(c.moveToNext())
			{
				String pDate = c.getString(c.getColumnIndex("ProductionDate"));
				String eDate = c.getString(c.getColumnIndex("ExStorageDate"));
				String pType = c.getString(c.getColumnIndex("ProductType"));
				String pDateDes = (pDate == null || pDate.equals("NULL") || pDate.equals("null")) ? "尚未登记入库" : pDate;
				String eDateDes = (eDate == null || eDate.equals("NULL") || eDate.equals("null")) ? "尚未登记出库" : eDate;
				String pTypeDes = (pType == null || pType.equals("NULL") || pType.equals("null")) ? "尚未登记类别" : pType;
				ShowCrouton(Style.INFO, String.format("电子标签验证成功：\n厂家：四川中兴机械\n生产日期：%s\n出厂日期：%s\n产品类别：%s", new String[]{String.valueOf(pDateDes), String.valueOf(eDateDes), String.valueOf(pTypeDes)}));
				break;
			}
		}
	}
  
  private ArrayList<Component> FillComponents(int level, int parent)
  {
  	ArrayList<Component> result = new ArrayList<Component>();
  	Cursor c = db.rawQuery("SELECT * FROM component WHERE cLevel=? and cParentNo=?", new String[]{String.valueOf(level), String.valueOf(parent)});
  	while(c.moveToNext())
  	{
  		Component p = new Component();
  		p.cNo = c.getInt(c.getColumnIndex("cNo"));
  		p.cID = c.getString(c.getColumnIndex("cID"));
  		p.cName = c.getString(c.getColumnIndex("cName"));
  		p.cLevel = c.getInt(c.getColumnIndex("cLevel"));
  		p.cParentNo = c.getInt(c.getColumnIndex("cParentNo"));
  		result.add(p);
  	}
  	return result;
  }
  
  private void ShowCrouton(final Style croutonStyle, String croutonText) {
    showCrouton(croutonText, croutonStyle, Configuration.DEFAULT);
    hintEdit.setText(croutonText);
    if (croutonStyle == Style.CONFIRM || croutonStyle == Style.INFO)
    	PlayNotification();
  }
  
  private void PlayNotification() {
  	Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);  
    Ringtone r = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);  
    r.play(); 
  }
  
	private String ReadCID(boolean isSilent)
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
		else if (!isSilent)
		{
			String croutonText = "没有感应到思木电子标签\n请将手持机靠近标签重试";
			ShowCrouton(Style.ALERT, croutonText);
		}
		return cid;
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
		if (t != 0)
		{
			String croutonText = "警告：该标签不是思木科技认证的合法防伪标签，谨防伪造！";
			ShowCrouton(Style.ALERT, croutonText);
		}
		return t==0;
	}
  
  private String getProductTypeID() {
    String typeIDText = croutonTextEdit.getText().toString().trim();

    if (TextUtils.isEmpty(typeIDText)) {
      ShowCrouton(Style.ALERT, "请填写产品编码！");
    }
    return typeIDText;
  }

  private void showCrouton(String croutonText, Style croutonStyle, Configuration configuration) {
    final boolean infinite = INFINITE == croutonStyle;
    
    if (infinite) {
      croutonText = getString(R.string.infinity_text);
    }
    
    final Crouton crouton;
    crouton = Crouton.makeText(getActivity(), croutonText, croutonStyle);

    if (infinite) {
      infiniteCrouton = crouton;
    }
    crouton.setOnClickListener(this).setConfiguration(infinite ? CONFIGURATION_INFINITE : configuration).show();
  }
}
