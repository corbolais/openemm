<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="bounceFilterForm" type="com.agnitas.emm.core.bounce.form.BounceFilterForm"--%>
<%--@elvariable id="userFormList" type="java.util.List<com.agnitas.userform.bean.UserForm>"--%>
<%--@elvariable id="mailingLists" type="java.util.List<org.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="actionBasedMailings" type="java.util.List<org.agnitas.emm.core.mailing.beans.LightweightMailing>"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="filterEmailAddressDefault" type="java.lang.String"--%>
<%--@elvariable id="isAllowedMailloopDomain" type="java.lang.Boolean"--%>

<jsp:include page="/${emm:ckEditorPath(pageContext.request)}/ckeditor-emm-helper.jsp"/>

<mvc:form servletRelativeAction="/administration/bounce/save.action" id="bounceFilterForm" modelAttribute="bounceFilterForm"
          data-form="resource">

    <mvc:hidden path="id"/>
    <c:set var="isNew" value="${bounceFilterForm.id <= 0}"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="settings.EditMailloop"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="shortName"><bean:message key="Name"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="shortName" id="shortName" maxlength="99" size="32" cssClass="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="description"><bean:message key="Description"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:textarea path="description" id="description" rows="5" cols="32" cssClass="form-control"/>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="report.mailing.filter" /></h2>
        </div>
        <div class="tile-content tile-content-forms" data-field="toggle-vis">
            <div class="hidden" data-field-vis-default="" data-field-vis-hide="#forward-field-container, #autoresponder-field-container"></div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="filterEmail">
                        <bean:message key="mailloop.filter_adr"/>
                        <button class="icon icon-help" data-help="help_${helplanguage}/settings/BounceFilterAddress.xml"
                                tabindex="-1" type="button"></button>
                    </label>
                </div>
                <c:if test="${isAllowedMailloopDomain}">
                    <div class="col-sm-8">
                        <label class="radio-inline" for="radio-address-default">
                            <input type="radio" id="radio-address-default" name="addressType" data-field-vis="" data-field-vis-show="#filterEmailDefault"
                                   data-field-vis-hide="#filterEmailOwn, #ownForwardEmailSelected" ${bounceFilterForm.ownForwardEmailSelected ? '' : 'checked'} />
                            <bean:message key="mailloop.address.default"/>
                        </label>
                        <label class="radio-inline" for="radio-address-individual">
                            <input type="radio" id="radio-address-individual" name="addressType" data-field-vis="" data-field-vis-show="#filterEmailOwn, #ownForwardEmailSelected"
                                   data-field-vis-hide="#filterEmailDefault" ${bounceFilterForm.ownForwardEmailSelected ? 'checked' : ''}/>
                            <bean:message key="mailloop.address.individual"/>
                        </label>
                    </div>

            </div><%-- end this form-group and start the new--%>
            <div class="form-group">

                    <div class="col-sm-4"></div>
                </c:if>
                <div class="col-sm-8">
                    <c:if test="${not empty filterEmailAddressDefault}">
                        <input id="filterEmailDefault" type="text" class="form-control" value="${filterEmailAddressDefault}" readonly="true"/>
                    </c:if>
                    <mvc:text path="filterEmail" id="filterEmailOwn" maxlength="99" size="42" cssClass="form-control"/>
                    <input id="ownForwardEmailSelected" name="ownForwardEmailSelected" class="hidden" value="true">
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="doForward"><bean:message key="settings.mailloop.forward"/></label>
                </div>
                <div class="col-sm-8">
                    <label class="toggle">
                        <mvc:checkbox path="doForward" id="doForward" cssClass="tag-spring" data-field-vis=""
                                      data-field-vis-show="#forward-field-container"/>
                        <div class="toggle-control"></div>
                    </label>
                </div>
            </div>
            <div id="forward-field-container" class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="forwardEmail"><bean:message key="settings.mailloop.forward.address"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="forwardEmail" id="forwardEmail" maxlength="99" size="42" cssClass="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="doAutoRespond"> <bean:message key="mailloop.autoresponder"/></label>
                </div>
                <div class="col-sm-8">
                    <label class="toggle">
                        <mvc:checkbox path="doAutoRespond" id="doAutoRespond" cssClass="tag-spring" data-field-vis=""
                                      data-field-vis-show="#autoresponder-field-container"/>
                        <div class="toggle-control"></div>
                    </label>
                </div>
            </div>

            <div id="autoresponder-field-container" class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="mailingListId"><bean:message key="mailloop.autoresponder.mailing"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="arMailingId" id="arMailingId" cssClass="form-control js-select" size="1">
                        <c:forEach items="${actionBasedMailings}" var="mailing">
                            <mvc:option value="${mailing.mailingID}">${mailing.shortname}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
            </div>
        </div>
    </div>

</mvc:form>

<script id="modal-editor-text" type="text/x-mustache-template">
    <div class="modal modal-editor">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                    </button>
                    <h4 class="modal-title">{{= title }}</h4>
                </div>
                <div class="modal-body">
                    <textarea id="modalTextArea" data-sync="\#{{= target }}" class="form-control js-editor-text" data-form-target="#bounceFilterForm"></textarea>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large" data-sync-from="#modalTextArea" data-sync-to="\#{{= target }}" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><bean:message key="button.Apply"/></span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>
<script id="modal-editor" type="text/x-mustache-template">
    {{ showHTMLEditor = $('\#tab-bounceFilterContentViewHTML').is(':visible') }}
    <div class="modal modal-editor">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                    </button>
                    <h4 class="modal-title">{{= title }}</h4>
                    <ul class="modal-header-nav">
                        <li>
                            <a href="#" data-toggle-tab="#tab-bounceFilterContentViewHTMLModal">
                                <bean:message key="mailingContentHTMLEditor"/>
                            </a>
                        </li>
                        <li class="active">
                            <a href="#" data-toggle-tab="#tab-bounceFilterContentViewCodeModal">
                                <bean:message key="mailing.HTML_Version"/>
                            </a>
                        </li>
                    </ul>
                </div>
                <div class="modal-body">
                    <div id="tab-bounceFilterContentViewHTMLModal" {{ (showHTMLEditor) ? print('data-tab-show') : print('data-tab-hide') }}>
                    <textarea id="modalTextArea" name="modalTextArea" data-sync="\#{{= target }}" class="form-control js-editor js-wysiwyg"
                              data-form-target="#bounceFilterForm"
                              data-browse-mailing-id="${bounceFilterForm.arMailingId}"></textarea>
                </div>

                <div id="tab-bounceFilterContentViewCodeModal" {{ (showHTMLEditor) ? print('data-tab-hide') : print('data-tab-show') }}>
                <div id="modalTextAreaEditor" class="form-control"></div>
            </div>

        </div>
        <div class="modal-footer">
            <div class="btn-group">
                <button type="button" class="btn btn-default btn-large" data-dismiss="modal">
                    <i class="icon icon-times"></i>
                    <span class="text"><bean:message key="button.Cancel"/></span>
                </button>
                <button type="button" class="btn btn-primary btn-large" data-sync-from="#modalTextArea" data-sync-to="\#{{= target }}" data-dismiss="modal">
                    <i class="icon icon-check"></i>
                    <span class="text"><bean:message key="button.Apply"/></span>
                </button>
            </div>
        </div>
    </div>
</script>
