<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="cart" type="com.es.phoneshop.model.cart.Cart" scope="request"/>
<table class="miniCart">
  <tr>
    <th class="center">Cart:</th>
  </tr>
  <tr>
    <td>
      <span>Total Quantity: ${cart.totalQuantity}<br></span>
      <span>Total Cost: <fmt:formatNumber value="${cart.totalCost}" type="currency"
                                                        currencySymbol="$"/>
  </span>
    </td>
  </tr>
</table>