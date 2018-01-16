package com.github.lkt.tpl.wechat;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;

/**
 *
 * Created by tangJ on 2017/12/17
 */
public enum WechatScene {

  SESSION(SendMessageToWX.Req.WXSceneSession),
  MOMENT(SendMessageToWX.Req.WXSceneTimeline),
  FAVOURITE(SendMessageToWX.Req.WXSceneFavorite);

  private final int type;

  WechatScene(int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }
}
