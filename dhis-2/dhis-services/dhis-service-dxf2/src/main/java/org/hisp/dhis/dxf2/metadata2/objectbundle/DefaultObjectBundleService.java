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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.dbms.DbmsManager;
import org.hisp.dhis.dxf2.metadata2.FlushMode;
import org.hisp.dhis.dxf2.metadata2.objectbundle.hooks.ObjectBundleHook;
import org.hisp.dhis.feedback.ErrorCode;
import org.hisp.dhis.feedback.ErrorReport;
import org.hisp.dhis.feedback.ObjectErrorReport;
import org.hisp.dhis.preheat.Preheat;
import org.hisp.dhis.preheat.PreheatIdentifier;
import org.hisp.dhis.preheat.PreheatMode;
import org.hisp.dhis.preheat.PreheatParams;
import org.hisp.dhis.preheat.PreheatService;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.schema.validation.SchemaValidator;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Service
@Transactional
public class DefaultObjectBundleService implements ObjectBundleService
{
    private static final Log log = LogFactory.getLog( DefaultObjectBundleService.class );

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private PreheatService preheatService;

    @Autowired
    private SchemaValidator schemaValidator;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IdentifiableObjectManager manager;

    @Autowired
    private DbmsManager dbmsManager;

    @Autowired( required = false )
    private List<ObjectBundleHook> objectBundleHooks = new ArrayList<>();

    @Override
    public ObjectBundle create( ObjectBundleParams params )
    {
        PreheatParams preheatParams = params.getPreheatParams();

        if ( params.getUser() == null )
        {
            params.setUser( currentUserService.getCurrentUser() );
        }

        preheatParams.setUser( params.getUser() );
        preheatParams.setObjects( params.getObjects() );

        if ( PreheatMode.REFERENCE == preheatParams.getPreheatMode() )
        {
            preheatParams.setReferences( preheatService.collectReferences( params.getObjects() ) );
        }

        for ( Class<? extends IdentifiableObject> klass : params.getObjects().keySet() )
        {
            params.getObjects().get( klass ).stream()
                .filter( identifiableObject -> StringUtils.isEmpty( identifiableObject.getUid() ) )
                .forEach( identifiableObject -> ((BaseIdentifiableObject) identifiableObject).setUid( CodeGenerator.generateCode() ) );
        }

        ObjectBundle bundle = new ObjectBundle( params, preheatService.preheat( preheatParams ), params.getObjects() );
        bundle.setObjectReferences( preheatService.collectObjectReferences( params.getObjects() ) );

        // add preheat placeholders for objects that will be created
        for ( Class<? extends IdentifiableObject> klass : bundle.getObjectMap().keySet() )
        {
            Map<PreheatIdentifier, Map<Class<? extends IdentifiableObject>, Map<String, IdentifiableObject>>> map = bundle.getPreheat().getMap();

            if ( !map.containsKey( PreheatIdentifier.UID ) ) map.put( PreheatIdentifier.UID, new HashMap<>() );
            if ( !map.get( PreheatIdentifier.UID ).containsKey( klass ) ) map.get( PreheatIdentifier.UID ).put( klass, new HashMap<>() );
            if ( !map.containsKey( PreheatIdentifier.CODE ) ) map.put( PreheatIdentifier.CODE, new HashMap<>() );
            if ( !map.get( PreheatIdentifier.CODE ).containsKey( klass ) ) map.get( PreheatIdentifier.CODE ).put( klass, new HashMap<>() );

            for ( IdentifiableObject identifiableObject : bundle.getObjects( klass, false ) )
            {
                if ( Preheat.isDefault( identifiableObject ) ) continue;

                if ( !StringUtils.isEmpty( identifiableObject.getUid() ) )
                {
                    map.get( PreheatIdentifier.UID ).get( klass ).put( identifiableObject.getUid(), identifiableObject );
                }

                if ( !StringUtils.isEmpty( identifiableObject.getCode() ) )
                {
                    map.get( PreheatIdentifier.CODE ).get( klass ).put( identifiableObject.getCode(), identifiableObject );
                }
            }
        }

        return bundle;
    }

