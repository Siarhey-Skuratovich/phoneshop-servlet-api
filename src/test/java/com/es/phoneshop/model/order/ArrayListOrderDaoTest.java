package com.es.phoneshop.model.order;

import com.es.phoneshop.model.GenericDao;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartItem;
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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArrayListOrderDaoTest {
  private final OrderService orderService = DefaultOrderService.getInstance();
  private final ProductDao productDao = ArrayListProductDao.getInstance();
  private final OrderDao orderDao = ArrayListOrderDao.getInstance();

  @Mock
  private ServletContextEvent event;
  @Mock
  private ServletContext servletContext;

  @Before
  public void setUp() throws Exception {
    boolean productArrayIsEmpty = productDao.findProducts(null, null, null).isEmpty();

    if (productArrayIsEmpty) {
      DemoDataServletContextListener demoDataServletContextListener = new DemoDataServletContextListener();
      when(event.getServletContext()).thenReturn(servletContext);
      when(servletContext.getInitParameter("insertDemoData")).thenReturn("true");
      demoDataServletContextListener.contextInitialized(event);
    }
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
  public void testGetByNullId() {
    assertFalse(orderDao.get(null).isPresent());
  }

  @Test
  public void checkIfTheOrderIsSaved() {
    Cart cart = new Cart();
    long productId1 = 0L;
    long productId2 = 4L;
    long productId3 = 7L;
    CartItem cartItem1 = new CartItem(productDao.get(productId1).get(), 1);
    CartItem cartItem2 = new CartItem(productDao.get(productId2).get(), 2);
    CartItem cartItem3 = new CartItem(productDao.get(productId3).get(), 3);
    List<CartItem> cartItemList = List.of(cartItem1, cartItem2, cartItem3);
    cart.setItems(cartItemList);

    Order expectedOrder = orderService.getOrder(cart);

    orderDao.save(expectedOrder);
    Order actualOrder = orderDao.get(0L).get();

    assertEquals(expectedOrder, actualOrder);
  }

  @Test
  public void testGetByOrderId() {
    Cart cart = new Cart();
    long productId1 = 0L;
    long productId2 = 4L;
    long productId3 = 7L;
    CartItem cartItem1 = new CartItem(productDao.get(productId1).get(), 1);
    CartItem cartItem2 = new CartItem(productDao.get(productId2).get(), 2);
    CartItem cartItem3 = new CartItem(productDao.get(productId3).get(), 3);
    List<CartItem> cartItemList = List.of(cartItem1, cartItem2, cartItem3);
    cart.setItems(cartItemList);
    Order expectedOrder = orderService.getOrder(cart);

    orderService.placeOrder(expectedOrder);
    String secureId = expectedOrder.getSecureId();

    Order actualOrder = orderDao.getBySecureId(secureId).get();

    assertEquals(expectedOrder, actualOrder);
  }
}