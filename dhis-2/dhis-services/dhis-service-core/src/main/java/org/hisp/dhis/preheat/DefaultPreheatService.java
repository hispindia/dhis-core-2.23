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

import com.google.common.collect.Sets;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.schema.Property;
import org.hisp.dhis.schema.PropertyType;
import org.hisp.dhis.schema.Schema;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.system.util.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
    private IdentifiableObjectManager manager;

    @Override
    @SuppressWarnings( "unchecked" )
    public Preheat preheat( PreheatParams params )
    {
        Preheat preheat = new Preheat();

        if ( PreheatMode.ALL == params.getPreheatMode() )
        {
            if ( params.getClasses().isEmpty() )
            {
                schemaService.getMetadataSchemas().stream().filter( Schema::isIdentifiableObject )
                    .forEach( schema -> params.getClasses().add( (Class<? extends IdentifiableObject>) schema.getKlass() ) );
            }

            for ( Class<? extends IdentifiableObject> klass : params.getClasses() )
            {
                List<? extends IdentifiableObject> objects = manager.getAllNoAcl( klass ); // should we use getAll here? are we allowed to reference unshared objects?
                preheat.put( params.getPreheatIdentifier(), objects );
            }
        }
        else if ( PreheatMode.REFERENCE == params.getPreheatMode() )
        {
            for ( Class<? extends IdentifiableObject> klass : params.getReferences().keySet() )
            {
                Collection<String> identifiers = params.getReferences().get( klass );

                if ( PreheatIdentifier.UID == params.getPreheatIdentifier() )
                {
                    List<? extends IdentifiableObject> objects = manager.getByUid( klass, identifiers );
                    preheat.put( params.getPreheatIdentifier(), objects );
                }
                else if ( PreheatIdentifier.CODE == params.getPreheatIdentifier() )
                {
                    List<? extends IdentifiableObject> objects = manager.getByCode( klass, identifiers );
                    preheat.put( params.getPreheatIdentifier(), objects );
                }
            }
        }

        return preheat;
    }

    @Override
    public void validate( PreheatParams params ) throws PreheatException
    {
        if ( PreheatMode.ALL == params.getPreheatMode() )
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
    public Map<Class<? extends IdentifiableObject>, Set<String>> collectReferences( Object object, PreheatIdentifier identifier )
    {
        return collectReferences( Sets.newHashSet( object ), identifier );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Map<Class<? extends IdentifiableObject>, Set<String>> collectReferences( Set<Object> objects, PreheatIdentifier identifier )
    {
        Map<Class<? extends IdentifiableObject>, Set<String>> map = new HashMap<>();

        if ( objects.isEmpty() )
        {
            return map;
        }

        Schema schema = schemaService.getDynamicSchema( objects.iterator().next().getClass() );
        List<Property> properties = schema.getProperties().stream()
            .filter( p -> p.isPersisted() && p.isOwner() && (PropertyType.REFERENCE == p.getPropertyType() || PropertyType.REFERENCE == p.getItemPropertyType()) )
            .collect( Collectors.toList() );

        properties.forEach( p -> {
            for ( Object object : objects )
            {
                if ( !p.isCollection() )
                {
                    Class<? extends IdentifiableObject> klass = (Class<? extends IdentifiableObject>) p.getKlass();
                    if ( !map.containsKey( klass ) ) map.put( klass, new HashSet<>() );
                    Object reference = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );

                    if ( reference != null )
                    {
                        IdentifiableObject identifiableObject = (IdentifiableObject) reference;

                        if ( PreheatIdentifier.UID == identifier )
                        {
                            if ( identifiableObject.getUid() != null )
                            {
                                map.get( klass ).add( identifiableObject.getUid() );
                            }
                        }
                        else if ( PreheatIdentifier.CODE == identifier )
                        {
                            if ( identifiableObject.getCode() != null )
                            {
                                map.get( klass ).add( identifiableObject.getCode() );
                            }
                        }
                    }
                }
                else
                {
                    Class<? extends IdentifiableObject> klass = (Class<? extends IdentifiableObject>) p.getItemKlass();
                    if ( !map.containsKey( klass ) ) map.put( klass, new HashSet<>() );
                    Collection<IdentifiableObject> reference = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );

                    for ( IdentifiableObject identifiableObject : reference )
                    {
                        if ( PreheatIdentifier.UID == identifier )
                        {
                            if ( identifiableObject.getUid() != null )
                            {
                                map.get( klass ).add( identifiableObject.getUid() );
                            }
                        }
                        else if ( PreheatIdentifier.CODE == identifier )
                        {
                            if ( identifiableObject.getCode() != null )
                            {
                                map.get( klass ).add( identifiableObject.getCode() );
                            }
                        }
                    }
                }
            }
        } );

        return map;
    }
}
