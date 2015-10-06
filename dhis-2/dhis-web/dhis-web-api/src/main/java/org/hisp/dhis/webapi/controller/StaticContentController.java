package org.hisp.dhis.webapi.controller;

/*
 * Copyright (c) 2004-2015, University of Oslo
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.hisp.dhis.dxf2.webmessage.WebMessage;
import org.hisp.dhis.dxf2.webmessage.WebMessageException;
import org.hisp.dhis.dxf2.webmessage.WebMessageStatus;
import org.hisp.dhis.external.location.LocationManager;
import org.hisp.dhis.external.location.LocationManagerException;
import org.hisp.dhis.setting.Setting;
import org.hisp.dhis.setting.StyleManager;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.webapi.utils.WebMessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.ImmutableMap;

/**
 * Serves and uploads custom images for the logo on the front page (logo_front)
 * and for the logo on the top banner (logo_banner).
 *
 * @author Stian Sandvold
 */
@Controller
@RequestMapping( "/staticContent" )
public class StaticContentController
{
    @Autowired
    private LocationManager locationManager;

    @Autowired
    private SystemSettingManager systemSettingManager;

    @Autowired
    private StyleManager styleManager;

    private static final String LOGO_BANNER = "logo_banner";
    private static final String LOGO_FRONT = "logo_front";

    private static final Map<String, String> KEY_WHITELIST_MAP = ImmutableMap.<String, String>builder().
        put( LOGO_BANNER, Setting.USE_CUSTOM_LOGO_BANNER.getDefaultValue().toString() ).
        put( LOGO_FRONT, Setting.USE_CUSTOM_LOGO_FRONT.getDefaultValue().toString() ).build();

    /**
     * Serves the PNG associated with the key. If custom logo is not used the
     * request will redirect to the default.
     *
     * @param key key associated with the file.
     * @throws WebMessageException
     */
    @RequestMapping( value = "/{key}", method = RequestMethod.GET )
    public void getStaticContent(
        @PathVariable( "key" ) String key, HttpServletResponse response )
        throws WebMessageException
    {
        if ( !KEY_WHITELIST_MAP.containsKey( key ) )
        {
            throw new WebMessageException( WebMessageUtils.notFound( "Key does not exist" ) );
        }

        Boolean useCustomFile = Boolean.parseBoolean( (String) systemSettingManager.getSystemSetting( KEY_WHITELIST_MAP.get( key ) ) );

        if ( !useCustomFile ) // Serve the default
        {
            try
            {
                response.sendRedirect( this.getDefaultLogoUrl( key ) );
            }
            catch ( IOException e )
            {
                throw new WebMessageException( WebMessageUtils.error( "Can't read the file." ) );
            }
        }
        else // Serve the custom
        {
            InputStream in = null;

            try
            {
                in = locationManager.getInputStream( key + ".png", "static" );
                response.setContentType( "image/png" );
                IOUtils.copy( in, response.getOutputStream() );
            }
            catch ( LocationManagerException e )
            {
                throw new WebMessageException(
                    WebMessageUtils.notFound( "The requested file could not be found." ));
            }
            catch ( IOException e )
            {
                throw new WebMessageException(
                    WebMessageUtils.error( "Error occurred trying to serve file.",
                        "An IOException was thrown, indicating a file I/O or networking error." ) );
            }
            finally
            {
                IOUtils.closeQuietly( in );
            }
        }
    }

    /**
     * Uploads PNG images based on a key. Only accepts PNG and white listed keys.
     *
     * @param key the key
     * @param file the image file
     * @throws WebMessageException
     * @throws IOException
     */
    @RequestMapping( value = "/{key}", method = RequestMethod.POST )
    public void updateStaticContent(
        @PathVariable( "key" ) String key,
        @RequestParam( value = "file", required = false ) MultipartFile file )
        throws WebMessageException, IOException
    {
        if ( file == null || file.isEmpty() )
        {
            throw new WebMessageException( WebMessageUtils.badRequest( "Missing parameter 'file'" ) );
        }

        // Only PNG is accepted at the current time. Ensure file is a PNG image.
        MimeType mimeType = MimeTypeUtils.parseMimeType( file.getContentType() );

        if( !mimeType.isCompatibleWith( MimeTypeUtils.IMAGE_PNG ))
        {
            throw new WebMessageException( new WebMessage(WebMessageStatus.WARNING, HttpStatus.UNSUPPORTED_MEDIA_TYPE ) );
        }

        // Only keys in the white list are accepted at the current time
        if ( !KEY_WHITELIST_MAP.containsKey( key ) )
        {
            throw new WebMessageException(
                WebMessageUtils.badRequest( "This key is not supported." ) );
        }

        File out;

        try
        {
            out = locationManager.getFileForWriting( key + ".png", "static" );
        }
        catch( LocationManagerException e)
        {
            throw new WebMessageException( WebMessageUtils.error(e.getMessage()) );
        }

        try
        {
            file.transferTo( out );
        }
        catch( IOException e)
        {
            throw new WebMessageException( WebMessageUtils.error( "Could not save file." ) );
        }
    }

    /**
     * Returns the relative url of the default logo for a given key.
     *
     * @param key the key associated with the logo or null if the key does not exist.
     * @return the relative url of the logo.
     */
    private String getDefaultLogoUrl( String key )
    {
        String relativeUrlToImage = null;

        if ( key.equals( LOGO_BANNER ) )
        {
            relativeUrlToImage = "/dhis-web-commons/css/" + styleManager.getCurrentStyleDirectory() + "/logo_banner.png";
        }

        if ( key.equals( LOGO_FRONT ) )
        {
            relativeUrlToImage = "/dhis-web-commons/flags/" + systemSettingManager.getFlagImage();
        }

        return relativeUrlToImage;
    }
}
