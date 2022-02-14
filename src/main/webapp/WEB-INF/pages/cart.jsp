<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="cart" type="com.es.phoneshop.model.product.cart.Cart" scope="request"/>
<tags:master pageTitle="Product Cart List">
  <p>
      ${cart}
  </p>
  <c:if test="${not empty param.successMessage}">
    <p class="success">
        ${param.successMessage}
    </p>
  </c:if>
  <c:if test="${not empty param.UrlParamError}">
    <p class="error">
        ${param.UrlParamError}
    </p>
  </c:if>
  <c:if test="${not empty validationErrors}">
    <p class="error">
      There were errors updating the cart:
    </p>
  </c:if>
  <form method="post" action="${pageContext.servletContext.contextPath}/cart">
    <table>
      <thead>
      <tr>
        <td>Image</td>
        <td>
          Description
        </td>
        <td class="price">
          Price
        </td>
        <td class="quantity">
          Quantity
        </td>
      </tr>
      </thead>
      <c:forEach var="item" items="${cart.items}" varStatus="status">
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
            <fmt:formatNumber value="${item.quantity}" var="quantity"/>
            <c:set var="error" value="${validationErrors[item.product.id]}"/>
            <input name="quantity" value="${not empty error ? paramValues['quantity'][status.index] : quantity}"
                   class="quantity"/>
            <input name="productId" type="hidden" value="${item.product.id}">
            <c:if test="${not empty error}">
              <div class="error">
                  ${error}
              </div>
            </c:if>
          </td>
          <td class="price">
            <a href="${pageContext.servletContext.contextPath}/products/price/${item.product.id}">
              <fmt:formatNumber value="${item.product.price}" type="currency"
                                currencySymbol="${item.product.currency.symbol}"/>
            </a>
          </td>
          <td>
            <button form="deleteCartItem"
                    formaction="${pageContext.servletContext.contextPath}/cart/deleteCartItem/${item.product.id}">
              Delete
            </button>
          </td>
        </tr>
      </c:forEach>
    </table>
    <p>
      <button>Update</button>
    </p>
  </form>
  <form id="deleteCartItem" method="post">
  </form>
</tags:master>