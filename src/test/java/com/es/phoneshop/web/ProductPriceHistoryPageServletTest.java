package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.ProductDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductPriceHistoryPageServletTest {

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private RequestDispatcher requestDispatcher;
  @Mock
  private ServletConfig config;
  @Mock
  private ServletContextEvent event;
  @Mock
  private ServletContext servletContext;

  private final ProductPriceHistoryPageServlet servlet = new ProductPriceHistoryPageServlet();

  @Before
  public void setup() throws ServletException {
    ProductDao productDao = ArrayListProductDao.getInstance();
    boolean productArrayIsEmpty = productDao.findProducts(null, null, null).isEmpty();

    if (productArrayIsEmpty) {
      DemoDataServletContextListener demoDataServletContextListener = new DemoDataServletContextListener();
      when(event.getServletContext()).thenReturn(servletContext);
      when(servletContext.getInitParameter("insertDemoData")).thenReturn("true");
      demoDataServletContextListener.contextInitialized(event);
    }
    servlet.init(config);
    when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
  }

  @Test
  public void testDoGetWithExistingId() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/3");
    servlet.doGet(request, response);

    verify(request).setAttribute(eq("product"), any());
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/productPriceHistories.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoGetWithNotExistingId() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/54");
    servlet.doGet(request, response);

    verify(request).setAttribute(eq("productId"), any());
    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/errorProductNotFound.jsp"));
    verify(requestDispatcher).forward(request, response);
  }
}