'use strict';

/* Controllers */

var mppControllers = angular.module('mppControllers', []);

mppControllers.controller('ProductListCtrl', ['$scope', 'Products',
    function($scope, Products) {
        $scope.products = [];
        $scope.socketStatus = 'not started.';

        $scope.dataSocket = null;
        var uid = null;

        $scope.productSearch = function() {
            $scope.socketStatus = 'requesting products...';
            $scope.products = [];
            var data = Products.get(function() {
                setUid(data.uid);
                openUpdateConnection(data.location);
            });
        };

        function setUid(newUid) {
            uid = newUid;
        }

        function setSocketStatus(newStatus) {
            $scope.socketStatus = newStatus;
            $scope.$apply();
        }

        function addProducts(products) {
            Array.prototype.push.apply($scope.products, products);
            $scope.$apply();
        }

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

        function dataSocketOpened(evt) {
            setSocketStatus('connected');
            doSend('{ "query": "' + uid +'" }');
        };

        function dataSocketClosed(evt) {
            setSocketStatus('closed');
        };

        function onMessage(evt) {
            addProducts($.parseJSON(evt.data));
        };

        function onError(evt) {
            setSocketStatus('error: ' + evt.data);
        };

        function doSend(message) {
            $scope.dataSocket.send(message);
        }
    }
]);
