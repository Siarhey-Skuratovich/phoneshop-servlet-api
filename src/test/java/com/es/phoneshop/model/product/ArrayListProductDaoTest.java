package com.es.phoneshop.model.product;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import com.es.phoneshop.web.DemoDataServletContextListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class ArrayListProductDaoTest {
  private ProductDao productDao;
  @Mock
  private ServletContextEvent event;
  @Mock
  private ServletContext servletContext;

  @Before
  public void setup() {
    productDao = ArrayListProductDao.getInstance();

    boolean productArrayIsNotEmpty = !productDao.findProducts(null, null, null).isEmpty();
    if (productArrayIsNotEmpty) {
      return;
    }

    DemoDataServletContextListener demoDataServletContextListener = new DemoDataServletContextListener();
    when(event.getServletContext()).thenReturn(servletContext);
    when(servletContext.getInitParameter("insertDemoData")).thenReturn("true");
    demoDataServletContextListener.contextInitialized(event);
  }

  @Test
  public void testFindProductsNoResults() {
    assertFalse(productDao.findProducts(null, null, null).isEmpty());
    assertFalse(productDao.findProducts("", null, null).isEmpty());
  }

  @Test
  public void checkIfTheRightProductHasBeenGot() {
    assertEquals("sgs3", productDao.get(2L).get().getCode());
  }

  @Test
  public void checkIfTheProductIsDeleted() {
    productDao.delete(4L);
    assertFalse(productDao.get(4L).isPresent());
  }

  @Test
  public void checkIfTheProductIsSaved() {
    Product newProduct = new Product("WAS-LX1", "Huawei P10 Lite", new BigDecimal(100), Currency.getInstance("USD"), 1, null);
    productDao.save(newProduct);
    assertNotNull(newProduct.getId());

    Optional<Product> productOptional = productDao.get(newProduct.getId());
    assertTrue(productOptional.isPresent());
    assertEquals("WAS-LX1", productOptional.get().getCode());
  }

  @Test
  public void testProductUpdate() {
    long productId = 3L;
    Product oldProduct = productDao.get(productId).get();
    Product newProduct = new Product("WAS-LX1", "Huawei P10 Lite", new BigDecimal(100), Currency.getInstance("USD"), 1, null);
    newProduct.setId(productId);
    productDao.save(newProduct);
    assertEquals("WAS-LX1", productDao.get(3L).get().getCode());
    productDao.save(oldProduct);
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
    productDao.delete(newProduct.getId());
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
    assertFalse(productDao.get(null).isPresent());
  }

  @Test
  public void testSortingByAscDescription() {
    List<Product> sortedList = productDao.findProducts(null, SortField.description, SortOrder.asc);
    for (int i = 1; i < sortedList.size(); i++) {
      assertTrue(sortedList.get(i - 1).getDescription()
              .compareTo(sortedList.get(i).getDescription()) <= 0);
    }
  }

  @Test
  public void testSortingByDescDescription() {
    List<Product> sortedList = productDao.findProducts(null, SortField.description, SortOrder.desc);
    for (int i = 1; i < sortedList.size(); i++) {
      assertTrue(sortedList.get(i - 1).getDescription()
              .compareTo(sortedList.get(i).getDescription()) >= 0);
    }
  }

  @Test
  public void testSortingByAscPrice() {
    List<Product> sortedList = productDao.findProducts(null, SortField.price, SortOrder.asc);
    for (int i = 1; i < sortedList.size(); i++) {
      assertTrue(sortedList.get(i - 1).getPrice().compareTo(sortedList.get(i).getPrice()) <= 0);
    }
  }

  @Test
  public void testSortingByDescPrice() {
    List<Product> sortedList = productDao.findProducts(null, SortField.price, SortOrder.desc);
    for (int i = 1; i < sortedList.size(); i++) {
      assertTrue(sortedList.get(i - 1).getPrice().compareTo(sortedList.get(i).getPrice()) >= 0);
    }
  }

  @Test
  public void testFindProductsByAdvancedSearch() {
    assertTrue(productDao.findProductsByAdvancedSearch("sgs",
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(1000),
            5)
            .stream()
            .allMatch(product -> product.getCode().equals("sgs")
                    && product.getPrice().intValue() >= 100
                    && product.getPrice().intValue() <= 1000
                    && product.getStock() >= 5));
  }

  @Test
  public void testFindProductsByAdvancedSearchWithEmptyProductCode() {
    assertTrue(productDao.findProductsByAdvancedSearch(null,
            BigDecimal.valueOf(300),
            BigDecimal.valueOf(1000),
            5)
            .stream()
            .allMatch(product -> product.getPrice().intValue() >= 300
                    && product.getPrice().intValue() <= 1000
                    && product.getStock() >= 5));
  }

  @Test
  public void testFindProductsByAdvancedSearchWithNullMinValue() {
    assertTrue(productDao.findProductsByAdvancedSearch(null,
            null,
            BigDecimal.valueOf(1000),
            5)
            .stream()
            .allMatch(product -> product.getPrice().intValue() <= 1000
                    && product.getStock() >= 5));
  }

  @Test
  public void testFindProductsByAdvancedSearchWithNullMaxValue() {
    assertTrue(productDao.findProductsByAdvancedSearch(null,
            BigDecimal.valueOf(300),
            null,
            5)
            .stream()
            .allMatch(product -> product.getPrice().intValue() >= 300
                    && product.getStock() >= 5));
  }

  @Test
  public void testFindProductsByAdvancedSearchWithNullMinStock() {
    assertTrue(productDao.findProductsByAdvancedSearch(null,
            BigDecimal.valueOf(300),
            BigDecimal.valueOf(1000),
            null)
            .stream()
            .allMatch(product -> product.getPrice().intValue() >= 300
                    && product.getPrice().intValue() <= 1000));
  }
}
