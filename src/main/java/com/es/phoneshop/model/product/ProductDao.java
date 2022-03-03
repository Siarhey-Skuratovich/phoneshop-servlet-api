package com.es.phoneshop.model.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductDao {
  Optional<Product> get(Long id);

  List<Product> findProducts(String query, SortField sortField, SortOrder sortOrder);

  List<Product> findProductsByAdvancedSearch(String productCode, BigDecimal minPrice, BigDecimal maxPrice, Integer minStock);

  void save(Product product);

  void delete(Long id);
}
