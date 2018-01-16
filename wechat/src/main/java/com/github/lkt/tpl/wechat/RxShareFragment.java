package com.github.lkt.tpl.wechat;

import android.app.Fragment;
import android.os.Bundle;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/**
 * Created by tangJ on 2017/12/18
 */
public class RxShareFragment extends Fragment {

  public static final String TAG = RxShareFragment.class.getSimpleName();

  private IWXAPI mWxApi;

  private Map<String, PublishSubject<WxResult>> mSubjects = new HashMap<>();

  private Map<String, PublishSubject<AuthResult>> authSubjects = new HashMap<>(); //auth


  private CompositeDisposable compositeDisposable = new CompositeDisposable();

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    WxBaseEntity entity = EntityFactory.getWxEntity();
    mWxApi = WXAPIFactory.createWXAPI(getActivity(), entity.getAppId());
    //mWxApi.handleIntent(getIntent(), this);

    //register app
    mWxApi.registerApp(entity.getAppId());

    //message
    Disposable disposable = WxBus.asShareEvent
        .filter(new Predicate<ShareEvent>() {
                  @Override
                  public boolean test(ShareEvent event) throws Exception {

                    String transaction = event.getTransaction();
                    return mSubjects.containsKey(transaction);
                  }
                }
        )
        .subscribe(new Consumer<ShareEvent>() {
          @Override
          public void accept(ShareEvent event) throws Exception {
            String transaction = event.getTransaction();
            PublishSubject<WxResult> subject = mSubjects.get(transaction);

            switch (event.getErrorCode()) {
              case BaseResp.ErrCode.ERR_OK: {
                subject.onNext(WxResult.OK);
                break;
              }
              case BaseResp.ErrCode.ERR_USER_CANCEL: {
                subject.onNext(WxResult.CANCEL);
                break;
              }
              case BaseResp.ErrCode.ERR_AUTH_DENIED:
              case BaseResp.ErrCode.ERR_UNSUPPORT:
              default:
                subject.onNext(WxResult.FAIL);
            }
            subject.onComplete();

            //remove transaction
            mSubjects.remove(transaction);
          }
        });
    compositeDisposable.add(disposable);

    //auth

