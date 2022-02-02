package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DemoDataServletContextListenerTest {
  @Mock
  private ServletContextEvent event;
  @Mock
  private ServletContext servletContext;

  private final DemoDataServletContextListener demoDataServletContextListener = new DemoDataServletContextListener();

  private final ProductDao productDao = ArrayListProductDao.getInstance();

  @Before
  public void setUp() {
    when(event.getServletContext()).thenReturn(servletContext);
  }

  @Test
  public void testIfAttributeInsertDataIsFalse() {
    int oldSize = productDao.findProducts(null, null, null).size();
    when(servletContext.getInitParameter("insertDemoData")).thenReturn("false");
    demoDataServletContextListener.contextInitialized(event);
    assertEquals(oldSize, productDao.findProducts(null, null, null).size());
  }

  @Test
  public void testIfAttributeInsertDataIsTrue() {
    int oldSize = productDao.findProducts(null, null, null).size();
    when(servletContext.getInitParameter("insertDemoData")).thenReturn("true");
    demoDataServletContextListener.contextInitialized(event);
    assertTrue(oldSize < productDao.findProducts(null, null, null).size());
    assertFalse(productDao.findProducts(null, null, null).isEmpty());
  }

  @Test
  public void testMultiplePriceChange() {
    when(servletContext.getInitParameter("insertDemoData")).thenReturn("true");
    demoDataServletContextListener.contextInitialized(event);
    List<Product> products = productDao.findProducts(null, null, null);
    assertTrue(products.stream()
            .filter(product -> product.getPriceChangesHistory().size() == 3).count() >= 12);
  }
}