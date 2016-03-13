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
import org.hisp.dhis.attribute.Attribute;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.common.MergeMode;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementOperand;
import org.hisp.dhis.feedback.ErrorCode;
import org.hisp.dhis.feedback.ErrorReport;
import org.hisp.dhis.feedback.ObjectErrorReport;
import org.hisp.dhis.query.Query;
import org.hisp.dhis.query.QueryService;
import org.hisp.dhis.query.Restrictions;
import org.hisp.dhis.schema.Property;
import org.hisp.dhis.schema.PropertyType;
import org.hisp.dhis.schema.Schema;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.system.util.ReflectionUtils;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserCredentials;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.validation.ValidationRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Transactional
public class DefaultPreheatService implements PreheatService
{
    @Autowired
    private SchemaService schemaService;

    @Autowired
    private QueryService queryService;

    @Autowired
    private IdentifiableObjectManager manager;

    @Autowired
    private CurrentUserService currentUserService;

    @Override
    @SuppressWarnings( "unchecked" )
    public Preheat preheat( PreheatParams params )
    {
        Preheat preheat = new Preheat();
        preheat.setUser( params.getUser() );
        preheat.setDefaults( manager.getDefaults() );
        preheat.setUsernames( getUsernames() );

        if ( preheat.getUser() == null )
        {
            preheat.setUser( currentUserService.getCurrentUser() );
        }

        if ( PreheatMode.ALL == params.getPreheatMode() )
        {
            if ( params.getClasses().isEmpty() )
            {
                schemaService.getMetadataSchemas().stream().filter( Schema::isIdentifiableObject )
                    .forEach( schema -> params.getClasses().add( (Class<? extends IdentifiableObject>) schema.getKlass() ) );
            }

            for ( Class<? extends IdentifiableObject> klass : params.getClasses() )
            {
                Query query = Query.from( schemaService.getDynamicSchema( klass ) );
                query.setUser( preheat.getUser() );
                List<? extends IdentifiableObject> objects = queryService.query( query );

                if ( PreheatIdentifier.UID == params.getPreheatIdentifier() || PreheatIdentifier.AUTO == params.getPreheatIdentifier() )
                {
                    preheat.put( PreheatIdentifier.UID, objects );
                }

                if ( PreheatIdentifier.CODE == params.getPreheatIdentifier() || PreheatIdentifier.AUTO == params.getPreheatIdentifier() )
                {
                    preheat.put( PreheatIdentifier.CODE, objects );
                }
            }
        }
        else if ( PreheatMode.REFERENCE == params.getPreheatMode() )
        {
            Map<Class<? extends IdentifiableObject>, Set<String>> uidMap = params.getReferences().get( PreheatIdentifier.UID );
            Map<Class<? extends IdentifiableObject>, Set<String>> codeMap = params.getReferences().get( PreheatIdentifier.CODE );

            if ( uidMap != null && (PreheatIdentifier.UID == params.getPreheatIdentifier() || PreheatIdentifier.AUTO == params.getPreheatIdentifier()) )
            {
                for ( Class<? extends IdentifiableObject> klass : uidMap.keySet() )
                {
                    Collection<String> identifiers = uidMap.get( klass );

                    if ( !identifiers.isEmpty() )
                    {
                        Query query = Query.from( schemaService.getDynamicSchema( klass ) );
                        query.setUser( preheat.getUser() );
                        query.add( Restrictions.in( "id", identifiers ) );
                        List<? extends IdentifiableObject> objects = queryService.query( query );
                        preheat.put( PreheatIdentifier.UID, objects );
                    }
                }
            }

            if ( codeMap != null && (PreheatIdentifier.CODE == params.getPreheatIdentifier() || PreheatIdentifier.AUTO == params.getPreheatIdentifier()) )
            {
                for ( Class<? extends IdentifiableObject> klass : codeMap.keySet() )
                {
                    Collection<String> identifiers = codeMap.get( klass );

                    if ( !identifiers.isEmpty() )
                    {
                        Query query = Query.from( schemaService.getDynamicSchema( klass ) );
                        query.setUser( preheat.getUser() );
                        query.add( Restrictions.in( "code", identifiers ) );
                        List<? extends IdentifiableObject> objects = queryService.query( query );
                        preheat.put( PreheatIdentifier.CODE, objects );
                    }
                }
            }
        }

        return preheat;
    }

