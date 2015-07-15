angular.module('hermes-subscription', [ 'ngResource', 'ui.bootstrap', 'xeditable' ]).run(function(editableOptions) {
	editableOptions.theme = 'bs3';
}).controller('subscription-controller', [ '$scope', '$filter', '$resource', function(scope, filter, resource) {
	var subscription_resource = resource('/api/subscriptions/', {}, {
		get_topic_names : {
			method : 'GET',
			isArray : true,
			url : '/api/meta/topics/names'
		},
		get_consumer_names : {
			method : 'GET',
			isArray : true,
			url : '/api/consumers/:topic'
		},
		get_subscribers : {
			method : 'GET',
			isArray : true
		},
		delete_subscriber : {
			method : 'DELETE',
			url : '/api/subscriptions/:id'
		}
	});

	scope.selected = {};
	scope.subscriber = {};

	scope.topic_names = subscription_resource.get_topic_names({}, function(data) {
		scope.topic_names = data;
	});

	scope.consumer_names = subscription_resource.get_consumer_names({}, function(data) {
		scope.consumer_names = collect_schemas(data, 'groupName', true);
	});

	scope.subscribers = subscription_resource.get_subscribers({}, function(data) {
		scope.subscribers = data;
	});

	scope.add_row = function add_row() {
		scope.inserted = {
			name : undefined,
			topic : undefined,
			group : undefined,
			endpoints : undefined
		};
		scope.subscribers.push(scope.inserted);
	};

	scope.add_subscriber = function add_subscriber(subscriber) {
		subscription_resource.save(subscriber, function(data) {
			scope.subscribers = subscription_resource.get_subscribers({}, function(data) {
				scope.subscribers = data;
			});

			show_op_info.show("保存Subscription成功", true);
		}, function(error_result) {
			show_op_info.show("保存失败: " + error_result.data, false);
		});
	};

	scope.del_row = function del_row(subscriber) {
		bootbox.confirm("确认删除Subscription?", function(result) {
			if (result) {
				subscription_resource.delete_subscriber({
					id : subscriber.id
				}, function(data) {
					show_op_info.show("删除成功", true);
				}, function(error_result) {
					show_op_info.show("删除失败: " + error_result.data, false);
				});
				var index = scope.subscribers.indexOf(subscriber);
				if (index > -1) {
					scope.subscribers.splice(index, 1);
				}
			}
		});
	}
} ]);
