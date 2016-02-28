'use strict';

var mppControllers = angular.module('mppControllers', []);

mppControllers.controller('ProductListCtrl', ['$scope', 'Products',
    function($scope, Products) {
        $scope.products = [];
        $scope.socketStatus = 'not started.';
        $scope.dataSocket = null;
        $scope.uid = null;
        $scope.counter = 0;
        $scope.total = 0;

        function resetData() {
            $scope.startTime = null;
            $scope.endTime = null;
            $scope.products = [];
        }

        $scope.productSearch = function() {
            resetData();
            $scope.socketStatus = 'requesting products...';
            var data = Products.get(function() {
                $scope.uid = data.uid;
                openUpdateConnection(data.location);
            });
        };

        function setSocketStatus(newStatus) {
            $scope.socketStatus = newStatus;
            $scope.$apply();
        }

        function setStartTime() {
            $scope.startTime = timeString();
            $scope.$apply();
        }

        function setEndTime() {
            $scope.endTime = timeString();
            $scope.$apply();
        }

        function timeString() {
            var d = new Date();
            return pad(d.getHours(), 2) + ':' + pad(d.getMinutes(), 2) + ':'
                 + pad(d.getSeconds(), 2) + '.' + pad(d.getMilliseconds(), 3);
        }

        function pad(num, size) {
            var s = "00" + num;
            return s.substr(s.length - size);
        }

        function addProducts(products) {
            Array.prototype.push.apply($scope.products, products);
            $scope.$apply();
            $('.loading').each(function(index, element) {
                setTimeout(function(){
                    element.classList.remove('loading');
                }, index * 300);
            });
        }

        function updateCounter(counter, total) {
            $scope.counter = counter;
            $scope.total = total;
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
            setStartTime();
            doSend('{ "query": "' + $scope.uid +'" }');
        }

        function onMessage(evt) {
            setSocketStatus('receiving products...');
            var updateData = $.parseJSON(evt.data);
            updateCounter(updateData.counter, updateData.total);
            addProducts(updateData.products);
        }

        function dataSocketClosed(evt) {
            setSocketStatus('closed');
            setEndTime();
        }

        function onError(evt) {
            setSocketStatus('error: ' + evt.data);
        }

        function doSend(message) {
            $scope.dataSocket.send(message);
        }
    }
]);
