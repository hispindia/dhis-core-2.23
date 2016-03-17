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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.common.base.MoreObjects;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.feedback.ObjectErrorReports;
import org.hisp.dhis.feedback.Stats;
import org.hisp.dhis.feedback.TypeReport;

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
    private final Map<Class<?>, TypeReport> typeReportMap = new HashMap<>();

    public ImportReport()
    {
    }

    @JsonProperty
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public Stats getStats()
    {
        Stats stats = new Stats();
        typeReportMap.values().forEach( typeReport -> stats.merge( typeReport.getStats() ) );

        return stats;
    }

    @JsonProperty
    @JacksonXmlElementWrapper( localName = "typeReports", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "typeReport", namespace = DxfNamespaces.DXF_2_0 )
    public List<TypeReport> getTypeReports()
    {
        return new ArrayList<>( typeReportMap.values() );
    }

    public Map<Class<?>, TypeReport> getTypeReportMap()
    {
        return typeReportMap;
    }

    public void addStats( Map<Class<?>, Stats> statsMap )
    {
        for ( Class<?> klass : statsMap.keySet() )
        {
            if ( !typeReportMap.containsKey( klass ) ) typeReportMap.put( klass, new TypeReport( klass ) );
            typeReportMap.get( klass ).getStats().merge( statsMap.get( klass ) );
        }
    }

    public void addObjectErrorReports( Map<Class<?>, ObjectErrorReports> objectErrorReports )
    {
        for ( Class<?> klass : objectErrorReports.keySet() )
        {
            if ( !typeReportMap.containsKey( klass ) ) typeReportMap.put( klass, new TypeReport( klass ) );
            typeReportMap.get( klass ).getObjectErrorReports().addObjectErrorReports( objectErrorReports.get( klass ) );
        }
    }

    public void addTypeReports( Map<Class<? extends IdentifiableObject>, TypeReport> typeReports )
    {
        for ( Class<?> klass : typeReports.keySet() )
        {
            if ( !typeReportMap.containsKey( klass ) ) typeReportMap.put( klass, new TypeReport( klass ) );
            TypeReport typeReport = typeReportMap.get( klass );
            typeReport.getStats().merge( typeReports.get( klass ).getStats() );
            typeReport.getObjectErrorReports().addObjectErrorReports( typeReports.get( klass ).getObjectErrorReports() );
        }
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this )
            .add( "stats", getStats() )
            .add( "typeReports", getTypeReports() )
            .toString();
    }
}
