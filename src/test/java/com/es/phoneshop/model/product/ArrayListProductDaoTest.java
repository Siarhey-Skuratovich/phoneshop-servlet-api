package com.es.phoneshop.model.product;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Currency;

public class ArrayListProductDaoTest {
  private ProductDao productDao;

  @Before
  public void setup() {
    productDao = new ArrayListProductDao();
  }

  @Test
  public void testFindProductsNoResults() {
    assertFalse(productDao.findProducts().isEmpty());
  }

  @Test
  public void checkIfTheRightProductHasBeenGot() {
    assertEquals("sgs3", productDao.getProduct(2L).get().getCode());
  }

  @Test
  public void checkIfTheProductIsDeleted() {
    productDao.delete(3L);
    assertFalse(productDao.findProducts().stream()
            .anyMatch(product -> product.getId().equals(3L)));
  }

  @Test
  public void checkIfTheProductIsSaved() {
    Product newProduct = new Product("WAS-LX1", "Huawei P10 Lite", new BigDecimal(100), Currency.getInstance("USD"), 1, null);
    productDao.save(newProduct);
    assertNotNull(newProduct.getId());
    assertTrue(productDao.findProducts().stream()
            .anyMatch(product -> "WAS-LX1".equals(product.getCode())));
  }

  @Test
  public void testFindProductsIfStockIsZero() {
    assertEquals(12, productDao.findProducts().size());
  }

  @Test
  public void testFindProductsIfPriceIsNull() {
    Product newProduct = new Product("WAS-LX1", "Huawei P10 Lite", null, Currency.getInstance("USD"), 1, null);
    productDao.save(newProduct);
    assertEquals(12, productDao.findProducts().size());
  }
}
