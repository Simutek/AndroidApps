package simu.avsubobjects;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVClassName;

@AVClassName("ProductCategory")
public class ProductCategory extends AVObject {
	public final String CN_CATEGORY_NAME = "categoryName";
	public String getCategoryName(){
		return getString(CN_CATEGORY_NAME);
	}
	public void setCategoryName(String value){
		put(CN_CATEGORY_NAME, value);
	}

	public final String CN_TEMP_SN = "vendorCode";
	public String getInitializeSN(){
		return getString(CN_TEMP_SN);
	}
	public void setInitializeSN(String value){
		put(CN_TEMP_SN, value);
	}

	public final String CN_LEVEL = "level";
	public int getLevel(){
		return getInt(CN_LEVEL);
	}
	public void setLevel(int value){
		put(CN_LEVEL, value);
	}

	public final String CN_PARENT = "parent";
	public ProductCategory getParent(){
		return getAVObject(CN_PARENT);
	}
	public void setParent(ProductCategory value){
		put(CN_PARENT, value);
	}

	public final String CN_TAG_GENERATION = "tagGeneration";
	public int getTagGeneration(){
		return getInt(CN_TAG_GENERATION);
	}
	public void setTagGenetation(int value){
		put(CN_TAG_GENERATION, value);
	}
}
