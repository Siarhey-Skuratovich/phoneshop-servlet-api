package com.es.phoneshop.web;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductDao;
import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartService;
import com.es.phoneshop.model.cart.DefaultCartService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class DeleteCartItemServlet extends HttpServlet {
  private CartService cartService;
  private ProductDao productDao;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    cartService = DefaultCartService.getInstance();
    productDao = ArrayListProductDao.getInstance();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String pathInfo = request.getPathInfo();
    if (pathInfo == null) {
      redirectToProductNotFoundPage(request, response, null);
      return;
    }
    String productIdString = pathInfo.substring(1);

    if (isNotADigit(productIdString)) {
      redirectToProductNotFoundPage(request, response, productIdString);
      return;
    }

    long productId = Long.parseLong(productIdString);

    Optional<Product> productOptional = productDao.get(productId);
    if (!productOptional.isPresent()) {
      redirectToProductNotFoundPage(request, response, productIdString);
      return;
    }

    Cart cart = cartService.getCart(request);
    if (!cart.getCartItemByProductId(productId).isPresent()) {
      response.sendRedirect(request.getContextPath()
              + "/cart?UrlParamError=No Product with id "+ productId + " in the Cart to delete");
      return;
    }

    cartService.delete(cart, productId, request.getSession());

    response.sendRedirect(request.getContextPath() + "/cart?successMessage=Cart Item deleted successfully");
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
}
