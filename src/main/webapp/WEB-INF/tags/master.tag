<%@ tag trimDirectiveWhitespaces="true" %>
<%@ attribute name="pageTitle" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
  <title>${pageTitle}</title>
  <link href='http://fonts.googleapis.com/css?family=Lobster+Two' rel='stylesheet' type='text/css'>
  <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/styles/main.css">
</head>
<body class="product-list">
<header>
  <a href="${pageContext.servletContext.contextPath}/">
    <img src="${pageContext.servletContext.contextPath}/images/logo.svg"/>
    PhoneShop
  </a>
  <a href="${pageContext.servletContext.contextPath}/cart"><jsp:include page="/cart/miniCart"/></a>
</header>
<main>
  <jsp:doBody/>
</main>
  <c:if test="${not empty recentlyViewedProducts}">
<h2>
  Recently Viewed:
</h2>
<table id="table-of-recently-viewed-products">
  <tr>
    <c:forEach var="product" items="${recentlyViewedProducts}">
      <td class="center">
        <img class="product-tile"
             src="https://raw.githubusercontent.com/andrewosipenko/phoneshop-ext-images/master/${product.imageUrl}"><br>
        <a href="${pageContext.servletContext.contextPath}/products/${product.id}">
            ${product.description}
        </a><br>
        <span><fmt:formatNumber value="${product.price}" type="currency"
                                currencySymbol="${product.currency.symbol}"/></span>
      </td>
    </c:forEach>
  </tr>
</table>
</c:if>
<p>
  (c) Expert-Soft
</p>
</body>
</html>