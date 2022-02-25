package com.es.phoneshop.web;

import com.es.phoneshop.model.cart.DefaultCartService;
import com.es.phoneshop.model.order.PaymentMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
  private HttpSession session;

  private final CheckoutPageServlet servlet = new CheckoutPageServlet();

  @Before
  public void setUp() throws Exception {
    servlet.init(config);
    when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute(DefaultCartService.class.getName() + ".lock")).thenReturn(new ReentrantLock());
  }

  @Test
  public void testDoGet() throws ServletException, IOException {
    servlet.doGet(request, response);

    verify(request).setAttribute(eq("order"), any());
    verify(request).setAttribute(eq("paymentMethods"), any());
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/checkout.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoPostWithInvalidAttributes() throws ServletException, IOException {
    servlet.doPost(request, response);
    verify(request).setAttribute(eq("validationErrors"), anyMap());
    verify(request).setAttribute(eq("order"), any());
    verify(request).setAttribute(eq("paymentMethods"), any());
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/checkout.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoPostWithValidAttributes() throws ServletException, IOException {
    when(request.getParameter("firstName")).thenReturn("asd");
    when(request.getParameter("lastName")).thenReturn("asd");
    when(request.getParameter("phone")).thenReturn("+375 (33) 123-23-23");
    when(request.getParameter("deliveryAddress")).thenReturn("asd");
    when(request.getParameter("deliveryDate")).thenReturn(LocalDate.now().toString());
    when(request.getParameter("paymentMethod")).thenReturn(PaymentMethod.CREDIT_CARD.name());
    servlet.doPost(request, response);
    verify(response).sendRedirect(any());
  }
}