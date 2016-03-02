package org.hisp.dhis.dxf2.metadata2.feedback;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.feedback.ErrorCode;
import org.hisp.dhis.feedback.ErrorReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@JacksonXmlRootElement( localName = "importReport", namespace = DxfNamespaces.DXF_2_0 )
public class ImportReport
{
    private ImportStats stats = new ImportStats();

    private Map<Class<?>, Map<ErrorCode, List<ErrorReport>>> errorReports = new HashMap<>();

    public ImportReport()
    {
    }

    @JsonProperty
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public ImportStats getStats()
    {
        return stats;
    }

    public void addErrorReports( List<ErrorReport> errorReports )
    {
        errorReports.forEach( this::addErrorReport );
    }

    public <T extends ErrorReport> void addErrorReport( T errorReport )
    {
        if ( !errorReports.containsKey( errorReport.getMainKlass() ) )
        {
            errorReports.put( errorReport.getMainKlass(), new HashMap<>() );
        }

        if ( !errorReports.get( errorReport.getMainKlass() ).containsKey( errorReport.getErrorCode() ) )
        {
            errorReports.get( errorReport.getMainKlass() ).put( errorReport.getErrorCode(), new ArrayList<>() );
        }

        errorReports.get( errorReport.getMainKlass() ).get( errorReport.getErrorCode() ).add( errorReport );
    }

    public void addErrorReport( Class<?> mainKlass, ErrorCode errorCode, Object... args )
    {
        ErrorReport errorReport = new ErrorReport( mainKlass, errorCode, args );
        addErrorReport( errorReport );
    }

    @JsonProperty
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public Map<Class<?>, Map<ErrorCode, List<ErrorReport>>> getErrorReports()
    {
        return errorReports;
    }

    public Map<ErrorCode, List<ErrorReport>> getErrorReports( Class<?> klass )
    {
        Map<ErrorCode, List<ErrorReport>> map = errorReports.get( klass );

        if ( map == null )
        {
            return new HashMap<>();
        }

        return map;
    }

    public List<ErrorReport> getErrorReports( Class<?> klass, ErrorCode errorCode )
    {
        Map<ErrorCode, List<ErrorReport>> map = errorReports.get( klass );

        if ( !map.containsKey( errorCode ) )
        {
            return new ArrayList<>();
        }

        return map.get( errorCode );
    }

    public void setErrorReports( Map<Class<?>, Map<ErrorCode, List<ErrorReport>>> errorReports )
    {
        this.errorReports = errorReports;
    }
}
