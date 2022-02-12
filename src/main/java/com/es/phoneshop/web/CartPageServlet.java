package com.es.phoneshop.web;

import com.es.phoneshop.model.product.*;
import com.es.phoneshop.model.product.cart.CartService;
import com.es.phoneshop.model.product.cart.DefaultCartService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CartPageServlet extends HttpServlet {
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
    request.setAttribute("cart", cartService.getCart(request));
    request.getRequestDispatcher("/WEB-INF/pages/cart.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String[] productIds = request.getParameterValues("productId");
    String[] quantities = request.getParameterValues("quantity");

    String messageOfUrlParamError = getUrlParamErrorMessageIfPresent(productIds, quantities);
    if (messageOfUrlParamError != null) {
      response.sendRedirect(request.getContextPath() + "/cart" + "?UrlParamError=" + messageOfUrlParamError);
      return;
    }

    Map<Long, String> validationErrors = new HashMap<>();
    for (int i = 0; i < productIds.length; i++) {
      if (isNotADigit(productIds[i])) {
        redirectToProductNotFoundPage(request, response, productIds[i]);
        return;
      }
      long productId = Long.parseLong(productIds[i]);

      Optional<Product> productOptional = productDao.getProduct(productId);
      if (!productOptional.isPresent()) {
        redirectToProductNotFoundPage(request, response, productIds[i]);
        return;
      }
      Product product = productOptional.get();

      int quantity;
      try {
        quantity = parseQuantityAccordingToLocale(request.getLocale(), quantities[i]);
      } catch (ParseException e) {
        validationErrors.put(productId, "Not a number");
        continue;
      }

      if (quantity <= 0) {
        validationErrors.put(productId, "Quantity must be more than 0");
        continue;
      }

      if (quantity > product.getStock()) {
        validationErrors.put(productId, "Out of stock. Available:" + product.getStock());
        continue;
      }

      cartService.update(cartService.getCart(request), productId, quantity, request.getSession());
    }
    if (validationErrors.isEmpty()) {
      response.sendRedirect(request.getContextPath() + "/cart" + "?successMessage=Products updated successfully");
    } else {
      request.setAttribute("validationErrors", validationErrors);
      doGet(request, response);
    }
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

  private String getUrlParamErrorMessageIfPresent(String[] productIds, String[] quantities) {
    if (quantities == null) {
      return "Missing any required quantity in URL params";
    }
    if (productIds == null) {
      return "Missing any required productId in URL params";
    }
    if (productIds.length != quantities.length) {
      return "ProductIds don't match their required quantities in URL params";
    }
    return null;
  }
}