    @Override
    public ObjectBundleValidation validate( ObjectBundle bundle )
    {
        ObjectBundleValidation objectBundleValidation = new ObjectBundleValidation();

        List<Class<? extends IdentifiableObject>> klasses = getSortedClasses( bundle );

        for ( Class<? extends IdentifiableObject> klass : klasses )
        {
            if ( bundle.getImportMode().isCreateAndUpdate() )
            {
                objectBundleValidation.addObjectErrorReports( validateBySchemas( klass, bundle.getObjectMap().get( klass ), bundle ) );
                objectBundleValidation.addObjectErrorReports( preheatService.checkReferences( bundle.getObjectMap().get( klass ),
                    bundle.getPreheat(), bundle.getPreheatIdentifier() ) );
                objectBundleValidation.addObjectErrorReports( preheatService.checkUniqueness( bundle.getObjectMap().get( klass ), bundle.getPreheat(),
                    bundle.getPreheatIdentifier() ) );
            }

            if ( bundle.getImportMode().isCreate() )
            {
                objectBundleValidation.addObjectErrorReports( validateForCreate( klass, bundle.getObjects( klass, true ), bundle ) );
                objectBundleValidation.addObjectErrorReports( validateBySchemas( klass, bundle.getObjects( klass, false ), bundle ) );
                objectBundleValidation.addObjectErrorReports( preheatService.checkReferences( bundle.getObjectMap().get( klass ),
                    bundle.getPreheat(), bundle.getPreheatIdentifier() ) );
                objectBundleValidation.addObjectErrorReports( preheatService.checkUniqueness( bundle.getObjectMap().get( klass ), bundle.getPreheat(),
                    bundle.getPreheatIdentifier() ) );
            }

            if ( bundle.getImportMode().isUpdate() )
            {
                objectBundleValidation.addObjectErrorReports( validateForUpdate( klass, bundle.getObjects( klass, false ), bundle ) );
                objectBundleValidation.addObjectErrorReports( validateBySchemas( klass, bundle.getObjects( klass, true ), bundle ) );
                objectBundleValidation.addObjectErrorReports( preheatService.checkReferences( bundle.getObjectMap().get( klass ),
                    bundle.getPreheat(), bundle.getPreheatIdentifier() ) );
                objectBundleValidation.addObjectErrorReports( preheatService.checkUniqueness( bundle.getObjectMap().get( klass ), bundle.getPreheat(),
                    bundle.getPreheatIdentifier() ) );
            }

            if ( bundle.getImportMode().isDelete() )
            {
                objectBundleValidation.addObjectErrorReports( validateForDelete( klass, bundle.getObjects( klass, false ), bundle ) );
            }
        }

        bundle.setObjectBundleStatus( ObjectBundleStatus.VALIDATED );

        return objectBundleValidation;
    }

    public List<ObjectErrorReport> validateForCreate( Class<? extends IdentifiableObject> klass, List<IdentifiableObject> objects, ObjectBundle bundle )
    {
        List<ObjectErrorReport> objectErrorReports = new ArrayList<>();

        if ( objects == null || objects.isEmpty() )
        {
            return objectErrorReports;
        }

        Iterator<IdentifiableObject> iterator = objects.iterator();
        int idx = 0;

        while ( iterator.hasNext() )
        {
            IdentifiableObject identifiableObject = iterator.next();
            IdentifiableObject object = bundle.getPreheat().get( bundle.getPreheatIdentifier(), identifiableObject );

            if ( object != null && object.getId() > 0 )
            {
                ObjectErrorReport objectErrorReport = new ObjectErrorReport( klass, idx );
                objectErrorReport.addErrorReport( new ErrorReport( klass, ErrorCode.E5000, bundle.getPreheatIdentifier(),
                    bundle.getPreheatIdentifier().getIdentifiersWithName( identifiableObject ) ) );
                objectErrorReports.add( objectErrorReport );
                iterator.remove();
            }

            idx++;
        }

        return objectErrorReports;
    }

