package com.github.lkt.tpl.wechat;

import android.util.Log;

/**
 * Created by tangJ on 2018/1/15
 */
public class LogUtil {

  static boolean DEBUG = true;

  static void d(String tag, String message, Object... args) {
    if (DEBUG) {
      Log.d(tag, String.format(message, args));
    }
  }
}
