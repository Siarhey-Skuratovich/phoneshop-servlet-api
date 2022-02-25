package com.es.phoneshop.web;

import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartItem;
import com.es.phoneshop.model.cart.DefaultCartService;
import com.es.phoneshop.model.order.PaymentMethod;
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
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CheckoutPageServletTest {
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
  @Mock
  private HttpSession session;

  private ProductDao productDao;

  private final Cart cart = new Cart();

  private final CheckoutPageServlet servlet = new CheckoutPageServlet();

  @Before
  public void setUp() throws Exception {
    productDao = ArrayListProductDao.getInstance();
    boolean productArrayIsEmpty = productDao.findProducts(null, null, null).isEmpty();

    if (productArrayIsEmpty) {
      DemoDataServletContextListener demoDataServletContextListener = new DemoDataServletContextListener();
      when(event.getServletContext()).thenReturn(servletContext);
      when(servletContext.getInitParameter("insertDemoData")).thenReturn("true");
      demoDataServletContextListener.contextInitialized(event);
    }
    servlet.init(config);
    when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute(DefaultCartService.class.getName() + ".cart")).thenReturn(cart);
    when(session.getAttribute(DefaultCartService.class.getName() + ".lock")).thenReturn(new ReentrantLock());
  }

  @Test
  public void testDoGet() throws ServletException, IOException {
    cart.getItems().add(new CartItem(productDao.get(1L).get(), 1));
    cart.getItems().add(new CartItem(productDao.get(2L).get(), 2));
    cart.getItems().add(new CartItem(productDao.get(3L).get(), 3));
    servlet.doGet(request, response);

    verify(request).setAttribute(eq("order"), any());
    verify(request).setAttribute(eq("paymentMethods"), any());
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/checkout.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoGetIfCartEmpty() throws ServletException, IOException {
    servlet.doGet(request, response);
    verify(response).sendRedirect(eq(request.getContextPath()
            + "/cart?emptyCartError=You haven't added any product to order."));
  }

  @Test
  public void testDoPostWithInvalidAttributes() throws ServletException, IOException {
    cart.getItems().add(new CartItem(productDao.get(1L).get(), 1));
    cart.getItems().add(new CartItem(productDao.get(2L).get(), 2));
    cart.getItems().add(new CartItem(productDao.get(3L).get(), 3));

    servlet.doPost(request, response);
    verify(request).setAttribute(eq("validationErrors"), anyMap());
    verify(request).setAttribute(eq("order"), any());
    verify(request).setAttribute(eq("paymentMethods"), any());
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/checkout.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoPostWithValidAttributes() throws ServletException, IOException {
    cart.getItems().add(new CartItem(productDao.get(1L).get(), 1));
    cart.getItems().add(new CartItem(productDao.get(2L).get(), 2));
    cart.getItems().add(new CartItem(productDao.get(3L).get(), 3));

    when(request.getParameter("firstName")).thenReturn("asd");
    when(request.getParameter("lastName")).thenReturn("asd");
    when(request.getParameter("phone")).thenReturn("+375 (33) 123-23-23");
    when(request.getParameter("deliveryAddress")).thenReturn("asd");
    when(request.getParameter("deliveryDate")).thenReturn(LocalDate.now().toString());
    when(request.getParameter("paymentMethod")).thenReturn(PaymentMethod.CREDIT_CARD.name());
    servlet.doPost(request, response);
    verify(response).sendRedirect(contains(request.getContextPath() + "/order/overview/"));
  }

  @Test
  public void testDoPostWithEmptyCart() throws ServletException, IOException {
    servlet.doPost(request, response);
    verify(response).sendRedirect(eq(request.getContextPath()
            + "/cart?emptyCartError=You haven't added any product to order."));
  }
}