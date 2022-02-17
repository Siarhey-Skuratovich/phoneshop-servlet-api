package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductDao;
import com.es.phoneshop.model.product.cart.CartService;
import com.es.phoneshop.model.product.cart.DefaultCartService;
import com.es.phoneshop.model.product.cart.exception.OutOfStockException;
import com.es.phoneshop.model.product.cart.exception.QuantitySumInCartWillBeMoreThanStockException;
import com.es.phoneshop.util.lock.DefaultSessionLockManager;
import com.es.phoneshop.util.lock.SessionLockManager;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Lock;

public class ProductDetailsPageServlet extends HttpServlet {
  private static final String RECENTLY_VIEWED_PRODUCTS_ATTRIBUTE = "recentlyViewedProducts";
  private static final String LOCK_SESSION_ATTRIBUTE = ProductDetailsPageServlet.class.getName() + ".lock";
  private ProductDao productDao;
  private CartService cartService;
  private final SessionLockManager sessionLockManager = new DefaultSessionLockManager();

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    productDao = ArrayListProductDao.getInstance();
    cartService = DefaultCartService.getInstance();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String productIdString = extractProductId(request);
    if (isNotADigit(productIdString)) {
      redirectToProductNotFoundPage(request, response, productIdString);
      return;
    }
    Optional<Product> optionalProduct = productDao.getProduct(Long.parseLong(productIdString));
    if (!optionalProduct.isPresent()) {
      redirectToProductNotFoundPage(request, response, productIdString);
      return;
    }
    request.setAttribute("product", optionalProduct.get());
    request.setAttribute("cart", cartService.getCart(request));
    putProductToRecentlyViewedBlock(optionalProduct.get(), request.getSession());
    request.getRequestDispatcher("/WEB-INF/pages/product.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String productIdString = extractProductId(request);
    if (isNotADigit(productIdString)) {
      redirectToProductNotFoundPage(request, response, productIdString);
      return;
    }
    long productId = Long.parseLong(productIdString);

    Optional<Product> productOptional = productDao.getProduct(productId);
    if (!productOptional.isPresent()) {
      redirectToProductNotFoundPage(request, response, productIdString);
      return;
    }
    Product product = productOptional.get();

    String quantityString = request.getParameter("quantity");
    if (quantityString == null) {
      response.sendRedirect(request.getContextPath()
              + "/products/"
              + productId
              + "?error=You haven't specified a quantity");
      return;
    }

    int quantity;
    try {
      quantity = parseQuantityAccordingToLocale(request.getLocale(), quantityString);
    } catch (ParseException e) {
      response.sendRedirect(request.getContextPath()
              + "/products/"
              + productId
              + "?quantity=" + quantityString + "&"
              + "error=Not a number");
      return;
    }

    if (quantity <= 0) {
      response.sendRedirect(request.getContextPath()
              + "/products/"
              + productId
              + "?quantity=" + quantityString + "&"
              + "error=Quantity must be more than 0");
      return;
    }

    try {
      cartService.add(cartService.getCart(request), productId, quantity, request.getSession());
    } catch (OutOfStockException e) {
      response.sendRedirect(request.getContextPath()
              + "/products/"
              + productId
              + "?quantity=" + quantityString + "&"
              + "error=Out of stock. Max Available:"
              + (product.getStock()));
      return;
    } catch (QuantitySumInCartWillBeMoreThanStockException e) {
      response.sendRedirect(request.getContextPath()
              + "/products/"
              + productId
              + "?error=Out of stock. "
              + (product.getStock() - e.getCurrentCartItemQuantity())
              + " more available.");
      return;
    }

    response.sendRedirect(request.getContextPath() + "/products/" + productId + "?message=Product added to cart");
  }

  private String extractProductId(HttpServletRequest request) {
    return request.getPathInfo().substring(1);
  }

  private boolean isNotADigit(String string) {
    return !string.matches("\\d+");
  }

  private void redirectToProductNotFoundPage(HttpServletRequest request,
                                             HttpServletResponse response,
                                             String productIdString) throws IOException {
    response.sendRedirect(request.getContextPath()
            + "/product-not-found"
            + "?productId="
            + productIdString);
  }

  private int parseQuantityAccordingToLocale(Locale locale, String quantityString) throws ParseException {
    NumberFormat format = NumberFormat.getInstance(locale);
    return format.parse(quantityString).intValue();
  }

  private void putProductToRecentlyViewedBlock(Product product, HttpSession session) {
    Lock sessionLock = sessionLockManager.getSessionLock(session, LOCK_SESSION_ATTRIBUTE);
    sessionLock.lock();
    try {
      Deque<Product> recentlyViewedProducts = (Deque<Product>) session.getAttribute(RECENTLY_VIEWED_PRODUCTS_ATTRIBUTE);
      if (recentlyViewedProducts == null) {
        recentlyViewedProducts = new ConcurrentLinkedDeque<>();
        session.setAttribute(RECENTLY_VIEWED_PRODUCTS_ATTRIBUTE, recentlyViewedProducts);
      }

      removeDuplicatesInRecentlyViewedBlock(recentlyViewedProducts, product);

      recentlyViewedProducts.addFirst(product);
      if (recentlyViewedProducts.size() == 4) {
        recentlyViewedProducts.pollLast();
      }
    } finally {
      sessionLock.unlock();
    }
  }

  private void removeDuplicatesInRecentlyViewedBlock(Deque<Product> recentlyViewedProducts, Product product) {
    Iterator<Product> productIterator = recentlyViewedProducts.iterator();
    while (productIterator.hasNext()) {
      if (productIterator.next().getId().equals(product.getId())) {
        productIterator.remove();
        break;
      }
    }
  }
}