    @Override
    public void validate( PreheatParams params ) throws PreheatException
    {
        if ( PreheatMode.ALL == params.getPreheatMode() || PreheatMode.NONE == params.getPreheatMode() )
        {
            // nothing to validate for now, if classes is empty it will get all metadata classes
        }
        else if ( PreheatMode.REFERENCE == params.getPreheatMode() )
        {
            if ( params.getReferences().isEmpty() )
            {
                throw new PreheatException( "PreheatMode.REFERENCE, but no references was provided." );
            }
        }
        else
        {
            throw new PreheatException( "Invalid preheat mode." );
        }
    }

    @Override
    public Map<PreheatIdentifier, Map<Class<? extends IdentifiableObject>, Set<String>>> collectReferences( IdentifiableObject object )
    {
        if ( object == null )
        {
            return new HashMap<>();
        }

        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> map = new HashMap<>();
        map.put( object.getClass(), Lists.newArrayList( (IdentifiableObject) object ) );

        return collectReferences( map );
    }

    @Override
    public Map<PreheatIdentifier, Map<Class<? extends IdentifiableObject>, Set<String>>> collectReferences( Collection<IdentifiableObject> objects )
    {
        if ( objects == null || objects.isEmpty() )
        {
            return new HashMap<>();
        }

        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> map = new HashMap<>();
        map.put( objects.iterator().next().getClass(), Lists.newArrayList( objects ) );

        return collectReferences( map );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Map<PreheatIdentifier, Map<Class<? extends IdentifiableObject>, Set<String>>> collectReferences( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> objects )
    {
        Map<PreheatIdentifier, Map<Class<? extends IdentifiableObject>, Set<String>>> map = new HashMap<>();

        map.put( PreheatIdentifier.UID, new HashMap<>() );
        map.put( PreheatIdentifier.CODE, new HashMap<>() );

        Map<Class<? extends IdentifiableObject>, Set<String>> uidMap = map.get( PreheatIdentifier.UID );
        Map<Class<? extends IdentifiableObject>, Set<String>> codeMap = map.get( PreheatIdentifier.CODE );
        Map<Class<? extends IdentifiableObject>, Map<String, Map<Object, String>>> uniqueMap = new HashMap<>();

        if ( objects.isEmpty() )
        {
            return map;
        }

        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> scanObjects = new HashMap<>();
        scanObjects.putAll( objects ); // clone objects list, we don't want to modify it

        if ( scanObjects.containsKey( User.class ) )
        {
            List<IdentifiableObject> users = scanObjects.get( User.class );
            List<IdentifiableObject> userCredentials = new ArrayList<>();

            for ( IdentifiableObject identifiableObject : users )
            {
                User user = (User) identifiableObject;

                if ( user.getUserCredentials() != null )
                {
                    userCredentials.add( user.getUserCredentials() );
                }
            }

            scanObjects.put( UserCredentials.class, userCredentials );
        }

        for ( Class<? extends IdentifiableObject> objectClass : scanObjects.keySet() )
        {
            Schema schema = schemaService.getDynamicSchema( objectClass );
            List<Property> identifiableProperties = schema.getProperties().stream()
                .filter( p -> p.isPersisted() && p.isOwner() && (PropertyType.REFERENCE == p.getPropertyType() || PropertyType.REFERENCE == p.getItemPropertyType()) )
                .collect( Collectors.toList() );

            List<IdentifiableObject> identifiableObjects = scanObjects.get( objectClass );

            if ( !uidMap.containsKey( objectClass ) ) uidMap.put( objectClass, new HashSet<>() );
            if ( !codeMap.containsKey( objectClass ) ) codeMap.put( objectClass, new HashSet<>() );

            for ( IdentifiableObject object : identifiableObjects )
            {
                identifiableProperties.forEach( p -> {
                    if ( !p.isCollection() )
                    {
                        Class<? extends IdentifiableObject> klass = (Class<? extends IdentifiableObject>) p.getKlass();

                        if ( !uidMap.containsKey( klass ) ) uidMap.put( klass, new HashSet<>() );
                        if ( !codeMap.containsKey( klass ) ) codeMap.put( klass, new HashSet<>() );

                        Object reference = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );

                        if ( reference != null )
                        {
                            IdentifiableObject identifiableObject = (IdentifiableObject) reference;

                            String uid = identifiableObject.getUid();
                            String code = identifiableObject.getCode();

                            if ( !StringUtils.isEmpty( uid ) ) uidMap.get( klass ).add( uid );
                            if ( !StringUtils.isEmpty( code ) ) codeMap.get( klass ).add( code );
                        }
                    }
                    else
                    {
                        Class<? extends IdentifiableObject> klass = (Class<? extends IdentifiableObject>) p.getItemKlass();

                        if ( !uidMap.containsKey( klass ) ) uidMap.put( klass, new HashSet<>() );
                        if ( !codeMap.containsKey( klass ) ) codeMap.put( klass, new HashSet<>() );

                        Collection<IdentifiableObject> reference = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );

                        for ( IdentifiableObject identifiableObject : reference )
                        {
                            String uid = identifiableObject.getUid();
                            String code = identifiableObject.getCode();

                            if ( !StringUtils.isEmpty( uid ) ) uidMap.get( klass ).add( uid );
                            if ( !StringUtils.isEmpty( code ) ) codeMap.get( klass ).add( code );
                        }
                    }
                } );

                if ( ValidationRule.class.isInstance( object ) )
                {
                    ValidationRule validationRule = (ValidationRule) object;

                    if ( !uidMap.containsKey( DataElement.class ) ) uidMap.put( DataElement.class, new HashSet<>() );
                    if ( !codeMap.containsKey( DataElement.class ) ) codeMap.put( DataElement.class, new HashSet<>() );

                    if ( validationRule.getLeftSide() != null && !validationRule.getLeftSide().getDataElementsInExpression().isEmpty() )
                    {
                        validationRule.getLeftSide().getDataElementsInExpression().stream()
                            .forEach( de -> {
                                if ( !StringUtils.isEmpty( de.getUid() ) ) uidMap.get( DataElement.class ).add( de.getUid() );
                                if ( !StringUtils.isEmpty( de.getCode() ) ) codeMap.get( DataElement.class ).add( de.getCode() );
                            } );
                    }

                    if ( validationRule.getRightSide() != null && !validationRule.getRightSide().getDataElementsInExpression().isEmpty() )
                    {
                        validationRule.getRightSide().getDataElementsInExpression().stream()
                            .forEach( de -> {
                                if ( !StringUtils.isEmpty( de.getUid() ) ) uidMap.get( DataElement.class ).add( de.getUid() );
                                if ( !StringUtils.isEmpty( de.getCode() ) ) codeMap.get( DataElement.class ).add( de.getCode() );
                            } );
                    }

                    if ( uidMap.get( DataElement.class ).isEmpty() ) uidMap.remove( DataElement.class );
                    if ( codeMap.get( DataElement.class ).isEmpty() ) codeMap.remove( DataElement.class );
                }

                if ( !uidMap.containsKey( Attribute.class ) ) uidMap.put( Attribute.class, new HashSet<>() );
                if ( !codeMap.containsKey( Attribute.class ) ) codeMap.put( Attribute.class, new HashSet<>() );

                object.getAttributeValues().forEach( av -> {
                    Attribute attribute = av.getAttribute();

                    if ( attribute != null )
                    {
                        if ( !StringUtils.isEmpty( attribute.getUid() ) ) uidMap.get( Attribute.class ).add( attribute.getUid() );
                        if ( !StringUtils.isEmpty( attribute.getCode() ) ) codeMap.get( Attribute.class ).add( attribute.getCode() );
                    }
                } );

                if ( !uidMap.containsKey( UserGroup.class ) ) uidMap.put( UserGroup.class, new HashSet<>() );
                if ( !codeMap.containsKey( UserGroup.class ) ) codeMap.put( UserGroup.class, new HashSet<>() );

                object.getUserGroupAccesses().forEach( uga -> {
                    UserGroup userGroup = uga.getUserGroup();

                    if ( userGroup != null )
                    {
                        if ( !StringUtils.isEmpty( userGroup.getUid() ) ) uidMap.get( UserGroup.class ).add( userGroup.getUid() );
                        if ( !StringUtils.isEmpty( userGroup.getCode() ) ) codeMap.get( UserGroup.class ).add( userGroup.getCode() );
                    }
                } );

                if ( !StringUtils.isEmpty( object.getUid() ) ) uidMap.get( objectClass ).add( object.getUid() );
                if ( !StringUtils.isEmpty( object.getCode() ) ) codeMap.get( objectClass ).add( object.getCode() );

                if ( uidMap.get( Attribute.class ).isEmpty() ) uidMap.remove( Attribute.class );
                if ( codeMap.get( Attribute.class ).isEmpty() ) codeMap.remove( Attribute.class );

                if ( uidMap.get( UserGroup.class ).isEmpty() ) uidMap.remove( UserGroup.class );
                if ( codeMap.get( UserGroup.class ).isEmpty() ) codeMap.remove( UserGroup.class );
            }

            uniqueMap.put( objectClass, handleUniqueProperties( schema, identifiableObjects ) );
        }

