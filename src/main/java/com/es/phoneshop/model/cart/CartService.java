package com.es.phoneshop.model.cart;

import com.es.phoneshop.model.cart.exception.QuantitySumInCartWillBeMoreThanStockException;
import com.es.phoneshop.model.cart.exception.OutOfStockException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public interface CartService {
  Cart getCart(HttpServletRequest request);

  void add(Cart cart, Long productId, int quantity, HttpSession session) throws QuantitySumInCartWillBeMoreThanStockException, OutOfStockException;

  void update(Cart cart, Long productId, int quantity, HttpSession session) throws OutOfStockException;

  void delete(Cart cart, Long productId, HttpSession session);

  void clearCart(Cart cart, HttpSession session);

  Cart makeCloneOf(Cart cart) throws IOException, ClassNotFoundException;

  void recalculateCart(Cart cart);
}
