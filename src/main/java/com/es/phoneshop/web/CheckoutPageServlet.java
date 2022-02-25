package com.es.phoneshop.web;

import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartService;
import com.es.phoneshop.model.cart.DefaultCartService;
import com.es.phoneshop.model.order.DefaultOrderService;
import com.es.phoneshop.model.order.Order;
import com.es.phoneshop.model.order.OrderService;
import com.es.phoneshop.model.order.exception.ValidationErrorsException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CheckoutPageServlet extends HttpServlet {
  private CartService cartService;
  private OrderService orderService;


  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    cartService = DefaultCartService.getInstance();
    orderService = DefaultOrderService.getInstance();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Cart cart = cartService.getCart(request);
    request.setAttribute("order", orderService.getOrder(cart));
    request.setAttribute("paymentMethods", orderService.getPaymentMethods());
    request.getRequestDispatcher("/WEB-INF/pages/checkout.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      Order placedOrder = orderService.placeOrder(request);
      response.sendRedirect(request.getContextPath() + "/order/overview/" + placedOrder.getSecureId());
    } catch (ValidationErrorsException e) {
      request.setAttribute("validationErrors", e.getValidationErrors());
      request.setAttribute("order", e.getOrder());
      request.setAttribute("paymentMethods", orderService.getPaymentMethods());
      request.getRequestDispatcher("/WEB-INF/pages/checkout.jsp").forward(request, response);
    }
  }
}