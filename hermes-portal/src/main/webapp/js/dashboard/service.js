var dashtopic = angular.module("dash-topic", [ 'ngResource', 'ui.bootstrap', 'smart-table' ]);

dashtopic.service("DashtopicService", [ "$http", "$resource", function($http, $resource) {
	var topic_briefs = [];
	var main_board_content = {};
	var topic_delays = [];
	var current_topic = "";

	var monitor_resource = $resource("/api/monitor/", {}, {
		get_topics : {
			method : "GET",
			isArray : true,
			url : "/api/monitor/brief/topics"
		},
		get_topic_delay : {
			method : "GET",
			isArray : true,
			url : "/api/monitor/detail/topics/:topic/delay"
		}
	});

	function do_update_main_board(topic) {
		$http.get('/console/dashboard?op=topic-detail&topic=' + topic).success(function(data, status, headers, config) {
			main_board_content = data;
		}).error(function(data, status, headers, config) {
			console.log(data);
		});
		current_topic = topic;
	}

	return {
		get_topic_briefs : function() {
			return topic_briefs;
		},
		get_main_board_content : function() {
			return main_board_content;
		},
		update_topic_briefs : function() {
			monitor_resource.get_topics({}, function(data) {
				topic_briefs = data;
				if (topic_briefs.length > 0) {
					do_update_main_board(topic_briefs[0].topic);
				}
			});
		},
		update_main_board_content : function(topic) {
			do_update_main_board(topic);
		},
		get_topic_delays : function() {
			return topic_delays;
		},
		update_topic_delays : function(topic) {
			monitor_resource.get_topic_delay({
				'topic' : topic
			}, function(data) {
				console.log(data);
				topic_delays = data;
			});
		},
		get_current_topic : function() {
			return current_topic;
		}
	}
} ]);