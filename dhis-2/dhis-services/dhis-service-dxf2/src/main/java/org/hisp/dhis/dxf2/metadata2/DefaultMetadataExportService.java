package org.hisp.dhis.dxf2.metadata2;

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
import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategory;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementCategoryOption;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementOperand;
import org.hisp.dhis.dataentryform.DataEntryForm;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.dxf2.common.OrderParams;
import org.hisp.dhis.fieldfilter.FieldFilterService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.legend.Legend;
import org.hisp.dhis.legend.LegendSet;
import org.hisp.dhis.node.NodeUtils;
import org.hisp.dhis.node.types.RootNode;
import org.hisp.dhis.node.types.SimpleNode;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.query.Query;
import org.hisp.dhis.query.QueryService;
import org.hisp.dhis.schema.Schema;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Component
public class DefaultMetadataExportService implements MetadataExportService
{
    private static final Log log = LogFactory.getLog( MetadataExportService.class );

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private QueryService queryService;

    @Autowired
    private FieldFilterService fieldFilterService;

    @Autowired
    private CurrentUserService currentUserService;

    @Override
    @SuppressWarnings( "unchecked" )
    public Map<Class<? extends IdentifiableObject>, List<? extends IdentifiableObject>> getMetadata( MetadataExportParams params )
    {
        Map<Class<? extends IdentifiableObject>, List<? extends IdentifiableObject>> metadata = new HashMap<>();

        if ( params.getClasses().isEmpty() )
        {
            schemaService.getMetadataSchemas().stream().filter( Schema::isIdentifiableObject )
                .forEach( schema -> params.getClasses().add( (Class<? extends IdentifiableObject>) schema.getKlass() ) );
        }

        log.info( "Export started at " + new Date() );

        User defaultUser = currentUserService.getCurrentUser();

        for ( Class<? extends IdentifiableObject> klass : params.getClasses() )
        {
            Query query;

            if ( params.getQuery( klass ) != null )
            {
                query = params.getQuery( klass );
            }
            else
            {
                OrderParams orderParams = new OrderParams( Sets.newHashSet( params.getDefaultOrder() ) );
                query = queryService.getQueryFromUrl( klass, params.getDefaultFilter(), orderParams.getOrders( schemaService.getDynamicSchema( klass ) ) );
            }

            if ( query.getUser() == null )
            {
                query.setUser( defaultUser );
            }

            query.setDefaultOrder();
            List<? extends IdentifiableObject> objects = queryService.query( query );

            if ( !objects.isEmpty() )
            {
                log.info( "Exported " + objects.size() + " objects of type " + klass.getSimpleName() );
                metadata.put( klass, objects );
            }
        }

        log.info( "Export done at " + new Date() );

        return metadata;
    }

    @Override
    public RootNode getMetadataAsNode( MetadataExportParams params )
    {
        RootNode rootNode = NodeUtils.createMetadata();
        rootNode.addChild( new SimpleNode( "date", new Date(), true ) );

        Map<Class<? extends IdentifiableObject>, List<? extends IdentifiableObject>> metadata = getMetadata( params );

        for ( Class<? extends IdentifiableObject> klass : metadata.keySet() )
        {
            rootNode.addChild( fieldFilterService.filter( klass, metadata.get( klass ), params.getFields( klass ) ) );
        }

        return rootNode;
    }

    @Override
    public void validate( MetadataExportParams params )
    {

    }

