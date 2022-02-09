package com.es.phoneshop.util.lock;

import javax.servlet.http.HttpSession;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultSessionLockManager implements SessionLockManager {

  @Override
  public Lock getSessionLock(HttpSession session, String placeOfLockAttribute) {
    Lock sessionLock = (Lock) session.getAttribute(placeOfLockAttribute);
    if (sessionLock == null) {
      synchronized (session) {
        sessionLock = (Lock) session.getAttribute(placeOfLockAttribute);
        if (sessionLock == null) {
          session.setAttribute(placeOfLockAttribute, sessionLock = new ReentrantLock());
        }
      }
    }
    return sessionLock;
  }
}
