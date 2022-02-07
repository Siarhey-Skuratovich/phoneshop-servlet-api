package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductDao;
import com.es.phoneshop.model.product.cart.Cart;
import com.es.phoneshop.model.product.cart.CartService;
import com.es.phoneshop.model.product.cart.DefaultCartService;
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
    when(session.getId()).thenReturn("123");
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
    when(request.getPathInfo()).thenReturn("/54");
    servlet.doGet(request, response);

    verify(request).setAttribute(eq("productId"), any());
    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/errorProductNotFound.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoGetWithInvalidId() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/asd");
    servlet.doGet(request, response);

    verify(request).setAttribute(eq("productId"), any());
    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/errorProductNotFound.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoPostWithInvalidId() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/asd");
    servlet.doPost(request, response);

    verify(request).setAttribute(eq("productId"), any());
    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/errorProductNotFound.jsp"));
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDoPostWithNotExistingId() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/46");
    servlet.doPost(request, response);

    verify(request).setAttribute(eq("productId"), any());
    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    verify(request).getRequestDispatcher(eq("/WEB-INF/pages/errorProductNotFound.jsp"));
    verify(requestDispatcher).forward(request, response);
    assertFalse(cartService.getCart(request).getCartItemByProductId(46L).isPresent());
  }

  @Test
  public void testDoPostWithInvalidQuantity() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/6");
    when(request.getParameter("quantity")).thenReturn("asd");
    servlet.doPost(request, response);

    verify(request).setAttribute(eq("error"), eq("Not a number"));
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    assertFalse(cartService.getCart(request).getCartItemByProductId(6L).isPresent());
  }

  @Test
  public void testDoPostWithQuantityMoreThanStock() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/6");
    when(request.getParameter("quantity")).thenReturn("1000");
    servlet.doPost(request, response);

    verify(request).setAttribute(eq("error"), any());
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    assertFalse(cartService.getCart(request).getCartItemByProductId(6L).isPresent());
  }

  @Test
  public void testDoPostWithValidAndExistingValues() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/5");
    when(request.getParameter("quantity")).thenReturn("3");
    servlet.doPost(request, response);

    verify(response).sendRedirect(any());
    assertTrue(cartService.getCart(request).getCartItemByProductId(5L).isPresent());
  }

  @Test
  public void testDoPostWithQuantityConveyedAccordingToEnglishLocale() throws ServletException, IOException {
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
}