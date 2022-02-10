package com.es.phoneshop.model.product.cart;

import com.es.phoneshop.model.product.cart.exception.QuantitySumInCartWillBeMoreThanStockException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public interface CartService {
  Cart getCart(HttpServletRequest request);

  void add(Cart cart, Long productId, int quantity, HttpSession session) throws QuantitySumInCartWillBeMoreThanStockException;

  void update(Cart cart, Long productId, int quantity, HttpSession session);
}
