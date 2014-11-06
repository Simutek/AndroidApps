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

public class MyApplication extends Application {
	
	public AVObject TempVendroObj = null;
	public AVObject TempRfidObj = null;
	

	private String Tag;
	protected String tag;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		log.d(Tag, "---------");
		
		AVOSCloud.initialize(this, "ztxdtfdpjrzbsu3serlcvbdvyk0pfscj0uq4abwpnzzq0xjt", 
				"3b42n9qeca6zh58r1fcd91rbblfgz24ro4boz502rl7ldms2");
//		//my test
//		AVOSCloud.initialize(this, "ytic05hcb4z3jr93yvhlbp42si2j15zb4ovw6bpt470xybc8", 
//				"as8cca8okxowz2663xflikbof927qhif1222lpenbvi7q07h");
		
		
		AVQuery<AVObject> QueryForVendor = new AVQuery<AVObject>("Vendor");
		try {
			TempVendroObj = QueryForVendor.get("GhRj3SamEr");
		} catch (AVException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}
