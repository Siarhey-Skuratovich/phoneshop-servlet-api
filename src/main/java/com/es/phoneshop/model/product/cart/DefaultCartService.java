package com.es.phoneshop.model.product.cart;

import com.es.phoneshop.model.product.ArrayListProductDao;
import com.es.phoneshop.model.product.Product;
import com.es.phoneshop.model.product.ProductDao;
import com.es.phoneshop.model.product.cart.exception.OutOfStockException;
import com.es.phoneshop.model.product.cart.exception.QuantitySumInCartWillBeMoreThanStockException;
import com.es.phoneshop.util.lock.DefaultSessionLockManager;
import com.es.phoneshop.util.lock.SessionLockManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

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
  public void add(Cart cart, Long productId, int quantity, HttpSession session) throws QuantitySumInCartWillBeMoreThanStockException, OutOfStockException {
    Lock sessionLock = sessionLockManager.getSessionLock(session, LOCK_SESSION_ATTRIBUTE);
    sessionLock.lock();
    try {
      Optional<Product> productOptional = productDao.getProduct(productId);
      Optional<CartItem> cartItemOptional = cart.getCartItemByProductId(productId);

      if (!productOptional.isPresent()) {
        return;
      }
      Product product = productOptional.get();

      if (!cartItemOptional.isPresent()) {
        if (quantity > product.getStock()) {
          throw new OutOfStockException();
        }
        cart.getItems().add(new CartItem(product, quantity));
        return;
      }
      CartItem existedCartItem = cartItemOptional.get();

      if (quantitySumInCartWillBeMoreThanStock(existedCartItem, quantity, existedCartItem.getProduct().getStock())) {
        throw new QuantitySumInCartWillBeMoreThanStockException(existedCartItem.getQuantity());
      }

      existedCartItem.increaseQuantity(quantity);

    } finally {
      recalculateCart(cart);
      sessionLock.unlock();
    }
  }

  @Override
  public void update(Cart cart, Long productId, int quantity, HttpSession session) throws OutOfStockException {
    Lock sessionLock = sessionLockManager.getSessionLock(session, LOCK_SESSION_ATTRIBUTE);
    sessionLock.lock();
    try {
      Optional<Product> productOptional = productDao.getProduct(productId);
      Optional<CartItem> cartItemOptional = cart.getCartItemByProductId(productId);

      if (!productOptional.isPresent()) {
        return;
      }
      Product product = productOptional.get();

      if (quantity > product.getStock()) {
        throw new OutOfStockException();
      }

      if (!cartItemOptional.isPresent()) {
        cart.getItems().add(new CartItem(product, quantity));
        return;
      }

      cartItemOptional.get().setQuantity(quantity);

    } finally {
      recalculateCart(cart);
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
      recalculateCart(cart);
      sessionLock.unlock();
    }
  }

  private boolean quantitySumInCartWillBeMoreThanStock(CartItem cartItem, int quantity, int stock) {
    return cartItem.getQuantity() + quantity > stock;
  }

  private void recalculateCart(Cart cart) {
    cart.setTotalCost(cart.getItems().stream()
            .map(cartItem -> cartItem
                    .getProduct()
                    .getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
            .reduce(BigDecimal.valueOf(0), BigDecimal::add));

    cart.setTotalQuantity(cart.getItems().stream().
            mapToInt(CartItem::getQuantity)
            .sum());
  }
}

