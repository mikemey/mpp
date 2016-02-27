'use strict';

var productsUrl = '/api/products';

var mppApp = angular.module('mppServices', ['ngResource']);
mppApp.factory('Products', ['$resource', function($resource) {
    return $resource(productsUrl);
}]);
