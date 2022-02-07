package com.es.phoneshop.model.product.mutex;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public class IdMutexProvider {

  private final Map<Mutex, WeakReference<Mutex>> mutexMap = new WeakHashMap<>();

  public Mutex getMutex(String id) {
    if (id == null) {
      throw new NullPointerException();
    }

    Mutex key = new MutexImpl(id);
    synchronized (mutexMap) {
      WeakReference<Mutex> ref = mutexMap.get(key);
      if (ref == null) {
        mutexMap.put(key, new WeakReference<>(key));
        return key;
      }
      Mutex mutex = ref.get();
      if (mutex == null) {
        mutexMap.put(key, new WeakReference<>(key));
        return key;
      }
      return mutex;
    }
  }

  public int getMutexCount() {
    synchronized (mutexMap) {
      return mutexMap.size();
    }
  }

  public interface Mutex {
  }

  private static class MutexImpl implements Mutex {
    private final String id;

    protected MutexImpl(String id) {
      this.id = id;
    }

    public boolean equals(Object o) {
      if (o == null) {
        return false;
      }
      if (this.getClass() == o.getClass()) {
        return this.id.equals(o.toString());
      }
      return false;
    }

    public int hashCode() {
      return id.hashCode();
    }

    public String toString() {
      return id;
    }
  }
}