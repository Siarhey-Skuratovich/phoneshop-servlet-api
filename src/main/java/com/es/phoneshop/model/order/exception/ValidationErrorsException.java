package com.es.phoneshop.model.order.exception;

import com.es.phoneshop.model.order.Order;

import java.util.Map;

public class ValidationErrorsException extends Exception{
  private final Map<String, String> validationErrors;
  private final Order order;

  public ValidationErrorsException(Map<String, String> validationErrors, Order order) {
    this.validationErrors = validationErrors;
    this.order = order;
  }

  public Map<String, String> getValidationErrors() {
    return validationErrors;
  }

  public Order getOrder() {
    return order;
  }
}
