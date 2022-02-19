package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductDao;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartService;
import com.es.phoneshop.model.cart.DefaultCartService;
import com.es.phoneshop.model.cart.exception.OutOfStockException;
import com.es.phoneshop.model.cart.exception.QuantitySumInCartWillBeMoreThanStockException;
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
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

@RunWith(MockitoJUnitRunner.class)
public class ProductListPageServletTest {

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private RequestDispatcher requestDispatcher;
  @Mock
  private ServletConfig config;
  @Mock
  private ServletContextEvent event;
  @Mock
  private ServletContext servletContext;
  @Mock
  private HttpSession session;

  private ProductDao productDao;

  private final CartService cartService = DefaultCartService.getInstance();

  private final Cart cart = new Cart();

  private final ProductListPageServlet servlet = new ProductListPageServlet();

  @Before
  public void setup() throws ServletException {
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
    when(session.getAttribute(DefaultCartService.class.getName() + ".cart")).thenReturn(cart);
    when(request.getLocale()).thenReturn(Locale.ENGLISH);
  }

  @Test
  public void testDoGet() throws ServletException, IOException {
    servlet.doGet(request, response);

    verify(request).setAttribute(eq("products"), any());
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/productList.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoPostWithInvalidId() throws IOException, ServletException {
    String productId = "asd";
    when(request.getParameter("productId")).thenReturn(productId);
    servlet.doPost(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/product-not-found"
            + "?productId="
            + productId);
  }

  @Test
  public void testDoPostWithNotExistingId() throws IOException, ServletException {
    long productId = 46L;
    when(request.getParameter("productId")).thenReturn(String.valueOf(productId));
    servlet.doPost(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/product-not-found"
            + "?productId="
            + productId);
    assertFalse(cartService.getCart(request).getCartItemByProductId(productId).isPresent());
  }

  @Test
  public void testDoPostWithInvalidQuantity() throws IOException, ServletException {
    long productId = 6L;
    String quantityString = "asd";
    when(request.getParameter("productId")).thenReturn(String.valueOf(productId));
    when(request.getParameter("quantity")).thenReturn(quantityString);
    servlet.doPost(request, response);

    verify(request).setAttribute("error", "Not a number");
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/productList.jsp"));
    verify(requestDispatcher).forward(request, response);
    assertFalse(cartService.getCart(request).getCartItemByProductId(productId).isPresent());
  }

  @Test
  public void testDoPostWithQuantityMoreThanStock() throws IOException, ServletException {
    long productId = 6L;
    String quantityString = "1000";
    when(request.getParameter("productId")).thenReturn(String.valueOf(productId));
    when(request.getParameter("quantity")).thenReturn(quantityString);
    servlet.doPost(request, response);

    verify(request).setAttribute("error","Out of stock. Max Available:" + productDao.get(productId).get().getStock());
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/productList.jsp"));
    verify(requestDispatcher).forward(request, response);
    assertFalse(cartService.getCart(request).getCartItemByProductId(productId).isPresent());
  }

  @Test
  public void testDoPostWithValidAndExistingValues() throws IOException, ServletException {
    long productId = 5L;
    String quantityString = "3";
    when(request.getParameter("productId")).thenReturn(String.valueOf(productId));
    when(request.getParameter("quantity")).thenReturn(quantityString);
    servlet.doPost(request, response);

    verify(response).sendRedirect(any());
    assertTrue(cartService.getCart(request).getCartItemByProductId(productId).isPresent());
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
    when(request.getParameter("productId")).thenReturn(String.valueOf(newProduct.getId()));
    when(request.getParameter("quantity")).thenReturn("1000");
    servlet.doPost(request, response);

    verify(response).sendRedirect(any());
    assertTrue(cartService.getCart(request).getCartItemByProductId(newProduct.getId()).isPresent());
  }

  @Test
  public void testDoPostIfQuantitySumInCartWillBeMoreThanStock() throws IOException, QuantitySumInCartWillBeMoreThanStockException, ServletException, OutOfStockException {
    long productId = 6L;
    Product product = productDao.get(productId).get();
    when(request.getParameter("productId")).thenReturn(String.valueOf(productId));


    cartService.add(cart, productId, 3, session);

    when(request.getParameter("quantity")).thenReturn("30");
    servlet.doPost(request, response);

    verify(request).setAttribute("error","Out of stock. "
            + (product.getStock() - 3)
            + " more available.");
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/productList.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoPostIfQuantityLessThan0() throws IOException, ServletException {
    long productId = 6L;
    String quantityString = "-3";
    when(request.getParameter("productId")).thenReturn(String.valueOf(productId));
    when(request.getParameter("quantity")).thenReturn(quantityString);
    servlet.doPost(request, response);

    verify(request).setAttribute("error", "Quantity must be more than 0");
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/productList.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoPostIfQuantityIsNull() throws IOException, ServletException {
    long productId = 6L;
    when(request.getParameter("productId")).thenReturn(String.valueOf(productId));
    when(request.getParameter("quantity")).thenReturn(null);
    servlet.doPost(request, response);

    verify(request).setAttribute("error", "You haven't specified a quantity");
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/productList.jsp"));
  }

  @Test
  public void testDoPostForSortingParamsIfAddedSuccessfully() throws IOException, ServletException {
    long productId = 9L;
    String quantityString = "3";
    when(request.getParameter("productId")).thenReturn(String.valueOf(productId));
    when(request.getParameter("quantity")).thenReturn(quantityString);

    when(request.getRequestURI()).thenReturn("/phoneshop-servlet-api/products");
    when(request.getQueryString()).thenReturn("sort=description&order=asc&query=");
    servlet.doPost(request, response);

    verify(response).sendRedirect("/phoneshop-servlet-api/products?" +
            "sort=description&order=asc&query=&successMessage=Product added to cart");
    assertTrue(cartService.getCart(request).getCartItemByProductId(productId).isPresent());
  }
}