package com.github.lkt.tpl.wechat;

import android.app.Activity;
import android.app.FragmentManager;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * wechat share + login
 * Created by tangJ on 2017/12/18
 */
public class RxWeChat {

  private static final String TAG = "RxWeChat";
  private static final Object TRIGGER = new Object();
  private RxShareFragment rxShareFragment;

  public RxWeChat(@NonNull Activity activity) {
    rxShareFragment = getShareFragment(activity);
  }

  private RxShareFragment getShareFragment(Activity activity) {
    RxShareFragment fragment = (RxShareFragment) activity.getFragmentManager().findFragmentByTag(
        TAG);

    if (fragment == null) {
      fragment = new RxShareFragment();
      FragmentManager fragmentManager = activity.getFragmentManager();
      fragmentManager.beginTransaction()
          .add(fragment, TAG)
          .commitAllowingStateLoss();
      fragmentManager.executePendingTransactions();
    }

    return fragment;
  }


  /**
   * 分享
   */
  public Observable<WxResult> share(final WxRequest request, final CompleteRequest action) {

    return Observable.just(TRIGGER)
        .map(new Function<Object, SendMessageToWX.Req>() {
          @Override
          public SendMessageToWX.Req apply(Object o) throws Exception {
            return request.getRequest();
          }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doAfterNext(new Consumer<SendMessageToWX.Req>() {
          @Override
          public void accept(SendMessageToWX.Req req) throws Exception {
            action.action();
          }
        })
        .observeOn(Schedulers.io())
        .flatMap(new Function<SendMessageToWX.Req, ObservableSource<WxResult>>() {
          @Override
          public ObservableSource<WxResult> apply(SendMessageToWX.Req req) throws Exception {
            PublishSubject<WxResult> subject = rxShareFragment.getSubject(req.transaction);

            rxShareFragment.sendRequest(req);
            return subject;
          }
        });

  }

  /**
   * 是否 已安装
   */
  public boolean isWxInstall() {
    return rxShareFragment.isWxInstalled();
  }

  /**
   * secret 有数据
   */
  public boolean isSecretAvliable() {
    WxBaseEntity entity = EntityFactory.getWxEntity();
    return entity != null && entity.getAppSecret() != null && entity.getAppSecret().length() > 0;
  }

  /**
   * 登录
   */
  public Observable<AuthResult> requestLogin() {

    return Observable.just(TRIGGER).flatMap(new Function<Object, ObservableSource<AuthResult>>() {
      @Override
      public ObservableSource<AuthResult> apply(Object o) throws Exception {
        SendAuth.Req request = WxRequest.getAuthRequest();
        //请求
        rxShareFragment.sendRequest(request);
        return rxShareFragment.getAuthSubject(request.state);
      }
    });
  }


  public static void registerWx(String appId, String appSecret) {
    EntityFactory.registerWxApi(null, appId, appSecret);
  }

  public interface CompleteRequest {
    void action();
  }

}
