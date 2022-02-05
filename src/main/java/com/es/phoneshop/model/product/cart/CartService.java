package com.es.phoneshop.model.product.cart;

import javax.servlet.http.HttpServletRequest;

public interface CartService {
  Cart getCart(HttpServletRequest request);

  void add(Cart cart, Long productId, int quantity);
}
