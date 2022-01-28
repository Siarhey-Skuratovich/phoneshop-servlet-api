package com.es.phoneshop.model.product;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
      List<Product> productList = (query != null && !query.isEmpty())
              ? applyQuery(query, products)
              : products;

      if (sortField != null) {
        Comparator<Product> sortingComparator = getSortingComparator(sortField, sortOrder);
        productList = productList.stream()
                .sorted(sortingComparator)
                .collect(Collectors.toList());
      }

      return productList.stream()
              .filter(product -> product.getPrice() != null)
              .filter(product -> product.getStock() > 0)
              .collect(Collectors.toList());

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

  private List<Product> applyQuery(String query, List<Product> products) {
    String[] keyWords = query.split(" ");
    Comparator<Product> comparator = Comparator.comparing((Product product) -> {
      int matchCount = 0;
      for (String keyWord : keyWords) {
        if (product.getDescription().contains(keyWord)) {
          matchCount++;
        }
      }
      return matchCount;
    }).reversed();

    return products.stream()
            .filter(product -> Arrays.stream(keyWords)
                    .anyMatch(keyWord -> product.getDescription().contains(keyWord)))
            .sorted(comparator)
            .collect(Collectors.toList());
  }

  private Comparator<Product> getSortingComparator(SortField sortField, SortOrder sortOrder) {
    Comparator<Product> sortingComparator = (sortField == SortField.description)
            ? Comparator.comparing(Product::getDescription)
            : Comparator.comparing(Product::getPrice);

    if (sortOrder == SortOrder.desc) {
      sortingComparator = sortingComparator.reversed();
    }
    return sortingComparator;
  }
}
