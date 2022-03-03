package com.es.phoneshop.model.product;

import com.es.phoneshop.model.GenericDao;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ArrayListProductDao extends GenericDao<Product> implements ProductDao {
  private final Map<SortField, Comparator<Product>> sortingComparators;

  private ArrayListProductDao() {
    EnumMap<SortField, Comparator<Product>> sortingComparators = new EnumMap<>(SortField.class);
    sortingComparators.put(SortField.description, Comparator.comparing(Product::getDescription));
    sortingComparators.put(SortField.price, Comparator.comparing(Product::getPrice));
    this.sortingComparators = Collections.unmodifiableMap(sortingComparators);
  }

  private static class InstanceHolder {
    private final static ArrayListProductDao instance = new ArrayListProductDao();
  }

  public static ArrayListProductDao getInstance() {
    return InstanceHolder.instance;
  }

  @Override
  public List<Product> findProducts(String query, SortField sortField, SortOrder sortOrder) {
    getLock().readLock().lock();
    try {
      List<Product> resultProductList = getItems().stream()
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
      getLock().readLock().unlock();
    }
  }

  @Override
  public List<Product> findProductsByAdvancedSearch(String productCode,
                                                    BigDecimal minPrice,
                                                    BigDecimal maxPrice,
                                                    Integer minStock) {
    getLock().readLock().lock();
    try {
      return getItems().stream()
              .filter(getAdvancedFilterPredicate(productCode, minPrice, maxPrice, minStock))
              .collect(Collectors.toList());
    } finally {
      getLock().readLock().unlock();
    }
  }

  @Override
  public void delete(Long id) {
    getLock().writeLock().lock();
    try {
      getItems().removeIf(product -> id.equals(product.getId()));
    } finally {
      getLock().writeLock().unlock();
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

  private Predicate<Product> getAdvancedFilterPredicate(String productCode,
                                                        BigDecimal minPrice,
                                                        BigDecimal maxPrice,
                                                        Integer minStock) {
    List<Predicate<Product>> allPredicates = new ArrayList<>();
    if (productCode != null) {
      allPredicates.add(product -> productCode.equals(product.getCode()));
    }

    if (minPrice != null) {
      allPredicates.add(product -> product.getPrice().compareTo(minPrice) >= 0);
    }

    if (maxPrice != null) {
      allPredicates.add(product -> product.getPrice().compareTo(maxPrice) <= 0);
    }

    if (minStock != null) {
      allPredicates.add(product -> product.getStock() >= minStock);
    }

    return allPredicates.stream().reduce(x -> true, Predicate::and);
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
    if (sortOrder == SortOrder.desc) {
      return sortingComparators.get(sortField).reversed();
    }
    return sortingComparators.get(sortField);
  }
}
