package com.es.phoneshop.model.product.cart;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductDao;
import com.es.phoneshop.web.DemoDataServletContextListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CartTest {
  private final CartService cartService = DefaultCartService.getInstance();
  @Mock
  private ServletContextEvent event;
  @Mock
  private ServletContext servletContext;

  private ProductDao productDao;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpSession session;

  private Cart cart = new Cart();

  @Before
  public void setUp() {
    productDao = ArrayListProductDao.getInstance();
    boolean productArrayIsEmpty = productDao.findProducts(null, null, null).isEmpty();

    if (productArrayIsEmpty) {
      DemoDataServletContextListener demoDataServletContextListener = new DemoDataServletContextListener();
      when(event.getServletContext()).thenReturn(servletContext);
      when(servletContext.getInitParameter("insertDemoData")).thenReturn("true");
      demoDataServletContextListener.contextInitialized(event);
    }

    when(request.getSession()).thenReturn(session);
    when(session.getAttribute(DefaultCartService.class.getName() + ".cart")).thenReturn(cart);
  }

  @Test
  public void testGetCartItemByExistingProductId() {
    Product product = productDao.getProduct(2L).get();
    cartService.add(cart,0L, 1, session);
    cartService.add(cart,product.getId(), 1, session);
    cartService.add(cart,3L, 1, session);
    assertTrue(cartService.getCart(request).getCartItemByProductId(product.getId()).isPresent());
  }

  @Test
  public void testGetCartItemByNotExistingProductId() {
    cartService.add(cart,0L, 1, session);
    cartService.add(cart,2L, 1, session);
    cartService.add(cart,3L, 1, session);
    assertFalse(cartService.getCart(request).getCartItemByProductId(54L).isPresent());
  }
}