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

  private ProductDao productDaoTemp;

  @Before
  public void createNewInstanceOfSingletonProductDaoUsingReflection() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException, InstantiationException {
    when(event.getServletContext()).thenReturn(servletContext);

    Class<ArrayListProductDao> arrayListProductDaoClass = ArrayListProductDao.class;
    Constructor<ArrayListProductDao> arrayListProductDaoClassConstructor = arrayListProductDaoClass.getDeclaredConstructor();
    arrayListProductDaoClassConstructor.setAccessible(true);

    Class<?> instanceHolderClass = arrayListProductDaoClass.getDeclaredClasses()[0];
    Field instanceField = instanceHolderClass.getDeclaredField("instance");
    instanceField.setAccessible(true);

    Field modifiersField = getModifiersField();
    modifiersField.setAccessible(true);
    modifiersField.setInt(instanceField, instanceField.getModifiers() & ~Modifier.FINAL);

    productDaoTemp =  arrayListProductDaoClassConstructor.newInstance();

    Class<DemoDataServletContextListener> demoDataServletContextListenerClass = DemoDataServletContextListener.class;
    Field productDaoFieldOfContextListener = demoDataServletContextListenerClass.getDeclaredField("productDao");
    modifiersField.setInt(productDaoFieldOfContextListener, productDaoFieldOfContextListener.getModifiers() & ~Modifier.FINAL);
    productDaoFieldOfContextListener.setAccessible(true);

    productDaoFieldOfContextListener.set(demoDataServletContextListener, productDaoTemp);
  }

  @Test
  public void testIfAttributeInsertDataIsFalse() {
    int oldSize = productDaoTemp.findProducts(null, null, null).size();
    when(servletContext.getInitParameter("insertDemoData")).thenReturn("false");
    demoDataServletContextListener.contextInitialized(event);
    assertEquals(oldSize, productDaoTemp.findProducts(null, null, null).size());
  }

  @Test
  public void testIfAttributeInsertDataIsTrue() {
    int oldSize = productDaoTemp.findProducts(null, null, null).size();
    when(servletContext.getInitParameter("insertDemoData")).thenReturn("true");
    demoDataServletContextListener.contextInitialized(event);
    assertTrue(oldSize < productDaoTemp.findProducts(null, null, null).size());
    assertFalse(productDaoTemp.findProducts(null, null, null).isEmpty());
  }

  @Test
  public void testMultiplePriceChange() {
    when(servletContext.getInitParameter("insertDemoData")).thenReturn("true");
    demoDataServletContextListener.contextInitialized(event);
    List<Product> products = productDaoTemp.findProducts(null, null, null);
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