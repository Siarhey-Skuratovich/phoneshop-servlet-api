package com.es.phoneshop.model.product.cart.exception;

public class QuantitySumInCartWillBeMoreThanStockException extends Exception {
  private final int currentCartItemQuantity;

  public QuantitySumInCartWillBeMoreThanStockException(int currentCartItemQuantity) {
    this.currentCartItemQuantity = currentCartItemQuantity;
  }

  public int getCurrentCartItemQuantity() {
    return currentCartItemQuantity;
  }
}
