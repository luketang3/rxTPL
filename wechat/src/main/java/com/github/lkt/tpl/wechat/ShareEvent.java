package com.github.lkt.tpl.wechat;

import java.io.Serializable;

/**
 * Created by tangJ on 2017/12/18
 */
public class ShareEvent implements Serializable {

  private final int type;
  private final String transaction;
  private final int errorCode;

  public ShareEvent(int type, String transaction, int errorCode) {
    this.type = type;
    this.transaction = transaction;
    this.errorCode = errorCode;
  }

  public int getType() {
    return type;
  }

  public String getTransaction() {
    return transaction;
  }

  public int getErrorCode() {
    return errorCode;
  }
}