    @Override
    @SuppressWarnings( "unchecked" )
    public MetadataExportParams getParamsFromMap( Map<String, List<String>> parameters )
    {
        MetadataExportParams params = new MetadataExportParams();
        Map<Class<? extends IdentifiableObject>, Map<String, List<String>>> map = new HashMap<>();

        if ( parameters.containsKey( "fields" ) )
        {
            params.setDefaultFields( parameters.get( "fields" ) );
            parameters.remove( "fields" );
        }

        if ( parameters.containsKey( "filter" ) )
        {
            params.setDefaultFilter( parameters.get( "filter" ) );
            parameters.remove( "filter" );
        }

        if ( parameters.containsKey( "order" ) )
        {
            params.setDefaultOrder( parameters.get( "order" ) );
            parameters.remove( "order" );
        }

        for ( String parameterKey : parameters.keySet() )
        {
            String[] parameter = parameterKey.split( ":" );
            Schema schema = schemaService.getSchemaByPluralName( parameter[0] );

            if ( schema == null || !schema.isIdentifiableObject() )
            {
                continue;
            }

            Class<? extends IdentifiableObject> klass = (Class<? extends IdentifiableObject>) schema.getKlass();

            // class is enabled if value = true, or fields/filter/order is present
            if ( "true".equalsIgnoreCase( parameters.get( parameterKey ).get( 0 ) ) || (parameter.length > 1 && ("fields".equalsIgnoreCase( parameter[1] )
                || "filter".equalsIgnoreCase( parameter[1] ) || "order".equalsIgnoreCase( parameter[1] ))) )
            {
                if ( !map.containsKey( klass ) ) map.put( klass, new HashMap<>() );
            }
            else
            {
                continue;
            }

            if ( parameter.length > 1 )
            {
                if ( "fields".equalsIgnoreCase( parameter[1] ) )
                {
                    if ( !map.get( klass ).containsKey( "fields" ) ) map.get( klass ).put( "fields", new ArrayList<>() );
                    map.get( klass ).get( "fields" ).addAll( parameters.get( parameterKey ) );
                }

                if ( "filter".equalsIgnoreCase( parameter[1] ) )
                {
                    if ( !map.get( klass ).containsKey( "filter" ) ) map.get( klass ).put( "filter", new ArrayList<>() );
                    map.get( klass ).get( "filter" ).addAll( parameters.get( parameterKey ) );
                }

                if ( "order".equalsIgnoreCase( parameter[1] ) )
                {
                    if ( !map.get( klass ).containsKey( "order" ) ) map.get( klass ).put( "order", new ArrayList<>() );
                    map.get( klass ).get( "order" ).addAll( parameters.get( parameterKey ) );
                }
            }
        }

        map.keySet().forEach( params::addClass );

        for ( Class<? extends IdentifiableObject> klass : map.keySet() )
        {
            Map<String, List<String>> classMap = map.get( klass );
            Schema schema = schemaService.getDynamicSchema( klass );

            if ( classMap.containsKey( "fields" ) ) params.addFields( klass, classMap.get( "fields" ) );

            if ( classMap.containsKey( "filter" ) && classMap.containsKey( "order" ) )
            {
                OrderParams orderParams = new OrderParams( Sets.newHashSet( classMap.get( "order" ) ) );
                Query query = queryService.getQueryFromUrl( klass, classMap.get( "filter" ), orderParams.getOrders( schema ) );
                query.setDefaultOrder();
                params.addQuery( query );
            }
            else if ( classMap.containsKey( "filter" ) )
            {
                Query query = queryService.getQueryFromUrl( klass, classMap.get( "filter" ), new ArrayList<>() );
                query.setDefaultOrder();
                params.addQuery( query );
            }
            else if ( classMap.containsKey( "order" ) )
            {
                OrderParams orderParams = new OrderParams();
                orderParams.setOrder( Sets.newHashSet( classMap.get( "order" ) ) );

                Query query = queryService.getQueryFromUrl( klass, new ArrayList<>(), orderParams.getOrders( schema ) );
                query.setDefaultOrder();
                params.addQuery( query );
            }
        }

        return params;
    }

    @Override
    public Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> getMetadataWithDependencies( IdentifiableObject object )
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = new HashMap<>();

        if ( DataSet.class.isInstance( object ) ) return handleDataSet( metadata, (DataSet) object );
        if ( Program.class.isInstance( object ) ) return handleProgram( metadata, (Program) object );

