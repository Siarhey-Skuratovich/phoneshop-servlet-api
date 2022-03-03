<%@ tag trimDirectiveWhitespaces="true" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="validationErrors" required="true" type="java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tr>
  <td>${label}</td>
  <td>
    <c:set var="error" value="${validationErrors.get(name)}"/>
    <input name=${name} value="${param[name]}"/>
    <c:if test="${not empty error}">
      <div class="error">
          ${error}
      </div>
    </c:if>
  </td>
</tr>
