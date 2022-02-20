package com.es.phoneshop.model.order;

import com.es.phoneshop.model.GenericDao;

import java.util.Optional;

public class ArrayListOrderDao extends GenericDao<Order> implements OrderDao {

  public ArrayListOrderDao() {
  }

  private static class InstanceHolder {
    private final static ArrayListOrderDao instance = new ArrayListOrderDao();
  }

  public static ArrayListOrderDao getInstance() {
    return InstanceHolder.instance;
  }

  @Override
  public Optional<Order> getBySecureId(String secureId) {
    if (secureId == null) {
      return Optional.empty();
    }

    getLock().readLock().lock();
    try {
      return getItems().stream()
              .filter(item -> secureId.equals(item.getSecureId()))
              .findAny();
    } finally {
      getLock().readLock().unlock();
    }
  }

}
