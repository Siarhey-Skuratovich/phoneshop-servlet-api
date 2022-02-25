package com.es.phoneshop.model.order;

import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.order.exception.EmptyCartException;
import com.es.phoneshop.model.order.exception.ValidationErrorsException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface OrderService {
  Order getOrder(Cart cart);

  List<PaymentMethod> getPaymentMethods();

  Order placeOrder(HttpServletRequest request) throws ValidationErrorsException, EmptyCartException;
}
