package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.ProductDao;

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
import java.util.Optional;

public class AdvancedSearchPageServlet extends HttpServlet {
  private ProductDao productDao;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    productDao = ArrayListProductDao.getInstance();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (containsNoRequiredParameters(request)) {
      request.getRequestDispatcher("/WEB-INF/pages/advancedSearch.jsp").forward(request, response);
      return;
    }

    Map<String, String> validationErrors = new HashMap<>();

    String productCode = retrieveProductCode(request);
    BigDecimal minPrice = retrievePrice(request, "minPrice", validationErrors);
    BigDecimal maxPrice = retrievePrice(request, "maxPrice", validationErrors);
    Integer minStock = retrieveMinStock(request, validationErrors);

    if (validationErrors.isEmpty()) {
      request.setAttribute("products",
              productDao.findProductsByAdvancedSearch(productCode, minPrice, maxPrice, minStock));
    } else {
      request.setAttribute("validationErrors", validationErrors);
    }
    request.getRequestDispatcher("/WEB-INF/pages/advancedSearch.jsp").forward(request, response);
  }

  private boolean containsNoRequiredParameters(HttpServletRequest request) {
    return request.getParameterMap().keySet().stream()
            .noneMatch(parameter -> parameter.matches("productCode|minPrice|maxPrice|minStock"));
  }

  private String retrieveProductCode(HttpServletRequest request) {
    String productCode = request.getParameter("productCode");
    if (isEmptyOrNull(productCode)) {
      return null;
    }
    return productCode;
  }

  private BigDecimal retrievePrice(HttpServletRequest request,
                                   String priceParameter,
                                   Map<String, String> validationErrors) {
    String priceString = request.getParameter(priceParameter);
    if (isEmptyOrNull(priceString)) {
      return null;
    }
    try {
      return BigDecimal.valueOf(parseNumberAccordingToLocale(priceString, request.getLocale()));
    } catch (ParseException e) {
      validationErrors.put(priceParameter, "Not a number");
      return null;
    }
  }

  private Integer retrieveMinStock(HttpServletRequest request, Map<String, String> validationErrors) {
    String minStockString = request.getParameter("minStock");
    if (isEmptyOrNull(minStockString)) {
      return null;
    }
    try {
      int minStock = parseNumberAccordingToLocale(minStockString, request.getLocale());
      if (minStock < 0) {
        validationErrors.put("minStock", "Must be positive");
      }
      return minStock;
    } catch (ParseException e) {
      validationErrors.put("minStock", "Not a number");
      return null;
    }
  }

  private boolean isEmptyOrNull(String string) {
    return Optional.ofNullable(string).map(String::isEmpty).orElse(true);
  }

  private int parseNumberAccordingToLocale(String numberString, Locale locale) throws ParseException {
    NumberFormat format = NumberFormat.getInstance(locale);
    return format.parse(numberString).intValue();
  }
}