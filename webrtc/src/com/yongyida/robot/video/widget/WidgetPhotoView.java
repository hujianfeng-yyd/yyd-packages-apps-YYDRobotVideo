package com.yongyida.robot.video.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yongyida.robot.video.R;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.command.User;

/**
 * 用户视图
 * 
 * @author
 * @since 2016-04-10
 */
public class WidgetPhotoView extends LinearLayout {
	private Context mContext;
	private CircleImageView mImageView;
	private TextView mTvUserName;
	private User mUser;

	public WidgetPhotoView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public WidgetPhotoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	private void init() {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.widget_photo_view, (ViewGroup) getParent());
		int width = Utils.dp2px(mContext, 110);
		layout.setLayoutParams(new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT, 1.0f));
		mImageView = (CircleImageView) layout.findViewById(R.id.iv_contact);
		mTvUserName = (TextView) layout.findViewById(R.id.tv_contact);
		addView(layout);
	}

	public void setImageResource(int resId) {
		mImageView.setImageResource(resId);
	}

	public void setText(String name) {
		mTvUserName.setText(name);
	}

	public User getUser() {
		return mUser;
	}

	public void setUser(User user) {
		mUser = user;
	}

	public boolean getSelected() {
		return mImageView.isSelected();
	}

	public void setSelected(boolean selected) {
		mImageView.setSelected(selected);
		mTvUserName.setSelected(selected);
	}

	public void setSelectedColor(int selectedColor) {
		mImageView.setSelectedColor(selectedColor);
	}
}
