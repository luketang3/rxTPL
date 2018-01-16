package com.github.lkt.tpl.wechat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by tangJ on 2017/12/17
 */
class Utils {
  static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
    //bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
    if (needRecycle) {
      bmp.recycle();
    }

    byte[] result = output.toByteArray();
    try {
      output.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

  static byte[] inputStreamToByte(InputStream is) {
    try {
      ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
      int ch;
      while ((ch = is.read()) != -1) {
        bytestream.write(ch);
      }
      byte imgdata[] = bytestream.toByteArray();
      bytestream.close();
      return imgdata;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  static int calSampleSize(BitmapFactory.Options options, int reqWidth,
      int reqHeight) {
    int width = options.outWidth;
    int height = options.outHeight;
    int inSampleSize = 1;
    if (width > reqWidth || height > reqHeight) {
      int widthRadio = Math.round(width * 1.0f / reqWidth);
      int heightRadio = Math.round(height * 1.0f / reqHeight);
      inSampleSize = Math.max(widthRadio, heightRadio);
    }
    return inSampleSize;
  }
}
