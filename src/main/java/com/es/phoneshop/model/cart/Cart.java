package com.es.phoneshop.model.cart;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;

public class Cart implements Serializable {
  private static final long serialVersionUID = -5168977499824515044L;

  private int totalQuantity;
  private BigDecimal totalCost;
  private List<CartItem> items;
  private Lock lock;

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
    if (lock != null) {
      lock.lock();
    }
    return items.stream()
            .filter(cartItem -> cartItem.getProduct().getId() == productId)
            .findAny();
  }

  public int getTotalQuantity() {
    return totalQuantity;
  }

  public void setTotalQuantity(int totalQuantity) {
    if (lock != null) {
      lock.lock();
    }
    this.totalQuantity = totalQuantity;
  }

  public BigDecimal getTotalCost() {
    if (lock != null) {
      lock.lock();
    }
    return totalCost;
  }

  public void setTotalCost(BigDecimal totalCost) {
    if (lock != null) {
      lock.lock();
    }
    this.totalCost = totalCost;
  }

  public void setItems(List<CartItem> items) {
    if (lock != null) {
      lock.lock();
    }
    this.items = items;
  }

  public void lockCartOn(Lock lock) {
    this.lock = lock;
    lock.lock();
  }

  public void unLockCart() {
    lock.unlock();
    lock = null;
  }
}
