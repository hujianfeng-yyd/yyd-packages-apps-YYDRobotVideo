package com.yongyida.robot.video.activity;

import java.util.List;
import java.util.zip.Inflater;

import com.yongyida.robot.video.R;
import com.yongyida.robot.video.command.User;
import com.yongyida.robot.video.widget.CircleImageView;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FriendGridViewAdapter extends BaseAdapter{

	private Context mContext;
	private List<User> mUserList;
	private LayoutInflater inflate;
	private int SelectID = -1;

	public FriendGridViewAdapter(Context context, List<User> data){
		mContext = context;
		mUserList = data;
		inflate = LayoutInflater.from(context);
	}
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mUserList==null?0:mUserList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setSelectItem(int SelectID){
		this.SelectID = SelectID;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;		
		if(convertView == null){
			holder = new ViewHolder();
			convertView = inflate.inflate(R.layout.friendgridview_adapter, null);
			holder.iv_item_photo = (CircleImageView)convertView.findViewById(R.id.iv_item_photo);
			holder.tv_item_name = (TextView)convertView.findViewById(R.id.tv_item_name);
			holder.tv_item_id = (TextView)convertView.findViewById(R.id.tv_item_id);
			holder.friend_gridview_item = (LinearLayout)convertView.findViewById(R.id.friend_gridview_item);
			convertView.setTag(holder);
		}else{
		   holder = (ViewHolder)convertView.getTag();	
		}
		User userInfo = mUserList.get(position);
		if (userInfo != null && userInfo.getPicture() != null){
			holder.iv_item_photo.setImageBitmap(BitmapFactory.decodeFile(userInfo.getPicture()));			
		}
		else{
			holder.iv_item_photo.setImageResource(R.drawable.ic_photo_default4);			
		}
		if(SelectID == position){
			holder.friend_gridview_item.setBackgroundColor(Color.parseColor("#FFA500"));
			//holder.iv_item_photo.setBackgroundResource(R.drawable.ic_back_pressed);
		}else{
			holder.friend_gridview_item.setBackground(null);
		}
		
		holder.tv_item_name.setText(userInfo.getUserName());
		holder.tv_item_id.setText(userInfo.getId()+"");
		return convertView;
	}

	class ViewHolder{
		CircleImageView iv_item_photo;
		TextView tv_item_name;
		TextView tv_item_id;
		LinearLayout friend_gridview_item;
	}
	
}
