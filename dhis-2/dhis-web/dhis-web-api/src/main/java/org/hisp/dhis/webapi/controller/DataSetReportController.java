package org.hisp.dhis.webapi.controller;

/*
 * Copyright (c) 2004-2016, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
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

import org.apache.commons.io.IOUtils;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.common.cache.CacheStrategy;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dataset.FormType;
import org.hisp.dhis.datasetreport.DataSetReportService;
import org.hisp.dhis.dxf2.webmessage.WebMessageException;
import org.hisp.dhis.i18n.I18nManager;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.system.grid.GridUtils;
import org.hisp.dhis.webapi.service.WebMessageService;
import org.hisp.dhis.webapi.utils.ContextUtils;
import org.hisp.dhis.webapi.utils.WebMessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;
import java.util.*;

/**
 * @author Stian Sandvold
 */
@Controller
@RequestMapping( value = "/datasetreport" )
public class DataSetReportController
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private DataSetReportService dataSetReportService;

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    private I18nManager i18nManager;

    @Autowired
    private ContextUtils contextUtils;

    @Autowired
    WebMessageService webMessageService;

    @RequestMapping( value = "", method = RequestMethod.GET )
    public void getDataSetReport( HttpServletRequest request, HttpServletResponse response )
        throws WebMessageException
    {
        Map<String, String[]> params = request.getParameterMap();
        OrganisationUnit selectedOrgunit;
        DataSet selectedDataSet;
        Period selectedPeriod;
        String customDataEntryFormCode = null;
        List<Grid> grids = new ArrayList<>();
        FormType formType;
        Set<String> dimension = new HashSet<>();
        boolean selectedUnitOnly = false;
        String type = null;

        // Make sure all required parameters are present
        if ( !params.containsKey( "ds" ) || !params.containsKey( "pe" ) || !params.containsKey( "ou" ) )
        {
            throw new WebMessageException( WebMessageUtils.badRequest( "Missing required parameters" ) );
        }

        // Fetch required data
        selectedDataSet = dataSetService.getDataSetNoAcl( params.get( "ds" )[0] );
        selectedPeriod = PeriodType.getPeriodFromIsoString( params.get( "pe" )[0] );
        selectedPeriod = periodService.reloadPeriod( selectedPeriod );
        selectedOrgunit = organisationUnitService.getOrganisationUnit( params.get( "ou" )[0] );

        // Fetch optional parameters
        if ( params.containsKey( "dimension" ) )
        {
            dimension.addAll( Arrays.asList( params.get( "dimension" ) ) );
        }

        if ( params.containsKey( "selectedUnitOnly" ) )
        {
            selectedUnitOnly = Boolean.parseBoolean( params.get( "selectedUnitOnly" )[0] );
        }

        if ( params.containsKey( "type" ) )
        {
            type = params.get( "type" )[0];
        }

        formType = selectedDataSet.getFormType();

        // ---------------------------------------------------------------------
        // Configure response
        // ---------------------------------------------------------------------

        contextUtils
            .configureResponse( response, ContextUtils.CONTENT_TYPE_HTML, CacheStrategy.RESPECT_SYSTEM_SETTING, null,
                false );

        // ---------------------------------------------------------------------
        // Assemble report
        // ---------------------------------------------------------------------

        if ( formType.isCustom() )
        {
            if ( type != null )
            {
                grids = dataSetReportService
                    .getCustomDataSetReportAsGrid( selectedDataSet, selectedPeriod, selectedOrgunit, dimension,
                        selectedUnitOnly, i18nManager.getI18nFormat() );
            }
            else
            {
                customDataEntryFormCode = dataSetReportService
                    .getCustomDataSetReport( selectedDataSet, selectedPeriod, selectedOrgunit, dimension,
                        selectedUnitOnly, i18nManager.getI18nFormat() );
            }
        }
        else if ( formType.isSection() )
        {
            grids = dataSetReportService
                .getSectionDataSetReport( selectedDataSet, selectedPeriod, selectedOrgunit, dimension, selectedUnitOnly,
                    i18nManager.getI18nFormat(), i18nManager.getI18n() );
        }
        else
        {
            grids = dataSetReportService
                .getDefaultDataSetReport( selectedDataSet, selectedPeriod, selectedOrgunit, dimension, selectedUnitOnly,
                    i18nManager.getI18nFormat(), i18nManager.getI18n() );
        }

        // ---------------------------------------------------------------------
        // Write response
        // ---------------------------------------------------------------------

        try
        {
            Writer w = response.getWriter();

            if ( formType.isCustom() && type == null )
            {
                IOUtils.write( customDataEntryFormCode, w );
            }
            else
            {
                grids.forEach( grid -> {
                    try
                    {
                        GridUtils.toHtmlCss( grid, w );
                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace();
                    }
                } );
            }

            w.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
}
