package com.github.lkt.tpl.wechat;


import io.reactivex.Observable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * client request
 * Created by tangJ on 2017/12/18
 */
public class WxClientRequest {

  private static final String BASE_URL = "https://api.weixin.qq.com";

  public static final String TAG = WxClientRequest.class.getSimpleName();


  private static String getRequestUrl(String host, Map<String, String> param) {
    String url = String.format("%s%s?", BASE_URL, host);

    StringBuilder sb = new StringBuilder(url);
    int index = 0;
    for (String key : param.keySet()) {
      if (index != 0) sb.append("&");
      sb.append(String.format("%s=%s", key, param.get(key)));
      index++;
    }
    return sb.toString();
  }

  /**
   * 获取 token
   * https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
   */
  public static Observable<Object> getToken(String appId, String secret, String code) {

    Map<String, String> map = new HashMap<>();
    map.put("appid", appId);
    map.put("secret", secret);
    map.put("code", code);
    map.put("grant_type", "authorization_code");
    final String targetUrl = getRequestUrl("/sns/oauth2/access_token", map);
    LogUtil.d(TAG ,"==>targetUrl:" + targetUrl);

    return Observable.fromCallable(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        String result = EntityFactory.getRequest(targetUrl);
        LogUtil.d(TAG, "==>result:" + result);
        return result;
      }
    });
  }

  /**
   * 获取 userInfo
   * /sns/userinfo?access_token=${token}&openid=$openId
   */
  public static Observable<Object> getUser(String openId, String token) {
    Map<String, String> param = new HashMap<>();
    param.put("openid", openId);
    param.put("access_token", token);

    final String targetUrl = getRequestUrl("/sns/userinfo", param);
    LogUtil.d(TAG, "==>targetUrl:" + targetUrl);
    return Observable.fromCallable(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        String result = EntityFactory.getRequest(targetUrl);
        LogUtil.d(TAG, "==>result:" + result);
        return result;
      }
    });

  }


}
