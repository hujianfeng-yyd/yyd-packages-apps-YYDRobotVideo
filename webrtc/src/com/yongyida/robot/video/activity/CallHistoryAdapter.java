package com.yongyida.robot.video.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import com.yongyida.robot.video.RobotApplication;
import com.yongyida.robot.video.R;
import com.yongyida.robot.video.sdk.CallHistory;

public class CallHistoryAdapter extends BaseAdapter {
	public static final int DAYMS = 24 * 60 * 60 * 1000;

	public interface OnListItemClickListener {
		public void onClick(int position, CallHistory history);
	}

	private List<CallHistory> mHistoryList;
	private Context mContext;
	private OnListItemClickListener mOnListItemClickListener;

	public CallHistoryAdapter(Context context, List<CallHistory> list) {
		mContext = context;
		mHistoryList = list;
	}

	/*
	 * 设置列表项监听器
	 */
	public void setListItemClickListener(OnListItemClickListener listener) {
		mOnListItemClickListener = listener;
	}

	// 获取ListView的项个数
	public int getCount() {
		return mHistoryList.size();
	}

	// 获取项
	public Object getItem(int position) {
		return mHistoryList.get(position);
	}

	// 获取项的ID
	public long getItemId(int position) {
		return position;
	}

	// 获取项的类型
	public int getItemViewType(int position) {
		return 0;
	}

	// 获取项的类型数
	public int getViewTypeCount() {
		return 1;
	}

	// 获取View
	@SuppressLint("InflateParams")
	public View getView(final int position, View convertView, ViewGroup parent) {
		final CallHistory history = mHistoryList.get(position);

		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_callhistory, null);
			holder = new ViewHolder();
			holder.line = (LinearLayout) convertView.findViewById(R.id.layout_callhistory);
			holder.name = (TextView) convertView.findViewById(R.id.ch_name);
			holder.id = (TextView) convertView.findViewById(R.id.ch_id);
			holder.time = (TextView) convertView.findViewById(R.id.ch_time);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		String dispName = history.getUser().getUserName();
		String dispNumber = history.getUser().getCallNumber();
		Date date = new Date(history.getCallTime());
		String temple = "M月d日";
		if (System.currentTimeMillis() - history.getCallTime() < DAYMS)
			temple = "HH:mm";
		String dispTime = new java.text.SimpleDateFormat(temple, RobotApplication.getInstance().getLocale())
				.format(date);

		if (!TextUtils.isEmpty(dispName)) {
			holder.name.setText(dispName);
		}
		else {
			//holder.name.setVisibility(View.GONE);  //隐藏会有问题
			holder.name.setText("");
		}

		if (dispNumber != null && !dispNumber.equals(dispName)) {
			holder.id.setText(dispNumber);
		}
		else {
			//holder.id.setVisibility(View.GONE);  //隐藏会有问题
			holder.id.setText("");
		}

		holder.time.setText(dispTime);

		holder.line.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnListItemClickListener != null) {
					mOnListItemClickListener.onClick(position, history);
				}
			}
		});
		return convertView;
	}

	static class ViewHolder {
		private LinearLayout line;
		private TextView name;
		private TextView id;
		private TextView time;
	}
}
