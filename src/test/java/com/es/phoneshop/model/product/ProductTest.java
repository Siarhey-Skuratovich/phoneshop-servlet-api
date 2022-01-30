package com.es.phoneshop.model.product;

import com.es.phoneshop.web.DemoDataServletContextListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductTest {
  private ProductDao productDao;
  @Mock
  private ServletContextEvent event;
  @Mock
  private ServletContext servletContext;

  private static boolean setUpIsDone = false;

  @Before
  public void setUp() {
    productDao = ArrayListProductDao.getInstance();

    if (setUpIsDone) {
      return;
    }

    DemoDataServletContextListener demoDataServletContextListener = new DemoDataServletContextListener();
    when(event.getServletContext()).thenReturn(servletContext);
    when(servletContext.getInitParameter("insertDemoData")).thenReturn("true");
    demoDataServletContextListener.contextInitialized(event);

    setUpIsDone = true;
  }

  @Test
  public void testMultiplePriceChange() {
    Product product = productDao.getProduct(3L).get();
    List<BigDecimal> expectedPrices = new ArrayList<>();
    expectedPrices.add(product.getPrice());

    BigDecimal price2 = new BigDecimal(190);
    product.setPrice(price2);
    expectedPrices.add(price2);
    productDao.save(product);

    BigDecimal price3 = new BigDecimal(180);
    product.setPrice(price3);
    expectedPrices.add(price3);
    productDao.save(product);

    List<PriceChange> history = productDao.getProduct(3L).get().getPriceChangesHistory();
    assertEquals(3, history.size());
    for (int i = 0; i < history.size(); i++) {
      assertEquals(expectedPrices.get(i), history.get(i).getPrice());
    }
  }
}