package com.es.phoneshop.model.product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;

public class PriceChange implements Serializable {
  private static final long serialVersionUID = -4613545321743128814L;

  private final String startDate;
  private final BigDecimal price;
  private final Currency currency;

  public PriceChange(LocalDate startDate, BigDecimal price, Currency currency) {
    this.startDate = startDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
    this.price = price;
    this.currency = currency;
  }

  public String getStartDate() {
    return startDate;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public Currency getCurrency() {
    return currency;
  }
}

