package com.es.phoneshop.web;

import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartService;
import com.es.phoneshop.model.cart.DefaultCartService;
import com.es.phoneshop.model.order.DefaultOrderService;
import com.es.phoneshop.model.order.Order;
import com.es.phoneshop.model.order.OrderService;
import com.es.phoneshop.model.order.PaymentMethod;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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
    Cart cart = cartService.getCart(request);
    Order order = orderService.getOrder(cart);
    Map<String, String> validationErrors = new HashMap<>();

    setRequiredParameter(request, "firstName", validationErrors, order::setFirstName);
    setRequiredParameter(request, "lastName", validationErrors, order::setLastName);
    setPhoneNumber(request, validationErrors, order);
    setRequiredParameter(request, "deliveryAddress", validationErrors, order::setDeliveryAddress);
    setDeliveryDate(request, validationErrors, order);
    setPaymentMethod(request, validationErrors, order);

    if (validationErrors.isEmpty()) {
      orderService.placeOrder(order);
      response.sendRedirect(request.getContextPath() + "/overview/" + order.getId());
    } else {
      request.setAttribute("validationErrors", validationErrors);
      request.setAttribute("order", order);
      request.setAttribute("paymentMethods", orderService.getPaymentMethods());
      request.getRequestDispatcher("/WEB-INF/pages/checkout.jsp").forward(request, response);
    }
  }

  private void setRequiredParameter(HttpServletRequest request, String parameter, Map<String, String> errors,
                                    Consumer<String> consumer) {
    String value = request.getParameter(parameter);
    if (value == null || value.isEmpty()) {
      errors.put(parameter, "Missing value");
    } else {
      consumer.accept(value);
    }
  }

  private void setDeliveryDate(HttpServletRequest request, Map<String, String> errors, Order order) {
    String deliveryDateString = request.getParameter("deliveryDate");
    if (deliveryDateString == null || deliveryDateString.isEmpty()) {
      errors.put("deliveryDate", "Missing value");
      return;
    }
    LocalDate deliveryDate;
    try {
      deliveryDate = LocalDate.parse(deliveryDateString);
    } catch (DateTimeParseException e) {
      errors.put("deliveryDate", "Invalid value");
      return;
    }
    if (deliveryDate.isBefore(LocalDate.now())) {
      errors.put("deliveryDate", "Invalid value");
      return;
    }
    order.setDeliveryDate(deliveryDate);
  }

  private void setPaymentMethod(HttpServletRequest request, Map<String, String> errors, Order order) {
    String paymentMethodString = request.getParameter("paymentMethod");
    if (paymentMethodString == null || paymentMethodString.isEmpty()) {
      errors.put("paymentMethod", "Missing value");
      return;
    }
    try {
      order.setPaymentMethod(PaymentMethod.valueOf(paymentMethodString));
    } catch (IllegalArgumentException e) {
      errors.put("paymentMethod", "Invalid value");
    }

  }

  private void setPhoneNumber(HttpServletRequest request, Map<String, String> errors, Order order) {
    String phoneString = request.getParameter("phone");
    if (phoneString == null || phoneString.isEmpty()) {
      errors.put("phone", "Missing phone");
      return;
    }
    if (!phoneString.matches("^\\+375 \\((17|29|33|44)\\) [0-9]{3}-[0-9]{2}-[0-9]{2}$"))
      errors.put("phone", "Phone number must correspond Belarusian format: +375 (**) ***-**-**");
    order.setPhone(phoneString);
  }
}