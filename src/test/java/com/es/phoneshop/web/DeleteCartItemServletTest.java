package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.ProductDao;
import com.es.phoneshop.model.product.cart.Cart;
import com.es.phoneshop.model.product.cart.CartService;
import com.es.phoneshop.model.product.cart.DefaultCartService;
import com.es.phoneshop.model.product.cart.exception.QuantitySumInCartWillBeMoreThanStockException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeleteCartItemServletTest {
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private RequestDispatcher requestDispatcher;
  @Mock
  private ServletConfig config;
  @Mock
  private HttpSession session;
  @Mock
  private ServletContextEvent event;
  @Mock
  private ServletContext servletContext;

  private ProductDao productDao;

  private CartService cartService;

  private final DeleteCartItemServlet servlet = new DeleteCartItemServlet();

  @Before
  public void setUp() throws Exception {
    cartService = DefaultCartService.getInstance();
    productDao = ArrayListProductDao.getInstance();
    boolean productArrayIsEmpty = productDao.findProducts(null, null, null).isEmpty();

    if (productArrayIsEmpty) {
      DemoDataServletContextListener demoDataServletContextListener = new DemoDataServletContextListener();
      when(event.getServletContext()).thenReturn(servletContext);
      when(servletContext.getInitParameter("insertDemoData")).thenReturn("true");
      demoDataServletContextListener.contextInitialized(event);
    }
    servlet.init(config);
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute(DefaultCartService.class.getName() + ".lock")).thenReturn(new ReentrantLock());
  }

  @Test
  public void testDoPostWithInvalidId() throws IOException, ServletException {
    String productId = "asd";
    when(request.getPathInfo()).thenReturn("/" + productId);
    servlet.doPost(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/product-not-found"
            + "?productId="
            + productId);
  }

  @Test
  public void testDoPostWithNotExistingId() throws IOException {
    long productId = 46L;
    when(request.getPathInfo()).thenReturn("/" + productId);
    servlet.doPost(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/product-not-found"
            + "?productId="
            + productId);
  }

  @Test
  public void testDoPostWithNoProductInCartWithSuchId() throws IOException, ServletException {
    String productId = "3";
    when(request.getPathInfo()).thenReturn("/" + productId);
    servlet.doPost(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/cart?UrlParamError=No Product with id "+ productId + " in the Cart to delete");
  }

  @Test
  public void testDoPostWithValidId() throws IOException, QuantitySumInCartWillBeMoreThanStockException {
    long productId = 3L;
    Cart cart = cartService.getCart(request);
    cartService.add(cart, productId, 3, session);
    when(session.getAttribute(DefaultCartService.class.getName() + ".cart")).thenReturn(cart);
    when(request.getPathInfo()).thenReturn("/" + productId);
    servlet.doPost(request, response);

    verify(response).sendRedirect(request.getContextPath() + "/cart?successMessage=Cart Item deleted successfully");
  }
}