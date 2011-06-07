package org.hisp.dhis.reportexcel.importing.action;

/*
 * Copyright (c) 2004-2011, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementOperand;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.reportexcel.importing.ImportDataGeneric;
import org.hisp.dhis.reportexcel.importitem.ExcelItem;
import org.hisp.dhis.reportexcel.utils.ExcelUtils;

/**
 * @author Chau Thu Tran
 * @version $Id$
 */

public class ImportDataNormalAction
    extends ImportDataGeneric
{
    // -------------------------------------------------------------------------
    // Inputs && Outputs
    // -------------------------------------------------------------------------

    private Integer importReportId;

    public void setImportReportId( Integer importReportId )
    {
        this.importReportId = importReportId;
    }

    // -------------------------------------------------------------------------
    // Override the abstract method
    // -------------------------------------------------------------------------

    public void executeToImport( OrganisationUnit organisationUnit, Period period, String[] importItemIds, Workbook wb )
    {
        Collection<ExcelItem> importItems = new ArrayList<ExcelItem>();

        if ( importItemIds != null )
        {
            for ( int i = 0; i < importItemIds.length; i++ )
            {
                importItems.add( importItemService.getImportItem( importItemIds[i] ) );
            }
        }
        else
        {
            importItems = importItemService.getImportReport( importReportId ).getExcelItems();
        }

        for ( ExcelItem importItem : importItems )
        {
            Sheet sheet = wb.getSheetAt( importItem.getSheetNo() - 1 );

            String value = ExcelUtils.readValueImportingByPOI( importItem.getRow(), importItem.getColumn(), sheet );

            if ( value.length() > 0 )
            {
                DataElementOperand operand = expressionService.getOperandsInExpression( importItem.getExpression() )
                    .iterator().next();

                DataElement dataElement = dataElementService.getDataElement( operand.getDataElementId() );

                DataElementCategoryOptionCombo optionCombo = categoryService.getDataElementCategoryOptionCombo( operand
                    .getOptionComboId() );

                String storedBy = currentUserService.getCurrentUsername();

                DataValue dataValue = dataValueService
                    .getDataValue( organisationUnit, dataElement, period, optionCombo );

                if ( dataValue == null )
                {
                    dataValue = new DataValue( dataElement, period, organisationUnit, value + "", storedBy, new Date(),
                        null, optionCombo );
                    dataValueService.addDataValue( dataValue );
                }
                else
                {
                    dataValue.setValue( value + "" );
                    dataValue.setTimestamp( new Date() );
                    dataValue.setStoredBy( storedBy );
                    dataValueService.updateDataValue( dataValue );
                }
            }
        }
    }
}