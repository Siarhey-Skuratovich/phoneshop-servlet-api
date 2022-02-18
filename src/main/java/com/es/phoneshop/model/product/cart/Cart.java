package com.es.phoneshop.model.product.cart;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Cart implements Serializable {
  private static final long serialVersionUID = -5168977499824515044L;

  private int totalQuantity;
  private BigDecimal totalCost;
  private List<CartItem> items;

  public Cart() {
    this.items = new CopyOnWriteArrayList<>();
    this.totalCost = BigDecimal.valueOf(0);
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

  public int getTotalQuantity() {
    return totalQuantity;
  }

  public void setTotalQuantity(int totalQuantity) {
    this.totalQuantity = totalQuantity;
  }

  public BigDecimal getTotalCost() {
    return totalCost;
  }

  public void setTotalCost(BigDecimal totalCost) {
    this.totalCost = totalCost;
  }

  public void setItems(List<CartItem> items) {
    this.items = items;
  }
}
