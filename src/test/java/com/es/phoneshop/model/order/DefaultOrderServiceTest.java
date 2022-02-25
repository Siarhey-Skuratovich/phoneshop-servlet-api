package com.es.phoneshop.model.order;

import com.es.phoneshop.model.GenericDao;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartItem;
import com.es.phoneshop.model.cart.DefaultCartService;
import com.es.phoneshop.model.order.exception.EmptyCartException;
import com.es.phoneshop.model.order.exception.ValidationErrorsException;
import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.ProductDao;
import com.es.phoneshop.web.DemoDataServletContextListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultOrderServiceTest {

  private final OrderService orderService = DefaultOrderService.getInstance();
  private final ProductDao productDao = ArrayListProductDao.getInstance();
  private final OrderDao orderDao = ArrayListOrderDao.getInstance();

  @Mock
  private ServletContextEvent event;
  @Mock
  private ServletContext servletContext;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpSession session;

  private final Cart cart = new Cart();

  @Before
  public void setUp() throws Exception {
    boolean productArrayIsEmpty = productDao.findProducts(null, null, null).isEmpty();

    if (productArrayIsEmpty) {
      DemoDataServletContextListener demoDataServletContextListener = new DemoDataServletContextListener();
      when(event.getServletContext()).thenReturn(servletContext);
      when(servletContext.getInitParameter("insertDemoData")).thenReturn("true");
      demoDataServletContextListener.contextInitialized(event);
    }

    when(request.getSession()).thenReturn(session);
    when(session.getAttribute(DefaultCartService.class.getName() + ".lock")).thenReturn(new ReentrantLock());
    when(session.getAttribute(DefaultCartService.class.getName() + ".cart")).thenReturn(cart);
  }

  @After
  public void refreshOrderDao() throws NoSuchFieldException, IllegalAccessException {
    Class<ArrayListOrderDao> arrayListOrderDaoClass = ArrayListOrderDao.class;
    Class<GenericDao<Order>> genericDaoClass = (Class<GenericDao<Order>>) arrayListOrderDaoClass.getSuperclass();
    Field itemsField = genericDaoClass.getDeclaredField("items");
    itemsField.setAccessible(true);
    itemsField.set(orderDao, new ArrayList<Order>());

    Field maxIdField = genericDaoClass.getDeclaredField("maxId");
    maxIdField.setAccessible(true);
    maxIdField.set(orderDao, 0);
  }

  @Test
  public void testGetOrder() {
    Cart cart = new Cart();
    long productId1 = 0L;
    long productId2 = 4L;
    long productId3 = 7L;
    CartItem cartItem1 = new CartItem(productDao.get(productId1).get(), 1);
    CartItem cartItem2 = new CartItem(productDao.get(productId2).get(), 2);
    CartItem cartItem3 = new CartItem(productDao.get(productId3).get(), 3);
    List<CartItem> cartItemList = List.of(cartItem1, cartItem2, cartItem3);
    cart.setItems(cartItemList);

    Order order = orderService.getOrder(cart);

    order.getItems().stream()
            .map(toId())
            .forEach(id -> assertTrue(cartItemList.stream()
                    .map(toId()).anyMatch(id::equals)));

    assertEquals(BigDecimal.valueOf(5), order.getDeliveryCost());
    assertEquals(cart.getTotalCost(), order.getSubtotal());
    assertEquals(cart.getTotalCost().add(order.getDeliveryCost()), order.getTotalCost());
    assertEquals(cart.getTotalQuantity(), order.getTotalQuantity());
  }

  @Test
  public void testGetPaymentMethod() {
    orderService.getPaymentMethods()
            .forEach(paymentMethod -> assertTrue(Arrays.asList(PaymentMethod.values()).contains(paymentMethod)));
  }

  @Test
  public void testPlaceOrderWithValidParams() throws ValidationErrorsException, EmptyCartException {
    cart.getItems().add(new CartItem(productDao.get(1L).get(), 1));
    cart.getItems().add(new CartItem(productDao.get(2L).get(), 2));
    cart.getItems().add(new CartItem(productDao.get(3L).get(), 3));

    when(request.getParameter("firstName")).thenReturn("asd");
    when(request.getParameter("lastName")).thenReturn("asd");
    when(request.getParameter("phone")).thenReturn("+375 (33) 123-23-23");
    when(request.getParameter("deliveryAddress")).thenReturn("asd");
    when(request.getParameter("deliveryDate")).thenReturn(LocalDate.now().toString());
    when(request.getParameter("paymentMethod")).thenReturn(PaymentMethod.CREDIT_CARD.name());
    Order order = orderService.placeOrder(request);
    assertTrue(orderDao.getBySecureId(order.getSecureId()).isPresent());
  }

  @Test(expected = ValidationErrorsException.class)
  public void testPlaceOrderWithInvalidFirstName() throws ValidationErrorsException, EmptyCartException {
    cart.getItems().add(new CartItem(productDao.get(1L).get(), 1));
    cart.getItems().add(new CartItem(productDao.get(2L).get(), 2));
    cart.getItems().add(new CartItem(productDao.get(3L).get(), 3));

    when(request.getParameter("firstName")).thenReturn("");
    when(request.getParameter("lastName")).thenReturn("asd");
    when(request.getParameter("phone")).thenReturn("+375 (33) 123-23-23");
    when(request.getParameter("deliveryAddress")).thenReturn("sad");
    when(request.getParameter("deliveryDate")).thenReturn(String.valueOf(LocalDate.now()));
    when(request.getParameter("paymentMethod")).thenReturn(PaymentMethod.CREDIT_CARD.name());
    orderService.placeOrder(request);
  }

  @Test(expected = ValidationErrorsException.class)
  public void testPlaceOrderWithInvalidLastName() throws ValidationErrorsException, EmptyCartException {
    cart.getItems().add(new CartItem(productDao.get(1L).get(), 1));
    cart.getItems().add(new CartItem(productDao.get(2L).get(), 2));
    cart.getItems().add(new CartItem(productDao.get(3L).get(), 3));

    when(request.getParameter("firstName")).thenReturn("asd");
    when(request.getParameter("lastName")).thenReturn("");
    when(request.getParameter("phone")).thenReturn("+375 (33) 123-23-23");
    when(request.getParameter("deliveryAddress")).thenReturn("sad");
    when(request.getParameter("deliveryDate")).thenReturn(String.valueOf(LocalDate.now()));
    when(request.getParameter("paymentMethod")).thenReturn(PaymentMethod.CREDIT_CARD.name());
    orderService.placeOrder(request);
  }

  @Test(expected = ValidationErrorsException.class)
  public void testPlaceOrderWithInvalidPhone() throws ValidationErrorsException, EmptyCartException {
    cart.getItems().add(new CartItem(productDao.get(1L).get(), 1));
    cart.getItems().add(new CartItem(productDao.get(2L).get(), 2));
    cart.getItems().add(new CartItem(productDao.get(3L).get(), 3));

    when(request.getParameter("firstName")).thenReturn("asd");
    when(request.getParameter("lastName")).thenReturn("asd");
    when(request.getParameter("phone")).thenReturn("+375 (3341) 123-23-23123");
    when(request.getParameter("deliveryAddress")).thenReturn("sad");
    when(request.getParameter("deliveryDate")).thenReturn(String.valueOf(LocalDate.now()));
    when(request.getParameter("paymentMethod")).thenReturn(PaymentMethod.CREDIT_CARD.name());
    orderService.placeOrder(request);
  }

  @Test(expected = ValidationErrorsException.class)
  public void testPlaceOrderWithInvalidDeliveryAddress() throws ValidationErrorsException, EmptyCartException {
    cart.getItems().add(new CartItem(productDao.get(1L).get(), 1));
    cart.getItems().add(new CartItem(productDao.get(2L).get(), 2));
    cart.getItems().add(new CartItem(productDao.get(3L).get(), 3));

    when(request.getParameter("firstName")).thenReturn("asd");
    when(request.getParameter("lastName")).thenReturn("asd");
    when(request.getParameter("phone")).thenReturn("+375 (33) 123-23-23");
    when(request.getParameter("deliveryAddress")).thenReturn("");
    when(request.getParameter("deliveryDate")).thenReturn(String.valueOf(LocalDate.now()));
    when(request.getParameter("paymentMethod")).thenReturn(PaymentMethod.CREDIT_CARD.name());
    orderService.placeOrder(request);
  }

  @Test(expected = ValidationErrorsException.class)
  public void testPlaceOrderWithInvalidDeliveryDate() throws ValidationErrorsException, EmptyCartException {
    cart.getItems().add(new CartItem(productDao.get(1L).get(), 1));
    cart.getItems().add(new CartItem(productDao.get(2L).get(), 2));
    cart.getItems().add(new CartItem(productDao.get(3L).get(), 3));

    when(request.getParameter("firstName")).thenReturn("asd");
    when(request.getParameter("lastName")).thenReturn("asd");
    when(request.getParameter("phone")).thenReturn("+375 (33) 123-23-23");
    when(request.getParameter("deliveryAddress")).thenReturn("asd");
    when(request.getParameter("deliveryDate")).thenReturn(String.valueOf(LocalDate.of(2002, 1, 3)));
    when(request.getParameter("paymentMethod")).thenReturn(PaymentMethod.CREDIT_CARD.name());
    orderService.placeOrder(request);
  }

  @Test(expected = ValidationErrorsException.class)
  public void testPlaceOrderWithInvalidPaymentMethod() throws ValidationErrorsException, EmptyCartException {
    cart.getItems().add(new CartItem(productDao.get(1L).get(), 1));
    cart.getItems().add(new CartItem(productDao.get(2L).get(), 2));
    cart.getItems().add(new CartItem(productDao.get(3L).get(), 3));

    when(request.getParameter("firstName")).thenReturn("asd");
    when(request.getParameter("lastName")).thenReturn("asd");
    when(request.getParameter("phone")).thenReturn("+375 (33) 123-23-23");
    when(request.getParameter("deliveryAddress")).thenReturn("asd");
    when(request.getParameter("deliveryDate")).thenReturn(LocalDate.now().toString());
    when(request.getParameter("paymentMethod")).thenReturn("asd");
    orderService.placeOrder(request);
  }

  @Test(expected = EmptyCartException.class)
  public void testPlaceOrderWithEmptyCart() throws ValidationErrorsException, EmptyCartException {
    orderService.placeOrder(request);
  }

  private Function<CartItem, Long> toId() {
    return cartItem -> cartItem.getProduct().getId();
  }
}