'use strict';

var mppApp = angular.module('mpp', [
  'ngRoute',
  'mppControllers',
  'mppServices'
]);

mppApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/search', {
        templateUrl: 'assets/partials/product-list.html',
        controller: 'ProductListCtrl'
      }).
      otherwise({
        redirectTo: '/search'
      });
  }]);

String.prototype.trunc = String.prototype.trunc ||
      function(n){
          return (this.length > n) ? this.substr(0,n-1)+'&hellip;' : this;
      };
