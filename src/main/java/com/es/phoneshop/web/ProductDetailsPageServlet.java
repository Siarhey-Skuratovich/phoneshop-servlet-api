package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductDao;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class ProductDetailsPageServlet extends HttpServlet {
  private ProductDao productDao;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    productDao = ArrayListProductDao.getInstance();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String productId = request.getPathInfo().substring(1);
    if (isIdInvalid(productId)) {
      forwardToProductNotFoundPage(request, response, productId);
      return;
    }
    Optional<Product> optionalProduct = productDao.getProduct(Long.parseLong(productId));
    if (optionalProduct.isPresent()) {
      request.setAttribute("product", optionalProduct.get());
      request.getRequestDispatcher("/WEB-INF/pages/product.jsp").forward(request, response);
    } else {
      forwardToProductNotFoundPage(request, response, productId);
    }
  }

  private boolean isIdInvalid(String productId) {
    return !productId.matches("\\d+");
  }

  private void forwardToProductNotFoundPage(HttpServletRequest request,
                                            HttpServletResponse response,
                                            String productId) throws ServletException, IOException {
    request.setAttribute("productId", productId);
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    request.getRequestDispatcher("/WEB-INF/pages/errorProductNotFound.jsp").forward(request, response);
  }
}
