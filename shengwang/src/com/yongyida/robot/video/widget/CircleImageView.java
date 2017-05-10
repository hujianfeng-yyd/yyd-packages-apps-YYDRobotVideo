package com.yongyida.robot.video.widget;

import com.yongyida.robot.video.comm.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 圆形图片
 * 
 */
public class CircleImageView extends ImageView {
	private int mRadius = 0;
	private Matrix mMatrix;
	private int mBorderWidth = 3;
	private int mBorderColor = Color.WHITE;
	private int mSelectedColor = Color.CYAN;
	
	public CircleImageView(Context context) {
		super(context);
	}

	public CircleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public int getBorderWidth() {
		return mBorderWidth;
	}

	public void setBorderWidth(int borderWidth) {
		mBorderWidth = borderWidth;
	}
	
	public int getBorderColor() {
		return mBorderColor;
	}

	public void setBorderColor(int borderColor) {
		mBorderColor = borderColor;
	}

	public int getSelectedColor() {
		return mSelectedColor;
	}

	public void setSelectedColor(int selectedColor) {
		mSelectedColor = selectedColor;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int w = getWidth();
		int h = getHeight();
		mRadius = (w <= h ? w : h) /2;
	}
	
	@Override
	public void draw(Canvas canvas) {
		BitmapDrawable drawable = (BitmapDrawable)getDrawable();
		if (drawable == null)
			return;
		
		Bitmap bitmap = drawable.getBitmap();
		BitmapShader shader = new BitmapShader(bitmap,
				BitmapShader.TileMode.CLAMP,
				BitmapShader.TileMode.CLAMP);
		
		int border = Utils.dp2px(getContext(), mBorderWidth);
		float scale = (getWidth()) * 1.0f / Math.min(bitmap.getWidth() - border - border, bitmap.getHeight() - border - border);
		mMatrix = new Matrix();
		mMatrix.setScale(scale, scale);
		shader.setLocalMatrix(mMatrix);
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setShader(shader);
		drawCircleBorder(
				canvas, 
				mRadius + getPaddingLeft(), 
				mRadius + getPaddingTop(), 
				mRadius - getPaddingLeft() - getPaddingRight(), 
				isSelected()?mSelectedColor:mBorderColor);
		canvas.drawCircle(
				mRadius + getPaddingLeft(), 
				mRadius + getPaddingTop(), 
				mRadius - getPaddingLeft() - getPaddingRight() - border, 
				paint);
	}
	
	private void drawCircleBorder(Canvas canvas, int cx, int cy, int radius, int color) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);
		paint.setDither(true);
		paint.setFilterBitmap(true);
		canvas.drawCircle(cx, cy, radius, paint);
	}
}
