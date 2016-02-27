'use strict';

/* Controllers */

var mppControllers = angular.module('mppControllers', []);

mppControllers.controller('ProductListCtrl', ['$scope', 'Products',
    function($scope, Products) {
        $scope.friends = [];
        var dataSocket = null;
        $scope.socketStatus = 'not started.';

        $scope.requestUpdate = function() {
            var data = Products.get(function() {
                openUpdateConnection(data.location);
            });
        };

        function setSocketStatus(newStatus) {
            $scope.socketStatus = newStatus;
            $scope.apply();
        }

        function openUpdateConnection(socketLocation) {
            if (dataSocket) {
                dataSocket.close();
            }
            dataSocket = new WebSocket(socketLocation);
            dataSocket.onopen = function(evt) { dataSocketOpened(evt) };
            dataSocket.onclose = function(evt) { dataSocketClosed(evt) };
            dataSocket.onmessage = function(evt) { onMessage(evt) };
            dataSocket.onerror = function(evt) { onError(evt) };
        }

        function dataSocketOpened(evt) {
            setSocketStatus('receiving data updates...');
            $scope.$apply();
            doSend("READY");
        };

        function dataSocketClosed(evt) {
            setSocketStatus('done.');
        };

        function onMessage(evt) {
            console.log('RESPONSE: ' + evt.data);
            dataSocket.close();
//            websocket.close();
        };

        function onError(evt) {
            setSocketStatus('error: ' + evt.data);
        };

        function doSend(message) {
            dataSocket.send(message);
        }

        function createFriend() {
          return { id: $scope.friends.length, name: "Name_" + $scope.friends.length };
        };

        function getFriends() {
            $scope.friends.push({ id: $scope.friends.length, name: "Name_" + $scope.friends.length });
            return $scope.friends;
        };
    }
]);
