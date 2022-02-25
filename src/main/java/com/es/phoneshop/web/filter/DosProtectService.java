package com.es.phoneshop.web.filter;

import java.util.Map;

public interface DosProtectService {
  boolean isAllowed(String ip);

  Map<String, DefaultDosProtectService.CountAndLastTime> getCountMap();
}
