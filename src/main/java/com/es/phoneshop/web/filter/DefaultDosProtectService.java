package com.es.phoneshop.web.filter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultDosProtectService implements DosProtectService {

  private static final long THRESHOLD = 20L;
  private final Map<String, CountAndLastTime> countMap = new ConcurrentHashMap<>();

  private DefaultDosProtectService() {
  }

  private static class InstanceHolder {
    private final static DosProtectService instance = new DefaultDosProtectService();
  }

  public static DosProtectService getInstance() {
    return DefaultDosProtectService.InstanceHolder.instance;
  }

  @Override
  public boolean isAllowed(String ip) {
    CountAndLastTime countAndLastTime = countMap.get(ip);
    if (countAndLastTime == null) {
      countAndLastTime = new CountAndLastTime();
      countMap.put(ip, countAndLastTime);
      return true;
    }
    if (isExpired(countAndLastTime)) {
      countAndLastTime.count = 1L;
      countAndLastTime.lastTime = LocalDateTime.now();
      return true;
    }
    countAndLastTime.count++;
    return countAndLastTime.count <= THRESHOLD;
  }

  private boolean isExpired(CountAndLastTime countAndLastTime) {
    return LocalDateTime.now().minusMinutes(1).isAfter(countAndLastTime.lastTime);
  }

  private static class CountAndLastTime {
    private long count;
    private LocalDateTime lastTime;

    public CountAndLastTime() {
      count = 1L;
      lastTime = LocalDateTime.now();
    }
  }
}
