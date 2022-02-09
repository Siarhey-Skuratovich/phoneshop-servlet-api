package com.es.phoneshop.model.product.cart;

import com.es.phoneshop.model.product.Product;

import java.util.concurrent.atomic.AtomicInteger;

public class CartItem {
  private final Product product;
  private final AtomicInteger quantity;

  public CartItem(Product product, int quantity) {
    this.product = product;
    this.quantity = new AtomicInteger(quantity);
  }

  public Product getProduct() {
    return product;
  }

  public int getQuantity() {
    return quantity.get();
  }

  @Override
  public String toString() {
    return "[" + product.getCode() + ", " + quantity + "]";
  }

  public void increaseQuantity(int quantity) {
    this.quantity.addAndGet(quantity);
  }
}
