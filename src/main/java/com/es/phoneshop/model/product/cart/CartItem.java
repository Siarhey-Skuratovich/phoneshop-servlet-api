package com.es.phoneshop.model.product.cart;

import com.es.phoneshop.model.product.Product;

public class CartItem {
  private final Product product;
  private int quantity;

  public CartItem(Product product, int quantity) {
    this.product = product;
    this.quantity = quantity;
  }

  public Product getProduct() {
    return product;
  }

  public int getQuantity() {
    return quantity;
  }

  @Override
  public String toString() {
    return "[" + product.getCode() + ", " + quantity + "]";
  }

  public void increaseQuantity(int quantity) {
    this.quantity += quantity;
  }
}
