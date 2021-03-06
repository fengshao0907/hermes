/**
 * Created by ske on 2015/6/1.
 */

dashtopic
		.controller(
				"dash-topic-controller",
				function($scope, $http, $resource, $compile, $sce, DashtopicService) {
					$scope.topic_briefs = [];
					$scope.main_board_content = {};
					$scope.current_topic = "";
					$scope.topic_delays = [];
					$scope.topic_latest = [];

					DashtopicService.update_topic_briefs();

					$scope.$watch(DashtopicService.get_topic_briefs, function() {
						$scope.topic_briefs = DashtopicService.get_topic_briefs();
					});

					$scope.$watch(DashtopicService.get_current_topic, function() {
						$scope.current_topic = DashtopicService.get_current_topic();
						DashtopicService.update_topic_delays($scope.current_topic);
						DashtopicService.update_topic_latest($scope.current_topic);
					});

					$scope.$watch(DashtopicService.get_topic_delays, function() {
						$scope.topic_delays = DashtopicService.get_topic_delays();
					});

					$scope.$watch(DashtopicService.get_topic_latest, function() {
						$scope.topic_latest = DashtopicService.get_topic_latest();
					});

					$scope.$watch(DashtopicService.get_main_board_content, function() {
						$scope.main_board_content = DashtopicService.get_main_board_content($scope.current_topic);
						$("#main_board").html($scope.main_board_content);
						$compile($("#main_board"))($scope);
					});

					$scope.nav_select = function(topic_brief) {
						DashtopicService.update_main_board_content(topic_brief.topic);
						$("#main_board").html($scope.main_board_content);
						$compile($("#main_board"))($scope);
					};

					$scope.get_produced_kibana = function(kibanaUrl) {
						var url = kibanaUrl
								+ "/#/visualize/edit/Message-Received?embed&_g=(refreshInterval:(display:'10%20seconds',pause:!f,section:1,value:5000),time:(from:now-1h,mode:quick,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:'eventType:Message.Received')),vis:(aggs:!((id:'1',params:(),schema:metric,type:count),(id:'3',params:(customInterval:'2h',extended_bounds:(),field:'@timestamp',interval:auto,min_doc_count:1),schema:segment,type:date_histogram),(id:'2',params:(filters:!((input:(query:(query_string:(analyze_wildcard:!t,query:'datas.topic:"
								+ $scope.current_topic
								+ "')))))),schema:group,type:filters)),listeners:(),params:(addLegend:!t,addTimeMarker:!f,addTooltip:!t,defaultYExtents:!f,mode:stacked,scale:linear,setYExtents:!f,shareYAxis:!t,times:!(),yAxis:()),type:histogram))";
						return $sce.trustAsResourceUrl(url);
					};

					$scope.get_consumed_kibana = function(kibanaUrl, consumer) {
						var url = kibanaUrl
								+ "/#/visualize/edit/Message-Acked?embed&_g=(refreshInterval:(display:'10%20seconds',pause:!f,section:1,value:10000),time:(from:now-1h,mode:quick,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:'eventType:Message.Acked')),vis:(aggs:!((id:'1',params:(),schema:metric,type:count),(id:'2',params:(customInterval:'2h',extended_bounds:(),field:'@timestamp',interval:auto,min_doc_count:1),schema:segment,type:date_histogram),(id:'3',params:(filters:!((input:(query:(query_string:(analyze_wildcard:!t,query:'datas.groupId:"
								+ consumer
								+ "')))))),schema:group,type:filters)),listeners:(),params:(addLegend:!t,addTimeMarker:!f,addTooltip:!t,defaultYExtents:!f,mode:stacked,scale:linear,setYExtents:!f,shareYAxis:!t,times:!(),yAxis:()),type:histogram))";
						return $sce.trustAsResourceUrl(url);
					};

					$scope.get_consumed_process_kibana = function(kibanaUrl, consumer) {
						var url = kibanaUrl
								+ "/#/visualize/edit/Process-Time?embed&_g=(refreshInterval:(display:'5%20seconds',pause:!f,section:1,value:5000),time:(from:now-1h,mode:quick,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:'*')),vis:(aggs:!((id:'1',params:(field:datas.processTime),schema:metric,type:avg),(id:'2',params:(customInterval:'2h',extended_bounds:(),field:'@timestamp',interval:auto,min_doc_count:1),schema:segment,type:date_histogram),(id:'3',params:(filters:!((input:(query:(query_string:(analyze_wildcard:!t,query:'datas.groupId:"
								+ consumer
								+ "%20AND%20datas.topic:"
								+ $scope.current_topic
								+ "')))))),schema:group,type:filters)),listeners:(),params:(addLegend:!f,addTimeMarker:!f,addTooltip:!t,defaultYExtents:!f,mode:stacked,scale:linear,setYExtents:!f,shareYAxis:!t,times:!(),yAxis:()),type:histogram))";
						return $sce.trustAsResourceUrl(url);
					};

					$scope.get_top_producer_kibana = function(kibanaUrl) {
						var url = kibanaUrl
								+ "/#/visualize/edit/Top-Producer?embed&_g=(filters:!(),refreshInterval:(display:'10%20seconds',pause:!f,section:1,value:10000),time:(from:now-1m,mode:relative,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:'eventType:Message.Received%20AND%20datas.topic:"
								+ $scope.current_topic
								+ "')),vis:(aggs:!((id:'1',params:(),schema:metric,type:count),(id:'2',params:(field:datas.producerIp.raw,order:desc,orderBy:'1',size:100),schema:bucket,type:terms)),listeners:(),params:(perPage:10,showMeticsAtAllLevels:!f,showPartialRows:!f),type:table))";
						return $sce.trustAsResourceUrl(url);
					};

					$scope.get_bottom_producer_kibana = function(kibanaUrl) {
						var url = kibanaUrl
								+ "/#/visualize/edit/Bottom-Producer?embed&_g=(refreshInterval:(display:'10%20seconds',pause:!f,section:1,value:10000),time:(from:now-1m,mode:relative,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:'eventType:Message.Received%20AND%20datas.topic:"
								+ $scope.current_topic
								+ "')),vis:(aggs:!((id:'1',params:(),schema:metric,type:count),(id:'2',params:(field:datas.producerIp.raw,order:asc,orderBy:'1',size:100),schema:bucket,type:terms)),listeners:(),params:(perPage:10,showMeticsAtAllLevels:!f,showPartialRows:!f),type:table))";
						return $sce.trustAsResourceUrl(url);
					};

					$scope.get_top_consumer_kibana = function(kibanaUrl) {
						var url = kibanaUrl
								+ "/#/visualize/edit/Top-Consumer?embed&_g=(refreshInterval:(display:'10%20seconds',pause:!f,section:1,value:10000),time:(from:now-1m,mode:relative,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:'eventType:Message.Acked%20AND%20datas.topic:"
								+ $scope.current_topic
								+ "')),vis:(aggs:!((id:'1',params:(),schema:metric,type:count),(id:'2',params:(field:datas.consumerIp.raw,order:desc,orderBy:'1',size:100),schema:bucket,type:terms)),listeners:(),params:(perPage:10,showMeticsAtAllLevels:!f,showPartialRows:!f),type:table))";
						return $sce.trustAsResourceUrl(url);
					};

					$scope.get_bottom_consumer_kibana = function(kibanaUrl, consumer) {
						var url = kibanaUrl
								+ "/#/visualize/edit/Bottom-Consumer?embed&_g=(refreshInterval:(display:'10%20seconds',pause:!f,section:1,value:10000),time:(from:now-1m,mode:relative,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:'eventType:Message.Acked%20AND%20datas.topic:"
								+ $scope.current_topic
								+ "')),vis:(aggs:!((id:'1',params:(),schema:metric,type:count),(id:'2',params:(field:datas.consumerIp.raw,order:asc,orderBy:'1',size:100),schema:bucket,type:terms)),listeners:(),params:(perPage:10,showMeticsAtAllLevels:!f,showPartialRows:!f),type:table))";
						return $sce.trustAsResourceUrl(url);
					};

					$scope.get_process_kibana = function(kibanaUrl) {
						var url = kibanaUrl
								+ "/#/visualize/edit/Process-Slowest?embed&_g=(refreshInterval:(display:'1%20minute',pause:!f,section:2,value:60000),time:(from:now-1h,mode:quick,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:'datas.topic:"
								+ $scope.current_topic
								+ "')),vis:(aggs:!((id:'1',params:(field:datas.processTime),schema:metric,type:avg),(id:'2',params:(field:datas.groupId.raw,order:desc,orderBy:'1',size:10),schema:bucket,type:terms)),listeners:(),params:(perPage:10,showMeticsAtAllLevels:!f,showPartialRows:!f),type:table))";
						return $sce.trustAsResourceUrl(url);
					};

					$scope.get_max_did_kibana = function(kibanaUrl) {
						var url = kibanaUrl
								+ "/#/visualize/edit/Max-Delivered-MID?embed&_g=(refreshInterval:(display:'1%20minute',pause:!f,section:2,value:60000),time:(from:now-7d,mode:quick,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:'eventType:Message.Delivered%20AND%20datas.topic:"
								+ $scope.current_topic
								+ "')),vis:(aggs:!((id:'1',params:(field:datas.msgId),schema:metric,type:max),(id:'2',params:(field:datas.groupId.raw,order:desc,orderBy:'1',size:5),schema:bucket,type:terms)),listeners:(),params:(perPage:10,showMeticsAtAllLevels:!f,showPartialRows:!f),type:table))";
						return $sce.trustAsResourceUrl(url);
					};

					$scope.get_max_aid_kibana = function(kibanaUrl) {
						var url = kibanaUrl
								+ "/#/visualize/edit/Max-Acked-MID?embed&_g=(refreshInterval:(display:'1%20minute',pause:!f,section:2,value:60000),time:(from:now-7d,mode:quick,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:'eventType:Message.Acked%20AND%20datas.topic:"
								+ $scope.current_topic
								+ "')),vis:(aggs:!((id:'1',params:(field:datas.msgId),schema:metric,type:max),(id:'2',params:(field:datas.groupId.raw,order:desc,orderBy:'1',size:20),schema:bucket,type:terms)),listeners:(),params:(perPage:10,showMeticsAtAllLevels:!f,showPartialRows:!f),type:table))";
						return $sce.trustAsResourceUrl(url);
					};

					$scope.normalize_delay = function(delay) {
						if (delay < 0) {
							delay = 0;
						}
						return delay / 1000;
					};

					$scope.truncate = function(raw, size) {
						raw = raw.replace(/\\"/g, '"');
						if (raw.length <= size) {
							return raw;
						}
						return raw.substring(0, size / 2) + " ... " + raw.substring(raw.length - size / 2);
					};

					$scope.refresh_latest = function() {
						DashtopicService.update_topic_latest($scope.current_topic);
					};

					function format_tree(obj) {
						var data = [];
						if (obj instanceof Object) {
							for ( var key in obj) {
								var node = {};
								var value = obj[key];
								node.text = key;
								if (value instanceof Object) {
									node.nodes = format_tree(value);
								} else {
									node.nodes = [ {
										text : value
									} ];
								}
								data.push(node);
							}
						} else {
							data.push({
								text : obj
							});
						}
						return data;
					}

					$scope.show_tree = function(ref_key, json_str) {
						$scope.current_refkey = ref_key;
						json_str = json_str.replace(/\\"/g, '"');
						if (json_str[0] == '"' && json_str[json_str.length - 1] == '"') {
							json_str = json_str.substring(1, json_str.length - 1);
						}
						var obj = JSON.parse(json_str);
						$scope.current_attr_json = obj;
						$("#data-tree").treeview({
							data : format_tree(obj),
							levels : 1
						});
						$("#attr-view").modal('show');
					};
				});