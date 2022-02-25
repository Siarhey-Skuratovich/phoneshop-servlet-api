package com.es.phoneshop.model.order;

import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartService;
import com.es.phoneshop.model.cart.DefaultCartService;
import com.es.phoneshop.model.order.exception.EmptyCartException;
import com.es.phoneshop.model.order.exception.ValidationErrorsException;
import com.es.phoneshop.util.lock.DefaultSessionLockManager;
import com.es.phoneshop.util.lock.SessionLockManager;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

public class DefaultOrderService implements OrderService {
  public static final String LOCK_SESSION_ATTRIBUTE = DefaultOrderService.class.getName() + ".lock";
  private final SessionLockManager sessionLockManager = new DefaultSessionLockManager();
  private final OrderDao orderDao;
  private final CartService cartService;


  private DefaultOrderService() {
    orderDao = ArrayListOrderDao.getInstance();
    cartService = DefaultCartService.getInstance();
  }

  private static class InstanceHolder {
    private final static OrderService instance = new DefaultOrderService();
  }

  public static OrderService getInstance() {
    return DefaultOrderService.InstanceHolder.instance;
  }

  @Override
  public Order getOrder(Cart cart) {
    Cart cloneCart;
    try {
      cloneCart = cartService.makeCloneOf(cart);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    Order order = new Order();
    cartService.recalculateCart(cloneCart);
    order.setItems(cloneCart.getItems());

    order.setTotalQuantity(cart.getTotalQuantity());
    order.setSubtotal(cart.getTotalCost());
    order.setDeliveryCost(calculateDeliveryCost());
    order.setTotalCost(order.getSubtotal().add(order.getDeliveryCost()));
    return order;
  }

  @Override
  public List<PaymentMethod> getPaymentMethods() {
    return List.of(PaymentMethod.values());
  }

  @Override
  public Order placeOrder(HttpServletRequest request) throws ValidationErrorsException, EmptyCartException {
    Cart cart = cartService.getCart(request);
    Lock sessionLock = sessionLockManager.getSessionLock(request.getSession(), LOCK_SESSION_ATTRIBUTE);
    cart.lockCartOn(sessionLock);
    try {
      if (cart.getItems().isEmpty()) {
        throw new EmptyCartException();
      }
      Map<String, String> validationErrors = new HashMap<>();
      Order order = getOrder(cart);

      setRequiredParameter(request, "firstName", validationErrors, order::setFirstName);
      setRequiredParameter(request, "lastName", validationErrors, order::setLastName);
      setPhoneNumber(request, validationErrors, order);
      setRequiredParameter(request, "deliveryAddress", validationErrors, order::setDeliveryAddress);
      setDeliveryDate(request, validationErrors, order);
      setPaymentMethod(request, validationErrors, order);

      if (validationErrors.isEmpty()) {
        order.setSecureId(UUID.randomUUID().toString());
        orderDao.save(order);
        cartService.clearCart(cart, request.getSession());
        return order;
      } else {
        throw new ValidationErrorsException(validationErrors, order);
      }
    } finally {
      cart.unLockCart();
    }
  }

  private BigDecimal calculateDeliveryCost() {
    return BigDecimal.valueOf(5);
  }


  private void setRequiredParameter(HttpServletRequest request, String parameter, Map<String, String> errors,
                                    Consumer<String> consumer) {
    String value = request.getParameter(parameter);
    if (value == null || value.isEmpty()) {
      errors.put(parameter, "Missing value");
    } else {
      consumer.accept(value);
    }
  }

  private void setDeliveryDate(HttpServletRequest request, Map<String, String> errors, Order order) {
    String deliveryDateString = request.getParameter("deliveryDate");
    if (deliveryDateString == null || deliveryDateString.isEmpty()) {
      errors.put("deliveryDate", "Missing value");
      return;
    }
    LocalDate deliveryDate;
    try {
      deliveryDate = LocalDate.parse(deliveryDateString);
    } catch (DateTimeParseException e) {
      errors.put("deliveryDate", "Invalid value");
      return;
    }
    if (deliveryDate.isBefore(LocalDate.now())) {
      errors.put("deliveryDate", "Invalid value");
      return;
    }
    order.setDeliveryDate(deliveryDate);
  }

  private void setPaymentMethod(HttpServletRequest request, Map<String, String> errors, Order order) {
    String paymentMethodString = request.getParameter("paymentMethod");
    if (paymentMethodString == null || paymentMethodString.isEmpty()) {
      errors.put("paymentMethod", "Missing value");
      return;
    }
    try {
      order.setPaymentMethod(PaymentMethod.valueOf(paymentMethodString));
    } catch (IllegalArgumentException e) {
      errors.put("paymentMethod", "Invalid value");
    }

  }

  private void setPhoneNumber(HttpServletRequest request, Map<String, String> errors, Order order) {
    String phoneString = request.getParameter("phone");
    if (phoneString == null || phoneString.isEmpty()) {
      errors.put("phone", "Missing phone");
      return;
    }
    if (!phoneString.matches("^\\+375 \\((17|29|33|44)\\) [0-9]{3}-[0-9]{2}-[0-9]{2}$"))
      errors.put("phone", "Phone number must correspond Belarusian format: +375 (**) ***-**-**");
    order.setPhone(phoneString);
  }

}

