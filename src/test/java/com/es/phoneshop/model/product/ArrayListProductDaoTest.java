package com.es.phoneshop.model.product;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

public class ArrayListProductDaoTest {
  private ProductDao productDao;

  @Before
  public void setup() {
    productDao = new ArrayListProductDao();
  }

  @Test
  public void testFindProductsNoResults() {
    assertFalse(productDao.findProducts(null, null, null).isEmpty());
    assertFalse(productDao.findProducts("", null, null).isEmpty());
  }

  @Test
  public void checkIfTheRightProductHasBeenGot() {
    assertEquals("sgs3", productDao.getProduct(2L).get().getCode());
  }

  @Test
  public void checkIfTheProductIsDeleted() {
    productDao.delete(3L);
    assertFalse(productDao.getProduct(3L).isPresent());
  }

  @Test
  public void checkIfTheProductIsSaved() {
    Product newProduct = new Product("WAS-LX1", "Huawei P10 Lite", new BigDecimal(100), Currency.getInstance("USD"), 1, null);
    productDao.save(newProduct);
    assertNotNull(newProduct.getId());

    Optional<Product> productOptional = productDao.getProduct(newProduct.getId());
    assertTrue(productOptional.isPresent());
    assertEquals("WAS-LX1", productOptional.get().getCode());
  }

  @Test
  public void testProductUpdate() {
    Product product = new Product("WAS-LX1", "Huawei P10 Lite", new BigDecimal(100), Currency.getInstance("USD"), 1, null);
    product.setId(3L);
    productDao.save(product);
    assertEquals("WAS-LX1", productDao.getProduct(3L).get().getCode());
  }

  @Test
  public void testFindProductsIfStockIsZero() {
    assertTrue(productDao.findProducts(null, null, null).stream().allMatch(product -> product.getStock() > 0));
  }

  @Test
  public void testFindProductsIfPriceIsNull() {
    int oldSize = productDao.findProducts(null, null, null).size();
    Product newProduct = new Product("WAS-LX1", "Huawei P10 Lite", null, Currency.getInstance("USD"), 1, null);
    productDao.save(newProduct);
    assertEquals(oldSize, productDao.findProducts(null, null, null).size());
  }

  @Test
  public void testFindProductsByQuery() {
    String query = "Samsung III";
    List<Product> filteredAndSortedByQueryList = productDao.findProducts(query, null, null);
    assertEquals("sgs3", filteredAndSortedByQueryList.get(0).getCode());
    assertEquals("sgs", filteredAndSortedByQueryList.get(1).getCode());
    assertEquals(2, filteredAndSortedByQueryList.size());
  }

  @Test
  public void testGetProductByNullId() {
    assertFalse(productDao.getProduct(null).isPresent());
  }

}
