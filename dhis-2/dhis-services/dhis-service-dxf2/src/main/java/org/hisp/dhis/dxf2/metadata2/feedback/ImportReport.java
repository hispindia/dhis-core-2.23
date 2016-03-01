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

import org.hisp.dhis.feedback.ErrorReport;
import org.hisp.dhis.feedback.ErrorReports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class ImportReport
{
    private ImportStats stats = new ImportStats();

    private Map<Class<?>, ErrorReports> errorReportMap = new HashMap<>();

    public ImportReport()
    {
    }

    public ImportStats getStats()
    {
        return stats;
    }

    public void addErrorReport( ErrorReport errorReport )
    {
        if ( !errorReportMap.containsKey( errorReport.getMainKlass() ) )
        {
            errorReportMap.put( errorReport.getMainKlass(), new ErrorReports() );
        }

        errorReportMap.get( errorReport.getMainKlass() ).getErrorReports().add( errorReport );
    }

    public void addErrorReports( ErrorReports errorReports )
    {
        if ( errorReports == null || errorReports.getErrorReports().isEmpty() )
        {
            return;
        }

        Class<?> mainKlass = errorReports.getErrorReports().get( 0 ).getMainKlass();

        if ( !errorReportMap.containsKey( mainKlass ) )
        {
            errorReportMap.put( mainKlass, new ErrorReports() );
        }

        errorReportMap.get( mainKlass ).getErrorReports().addAll( errorReports.getErrorReports() );
    }

    public Map<Class<?>, ErrorReports> getErrorReportMap()
    {
        return errorReportMap;
    }

    public List<ErrorReports> getErrorReports()
    {
        return new ArrayList<>( errorReportMap.values() );
    }
}
