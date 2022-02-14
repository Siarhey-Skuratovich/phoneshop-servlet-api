package com.es.phoneshop.web;

import com.es.phoneshop.model.product.*;
import com.es.phoneshop.model.product.cart.CartService;
import com.es.phoneshop.model.product.cart.DefaultCartService;
import com.es.phoneshop.model.product.cart.exception.QuantitySumInCartWillBeMoreThanStockException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class ProductListPageServlet extends HttpServlet {
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
    String query = request.getParameter("query");
    String sortField = request.getParameter("sort");
    String sortOrder = request.getParameter("order");
    try {
      request.setAttribute("products", productDao.findProducts(query,
              Optional.ofNullable(sortField).map(SortField::valueOf).orElse(null),
              Optional.ofNullable(sortOrder).map(SortOrder::valueOf).orElse(null)));
    } catch (IllegalArgumentException e) {
      request.setAttribute("products", productDao.findProducts(query, null, null));
    }
    request.getRequestDispatcher("/WEB-INF/pages/productList.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String[] productIds = request.getParameterValues("productId");
    String productIdString = productIds[productIds.length - 1];

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

    String[] quantities = request.getParameterValues("quantity");
    String quantityString = quantities[quantities.length - 1];
    if (quantityString == null) {
      response.sendRedirect(prepareRedirectURL(request, "&error=You haven't specified a quantity"));
      return;
    }

    int quantity;
    try {
      quantity = parseQuantityAccordingToLocale(request.getLocale(), quantityString);
    } catch (ParseException e) {
      response.sendRedirect(prepareRedirectURL(request, "&error=Not a number"));
      return;
    }

    if (quantity <= 0) {
      response.sendRedirect(prepareRedirectURL(request, "&error=Quantity must be more than 0"));
      return;
    }

    if (quantity > product.getStock()) {
      response.sendRedirect(prepareRedirectURL(request, "&error=Out of stock. Available:" + product.getStock()));
      return;
    }

    try {
      cartService.add(cartService.getCart(request), productId, quantity, request.getSession());
    } catch (QuantitySumInCartWillBeMoreThanStockException e) {
      response.sendRedirect(prepareRedirectURL(request, "&error=Out of stock. "
                      + (product.getStock() - e.getCurrentCartItemQuantity())
                      + " more available."));
      return;
    }

    response.sendRedirect(prepareRedirectURL(request, "&successMessage=Product added to cart"));
  }

  private int parseQuantityAccordingToLocale(Locale locale, String quantityString) throws ParseException {
    NumberFormat format = NumberFormat.getInstance(locale);
    return format.parse(quantityString).intValue();
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

  private String prepareRedirectURL(HttpServletRequest request, String message) {
    String params = request.getParameterMap().entrySet().stream()
            .filter(entry -> !entry.getKey().matches("successMessage|error"))
            .map(entry -> entry.getKey() + "=" + entry.getValue()[entry.getValue().length - 1])
            .collect(Collectors.joining("&"));

    return request.getRequestURI()
            + "?"
            + params
            + message;
  }
}