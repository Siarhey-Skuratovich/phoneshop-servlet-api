package com.es.phoneshop.model.product.cart;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class Cart {
  private final List<CartItem> items;

  public Cart() {
    this.items = new CopyOnWriteArrayList<>();
  }

  public List<CartItem> getItems() {
    return items;
  }

  @Override
  public String toString() {
    return "Cart " + items;
  }

  public Optional<CartItem> getCartItemByProductId(long productId) {
    return items.stream()
            .filter(cartItem -> cartItem.getProduct().getId() == productId)
            .findAny();
  }
}
