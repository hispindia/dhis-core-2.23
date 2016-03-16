'use strict';

/* Controllers */
var d2Controllers = angular.module('d2Controllers', [])

//Controller for column show/hide
.controller('ColumnDisplayController', 
    function($scope, 
            $modalInstance, 
            hiddenGridColumns,
            gridColumns){
    
    $scope.gridColumns = gridColumns;
    $scope.hiddenGridColumns = hiddenGridColumns;
    
    $scope.close = function () {
      $modalInstance.close($scope.gridColumns);
    };
    
    $scope.showHideColumns = function(gridColumn){
       
        if(gridColumn.show){                
            $scope.hiddenGridColumns--;            
        }
        else{
            $scope.hiddenGridColumns++;            
        }      
    };    
})

//controller for dealing with google map
.controller('MapController',
        function($scope, 
                $modalInstance,
                CurrentSelection,
                DHIS2URL,                
                location) {
    
    $scope.home = function(){        
        window.location = DHIS2URL;
    };
    
    $scope.location = location;
    
    $scope.close = function () {
        $modalInstance.close();
    };
    
    $scope.captureCoordinate = function(){
        $scope.location = CurrentSelection.getLocation();
        $modalInstance.close($scope.location);
    };
        })

////Controller for audit history
.controller('AuditHistoryController', function ($scope, $modalInstance, $modal, AuditHistoryDataService, DateUtils,
                                                    eventId, dataType, nameIdMap) {

    $scope.itemList = {};

    $scope.model = {type: dataType};

    $scope.close = function () {
        $modalInstance.close();
    };

    $scope.$watch("itemList", function (itemList) {
        $scope.listInDisplay = itemList;
    });

    $scope.searchTheList = function () {
        var searchStr = $scope.model.searchText;
        var filteredItemList = {};
        for (var item in $scope.itemList) {
            for (var index = 0; index < $scope.itemList[item].length; index++) {
                var element = $scope.itemList[item][index];
                if (!isSubString(item, searchStr) && !isSubString(element.created, searchStr) && !isSubString(element.value, searchStr) && !isSubString(element.modifiedBy, searchStr)) {
                } else {
                    if (!filteredItemList[item]) {
                        filteredItemList[item] = [];
                    }
                    filteredItemList[item].push(element);
                }
            }
        }
        $scope.listInDisplay = filteredItemList;
    }
    $scope.isListEmpty = function() {
        return Object.keys($scope.listInDisplay).length === 0;
    }

    function isSubString(str, subStr) {
        return str.toLowerCase().indexOf(subStr.toLowerCase()) > -1;
    }


    AuditHistoryDataService.getAuditHistoryData(eventId, dataType).then(function (data) {

        $scope.itemList = {};

        var reponseData = data.trackedEntityDataValueAudits ? data.trackedEntityDataValueAudits :
            data.trackedEntityAttributeValueAudits ? data.trackedEntityAttributeValueAudits : null;
        if (reponseData) {
            for (var index = 0; index < reponseData.length; index++) {

                var dataValue = reponseData[index];

                /*The true/false values are displayed as Yes/No*/
                if (dataValue.value === "true") {
                    dataValue.value = "Yes";
                } else if (dataValue.value === "false") {
                    dataValue.value = "No";
                }
                if (dataType === "attribute") {
                    if (nameIdMap[dataValue.trackedEntityAttribute.id] && nameIdMap[dataValue.trackedEntityAttribute.id].displayName) {
                        var attributeName = nameIdMap[dataValue.trackedEntityAttribute.id].displayName;
                        if (!$scope.itemList[attributeName]) {
                            $scope.itemList[attributeName] = [];
                        }
                        $scope.itemList[attributeName].push({
                            created: DateUtils.formatToHrsMinsSecs(dataValue.created), value: dataValue.value,
                            auditType: dataValue.auditType, modifiedBy: dataValue.modifiedBy
                        });
                    }
                } else if (dataType === "dataElement") {
                    if (nameIdMap[dataValue.dataElement.id] && nameIdMap[dataValue.dataElement.id].dataElement) {
                        var dataElementName = nameIdMap[dataValue.dataElement.id].dataElement.displayName;
                        if (!$scope.itemList[dataElementName]) {
                            $scope.itemList[dataElementName] = [];
                        }
                        $scope.itemList[dataElementName].push({
                            created: DateUtils.formatToHrsMinsSecs(dataValue.created), value: dataValue.value,
                            auditType: dataValue.auditType, modifiedBy: dataValue.modifiedBy
                        });
                    }
                }
            }
        }
    });
});



