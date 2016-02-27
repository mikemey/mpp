'use strict';

/* Controllers */

var mppControllers = angular.module('mppControllers', []);

mppControllers.controller('ProductListCtrl', ['$scope',
function($scope) {
  $scope.greeting = 'DDD';

  $scope.requestProducts =function() {
   return $scope.$apply(function() {
    console.log('add ' + $scope.greeting);
    $scope.greeting = $scope.greeting + ' - ' + $scope.greeting;
    console.log('add ' + $scope.greeting);
    console.log('up to here ');
  });
  };

  $scope.greeting = $scope.greeting + ' - ' + $scope.greeting;
}]);

//mppControllers.controller('ProductListCtrl', ['$scope',
//  function($scope) {
////    $scope.products = [];
//    $scope.txt = 'start';

    //$scope.products.push({ name: 'first', offerUrl: 'http://www.google.com', desc: 'Lorem Ipsum', imgUrl: 'http://www.credit-card-logos.com/images/multiple_credit-card-logos-2/credit_card_logos_29.gif' });

//    function requestProducts() {
//        $scope.$apply($scope.addProduct('second'));
////        addProduct({
////            name: 'second',
////            offerUrl: 'http://www.google.com',
////            desc: 'Lorem Ipsum',
////            imgUrl: 'http://www.credit-card-logos.com/images/multiple_credit-card-logos-2/credit_card_logos_29.gif'
////        });
////        $scope.queueUpdateRequest('/api/products');
//    };
//
//    $scope.addProduct = function() {
//        console.log('before: ' + $scope.txt);
//        $scope.txt = $scope.txt + ' ' + product;
//        console.log('after : ' + $scope.txt);
//    };
//    $scope.addProduct('first');
//  }]);
