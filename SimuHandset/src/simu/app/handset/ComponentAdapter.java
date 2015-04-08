package simu.app.handset;

import java.util.List;

import simu.app.handset.R;
import simu.avsubobjects.ProductCategory;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ComponentAdapter extends BaseAdapter {
	private List<ProductCategory> mList;
	private Context mContext;

	public ComponentAdapter(Context pContext, List<ProductCategory> pList) {
		this.mContext = pContext;
		this.mList = pList;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 下面是重要代码
	 */
	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	// LayoutInflater _LayoutInflater=LayoutInflater.from(mContext);
	// convertView=_LayoutInflater.inflate(R.layout.componentitem, null);
	// if(convertView!=null)
	// {
	// TextView _TextView1=(TextView)convertView.findViewById(R.id.textView1);
	// TextView _TextView2=(TextView)convertView.findViewById(R.id.textView2);
	// _TextView1.setText(mList.get(position).getTempSN());
	// _TextView2.setText(mList.get(position).getCategoryName());
	// }
	// return convertView;
	// }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (null == convertView) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.group_item_view, null);
			holder = new ViewHolder();
			convertView.setTag(holder);
			holder.groupItem = (TextView)convertView.findViewById(R.id.groupItem);
			holder.groupItem2 = (TextView)convertView.findViewById(R.id.groupItem2);
		}else {
			holder = (ViewHolder)convertView.getTag();
		}
		holder.groupItem.setTextColor(Color.BLACK);  
        holder.groupItem.setText(mList.get(position).getTempSN()); 
        holder.groupItem2.setText(mList.get(position).getCategoryName());
          
        return convertView; 

	}

	static class ViewHolder {
		TextView groupItem;
		TextView groupItem2;
	}

}
