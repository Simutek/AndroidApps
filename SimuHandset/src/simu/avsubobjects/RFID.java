package simu.avsubobjects;

import java.util.Date;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVClassName;


@AVClassName("RFID")
public class RFID extends AVObject {
	public static final String CN_GENERATION = "generation";
	public int getGeneration(){
		return getInt(CN_GENERATION);
	}
	public void setGeneration(int value){
		put(CN_GENERATION, value);
	}
	
	public static final String CN_INITIALIZE_DATE = "initializeDate";
	public Date getInitializeDate(){
		return getDate(CN_INITIALIZE_DATE);
	}
	public void setInitializeDate(Date value){
		put(CN_INITIALIZE_DATE, value);
	}
	
	public static final String CN_GUID = "guid";
	public String getGuid(){
		return getString(CN_GUID);
	}
	public void setGuid(String value){
		put(CN_GUID, value);
	}
	
	public static final String CN_CID = "cid";
	public String getCid(){
		return getString(CN_CID);
	}
	public void setCid(String value){
		put(CN_CID, value);
	}

	public static final String CN_PRINTING_SN = "printingSN";
	public int getPrintingSN(){
		return getInt(CN_PRINTING_SN);
	}
	public void setPrintingSN(int value){
		put(CN_PRINTING_SN, value);
	}

	public static final String CN_INITIALIZE_SN = "initializeSN";
	public int getInitializeSN(){
		return getInt(CN_INITIALIZE_SN);
	}
	public void setInitializeSN(int value){
		put(CN_INITIALIZE_SN, value);
	}

	public static final String CN_VENDOR = "vendor";
	public Vendor getVendor(){
		return getAVObject(CN_VENDOR);
	}
	public void setVendor(Vendor value){
		put(CN_VENDOR, value);
	}

	public static final String CN_STATU = "statu";
	public int getStatu(){
		return getInt(CN_STATU);
	}
	public void setStatu(int value){
		put(CN_STATU, value);
	}
}
