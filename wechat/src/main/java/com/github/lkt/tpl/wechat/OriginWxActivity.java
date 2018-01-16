package com.github.lkt.tpl.wechat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import io.reactivex.annotations.Nullable;

/**
 * wx 转换
 * Created by tangJ on 2017/12/18
 */
public class OriginWxActivity extends Activity implements IWXAPIEventHandler {
  public static final String TAG = OriginWxActivity.class.getSimpleName();

  private IWXAPI mWxApi;

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    mWxApi.handleIntent(intent, this);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    WxBaseEntity entity = EntityFactory.getWxEntity();
    mWxApi = WXAPIFactory.createWXAPI(this, entity.getAppId());
    mWxApi.handleIntent(getIntent(), this);
  }


  @Override
  public void onReq(BaseReq baseReq) {
    LogUtil.d(TAG, "==>onReq");
  }

  @Override
  public void onResp(BaseResp baseResp) {
    LogUtil.d(TAG, "==>onResp: %s, errorCode: %s", baseResp.getType(), baseResp.errCode);
    if (baseResp instanceof SendMessageToWX.Resp) { //消息
      ShareEvent event = new ShareEvent(baseResp.getType(), baseResp.transaction,
          baseResp.errCode);
      WxBus.send(event);
      finish();
    } else if (baseResp instanceof SendAuth.Resp) { //登录授权

      SendAuth.Resp resp = (SendAuth.Resp) baseResp;
      AuthEvent event = new AuthEvent();
      event.errorCode = resp.errCode;
      event.code = resp.code;
      event.stateCode = resp.state;
      WxBus.send(event);
      finish();
    }

  }
}
