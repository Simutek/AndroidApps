package simu.avsubobjects;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVClassName;;

@AVClassName("Vendor")
public class Vendor extends AVObject {
	public final String CN_VENDOR_NAME = "vendorName";
	public String getVendorName(){
		return getString(CN_VENDOR_NAME);
	}
	public void setVendor(String value){
		put(CN_VENDOR_NAME, value);
	}

	public final String CN_VENDOR_CODE = "vendorCode";
	public String getVendorCode(){
		return getString(CN_VENDOR_CODE);
	}
	public void setVendorCode(String value){
		put(CN_VENDOR_CODE, value);
	}
}
