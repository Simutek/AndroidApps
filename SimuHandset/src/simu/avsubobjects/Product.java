package simu.avsubobjects;

import java.util.Date;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

@AVClassName("Product")
public class Product extends AVObject {
	//category,rfid, vendor, productDate, productSn, factoryDate
	public final String CN_CATEGORY = "category";
	public ProductCategory getCategory(){
		return getAVObject(CN_CATEGORY);
	}
	public void setCategory(ProductCategory value){
		put(CN_CATEGORY, value);
	}

	public final String CN_RFID = "rfid";
	public RFID getRFID(){
		return getAVObject(CN_RFID);
	}
	public void setRFID(RFID value){
		put(CN_RFID, value);
	}

	public final String CN_VENDOR = "vendor";
	public Vendor getVendor(){
		return getAVObject(CN_VENDOR);
	}
	public void setVendor(Vendor value){
		put(CN_VENDOR, value);
	}
	
	public final String CN_PRODUCT_DATE = "productDate";
	public Date getProductDate(){
		return getDate(CN_PRODUCT_DATE);
	}
	public void setProductDate(Date value){
		put(CN_PRODUCT_DATE, value);
	}

	public final String CN_PRODUCT_SN = "productSN";
	public String getProductSN(){
		return getString(CN_PRODUCT_SN);
	}
	public void setProductSN(String value){
		put(CN_PRODUCT_SN, value);
	}
	
	public final String CN_FACTORY_DATE = "factoryDate";
	public Date getFactoryDate(){
		return getDate(CN_FACTORY_DATE);
	}
	public void setFactoryDate(Date value){
		put(CN_FACTORY_DATE, value);
	}
}
