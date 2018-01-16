package com.github.lkt.tpl.wechat;

/**
 * 需要注册的 entity
 * Created by tangJ on 2017/12/17
 */
public class WxBaseEntity {

  private final String appId;
  private final String appSecret;

  public WxBaseEntity(String appId, String appSecret) {
    this.appId = appId;
    this.appSecret = appSecret;
  }

  public String getAppId() {
    return appId;
  }

  public String getAppSecret() {
    return appSecret;
  }
}
