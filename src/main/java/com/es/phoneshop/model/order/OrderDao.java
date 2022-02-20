package com.es.phoneshop.model.order;

import java.util.Optional;

public interface OrderDao {
  Optional<Order> get(Long id);

  void save(Order order);

  Optional<Order> getBySecureId(String secureId);
}
