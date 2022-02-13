<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="cart" type="com.es.phoneshop.model.product.cart.Cart" scope="request"/>
<table class="miniCart">
  <tr>
    <th class="center">Cart:</th>
  </tr>
  <tr>
    <td>
      <span>Total Quantity: ${cart.totalQuantity}<br></span>
      <span>Total Cost:
        <c:if test="${cart.totalCostsMap.size() == 0}">
          0
        </c:if>
  <c:forEach var="entry" items="${cart.totalCostsMap.entrySet()}" varStatus="status">
    <fmt:formatNumber value="${entry.value}" type="currency" currencySymbol="${entry.key.symbol}"/>
    <c:if test="${not status.last}">
      and
    </c:if>
  </c:forEach>
  </span>
    </td>
  </tr>
</table>