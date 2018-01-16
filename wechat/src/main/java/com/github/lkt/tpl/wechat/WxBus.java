package com.github.lkt.tpl.wechat;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by tangJ on 2017/12/18
 */
class WxBus {

  private static Subject<Object> bus = PublishSubject.create().toSerialized();

  static void send(Object event) {
    if (bus.hasObservers())
      bus.onNext(event);
  }

  //static Observable<Object> asObservable = bus;

  static Observable<ShareEvent> asShareEvent = bus.ofType(ShareEvent.class);

  static Observable<AuthEvent> asAuthEvent = bus.ofType(AuthEvent.class);

}
