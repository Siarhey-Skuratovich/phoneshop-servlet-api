package com.es.phoneshop.model.order;

import com.es.phoneshop.model.GenericDao;

public class ArrayListOrderDao extends GenericDao<Order> implements OrderDao {

  public ArrayListOrderDao() {
  }

  private static class InstanceHolder {
    private final static ArrayListOrderDao instance = new ArrayListOrderDao();
  }

  public static ArrayListOrderDao getInstance() {
    return InstanceHolder.instance;
  }
}
