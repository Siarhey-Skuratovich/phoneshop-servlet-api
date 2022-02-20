package com.es.phoneshop.web;

import com.es.phoneshop.model.order.*;
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
import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderOverviewPageServletTest {
  private OrderOverviewPageServlet servlet = new OrderOverviewPageServlet();
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private RequestDispatcher requestDispatcher;
  @Mock
  private ServletConfig config;

  private OrderService orderService;

  private OrderDao orderDao;

  @Before
  public void setUp() throws ServletException {
    orderDao = ArrayListOrderDao.getInstance();
    orderService = DefaultOrderService.getInstance();

    servlet.init(config);

    when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
  }

  @Test
  public void testDoGetWithNotExistingSecureId() throws ServletException, IOException {
    orderService.placeOrder(new Order());
    orderService.placeOrder(new Order());
    orderService.placeOrder(new Order());

    String secureId = "asd";
    when(request.getPathInfo()).thenReturn("/" + secureId);
    servlet.doGet(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/order-not-found"
            + "?secureOrderId="
            + secureId);
  }

  @Test
  public void testDoGetWithExistingSecureId() throws ServletException, IOException {
    Order order = new Order();
    String secureId = UUID.randomUUID().toString();
    order.setSecureId(secureId);
    orderDao.save(order);

    when(request.getPathInfo()).thenReturn("/" + secureId);
    servlet.doGet(request, response);

    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/orderOverview.jsp"));
    verify(requestDispatcher).forward(request, response);
  }
}