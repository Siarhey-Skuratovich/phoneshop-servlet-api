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
import java.util.ArrayDeque;
import java.util.Currency;
import java.util.Deque;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductDetailsPageServletTest {

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

  private final ProductDetailsPageServlet servlet = new ProductDetailsPageServlet();

  private final Cart cart = new Cart();


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
  public void testDoGetWithExistingId() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/3");
    servlet.doGet(request, response);

    verify(request).setAttribute(eq("product"), any());
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/product.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoGetWithNotExistingId() throws ServletException, IOException {
    long productId = 54L;
    when(request.getPathInfo()).thenReturn("/" + productId);
    servlet.doGet(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/product-not-found"
            + "?productId="
            + productId);
  }

  @Test
  public void testDoGetWithInvalidId() throws ServletException, IOException {
    String productId = "asd";
    when(request.getPathInfo()).thenReturn("/" + productId);
    servlet.doGet(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/product-not-found"
            + "?productId="
            + productId);
  }

  @Test
  public void testDoPostWithInvalidId() throws IOException {
    String productId = "asd";
    when(request.getPathInfo()).thenReturn("/" + productId);
    servlet.doPost(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/product-not-found"
            + "?productId="
            + productId);
  }

  @Test
  public void testDoPostWithNotExistingId() throws IOException {
    long productId = 46L;
    when(request.getPathInfo()).thenReturn("/" + productId);
    servlet.doPost(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/product-not-found"
            + "?productId="
            + productId);
    assertFalse(cartService.getCart(request).getCartItemByProductId(46L).isPresent());
  }

  @Test
  public void testDoPostWithInvalidQuantity() throws IOException {
    long productId = 6L;
    String quantityString = "asd";
    when(request.getPathInfo()).thenReturn("/" + productId);
    when(request.getParameter("quantity")).thenReturn(quantityString);
    servlet.doPost(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/products/"
            + productId
            + "?quantity=" + quantityString + "&"
            + "error=Not a number");
    assertFalse(cartService.getCart(request).getCartItemByProductId(productId).isPresent());
  }

  @Test
  public void testDoPostWithQuantityMoreThanStock() throws IOException {
    long productId = 6L;
    String quantityString = "1000";
    Product product = productDao.get(productId).get();
    when(request.getPathInfo()).thenReturn("/" + productId);
    when(request.getParameter("quantity")).thenReturn(quantityString);
    servlet.doPost(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/products/"
            + productId
            + "?quantity=" + quantityString + "&"
            + "error=Out of stock. Max Available:"
            + (product.getStock()));
    assertFalse(cartService.getCart(request).getCartItemByProductId(productId).isPresent());
  }

  @Test
  public void testDoPostWithValidAndExistingValues() throws IOException {
    when(request.getPathInfo()).thenReturn("/5");
    when(request.getParameter("quantity")).thenReturn("3");
    servlet.doPost(request, response);

    verify(response).sendRedirect(any());
    assertTrue(cartService.getCart(request).getCartItemByProductId(5L).isPresent());
  }

  @Test
  public void testDoPostWithQuantityConveyedAccordingToEnglishLocale() throws IOException {
    Product newProduct = new Product("WAS-LX1",
            "Huawei P10 Lite",
            new BigDecimal(100),
            Currency.getInstance("USD"),
            10000,
            null);
    productDao.save(newProduct);
    when(request.getPathInfo()).thenReturn("/" + newProduct.getId());
    when(request.getParameter("quantity")).thenReturn("1,000");
    servlet.doPost(request, response);

    verify(response).sendRedirect(any());
    assertTrue(cartService.getCart(request).getCartItemByProductId(newProduct.getId()).isPresent());
  }

  @Test
  public void testDoGetAddingProductToRecentlyViewedBlock() throws ServletException, IOException {
    long productId = 6L;
    when(request.getPathInfo()).thenReturn("/" + productId);
    Deque<Product> recentlyViewedProducts = new ArrayDeque<>(3);
    when(session.getAttribute("recentlyViewedProducts")).thenReturn(recentlyViewedProducts);
    servlet.doGet(request, response);

    assertEquals(1, recentlyViewedProducts.size());
    assertTrue(recentlyViewedProducts.stream().anyMatch(product -> product.getId().equals(productId)));
    assertEquals(productId, recentlyViewedProducts.getFirst().getId().longValue());
  }

  @Test
  public void testDoGetRemovingDuplicatesInRecentlyViewedBlock() throws ServletException, IOException {
    long productId = 6L;
    when(request.getPathInfo()).thenReturn("/" + productId);
    Deque<Product> recentlyViewedProducts = new ArrayDeque<>(3);
    when(session.getAttribute("recentlyViewedProducts")).thenReturn(recentlyViewedProducts);
    servlet.doGet(request, response);
    servlet.doGet(request, response);

    assertEquals(1, recentlyViewedProducts.size());
    assertTrue(recentlyViewedProducts.stream().anyMatch(product -> product.getId().equals(productId)));
    assertEquals(productId, recentlyViewedProducts.getFirst().getId().longValue());
  }

  @Test
  public void testDoGetRecentlyViewedBlockMustBeLessThan4() throws ServletException, IOException {
    Deque<Product> recentlyViewedProducts = new ArrayDeque<>(3);
    when(session.getAttribute("recentlyViewedProducts")).thenReturn(recentlyViewedProducts);
    for (long i = 2L; i < 7L; i++) {
      when(request.getPathInfo()).thenReturn("/" + i);
      servlet.doGet(request, response);
    }

    assertEquals(3, recentlyViewedProducts.size());
  }

  @Test
  public void testDoPostIfQuantitySumInCartWillBeMoreThanStock() throws IOException, QuantitySumInCartWillBeMoreThanStockException, OutOfStockException {
    long productId = 6L;
    Product product = productDao.get(productId).get();
    when(request.getPathInfo()).thenReturn("/" + productId);

    cartService.add(cart, productId, 3, session);

    when(request.getParameter("quantity")).thenReturn("30");
    servlet.doPost(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/products/"
            + productId
            + "?error=Out of stock. "
            + (product.getStock() - cartService.getCart(request).getCartItemByProductId(productId).get().getQuantity())
            + " more available.");
  }

  @Test
  public void testDoPostIfQuantityLessThan0() throws IOException {
    long productId = 6L;
    String quantityString = "-3";
    when(request.getPathInfo()).thenReturn("/" + productId);
    when(request.getParameter("quantity")).thenReturn(quantityString);
    servlet.doPost(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/products/"
            + productId
            + "?quantity=" + quantityString + "&"
            + "error=Quantity must be more than 0");
  }

  @Test
  public void testDoPostIfQuantityIsNull() throws IOException {
    long productId = 6L;
    when(request.getPathInfo()).thenReturn("/" + productId);
    when(request.getParameter("quantity")).thenReturn(null);
    servlet.doPost(request, response);

    verify(response).sendRedirect(request.getContextPath()
            + "/products/"
            + productId
            + "?error=You haven't specified a quantity");
  }
}