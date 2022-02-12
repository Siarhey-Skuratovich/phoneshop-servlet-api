package com.es.phoneshop.model.product.cart;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductDao;
import com.es.phoneshop.model.product.cart.exception.QuantitySumInCartWillBeMoreThanStockException;
import com.es.phoneshop.util.lock.DefaultSessionLockManager;
import com.es.phoneshop.util.lock.SessionLockManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

public class DefaultCartService implements CartService {
  private static final String CART_SESSION_ATTRIBUTE = DefaultCartService.class.getName() + ".cart";
  private static final String LOCK_SESSION_ATTRIBUTE = DefaultCartService.class.getName() + ".lock";
  private final ProductDao productDao;
  private final SessionLockManager sessionLockManager = new DefaultSessionLockManager();

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
    Lock sessionLock = sessionLockManager.getSessionLock(request.getSession(), LOCK_SESSION_ATTRIBUTE);
    sessionLock.lock();
    try {
      Cart cart = (Cart) request.getSession().getAttribute(CART_SESSION_ATTRIBUTE);
      if (cart == null) {
        request.getSession().setAttribute(CART_SESSION_ATTRIBUTE, cart = new Cart());
      }
      return cart;
    } finally {
      sessionLock.unlock();
    }
  }

  @Override
  public void add(Cart cart, Long productId, int quantity, HttpSession session) throws QuantitySumInCartWillBeMoreThanStockException {
    Lock sessionLock = sessionLockManager.getSessionLock(session, LOCK_SESSION_ATTRIBUTE);
    sessionLock.lock();
    try {
      CartItem existedCartItem = getExistedOrAddNewCartItem(cart, productId, quantity);

      if (existedCartItem == null) {
        return;
      }

      if (quantitySumInCartWillBeMoreThanStock(existedCartItem, quantity, existedCartItem.getProduct().getStock())) {
        throw new QuantitySumInCartWillBeMoreThanStockException(existedCartItem.getQuantity());
      }

      existedCartItem.increaseQuantity(quantity);

    } finally {
      sessionLock.unlock();
    }
  }

  @Override
  public void update(Cart cart, Long productId, int quantity, HttpSession session) {
    Lock sessionLock = sessionLockManager.getSessionLock(session, LOCK_SESSION_ATTRIBUTE);
    sessionLock.lock();
    try {
      CartItem existedCartItem = getExistedOrAddNewCartItem(cart, productId, quantity);

      if (existedCartItem != null) {
        existedCartItem.setQuantity(quantity);
      }

    } finally {
      sessionLock.unlock();
    }
  }

  @Override
  public void delete(Cart cart, Long productId, HttpSession session) {
    Lock sessionLock = sessionLockManager.getSessionLock(session, LOCK_SESSION_ATTRIBUTE);
    sessionLock.lock();
    try {
      cart.getItems().removeIf(cartItem -> productId.equals(cartItem.getProduct().getId()));

    } finally {
      sessionLock.unlock();
    }
  }

  private boolean quantitySumInCartWillBeMoreThanStock(CartItem cartItem, int quantity, int stock) {
    return cartItem.getQuantity() + quantity > stock;
  }

  private CartItem getExistedOrAddNewCartItem(Cart cart, Long productId, int quantity) {
    Optional<Product> productOptional = productDao.getProduct(productId);
    Optional<CartItem> cartItemOptional = cart.getCartItemByProductId(productId);

    if (!productOptional.isPresent()) {
      return null;
    }
    Product product = productOptional.get();

    if (!cartItemOptional.isPresent()) {
      cart.getItems().add(new CartItem(product, quantity));
      return null;
    }
    return cartItemOptional.get();
  }
}
