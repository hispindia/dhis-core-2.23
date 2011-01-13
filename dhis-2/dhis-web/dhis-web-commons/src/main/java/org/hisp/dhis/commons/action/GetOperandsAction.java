package org.hisp.dhis.commons.action;

/*
 * Copyright (c) 2004-2010, University of Oslo
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
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementOperand;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataelement.comparator.DataElementOperandNameComparator;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.paging.ActionPagingSupport;
import org.hisp.dhis.system.filter.AggregatableDataElementFilter;
import org.hisp.dhis.system.util.Filter;
import org.hisp.dhis.system.util.FilterUtils;

/**
 * @author Lars Helge Overland
 */
public class GetOperandsAction
    extends ActionPagingSupport<DataElementOperand>
{
    private static Filter<DataElement> AGGREGATABLE_FILTER = new AggregatableDataElementFilter();
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private DataElementCategoryService dataElementCategoryService;

    public void setDataElementCategoryService( DataElementCategoryService dataElementCategoryService )
    {
        this.dataElementCategoryService = dataElementCategoryService;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    // -------------------------------------------------------------------------
    // Output & Input
    // -------------------------------------------------------------------------

    private Integer id;

    public void setId( Integer id )
    {
        this.id = id;
    }
    
    private String key;

    public void setKey( String key )
    {
        this.key = key;
    }

    private Integer dataSetId;

    public void setDataSetId( Integer dataSetId )
    {
        this.dataSetId = dataSetId;
    }

    private String aggregationOperator;

    public void setAggregationOperator( String aggregationOperator )
    {
        this.aggregationOperator = aggregationOperator;
    }

    private boolean includeTotals = false;

    public void setIncludeTotals( boolean includeTotals )
    {
        this.includeTotals = includeTotals;
    }

    public List<DataElementOperand> operands;

    public List<DataElementOperand> getOperands()
    {
        return operands;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
    {
        List<DataElement> dataElements = new ArrayList<DataElement>();

        if ( id != null )
        {
            dataElements = new ArrayList<DataElement>( dataElementService.getDataElementGroup( id ).getMembers() );
        }
        else if ( !StringUtils.isEmpty( key ) )
        {
            dataElements = new ArrayList<DataElement>( dataElementService.getDataElementsLikeName( key ) );
        }
        else if ( dataSetId != null )
        {
            dataElements = new ArrayList<DataElement>( dataSetService.getDataSet( dataSetId ).getDataElements() );
        }
        else
        {
            dataElements = new ArrayList<DataElement>( dataElementService.getAllDataElements() );
        }

        if ( aggregationOperator != null )
        {
            FilterUtils.filter( dataElements, AGGREGATABLE_FILTER );
        }

        operands = new ArrayList<DataElementOperand>( dataElementCategoryService.getOperands( dataElements, includeTotals ) );

        Collections.sort( operands, new DataElementOperandNameComparator() );

        if ( this.usepaging )
        {
            this.paging = createPaging( operands.size() );

            operands = operands.subList( paging.getStartPos(), paging.getEndPos() );
        }

        return SUCCESS;
    }
}
