( function ( $ ) {
    $.fn.fileResourceEntryField = function() {

        // TODO Use i18n
        // TODO Disable field when offline
        // TODO Re-init on period change

        var $container = $( this );

        var $field = $container.find( '.entryfileresource' );
        var $button = $container.find( '.upload-button' );

        var $fileInput = $container.find( 'input[type=file]' );

        var $fileinfo = $container.find( '.upload-fileinfo' );
        var $fileinfoName = $fileinfo.find( '.upload-fileinfo-name' );
        var $fileinfoSize = $fileinfo.find( '.upload-fileinfo-size' );

        var $progress = $container.find( '.upload-progress' );
        var $progressBar = $progress.find( '.upload-progress-bar' );
        var $progressInfo = $progress.find( '.upload-progress-info' );

        var id = $field.attr( 'id' );

        var split = dhis2.de.splitFieldId( id );

        var dataElementId = split.dataElementId;
        var optionComboId = split.optionComboId;
        var orgUnitid = dhis2.de.currentOrganisationUnitId;
        var periodId = $( '#selectedPeriodId' ).val();

        var formData = {
            'de': dataElementId,
            'co': optionComboId,
            'ou': orgUnitid,
            'pe': periodId
        };

        // Logic
        var deleteFileDataValue = function() {
            var postData = formData;
            postData.value = '';

            $.ajax( {
                url: '../api/dataValues',
                type: 'POST',
                dataType: 'json',
                data: postData,
                success: function()
                {
                    $fileinfoName.text( '' );
                    $fileinfoSize.text( '' );
                    $fileinfo.hide();
                    $field.css( 'background-color', '' );
                    setButtonUpload();
                },
                error: function( data )
                {
                    console.log( data.errorThrown );
                }
            } );
        };

        var setButtonDelete = function() {
            $button.button( {
                text: false,
                icons: {
                    primary: 'fa fa-trash-o'
                }
            } );
            $button.unbind( 'click' );
            $button.on( 'click', function() {
                $( '#fileDeleteConfirmationDialog' ).dialog( {
                    title: 'Confirm deletion',
                    resizable: false,
                    height: 140,
                    modal: true,
                    buttons: {
                        'Delete': function() {
                            deleteFileDataValue();
                            $( this ).dialog( 'close' );
                        },
                        Cancel: function() {
                            $( this ).dialog( 'close' );
                        }
                    }
                } );
            } );
            $button.button( 'enable' );
        };

        var setButtonUpload = function() {
            $button.button( {
                text: false,
                icons: {
                    primary: 'fa fa-upload'
                }
            } );
            $button.unbind( 'click' );
            $button.on( 'click', function()
            {
                $fileInput.click();
            } );
            $button.button( 'enable' );
        };

        var resetAndHideProgress = function() {
            $progressBar.toggleClass( 'upload-progress-bar-complete', true );
            $progressBar.css( 'width', 0 );
            $progress.hide();
        };

        var onFileDataValueSavedSuccess = function( fileResource ) {
            var name = fileResource.name, size = '(' + filesize( fileResource.contentLength ) + ')';

            $fileinfoName.text( name );
            $fileinfoSize.text( size );
            $progressBar.toggleClass( 'upload-progress-bar-complete' );
            $fileinfo.show();
            resetAndHideProgress();
            setButtonDelete();
            $button.button( 'enable' );
        };

        // Button setup
        $button.button( {
            text: false,
            icons: {
                primary: 'fa fa-ban'
            }
        } );
        $button.button( 'disable' );

        $( document ).on( dhis2.de.event.dataValuesLoaded, function() {
            ( typeof( $field.data( 'value' ) ) == 'undefined' ) ? setButtonUpload() : setButtonDelete();
        } );

        // Initialize file uploader
        $fileInput.fileupload( {
            url: '../api/dataValues/files',
            paramName: 'file',
            multipart: true,
            replaceFileInput: false,
            progressInterval: 250, /* ms */
            formData: formData,
            start: function( e )
            {
                $button.button( 'disable' );
                $progressBar.toggleClass( 'upload-progress-bar-complete', false );
                $fileinfo.hide();
                $progress.show();
            },
            progress: function( e, data )
            {
                var percent = parseInt( data.loaded / data.total * 100, 10 );
                $progressBar.css( 'width', percent + '%' );
                $progressInfo.text( percent + '%' );
            },
            fail: function( e, data )
            {
                setHeaderDelayMessage( "File upload failed!" );
                console.log( data.errorThrown );
                setButtonUpload();
            },
            done: function( e, data )
            {
                var fileResource = data.result.response.fileResource;

                saveFileResource( dataElementId, optionComboId, id, fileResource, function() {
                    onFileDataValueSavedSuccess( fileResource );
                } );
            }
        } );
    };
} )( jQuery );