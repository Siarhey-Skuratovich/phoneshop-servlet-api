package com.es.phoneshop.model.product;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ArrayListProductDao implements ProductDao {
  private long maxId;
  private final List<Product> products;
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private ArrayListProductDao() {
    products = new ArrayList<>();
  }

  private static class InstanceHolder {
    private final static ArrayListProductDao instance = new ArrayListProductDao();
  }

  public static ArrayListProductDao getInstance() {
    return InstanceHolder.instance;
  }

  @Override
  public Optional<Product> getProduct(Long id) {
    if (id == null) {
      return Optional.empty();
    }

    lock.readLock().lock();
    try {
      return products.stream()
              .filter(product -> id.equals(product.getId()))
              .findAny();
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public List<Product> findProducts(String query, SortField sortField, SortOrder sortOrder) {
    lock.readLock().lock();
    try {
      List<Product> resultProductList = products.stream()
              .filter(getFilterPredicate(query))
              .collect(Collectors.toList());

      Comparator<Product> sortingComparator = getSortingComparator(query, sortField, sortOrder);
      if (sortingComparator != null) {
        resultProductList = resultProductList.stream()
                .sorted(sortingComparator)
                .collect(Collectors.toList());
      }
      return resultProductList;

    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public void save(Product product) {
    lock.writeLock().lock();
    try {
      if (product.getId() != null) {
        for (int i = 0; i < products.size(); i++) {
          if (product.getId().equals(products.get(i).getId())) {
            products.set(i, product);
            break;
          }
        }
      } else {
        product.setId(maxId++);
        products.add(product);
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public void delete(Long id) {
    lock.writeLock().lock();
    try {
      products.removeIf(product -> id.equals(product.getId()));
    } finally {
      lock.writeLock().unlock();
    }
  }

  private Predicate<Product> getFilterPredicate(String query) {
    Predicate<Product> notNullPricePredicate = product -> product.getPrice() != null;
    Predicate<Product> notEmptyStockPredicate = product -> product.getStock() > 0;

    if (query == null) {
      return notEmptyStockPredicate.and(notNullPricePredicate);
    }

    String[] keyWords = query.split(" ");
    Predicate<Product> containsAnyKeyWordPredicate = product -> Arrays.stream(keyWords)
            .anyMatch(keyWord -> product.getDescription().contains(keyWord));

    return notEmptyStockPredicate.and(notNullPricePredicate).and(containsAnyKeyWordPredicate);
  }

  private Comparator<Product> getSortingComparator(String query, SortField sortField, SortOrder sortOrder) {
    if (query != null && !query.isEmpty() && sortField == null) {
      return getSortingComparatorByQuery(query);
    }
    if (sortField != null) {
      return getSortingComparatorByFieldAndOrder(sortField, sortOrder);
    }
    return null;
  }

  private Comparator<Product> getSortingComparatorByQuery(String query) {
    String[] keyWords = query.split(" ");
    return Comparator.comparing((Product product) -> Arrays.stream(keyWords)
            .filter(product.getDescription()::contains).count())
            .reversed();
  }

  private Comparator<Product> getSortingComparatorByFieldAndOrder(SortField sortField, SortOrder sortOrder) {
    EnumMap<SortField, Comparator<Product>> sortingComparators = new EnumMap<>(SortField.class);
    sortingComparators.put(SortField.description, Comparator.comparing(Product::getDescription));
    sortingComparators.put(SortField.price, Comparator.comparing(Product::getPrice));

    if (sortOrder == SortOrder.desc) {
      return sortingComparators.get(sortField).reversed();
    }
    return sortingComparators.get(sortField);
  }
}
