package com.es.phoneshop.model.product.cart;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductDao;
import com.es.phoneshop.model.product.cart.exception.OutOfStockException;
import com.es.phoneshop.model.product.cart.exception.QuantitySumInCartWillBeMoreThanStockException;
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
import java.math.BigDecimal;
import java.util.Currency;
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
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpSession session;

  private final Cart cart = new Cart();

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
  public void testAddingNewProductInCart() throws QuantitySumInCartWillBeMoreThanStockException, OutOfStockException {
    long productId = 3L;
    cartService.add(cart, productId, 1, session);
    Optional<CartItem> optionalCartItem = cartService.getCart(request).getCartItemByProductId(productId);
    assertTrue(optionalCartItem.isPresent());
  }

  @Test
  public void testAddingExistingProduct() throws QuantitySumInCartWillBeMoreThanStockException, OutOfStockException {
    long productId = 5L;
    cartService.add(cart, productId, 1, session);
    cartService.add(cart, productId, 1, session);
    Optional<CartItem> optionalCartItem = cartService.getCart(request).getCartItemByProductId(productId);
    assertEquals(2, optionalCartItem.get().getQuantity());

    Product product = productDao.getProduct(productId).get();
    assertEquals(1, cartService.getCart(request).getItems().stream()
            .filter(item -> item.getProduct().getCode().equals(product.getCode()))
            .count());
  }

  @Test
  public void testDeletingProduct() throws QuantitySumInCartWillBeMoreThanStockException, OutOfStockException {
    long productId = 5L;
    cartService.add(cart, productId, 1, session);
    assertEquals(1, cartService.getCart(request).getItems().size());
    assertEquals(1, cart.getItems().stream().filter(cartItem -> productId == cartItem.getProduct().getId()).count());

    cartService.delete(cart, productId, session);
    assertEquals(0, cartService.getCart(request).getItems().size());
    assertEquals(0, cart.getItems().stream().filter(cartItem -> productId == cartItem.getProduct().getId()).count());
  }

  @Test
  public void testUpdatingCartItemQuantity() throws QuantitySumInCartWillBeMoreThanStockException, OutOfStockException {
    long productId = 5L;
    cartService.add(cart, productId, 1, session);
    assertEquals(1, cartService.getCart(request).getCartItemByProductId(productId).get().getQuantity());

    cartService.update(cart, productId, 3, session);
    assertEquals(3, cartService.getCart(request).getCartItemByProductId(productId).get().getQuantity());
  }

  @Test
  public void testRecalculatingTotalPriceAndQuantity() throws QuantitySumInCartWillBeMoreThanStockException, OutOfStockException {
    assertEquals(0, cart.getTotalQuantity());
    assertEquals(0, cart.getTotalCost().intValue());
    long productId1 = 5L;
    cartService.add(cart, productId1, 2, session);
    cartService.add(cart, productId1 + 1, 1, session);

    Product product1 = productDao.getProduct(productId1).get();
    Product product2 = productDao.getProduct(productId1 + 1).get();


    assertEquals(3, cart.getTotalQuantity());

    Currency usd = Currency.getInstance("USD");
    assertEquals(product1.getPrice().multiply(BigDecimal.valueOf(2)).add(product2.getPrice()),
            cart.getTotalCost());
  }
}