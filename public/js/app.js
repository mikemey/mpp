'use strict';

String.prototype.trunc = String.prototype.trunc ||
      function(n){
          return (this.length > n) ? this.substr(0,n-1)+'&hellip;' : this;
      };

var mppApp = angular.module('mpp', [
  'ngRoute',
  'mppControllers'
]);

mppApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/products', {
        templateUrl: 'assets/partials/product-list.html',
        controller: 'ProductListCtrl'
      }).
      otherwise({
        redirectTo: '/products'
      });
  }]);
