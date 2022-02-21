package com.es.phoneshop.web.filter;

import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultDosProtectServiceTest {

  private DosProtectService dosProtectService = DefaultDosProtectService.getInstance();

  @After
  public void reset() throws Exception {
    Class<DefaultDosProtectService> defaultDosProtectServiceClass = DefaultDosProtectService.class;
    Field fieldCountMap = defaultDosProtectServiceClass.getDeclaredField("countMap");
    fieldCountMap.setAccessible(true);
    fieldCountMap.set(dosProtectService, new ConcurrentHashMap<>());
  }

  @Test
  public void testIsAllowedIfLessThan21RequestsPerMinute() {
    for (int i = 0; i < 20; i++) {
      assertTrue(dosProtectService.isAllowed("1"));
    }
  }

  @Test
  public void testIsAllowedIfMoreThan20RequestsPerMinute() {
    for (int i = 0; i < 20; i++) {
      assertTrue(dosProtectService.isAllowed("1"));
    }
    for (int i = 0; i < 5; i++) {
      assertFalse(dosProtectService.isAllowed("1"));
    }
  }

  @Test
  public void testIsAllowedIIfCountOfRequestsIsExpired() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, InstantiationException {
    Class<?> innerClass = DefaultDosProtectService.class.getDeclaredClasses()[1];

    Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
    constructor.setAccessible(true);
    Object innerInstance = constructor.newInstance();

    Field fieldLastTime = innerClass.getDeclaredField("lastTime");
    fieldLastTime.setAccessible(true);
    fieldLastTime.set(innerInstance, LocalDateTime.now().minusMinutes(3));

    Field fieldCount = innerClass.getDeclaredField("count");
    fieldCount.setAccessible(true);
    fieldCount.set(innerInstance, 30L);

    Class<DefaultDosProtectService> defaultDosProtectServiceClass = DefaultDosProtectService.class;
    Field fieldCountMap = defaultDosProtectServiceClass.getDeclaredField("countMap");
    fieldCountMap.setAccessible(true);
    ((Map) fieldCountMap.get(dosProtectService)).put("1", innerInstance);

    assertTrue(dosProtectService.isAllowed("1"));

  }
}