<%@ page contentType="text/html; charset=utf-8" isELIgnored="false" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<jsp:useBean id="ctx" type="com.ctrip.hermes.portal.console.dashboard.Context" scope="request" />
<jsp:useBean id="payload" type="com.ctrip.hermes.portal.console.dashboard.Payload" scope="request" />
<jsp:useBean id="model" type="com.ctrip.hermes.portal.console.dashboard.Model" scope="request" />

<div class="row">
	<div class="col-md-6">
		<div class="panel panel-success">
			<div class="panel-heading">
				<span class="label label-danger">生产</span>
				<button type="button" data-toggle="modal" data-target="#top-producer-modal" class="btn btn-xs btn-success" style="text-align: center;"><span class="glyphicon glyphicon-th-list"></span>
					生产速度</button>
				<button type="button" data-toggle="modal" data-target="#top-consumer-modal" class="btn btn-xs btn-warning" style="text-align: center;"><span class="glyphicon glyphicon-th-list"></span>
					消费速度</button>
				<button type="button" data-toggle="modal" data-target="#top-process-modal" class="btn btn-xs btn-primary" style="text-align: center;"><span class="glyphicon glyphicon-time"></span> 消费时间</button>
				<button type="button" data-toggle="modal" data-target="#top-latest-modal" class="btn btn-xs btn-info" style="text-align: center;"><span class="glyphicon glyphicon-eye-open"></span> 最新消息</button>
			</div>
			<div class="panel-body">
				<iframe style="border: 0" ng-src="{{get_produced_kibana('${model.kibanaUrl}')}}" height="200" width="100%"></iframe>
			</div>
		</div>
	</div>
	<div class="col-md-6">
		<div class="panel panel-info">
			<div class="panel-heading">Consumer 消费延时</div>
			<table class="table table-hover" st-pipe="topic_delays" st-table="delay_table">
				<thead>
					<tr>
						<th st-sort="consumer">Consumer 名称</th>
						<th st-sort="partition">Partition ID</th>
						<th st-sort="delay">Delay(秒)</th>
					</tr>
					<tr>
						<th><input st-search="consumer" placeholder="Consumer Name" class="input-sm form-control" type="search" ng-model-options="{updateOn:'blur'}" /></th>
						<th><input st-search="partition" placeholder="Partition ID" class="input-sm form-control" type="search" ng-model-options="{updateOn:'blur'}" /></th>
						<th><input st-search="delay" placeholder="Delay" class="input-sm form-control" type="search" ng-model-options="{updateOn:'blur'}" /></th>
					</tr>
				</thead>
				<tbody ng-if="!is_loading">
					<tr ng-click="" ng-repeat="delay in topic_delays">
						<td><span ng-bind="delay.consumer"></span></td>
						<td><span ng-bind="delay.partitionId"></span></td>
						<td><span ng-bind="normalize_delay(delay.delay)"></span></td>
					</tr>
				</tbody>
				<tbody ng-if="is_loading">
					<tr>
						<td colspan="1" class="text-center">Loading ...</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</div>
<div class="row" ng-repeat="delay in topic_delays">
	<div class="col-md-6">
		<div class="panel panel-success">
			<div class="panel-heading">
				<span class="label label-danger">
					消费组：
					<span ng-bind="delay.consumer"></span>
				</span>
			</div>
			<div class="panel-body">
				<iframe style="border: 0" ng-src="{{get_consumed_kibana('${model.kibanaUrl}', delay.consumer)}}" height="200" width="100%"></iframe>
			</div>
		</div>
	</div>
	<div class="col-md-6">
		<div class="panel panel-success">
			<div class="panel-heading">
				<span class="label label-danger"> 消费时间 </span>
			</div>
			<div class="panel-body">
				<iframe style="border: 0" ng-src="{{get_consumed_process_kibana('${model.kibanaUrl}', delay.consumer)}}" height="200" width="100%"></iframe>
			</div>
		</div>
	</div>
</div>
<div class="modal fade" id="top-producer-modal" tabindex="-1" role="dialog" aria-labelledby="top-producer-label" aria-hidden="true">
	<div class="modal-dialog" style="width: 800px">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="top-producer-label">生产速度/分钟</h4>
			</div>
			<div class="modal-body">
				<div class="row">
					<div class="col-sm-6">
						<div class="panel panel-success">
							<div class="panel-heading">
								<span class="label label-danger">最快排行</span>
							</div>
							<div class="panel-body" style="height: 300px">
								<iframe ng-src="{{get_top_producer_kibana('${model.kibanaUrl}')}}" style="border: 0" width="100%" height="100%"></iframe>
							</div>
						</div>
					</div>
					<div class="col-sm-6">
						<div class="panel panel-success">
							<div class="panel-heading">
								<span class="label label-danger">最慢排行</span>
							</div>
							<div class="panel-body" style="height: 300px">
								<iframe ng-src="{{get_bottom_producer_kibana('${model.kibanaUrl}')}}" style="border: 0" width="100%" height="100%"></iframe>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

