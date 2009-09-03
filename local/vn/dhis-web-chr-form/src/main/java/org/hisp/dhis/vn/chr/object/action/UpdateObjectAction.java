package org.hisp.dhis.vn.chr.object.action;

/**
 * @author Chau Thu Tran
 * 
 */

import org.hisp.dhis.system.util.CodecUtils;
import org.hisp.dhis.vn.chr.Form;
import org.hisp.dhis.vn.chr.FormService;
import org.hisp.dhis.vn.chr.form.action.ActionSupport;
import org.hisp.dhis.vn.chr.jdbc.FormManager;

public class UpdateObjectAction
    extends ActionSupport
{

    // -----------------------------------------------------------------------------------------------
    // Dependencies
    // -----------------------------------------------------------------------------------------------

    private FormManager formManager;

    public void setFormManager( FormManager formManager )
    {
        this.formManager = formManager;
    }

    private FormService formService;

    public void setFormService( FormService formService )
    {
        this.formService = formService;
    }

    // -----------------------------------------------------------------------------------------------
    // Input && Output
    // -----------------------------------------------------------------------------------------------

    private Integer formId;

    public void setFormId( Integer formId )
    {
        this.formId = formId;
    }

    private String[] data;

    public void setData( String[] data )
    {
        this.data = data;
    }

    // -----------------------------------------------------------------------------------------------
    // Implement : process Select SQL
    // -----------------------------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        try
        {
            Form form = formService.getForm( formId.intValue() );

            // convert data to Unicode
            for ( int i = 0; i < data.length; i++ )
            {
                if ( data[i].length() != 0 )
                {
                    data[i] = CodecUtils.unescape( data[i] );
                }
            }

            message = i18n.getString( "update" ) + " " + i18n.getString( "success" );

            formManager.updateObject( form, data );

            return SUCCESS;

        }
        catch ( Exception ex )
        {
            message = i18n.getString( "update" ) + " " + i18n.getString( "error" );
            ex.printStackTrace();
        }

        return ERROR;
    }
}
