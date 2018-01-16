package com.github.lkt.tpl.wechat;

/**
 * Created by tangJ on 2017/12/17
 */
public enum WechatResource {
  TEXT("text"),
  IMAGE("img"),
  MUSIC("music"),
  VIDEO("video"),
  APP("app"),
  URL("webpage");

  private final String type;

  WechatResource(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
