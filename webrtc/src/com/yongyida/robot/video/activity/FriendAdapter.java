package com.yongyida.robot.video.activity;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import com.yongyida.robot.video.R;
import com.yongyida.robot.video.command.User;
import com.yongyida.robot.video.widget.CircleImageView;

public class FriendAdapter extends BaseAdapter {
	private List<User> mUserList;
	private Context mContext;

	public FriendAdapter(Context context, List<User> data) {
		mContext = context;
		mUserList = data;
	}

	public int getCount() {
		return mUserList.size();
	}

	public Object getItem(int position) {
		return mUserList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		return 0;
	}

	public int getViewTypeCount() {
		return 1;
	}

	// 获取View
	public View getView(final int position, View convertView, ViewGroup parent) {
		final User user = mUserList.get(position);
		CircleImageView imageView = new CircleImageView(mContext);
		imageView.setSelectedColor(Color.CYAN);
		if (user != null && user.getPicture() != null)
			imageView.setImageBitmap(BitmapFactory.decodeFile(user.getPicture()));
		else
			imageView.setImageResource(R.drawable.ic_photo_default1);

		return imageView;
	}
}
