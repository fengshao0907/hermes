function filter_consumer_rows(rows, filter, table_state) {
	rows = table_state.search.predicateObject ? filter('filter')(rows,
			table_state.search.predicateObject) : rows;
	if (table_state.sort.predicate) {
		rows = filter('orderBy')(rows, table_state.sort.predicate,
				table_state.sort.reverse);
	}
	return rows;
}

function reload_table(scope, data) {
	scope.src_consumers = data;
	scope.consumer_rows = scope.src_consumers;
}

angular.module('hermes-consumer', [ 'ngResource', 'smart-table' ]).controller(
		'consumer-controller',
		[
				'$scope',
				'$filter',
				'$resource',
				function(scope, filter, resource) {
					consumer_resource = resource(
							'/api/consumers/:topic/:consumer', {}, {
								'add_consumer' : {
									method:'POST',
									url:'/api/consumers/add/multiple'
								}
							});
					meta_resource = resource('/api/meta', {}, {
						'get_topic_names' : {
							method : 'GET',
							isArray : true,
							url : '/api/meta/topics/names'
						}
					});

					scope.is_loading = true;
					scope.src_consumers = [];
					scope.consumer_rows = [];
					scope.new_consumer = {
						orderedConsume : true,
						topicNames : []
					};

					scope.order_opts = [ true, false ];

					meta_resource.get_topic_names({}, function(result) {
						var result = new Bloodhound({
							local : result,
							datumTokenizer : Bloodhound.tokenizers.whitespace,
							queryTokenizer : Bloodhound.tokenizers.whitespace,
						});
						$('#inputTopicName').on(('tokenfield:createdtoken'),function(e){
							var topicList = $('#inputTopicName').tokenfield('getTokens');
							var idx;
							for ( idx =0;idx<(topicList.length-1); idx++){
								if(e.attrs.value == topicList[idx].value){
									topicList.pop();
									$('#inputTopicName').tokenfield('setTokens', topicList);
								}
							}
							for( idx =0;idx< result.local.length;idx++){
								if(e.attrs.value == result.local[idx]){
									break;
								}
							}
							if(idx==result.local.length){
								topicList.pop();
								$('#inputTopicName').tokenfield('setTokens', topicList);
							}
						}).tokenfield({
							typeahead : [ {
								hint : true,
								highlight : true,
								minLength : 1
							}, {
								name : 'topics',
								source : result 
							} ],
							beautify : false
						});
					});

					scope.get_consumers = function get_consumers(table_state) {
						consumer_resource.query().$promise.then(function(
								query_result) {
							scope.src_consumers = query_result;
							scope.consumer_rows = filter_consumer_rows(
									scope.src_consumers, filter, table_state);
							scope.is_loading = false;
						});
					};
					scope.newTopicNames = "";
					scope.add_consumer = function add_consumer(new_consumer) {
						new_consumer.topicNames = scope.newTopicNames
								.split(",");
						consumer_resource.add_consumer({}, new_consumer, function(save_result) {
							console.log(save_result);
							consumer_resource.query().$promise.then(function(
									query_result) {
								reload_table(scope, query_result);
								show_op_info.show("新增 consumer "
										+ new_consumer.groupName + " for topoics ("
										+ new_consumer.topicNames + ") 成功!", true);
							});
						}, function(error_result) {
							show_op_info.show("新增 consumer " + new_consumer.groupName
									+ " for topoics (" + new_consumer.topicNames + ") 失败! "
									+ error_result.data, false);
						});
					};

					scope.del_consumer = function del_consumer(topicName,
							groupName) {
						bootbox.confirm("确认删除 Consumer: " + groupName + "("
								+ topicName + ")?", function(result) {
							if (result) {
								consumer_resource.remove({
									topic : topicName,
									consumer : groupName
								}, function(remove_result) {
									consumer_resource.query({},
											function(query_result) {
												reload_table(scope,
														query_result);
												show_op_info.show("删除成功: "
														+ groupName + "("
														+ topicName + ")",
														true);
											});
								}, function(error_result) {
									show_op_info.show("删除失败: " + groupName
											+ "(" + topicName + "), "
											+ error_result.data, false);
								});
							}
						});
					};
				} ]);
