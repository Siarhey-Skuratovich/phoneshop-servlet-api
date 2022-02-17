<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${param.sort != null || param.query != null}">
?sort=${param.sort}&order=${param.order}&query=${param.query}
</c:if>
