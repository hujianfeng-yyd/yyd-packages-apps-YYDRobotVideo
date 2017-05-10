package com.yongyida.robot.video.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import com.yongyida.robot.video.R;

/**
 * 旋转木马布局 Created by dalong on 2016/11/12.
 * 
 */
public class CarrouselLayout extends RelativeLayout {
	public static final String TAG = CarrouselLayout.class.getSimpleName();

	@SuppressWarnings("unused")
	private Context mContext;

	// 自动旋转 默认不自动
	private boolean mAutoRotation;

	// 旋转间隔时间 默认设置为2秒
	private int mRotationTime;

	// 旋转木马旋转半径 圆的半径
	private float mCarrouselR;

	// camera和旋转木马距离
	private float mDistance = 2f * mCarrouselR;

	// 旋转方向 分0顺时针和 1逆时针 俯视旋转木马看
	private int mRotateDirection;

	// handler
	private CarrouselRotateHandler mHandler;

	// 手势处理
	private GestureDetector mGestureDetector;

	// x旋转
	private int mRotationX;

	// Z旋转
	private int mRotationZ;

	// 旋转的角度
	private float mAngle = 0;

	// 旋转木马子view
	private List<View> mCarrouselViews = new ArrayList<>();

	// 旋转木马子view的数量
	private int mViewCount;

	// 半径扩散动画
	private ValueAnimator mAnimationR;

	// 记录最后的角度 用来记录上一次取消touch之后的角度
	private float mLastAngle;

	// 是否在触摸
	private boolean mIsTouching;

	// 旋转动画
	private ValueAnimator mRestAnimator;

	// 选中item
	private int mSelectItem;

	// item选中回调接口
	private OnCarrouselItemSelectedListener mOnCarrouselItemSelectedListener;

	// item点击回调接口
	private OnCarrouselItemClickListener mOnCarrouselItemClickListener;
	
	private OnCarrouselItemLongClickListener mOnCarrouselItemLongClickListener;

	// x轴旋转动画
	private ValueAnimator mXAnimation;

	// z轴旋转动画
	private ValueAnimator mZAnimation;

	public CarrouselLayout(Context context) {
		this(context, null);
	}

