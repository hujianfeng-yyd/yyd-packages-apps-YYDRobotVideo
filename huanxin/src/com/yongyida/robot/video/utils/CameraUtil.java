package com.yongyida.robot.video.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;

@SuppressWarnings("deprecation")
public class CameraUtil {
	private static final String TAG = CameraUtil.class.getSimpleName();

	/**
	 * 尺寸从小到大排序
	 */
	private static class SizeComparator implements Comparator<Camera.Size> {
		public int compare(Size lhs, Size rhs) {
			if (lhs.width == rhs.width)
				return 0;
			else if (lhs.width > rhs.width)
				return 1;
			else
				return -1;
		}
	}

	/**
	 * 返回最优的图像大小
	 *
	 */
	public static Size getOptimalSize(List<Camera.Size> list, int width, int height) {
		Collections.sort(list, new SizeComparator());

		//如果宽高都相等，即最优。
		for (Size s : list) {
			if (s.width == width && s.height == height)
				return s;
		}
		
		//如果宽或高相等，则次优。
		for (Size s : list) {
			if (s.width == width || s.height == height)
				return s;
		}
		
		//选择第一个大于指定的size。
		for (Size s : list) {
			if (s.width*s.height >= width*height)
				return s;
		}
		
		//返回第一个size
		return list.get(0);
	}

	/**
	 * 打印尺寸列表
	 * 
	 */
	public static void printSizeList(String sizeName, List<Size> sizes) {
		log.d(TAG, sizeName);

		for (Size s : sizes) {
			log.d(TAG, s.width + " x " + s.height);
		}
	}

	/**
	 * 打印摄像头支持的对焦模式
	 * 
	 */
	public static void printSupportFocusModes(Camera.Parameters params) {
		log.d(TAG, "SupportFocusModes:");

		List<String> focusModes = params.getSupportedFocusModes();
		for (String mode : focusModes) {
			log.d(TAG, mode);
		}
	}

	/**
	 * 打印摄像头支持的颜色格式 视频编码时要设置视频输出帧的颜色格式，如：COLOR_FormatYUV420Planar
	 * 
	 */
	@SuppressLint("NewApi")
	public static void printSupportColorFormats() {
		log.d(TAG, "SupportColorFormats:");

		int numCodecs = MediaCodecList.getCodecCount();
		MediaCodecInfo codecInfo = null;

		for (int i = 0; i < numCodecs; i++) {
			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);

			// 如果不是编码器，则跳过
			if (info.isEncoder())
				continue;

			String[] types = info.getSupportedTypes();
			boolean found = false;
			for (int j = 0; j < types.length; j++) {
				if (types[j].equals("video/avc")) {
					log.d(TAG, "Found: " + info.getName() + ", support:" + types[j]);
					found = true;
					break;
				}
			}

			if (found) {
				codecInfo = info;
				break;
			}
		}

		MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType("video/avc");
		log.d(TAG, "capabilities.colorFormats:" + capabilities.colorFormats.length + ", "
				+ Arrays.toString(capabilities.colorFormats));

		for (int i = 0; i < capabilities.colorFormats.length; i++) {
			int format = capabilities.colorFormats[i];
			log.d(TAG, "format:" + format + ", " + getColorFormatString(format));
		}
	}

	public static String getColorFormatString(int colorFormat) {
		String strFormat = "";
		switch (colorFormat) {
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
			strFormat = "COLOR_FormatYUV420Planar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
			strFormat = "COLOR_FormatYUV420PackedPlanar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
			strFormat = "COLOR_FormatYUV420SemiPlanar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
			strFormat = "COLOR_FormatYUV420PackedSemiPlanar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar:
			strFormat = "COLOR_QCOM_FormatYUV420SemiPlanar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
			strFormat = "COLOR_TI_FormatYUV420PackedSemiPlanar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422PackedPlanar:
			strFormat = "COLOR_FormatYUV422PackedPlanar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422PackedSemiPlanar:
			strFormat = "COLOR_FormatYUV422PackedSemiPlanar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422Planar:
			strFormat = "COLOR_FormatYUV422Planar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422SemiPlanar:
			strFormat = "COLOR_FormatYUV422SemiPlanar";
			break;
		default:
			strFormat = "ColorFormat unknown";
			break;
		}
		return strFormat;
	}

	/**
	 * 打印摄像头支持的视频输出帧格式 视频onPreviewFrame时的输出帧格式，如ImageFormat.NV21。
	 * 
	 */
	public static void printSupportPreviewFormats(Camera.Parameters params) {
		log.d(TAG, "SupportPreviewFormats:");

		List<Integer> formats = params.getSupportedPreviewFormats();
		for (Integer format : formats) {
			log.d(TAG, "format: " + format + ", " + getPreviewFormatString(format));
		}
	}

	public static String getPreviewFormatString(int previewFormat) {
		String formate = "";

		switch (previewFormat) {
		case ImageFormat.NV21:
			formate = "ImageFormat.NV21";
			break;
		case ImageFormat.YV12:
			formate = "ImageFormat.YV12";
			break;
		case ImageFormat.YUY2:
			formate = "ImageFormat.YUY2";
			break;
		case ImageFormat.NV16:
			formate = "ImageFormat.NV16";
			break;
		case ImageFormat.JPEG:
			formate = "ImageFormat.JPEG";
			break;
		case ImageFormat.RGB_565:
			formate = "ImageFormat.RGB_565";
			break;
		case ImageFormat.UNKNOWN:
			formate = "ImageFormat.UNKNOWN";
			break;
		default:
			formate = "ImageFormat error";
			break;
		}

		return formate;
	}

	/**
	 * Camera坐标变换
	 *
	 */
	public static void matrix(Matrix matrix, boolean mirror, int displayOrientation, int viewWidth, int viewHeight) {
		matrix.setScale(mirror ? -1 : 1, 1);

		// This is the value for android.hardware.Camera.setDisplayOrientation.
		if (displayOrientation != 0)
			matrix.postRotate(displayOrientation);

		// Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
		// UI coordinates range from (0, 0) to (width, height).
		matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
		matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
	}

}
