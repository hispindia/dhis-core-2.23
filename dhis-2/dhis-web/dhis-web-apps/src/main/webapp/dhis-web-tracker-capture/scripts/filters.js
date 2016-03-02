'use strict';

/* Filters */

var trackerCaptureFilters = angular.module('trackerCaptureFilters', [])

.filter('eventListFilter', function($filter){    
    
    return function(pagedList, fullList, filterText){

        if(!pagedList ){
            return;
        }
        
        if(!filterText){
            return pagedList;
        }        
           
        var filteredData = fullList;
        filteredData = $filter('filter')(filteredData, filterText);
        return filteredData;
    }; 
});