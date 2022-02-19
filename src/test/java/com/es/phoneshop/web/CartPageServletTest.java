package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductDao;
import com.es.phoneshop.model.cart.DefaultCartService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CartPageServletTest {
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private RequestDispatcher requestDispatcher;
  @Mock
  private ServletConfig config;
  @Mock
  private HttpSession session;
  @Mock
  private ServletContextEvent event;
  @Mock
  private ServletContext servletContext;

  private ProductDao productDao;

  private final CartPageServlet servlet = new CartPageServlet();

  @Before
  public void setUp() throws ServletException {
    productDao = ArrayListProductDao.getInstance();
    boolean productArrayIsEmpty = productDao.findProducts(null, null, null).isEmpty();

    if (productArrayIsEmpty) {
      DemoDataServletContextListener demoDataServletContextListener = new DemoDataServletContextListener();
      when(event.getServletContext()).thenReturn(servletContext);
      when(servletContext.getInitParameter("insertDemoData")).thenReturn("true");
      demoDataServletContextListener.contextInitialized(event);
    }
    servlet.init(config);
    when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute(DefaultCartService.class.getName() + ".lock")).thenReturn(new ReentrantLock());
    when(request.getLocale()).thenReturn(Locale.ENGLISH);
  }

  @Test
  public void testDoGet() throws ServletException, IOException {
    servlet.doGet(request, response);

    verify(request).setAttribute(eq("cart"), any());
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/cart.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoPostWithInvalidId() throws IOException, ServletException {
    String productIdString = "asd";
    when(request.getParameterValues("productId")).thenReturn(new String[]{productIdString});
    when(request.getParameterValues("quantity")).thenReturn(new String[]{"3"});
    servlet.doPost(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/product-not-found"
            + "?productId="
            + productIdString);
  }

  @Test
  public void testDoPostWithNotExistingId() throws IOException, ServletException {
    String productIdString = "115";
    when(request.getParameterValues("productId")).thenReturn(new String[]{productIdString});
    when(request.getParameterValues("quantity")).thenReturn(new String[]{"3"});
    servlet.doPost(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/product-not-found"
            + "?productId="
            + productIdString);
  }

  @Test
  public void testDoPostWithInvalidQuantity() throws IOException, ServletException {
    String productIdString = "7";
    when(request.getParameterValues("productId")).thenReturn(new String[]{productIdString});
    when(request.getParameterValues("quantity")).thenReturn(new String[]{"asd"});
    servlet.doPost(request, response);

    verify(request).setAttribute(eq("validationErrors"), any());
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/cart.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoPostWithQuantityMoreThanStock() throws IOException, ServletException {
    long productId = 7L;
    when(request.getParameterValues("productId")).thenReturn(new String[]{String.valueOf(productId)});
    when(request.getParameterValues("quantity")).thenReturn(new String[]{"1000"});
    servlet.doPost(request, response);

    verify(request).setAttribute(eq("validationErrors"), any());
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/cart.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoPostWithValidAndExistingValues() throws IOException, ServletException {
    long productId = 7L;
    when(request.getParameterValues("productId")).thenReturn(new String[]{String.valueOf(productId)});
    when(request.getParameterValues("quantity")).thenReturn(new String[]{"3"});
    servlet.doPost(request, response);

    verify(response).sendRedirect(eq(request.getContextPath() + "/cart" + "?successMessage=Products updated successfully"));
  }

  @Test
  public void testDoPostWithQuantityConveyedAccordingToEnglishLocale() throws IOException, ServletException {
    Product newProduct = new Product("WAS-LX1",
            "Huawei P10 Lite",
            new BigDecimal(100),
            Currency.getInstance("USD"),
            10000,
            null);
    productDao.save(newProduct);
    when(request.getParameterValues("productId")).thenReturn(new String[]{String.valueOf(newProduct.getId())});
    when(request.getParameterValues("quantity")).thenReturn(new String[]{"1,0"});
    servlet.doPost(request, response);

    verify(response).sendRedirect(eq(request.getContextPath() + "/cart" + "?successMessage=Products updated successfully"));
  }

  @Test
  public void testDoPostIfQuantityLessThan0() throws IOException, ServletException {
    long productId = 6L;
    when(request.getParameterValues("productId")).thenReturn(new String[]{String.valueOf(productId)});
    when(request.getParameterValues("quantity")).thenReturn(new String[]{"-3"});

    servlet.doPost(request, response);

    verify(request).setAttribute(eq("validationErrors"), any());
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/cart.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoPostIfQuantitiesArrayIsNull() throws IOException, ServletException {
    long productId = 6L;
    when(request.getParameterValues("productId")).thenReturn(new String[]{String.valueOf(productId)});
    when(request.getParameterValues("quantity")).thenReturn(null);
    servlet.doPost(request, response);

    verify(response).sendRedirect(eq(request.getContextPath()
            + "/cart?UrlParamError="
            + "Missing any required quantity in URL params"));
  }

  @Test
  public void testDoPostIfLengthOfQuantitiesArraysNotEqualsProductIdsArrayLength() throws IOException, ServletException {
    long productId = 6L;
    when(request.getParameterValues("productId")).thenReturn(new String[]{String.valueOf(productId)});
    when(request.getParameterValues("quantity")).thenReturn(new String[]{"3", "4"});
    servlet.doPost(request, response);

    verify(response).sendRedirect(eq(request.getContextPath()
            + "/cart?UrlParamError="
            + "ProductIds don't match their required quantities in URL params"));
  }

  @Test
  public void testDoPostIfProductIdsArrayIsNull() throws IOException, ServletException {
    when(request.getParameterValues("productId")).thenReturn(null);
    when(request.getParameterValues("quantity")).thenReturn(new String[]{"3", "4"});
    servlet.doPost(request, response);

    verify(response).sendRedirect(eq(request.getContextPath()
            + "/cart?UrlParamError="
            + "Missing any required productId in URL params"));
  }



}