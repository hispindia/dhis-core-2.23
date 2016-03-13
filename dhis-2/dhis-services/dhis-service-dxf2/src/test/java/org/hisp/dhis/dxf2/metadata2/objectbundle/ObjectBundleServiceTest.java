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

import com.google.common.collect.Sets;
import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.attribute.Attribute;
import org.hisp.dhis.common.IdScheme;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.common.MergeMode;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategory;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementCategoryOption;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dataelement.DataElementOperand;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.feedback.ErrorCode;
import org.hisp.dhis.feedback.ErrorReport;
import org.hisp.dhis.feedback.ObjectErrorReport;
import org.hisp.dhis.importexport.ImportStrategy;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.option.Option;
import org.hisp.dhis.option.OptionSet;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.preheat.PreheatErrorReport;
import org.hisp.dhis.preheat.PreheatIdentifier;
import org.hisp.dhis.preheat.PreheatMode;
import org.hisp.dhis.render.RenderFormat;
import org.hisp.dhis.render.RenderService;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserService;
import org.hisp.dhis.validation.ValidationRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class ObjectBundleServiceTest
    extends DhisSpringTest
{
    @Autowired
    private ObjectBundleService objectBundleService;

    @Autowired
    private IdentifiableObjectManager manager;

    @Autowired
    private RenderService _renderService;

    @Autowired
    private UserService _userService;

    @Override
    protected void setUpTest() throws Exception
    {
        renderService = _renderService;
        userService = _userService;
    }

    @Test
    public void testCreateObjectBundle()
    {
        ObjectBundleParams params = new ObjectBundleParams();
        ObjectBundle bundle = objectBundleService.create( params );

        assertNotNull( bundle );
    }

    @Test
    public void testCreateDoesPreheating()
    {
        DataElementGroup dataElementGroup = fromJson( "dxf2/degAUidRef.json", DataElementGroup.class );
        defaultSetup();

        ObjectBundleParams params = new ObjectBundleParams();
        params.setPreheatMode( PreheatMode.REFERENCE );
        params.addObject( dataElementGroup );

        ObjectBundle bundle = objectBundleService.create( params );

        assertNotNull( bundle );
        assertFalse( bundle.getPreheat().isEmpty() );
        assertFalse( bundle.getPreheat().isEmpty( PreheatIdentifier.UID ) );
        assertFalse( bundle.getPreheat().isEmpty( PreheatIdentifier.UID, DataElement.class ) );
        assertTrue( bundle.getPreheat().containsKey( PreheatIdentifier.UID, DataElement.class, "deabcdefghA" ) );
        assertTrue( bundle.getPreheat().containsKey( PreheatIdentifier.UID, DataElement.class, "deabcdefghB" ) );
        assertTrue( bundle.getPreheat().containsKey( PreheatIdentifier.UID, DataElement.class, "deabcdefghC" ) );
        assertFalse( bundle.getPreheat().containsKey( PreheatIdentifier.UID, DataElement.class, "deabcdefghD" ) );
    }

    @Test
    public void testObjectBundleShouldAddToObjectAndPreheat()
    {
        DataElementGroup dataElementGroup = fromJson( "dxf2/degAUidRef.json", DataElementGroup.class );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.VALIDATE );
        params.addObject( dataElementGroup );

        ObjectBundle bundle = objectBundleService.create( params );
        bundle.getPreheat().put( bundle.getPreheatIdentifier(), dataElementGroup );

        assertTrue( bundle.getObjectMap().get( DataElementGroup.class ).contains( dataElementGroup ) );
        assertTrue( bundle.getPreheat().containsKey( PreheatIdentifier.UID, DataElementGroup.class, dataElementGroup.getUid() ) );
    }

    @Test
    public void testPreheatValidations() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_validate1.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.VALIDATE );
        params.setImportMode( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );
        assertFalse( validate.getObjectErrorReports().isEmpty() );

        List<ObjectErrorReport> objectErrorReports = validate.getAllObjectErrorReports( DataElement.class );
        assertFalse( objectErrorReports.isEmpty() );

        for ( ObjectErrorReport objectErrorReport : objectErrorReports )
        {
            for ( ErrorCode errorCode : objectErrorReport.getErrorCodes() )
            {
                List<ErrorReport> errorReports = objectErrorReport.getErrorReportsByCode().get( errorCode );

                assertFalse( errorReports.isEmpty() );

                for ( ErrorReport errorReport : errorReports )
                {
                    assertTrue( PreheatErrorReport.class.isInstance( errorReport ) );
                    PreheatErrorReport preheatErrorReport = (PreheatErrorReport) errorReport;
                    assertEquals( PreheatIdentifier.UID, preheatErrorReport.getPreheatIdentifier() );

                    if ( DataElementCategoryCombo.class.isInstance( preheatErrorReport.getValue() ) )
                    {
                        assertEquals( "p0KPaWEg3cf", preheatErrorReport.getObjectReference().getUid() );
                    }
                    else if ( User.class.isInstance( preheatErrorReport.getValue() ) )
                    {
                        assertEquals( "GOLswS44mh8", preheatErrorReport.getObjectReference().getUid() );
                    }
                    else if ( OptionSet.class.isInstance( preheatErrorReport.getValue() ) )
                    {
                        assertEquals( "pQYCiuosBnZ", preheatErrorReport.getObjectReference().getUid() );
                    }
                }
            }
        }
    }

    @Test
    public void testPreheatValidationsWithCatCombo() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_validate1.json" ).getInputStream(), RenderFormat.JSON );

        DataElementCategoryCombo categoryCombo = manager.getByName( DataElementCategoryCombo.class, "default" );
        categoryCombo.setUid( "p0KPaWEg3cf" );
        manager.update( categoryCombo );

        OptionSet optionSet = new OptionSet( "OptionSet: pQYCiuosBnZ" );
        optionSet.setAutoFields();
        optionSet.setUid( "pQYCiuosBnZ" );
        manager.save( optionSet );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.VALIDATE );
        params.setImportMode( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );
        assertFalse( validate.getObjectErrorReports().isEmpty() );

        List<ObjectErrorReport> objectErrorReports = validate.getAllObjectErrorReports( DataElement.class );
        assertFalse( objectErrorReports.isEmpty() );

        for ( ObjectErrorReport objectErrorReport : objectErrorReports )
        {
            for ( ErrorCode errorCode : objectErrorReport.getErrorCodes() )
            {
                List<ErrorReport> errorReports = objectErrorReport.getErrorReportsByCode().get( errorCode );

                assertFalse( errorReports.isEmpty() );

                for ( ErrorReport errorReport : errorReports )
                {
                    assertTrue( PreheatErrorReport.class.isInstance( errorReport ) );
                    PreheatErrorReport preheatErrorReport = (PreheatErrorReport) errorReport;
                    assertEquals( PreheatIdentifier.UID, preheatErrorReport.getPreheatIdentifier() );

                    if ( DataElementCategoryCombo.class.isInstance( preheatErrorReport.getValue() ) )
                    {
                        assertFalse( true );
                    }
                    else if ( User.class.isInstance( preheatErrorReport.getValue() ) )
                    {
                        assertEquals( "GOLswS44mh8", preheatErrorReport.getObjectReference().getUid() );
                    }
                    else if ( OptionSet.class.isInstance( preheatErrorReport.getValue() ) )
                    {
                        assertFalse( true );
                    }
                }
            }
        }
    }

    @Test
    public void testCreatePreheatValidationsInvalidObjects() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_validate2.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.VALIDATE );
        params.setImportMode( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );

        assertFalse( validate.getObjectErrorReports().isEmpty() );

        assertEquals( 3, validate.getErrorReportsByCode( DataElement.class, ErrorCode.E5002 ).size() );
        assertEquals( 3, validate.getErrorReportsByCode( DataElement.class, ErrorCode.E4000 ).size() );
    }

    @Test
    public void testUpdatePreheatValidationsInvalidObjects() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_validate2.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.VALIDATE );
        params.setImportMode( ImportStrategy.UPDATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );

        assertFalse( validate.getObjectErrorReports().isEmpty() );
        assertEquals( 3, validate.getErrorReportsByCode( DataElement.class, ErrorCode.E5001 ).size() );
    }

    @Test
    public void testUpdateRequiresValidReferencesUID() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_validate4.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.VALIDATE );
        params.setPreheatIdentifier( PreheatIdentifier.UID );
        params.setImportMode( ImportStrategy.UPDATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );

        assertEquals( 3, validate.getObjectErrorReports( DataElement.class ).size() );
    }

    @Test
    public void testUpdateWithPersistedObjectsRequiresValidReferencesUID() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_validate7.json" ).getInputStream(), RenderFormat.JSON );
        defaultSetup();

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.VALIDATE );
        params.setPreheatIdentifier( PreheatIdentifier.UID );
        params.setImportMode( ImportStrategy.UPDATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );

        assertEquals( 1, validate.getErrorReportsByCode( DataElement.class, ErrorCode.E5001 ).size() );
        assertFalse( validate.getErrorReportsByCode( DataElement.class, ErrorCode.E4000 ).isEmpty() );
        assertEquals( 0, bundle.getObjectMap().get( DataElement.class ).size() );
    }

    @Test
    public void testUpdateRequiresValidReferencesCODE() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_validate5.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.VALIDATE );
        params.setPreheatIdentifier( PreheatIdentifier.CODE );
        params.setImportMode( ImportStrategy.UPDATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );

        assertFalse( validate.getObjectErrorReports( DataElement.class ).isEmpty() );
        assertEquals( 3, validate.getErrorReportsByCode( DataElement.class, ErrorCode.E5001 ).size() );
    }

    @Test
    public void testUpdateRequiresValidReferencesAUTO() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_validate6.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.VALIDATE );
        params.setPreheatIdentifier( PreheatIdentifier.AUTO );
        params.setImportMode( ImportStrategy.UPDATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );

        assertFalse( validate.getObjectErrorReports( DataElement.class ).isEmpty() );
        assertEquals( 3, validate.getErrorReportsByCode( DataElement.class, ErrorCode.E5001 ).size() );
    }

    @Test
    public void testDeleteRequiresValidReferencesUID() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_validate4.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.VALIDATE );
        params.setPreheatIdentifier( PreheatIdentifier.UID );
        params.setImportMode( ImportStrategy.DELETE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );

        assertFalse( validate.getObjectErrorReports( DataElement.class ).isEmpty() );
        assertEquals( 3, validate.getErrorReportsByCode( DataElement.class, ErrorCode.E5001 ).size() );
    }

    @Test
    public void testDeleteRequiresValidReferencesCODE() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_validate5.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.VALIDATE );
        params.setPreheatIdentifier( PreheatIdentifier.CODE );
        params.setImportMode( ImportStrategy.DELETE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );

        assertFalse( validate.getObjectErrorReports( DataElement.class ).isEmpty() );
        assertEquals( 3, validate.getErrorReportsByCode( DataElement.class, ErrorCode.E5001 ).size() );
    }

    @Test
    public void testDeleteRequiresValidReferencesAUTO() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_validate6.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.VALIDATE );
        params.setPreheatIdentifier( PreheatIdentifier.AUTO );
        params.setImportMode( ImportStrategy.DELETE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );

        assertFalse( validate.getObjectErrorReports( DataElement.class ).isEmpty() );
        assertEquals( 3, validate.getErrorReportsByCode( DataElement.class, ErrorCode.E5001 ).size() );
    }

    @Test
    public void testPreheatValidationsIncludingMerge() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_validate3.json" ).getInputStream(), RenderFormat.JSON );
        defaultSetup();

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.VALIDATE );
        params.setMergeMode( MergeMode.REPLACE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );

        assertNotNull( validate );
    }

    @Test
    public void testSimpleDataElementDeleteUID() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_simple_delete_uid.json" ).getInputStream(), RenderFormat.JSON );
        defaultSetup();

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.DELETE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        objectBundleService.validate( bundle );
        objectBundleService.commit( bundle );

        List<DataElement> dataElements = manager.getAll( DataElement.class );
        assertEquals( 1, dataElements.size() );
        assertEquals( "deabcdefghB", dataElements.get( 0 ).getUid() );
    }

    @Test
    public void testSimpleDataElementDeleteCODE() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_simple_delete_code.json" ).getInputStream(), RenderFormat.JSON );
        defaultSetup();

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setPreheatIdentifier( PreheatIdentifier.CODE );
        params.setImportMode( ImportStrategy.DELETE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        objectBundleService.validate( bundle );
        objectBundleService.commit( bundle );

        List<DataElement> dataElements = manager.getAll( DataElement.class );
        assertEquals( 1, dataElements.size() );
        assertEquals( "DataElementCodeD", dataElements.get( 0 ).getCode() );
    }

    @Test
    public void testCreateSimpleMetadataUID() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/simple_metadata.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        objectBundleService.validate( bundle );
        objectBundleService.commit( bundle );

        List<OrganisationUnit> organisationUnits = manager.getAll( OrganisationUnit.class );
        List<DataElement> dataElements = manager.getAll( DataElement.class );
        List<DataSet> dataSets = manager.getAll( DataSet.class );
        List<UserAuthorityGroup> userRoles = manager.getAll( UserAuthorityGroup.class );
        List<User> users = manager.getAll( User.class );

        assertFalse( organisationUnits.isEmpty() );
        assertFalse( dataElements.isEmpty() );
        assertFalse( dataSets.isEmpty() );
        assertFalse( users.isEmpty() );
        assertFalse( userRoles.isEmpty() );

        Map<Class<? extends IdentifiableObject>, IdentifiableObject> defaults = manager.getDefaults();

        DataSet dataSet = dataSets.get( 0 );
        User user = users.get( 0 );

        for ( DataElement dataElement : dataElements )
        {
            assertNotNull( dataElement.getCategoryCombo() );
            assertEquals( defaults.get( DataElementCategoryCombo.class ), dataElement.getCategoryCombo() );
        }

        assertFalse( dataSet.getSources().isEmpty() );
        assertFalse( dataSet.getDataElements().isEmpty() );
        assertEquals( 1, dataSet.getSources().size() );
        assertEquals( 2, dataSet.getDataElements().size() );
        assertEquals( PeriodType.getPeriodTypeByName( "Monthly" ), dataSet.getPeriodType() );

        assertNotNull( user.getUserCredentials() );
        assertEquals( "admin", user.getUserCredentials().getUsername() );
        assertFalse( user.getUserCredentials().getUserAuthorityGroups().isEmpty() );
        assertFalse( user.getOrganisationUnits().isEmpty() );
        assertEquals( "PdWlltZnVZe", user.getOrganisationUnit().getUid() );
    }

    @Test
    public void testCreateSimpleMetadataAttributeValuesUID() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/simple_metadata_with_av.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        objectBundleService.validate( bundle );
        objectBundleService.commit( bundle );

        List<OrganisationUnit> organisationUnits = manager.getAll( OrganisationUnit.class );
        List<DataElement> dataElements = manager.getAll( DataElement.class );
        List<DataSet> dataSets = manager.getAll( DataSet.class );
        List<UserAuthorityGroup> userRoles = manager.getAll( UserAuthorityGroup.class );
        List<User> users = manager.getAll( User.class );
        List<Option> options = manager.getAll( Option.class );
        List<OptionSet> optionSets = manager.getAll( OptionSet.class );
        List<Attribute> attributes = manager.getAll( Attribute.class );

        assertFalse( organisationUnits.isEmpty() );
        assertFalse( dataElements.isEmpty() );
        assertFalse( dataSets.isEmpty() );
        assertFalse( users.isEmpty() );
        assertFalse( userRoles.isEmpty() );
        assertEquals( 2, attributes.size() );
        assertEquals( 2, options.size() );
        assertEquals( 1, optionSets.size() );

        Map<Class<? extends IdentifiableObject>, IdentifiableObject> defaults = manager.getDefaults();

        DataSet dataSet = dataSets.get( 0 );
        User user = users.get( 0 );
        OptionSet optionSet = optionSets.get( 0 );

        for ( DataElement dataElement : dataElements )
        {
            assertNotNull( dataElement.getCategoryCombo() );
            assertEquals( defaults.get( DataElementCategoryCombo.class ), dataElement.getCategoryCombo() );
        }

        assertFalse( dataSet.getSources().isEmpty() );
        assertFalse( dataSet.getDataElements().isEmpty() );
        assertEquals( 1, dataSet.getSources().size() );
        assertEquals( 2, dataSet.getDataElements().size() );
        assertEquals( PeriodType.getPeriodTypeByName( "Monthly" ), dataSet.getPeriodType() );

        assertNotNull( user.getUserCredentials() );
        assertEquals( "admin", user.getUserCredentials().getUsername() );
        assertFalse( user.getUserCredentials().getUserAuthorityGroups().isEmpty() );
        assertFalse( user.getOrganisationUnits().isEmpty() );
        assertEquals( "PdWlltZnVZe", user.getOrganisationUnit().getUid() );

        assertEquals( 2, optionSet.getOptions().size() );

        // attribute value check
        DataElement dataElementA = manager.get( DataElement.class, "SG4HuKlNEFH" );
        DataElement dataElementB = manager.get( DataElement.class, "CCwk5Yx440o" );
        DataElement dataElementC = manager.get( DataElement.class, "j5PneRdU7WT" );
        DataElement dataElementD = manager.get( DataElement.class, "k90AVpBahO4" );

        assertNotNull( dataElementA );
        assertNotNull( dataElementB );
        assertNotNull( dataElementC );
        assertNotNull( dataElementD );

        assertTrue( dataElementA.getAttributeValues().isEmpty() );
        assertTrue( dataElementB.getAttributeValues().isEmpty() );
        assertFalse( dataElementC.getAttributeValues().isEmpty() );
        assertFalse( dataElementD.getAttributeValues().isEmpty() );
    }

    @Test
    public void testValidateMetadataAttributeValuesUniqueAndMandatoryUID() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/simple_metadata_uga.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.VALIDATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validation = objectBundleService.validate( bundle );
    }

    @Test
    public void testCreateDataSetsWithUgaUID() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/simple_metadata_uga.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        objectBundleService.validate( bundle );
        objectBundleService.commit( bundle );

        List<OrganisationUnit> organisationUnits = manager.getAll( OrganisationUnit.class );
        List<DataElement> dataElements = manager.getAll( DataElement.class );
        List<UserAuthorityGroup> userRoles = manager.getAll( UserAuthorityGroup.class );
        List<User> users = manager.getAll( User.class );
        List<UserGroup> userGroups = manager.getAll( UserGroup.class );

        assertEquals( 1, organisationUnits.size() );
        assertEquals( 2, dataElements.size() );
        assertEquals( 1, userRoles.size() );
        assertEquals( 1, users.size() );
        assertEquals( 2, userGroups.size() );

        assertEquals( 1, dataElements.get( 0 ).getUserGroupAccesses().size() );
        assertEquals( 1, dataElements.get( 1 ).getUserGroupAccesses().size() );
    }

    @Test
    public void testUpdateDataElementsUID() throws IOException
    {
        defaultSetup();

        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_update1.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setPreheatMode( PreheatMode.REFERENCE );
        params.setImportMode( ImportStrategy.UPDATE );
        params.setObjects( metadata );

        Map<String, DataElement> dataElementMap = manager.getIdMap( DataElement.class, IdScheme.UID );
        UserGroup userGroup = manager.get( UserGroup.class, "ugabcdefghA" );
        assertEquals( 4, dataElementMap.size() );
        assertNotNull( userGroup );

        ObjectBundle bundle = objectBundleService.create( params );
        objectBundleService.validate( bundle );
        objectBundleService.commit( bundle );

        DataElement dataElementA = dataElementMap.get( "deabcdefghA" );
        DataElement dataElementB = dataElementMap.get( "deabcdefghB" );
        DataElement dataElementC = dataElementMap.get( "deabcdefghC" );
        DataElement dataElementD = dataElementMap.get( "deabcdefghD" );

        assertNotNull( dataElementA );
        assertNotNull( dataElementB );
        assertNotNull( dataElementC );
        assertNotNull( dataElementD );

        assertEquals( "DEA", dataElementA.getName() );
        assertEquals( "DEB", dataElementB.getName() );
        assertEquals( "DEC", dataElementC.getName() );
        assertEquals( "DED", dataElementD.getName() );

        assertEquals( "DECA", dataElementA.getCode() );
        assertEquals( "DECB", dataElementB.getCode() );
        assertEquals( "DECC", dataElementC.getCode() );
        assertEquals( "DECD", dataElementD.getCode() );

        assertEquals( "DESA", dataElementA.getShortName() );
        assertEquals( "DESB", dataElementB.getShortName() );
        assertEquals( "DESC", dataElementC.getShortName() );
        assertEquals( "DESD", dataElementD.getShortName() );

        assertEquals( "DEDA", dataElementA.getDescription() );
        assertEquals( "DEDB", dataElementB.getDescription() );
        assertEquals( "DEDC", dataElementC.getDescription() );
        assertEquals( "DEDD", dataElementD.getDescription() );

        assertEquals( 1, dataElementA.getUserGroupAccesses().size() );
        assertEquals( 0, dataElementB.getUserGroupAccesses().size() );
        assertEquals( 1, dataElementC.getUserGroupAccesses().size() );
        assertEquals( 0, dataElementD.getUserGroupAccesses().size() );
    }

    @Test
    public void testUpdateDataElementsCODE() throws IOException
    {
        defaultSetup();

        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_update2.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setPreheatIdentifier( PreheatIdentifier.CODE );
        params.setImportMode( ImportStrategy.UPDATE );
        params.setObjects( metadata );

        Map<String, DataElement> dataElementMap = manager.getIdMap( DataElement.class, IdScheme.UID );
        UserGroup userGroup = manager.get( UserGroup.class, "ugabcdefghA" );
        assertEquals( 4, dataElementMap.size() );
        assertNotNull( userGroup );

        ObjectBundle bundle = objectBundleService.create( params );
        objectBundleService.validate( bundle );
        objectBundleService.commit( bundle );

        DataElement dataElementA = dataElementMap.get( "deabcdefghA" );
        DataElement dataElementB = dataElementMap.get( "deabcdefghB" );
        DataElement dataElementC = dataElementMap.get( "deabcdefghC" );
        DataElement dataElementD = dataElementMap.get( "deabcdefghD" );

        assertNotNull( dataElementA );
        assertNotNull( dataElementB );
        assertNotNull( dataElementC );
        assertNotNull( dataElementD );

        assertEquals( "DEA", dataElementA.getName() );
        assertEquals( "DEB", dataElementB.getName() );
        assertEquals( "DEC", dataElementC.getName() );
        assertEquals( "DED", dataElementD.getName() );

        assertEquals( "DataElementCodeA", dataElementA.getCode() );
        assertEquals( "DataElementCodeB", dataElementB.getCode() );
        assertEquals( "DataElementCodeC", dataElementC.getCode() );
        assertEquals( "DataElementCodeD", dataElementD.getCode() );

        assertEquals( "DESA", dataElementA.getShortName() );
        assertEquals( "DESB", dataElementB.getShortName() );
        assertEquals( "DESC", dataElementC.getShortName() );
        assertEquals( "DESD", dataElementD.getShortName() );

        assertEquals( "DEDA", dataElementA.getDescription() );
        assertEquals( "DEDB", dataElementB.getDescription() );
        assertEquals( "DEDC", dataElementC.getDescription() );
        assertEquals( "DEDD", dataElementD.getDescription() );

        assertEquals( 1, dataElementA.getUserGroupAccesses().size() );
        assertEquals( 0, dataElementB.getUserGroupAccesses().size() );
        assertEquals( 1, dataElementC.getUserGroupAccesses().size() );
        assertEquals( 0, dataElementD.getUserGroupAccesses().size() );
    }

    @Test
    public void testCreateDataSetWithSections() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/dataset_with_sections.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );
        assertTrue( validate.getObjectErrorReports().isEmpty() );

        objectBundleService.commit( bundle );

        List<DataSet> dataSets = manager.getAll( DataSet.class );
        List<Section> sections = manager.getAll( Section.class );
        List<OrganisationUnit> organisationUnits = manager.getAll( OrganisationUnit.class );
        List<DataElement> dataElements = manager.getAll( DataElement.class );
        List<UserAuthorityGroup> userRoles = manager.getAll( UserAuthorityGroup.class );
        List<User> users = manager.getAll( User.class );

        assertFalse( organisationUnits.isEmpty() );
        assertFalse( dataElements.isEmpty() );
        assertFalse( dataSets.isEmpty() );
        assertFalse( users.isEmpty() );
        assertFalse( userRoles.isEmpty() );

        assertEquals( 1, dataSets.size() );
        assertEquals( 2, sections.size() );

        DataSet dataSet = dataSets.get( 0 );
        assertEquals( 2, dataSet.getSections().size() );

        Section section1 = sections.get( 0 );
        Section section2 = sections.get( 1 );

        assertEquals( 1, section1.getDataElements().size() );
        assertEquals( 1, section2.getDataElements().size() );

        assertNotNull( section1.getDataSet() );
        assertNotNull( section2.getDataSet() );
    }

    @Test
    public void testCreateDataSetWithSectionsAndGreyedFields() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/dataset_with_sections_gf.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );
        assertTrue( validate.getObjectErrorReports().isEmpty() );

        objectBundleService.commit( bundle );

        List<DataSet> dataSets = manager.getAll( DataSet.class );
        List<Section> sections = manager.getAll( Section.class );
        List<OrganisationUnit> organisationUnits = manager.getAll( OrganisationUnit.class );
        List<DataElement> dataElements = manager.getAll( DataElement.class );
        List<UserAuthorityGroup> userRoles = manager.getAll( UserAuthorityGroup.class );
        List<User> users = manager.getAll( User.class );
        List<DataElementOperand> dataElementOperands = manager.getAll( DataElementOperand.class );
        List<TrackedEntity> trackedEntities = manager.getAll( TrackedEntity.class );
        List<OrganisationUnitLevel> organisationUnitLevels = manager.getAll( OrganisationUnitLevel.class );

        assertFalse( organisationUnits.isEmpty() );
        assertEquals( 1, organisationUnitLevels.size() );
        assertEquals( 1, trackedEntities.size() );
        assertFalse( dataElements.isEmpty() );
        assertFalse( users.isEmpty() );
        assertFalse( userRoles.isEmpty() );

        assertEquals( 1, dataSets.size() );
        assertEquals( 2, sections.size() );
        assertEquals( 1, dataElementOperands.size() );

        DataSet dataSet = dataSets.get( 0 );
        assertEquals( 2, dataSet.getSections().size() );

        Section section1 = sections.get( 0 );
        Section section2 = sections.get( 1 );

        assertEquals( 1, section1.getDataElements().size() );
        assertEquals( 1, section2.getDataElements().size() );

        assertNotNull( section1.getDataSet() );
        assertNotNull( section2.getDataSet() );

        Section section = manager.get( Section.class, "C50M0WxaI7y" );
        assertNotNull( section.getDataSet() );
        assertNotNull( section.getCategoryCombo() );
        assertEquals( 1, section.getGreyedFields().size() );

        DataElementCategoryCombo categoryCombo = manager.get( DataElementCategoryCombo.class, "faV8QvLgIwB" );
        assertNotNull( categoryCombo );

        DataElementCategory category = manager.get( DataElementCategory.class, "XJGLlMAMCcn" );
        assertNotNull( category );

        DataElementCategoryOption categoryOption1 = manager.get( DataElementCategoryOption.class, "JYiFOMKa25J" );
        DataElementCategoryOption categoryOption2 = manager.get( DataElementCategoryOption.class, "tdaMRD34m8o" );

        assertNotNull( categoryOption1 );
        assertNotNull( categoryOption2 );
    }

    @Test
    public void testUpdateDataSetWithSectionsAndGreyedFields() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/dataset_with_sections_gf.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );
        assertTrue( validate.getObjectErrorReports().isEmpty() );

        objectBundleService.commit( bundle );

        metadata = renderService.fromMetadata( new ClassPathResource( "dxf2/dataset_with_sections_gf_update.json" ).getInputStream(), RenderFormat.JSON );

        params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.UPDATE );
        params.setObjects( metadata );

        bundle = objectBundleService.create( params );
        validate = objectBundleService.validate( bundle );
        assertTrue( validate.getObjectErrorReports().isEmpty() );

        objectBundleService.commit( bundle );

        List<DataSet> dataSets = manager.getAll( DataSet.class );
        List<Section> sections = manager.getAll( Section.class );
        List<OrganisationUnit> organisationUnits = manager.getAll( OrganisationUnit.class );
        List<DataElement> dataElements = manager.getAll( DataElement.class );
        List<UserAuthorityGroup> userRoles = manager.getAll( UserAuthorityGroup.class );
        List<User> users = manager.getAll( User.class );
        List<DataElementOperand> dataElementOperands = manager.getAll( DataElementOperand.class );

        assertFalse( organisationUnits.isEmpty() );
        assertFalse( dataElements.isEmpty() );
        assertFalse( users.isEmpty() );
        assertFalse( userRoles.isEmpty() );

        assertEquals( 1, dataSets.size() );
        assertEquals( 2, sections.size() );
        assertEquals( 1, dataElementOperands.size() );

        DataSet dataSet = dataSets.get( 0 );
        assertEquals( "Updated Data Set", dataSet.getName() );
        assertEquals( 2, dataSet.getSections().size() );
        assertNotNull( dataSet.getUser() );

        Section section1 = manager.get( Section.class, "JwcV2ZifEQf" );
        assertNotNull( section1.getDataSet() );
        assertNotNull( section1.getCategoryCombo() );
        assertEquals( 1, section1.getGreyedFields().size() );
        assertEquals( 1, section1.getDataElements().size() );
        assertNotNull( section1.getDataSet() );

        Section section2 = manager.get( Section.class, "C50M0WxaI7y" );
        assertNotNull( section2.getDataSet() );
        assertNotNull( section2.getCategoryCombo() );
        assertTrue( section2.getGreyedFields().isEmpty() );
        assertEquals( 1, section2.getDataElements().size() );
        assertNotNull( section2.getDataSet() );
    }

    @Test
    public void testCreateDataSetWithCompulsoryDataElements() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/dataset_with_compulsory.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );
        assertTrue( validate.getObjectErrorReports().isEmpty() );

        objectBundleService.commit( bundle );

        List<DataSet> dataSets = manager.getAll( DataSet.class );
        List<OrganisationUnit> organisationUnits = manager.getAll( OrganisationUnit.class );
        List<DataElement> dataElements = manager.getAll( DataElement.class );
        List<UserAuthorityGroup> userRoles = manager.getAll( UserAuthorityGroup.class );
        List<User> users = manager.getAll( User.class );
        List<DataElementOperand> dataElementOperands = manager.getAll( DataElementOperand.class );

        assertFalse( organisationUnits.isEmpty() );
        assertFalse( dataElements.isEmpty() );
        assertFalse( users.isEmpty() );
        assertFalse( userRoles.isEmpty() );

        assertEquals( 1, dataSets.size() );
        assertEquals( 1, dataElementOperands.size() );

        DataSet dataSet = dataSets.get( 0 );
        assertEquals( "DataSetA", dataSet.getName() );
        assertTrue( dataSet.getSections().isEmpty() );
        assertNotNull( dataSet.getUser() );
        assertEquals( 1, dataSet.getCompulsoryDataElementOperands().size() );
    }

    @Test
    public void testCreateMetadataWithIndicator() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/metadata_with_indicators.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );
        assertTrue( validate.getObjectErrorReports().isEmpty() );

        objectBundleService.commit( bundle );

        List<OrganisationUnit> organisationUnits = manager.getAll( OrganisationUnit.class );
        List<DataElement> dataElements = manager.getAll( DataElement.class );
        List<Indicator> indicators = manager.getAll( Indicator.class );

        assertFalse( organisationUnits.isEmpty() );
        assertEquals( 3, dataElements.size() );
        assertEquals( 1, indicators.size() );
    }

    @Test
    public void testCreateMetadataWithValidationRules() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/metadata_with_vr.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );
        assertTrue( validate.getObjectErrorReports().isEmpty() );

        objectBundleService.commit( bundle );

        List<DataSet> dataSets = manager.getAll( DataSet.class );
        List<OrganisationUnit> organisationUnits = manager.getAll( OrganisationUnit.class );
        List<DataElement> dataElements = manager.getAll( DataElement.class );
        List<UserAuthorityGroup> userRoles = manager.getAll( UserAuthorityGroup.class );
        List<User> users = manager.getAll( User.class );
        List<ValidationRule> validationRules = manager.getAll( ValidationRule.class );

        assertFalse( dataSets.isEmpty() );
        assertFalse( organisationUnits.isEmpty() );
        assertFalse( dataElements.isEmpty() );
        assertFalse( users.isEmpty() );
        assertFalse( userRoles.isEmpty() );
        assertEquals( 2, validationRules.size() );

        ValidationRule validationRule1 = manager.get( ValidationRule.class, "ztzsVjSIWg7" );
        assertNotNull( validationRule1.getLeftSide() );
        assertNotNull( validationRule1.getRightSide() );
        assertFalse( validationRule1.getLeftSide().getDataElementsInExpression().isEmpty() );
        assertFalse( validationRule1.getRightSide().getDataElementsInExpression().isEmpty() );
        assertEquals( "jocQSivF2ry", validationRule1.getLeftSide().getDataElementsInExpression().iterator().next().getUid() );
        assertEquals( "X0ypiOyoDbw", validationRule1.getRightSide().getDataElementsInExpression().iterator().next().getUid() );

        ValidationRule validationRule2 = manager.get( ValidationRule.class, "TGvH4Hiyduc" );
        assertNotNull( validationRule2.getLeftSide() );
        assertNotNull( validationRule2.getRightSide() );
        assertFalse( validationRule2.getLeftSide().getDataElementsInExpression().isEmpty() );
        assertFalse( validationRule2.getRightSide().getDataElementsInExpression().isEmpty() );
        assertEquals( "jocQSivF2ry", validationRule2.getLeftSide().getDataElementsInExpression().iterator().next().getUid() );
        assertEquals( "X0ypiOyoDbw", validationRule2.getRightSide().getDataElementsInExpression().iterator().next().getUid() );
    }

    @Test
    public void testUpdateMetadataWithValidationRules() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/metadata_with_vr.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );
        assertTrue( validate.getObjectErrorReports().isEmpty() );

        objectBundleService.commit( bundle );

        metadata = renderService.fromMetadata( new ClassPathResource( "dxf2/metadata_with_vr_update.json" ).getInputStream(), RenderFormat.JSON );

        params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.UPDATE );
        params.setObjects( metadata );

        bundle = objectBundleService.create( params );
        validate = objectBundleService.validate( bundle );
        assertTrue( validate.getObjectErrorReports().isEmpty() );

        objectBundleService.commit( bundle );

        List<DataSet> dataSets = manager.getAll( DataSet.class );
        List<OrganisationUnit> organisationUnits = manager.getAll( OrganisationUnit.class );
        List<DataElement> dataElements = manager.getAll( DataElement.class );
        List<UserAuthorityGroup> userRoles = manager.getAll( UserAuthorityGroup.class );
        List<User> users = manager.getAll( User.class );
        List<ValidationRule> validationRules = manager.getAll( ValidationRule.class );

        assertFalse( dataSets.isEmpty() );
        assertFalse( organisationUnits.isEmpty() );
        assertFalse( dataElements.isEmpty() );
        assertFalse( users.isEmpty() );
        assertFalse( userRoles.isEmpty() );
        assertEquals( 2, validationRules.size() );

        ValidationRule validationRule1 = manager.get( ValidationRule.class, "ztzsVjSIWg7" );
        assertNotNull( validationRule1.getLeftSide() );
        assertNotNull( validationRule1.getRightSide() );
        assertFalse( validationRule1.getLeftSide().getDataElementsInExpression().isEmpty() );
        assertFalse( validationRule1.getRightSide().getDataElementsInExpression().isEmpty() );
        assertEquals( "vAczVs4mxna", validationRule1.getLeftSide().getDataElementsInExpression().iterator().next().getUid() );
        assertEquals( "X0ypiOyoDbw", validationRule1.getRightSide().getDataElementsInExpression().iterator().next().getUid() );

        ValidationRule validationRule2 = manager.get( ValidationRule.class, "TGvH4Hiyduc" );
        assertNotNull( validationRule2.getLeftSide() );
        assertNotNull( validationRule2.getRightSide() );
        assertFalse( validationRule2.getLeftSide().getDataElementsInExpression().isEmpty() );
        assertFalse( validationRule2.getRightSide().getDataElementsInExpression().isEmpty() );
        assertEquals( "jocQSivF2ry", validationRule2.getLeftSide().getDataElementsInExpression().iterator().next().getUid() );
        assertEquals( "vAczVs4mxna", validationRule2.getRightSide().getDataElementsInExpression().iterator().next().getUid() );
    }

    @Test
    public void testCreateUsers() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/users.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );
        assertTrue( validate.getObjectErrorReports().isEmpty() );
        objectBundleService.commit( bundle );

        List<User> users = manager.getAll( User.class );
        assertEquals( 3, users.size() );

        User userA = manager.get( User.class, "sPWjoHSY03y" );
        User userB = manager.get( User.class, "MwhEJUnTHkn" );

        assertNotNull( userA );
        assertNotNull( userB );

        assertNotNull( userA.getUserCredentials().getUserInfo() );
        assertNotNull( userB.getUserCredentials().getUserInfo() );
        assertNotNull( userA.getUserCredentials().getUserInfo().getUserCredentials() );
        assertNotNull( userB.getUserCredentials().getUserInfo().getUserCredentials() );
        assertEquals( "UserA", userA.getUserCredentials().getUserInfo().getUserCredentials().getUsername() );
        assertEquals( "UserB", userB.getUserCredentials().getUserInfo().getUserCredentials().getUsername() );

        assertNotNull( userA.getUserCredentials().getUser() );
        assertNotNull( userB.getUserCredentials().getUser() );
        assertNotNull( userA.getUserCredentials().getUser().getUserCredentials() );
        assertNotNull( userB.getUserCredentials().getUser().getUserCredentials() );
        assertEquals( "admin", userA.getUserCredentials().getUser().getUserCredentials().getUsername() );
        assertEquals( "admin", userB.getUserCredentials().getUser().getUserCredentials().getUsername() );
    }

    @Test
    public void testUpdateUsers() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/users.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );
        assertTrue( validate.getObjectErrorReports().isEmpty() );
        objectBundleService.commit( bundle );

        metadata = renderService.fromMetadata( new ClassPathResource( "dxf2/users_update.json" ).getInputStream(), RenderFormat.JSON );

        params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.UPDATE );
        params.setObjects( metadata );

        bundle = objectBundleService.create( params );
        validate = objectBundleService.validate( bundle );
        assertTrue( validate.getObjectErrorReports().isEmpty() );
        objectBundleService.commit( bundle );

        List<User> users = manager.getAll( User.class );
        assertEquals( 3, users.size() );

        User userA = manager.get( User.class, "sPWjoHSY03y" );
        User userB = manager.get( User.class, "MwhEJUnTHkn" );

        assertNotNull( userA );
        assertNotNull( userB );

        assertNotNull( userA.getUserCredentials().getUserInfo() );
        assertNotNull( userB.getUserCredentials().getUserInfo() );
        assertNotNull( userA.getUserCredentials().getUserInfo().getUserCredentials() );
        assertNotNull( userB.getUserCredentials().getUserInfo().getUserCredentials() );
        assertEquals( "UserAA", userA.getUserCredentials().getUserInfo().getUserCredentials().getUsername() );
        assertEquals( "UserBB", userB.getUserCredentials().getUserInfo().getUserCredentials().getUsername() );

        assertNotNull( userA.getUserCredentials().getUser() );
        assertNotNull( userB.getUserCredentials().getUser() );
        assertNotNull( userA.getUserCredentials().getUser().getUserCredentials() );
        assertNotNull( userB.getUserCredentials().getUser().getUserCredentials() );
        assertEquals( "admin", userA.getUserCredentials().getUser().getUserCredentials().getUsername() );
        assertEquals( "admin", userB.getUserCredentials().getUser().getUserCredentials().getUsername() );
    }

    @Test
    public void testCreateAndUpdateMetadata1() throws IOException
    {
        defaultSetup();

        Map<String, DataElement> dataElementMap = manager.getIdMap( DataElement.class, IdScheme.UID );
        UserGroup userGroup = manager.get( UserGroup.class, "ugabcdefghA" );
        assertEquals( 4, dataElementMap.size() );
        assertNotNull( userGroup );

        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_create_and_update1.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE_AND_UPDATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        assertTrue( objectBundleService.validate( bundle ).getObjectErrorReports().isEmpty() );
        objectBundleService.commit( bundle );

        DataElement dataElementA = dataElementMap.get( "deabcdefghA" );
        DataElement dataElementB = dataElementMap.get( "deabcdefghB" );
        DataElement dataElementC = dataElementMap.get( "deabcdefghC" );
        DataElement dataElementD = dataElementMap.get( "deabcdefghD" );

        assertNotNull( dataElementA );
        assertNotNull( dataElementB );
        assertNotNull( dataElementC );
        assertNotNull( dataElementD );

        assertEquals( "DEA", dataElementA.getName() );
        assertEquals( "DEB", dataElementB.getName() );
        assertEquals( "DEC", dataElementC.getName() );
        assertEquals( "DED", dataElementD.getName() );

        assertEquals( "DECA", dataElementA.getCode() );
        assertEquals( "DECB", dataElementB.getCode() );
        assertEquals( "DECC", dataElementC.getCode() );
        assertEquals( "DECD", dataElementD.getCode() );

        assertEquals( "DESA", dataElementA.getShortName() );
        assertEquals( "DESB", dataElementB.getShortName() );
        assertEquals( "DESC", dataElementC.getShortName() );
        assertEquals( "DESD", dataElementD.getShortName() );

        assertEquals( "DEDA", dataElementA.getDescription() );
        assertEquals( "DEDB", dataElementB.getDescription() );
        assertEquals( "DEDC", dataElementC.getDescription() );
        assertEquals( "DEDD", dataElementD.getDescription() );

        assertEquals( 1, dataElementA.getUserGroupAccesses().size() );
        assertEquals( 0, dataElementB.getUserGroupAccesses().size() );
        assertEquals( 1, dataElementC.getUserGroupAccesses().size() );
        assertEquals( 0, dataElementD.getUserGroupAccesses().size() );
    }

    @Test
    public void testCreateAndUpdateMetadata2() throws IOException
    {
        defaultSetup();

        Map<String, DataElement> dataElementMap = manager.getIdMap( DataElement.class, IdScheme.UID );
        UserGroup userGroup = manager.get( UserGroup.class, "ugabcdefghA" );
        assertEquals( 4, dataElementMap.size() );
        assertNotNull( userGroup );

        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_create_and_update2.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE_AND_UPDATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        assertTrue( objectBundleService.validate( bundle ).getObjectErrorReports().isEmpty() );
        objectBundleService.commit( bundle );

        DataElement dataElementA = manager.get( DataElement.class, "deabcdefghA" );
        DataElement dataElementB = manager.get( DataElement.class, "deabcdefghB" );
        DataElement dataElementC = manager.get( DataElement.class, "deabcdefghC" );
        DataElement dataElementD = manager.get( DataElement.class, "deabcdefghD" );
        DataElement dataElementE = manager.get( DataElement.class, "deabcdefghE" );

        assertNotNull( dataElementA );
        assertNotNull( dataElementB );
        assertNotNull( dataElementC );
        assertNotNull( dataElementD );
        assertNotNull( dataElementE );

        assertEquals( "DEA", dataElementA.getName() );
        assertEquals( "DEB", dataElementB.getName() );
        assertEquals( "DEC", dataElementC.getName() );
        assertEquals( "DED", dataElementD.getName() );
        assertEquals( "DEE", dataElementE.getName() );

        assertEquals( "DECA", dataElementA.getCode() );
        assertEquals( "DECB", dataElementB.getCode() );
        assertEquals( "DECC", dataElementC.getCode() );
        assertEquals( "DECD", dataElementD.getCode() );
        assertEquals( "DECE", dataElementE.getCode() );

        assertEquals( "DESA", dataElementA.getShortName() );
        assertEquals( "DESB", dataElementB.getShortName() );
        assertEquals( "DESC", dataElementC.getShortName() );
        assertEquals( "DESD", dataElementD.getShortName() );
        assertEquals( "DESE", dataElementE.getShortName() );

        assertEquals( "DEDA", dataElementA.getDescription() );
        assertEquals( "DEDB", dataElementB.getDescription() );
        assertEquals( "DEDC", dataElementC.getDescription() );
        assertEquals( "DEDD", dataElementD.getDescription() );
        assertEquals( "DEDE", dataElementE.getDescription() );

        assertEquals( 1, dataElementA.getUserGroupAccesses().size() );
        assertEquals( 0, dataElementB.getUserGroupAccesses().size() );
        assertEquals( 1, dataElementC.getUserGroupAccesses().size() );
        assertEquals( 0, dataElementD.getUserGroupAccesses().size() );
    }

    @Test
    public void testCreateAndUpdateMetadata3() throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/de_create_and_update3.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE_AND_UPDATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        assertTrue( objectBundleService.validate( bundle ).getObjectErrorReports().isEmpty() );
        objectBundleService.commit( bundle );

        DataElement dataElementE = manager.get( DataElement.class, "deabcdefghE" );

        assertNotNull( dataElementE );
        assertEquals( "DEE", dataElementE.getName() );
        assertEquals( "DECE", dataElementE.getCode() );
        assertEquals( "DESE", dataElementE.getShortName() );
        assertEquals( "DEDE", dataElementE.getDescription() );
    }

    @Test
    public void testCreateMetadataWithSuperuserRoleInjected() throws IOException
    {
        createUserAndInjectSecurityContext( true );

        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "dxf2/metadata_superuser_bug.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportMode( ImportStrategy.CREATE_AND_UPDATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidation validate = objectBundleService.validate( bundle );

        assertFalse( validate.getObjectErrorReports().isEmpty() );
        assertEquals( 1, validate.getErrorReportsByCode( UserAuthorityGroup.class, ErrorCode.E5003 ).size() );
    }

    private void defaultSetup()
    {
        DataElement de1 = createDataElement( 'A' );
        DataElement de2 = createDataElement( 'B' );
        DataElement de3 = createDataElement( 'C' );
        DataElement de4 = createDataElement( 'D' );

        manager.save( de1 );
        manager.save( de2 );
        manager.save( de3 );
        manager.save( de4 );

        User user = createUser( 'A' );
        manager.save( user );

        UserGroup userGroup = createUserGroup( 'A', Sets.newHashSet( user ) );
        manager.save( userGroup );
    }
}