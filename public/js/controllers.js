'use strict';

/* Controllers */

var mppControllers = angular.module('mppControllers', []);

mppControllers.controller('ProductListCtrl', ['$scope', 'Products',
    function($scope, Products) {
        $scope.friends = [];
        $scope.socketStatus = 'not started.  cccc';

        $scope.dataSocket = null;
        var uid = null;

        $scope.requestUpdate = function() {
            console.debug('1. requesting products');
            var data = Products.get(function() {
                console.debug('[' + data.uid + '] 2. product response received.');
                setUid(data.uid);
                openUpdateConnection(data.location);
            });
        };

        function setUid(newUid) {
            uid = newUid;
////            $scope.apply();
        }
//
//        function setSocketStatus(newStatus) {
//            $scope.socketStatus = newStatus;
//            $scope.apply();
//        }
//
        function openUpdateConnection(socketLocation) {
            if ($scope.dataSocket) {
                $scope.dataSocket.close();
            }
            $scope.dataSocket = new WebSocket(socketLocation);
            $scope.dataSocket.onopen = function(evt) { dataSocketOpened(evt) };
            $scope.dataSocket.onclose = function(evt) { dataSocketClosed(evt) };
            $scope.dataSocket.onmessage = function(evt) { onMessage(evt) };
            $scope.dataSocket.onerror = function(evt) { onError(evt) };
        }
//
        function dataSocketOpened(evt) {
            console.debug('[' + uid + '] 3. socket connected.');
            doSend('{ "query": "' + uid +'" }');
        };
//
        function dataSocketClosed(evt) {
            console.info('socket closed.');
//            setSocketStatus('[' + uid + ']closed.');
        };

        function onMessage(evt) {
            console.log('[' + uid + '] RECV: ' + evt.data);
//            // $scope.dataSocket.close();
////            websocket.close();
        };
//
        function onError(evt) {
            console.error('RECV: ' + evt.data);
//            setSocketStatus('[' + uid + ']error: ' + evt.data);
        };
//
        function doSend(message) {
            console.debug('[' + uid + '] 4. sending: [' + message + ']');
            $scope.dataSocket.send(message);
        }
//
//        function createFriend() {
//          return { id: $scope.friends.length, name: "Name_" + $scope.friends.length };
//        };
//
//        function getFriends() {
//            $scope.friends.push({ id: $scope.friends.length, name: "Name_" + $scope.friends.length });
//            return $scope.friends;
//        };
    }
]);
