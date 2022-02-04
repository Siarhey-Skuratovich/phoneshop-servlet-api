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
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultCartServiceTest {
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
  public void testAddingNewProductInCart() {
    long productId = 3L;
    cartService.add(productId, 1);
    Optional<CartItem> optionalCartItem = cartService.getCart().getCartItemByProductId(productId);
    assertTrue(optionalCartItem.isPresent());
  }

  @Test
  public void testAddingExistingProduct() {
    long productId = 5L;
    cartService.add(productId, 1);
    cartService.add(productId, 1);
    Optional<CartItem> optionalCartItem = cartService.getCart().getCartItemByProductId(productId);
    assertEquals(2, optionalCartItem.get().getQuantity());

    Product product = productDao.getProduct(productId).get();
    assertEquals(1, cartService.getCart().getItems().stream()
            .filter(item -> item.getProduct().getCode().equals(product.getCode()))
            .count());
  }

}