<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="cart" type="com.es.phoneshop.model.product.cart.Cart" scope="request"/>
<tags:master pageTitle="Product Cart List">
  <p>
      ${cart}
  </p>
  <form method="post">
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
      <c:forEach var="item" items="${cart.items}">
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
          <td class="price">
            <a href="${pageContext.servletContext.contextPath}/products/price/${item.product.id}">
              <fmt:formatNumber value="${item.product.price}" type="currency"
                                currencySymbol="${item.product.currency.symbol}"/>
            </a>
          </td>
          <td>
            <fmt:formatNumber value="${item.quantity}" var="quantity"/>
            <input name="quantity" value="${quantity}" class="quantity"/>
            <input name="productId" type="hidden" value="${item.product.id}">
          </td>
        </tr>
      </c:forEach>
    </table>
    <p>
      <button>Update</button>
    </p>
  </form>
</tags:master>