package com.github.lkt.tpl.wechat;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by tangJ on 2017/12/17
 */
public class EntityFactory {
  private static OkHttpClient client;

  private static WxBaseEntity wxEntity;

  public static OkHttpClient getHttpClient() {
    if (client == null) {
      client = new OkHttpClient.Builder()
          .connectTimeout(10, TimeUnit.SECONDS)
          .readTimeout(50, TimeUnit.SECONDS)
          .writeTimeout(50, TimeUnit.SECONDS)
          .build();
    }
    return client;
  }

  /**
   * yes or null
   */
  public static InputStream getRemoteByte(String url) {
    if (url == null || !url.startsWith("http")) return null;
    Request request = new Request.Builder()
        .url(url)
        .build();
    try {
      Response response = getHttpClient().newCall(request).execute();
      if (response.isSuccessful()) {
        return response.body().byteStream();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 返回字符串
   */
  public static String getRequest(String url) throws IOException {
    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = getHttpClient().newCall(request).execute();
    if (response.isSuccessful()) {
      return response.body().string();
    }
    return null;
  }

  public static void registerWxApi(Context context, String appId, String appSecret) {
    if (wxEntity == null) {
      wxEntity = new WxBaseEntity(appId, appSecret);
    }
  }

  public static WxBaseEntity getWxEntity() {
    return wxEntity;
  }
}
