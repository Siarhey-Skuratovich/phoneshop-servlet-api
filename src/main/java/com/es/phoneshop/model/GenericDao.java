package com.es.phoneshop.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class GenericDao <T extends Identifiable> {
  private long maxId;
  private final List<T> items = new ArrayList<>();
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  public Optional<T> get(Long id) {
    if (id == null) {
      return Optional.empty();
    }

    lock.readLock().lock();
    try {
      return items.stream()
              .filter(item -> id.equals(item.getId()))
              .findAny();
    } finally {
      lock.readLock().unlock();
    }
  }

  public void save(T t) {
    lock.writeLock().lock();
    try {
      if (t.getId() != null) {
        for (int i = 0; i < items.size(); i++) {
          if (t.getId().equals(items.get(i).getId())) {
            items.set(i, t);
            break;
          }
        }
      } else {
        t.setId(maxId++);
        items.add(t);
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  protected List<T> getItems() {
    return items;
  }

  protected ReadWriteLock getLock() {
    return lock;
  }
}
