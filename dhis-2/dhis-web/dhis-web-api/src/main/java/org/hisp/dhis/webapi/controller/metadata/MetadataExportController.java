package org.hisp.dhis.webapi.controller.metadata;

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
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.dxf2.metadata2.MetadataExportParams;
import org.hisp.dhis.dxf2.metadata2.MetadataExportService;
import org.hisp.dhis.fieldfilter.DefaultFieldFilterService;
import org.hisp.dhis.node.NodeUtils;
import org.hisp.dhis.node.types.CollectionNode;
import org.hisp.dhis.node.types.RootNode;
import org.hisp.dhis.node.types.SimpleNode;
import org.hisp.dhis.schema.Schema;
import org.hisp.dhis.schema.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Controller
@RequestMapping( "/metadata/export" )
public class MetadataExportController
{
    @Autowired
    private MetadataExportService metadataExportService;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private DefaultFieldFilterService fieldFilterService;

    @RequestMapping( value = "", method = RequestMethod.GET )
    public @ResponseBody RootNode getMetadata( @RequestParam Map<String, String> rpParameters )
    {
        RootNode rootNode = NodeUtils.createMetadata();
        rootNode.addChild( new SimpleNode( "date", new Date(), true ) );

        MetadataExportParams params = metadataExportService.getParamsFromMap( rpParameters );

        Map<Class<? extends IdentifiableObject>, List<? extends IdentifiableObject>> metadata = metadataExportService.getMetadata( params );

        for ( Class<? extends IdentifiableObject> klass : metadata.keySet() )
        {
            Schema schema = schemaService.getDynamicSchema( klass );
            CollectionNode collectionNode = fieldFilterService.filter( klass, metadata.get( klass ), Lists.newArrayList( ":owner" ) );
            rootNode.addChild( collectionNode );
        }

        return rootNode;
    }
}
