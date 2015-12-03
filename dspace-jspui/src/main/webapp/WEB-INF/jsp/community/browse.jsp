<%@ include file="/common/taglib.jsp"%>
<div class="form-horizontal">
<display:table list="${communities}" class="jtable" cellspacing="0" cellpadding="0" id="community">
	<display:column property="ID" style="width: 30%;"/>
	<display:column property="metadataFieldPlaceMap.dc_title_1" titleKey="label.title" style="width: 60%;"/>
	<display:column titleKey="label.operations" style="width: 10%;">
		<a href="<c:url value="/community/get.htm?communityId=${community.ID}"/>">
			<i class="fa fa-search-plus" title="<fmt:message key="prompt.detail"/>"  style="cursor:pointer"></i>&nbsp;&nbsp;
		</a>
	</display:column>
</display:table>
</div>