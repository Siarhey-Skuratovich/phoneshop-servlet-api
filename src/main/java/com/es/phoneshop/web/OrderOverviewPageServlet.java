package com.es.phoneshop.web;

import com.es.phoneshop.model.order.ArrayListOrderDao;
import com.es.phoneshop.model.order.Order;
import com.es.phoneshop.model.order.OrderDao;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class OrderOverviewPageServlet extends HttpServlet {
  private OrderDao orderDao;


  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    orderDao = ArrayListOrderDao.getInstance();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String secureOrderId = request.getPathInfo().substring(1);
    Optional<Order> optionalOrder = orderDao.getBySecureId(secureOrderId);
    if (!optionalOrder.isPresent()) {
      redirectToOrderNotFoundPage(request, response, secureOrderId);
      return;
    }

    request.setAttribute("order", optionalOrder.get());
    request.getRequestDispatcher("/WEB-INF/pages/orderOverview.jsp").forward(request, response);
  }

  private void redirectToOrderNotFoundPage(HttpServletRequest request,
                                           HttpServletResponse response,
                                           String secureOrderIdString) throws IOException {
    response.sendRedirect(request.getContextPath()
            + "/order-not-found"
            + "?secureOrderId="
            + secureOrderIdString);
  }
}