    public List<ObjectErrorReport> validateForUpdate( Class<? extends IdentifiableObject> klass, List<IdentifiableObject> objects, ObjectBundle bundle )
    {
        List<ObjectErrorReport> objectErrorReports = new ArrayList<>();

        if ( objects == null || objects.isEmpty() )
        {
            return objectErrorReports;
        }

        Iterator<IdentifiableObject> iterator = objects.iterator();
        int idx = 0;

        while ( iterator.hasNext() )
        {
            IdentifiableObject identifiableObject = iterator.next();
            IdentifiableObject object = bundle.getPreheat().get( bundle.getPreheatIdentifier(), identifiableObject );

            if ( object == null || object.getId() == 0 )
            {
                if ( Preheat.isDefaultClass( identifiableObject.getClass() ) ) continue;

                ObjectErrorReport objectErrorReport = new ObjectErrorReport( klass, idx );
                objectErrorReport.addErrorReport( new ErrorReport( klass, ErrorCode.E5001, bundle.getPreheatIdentifier(),
                    bundle.getPreheatIdentifier().getIdentifiersWithName( identifiableObject ) ) );
                objectErrorReports.add( objectErrorReport );
                iterator.remove();
            }

            idx++;
        }

        return objectErrorReports;
    }

    public List<ObjectErrorReport> validateForDelete( Class<? extends IdentifiableObject> klass, List<IdentifiableObject> objects, ObjectBundle bundle )
    {
        List<ObjectErrorReport> objectErrorReports = new ArrayList<>();

        if ( objects == null || objects.isEmpty() )
        {
            return objectErrorReports;
        }

        Iterator<IdentifiableObject> iterator = objects.iterator();
        int idx = 0;

        while ( iterator.hasNext() )
        {
            IdentifiableObject identifiableObject = iterator.next();
            IdentifiableObject object = bundle.getPreheat().get( bundle.getPreheatIdentifier(), identifiableObject );

            if ( object == null || object.getId() == 0 )
            {
                if ( Preheat.isDefaultClass( identifiableObject.getClass() ) ) continue;

                ObjectErrorReport objectErrorReport = new ObjectErrorReport( klass, idx );
                objectErrorReport.addErrorReport( new ErrorReport( klass, ErrorCode.E5001, bundle.getPreheatIdentifier(),
                    bundle.getPreheatIdentifier().getIdentifiersWithName( identifiableObject ) ) );
                objectErrorReports.add( objectErrorReport );
                iterator.remove();
            }

            idx++;
        }

        return objectErrorReports;
    }

