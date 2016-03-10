package org.hisp.dhis.dxf2.metadata2.objectbundle;

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

import com.google.common.base.MoreObjects;
import org.hisp.dhis.feedback.ErrorCode;
import org.hisp.dhis.feedback.ErrorReport;
import org.hisp.dhis.feedback.ObjectErrorReport;
import org.hisp.dhis.feedback.ObjectErrorReports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class ObjectBundleValidation
{
    private Map<Class<?>, ObjectErrorReports> objectErrorReportsMap = new HashMap<>();

    public ObjectBundleValidation()
    {
    }

    public void addObjectErrorReports( List<ObjectErrorReport> objectErrorReports )
    {
        objectErrorReports.forEach( this::addObjectErrorReport );
    }

    public void addObjectErrorReport( ObjectErrorReport objectErrorReport )
    {
        if ( objectErrorReport == null || objectErrorReport.getErrorCodes().isEmpty() )
        {
            return;
        }

        Class<?> objectClass = objectErrorReport.getObjectClass();

        if ( !objectErrorReportsMap.containsKey( objectClass ) )
        {
            objectErrorReportsMap.put( objectClass, new ObjectErrorReports() );
        }

        ObjectErrorReports objectErrorReports = objectErrorReportsMap.get( objectClass );
        objectErrorReports.addObjectErrorReport( objectErrorReport );
        objectErrorReports.getStats().incIgnored();
    }

    public List<ObjectErrorReport> getAllObjectErrorReports( Class<?> klass )
    {
        List<ObjectErrorReport> objectErrorReports = new ArrayList<>();
        ObjectErrorReports errorReports = objectErrorReportsMap.get( klass );
        errorReports.getObjectErrorReports().forEach( objectErrorReports::add );

        return objectErrorReports;
    }

    public ObjectErrorReports getObjectErrorReports( Class<?> klass )
    {
        return objectErrorReportsMap.get( klass );
    }

    public List<ErrorReport> getErrorReportsByCode( Class<?> klass, ErrorCode errorCode )
    {
        List<ErrorReport> errorReports = new ArrayList<>();

        if ( !objectErrorReportsMap.containsKey( klass ) )
        {
            return errorReports;
        }

        Collection<ObjectErrorReport> objectErrorReports = objectErrorReportsMap.get( klass ).getObjectErrorReports();

        for ( ObjectErrorReport objectErrorReport : objectErrorReports )
        {
            List<ErrorReport> byCode = objectErrorReport.getErrorReportsByCode().get( errorCode );

            if ( byCode != null )
            {
                errorReports.addAll( byCode );
            }
        }

        return errorReports;
    }

    public Map<Class<?>, ObjectErrorReports> getObjectErrorReports()
    {
        return objectErrorReportsMap;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this )
            .add( "objectErrorReportsMap", objectErrorReportsMap )
            .toString();
    }
}
