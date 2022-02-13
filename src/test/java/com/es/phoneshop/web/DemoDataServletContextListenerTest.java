package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import java.lang.reflect.*;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DemoDataServletContextListenerTest {
  @Mock
  private ServletContextEvent event;
  @Mock
  private ServletContext servletContext;

  private DemoDataServletContextListener demoDataServletContextListener = new DemoDataServletContextListener();

  private ProductDao productDao = ArrayListProductDao.getInstance();

  @Before
  public void setUp() {
    when(event.getServletContext()).thenReturn(servletContext);
  }

  @After
  public void resetProductDao() throws IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InvocationTargetException, InstantiationException {
    Class<ArrayListProductDao> arrayListProductDaoClass = ArrayListProductDao.class;
    Constructor<ArrayListProductDao> arrayListProductDaoClassConstructor = arrayListProductDaoClass.getDeclaredConstructor();
    arrayListProductDaoClassConstructor.setAccessible(true);

    Class<?> instanceHolderClass = arrayListProductDaoClass.getDeclaredClasses()[0];
    Field field = instanceHolderClass.getDeclaredField("instance");
    field.setAccessible(true);

    Field modifiersField = getModifiersField();
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

    field.set(null, arrayListProductDaoClassConstructor.newInstance());
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

  private static Field getModifiersField() throws NoSuchFieldException {
    try {
      return Field.class.getDeclaredField("modifiers");
    } catch (NoSuchFieldException e) {
      try {
        Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
        getDeclaredFields0.setAccessible(true);
        Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
        for (Field field : fields) {
          if ("modifiers".equals(field.getName())) {
            return field;
          }
        }
      } catch (ReflectiveOperationException ex) {
        e.addSuppressed(ex);
      }
      throw e;
    }
  }
}