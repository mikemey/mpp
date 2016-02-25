'use strict';

/* Controllers */

var mppControllers = angular.module('mppControllers', []);

mppControllers.controller('ProductListCtrl', ['$scope', '$timeout',
  function($scope, $timeout) {
    $scope.products = [];
    $scope.raw = '';

    function addProduct(scope, product) {
        var existing = scope.products;
        console.log("existing: " + existing );
        console.log("ex len: " + existing.length );
        existing.push(product)
        scope.products = existing;
        scope.$apply();
        console.log("   added: " + product.name);
        console.log("products: " + scope.products.length);
    };

    function pushCard(cname) {
        $timeout(function() {
            $scope.products.push({ name: cname, offerUrl: 'http://www.google.com', desc: 'Lorem Ipsum', imgUrl: 'http://www.credit-card-logos.com/images/multiple_credit-card-logos-2/credit_card_logos_29.gif' });
            $scope.raw = $scope.raw + ' ' + cname;
            console.log("raw: " + $scope.raw);
        }, 1750);
    };

    $scope.pushCard = pushCard

    function doneMessage() {
        $('.spinner').text('done!');
    };

    $scope.raw = 'start';

    $scope.queueUpdateRequest = function(serverUrl) {
        $timeout(function() {
            $scope.sendUpdateRequest(serverUrl);
        }, 750);
    };

    $scope.sendUpdateRequest = function(serverUrl) {
        console.log("function push.");
        pushCard('function');
        $.ajax({
          url  : serverUrl,
          type : 'GET',
        }).done(function(data, statusText, xhr){
          var status = xhr.status;
          console.log("CALLBACK push.");
          pushCard('CALLBACK');
//          if(status == 200) {
//                var json = $.parseJSON(data);
//                for(var i = 0; i < json.length; i++) {
//                    addProduct($scope, json[i]);
//                }
//                $scope.queueUpdateRequest(serverUrl);
//          } else if (status == 202) {
//                var json = $.parseJSON(data);
//                $('.spinner').text('querying...');
//                $scope.queueUpdateRequest(json.location);
//          } else if (status == 205) {
//                doneMessage();
//          }
        });
    };


    $scope.requestProducts = function() {
        $scope.queueUpdateRequest('/api/products');
    };
  }]);