	public CarrouselLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CarrouselLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		mContext = context;

		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CarrouselLayout);
		mAutoRotation = typedArray.getBoolean(R.styleable.CarrouselLayout_autoRotation, false);
		mRotationTime = typedArray.getInt(R.styleable.CarrouselLayout_rotationTime, 2000);
		mCarrouselR = typedArray.getDimension(R.styleable.CarrouselLayout_r, 200);
		mRotateDirection = typedArray.getInt(R.styleable.CarrouselLayout_rotateDirection, 0);
		typedArray.recycle();

		mGestureDetector = new GestureDetector(context, getGestureDetectorController());
		initHandler();
	}

	/**
	 * 初始化handler对象
	 */
	private void initHandler() {
		mHandler = new CarrouselRotateHandler(mAutoRotation, mRotationTime, mRotateDirection) {
			@Override
			public void onRotating(CarrouselRotateDirection rotateDirection) {// 接受到需要旋转指令
				try {
					if (mViewCount != 0) {// 判断自动滑动从那边开始
						int perAngle = 0;
						switch (rotateDirection) {
						case clockwise:
							perAngle = 360 / mViewCount;
							break;
						case anticlockwise:
							perAngle = -360 / mViewCount;
							break;
						}
						if (mAngle == 360) {
							mAngle = 0f;
						}
						startAnimRotation(mAngle + perAngle, null);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	private GestureDetector.SimpleOnGestureListener getGestureDetectorController() {
		return new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				// 转换成弧度
				double radians = Math.toRadians(mRotationZ);
				mAngle += Math.cos(radians) * (distanceX / 4) + Math.sin(radians) * (distanceY / 4);

				// 初始化
				refreshLayout();
				return true;
			}
		};
	}

	/**
	 * 初始化 计算平均角度后各个子view的位置
	 */
	public void refreshLayout() {
		for (int i = 0; i < mCarrouselViews.size(); i++) {
			double radians = mAngle + 180 - i * 360 / mViewCount;
			float x0 = (float) Math.sin(Math.toRadians(radians)) * mCarrouselR;
			float y0 = (float) Math.cos(Math.toRadians(radians)) * mCarrouselR;
			float scale0 = (mDistance - y0) / (mDistance + mCarrouselR);
			mCarrouselViews.get(i).setScaleX(scale0);
			mCarrouselViews.get(i).setScaleY(scale0);

			float rotationX_y = (float) Math.sin(Math.toRadians(mRotationX * Math.cos(Math.toRadians(radians))))
					* mCarrouselR;
			float rotationZ_y = -(float) Math.sin(Math.toRadians(-mRotationZ)) * x0;
			float rotationZ_x = (((float) Math.cos(Math.toRadians(-mRotationZ)) * x0) - x0);
			mCarrouselViews.get(i).setTranslationX(x0 + rotationZ_x);
			mCarrouselViews.get(i).setTranslationY(rotationX_y + rotationZ_y);

			// 不显示背面的小视图
			mCarrouselViews.get(i).setVisibility(scale0 > 0.5f ? View.VISIBLE : View.INVISIBLE);
		}
		List<View> arrayViewList = new ArrayList<>();
		arrayViewList.clear();
		for (int i = 0; i < mCarrouselViews.size(); i++) {
			arrayViewList.add(mCarrouselViews.get(i));
		}
		sortList(arrayViewList);
		postInvalidate();
	}

	/**
	 * 排序 對子View 排序，然后根据变化选中是否重绘,这样是为了实现view 在显示的时候来控制当前要显示的是哪三个view，可以改变排序看下效果
	 * 
	 * @param list
	 */
	@SuppressWarnings("unchecked")
	private <T> void sortList(List<View> list) {
		@SuppressWarnings("rawtypes")
		Comparator comparator = new SortComparator();
		T[] array = list.toArray((T[]) new Object[list.size()]);
		Arrays.sort(array, comparator);
		int i = 0;
		ListIterator<T> it = (ListIterator<T>) list.listIterator();
		while (it.hasNext()) {
			it.next();
			it.set(array[i++]);
		}
		for (int j = 0; j < list.size(); j++) {
			list.get(j).bringToFront();
		}
	}

	/**
	 * 筛选器
	 */
	private class SortComparator implements Comparator<View> {
		@Override
		public int compare(View o1, View o2) {
			return (int) (1000 * o1.getScaleX() - 1000 * o2.getScaleX());
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		refreshLayout();
		if (mAutoRotation) {
			mHandler.sendEmptyMessageDelayed(CarrouselRotateHandler.mMsgWhat, mHandler.getmRotationTime());
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed) {
			checkChildView();
			startAnimationR();
		}
	}

	/**
	 * 旋转木马半径打开动画
	 */
	public void startAnimationR() {
		startAnimationR(1f, mCarrouselR);
	}

	/**
	 * 旋转木马半径动画
	 * 
	 * @param isOpen
	 *            是否打开 否则关闭
	 */
	public void startAnimationR(boolean isOpen) {
		if (isOpen) {
			startAnimationR(1f, mCarrouselR);
		}
		else {
			startAnimationR(mCarrouselR, 1f);
		}
	}

	/**
	 * 半径扩散、收缩动画 根据设置半径来实现
	 * 
	 * @param from
	 * @param to
	 */
	public void startAnimationR(float from, float to) {
		mAnimationR = ValueAnimator.ofFloat(from, to);
		mAnimationR.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				mCarrouselR = (Float) valueAnimator.getAnimatedValue();
				refreshLayout();
			}
		});
		mAnimationR.setInterpolator(new DecelerateInterpolator());
		mAnimationR.setDuration(2000);
		mAnimationR.start();
	}

	public void checkChildView() {
		// 先清空views里边可能存在的view防止重复
		for (int i = 0; i < mCarrouselViews.size(); i++) {
			mCarrouselViews.remove(i);
		}

		final int count = getChildCount(); // 获取子View的个数
		mViewCount = count;
		for (int i = 0; i < count; i++) {
			final View view = getChildAt(i); // 获取指定的子view
			final int position = i;
			mCarrouselViews.add(view);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mOnCarrouselItemClickListener != null) {
						mOnCarrouselItemClickListener.onItemClick(view, position);
					}
				}
			});
			
			view.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					if (mOnCarrouselItemLongClickListener != null) {
						mOnCarrouselItemLongClickListener.onItemClick(view, position);
						return true;
					}
					
					return false;
				}
			});
		}
	}

	/**
	 * 复位
	 */
	private void restView() {
		if (mViewCount == 0) {
			return;
		}

		float resultAngle = 0;
		// 平均角度
		float aveAngle = 360 / mViewCount;
		if (mAngle < 0) {
			aveAngle = -aveAngle;
		}

		float minvalue = (int) (mAngle / aveAngle) * aveAngle;// 最小角度
		float maxvalue = (int) (mAngle / aveAngle) * aveAngle + aveAngle;// 最大角度
		if (mAngle >= 0) {// 分为是否小于0的情况
			if (mAngle - mLastAngle > 0) {
				resultAngle = maxvalue;
			}
			else {
				resultAngle = minvalue;
			}
		}
		else {
			if (mAngle - mLastAngle < 0) {
				resultAngle = maxvalue;
			}
			else {
				resultAngle = minvalue;
			}
		}
		startAnimRotation(resultAngle, null);
	}

	/**
	 * 动画旋转
	 * 
	 * @param resultAngle
	 * @param complete
	 */
	private void startAnimRotation(float resultAngle, final Runnable complete) {
		if (mAngle == resultAngle) {
			return;
		}

		mRestAnimator = ValueAnimator.ofFloat(mAngle, resultAngle);
		// 设置旋转匀速插值器
		mRestAnimator.setInterpolator(new LinearInterpolator());
		mRestAnimator.setDuration(300);
		mRestAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				if (mIsTouching == false) {
					mAngle = (Float) animation.getAnimatedValue();
					refreshLayout();
				}
			}
		});
		mRestAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (mIsTouching == false) {
					mSelectItem = calculateItem();
					if (mSelectItem < 0) {
						mSelectItem = mViewCount + mSelectItem;
					}
					if (mOnCarrouselItemSelectedListener != null) {
						mOnCarrouselItemSelectedListener.selected(mCarrouselViews.get(mSelectItem), mSelectItem);
					}
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});

		if (complete != null) {
			mRestAnimator.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					complete.run();
				}

				@Override
				public void onAnimationCancel(Animator animation) {

				}

				@Override
				public void onAnimationRepeat(Animator animation) {

				}
			});
		}
		mRestAnimator.start();
	}

	/**
	 * 通过角度计算是第几个item
	 *
	 * @return
	 */
	private int calculateItem() {
		return (int) (mAngle / (360.0f / mViewCount)) % mViewCount;
	}

	/**
	 * 触摸操作
	 *
	 * @param event
	 * @return
	 */
	private boolean onTouch(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mLastAngle = mAngle;
			mIsTouching = true;
		}

		boolean result = mGestureDetector.onTouchEvent(event);
		if (result) {
			this.getParent().requestDisallowInterceptTouchEvent(true);// 通知父控件勿拦截本控件
		}

		if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
			mIsTouching = false;
			restView();
			return true;
		}
		return true;
	}

	/**
	 * 触摸方法
	 *
	 * @param event
	 * @return
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		setCanAutoRotation(event);
		return true;
	}

	/**
	 * 触摸停止计时器
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		onTouch(ev);
		setCanAutoRotation(ev);
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * 触摸时停止自动加载
	 * 
	 * @param event
	 */
	public void setCanAutoRotation(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			stopAutoRotation();
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			resumeAutoRotation();
			break;
		}
	}

	/**
	 * 停止自动加载
	 */
	public void stopAutoRotation() {
		if (mHandler != null && mAutoRotation) {
			mHandler.removeMessages(CarrouselRotateHandler.mMsgWhat);
		}
	}

	/**
	 * 从新启动自动加载
	 */
	public void resumeAutoRotation() {
		if (mHandler != null && mAutoRotation) {
			mHandler.sendEmptyMessageDelayed(CarrouselRotateHandler.mMsgWhat, mHandler.getmRotationTime());
		}
	}

	/**
	 * 获取所有的view
	 *
	 * @return
	 */
	public List<View> getViews() {
		return mCarrouselViews;
	}

	/**
	 * 获取角度
	 *
	 * @return
	 */
	public float getAngle() {
		return mAngle;
	}

	/**
	 * 设置角度
	 *
	 * @param angle
	 */
	public void setAngle(float angle) {
		mAngle = angle;
	}

	/**
	 * 获取距离
	 *
	 * @return
	 */
	public float getDistance() {
		return mDistance;
	}

	/**
	 * 设置距离
	 *
	 * @param distance
	 */
	public void setDistance(float distance) {
		mDistance = distance;
	}

	/**
	 * 获取半径
	 *
	 * @return
	 */
	public float getR() {
		return mCarrouselR;
	}

	/**
	 * 获取选择是第几个item
	 *
	 * @return
	 */
	public int getSelectItem() {
		return mSelectItem;
	}

	/**
	 * 设置选中方法
	 *
	 * @param selectItem
	 */
	public void setSelectItem(int selectItem) {
		if (selectItem >= 0) {
			float angle = 0;
			if (getSelectItem() == 0) {
				if (selectItem == mCarrouselViews.size() - 1) {
					angle = mAngle - (360 / mViewCount);
				}
				else {
					angle = mAngle + (360 / mViewCount);
				}
			}
			else if (getSelectItem() == mCarrouselViews.size() - 1) {
				if (selectItem == 0) {
					angle = mAngle + (360 / mViewCount);
				}
				else {
					angle = mAngle - (360 / mViewCount);
				}
			}
			else {
				if (selectItem > getSelectItem()) {
					angle = mAngle + (360 / mViewCount);
				}
				else {
					angle = mAngle - (360 / mViewCount);
				}
			}

			float resultAngle = 0;
			float part = 360 / mViewCount;
			if (angle < 0) {
				part = -part;
			}
			// 最小角度
			float minvalue = (int) (angle / part) * part;
			// 最大角度
			float maxvalue = (int) (angle / part) * part;
			if (angle >= 0) {// 分为是否小于0的情况
				if (angle - mLastAngle > 0) {
					resultAngle = maxvalue;
				}
				else {
					resultAngle = minvalue;
				}
			}
			else {
				if (angle - mLastAngle < 0) {
					resultAngle = maxvalue;
				}
				else {
					resultAngle = minvalue;
				}
			}

			if (mViewCount > 0)
				startAnimRotation(resultAngle, null);
		}
	}

	/**
	 * 设置半径
	 *
	 * @param r
	 */
	public CarrouselLayout setR(float r) {
		mCarrouselR = r;
		mDistance = 2f * r;
		return this;
	}

	/**
	 * 选中回调接口实现
	 *
	 * @param mOnCarrouselItemSelectedListener
	 */
	public void setOnCarrouselItemSelectedListener(OnCarrouselItemSelectedListener listener) {
		mOnCarrouselItemSelectedListener = listener;
	}

	/**
	 * 点击事件回调
	 *
	 * @param mOnCarrouselItemClickListener
	 */
	public void setOnCarrouselItemClickListener(OnCarrouselItemClickListener listener) {
		mOnCarrouselItemClickListener = listener;
	}
	
	/**
	 * 长点击事件回调
	 *
	 * @param mOnCarrouselItemClickListener
	 */
	public void setOnCarrouselItemLongClickListener(OnCarrouselItemLongClickListener listener) {
		mOnCarrouselItemLongClickListener = listener;
	}

	/**
	 * 设置是否自动切换
	 *
	 * @param autoRotation
	 */
	public CarrouselLayout setAutoRotation(boolean autoRotation) {
		mAutoRotation = autoRotation;
		mHandler.setAutoRotation(autoRotation);
		return this;
	}

	/**
	 * 获取自动切换时间
	 *
	 * @return
	 */
	public long getAutoRotationTime() {
		return mHandler.getmRotationTime();
	}

	/**
	 * 设置自动切换时间间隔
	 *
	 * @param autoRotationTime
	 */
	public CarrouselLayout setAutoRotationTime(long autoRotationTime) {
		if (mHandler != null)
			mHandler.setmRotationTime(autoRotationTime);
		return this;
	}

	/**
	 * 是否自动切换
	 *
	 * @return
	 */
	public boolean isAutoRotation() {
		return mAutoRotation;
	}

	/**
	 * 设置自动选择方向
	 * 
	 * @param mCarrouselRotateDirection
	 * @return
	 */
	public CarrouselLayout setAutoScrollDirection(CarrouselRotateDirection direction) {
		if (mHandler != null)
			mHandler.setmRotateDirection(direction);
		return this;
	}

	public void createXAnimation(int from, int to, boolean start) {
		if (mXAnimation != null)
			if (mXAnimation.isRunning() == true)
				mXAnimation.cancel();
		mXAnimation = ValueAnimator.ofInt(from, to);
		mXAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mRotationX = (Integer) animation.getAnimatedValue();
				refreshLayout();
			}
		});
		mXAnimation.setInterpolator(new LinearInterpolator());
		mXAnimation.setDuration(2000);
		if (start)
			mXAnimation.start();
	}

	public ValueAnimator createZAnimation(int from, int to, boolean start) {
		if (mZAnimation != null)
			if (mZAnimation.isRunning() == true)
				mZAnimation.cancel();
		mZAnimation = ValueAnimator.ofInt(from, to);
		mZAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mRotationZ = (Integer) animation.getAnimatedValue();
				refreshLayout();
			}
		});
		mZAnimation.setInterpolator(new LinearInterpolator());
		mZAnimation.setDuration(2000);
		if (start)
			mZAnimation.start();
		return mZAnimation;
	}

	public CarrouselLayout setRotationX(int rotationX) {
		mRotationX = rotationX;
		return this;
	}

	public float getRotationX() {
		return mRotationX;
	}

	public CarrouselLayout setRotationZ(int rotationZ) {
		mRotationZ = rotationZ;
		return this;
	}

	public int getRotationZ() {
		return mRotationZ;
	}

	public ValueAnimator getRestAnimator() {
		return mRestAnimator;
	}

	public ValueAnimator getAnimationR() {
		return mAnimationR;
	}

	public void setAnimationX(ValueAnimator xAnimation) {
		mXAnimation = xAnimation;
	}

	public ValueAnimator getAnimationX() {
		return mXAnimation;
	}

	public void setAnimationZ(ValueAnimator zAnimation) {
		mZAnimation = zAnimation;
	}

	public ValueAnimator getAnimationZ() {
		return mZAnimation;
	}

	/**
	 * 旋转木马旋转方向
	 */
	public enum CarrouselRotateDirection {
		clockwise, //顺时针方向
		anticlockwise //逆时针方向
	}

	/**
	 * 点击回调接口
	 */
	public interface OnCarrouselItemClickListener {
		void onItemClick(View view, int position);
	}

	/**
	 * 选择回调接口
	 */
	public interface OnCarrouselItemSelectedListener {
		void selected(View view, int position);
	}
	
	/**
	 * 长点击回调接口
	 */
	public interface OnCarrouselItemLongClickListener {
		void onItemClick(View view, int position);
	}

	/**
	 * 旋转木马自动旋转控制handler
	 */
	@SuppressLint("HandlerLeak")
	public abstract class CarrouselRotateHandler extends Handler {
		//消息what
		public static final int mMsgWhat = 1000;
		//是否旋转
		private boolean isAutoRotation;
		//旋转事件间隔
		private long mRotationTime;
		//消息对象
		private Message message;
		//旋转方向
		private CarrouselRotateDirection mRotateDirection;

		public CarrouselRotateHandler(boolean isAutoRotation, int mRotationTime, int mRotateDirection) {
			this.isAutoRotation = isAutoRotation;
			this.mRotationTime = mRotationTime;
			this.mRotateDirection = mRotateDirection == 0 ? CarrouselRotateDirection.clockwise
					: CarrouselRotateDirection.anticlockwise;
			message = createMessage();
			setAutoRotation(isAutoRotation);
		}

		/**
		 * 消息处理
		 * 
		 * @param msg
		 */
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case mMsgWhat:
				//如果自动旋转
				if (isAutoRotation) {
					//旋转通知
					onRotating(mRotateDirection);
					//再次发送消息  循环
					sendMessage();
				}
				break;
			}
		}

		/**
		 * 需要旋转通知方法
		 */
		public abstract void onRotating(CarrouselRotateDirection mRotateDirection);

		/**
		 * 创建消息对象
		 * 
		 * @return
		 */
		private Message createMessage() {
			Message message = new Message();
			message.what = mMsgWhat;
			return message;
		}

		/**
		 * 发送消息
		 */
		public void sendMessage() {
			//清除所有mMsgWhat的消息
			try {
				removeMessages(mMsgWhat);
			}
			catch (Exception e) {
			}
			message = createMessage();
			this.sendMessageDelayed(message, mRotationTime);
		}

		/**
		 * 获取是否自动旋转
		 * 
		 * @return
		 */
		public boolean isAutoRotation() {
			return isAutoRotation;
		}

		/**
		 * 设置是否自动旋转
		 * 
		 * @param autoRotation
		 */
		public void setAutoRotation(boolean autoRotation) {
			isAutoRotation = autoRotation;
			if (autoRotation) {//如果需要旋转
				sendMessage();
			}
			else {//不需要旋转  需要清除所有消息队列中的消息
				removeMessages(mMsgWhat);
			}
		}

		/**
		 * 获取旋转事件间隔
		 * 
		 * @return
		 */
		public long getmRotationTime() {
			return mRotationTime;
		}

		/**
		 * 设置旋转事件间隔
		 * 
		 * @param mRotationTime
		 */
		public void setmRotationTime(long mRotationTime) {
			this.mRotationTime = mRotationTime;
		}

		/**
		 * 获取旋转方向
		 * 
		 * @return
		 */
		public CarrouselRotateDirection getmRotateDirection() {
			return mRotateDirection;
		}

		/**
		 * 设置旋转方向
		 * 
		 * @param mRotateDirection
		 */
		public void setmRotateDirection(CarrouselRotateDirection mRotateDirection) {
			this.mRotateDirection = mRotateDirection;
		}
	}

}
