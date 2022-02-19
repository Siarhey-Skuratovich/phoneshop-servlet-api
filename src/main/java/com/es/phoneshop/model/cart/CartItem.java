package com.es.phoneshop.model.cart;

import com.es.phoneshop.model.product.Product;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class CartItem implements Serializable, Cloneable {
  private static final long serialVersionUID = 2814867576609344537L;

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

  public void setQuantity(int quantity) {
    this.quantity.set(quantity);
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
