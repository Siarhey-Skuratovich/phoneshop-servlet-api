<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="product" type="com.es.phoneshop.model.product.Product" scope="request"/>
<jsp:useBean id="cart" type="com.es.phoneshop.model.product.cart.Cart" scope="request"/>
<tags:master pageTitle="Product Details">
  <p>
    ${cart}
  </p>
  <c:if test="${not empty param.message}">
    <div class="success">
        ${param.message}
    </div>
  </c:if>
  <c:if test="${not empty param.error}">
    <div class="error">
        There was an error adding to cart
    </div>
  </c:if>
  <p>
      ${product.description}
  </p>
  <form method="post" action="${pageContext.servletContext.contextPath}/products/${product.id}">
    <table>
      <tr>
        <td>Image</td>
        <td>
          <img src="https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/${product.imageUrl}">
        </td>
      </tr>
      <tr>
        <td>Code</td>
        <td class="code">${product.code}</td>
      </tr>
      <tr>
        <td>Price</td>
        <td class="price">
          <fmt:formatNumber value="${product.price}" type="currency"
                            currencySymbol="${product.currency.symbol}"/>
        </td>
      </tr>
      <tr>
        <td>Product Stock</td>
        <td class="stock">${product.stock}</td>
      </tr>
      <tr>
        <td>
          Quantity
        </td>
        <td class="quantity">
          <input class="quantity" name="quantity" value="${not empty param.error ? param.quantity : 1}">
          <c:if test="${not empty param.error}">
            <div class="error">
              ${param.error}
            </div>
          </c:if>
        </td>
      </tr>
    </table>
    <button>Add to cart</button>
  </form>
  <p>
    <a href="${pageContext.servletContext.contextPath}/products/price/${product.id}">
      <button>Watch the price history</button>
    </a>
  </p>
  <p>
    <a href="${pageContext.servletContext.contextPath}/products">
      <button>To product list</button>
    </a>
  </p>
</tags:master>