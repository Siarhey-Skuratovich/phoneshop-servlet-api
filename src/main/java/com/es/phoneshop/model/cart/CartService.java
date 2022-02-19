package com.es.phoneshop.model.cart;

import com.es.phoneshop.model.cart.exception.QuantitySumInCartWillBeMoreThanStockException;
import com.es.phoneshop.model.cart.exception.OutOfStockException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public interface CartService {
  Cart getCart(HttpServletRequest request);

  void add(Cart cart, Long productId, int quantity, HttpSession session) throws QuantitySumInCartWillBeMoreThanStockException, OutOfStockException;

  void update(Cart cart, Long productId, int quantity, HttpSession session) throws OutOfStockException;

  void delete(Cart cart, Long productId, HttpSession session);
}
