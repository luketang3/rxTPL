package com.github.lkt.tpl;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Created by tangJ on 2018/1/16
 */
public class Utils {

  public static String getAppMeta(Context context, String key, String defaultValue) {
    String result = "";
    try {
      ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(
          context.getPackageName(), PackageManager.GET_META_DATA);
      result = applicationInfo.metaData.getString(key, result);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    return (result != null && result.length() > 0) ? result : defaultValue;
    //var result: String = ""
  }
}
