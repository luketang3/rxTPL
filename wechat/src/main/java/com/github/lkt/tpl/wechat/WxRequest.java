package com.github.lkt.tpl.wechat;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.github.lkt.tpl.wechat.Utils.calSampleSize;


/**
 * wx 相关的 request 请求
 * Created by tangJ on 2017/11/28
 */
public class WxRequest {

  private static final int THUMB_SIZE = 150; //

  private WechatResource resource;
  private WechatScene scene;
  private String title;
  private String content;
  private Resources resources;
  private int drawableRes;
  private File file;
  private String imgPath;
  private String url;
  private byte[] imgBytes;

  private Bitmap imgBitmap;
  private boolean bitmapRecycle; //


  public static SendAuth.Req getAuthRequest() {
    final SendAuth.Req req = new SendAuth.Req();
    req.scope = "snsapi_userinfo";
    //req.state = "wechat_sdk_demo_test";
    req.state = buildTransaction("auth");
    return req;
  }


  //耗时
  public SendMessageToWX.Req getRequest() throws IOException {
    SendMessageToWX.Req req = new SendMessageToWX.Req();
    //标志
    req.transaction = buildTransaction(resource.getType());
    req.message = getMessage();
    //
    req.scene = scene.getType();
    return req;
  }

  private WXMediaMessage getMessage() throws IOException {
    //new WXMediaMessage.Builder()
    WXMediaMessage message = new WXMediaMessage();

    //file

    WXMediaMessage.IMediaObject mediaObject = null;
    byte thumbBytes[] = null;
    if (resource == WechatResource.URL) {
      WXWebpageObject webObject = new WXWebpageObject();
      webObject.webpageUrl = url;
      //需要缩略图
      thumbBytes = getImageArray(true);
      mediaObject = webObject;
    } else if (resource == WechatResource.IMAGE) {
      WXImageObject wxImageObject = new WXImageObject();
      //wxImageObject.imageData
      if (file != null) { //文件分享
        wxImageObject.imagePath = file.getAbsolutePath();
      } else {
        wxImageObject.imageData = getImageArray(false);
      }

      mediaObject = wxImageObject;
    } else if (resource == WechatResource.APP) { //小程序

    }

    //内容
    message.title = title;
    message.description = content;
    message.thumbData = thumbBytes;
    message.mediaObject = mediaObject;

    return message;
  }

  /**
   * 对应获取优先级
   * array > (resource + imgRes) > Bitmap > File > url
   */
  private byte[] getImageArray(boolean needCompress) throws IOException {

    if (imgBytes != null) {
      if (needCompress) {
        return getCompressBytes(imgBytes);
      } else {
        return imgBytes;
      }
    } else if (resources != null && drawableRes > 0) { //有 image
      //byte[] bytes = Utils.bmpToByteArray(originBitmap, true);
      return getCompressBytes(resources, drawableRes, needCompress);
    } else if (imgBitmap != null) {
      //=
      Bitmap targetBitmap = bitmapRecycle ? imgBitmap :
          imgBitmap.copy(Bitmap.Config.ARGB_8888, true);
      byte[] bytes = Utils.bmpToByteArray(targetBitmap, true);
      if (needCompress) return getCompressBytes(bytes);
      else return bytes;
    } else if (file != null && file.exists()) { //compress file
      return getCompressBytes(file);
    } else if (imgPath != null && imgPath.startsWith("http")) { //远程地址
      InputStream in = EntityFactory.getRemoteByte(imgPath);
      byte[] origin = Utils.inputStreamToByte(in);
      in.close();
      if (needCompress) return getCompressBytes(origin);
      else return origin;
    }

    return null;
  }

  private byte[] getCompressBytes(Resources res, int resId, boolean needCompress) {

    if (needCompress) {
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      //BitmapFactory.decodeByteArray(origin, 0, origin.length, options);
      BitmapFactory.decodeResource(res, resId, options);

      options.inSampleSize = calSampleSize(options, THUMB_SIZE, THUMB_SIZE);
      options.inJustDecodeBounds = false;

      Bitmap bitmap = BitmapFactory.decodeResource(res, resId, options);
      return getSampleBytes(bitmap);
    } else {
      Bitmap bitmap = BitmapFactory.decodeResource(res, resId);
      return Utils.bmpToByteArray(bitmap, true);
    }
  }

  /**
   * compress byte from array
   */
  private byte[] getCompressBytes(byte[] origin) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeByteArray(origin, 0, origin.length, options);

    options.inSampleSize = calSampleSize(options, THUMB_SIZE, THUMB_SIZE);
    options.inJustDecodeBounds = false;

    Bitmap bitmap = BitmapFactory.decodeByteArray(origin, 0, origin.length, options);
    return getSampleBytes(bitmap);
  }

  /**
   * compress byte from file
   */
  private byte[] getCompressBytes(File file) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;

    BitmapFactory.decodeFile(file.getAbsolutePath());

    options.inSampleSize = calSampleSize(options, THUMB_SIZE, THUMB_SIZE);
    options.inJustDecodeBounds = false;
    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    return getSampleBytes(bitmap);
  }

  private byte[] getSampleBytes(Bitmap bitmap) {
    byte[] targetArray = null;
    if (bitmap != null) {
      Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, THUMB_SIZE, THUMB_SIZE, true);
      targetArray = Utils.bmpToByteArray(scaleBitmap, true);
      bitmap.recycle();
    }
    return targetArray;
  }


  private static String buildTransaction(final String type) {
    return (type == null) ? String.valueOf(
        System.currentTimeMillis()) : type + System.currentTimeMillis();
  }

  public static class Builder {

    private WechatResource resource;
    private WechatScene scene;
    private String title;
    private String content;
    private Resources resources;
    private int drawableRes;
    private File file;
    private String imgPath;
    private String url;

    private byte[] imgBytes;
    private Bitmap imgBitmap;

    private boolean bitmapRecycle; //


    public Builder(WechatResource resource, WechatScene scene) {
      this.resource = resource;
      this.scene = scene;
    }


    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder content(String content) {
      this.content = content;
      return this;
    }

    public Builder img(Resources resources, int drawableRes) {
      this.resources = resources;
      this.drawableRes = drawableRes;
      return this;
    }

    public Builder img(File file) {
      this.file = file;
      return this;
    }

    public Builder img(String imgPath) {
      this.imgPath = imgPath;
      return this;
    }

    public Builder img(byte[] imgBytes) {
      this.imgBytes = imgBytes;
      return this;
    }

    public Builder img(Bitmap imgBitmap) {
      this.imgBitmap = imgBitmap;
      return this;
    }

    public Builder url(String url) {
      this.url = url;
      return this;
    }

    public Builder recycle(boolean needRecycle) {
      this.bitmapRecycle = needRecycle;
      return this;
    }

    public WxRequest build() {
      WxRequest request = new WxRequest();
      request.resource = resource;
      request.scene = scene;
      request.title = title;
      request.content = content;
      request.resources = resources;
      request.drawableRes = drawableRes;
      request.file = file;
      request.imgPath = imgPath;
      request.url = url;
      request.imgBytes = imgBytes;
      request.imgBitmap = imgBitmap;
      request.bitmapRecycle = bitmapRecycle;
      return request;
    }
  }
}
