package com.es.phoneshop.web;

import com.es.phoneshop.model.product.*;
import com.es.phoneshop.model.product.cart.CartService;
import com.es.phoneshop.model.product.cart.DefaultCartService;
import com.es.phoneshop.model.product.cart.exception.OutOfStockException;
import com.es.phoneshop.model.product.cart.exception.QuantitySumInCartWillBeMoreThanStockException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Optional;

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
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    String productIdString = request.getParameter("productId");
    if (productIdString == null) {
      redirectToProductNotFoundPage(request, response, null);
      return;
    }

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
      request.setAttribute("error", "You haven't specified a quantity");
      doGet(request, response);
      return;
    }

    int quantity;
    try {
      quantity = parseQuantityAccordingToLocale(request.getLocale(), quantityString);
    } catch (ParseException e) {
      request.setAttribute("error", "Not a number");
      doGet(request, response);
      return;
    }

    if (quantity <= 0) {
      request.setAttribute("error", "Quantity must be more than 0");
      doGet(request, response);
      return;
    }

    try {
      cartService.add(cartService.getCart(request), productId, quantity, request.getSession());
    } catch (OutOfStockException e) {
      request.setAttribute("error","Out of stock. Max Available:" + product.getStock());
      doGet(request, response);
      return;
    } catch (QuantitySumInCartWillBeMoreThanStockException e) {
      request.setAttribute("error","Out of stock. "
              + (product.getStock() - e.getCurrentCartItemQuantity())
              + " more available.");
      doGet(request, response);
      return;
    }

    response.sendRedirect(prepareSuccessRedirectURL(request));
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

  private String prepareSuccessRedirectURL(HttpServletRequest request) {
    return request.getRequestURI()
            + "?"
            + request.getQueryString()
            + "&successMessage=Product added to cart";
  }
}