    public List<ObjectErrorReport> validateBySchemas( Class<? extends IdentifiableObject> klass, List<IdentifiableObject> objects, ObjectBundle bundle )
    {
        List<ObjectErrorReport> objectErrorReports = new ArrayList<>();

        if ( objects == null || objects.isEmpty() )
        {
            return objectErrorReports;
        }

        Iterator<IdentifiableObject> iterator = objects.iterator();
        int idx = 0;

        while ( iterator.hasNext() )
        {
            IdentifiableObject identifiableObject = iterator.next();
            List<ErrorReport> validationErrorReports = schemaValidator.validate( identifiableObject );

            if ( !validationErrorReports.isEmpty() )
            {
                ObjectErrorReport objectErrorReport = new ObjectErrorReport( klass, idx );
                objectErrorReport.addErrorReports( validationErrorReports );
                objectErrorReports.add( objectErrorReport );
                iterator.remove();
            }

            idx++;
        }

        return objectErrorReports;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public void commit( ObjectBundle bundle )
    {
        if ( ObjectBundleMode.VALIDATE == bundle.getObjectBundleMode() )
        {
            return; // skip if validate only
        }

        List<Class<? extends IdentifiableObject>> klasses = getSortedClasses( bundle );
        Session session = sessionFactory.getCurrentSession();

        objectBundleHooks.forEach( hook -> hook.preImport( bundle ) );

        for ( Class<? extends IdentifiableObject> klass : klasses )
        {
            List<IdentifiableObject> persistedObjects = bundle.getObjects( klass, true );
            List<IdentifiableObject> nonPersistedObjects = bundle.getObjects( klass, false );

            if ( bundle.getImportMode().isCreateAndUpdate() )
            {
                handleCreates( session, nonPersistedObjects, bundle );
                handleUpdates( session, persistedObjects, bundle );
            }
            else if ( bundle.getImportMode().isCreate() )
            {
                handleCreates( session, nonPersistedObjects, bundle );
            }
            else if ( bundle.getImportMode().isUpdate() )
            {
                handleUpdates( session, persistedObjects, bundle );
            }
            else if ( bundle.getImportMode().isDelete() )
            {
                handleDeletes( session, persistedObjects, bundle );
            }

            if ( FlushMode.AUTO == bundle.getFlushMode() ) session.flush();
        }

        objectBundleHooks.forEach( hook -> hook.postImport( bundle ) );
        session.flush();

        dbmsManager.clearSession();
        bundle.setObjectBundleStatus( ObjectBundleStatus.COMMITTED );
    }

    private void handleCreates( Session session, List<IdentifiableObject> objects, ObjectBundle bundle )
    {
        if ( objects.isEmpty() ) return;
        log.info( "Creating " + objects.size() + " object(s) of type " + objects.get( 0 ).getClass().getSimpleName() );

        for ( IdentifiableObject object : objects )
        {
            if ( Preheat.isDefault( object ) ) continue;

            objectBundleHooks.forEach( hook -> hook.preCreate( object, bundle ) );

            preheatService.connectReferences( object, bundle.getPreheat(), bundle.getPreheatIdentifier() );
            manager.save( object, bundle.getUser(), false );

            bundle.getPreheat().put( bundle.getPreheatIdentifier(), object );

            objectBundleHooks.forEach( hook -> hook.postCreate( object, bundle ) );

            if ( log.isDebugEnabled() )
            {
                String msg = "Created object '" + bundle.getPreheatIdentifier().getIdentifiersWithName( object ) + "'";
                log.debug( msg );
            }

            if ( FlushMode.OBJECT == bundle.getFlushMode() ) session.flush();
        }
    }

    private void handleUpdates( Session session, List<IdentifiableObject> objects, ObjectBundle bundle )
    {
        if ( objects.isEmpty() ) return;
        log.info( "Updating " + objects.size() + " object(s) of type " + objects.get( 0 ).getClass().getSimpleName() );

        for ( IdentifiableObject object : objects )
        {
            if ( Preheat.isDefault( object ) ) continue;

            objectBundleHooks.forEach( hook -> hook.preUpdate( object, bundle ) );

            preheatService.connectReferences( object, bundle.getPreheat(), bundle.getPreheatIdentifier() );

            IdentifiableObject persistedObject = bundle.getPreheat().get( bundle.getPreheatIdentifier(), object );

            persistedObject.mergeWith( object, bundle.getMergeMode() );
            persistedObject.mergeSharingWith( object );

            sessionFactory.getCurrentSession().update( persistedObject );

            objectBundleHooks.forEach( hook -> hook.postUpdate( persistedObject, bundle ) );

            bundle.getPreheat().put( bundle.getPreheatIdentifier(), persistedObject );

            if ( log.isDebugEnabled() )
            {
                String msg = "Updated object '" + bundle.getPreheatIdentifier().getIdentifiersWithName( persistedObject ) + "'";
                log.debug( msg );
            }

            if ( FlushMode.OBJECT == bundle.getFlushMode() ) session.flush();
        }
    }

    private void handleDeletes( Session session, List<IdentifiableObject> objects, ObjectBundle bundle )
    {
        if ( objects.isEmpty() ) return;
        log.info( "Deleting " + objects.size() + " object(s) of type " + objects.get( 0 ).getClass().getSimpleName() );

        List<IdentifiableObject> persistedObjects = bundle.getPreheat().getAll( bundle.getPreheatIdentifier(), objects );

        for ( IdentifiableObject object : persistedObjects )
        {
            objectBundleHooks.forEach( hook -> hook.preDelete( object, bundle ) );
            manager.delete( object, bundle.getUser() );

            bundle.getPreheat().remove( bundle.getPreheatIdentifier(), object );

            if ( log.isDebugEnabled() )
            {
                String msg = "Deleted object '" + bundle.getPreheatIdentifier().getIdentifiersWithName( object ) + "'";
                log.debug( msg );
            }

            if ( FlushMode.OBJECT == bundle.getFlushMode() ) session.flush();
        }
    }

    @SuppressWarnings( "unchecked" )
    private List<Class<? extends IdentifiableObject>> getSortedClasses( ObjectBundle bundle )
    {
        List<Class<? extends IdentifiableObject>> klasses = new ArrayList<>();

        schemaService.getMetadataSchemas().forEach( schema -> {
            Class<? extends IdentifiableObject> klass = (Class<? extends IdentifiableObject>) schema.getKlass();

            if ( bundle.getObjectMap().containsKey( klass ) )
            {
                klasses.add( klass );
            }
        } );

        return klasses;
    }
}