    //new  AuthEvent();
    disposable = WxBus.asAuthEvent.filter(new Predicate<AuthEvent>() {
      @Override
      public boolean test(AuthEvent authEvent) throws Exception {

        return !authSubjects.isEmpty();
      }
    }).subscribe(new Consumer<AuthEvent>() {
      @Override
      public void accept(AuthEvent authEvent) throws Exception {
        String stateCode = authEvent.stateCode;
        PublishSubject<AuthResult> subject;
        if (stateCode != null) {
          subject = authSubjects.get(stateCode);
        } else {
          //获取第一个
          subject = authSubjects.values().iterator().next();
        }

        switch (authEvent.errorCode) {
          case BaseResp.ErrCode.ERR_OK: {
            //subject.onNext(WxResult.OK);
            requestUserInfo(subject, authEvent);

            break;
          }
          case BaseResp.ErrCode.ERR_USER_CANCEL: {
            //subject.onNext(WxResult.CANCEL);
            AuthResult result = new AuthResult();
            result.result = WxResult.CANCEL;
            handWxResult(subject, authEvent, result);
            break;
          }
          case BaseResp.ErrCode.ERR_AUTH_DENIED: {
            AuthResult result = new AuthResult();
            result.result = WxResult.DENY;
            handWxResult(subject, authEvent, result);
          }
          default: {
            AuthResult result = new AuthResult();
            result.result = WxResult.FAIL;
            handWxResult(subject, authEvent, result);
          }

        }
      }
    });
    compositeDisposable.add(disposable);
  }

  private void requestUserInfo(final PublishSubject<AuthResult> subject,
      final AuthEvent authEvent) {
    WxBaseEntity entity = EntityFactory.getWxEntity();
    Disposable disposable = WxClientRequest.getToken(entity.getAppId(), entity.getAppSecret(),
        authEvent.code)
        .flatMap(new Function<Object, ObservableSource<TokenEntity>>() {
          @Override
          public ObservableSource<TokenEntity> apply(Object o) throws Exception {
            //return WxClientRequest.getUser();
            LogUtil.d("==>token result: %s", o.toString());
            JSONObject json = new JSONObject(o.toString());
            TokenEntity tokenEntity = new TokenEntity();

            if (json.has("errcode")) {
              tokenEntity.ok = false;
              tokenEntity.errorMsg = json.optString("errmsg");
            } else {
              tokenEntity.ok = true;
              tokenEntity.appId = json.optString("openid");
              tokenEntity.token = json.optString("access_token");
              tokenEntity.unionId = json.optString("unionid");
            }
            return Observable.just(tokenEntity);
          }
        })
        .flatMap(new Function<TokenEntity, ObservableSource<WxUserEntity>>() {
          @Override
          public ObservableSource<WxUserEntity> apply(TokenEntity tokenEntity) throws Exception {
            if (tokenEntity.ok) {
              final Observable<Object> user = WxClientRequest.getUser(tokenEntity.appId,
                  tokenEntity.token);

              return user.zipWith(Observable.just(tokenEntity),
                  new BiFunction<Object, TokenEntity, WxUserEntity>() {
                    @Override
                    public WxUserEntity apply(Object o,
                        TokenEntity tokenEntity) throws Exception {
                      String result = o.toString();
                      LogUtil.d(TAG, "==>user result: %s", result);
                      JSONObject jsonObject = new JSONObject(result);
                      WxUserEntity userEntity = new WxUserEntity();
                      if (jsonObject.has("errcode")) {
                        userEntity.ok = false;
                        userEntity.errorMsg = jsonObject.optString("errmsg");
                      } else {
                        userEntity.ok = true;
                        userEntity.openId = jsonObject.optString("openid");
                        userEntity.name = jsonObject.optString("nickname");
                        userEntity.sex = jsonObject.optInt("sex");

                        userEntity.country = jsonObject.optString("country");
                        userEntity.province = jsonObject.optString("province");
                        userEntity.city = jsonObject.optString("city");

                        userEntity.unionId = jsonObject.optString("unionid");
                        userEntity.avatar = jsonObject.optString("headimgurl");
                        //
                      }
                      return userEntity;
                    }
                  });
            }
            WxUserEntity userEntity = new WxUserEntity();
            userEntity.errorMsg = tokenEntity.errorMsg;
            return Observable.just(userEntity);
          }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            new Consumer<WxUserEntity>() {
              @Override
              public void accept(WxUserEntity o) throws Exception {

                AuthResult result = new AuthResult();
                //WxUserEntity userEntity = new WxUserEntity();
                result.entity = o;
                if (o.ok) {
                  result.result = WxResult.OK;
                } else {
                  result.result = WxResult.FAIL;
                  o.errorType = 1;
                }

                handWxResult(subject, authEvent, result);
              }
            },
            new Consumer<Throwable>() {
              @Override
              public void accept(Throwable throwable) throws Exception {
                //Timber.e(throwable);
                String message = throwable.getMessage();

                AuthResult result = new AuthResult();
                result.result = WxResult.FAIL;

                WxUserEntity userEntity = new WxUserEntity();
                //userEntity.ok = false;
                userEntity.errorType = 2;
                userEntity.errorMsg = message;

                result.entity = userEntity;

                handWxResult(subject, authEvent, result);
              }
            }
        );
    compositeDisposable.add(disposable);
  }

  private void handWxResult(PublishSubject<AuthResult> subject, AuthEvent authEvent,
      AuthResult result) {
    subject.onNext(result);
    subject.onComplete();
    if (authEvent.stateCode != null) {
      authSubjects.remove(authEvent.stateCode);
    } else {
      authSubjects.clear();
    }
  }


  public PublishSubject<WxResult> getSubject(String key) {
    PublishSubject<WxResult> subject = mSubjects.get(key);
    if (subject == null) {
      subject = PublishSubject.create();
      mSubjects.put(key, subject);
    }
    return subject;
  }

  public PublishSubject<AuthResult> getAuthSubject(String key) {
    PublishSubject<AuthResult> subject = authSubjects.get(key);
    if (subject == null) {
      subject = PublishSubject.create();
      authSubjects.put(key, subject);
    }
    return subject;
  }

  /**
   *
   */
  public void sendRequest(BaseReq req) {
    //Intent intent = new Intent(getActivity(), OriginWxActivity.class);
    //startActivityForResult(intent, CODE_SHARE);
    mWxApi.sendReq(req);
  }


  @Override
  public void onDestroyView() {
    super.onDestroyView();
    compositeDisposable.clear();
  }

  public boolean isWxInstalled() {
    return mWxApi.isWXAppInstalled();
  }


  /**
   * {
   * "access_token": "5_HUndZjuVEP9lmLeXkZXJ_dkGckSsuxl_qrFqQQ1-jqp2t4ZdC28HXvGFWtFTeR7dh0z-KVxplun9eQYMVFtWVO2lZSrs96cDckTStmwX5qs",
   * "expires_in": 7200,
   * "refresh_token": "5_kM8pdxwAqpS9bMMetrTdLSgdSZ85ZDSELIQjWApS-2kqkxm05vNcd1n30v-ShaE9k08WIB-3LF49RLqMjhfXwwpTFpwSTQhCGLTIbsLj_sM",
   * "openid": "oFep60ajlmeZ1_OOqXra-65l8_lk",
   * "scope": "snsapi_userinfo",
   * "unionid": "oJWg103RWq1gNedDNlfheesO4gQ0"
   * }
   */
  class TokenEntity {
    String token;
    String appId;
    String unionId;

    boolean ok;
    String errorMsg;
  }

}
