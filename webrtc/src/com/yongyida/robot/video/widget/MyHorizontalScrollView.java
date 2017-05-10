package com.yongyida.robot.video.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.yongyida.robot.video.comm.Utils;

public class MyHorizontalScrollView extends HorizontalScrollView {
	public static final String TAG = "MyHorizontalScrollView";
	private static final int MAX_VISIBLE_COUNT = 5;
	private static final int DEFAULT_PADDING = 3;
	private static final float INIT_RATE = 0.80F;
	private static final float INCR_RATE = 0.25F;

	private ListAdapter mAdapter;
	private LinearLayout mContainer;
	private int mVisibleCount;
	private int mPadding;
	private int mChildAverWidth;
	private int mFirstIndex = -1;
	private int mCurrentIndex;
	private OnItemClickListener mOnItemClickListener;
	private OnItemClickListener mOnItemLongClickListener;
	private View mSelectedView;

	public MyHorizontalScrollView(Context context) {
		super(context);
		init();
	}

	public MyHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MyHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mContainer = (LinearLayout) getChildAt(0);
	}

	public ListAdapter getAdapter() {
		return mAdapter;
	}

	public void setAdapter(ListAdapter adapter) {
		mAdapter = adapter;
		mContainer = (LinearLayout) getChildAt(0);

		int padding = Utils.dp2px(getContext(), DEFAULT_PADDING);
		for (int i = 0; i < mAdapter.getCount(); i++) {
			View view = mAdapter.getView(i, null, mContainer);
			view.setOnClickListener(mOnClickListener);
			view.setOnLongClickListener(mOnLongClickListener);
			view.setPadding(padding, 0, padding, 0);
			mContainer.addView(view, i);
		}

		mVisibleCount = getVisibleCount(mContainer.getChildCount());
		reLayoutChilds(0);
	}

	private int getVisibleCount(int childCount) {
		if (childCount < MAX_VISIBLE_COUNT)
			return childCount;
		else
			return MAX_VISIBLE_COUNT;
	}

	public void clear() {
		if (mContainer != null) {
			mContainer.removeAllViews();
		}
	}

	private int onSizeChangedIndex = 0;

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (onSizeChangedIndex == 0 && mContainer != null) {
			reLayoutChilds(0);
		}
		onSizeChangedIndex++;
	}

	private int getChildAverWidth() {
		int scrollViewWidth = getWidth();
		mPadding = Utils.dp2px(this.getContext(), DEFAULT_PADDING + DEFAULT_PADDING);
		return (int) (Math.ceil((double) scrollViewWidth / MAX_VISIBLE_COUNT - mPadding));
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		int firstIndex = getFirstViewIndex(getScrollX());

		if (firstIndex < mContainer.getChildCount() - 4) {
			if (mFirstIndex != firstIndex)
				mFirstIndex = firstIndex;

			mCurrentIndex = reSizeChilds(mFirstIndex, getScrollX());
			if (mOnItemClickListener != null) {
				View v = mContainer.getChildAt(mCurrentIndex);
				switchSelectedView(v, true);
				mOnItemClickListener.onItemClick(v, mCurrentIndex);
			}
		}
		super.onScrollChanged(l, t, oldl, oldt);
	}

	private int getFirstViewIndex(int scrollx) {
		return (int) (scrollx / Math.ceil(mChildAverWidth * (INIT_RATE - INCR_RATE) + mPadding));
	}

	private double getFirstRate(int visibleCount) {
		return INIT_RATE + (INCR_RATE * ((MAX_VISIBLE_COUNT - visibleCount) / 2));
	}

	private int getMarginLeft(int visibleCount, int childAveWidth, int padding) {
		int count = MAX_VISIBLE_COUNT / 2 - visibleCount / 2;
		int totalWidth = 0;

		double rate = INIT_RATE;
		for (int i = 0; i < count; i++) {
			totalWidth += (int) Math.ceil(childAveWidth * rate) + padding;
			;
			rate += INCR_RATE;
		}

		return totalWidth;
	}

	private void reLayoutChilds(int firstIndex) {
		mChildAverWidth = getChildAverWidth();

		int mid = mVisibleCount / 2;
		double firstRate = getFirstRate(mVisibleCount);
		int setCount = Math.min(mContainer.getChildCount(), mVisibleCount + 1);
		int width;
		int marginLeft = 0;
		if (mVisibleCount < MAX_VISIBLE_COUNT)
			marginLeft = getMarginLeft(mVisibleCount, mChildAverWidth, mPadding);

		for (int i = 0; i < setCount && firstIndex + i < mContainer.getChildCount(); i++) {
			width = (int) Math.ceil(mChildAverWidth * firstRate) + mPadding;
			setViewSize(mContainer.getChildAt(firstIndex + i), width, width, (i == 0) ? marginLeft : 0);

			if (i < mVisibleCount)
				firstRate += (i < mid) ? INCR_RATE : -INCR_RATE;
		}
	}

	private int reSizeChilds(int firstIndex, int scrollx) {
		int mid = mVisibleCount / 2;
		double firstRate = getFirstRate(mVisibleCount);
		int firstWidth = (int) Math.ceil(mChildAverWidth * (firstRate - INCR_RATE)) + mPadding;
		double scale = (double) (scrollx % firstWidth) / firstWidth;
		double curWidth, preWidth, sw;
		int width;
		int maxWidth = 0;
		int maxWidthIndex = firstIndex;

		for (int i = 0; i < firstIndex; ++i) {
			if (mContainer.getChildAt(i).getWidth() != firstWidth)
				setViewSize(mContainer.getChildAt(i), firstWidth, firstWidth, 0);
		}

		for (int i = 0; i < mVisibleCount + 1 && firstIndex + i < mContainer.getChildCount(); i++) {
			// view基础大小
			curWidth = Math.ceil(mChildAverWidth * firstRate) + mPadding;
			preWidth = Math.ceil(mChildAverWidth * (firstRate + ((i <= mid) ? -INCR_RATE : INCR_RATE))) + mPadding;

			// view动态大小
			sw = (curWidth - preWidth) * scale;
			width = (int) Math.ceil(curWidth - sw);
			setViewSize(mContainer.getChildAt(firstIndex + i), width, width, 0);
			if (width > maxWidth) {
				maxWidth = width;
				maxWidthIndex = firstIndex + i;
			}

			if (i < mVisibleCount)
				firstRate += (i < mid) ? INCR_RATE : -INCR_RATE;
		}

		return maxWidthIndex;
	}

	private void setViewSize(View view, int w, int h, int left) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(w, h);
		params.setMargins(left, 0, 0, 0);
		view.setLayoutParams(params);
	}

	public interface OnItemClickListener {
		public void onItemClick(View view, int position);
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		mOnItemClickListener = onItemClickListener;
	}

	public void setOnItemLongClickListener(OnItemClickListener onItemClickListener) {
		mOnItemLongClickListener = onItemClickListener;
	}

	private OnClickListener mOnClickListener = new OnClickListener() {
		public void onClick(View view) {
			boolean selected = switchSelectedView(view, !view.isSelected());
			if (mOnItemClickListener != null) {
				mCurrentIndex = mContainer.indexOfChild(view);
				mOnItemClickListener.onItemClick(selected ? view : null, selected ? mCurrentIndex : -1);
			}
		}
	};

	private OnLongClickListener mOnLongClickListener = new OnLongClickListener() {
		public boolean onLongClick(View view) {
			if (mOnItemLongClickListener != null) {
				mCurrentIndex = mContainer.indexOfChild(view);
				mOnItemLongClickListener.onItemClick(view, mCurrentIndex);
			}
			return true;
		}
	};

	private boolean switchSelectedView(View view, boolean value) {
		if (mSelectedView != null) {
			if (mSelectedView != view) {
				mSelectedView.setSelected(!value);
				view.setSelected(value);
				mSelectedView = view;
			}
			else {
				mSelectedView.setSelected(value);
			}
		}
		else {
			view.setSelected(value);
			mSelectedView = view;
		}

		return value;
	}
}
