package org.hisp.dhis.common.adapter;

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

import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.organisationunit.OrganisationUnit;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.UUID;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class OrganisationUnitXmlAdapter extends XmlAdapter<BaseIdentifiableObject, OrganisationUnit>
{
    private BaseIdentifiableObjectXmlAdapter baseIdentifiableObjectXmlAdapter = new BaseIdentifiableObjectXmlAdapter();

    @Override
    public OrganisationUnit unmarshal( BaseIdentifiableObject identifiableObject ) throws Exception
    {
        OrganisationUnit organisationUnit = new OrganisationUnit();

        organisationUnit.setUid( identifiableObject.getUid() );
        organisationUnit.setLastUpdated( identifiableObject.getLastUpdated() );
        organisationUnit.setName( identifiableObject.getName() == null ? UUID.randomUUID().toString() : identifiableObject.getName() );

        return organisationUnit;
    }

    @Override
    public BaseIdentifiableObject marshal( OrganisationUnit organisationUnit ) throws Exception
    {
        return baseIdentifiableObjectXmlAdapter.marshal( organisationUnit );
    }
}
