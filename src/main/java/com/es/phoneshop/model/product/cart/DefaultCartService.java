package com.es.phoneshop.model.product.cart;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

public class DefaultCartService implements CartService{
  private static final String CART_SESSION_ATTRIBUTE = DefaultCartService.class.getName() + ".cart";
  private static final String SESSION_MUTEX_ATTRIBUTE = DefaultCartService.class.getName() + ".mutex";
  private final ProductDao productDao;

  private DefaultCartService() {
    this.productDao = ArrayListProductDao.getInstance();
  }

  private static class InstanceHolder {
    private final static CartService instance = new DefaultCartService();
  }

  public static CartService getInstance() {
    return DefaultCartService.InstanceHolder.instance;
  }

  @Override
  public Cart getCart(HttpServletRequest request) {
    synchronized (getSessionMutex(request.getSession())) {
      Cart cart = (Cart) request.getSession().getAttribute(CART_SESSION_ATTRIBUTE);
      if (cart == null) {
        request.getSession().setAttribute(CART_SESSION_ATTRIBUTE, cart = new Cart());
      }
      return cart;
    }
  }

  @Override
  public void add(Cart cart, Long productId, int quantity, HttpSession session) {
    synchronized (getSessionMutex(session)) {
      Optional<CartItem> cartItemOptional = cart.getCartItemByProductId(productId);
      if (cartItemOptional.isPresent()) {
        cartItemOptional.get().increaseQuantity(quantity);
        return;
      }
      Optional<Product> productOptional = productDao.getProduct(productId);
      productOptional.ifPresent(product -> cart.getItems().add(new CartItem(product, quantity)));
    }
  }

  private Object getSessionMutex(HttpSession session) {
    Object mutex = session.getAttribute(SESSION_MUTEX_ATTRIBUTE);
    if (mutex == null) {
      mutex = new Object();
      session.setAttribute(SESSION_MUTEX_ATTRIBUTE, mutex);
    }
    return mutex;
  }
}