        System.err.println( "uniqueMap: " + uniqueMap );

        return map;
    }

    private Map<String, Map<Object, String>> handleUniqueProperties( Schema schema, List<IdentifiableObject> objects )
    {
        List<Property> uniqueProperties = schema.getProperties().stream()
            .filter( p -> p.isPersisted() && p.isOwner() && p.isUnique() )
            .collect( Collectors.toList() );

        Map<String, Map<Object, String>> map = new HashMap<>();

        for ( IdentifiableObject object : objects )
        {
            uniqueProperties.forEach( property -> {
                if ( !map.containsKey( property.getName() ) ) map.put( property.getName(), new HashMap<>() );
                Object value = ReflectionUtils.invokeMethod( object, property.getGetterMethod() );

                if ( value != null )
                {
                    map.get( property.getName() ).put( value, object.getUid() );
                }
            } );
        }

        return map;
    }

    @Override
    public Map<Class<?>, Map<String, Map<String, Object>>> collectObjectReferences( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> objects )
    {
        Map<Class<?>, Map<String, Map<String, Object>>> refs = new HashMap<>();

        if ( objects.isEmpty() )
        {
            return refs;
        }

        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> scanObjects = new HashMap<>();
        scanObjects.putAll( objects ); // clone objects list, we don't want to modify it

        if ( scanObjects.containsKey( User.class ) )
        {
            List<IdentifiableObject> users = scanObjects.get( User.class );
            List<IdentifiableObject> userCredentials = new ArrayList<>();

            for ( IdentifiableObject identifiableObject : users )
            {
                User user = (User) identifiableObject;

                if ( user.getUserCredentials() != null )
                {
                    userCredentials.add( user.getUserCredentials() );
                }
            }

            scanObjects.put( UserCredentials.class, userCredentials );
        }

        for ( Class<? extends IdentifiableObject> objectClass : scanObjects.keySet() )
        {
            Schema schema = schemaService.getDynamicSchema( objectClass );
            List<Property> properties = schema.getProperties().stream()
                .filter( p -> p.isPersisted() && p.isOwner() && (PropertyType.REFERENCE == p.getPropertyType() || PropertyType.REFERENCE == p.getItemPropertyType()) )
                .collect( Collectors.toList() );

            List<IdentifiableObject> identifiableObjects = scanObjects.get( objectClass );
            Map<String, Map<String, Object>> objectReferenceMap = new HashMap<>();
            refs.put( objectClass, objectReferenceMap );

            for ( IdentifiableObject object : identifiableObjects )
            {
                objectReferenceMap.put( object.getUid(), new HashMap<>() );

                properties.forEach( p -> {
                    if ( !p.isCollection() )
                    {
                        IdentifiableObject reference = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );

                        if ( reference != null )
                        {
                            try
                            {
                                IdentifiableObject identifiableObject = (IdentifiableObject) p.getKlass().newInstance();
                                identifiableObject.mergeWith( reference, MergeMode.REPLACE );
                                objectReferenceMap.get( object.getUid() ).put( p.getName(), identifiableObject );
                            }
                            catch ( InstantiationException | IllegalAccessException ignored )
                            {
                            }
                        }
                    }
                    else
                    {
                        Collection<IdentifiableObject> refObjects = ReflectionUtils.newCollectionInstance( p.getKlass() );
                        Collection<IdentifiableObject> references = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );

                        if ( references != null )
                        {
                            for ( IdentifiableObject reference : references )
                            {
                                try
                                {
                                    IdentifiableObject identifiableObject = (IdentifiableObject) p.getItemKlass().newInstance();
                                    identifiableObject.mergeWith( reference, MergeMode.REPLACE );
                                    refObjects.add( identifiableObject );
                                }
                                catch ( InstantiationException | IllegalAccessException ignored )
                                {
                                }

                            }
                        }

                        objectReferenceMap.get( object.getUid() ).put( p.getCollectionName(), refObjects );
                    }
                } );
            }
        }

        return refs;
    }

    @Override
    public List<ObjectErrorReport> checkReferences( List<IdentifiableObject> objects, Preheat preheat, PreheatIdentifier identifier )
    {
        List<ObjectErrorReport> objectErrorReports = new ArrayList<>();

        if ( objects.isEmpty() )
        {
            return objectErrorReports;
        }

        for ( int i = 0; i < objects.size(); i++ )
        {
            IdentifiableObject object = objects.get( i );
            List<PreheatErrorReport> errorReports = checkReferences( object, preheat, identifier );

            if ( errorReports.isEmpty() ) continue;

            ObjectErrorReport objectErrorReport = new ObjectErrorReport( object.getClass(), i );
            objectErrorReport.addErrorReports( errorReports );
            objectErrorReports.add( objectErrorReport );

        }

        return objectErrorReports;
    }

    @Override
    public List<PreheatErrorReport> checkReferences( IdentifiableObject object, Preheat preheat, PreheatIdentifier identifier )
    {
        List<PreheatErrorReport> preheatErrorReports = new ArrayList<>();

        if ( object == null )
        {
            return preheatErrorReports;
        }

        Schema schema = schemaService.getDynamicSchema( object.getClass() );
        schema.getProperties().stream()
            .filter( p -> p.isPersisted() && p.isOwner() && (PropertyType.REFERENCE == p.getPropertyType() || PropertyType.REFERENCE == p.getItemPropertyType()) )
            .forEach( p -> {
                if ( skipCheckAndConnect( p.getKlass() ) || skipCheckAndConnect( p.getItemKlass() ) )
                {
                    return;
                }

                if ( !p.isCollection() )
                {
                    IdentifiableObject refObject = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );
                    IdentifiableObject ref = preheat.get( identifier, refObject );

                    if ( ref == null && refObject != null && !Preheat.isDefaultClass( refObject.getClass() ) )
                    {
                        preheatErrorReports.add( new PreheatErrorReport( identifier, object.getClass(), ErrorCode.E5002,
                            identifier.getIdentifiersWithName( refObject ), identifier.getIdentifiersWithName( object ), p.getName() ) );
                    }
                }
                else
                {
                    Collection<IdentifiableObject> objects = ReflectionUtils.newCollectionInstance( p.getKlass() );
                    Collection<IdentifiableObject> refObjects = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );

                    for ( IdentifiableObject refObject : refObjects )
                    {
                        if ( Preheat.isDefault( refObject ) ) continue;

                        IdentifiableObject ref = preheat.get( identifier, refObject );

                        if ( ref == null && refObject != null && !Preheat.isDefaultClass( refObject.getClass() ) )
                        {
                            preheatErrorReports.add( new PreheatErrorReport( identifier, object.getClass(), ErrorCode.E5002,
                                identifier.getIdentifiersWithName( refObject ), identifier.getIdentifiersWithName( object ), p.getCollectionName() ) );
                        }
                        else
                        {
                            objects.add( refObject );
                        }
                    }

                    ReflectionUtils.invokeMethod( object, p.getSetterMethod(), objects );
                }
            } );

        if ( schema.havePersistedProperty( "attributeValues" ) )
        {
            object.getAttributeValues().stream()
                .filter( attributeValue -> attributeValue.getAttribute() != null && preheat.get( identifier, attributeValue.getAttribute() ) == null )
                .forEach( attributeValue -> preheatErrorReports.add( new PreheatErrorReport( identifier, object.getClass(), ErrorCode.E5002,
                    identifier.getIdentifiersWithName( attributeValue.getAttribute() ), identifier.getIdentifiersWithName( object ), "attributeValues" ) ) );
        }

        if ( schema.havePersistedProperty( "userGroupAccesses" ) )
        {
            object.getUserGroupAccesses().stream()
                .filter( userGroupAccess -> userGroupAccess.getUserGroup() != null && preheat.get( identifier, userGroupAccess.getUserGroup() ) == null )
                .forEach( attributeValue -> preheatErrorReports.add( new PreheatErrorReport( identifier, object.getClass(), ErrorCode.E5002,
                    identifier.getIdentifiersWithName( attributeValue.getUserGroup() ), identifier.getIdentifiersWithName( object ), "userGroupAccesses" ) ) );
        }

        return preheatErrorReports;
    }

    @Override
    public List<ObjectErrorReport> checkUniqueness( List<IdentifiableObject> objects, Preheat preheat )
    {
        List<ObjectErrorReport> objectErrorReports = new ArrayList<>();

        if ( objects.isEmpty() )
        {
            return objectErrorReports;
        }

        for ( int i = 0; i < objects.size(); i++ )
        {
            IdentifiableObject object = objects.get( i );
            List<ErrorReport> errorReports = checkUniqueness( object, preheat );

            if ( errorReports.isEmpty() ) continue;

            ObjectErrorReport objectErrorReport = new ObjectErrorReport( object.getClass(), i );
            objectErrorReport.addErrorReports( errorReports );
            objectErrorReports.add( objectErrorReport );

        }

        return objectErrorReports;
    }

    @Override
    public List<ErrorReport> checkUniqueness( IdentifiableObject object, Preheat preheat )
    {
        List<ErrorReport> errorReports = new ArrayList<>();

        if ( object == null )
        {
            return errorReports;
        }

        return errorReports;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public void connectReferences( Object object, Preheat preheat, PreheatIdentifier identifier )
    {
        if ( object == null )
        {
            return;
        }

        Map<Class<? extends IdentifiableObject>, IdentifiableObject> defaults = preheat.getDefaults();

        Schema schema = schemaService.getDynamicSchema( object.getClass() );
        schema.getProperties().stream()
            .filter( p -> p.isPersisted() && p.isOwner() && (PropertyType.REFERENCE == p.getPropertyType() || PropertyType.REFERENCE == p.getItemPropertyType()) )
            .forEach( p -> {
                if ( skipCheckAndConnect( p.getKlass() ) || skipCheckAndConnect( p.getItemKlass() ) )
                {
                    return;
                }

                if ( !p.isCollection() )
                {
                    IdentifiableObject refObject = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );
                    IdentifiableObject ref = preheat.get( identifier, refObject );

                    if ( Preheat.isDefaultClass( refObject ) && (ref == null || "default".equals( refObject.getName() )) )
                    {
                        ref = defaults.get( refObject.getClass() );
                    }

                    if ( ref != null && ref.getId() == 0 )
                    {
                        ReflectionUtils.invokeMethod( object, p.getSetterMethod(), (Object) null );
                    }
                    else
                    {
                        ReflectionUtils.invokeMethod( object, p.getSetterMethod(), ref );
                    }
                }
                else
                {
                    Collection<IdentifiableObject> objects = ReflectionUtils.newCollectionInstance( p.getKlass() );
                    Collection<IdentifiableObject> refObjects = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );

                    for ( IdentifiableObject refObject : refObjects )
                    {
                        IdentifiableObject ref = preheat.get( identifier, refObject );

                        if ( Preheat.isDefaultClass( refObject ) && (ref == null || "default".equals( refObject.getName() )) )
                        {
                            ref = defaults.get( refObject.getClass() );
                        }

                        if ( ref != null && ref.getId() != 0 ) objects.add( ref );
                    }

                    ReflectionUtils.invokeMethod( object, p.getSetterMethod(), objects );
                }
            } );
    }

    @SuppressWarnings( "unchecked" )
    private Map<String, UserCredentials> getUsernames()
    {
        Map<String, UserCredentials> userCredentialsMap = new HashMap<>();
        Query query = Query.from( schemaService.getDynamicSchema( UserCredentials.class ) );
        List<UserCredentials> userCredentials = (List<UserCredentials>) queryService.query( query );

        for ( UserCredentials uc : userCredentials )
        {
            userCredentialsMap.put( uc.getUsername(), uc );
        }

        return userCredentialsMap;
    }

    private boolean skipCheckAndConnect( Class<?> klass )
    {
        return klass != null && (UserCredentials.class.isAssignableFrom( klass ) || DataElementOperand.class.isAssignableFrom( klass ));
    }
}
