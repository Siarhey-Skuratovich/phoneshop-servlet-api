package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.ProductDao;
import com.es.phoneshop.model.product.cart.CartService;
import com.es.phoneshop.model.product.cart.DefaultCartService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductDetailsPageServletTest {

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

  private final CartService cartService = DefaultCartService.getInstance();

  private final ProductDetailsPageServlet servlet = new ProductDetailsPageServlet();

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
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/product.jsp"));
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

  @Test
  public void testDoGetWithInvalidId() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/asd");
    servlet.doGet(request, response);

    verify(request).setAttribute(eq("productId"), any());
    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/errorProductNotFound.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoPostWithInvalidId() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/asd");
    servlet.doPost(request, response);

    verify(request).setAttribute(eq("productId"), any());
    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/errorProductNotFound.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoPostWithNotExistingId() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/46");
    servlet.doPost(request, response);

    verify(request).setAttribute(eq("productId"), any());
    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/errorProductNotFound.jsp"));
    verify(requestDispatcher).forward(request, response);
    assertFalse(cartService.getCart().getCartItemByProductId(46L).isPresent());
  }

  @Test
  public void testDoPostWithInvalidQuantity() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/6");
    when(request.getParameter("quantity")).thenReturn("asd");
    servlet.doPost(request, response);

    verify(request).setAttribute(eq("error"), eq("Not a number"));
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    assertFalse(cartService.getCart().getCartItemByProductId(6L).isPresent());
  }

  @Test
  public void testDoPostWithQuantityMoreThanStock() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/6");
    when(request.getParameter("quantity")).thenReturn("1000");
    servlet.doPost(request, response);

    verify(request).setAttribute(eq("error"), any());
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    assertFalse(cartService.getCart().getCartItemByProductId(6L).isPresent());
  }

  @Test
  public void testDoPostWithValidAndExistingValues() throws ServletException, IOException  {
    when(request.getPathInfo()).thenReturn("/5");
    when(request.getParameter("quantity")).thenReturn("3");
    servlet.doPost(request, response);

    verify(response).sendRedirect(any());
    assertTrue(cartService.getCart().getCartItemByProductId(5L).isPresent());
  }

}