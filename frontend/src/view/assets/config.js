/* <%@ page contentType="application/javascript" %> */
/* <%@ page import="org.agnitas.util.AgnUtils" %> */
/* <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> */
/* <%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common" prefix="emm" %> */

window.agnResolveRelativeUrl = function(relativeUrl, excludeSessionId) {
  if (excludeSessionId === true) {
    return '${pageContext.request.contextPath}/' + relativeUrl;
  } else {
    var appendablePart = '';
    var position = relativeUrl.indexOf('?');

    if (position < 0) {
      position = relativeUrl.indexOf('#');
    }

    if (position >= 0) {
      appendablePart = relativeUrl.substring(position);
      relativeUrl = relativeUrl.substring(0, position);
    }

    return '<c:url value="/{RELATIVE-PATH}"/>'.replace('{RELATIVE-PATH}', relativeUrl) + appendablePart;
  }
};

/* <c:set var="timeZoneId" value="${emm:getTimeZoneId(pageContext.request)}"/> */
/* <c:choose>
     <c:when test="${empty timeZoneId}"> */
       window.agnTimeZoneId = null;
/*   </c:when>
     <c:otherwise> */
       window.agnTimeZoneId = '${timeZoneId}';
/*   </c:otherwise>
</c:choose> */

/* <c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN%>" />
   <c:set var="adminLocale" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN].locale}" />*/
/* <c:choose>
     <c:when test="${empty adminLocale}"> */
       window.adminLocale = navigator.language;
/*   </c:when>
     <c:otherwise> */
       window.adminLocale = '${adminLocale}'.replace('_', '-');
/*   </c:otherwise>
</c:choose> */
