package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductDao;
import com.es.phoneshop.model.product.cart.CartService;
import com.es.phoneshop.model.product.cart.DefaultCartService;

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

public class ProductDetailsPageServlet extends HttpServlet {
  private static final String RECENTLY_VIEWED_PRODUCTS_ATTRIBUTE = "recentlyViewedProducts";
  private ProductDao productDao;
  private CartService cartService;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    productDao = ArrayListProductDao.getInstance();
    cartService = DefaultCartService.getInstance();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String productId = extractProductId(request);
    if (isNotADigit(productId)) {
      forwardToProductNotFoundPage(request, response, productId);
      return;
    }
    Optional<Product> optionalProduct = productDao.getProduct(Long.parseLong(productId));
    if (!optionalProduct.isPresent()) {
      forwardToProductNotFoundPage(request, response, productId);
      return;
    }
    request.setAttribute("product", optionalProduct.get());
    request.setAttribute("cart", cartService.getCart(request));
    putProductToRecentlyViewedBlock(optionalProduct.get(), request.getSession());
    request.getRequestDispatcher("/WEB-INF/pages/product.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String productIdString = extractProductId(request);
    if (isNotADigit(productIdString)) {
      forwardToProductNotFoundPage(request, response, productIdString);
      return;
    }
    long productId = Long.parseLong(productIdString);

    Optional<Product> productOptional = productDao.getProduct(productId);
    if (!productOptional.isPresent()) {
      forwardToProductNotFoundPage(request, response, productIdString);
      return;
    }
    Product product = productOptional.get();

    String quantityString = request.getParameter("quantity");
    int quantity;
    try {
      quantity = parseQuantityAccordingToLocale(request.getLocale(), quantityString);
    } catch (ParseException e) {
      request.setAttribute("error", "Not a number");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      doGet(request, response);
      return;
    }

    if (quantity > product.getStock()) {
      request.setAttribute("error", "Out of stock. Available: " + product.getStock());
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      doGet(request, response);
      return;
    }

    cartService.add(cartService.getCart(request), productId, quantity, request.getSession());
    response.sendRedirect(request.getContextPath() + "/products/" + productId + "?message=Product added to cart");
  }

  private String extractProductId(HttpServletRequest request) {
    return request.getPathInfo().substring(1);
  }

  private boolean isNotADigit(String string) {
    return string.matches("\\D+");
  }

  private int parseQuantityAccordingToLocale(Locale locale, String quantityString) throws ParseException {
    NumberFormat format = NumberFormat.getInstance(locale);
    return format.parse(quantityString).intValue();
  }

  private void forwardToProductNotFoundPage(HttpServletRequest request,
                                            HttpServletResponse response,
                                            String productIdString) throws ServletException, IOException {
    request.setAttribute("productId", productIdString);
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    request.getRequestDispatcher("/WEB-INF/pages/errorProductNotFound.jsp").forward(request, response);
  }

  private void putProductToRecentlyViewedBlock(Product product, HttpSession session) {
    Deque<Product> recentlyViewedProducts = (Deque<Product>) session.getAttribute(RECENTLY_VIEWED_PRODUCTS_ATTRIBUTE);
    if (recentlyViewedProducts == null) {
      recentlyViewedProducts = new ArrayDeque<>(3);
      session.setAttribute(RECENTLY_VIEWED_PRODUCTS_ATTRIBUTE, recentlyViewedProducts);
    }

    removeDuplicatesInRecentlyViewedBlock(recentlyViewedProducts, product);

    recentlyViewedProducts.addFirst(product);
    if (recentlyViewedProducts.size() == 4) {
      recentlyViewedProducts.pollLast();
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