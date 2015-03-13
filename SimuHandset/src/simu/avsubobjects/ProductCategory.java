package simu.avsubobjects;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVClassName;

@AVClassName("ProductCategory")
public class ProductCategory extends AVObject {
	public static final String CN_CATEGORY_NAME = "categoryName";
	public static final String CN_CATEGORY_FULLNAME = "fullName";
	
	//增加FullName的get/set方法
	public String getFullName(){
		return getString(CN_CATEGORY_FULLNAME);
	}

	public void setFullName(String value){
		put(CN_CATEGORY_FULLNAME,value);
	}
	
	public String getCategoryName(){
		return getString(CN_CATEGORY_NAME);
	}
	public void setCategoryName(String value){
		put(CN_CATEGORY_NAME, value);
	}

	public static final String CN_TEMP_SN = "tempSN";
	public String getTempSN(){
		return getString(CN_TEMP_SN);
	}
	public void setTempSN(String value){
		put(CN_TEMP_SN, value);
	}

	public static final String CN_LEVEL = "level";
	public int getLevel(){
		return getInt(CN_LEVEL);
	}
	public void setLevel(int value){
		put(CN_LEVEL, value);
	}

	public static final String CN_PARENT = "parent";
	public ProductCategory getParent(){
		return getAVObject(CN_PARENT);
	}
	public void setParent(ProductCategory value){
		put(CN_PARENT, value);
	}

	public static final String CN_TAG_GENERATION = "tagGeneration";
	public int getTagGeneration(){
		return getInt(CN_TAG_GENERATION);
	}
	public void setTagGenetation(int value){
		put(CN_TAG_GENERATION, value);
	}
}
