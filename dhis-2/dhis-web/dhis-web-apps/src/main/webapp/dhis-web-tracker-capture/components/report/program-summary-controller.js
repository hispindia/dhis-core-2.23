trackerCapture.controller('ProgramSummaryController',
        function($scope,
                $modal,
                DateUtils,
                EventUtils,
                TEIService,
                TEIGridService,
                TranslationService,
                AttributesFactory,
                ProgramFactory,
                DHIS2EventFactory,
                storage) {

    TranslationService.translate();
    
    $scope.today = DateUtils.format(moment());
    
    $scope.ouModes = [{name: 'SELECTED'}, {name: 'CHILDREN'}, {name: 'DESCENDANTS'}, {name: 'ACCESSIBLE'}];         
    $scope.selectedOuMode = $scope.ouModes[0];
    $scope.report = {};
    
    //watch for selection of org unit from tree
    $scope.$watch('selectedOrgUnit', function() {        
        if( angular.isObject($scope.selectedOrgUnit)){            
            storage.set('SELECTED_OU', $scope.selectedOrgUnit);            
            $scope.loadPrograms($scope.selectedOrgUnit);
        }
    });
    
    //load programs associated with the selected org unit.
    $scope.loadPrograms = function(orgUnit) {                
        
        $scope.selectedOrgUnit = orgUnit;
        
        if (angular.isObject($scope.selectedOrgUnit)) {   

            ProgramFactory.getAll().then(function(programs){
                $scope.programs = [];
                angular.forEach(programs, function(program){                            
                    if(program.organisationUnits.hasOwnProperty($scope.selectedOrgUnit.id)){                                
                        $scope.programs.push(program);
                    }
                });

                if($scope.programs.length === 0){
                    $scope.selectedProgram = null;
                }
                else{
                    if($scope.selectedProgram){
                        angular.forEach($scope.programs, function(program){                            
                            if(program.id === $scope.selectedProgram.id){                                
                                $scope.selectedProgram = program;
                            }
                        });
                    }
                    else{                        
                        if($scope.programs.length === 1){
                            $scope.selectedProgram = $scope.programs[0];
                        }                        
                    }
                } 
            });
        }        
    };
    
    //watch for selection of program
    $scope.$watch('selectedProgram', function() {        
        if( angular.isObject($scope.selectedProgram)){            
            $scope.reportStarted = false;
            $scope.dataReady = false;
        }
    });
    
    $scope.generateReport = function(){
        
        $scope.reportStarted = true;
        $scope.dataReady = false;
        
        //check for form validity
        $scope.outerForm.submitted = true;        
        if( $scope.outerForm.$invalid || !$scope.selectedProgram){
            return false;
        }
        
        $scope.programStages = [];
        angular.forEach($scope.selectedProgram.programStages, function(stage){
            $scope.programStages[stage.id] = stage;
        });
            
        AttributesFactory.getByProgram($scope.selectedProgram).then(function(atts){            
            $scope.gridColumns = TEIGridService.generateGridColumns(atts, $scope.selectedOuMode.name);      
        });  
        
        //fetch TEIs for the selected program and orgunit/mode
        TEIService.search($scope.selectedOrgUnit.id, 
                            $scope.selectedOuMode.name,
                            null,
                            'program=' + $scope.selectedProgram.id,
                            null,
                            $scope.pager,
                            false).then(function(data){
            if(data.rows){
                $scope.teiCount = data.rows.length;                
            }
            
            
            //process tei grid
            $scope.teiList = TEIGridService.format(data);          
            
            DHIS2EventFactory.getByOrgUnitAndProgram($scope.selectedOrgUnit.id, $scope.selectedOuMode.name, $scope.selectedProgram.id).then(function(eventList){
                $scope.dhis2Events = [];
                angular.forEach(eventList, function(ev){
                    if(ev.trackedEntityInstance){
                        ev.name = $scope.programStages[ev.programStage].name;
                        ev.programName = $scope.selectedProgram.name;
                        ev.statusColor = EventUtils.getEventStatusColor(ev); 
                        ev.eventDate = DateUtils.format(ev.eventDate);
                        
                        if($scope.dhis2Events[ev.trackedEntityInstance]){
                            $scope.dhis2Events[ev.trackedEntityInstance].push(ev);
                        }
                        else{
                            $scope.dhis2Events[ev.trackedEntityInstance] = [ev];
                        }
                        ev = EventUtils.setEventOrgUnitName(ev);
                    }
                });
                $scope.reportStarted = false;
                $scope.dataReady = true;                
            });
        });
    };
    
    $scope.showEventDetails = function(dhis2Event, selectedTei){
        
        var modalInstance = $modal.open({
            templateUrl: 'components/report/event-details.html',
            controller: 'EventDetailsController',
            resolve: {
                dhis2Event: function () {
                    return dhis2Event;
                },
                gridColumns: function(){
                    return $scope.gridColumns;
                },
                selectedTei: function(){
                    return selectedTei;
                },
                entityName: function(){
                    return $scope.selectedProgram.trackedEntity.name;
                },
                reportMode: function(){
                    return 'PROGRAM';
                }
            }
        });

        modalInstance.result.then({
        });
    };   
    
})

//Controller for event details
.controller('EventDetailsController', 
    function($scope, 
            $modalInstance,
            orderByFilter,
            ProgramStageFactory,
            dhis2Event,
            selectedTei,
            gridColumns,
            entityName,
            reportMode){
    
    $scope.selectedTei = selectedTei;
    $scope.gridColumns = gridColumns;
    $scope.entityName = entityName;
    $scope.reportMode = reportMode;
    $scope.currentEvent = dhis2Event;
    $scope.currentEvent.providedElsewhere = [];
    
    if(!angular.isUndefined( $scope.currentEvent.notes)){
        $scope.currentEvent.notes = orderByFilter($scope.currentEvent.notes, '-storedDate');            
        angular.forEach($scope.currentEvent.notes, function(note){
            note.storedDate = moment(note.storedDate).format('YYYY-MM-DD @ hh:mm A');
        });
    }
    
    ProgramStageFactory.get($scope.currentEvent.programStage).then(function(stage){
        $scope.currentStage = stage;

        $scope.allowProvidedElsewhereExists = false;
        angular.forEach($scope.currentStage.programStageDataElements, function(prStDe){
            $scope.currentStage.programStageDataElements[prStDe.dataElement.id] = prStDe.dataElement;
            if(prStDe.allowProvidedElsewhere){
                $scope.allowProvidedElsewhereExists = true;
                $scope.currentEvent.providedElsewhere[prStDe.dataElement.id] = '';   
            }                
        });
        angular.forEach($scope.currentEvent.dataValues, function(dataValue){
            var val = dataValue.value;
            if(val){
                var de = $scope.currentStage.programStageDataElements[dataValue.dataElement];
                if( de && de.type === 'int' && val){
                    val = parseInt(val);
                    dataValue.value = val;
                }
                $scope.currentEvent[dataValue.dataElement] = val;
            }                    
        });
    });
    
    $scope.close = function () {
        $modalInstance.close();
    };
});