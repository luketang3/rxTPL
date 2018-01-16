package com.github.lkt.tpl;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.github.lkt.tpl.wechat.AuthResult;
import com.github.lkt.tpl.wechat.RxWeChat;
import com.github.lkt.tpl.wechat.WechatResource;
import com.github.lkt.tpl.wechat.WechatScene;
import com.github.lkt.tpl.wechat.WxRequest;
import com.github.lkt.tpl.wechat.WxResult;
import com.github.lkt.tpl.wechat.WxUserEntity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

  public static final String APP_ID = "wxd930ea5d5a258f4f";

  private static final String WECHAT_APP_ID = "WECHAT_APP_ID";
  private static final String WECHAT_APP_SECRET = "WECHAT_APP_SECRET";

  private CompositeDisposable compositeDisposable = new CompositeDisposable();

  private RxWeChat rxWeChat;

  private TextView mText;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    //RxWeChat.registerWx(APP_ID, "");
    String appId = Utils.getAppMeta(this, WECHAT_APP_ID, APP_ID);
    String appSecret = Utils.getAppMeta(this, WECHAT_APP_SECRET, "");
    log("==>appId: %s", appId);
    RxWeChat.registerWx(appId, appSecret);
    rxWeChat = new RxWeChat(this);

    mText = findViewById(R.id.txt);

    //登录
    findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        toUserLogin();
      }
    });
    //链接分享
    findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //toUserLogin();
        toUrlShare();
      }
    });
    //图片分享
    findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        toImageShare();
      }
    });

    //JSONStringer
    //JSONObject.wrap()
  }

  private void toImageShare() {

    if (!checkInstall()) return;

    new AlertDialog.Builder(this)
        .setItems(new CharSequence[]{"好友", "朋友圈", "收藏"}, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, final int which) {

            WechatScene scene = which == 0 ? WechatScene.SESSION :
                which == 1 ? WechatScene.MOMENT : WechatScene.FAVOURITE;

            Disposable disposable = rxWeChat.share(
                new WxRequest.Builder(WechatResource.IMAGE, scene)
                    /*.title("title")
                    .content("content")*/
                    //远程图片
                    .img("https://ws1.sinaimg.cn/large/610dc034ly1fgi3vd6irmj20u011i439.jpg")
                    .build(), new RxWeChat.CompleteRequest() {
                  @Override
                  public void action() {
                    //LogUtil.d()
                    log("==>complete request");
                  }
                }
            )
                .subscribe(
                    new ResultConsumer(),
                    new ThrowableConsumer()
                );

            compositeDisposable.add(disposable);

          }
        })
        .show();
  }

  private void toUrlShare() {
    final String url = "http://www.yodinfo.com";
    if (!checkInstall()) return;

    new AlertDialog.Builder(this)
        .setItems(new CharSequence[]{"好友", "朋友圈", "收藏"}, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, final int which) {

            WechatScene scene = which == 0 ? WechatScene.SESSION :
                which == 1 ? WechatScene.MOMENT : WechatScene.FAVOURITE;

            Disposable disposable = rxWeChat.share(new WxRequest.Builder(WechatResource.URL, scene)
                    .title("title")
                    .content("content")
                    .img(getResources(), R.mipmap.ic_launcher)
                    .url(url)
                    .build(), new RxWeChat.CompleteRequest() {
                  @Override
                  public void action() {
                    //LogUtil.d()
                    log("==>complete request");
                  }
                }
            )
                .subscribe(
                    new ResultConsumer(),
                    new ThrowableConsumer()
                );

            compositeDisposable.add(disposable);

          }
        })
        .show();

  }

  class ResultConsumer implements Consumer<WxResult> {

    @Override
    public void accept(WxResult wxResult) throws Exception {
      switch (wxResult) {
        case OK:
          toast("成功");
          break;
        case FAIL:
          toast("失败");
          break;
        case CANCEL:
          toast("取消");
          break;
        case DENY:
          toast("拒绝");
          break;
      }

    }
  }

  class ThrowableConsumer implements Consumer<Throwable> {

    @Override
    public void accept(Throwable throwable) throws Exception {
      toast(throwable.getMessage());
      throwable.printStackTrace();
      log("message:%s", throwable.getMessage());
    }
  }

  private void log(String message, Object... args) {
    Log.d(MainActivity.class.getSimpleName(), String.format(message, args));
  }

  private boolean checkInstall() {
    return rxWeChat.isWxInstall();
  }

  private void toast(CharSequence ch) {
    Toast.makeText(this, ch, Toast.LENGTH_SHORT).show();
  }

  private void toUserLogin() {
    toast("start");
    if (!rxWeChat.isSecretAvliable()) {
      toast("未注册 secret");
      return;
    }

    Disposable disposable = rxWeChat.requestLogin()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            new Consumer<AuthResult>() {
              @Override
              public void accept(AuthResult authResult) throws Exception {
                if (authResult.result == WxResult.OK) {
                  WxUserEntity entity = authResult.entity;
                  String description = entity.name + "\n"
                      + entity.openId + "\n"
                      + entity.avatar + "\n";
                  showInfo(description);
                } else {
                  showInfo(authResult.result.name());
                }
              }
            }, new ThrowableConsumer()
        );
    compositeDisposable.add(disposable);

  }

  private void showInfo(CharSequence ch) {
    mText.setText(ch);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    compositeDisposable.clear();
  }
}
