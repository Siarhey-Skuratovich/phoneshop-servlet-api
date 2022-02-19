<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="order" type="com.es.phoneshop.model.order.Order" scope="request"/>
<tags:master pageTitle="Checkout">
  <c:if test="${not empty validationErrors}">
    <p class="error">
      There were errors submitting Order details:
    </p>
  </c:if>
  <p>
  <table>
    <thead>
    <tr>
      <td>Image</td>
      <td>
        Description
      </td>
      <td class="quantity">
        Quantity
      </td>
      <td class="price">
        Price
      </td>
    </tr>
    </thead>
    <c:forEach var="item" items="${order.items}" varStatus="status">
      <tr>
        <td>
          <img class="product-tile"
               src="https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/${item.product.imageUrl}">
        </td>
        <td>
          <a href="${pageContext.servletContext.contextPath}/products/${item.product.id}">
              ${item.product.description}
          </a>
        </td>
        <td class="quantity">
            ${item.quantity}
        </td>
        <td class="price">
          <a href="${pageContext.servletContext.contextPath}/products/price/${item.product.id}">
            <fmt:formatNumber value="${item.product.price}" type="currency"
                              currencySymbol="${item.product.currency.symbol}"/>
          </a>
        </td>
      </tr>
    </c:forEach>
    <tr>
      <td></td>
      <td></td>
      <td>Total Quantity: ${order.totalQuantity}</td>
      <td>Subtotal: <fmt:formatNumber value="${order.subtotal}" type="currency"
                                      currencySymbol="$"/></td>
    </tr>
    <tr>
      <td></td>
      <td></td>
      <td></td>
      <td>Delivery Cost: <fmt:formatNumber value="${order.deliveryCost}" type="currency"
                                           currencySymbol="$"/></td>
    </tr>
    <tr>
      <td></td>
      <td></td>
      <td></td>
      <td>Total Cost: <fmt:formatNumber value="${order.totalCost}" type="currency"
                                        currencySymbol="$"/></td>
    </tr>
  </table>
  <h2>Order details:</h2>
  <form method="post">
    <table>
      <tags:orderFormRow name="firstName" label="First name" order="${order}" validationErrors="${validationErrors}"/>
      <tags:orderFormRow name="lastName" label="Last name" order="${order}" validationErrors="${validationErrors}"/>
      <tags:orderFormRow name="phone" label="Phone" order="${order}" validationErrors="${validationErrors}"/>

      <tr>
        <td>Delivery date<span style="color: red">*</span></td>
        <td>
          <c:set var="error" value="${validationErrors.get('deliveryDate')}"/>
          <input type="date" name="deliveryDate"
                 value="${not empty error ? param['deliveryDate'] : order['deliveryDate']}"/>
          <c:if test="${not empty error}">
            <div class="error">
                ${error}
            </div>
          </c:if>
        </td>
      </tr>

      <tags:orderFormRow name="deliveryAddress" label="Delivery address" order="${order}"
                         validationErrors="${validationErrors}"/>

      <tr>
        <td>Payment method<span style="color: red">*</span></td>
        <td>
          <select name="paymentMethod">
            <option></option>
            <c:set var="error" value="${validationErrors.get('paymentMethod')}"/>
            <c:forEach var="paymentMethod" items="${paymentMethods}">
              <option <c:if test="${paymentMethod == order.paymentMethod}">selected</c:if>>
                  ${paymentMethod}
              </option>
            </c:forEach>
          </select>
          <c:if test="${not empty error}">
            <div class="error">
                ${error}
            </div>
          </c:if>
        </td>
      </tr>
    </table>
    <p>
      <button>Order</button>
    </p>
  </form>
</tags:master>