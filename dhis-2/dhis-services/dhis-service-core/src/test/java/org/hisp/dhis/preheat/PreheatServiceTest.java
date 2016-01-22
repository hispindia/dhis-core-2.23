package org.hisp.dhis.preheat;

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

import com.google.common.collect.Lists;
import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.legend.LegendSet;
import org.hisp.dhis.option.OptionSet;
import org.hisp.dhis.user.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class PreheatServiceTest
    extends DhisSpringTest
{
    @Autowired
    private PreheatService preheatService;

    @Test( expected = PreheatException.class )
    public void testValidateAllFail()
    {
        PreheatParams params = new PreheatParams().setPreheatMode( PreheatMode.ALL );
        preheatService.validate( params );
    }

    @Test
    public void testValidateAll()
    {
        PreheatParams params = new PreheatParams().setPreheatMode( PreheatMode.ALL );
        params.getClasses().add( DataElement.class );

        preheatService.validate( params );
    }

    @Test( expected = PreheatException.class )
    public void testValidateRefFail()
    {
        PreheatParams params = new PreheatParams().setPreheatMode( PreheatMode.REFERENCE );
        preheatService.validate( params );
    }

    @Test
    public void testValidateRef()
    {
        PreheatParams params = new PreheatParams().setPreheatMode( PreheatMode.REFERENCE );
        params.getReferences().put( DataElement.class, Lists.newArrayList( "ID1", "ID2" ) );

        preheatService.validate( params );
    }

    @Test
    public void testScanNoObjectsDE()
    {
        DataElement dataElement = new DataElement( "DataElementA" );
        dataElement.setAutoFields();

        Map<Class<? extends IdentifiableObject>, List<String>> references = preheatService.scanObjectForReferences( dataElement, PreheatIdentifier.UID );

        assertTrue( references.containsKey( OptionSet.class ) );
        assertTrue( references.containsKey( LegendSet.class ) );
        assertTrue( references.containsKey( DataElementCategoryCombo.class ) );
        assertTrue( references.containsKey( User.class ) );
    }

    @Test
    public void testScanNoObjectsDEG()
    {
        DataElementGroup dataElementGroup = new DataElementGroup( "DataElementGroupA" );
        dataElementGroup.setAutoFields();

        Map<Class<? extends IdentifiableObject>, List<String>> references = preheatService.scanObjectForReferences( dataElementGroup, PreheatIdentifier.UID );

        assertTrue( references.containsKey( DataElement.class ) );
        assertTrue( references.containsKey( User.class ) );
    }

    @Test
    public void testScanReferenceUidDEG()
    {
        DataElementGroup dataElementGroup = new DataElementGroup( "DataElementGroupA" );
        dataElementGroup.setAutoFields();

        DataElement de1 = new DataElement( "DataElement1" );
        DataElement de2 = new DataElement( "DataElement1" );
        DataElement de3 = new DataElement( "DataElement1" );

        de1.setAutoFields();
        de2.setAutoFields();
        de3.setAutoFields();

        User user = new User();
        user.setAutoFields();

        dataElementGroup.addDataElement( de1 );
        dataElementGroup.addDataElement( de2 );
        dataElementGroup.addDataElement( de3 );

        dataElementGroup.setUser( user );

        Map<Class<? extends IdentifiableObject>, List<String>> references = preheatService.scanObjectForReferences( dataElementGroup, PreheatIdentifier.UID );

        assertTrue( references.containsKey( DataElement.class ) );
        assertTrue( references.containsKey( User.class ) );

        assertEquals( 3, references.get( DataElement.class ).size() );
        assertEquals( 1, references.get( User.class ).size() );

        assertTrue( references.get( DataElement.class ).contains( de1.getUid() ) );
        assertTrue( references.get( DataElement.class ).contains( de2.getUid() ) );
        assertTrue( references.get( DataElement.class ).contains( de3.getUid() ) );
        assertEquals( user.getUid(), references.get( User.class ).get( 0 ) );
    }
}
