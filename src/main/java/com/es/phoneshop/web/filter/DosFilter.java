package com.es.phoneshop.web.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DosFilter implements Filter {
  private static final int TOO_MANY_REQUESTS_STATUS = 429;

  private DosProtectService dosProtectService;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    dosProtectService = DefaultDosProtectService.getInstance();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    if (dosProtectService.isAllowed(request.getRemoteAddr())) {
      filterChain.doFilter(request, response);
    } else {
      ((HttpServletResponse) response).setStatus(TOO_MANY_REQUESTS_STATUS);
    }
  }

  @Override
  public void destroy() {

  }
}
