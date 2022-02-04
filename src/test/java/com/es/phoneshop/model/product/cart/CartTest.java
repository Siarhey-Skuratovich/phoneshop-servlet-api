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
  }

  @Test
  public void testGetCartItemByExistingProductId() {
    Product product = productDao.getProduct(2L).get();
    cartService.add(0L, 1);
    cartService.add(product.getId(), 1);
    cartService.add(3L, 1);
    assertTrue(cartService.getCart().getCartItemByProductId(product.getId()).isPresent());
  }

  @Test
  public void testGetCartItemByNotExistingProductId() {
    cartService.add(0L, 1);
    cartService.add(2L, 1);
    cartService.add(3L, 1);
    assertFalse(cartService.getCart().getCartItemByProductId(54L).isPresent());
  }
}