<div class="modal fade" id="top-consumer-modal" tabindex="-1" role="dialog" aria-labelledby="top-consumer-label" aria-hidden="true">
	<div class="modal-dialog" style="width: 800px">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="top-consumer-label">消费速度</h4>
			</div>
			<div class="modal-body">
				<div class="row">
					<div class="col-sm-6">
						<div class="panel panel-success">
							<div class="panel-heading">
								<span class="label label-danger">最快排行</span>
							</div>
							<div class="panel-body" style="height: 300px">
								<iframe ng-src="{{get_top_consumer_kibana('${model.kibanaUrl}')}}" style="border: 0" width="100%" height="100%"></iframe>
							</div>
						</div>
					</div>
					<div class="col-sm-6">
						<div class="panel panel-success">
							<div class="panel-heading">
								<span class="label label-danger">最慢排行</span>
							</div>
							<div class="panel-body" style="height: 300px">
								<iframe ng-src="{{get_bottom_consumer_kibana('${model.kibanaUrl}')}}" style="border: 0" width="100%" height="100%"></iframe>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

<div class="modal fade" id="top-process-modal" tabindex="-1" role="dialog" aria-labelledby="top-process-label" aria-hidden="true">
	<div class="modal-dialog" style="width: 900px">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="top-process-label">消费时间</h4>
			</div>
			<div class="modal-body">
				<div class="row">
					<div class="col-sm-4">
						<div class="panel panel-success">
							<div class="panel-heading">
								<span class="label label-danger">消费最慢排行</span>
							</div>
							<div class="panel-body" style="height: 300px">
								<iframe ng-src="{{get_process_kibana('${model.kibanaUrl}')}}" style="border: 0" width="100%" height="100%"></iframe>
							</div>
						</div>
					</div>
					<div class="col-sm-4">
						<div class="panel panel-success">
							<div class="panel-heading">
								<span class="label label-danger">最大投递ID</span>
							</div>
							<div class="panel-body" style="height: 300px">
								<iframe ng-src="{{get_max_did_kibana('${model.kibanaUrl}')}}" style="border: 0" width="100%" height="100%"></iframe>
							</div>
						</div>
					</div>
					<div class="col-sm-4">
						<div class="panel panel-success">
							<div class="panel-heading">
								<span class="label label-danger">最大ACK ID</span>
							</div>
							<div class="panel-body" style="height: 300px">
								<iframe ng-src="{{get_max_aid_kibana('${model.kibanaUrl}')}}" style="border: 0" width="100%" height="100%"></iframe>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

<div class="modal fade" id="top-latest-modal" tabindex="-1" role="dialog" aria-labelledby="top-latest-label" aria-hidden="true">
	<div class="modal-dialog" style="width: 1100px">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="top-latest-label">
					<b><span ng-bind="current_topic" style="text-transform: capitalize;"></span></b> 最新消息 &emsp;
					<button class="btn btn-xs btn-success" ng-click="refresh_latest()"><span class="glyphicon glyphicon-refresh"></span> 刷新</button>
				</h4>
			</div>
			<div class="modal-body">
				<table class="table table-bordered table-striped table-condensed table-hover">
					<thead>
						<tr>
							<th>#</th>
							<th>Producer</th>
							<th>Ref Key</th>
							<th>Attributes</th>
							<th>Codec</th>
							<th>Date</th>
							<th>Payload</th>
						</tr>
					</thead>
					<tbody>
						<tr ng-repeat="row in topic_latest">
							<td><span ng-bind="$index + 1"></span></td>
							<td><span ng-bind="row.rawMessage.producerIp"></span></td>
							<td><a href="" tooltip="{{row.rawMessage.refKey}}" tooltip-trigger="click">
									<span ng-bind="truncate(row.rawMessage.refKey, 20)"></span>
								</a></td>
							<td><a href="" tooltip="点击查看详情" ng-click="show_tree(row.rawMessage.refKey, row.attributesString)">
									<span ng-bind="truncate(row.attributesString, 20)"></span>
								</a></td>
							<td><span ng-bind="row.rawMessage.codecType"></span></td>
							<td><span ng-bind="row.rawMessage.creationDate | date:'MM-dd HH:mm:ss'"></span></td>
							<td><a tooltip="点击查看详情" href="" ng-click="show_tree(row.rawMessage.refKey, row.payloadString)">
									<span ng-bind="truncate(row.payloadString, 30)"></span>
								</a></td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</div>

<div class="modal fade" id="attr-view" tabindex="-1" role="dialog" aria-labelledby="attr-view-label" aria-hidden="true">
	<div class="modal-dialog" style="width: 1024px">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="attr-view-label">
					REF-KEY:
					<span ng-bind="current_refkey"></span>
				</h4>
			</div>
			<div class="modal-body">
				<div class="container-fluid">
					<div class="row">
						<div class="col-md-6">
							<h5>
								<label class="label label-primary">消息结构</label>
							</h5>
							<div id="data-tree"></div>
						</div>
						<div class="col-md-6">
							<h5>
								<label class="label label-primary">源消息</label>
							</h5>
							<pre style="max-height: 600px; overflow: scroll;">{{current_attr_json | json:2}}</pre>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>