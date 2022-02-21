package com.es.phoneshop.web.filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DosFilterTest {
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  FilterChain filterChain;
  @Mock
  DosProtectService dosProtectService;

  DosFilter dosFilter = new DosFilter();

  @Before
  public void setUp() throws Exception {
    Class<DosFilter> dosFilterClass = DosFilter.class;
    Field fieldDosProtectService = dosFilterClass.getDeclaredField("dosProtectService");
    fieldDosProtectService.setAccessible(true);
    fieldDosProtectService.set(dosFilter, dosProtectService);
  }

  @Test
  public void testDoFilterIfAllowed() throws IOException, ServletException {
    when(dosProtectService.isAllowed(any())).thenReturn(true);
    dosFilter.doFilter(request, response, filterChain);
    verify(filterChain).doFilter(request, response);
  }

  @Test
  public void testDoFilterIfNotAllowed() throws IOException, ServletException {
    when(dosProtectService.isAllowed(any())).thenReturn(false);
    dosFilter.doFilter(request, response, filterChain);
    verify(response).setStatus(429);
  }
}