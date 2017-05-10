package com.yongyida.robot.video.util;


import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

/**
 * 使用了google zxing作为二维码生成工具
 */
public class ZxingUtils {
	

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    private static Bitmap toBufferedImage(BitMatrix matrix) {
        int widthPix = matrix.getWidth();
        int heightPix = matrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < widthPix; x++) {
            for (int y = 0; y < heightPix; y++) {
            	bitmap.setPixel(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return bitmap;
       

    }

    private static boolean writeToFile(BitMatrix matrix, String format, File file) throws IOException {
    	Bitmap bitmap = toBufferedImage(matrix);
    	 //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！ 
    	return bitmap != null && bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
    	
    }

    /** 将内容contents生成长宽均为width的图片，图片路径由imgPath指定
     */
    public static boolean getQRCodeImge(String contents, int width, String imgPath) {
        return getQRCodeImge(contents, width, width, imgPath);
    }

    /** 将内容contents生成长为width，宽为width的图片，图片路径由imgPath指定
     */
	public static boolean getQRCodeImge(String contents, int width, int height, String imgPath) {
		try {
            Map<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF8");
            hints.put(EncodeHintType.MARGIN, 1); //default is 4
			BitMatrix bitMatrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, width, height, hints);

             File imageFile = new File(imgPath);
             if(imageFile.exists()){
            	 imageFile.delete();
             }
             return writeToFile(bitMatrix, "png", imageFile);

          

		} catch (Exception e) {
	
            return false;
		}
	}
	

}