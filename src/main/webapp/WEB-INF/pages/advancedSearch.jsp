<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<tags:master pageTitle="Product List">
  <h1>Advanced Search Page</h1>
  <form>
    <table>
      <tr>
        <td>Product code</td>
        <td><input name="productCode" value="${param['productCode']}"/></td>
      </tr>
      <tags:advancedSearchFormRow name="minPrice" label="Min price" validationErrors="${validationErrors}"/>
      <tags:advancedSearchFormRow name="maxPrice" label="Max price" validationErrors="${validationErrors}"/>
      <tags:advancedSearchFormRow name="minStock" label="Min stock" validationErrors="${validationErrors}"/>
    </table>
    <p>
      <button>Search</button>
    </p>
  </form>
  <c:if test="${not empty param.successMessage}">
    <p class="success">
        ${param.successMessage}
    </p>
  </c:if>
  <c:if test="${not empty products}">
    <p class="success">Found ${products.size()} product(s)</p>
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
      </tr>
      </thead>
      <c:forEach var="product" items="${products}" varStatus="status">
        <tr>
          <td>
            <img class="product-tile"
                 src="https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/${product.imageUrl}">
          </td>
          <td>
            <a href="${pageContext.servletContext.contextPath}/products/${product.id}">
                ${product.description}
            </a>
          </td>
          <td class="price">
            <a href="${pageContext.servletContext.contextPath}/products/price/${product.id}">
              <fmt:formatNumber value="${product.price}" type="currency" currencySymbol="${product.currency.symbol}"/>
            </a>
          </td>
        </tr>
      </c:forEach>
    </table>
  </c:if>
</tags:master>
