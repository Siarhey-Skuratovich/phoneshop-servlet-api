package com.es.phoneshop.model.order;

import com.es.phoneshop.model.cart.Cart;
import com.es.phoneshop.model.cart.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultOrderService implements OrderService {
  private final OrderDao orderDao;

  private DefaultOrderService() {
    orderDao = ArrayListOrderDao.getInstance();
  }

  private static class InstanceHolder {
    private final static OrderService instance = new DefaultOrderService();
  }

  public static OrderService getInstance() {
    return DefaultOrderService.InstanceHolder.instance;
  }

  @Override
  public Order getOrder(Cart cart) {
    Order order = new Order();
    order.setItems(cart.getItems().stream().map(cartItem -> {
              try {
                return (CartItem) cartItem.clone();
              } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
              }
            }).collect(Collectors.toList()));

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
  public void placeOrder(Order order) {
    orderDao.save(order);
  }

  private BigDecimal calculateDeliveryCost() {
    return BigDecimal.valueOf(5);
  }

}

