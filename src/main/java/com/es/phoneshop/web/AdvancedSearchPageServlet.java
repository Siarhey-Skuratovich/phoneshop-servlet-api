package com.es.phoneshop.web;

import com.es.phoneshop.model.cart.CartService;
import com.es.phoneshop.model.cart.DefaultCartService;
import com.es.phoneshop.model.product.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdvancedSearchPageServlet extends HttpServlet {
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
    String productCodeString = request.getParameter("productCode");
    String minPriceString = request.getParameter("minPrice");
    String maxPriceString = request.getParameter("maxPrice");
    String minStockString = request.getParameter("minStock");

    if (searchFormIsNotEmpty(productCodeString, minPriceString, maxPriceString, minStockString)) {
      Map<String, String> validationErrors = new HashMap<>();

      BigDecimal minPrice = null;
      if (isNotADigit(minPriceString)) {
        validationErrors.put("minPrice", "Not a number");
      } else {
        minPrice = BigDecimal.valueOf(Integer.parseInt(minPriceString));
      }

      BigDecimal maxPrice = null;
      if (isNotADigit(maxPriceString)) {
        validationErrors.put("maxPrice", "Not a number");
      } else {
        maxPrice = BigDecimal.valueOf(Integer.parseInt(maxPriceString));
      }

      int minStock = 0;
      if (isNotADigit(minStockString)) {
        validationErrors.put("minStock", "Not a number");
      } else {
        minStock = Integer.parseInt(minStockString);
        if (minStock < 0) {
          validationErrors.put("minStock", "Must be positive");
        }
      }

      if (validationErrors.isEmpty()) {
        request.setAttribute("products",
                productDao.findProductsByAdvancedSearch(productCodeString, minPrice, maxPrice, minStock));
      } else {
        request.setAttribute("validationErrors", validationErrors);
      }
    }
    request.getRequestDispatcher("/WEB-INF/pages/advancedSearch.jsp").forward(request, response);
  }

  private boolean searchFormIsNotEmpty(String productCodeString,
                                       String minPriceString,
                                       String maxPriceString,
                                       String minStockString) {
    return productCodeString != null && minPriceString != null && maxPriceString != null && minStockString != null;
  }

  private boolean isNotADigit(String string) {
    return !string.matches("\\d+");
  }
}