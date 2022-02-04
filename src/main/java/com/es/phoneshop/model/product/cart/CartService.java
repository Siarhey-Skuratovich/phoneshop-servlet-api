package com.es.phoneshop.model.product.cart;

public interface CartService {
  Cart getCart();

  void add(Long productId, int quantity);
}
