<%@ page contentType="text/html; charset=utf-8" isELIgnored="false" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<jsp:useBean id="ctx" type="com.ctrip.hermes.portal.console.dashboard.Context" scope="request" />
<jsp:useBean id="payload" type="com.ctrip.hermes.portal.console.dashboard.Payload" scope="request" />
<jsp:useBean id="model" type="com.ctrip.hermes.portal.console.dashboard.Model" scope="request" />

<a:layout>
	<link href="${model.webapp}/css/dashboard.css" type="text/css" rel="stylesheet">
	<link href="${model.webapp}/css/bootstrap-treeview.min.css" type="text/css" rel="stylesheet">
	<div class="container fluid" ng-app="dash-topic" ng-controller="dash-topic-controller">
		<div class="row">
			<div class="col-md-2 sidebar">
				<ul class="nav nav-sidebar">
					<li ng-click="nav_select(topic_brief)" ng-repeat="topic_brief in topic_briefs" role="presentation" ng-class="$first ? 'active' : ''">
						<a href="" role="tab" data-toggle="tab" aria-controls="content">
							<span ng-bind="topic_brief.topic" style="text-transform: capitalize;"></span>
							<span style="float: right; margin-top: 6px" class="status-ok" ng-if="topic_brief.dangerLevel==0"></span>
							<span style="float: right; margin-top: 6px" class="status-danger" ng-if="topic_brief.dangerLevel==2"></span>
							<span style="float: right; margin-top: 6px" class="status-warn" ng-if="topic_brief.dangerLevel==1"></span>
						</a>
					</li>
				</ul>
			</div>
			<div id="main_board" class="col-md-10 col-md-offset-2 main" style="margin-left: 15%"></div>
		</div>
	</div>
	<script type="text/javascript" src="${model.webapp}/js/bootstrap-treeview.min.js"></script>
	<script type="text/javascript" src="${model.webapp}/js/angular/smart-table.min.js"></script>
	<script type="text/javascript" src="${model.webapp}/js/dashboard/topic-service.js"></script>
	<script type="text/javascript" src="${model.webapp}/js/dashboard/topic-controller.js"></script>
</a:layout>