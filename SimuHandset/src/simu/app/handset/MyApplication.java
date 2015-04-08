package simu.app.handset;

import java.util.List;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.LogUtil.log;

import android.app.Application;
import android.util.Log;
import simu.avsubobjects.*;

public class MyApplication extends Application {
	
	public ProductCategory curProductCategory = null;

	@Override
	public void onCreate() {
		super.onCreate();
		
		AVOSCloud.initialize(this, "ztxdtfdpjrzbsu3serlcvbdvyk0pfscj0uq4abwpnzzq0xjt", 
				"3b42n9qeca6zh58r1fcd91rbblfgz24ro4boz502rl7ldms2");
		
		AVObject.registerSubclass(RFID.class);
		AVObject.registerSubclass(Vendor.class);
		AVObject.registerSubclass(ProductCategory.class);
		AVObject.registerSubclass(Product.class);
	}

	public ProductCategory getCurProductCategory() {
		return curProductCategory;
	}

	public void setCurProductCategory(ProductCategory curProductCategory) {
		Log.d("MyApplication", "curPC has been setted !!");
		this.curProductCategory = curProductCategory;
	}
	
	

}