        return metadata;
    }

    @Override
    public RootNode getMetadataWithDependenciesAsNode( IdentifiableObject object )
    {
        RootNode rootNode = NodeUtils.createMetadata();
        rootNode.addChild( new SimpleNode( "date", new Date(), true ) );

        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = getMetadataWithDependencies( object );

        for ( Class<? extends IdentifiableObject> klass : metadata.keySet() )
        {
            rootNode.addChild( fieldFilterService.filter( klass, metadata.get( klass ), Lists.newArrayList( ":owner" ) ) );
        }

        return rootNode;
    }

    //-----------------------------------------------------------------------------------
    // Utility Methods
    //-----------------------------------------------------------------------------------

    private Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> handleDataSet( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata, DataSet dataSet )
    {
        if ( !metadata.containsKey( DataSet.class ) ) metadata.put( DataSet.class, new ArrayList<>() );
        metadata.get( DataSet.class ).add( dataSet );

        dataSet.getDataElements().forEach( dataElement -> handleDataElement( metadata, dataElement ) );
        dataSet.getSections().forEach( section -> handleSection( metadata, section ) );
        dataSet.getIndicators().forEach( indicator -> handleIndicator( metadata, indicator ) );

        handleDataEntryForm( metadata, dataSet.getDataEntryForm() );
        handleLegendSet( metadata, dataSet.getLegendSet() );
        handleCategoryCombo( metadata, dataSet.getCategoryCombo() );

        dataSet.getCompulsoryDataElementOperands().forEach( dataElementOperand -> handleDataElementOperand( metadata, dataElementOperand ) );

        return metadata;
    }

    private Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> handleDataElementOperand( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata, DataElementOperand dataElementOperand )
    {
        if ( dataElementOperand == null ) return metadata;

        handleCategoryOptionCombo( metadata, dataElementOperand.getCategoryOptionCombo() );
        handleLegendSet( metadata, dataElementOperand.getLegendSet() );

        return metadata;
    }

    private Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> handleCategoryOptionCombo( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata, DataElementCategoryOptionCombo categoryOptionCombo )
    {
        if ( categoryOptionCombo == null ) return metadata;
        if ( !metadata.containsKey( DataElementCategoryOptionCombo.class ) ) metadata.put( DataElementCategoryOptionCombo.class, new ArrayList<>() );
        metadata.get( DataElementCategoryOptionCombo.class ).add( categoryOptionCombo );

        categoryOptionCombo.getCategoryOptions().forEach( categoryOption -> handleCategoryOption( metadata, categoryOption ) );

        return metadata;
    }

    private Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> handleCategoryCombo( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata, DataElementCategoryCombo categoryCombo )
    {
        if ( categoryCombo == null ) return metadata;
        if ( !metadata.containsKey( DataElementCategoryCombo.class ) ) metadata.put( DataElementCategoryCombo.class, new ArrayList<>() );
        metadata.get( DataElementCategoryCombo.class ).add( categoryCombo );

        categoryCombo.getCategories().forEach( category -> handleCategory( metadata, category ) );

        return metadata;
    }

    private Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> handleCategory( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata, DataElementCategory category )
    {
        if ( category == null ) return metadata;
        if ( !metadata.containsKey( DataElementCategory.class ) ) metadata.put( DataElementCategory.class, new ArrayList<>() );
        metadata.get( DataElementCategory.class ).add( category );

        category.getCategoryOptions().forEach( categoryOption -> handleCategoryOption( metadata, categoryOption ) );

        return metadata;
    }

    private Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> handleCategoryOption( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata, DataElementCategoryOption categoryOption )
    {
        if ( categoryOption == null ) return metadata;
        if ( !metadata.containsKey( DataElementCategoryOption.class ) ) metadata.put( DataElementCategoryOption.class, new ArrayList<>() );
        metadata.get( DataElementCategoryOption.class ).add( categoryOption );

        return metadata;
    }

    private Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> handleLegendSet( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata, LegendSet legendSet )
    {
        if ( legendSet == null ) return metadata;
        if ( !metadata.containsKey( LegendSet.class ) ) metadata.put( LegendSet.class, new ArrayList<>() );
        metadata.get( LegendSet.class ).add( legendSet );

        legendSet.getLegends().forEach( legend -> handleLegend( metadata, legend ) );

        return metadata;
    }

    private Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> handleLegend( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata, Legend legend )
    {
        if ( legend == null ) return metadata;
        if ( !metadata.containsKey( Legend.class ) ) metadata.put( Legend.class, new ArrayList<>() );
        metadata.get( Legend.class ).add( legend );

        return metadata;
    }

    private Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> handleDataEntryForm( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata, DataEntryForm dataEntryForm )
    {
        if ( dataEntryForm == null ) return metadata;
        if ( !metadata.containsKey( DataEntryForm.class ) ) metadata.put( DataEntryForm.class, new ArrayList<>() );
        metadata.get( DataEntryForm.class ).add( dataEntryForm );

        return metadata;
    }

    private Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> handleDataElement( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata, DataElement dataElement )
    {
        if ( dataElement == null ) return metadata;
        if ( !metadata.containsKey( DataElement.class ) ) metadata.put( DataElement.class, new ArrayList<>() );
        metadata.get( DataElement.class ).add( dataElement );

        handleCategoryCombo( metadata, dataElement.getCategoryCombo() );

        return metadata;
    }

    private Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> handleSection( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata, Section section )
    {
        if ( section == null ) return metadata;
        if ( !metadata.containsKey( Section.class ) ) metadata.put( Section.class, new ArrayList<>() );
        metadata.get( Section.class ).add( section );

        return metadata;
    }

    private Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> handleIndicator( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata, Indicator indicator )
    {
        if ( indicator == null ) return metadata;
        if ( !metadata.containsKey( Indicator.class ) ) metadata.put( Indicator.class, new ArrayList<>() );
        metadata.get( Indicator.class ).add( indicator );

        return metadata;
    }

    private Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> handleProgram( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata, Program program )
    {
        if ( program == null ) return metadata;
        if ( !metadata.containsKey( Program.class ) ) metadata.put( Program.class, new ArrayList<>() );
        metadata.get( Program.class ).add( program );

        return metadata;
    }
}
