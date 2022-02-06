<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="product" type="com.es.phoneshop.model.product.Product" scope="request"/>
<tags:master pageTitle="Price History">
  <h1>
    Price History
  </h1>
  <h2>
    ${product.description}
  </h2>
  <table>
    <tr>
      <td>
        <b>Start day</b>
      </td>
      <td>
        <b>Price</b>
      </td>
    </tr>
    <c:forEach var="change" items="${product.priceChangesHistory}">
      <tr>
        <td>
          ${change.startDate}
        </td>
        <td>
          <fmt:formatNumber value="${change.price}" type="currency" currencySymbol="${change.currency.symbol}"/>
        </td>
      </tr>
    </c:forEach>
  </table>
  <p>
    <a href="${pageContext.servletContext.contextPath}/products">
      <button>To product list</button>
    </a>
  </p>
</tags:master>

