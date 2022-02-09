package com.es.phoneshop.util.lock;

import javax.servlet.http.HttpSession;
import java.util.concurrent.locks.Lock;

public interface SessionLockManager {

  Lock getSessionLock(HttpSession session, String placeOfLockAttribute);
}
