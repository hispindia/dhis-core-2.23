package org.hisp.dhis.message;

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

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.hisp.dhis.user.UserSettingService.KEY_MESSAGE_EMAIL_NOTIFICATION;

import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.hisp.dhis.commons.util.DebugUtils;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.setting.SettingKey;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.system.velocity.VelocityManager;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserSettingService;
import org.springframework.scheduling.annotation.Async;

import com.google.common.base.Strings;

/**
 * @author Lars Helge Overland
 */
public class EmailMessageSender
    implements MessageSender
{
    private static final Log log = LogFactory.getLog( EmailMessageSender.class );

    private static final String FROM_ADDRESS = "noreply@dhis2.org";

    private static final String DEFAULT_APPLICATION_TITLE = "DHIS 2";

    private static final String DEFAULT_FROM_NAME = DEFAULT_APPLICATION_TITLE + " Message [No reply]";

    private static final String DEFAULT_SUBJECT_PREFIX = "[" + DEFAULT_APPLICATION_TITLE + "] ";

    private static final String LB = System.getProperty( "line.separator" );

    private static final String MESSAGE_EMAIL_TEMPLATE = "message_email";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SystemSettingManager systemSettingManager;
    
    public void setSystemSettingManager( SystemSettingManager systemSettingManager )
    {
        this.systemSettingManager = systemSettingManager;
    }
    
    private ConfigurationService configurationService;
    
    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }
    
    private UserSettingService userSettingService;

    public void setUserSettingService( UserSettingService userSettingService )
    {
        this.userSettingService = userSettingService;
    }

    // -------------------------------------------------------------------------
    // MessageSender implementation
    // -------------------------------------------------------------------------

    /**
     * Note this methods is invoked asynchronously.
     */
    @Async
    @Override
    public String sendMessage( String subject, String text, String footer, User sender, Set<User> users, boolean forceSend )
    {
        String hostName = systemSettingManager.getEmailHostName();
        int port = systemSettingManager.getEmailPort();
        String username = systemSettingManager.getEmailUsername();
        String password = configurationService.getConfiguration().getSmtpPassword();
        boolean tls = systemSettingManager.getEmailTls();
        String from = systemSettingManager.getEmailSender();

        if ( hostName == null )
        {
            return null;
        }

        String plainContent = renderPlainContent( text, sender );
        String htmlContent = renderHtmlContent( text, footer, sender );

        try
        {
            HtmlEmail email = getHtmlEmail( hostName, port, username, password, tls, from );
            email.setSubject( customizeTitle( DEFAULT_SUBJECT_PREFIX ) + subject );
            email.setTextMsg( plainContent );
            email.setHtmlMsg( htmlContent );

            boolean hasRecipients = false;

            for ( User user : users )
            {
                boolean doSend = forceSend || (Boolean) userSettingService.getUserSetting( KEY_MESSAGE_EMAIL_NOTIFICATION, false, user );

                if ( doSend && user.getEmail() != null && !user.getEmail().trim().isEmpty() )
                {
                    email.addBcc( user.getEmail() );

                    log.info( "Sending email to user: " + user.getUsername() + " with email address: " + user.getEmail() + " to host: " + hostName + ":" + port );

                    hasRecipients = true;
                }
            }

            if ( hasRecipients )
            {
                email.send();
                log.info( "Email sent using host: " + hostName + ":" + port + " with TLS: " + tls );
            }
        }
        catch ( EmailException ex )
        {
            log.warn( "Could not send email: " + ex.getMessage() + ", " + DebugUtils.getStackTrace( ex ) );
        }
        catch ( RuntimeException ex )
        {
            log.warn( "Error while sending email: " + ex.getMessage() + ", " + DebugUtils.getStackTrace( ex ) );
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private HtmlEmail getHtmlEmail( String hostName, int port, String username, String password, boolean tls, String sender )
        throws EmailException
    {
        HtmlEmail email = new HtmlEmail();
        email.setHostName( hostName );
        email.setFrom( defaultIfEmpty( sender, FROM_ADDRESS ), customizeTitle( DEFAULT_FROM_NAME ) );
        email.setSmtpPort( port );
        email.setStartTLSEnabled( tls );

        if ( username != null && password != null )
        {
            email.setAuthenticator( new DefaultAuthenticator( username, password ) );
        }

        return email;
    }

    private String renderPlainContent( String text, User sender )
    {
        return sender == null ? text : ( text + LB + LB +
            sender.getName() + LB +
            ( sender.getOrganisationUnitsName() != null ? ( sender.getOrganisationUnitsName() + LB ) : StringUtils.EMPTY ) +
            ( sender.getEmail() != null ? ( sender.getEmail() + LB ) : StringUtils.EMPTY ) +
            ( sender.getPhoneNumber() != null ? ( sender.getPhoneNumber() + LB ) : StringUtils.EMPTY ) );
    }

    private String renderHtmlContent( String text, String footer, User sender )
    {
        HashMap<String, Object> content = new HashMap<>();

        if ( !Strings.isNullOrEmpty( text ) )
        {
            content.put( "text", text.replaceAll( "\\r?\\n", "<br>" ) );
        }

        if ( !Strings.isNullOrEmpty( footer ) )
        {
            content.put( "footer", footer );
        }

        if ( sender != null )
        {
            content.put( "senderName", sender.getName() );

            if ( sender.getOrganisationUnitsName() != null )
            {
                content.put( "organisationUnitsName", sender.getOrganisationUnitsName() );
            }

            if ( sender.getEmail() != null )
            {
                content.put( "email", sender.getEmail() );
            }

            if ( sender.getPhoneNumber() != null )
            {
                content.put( "phoneNumber", sender.getPhoneNumber() );
            }
        }

        return new VelocityManager().render( content, MESSAGE_EMAIL_TEMPLATE );
    }

    private String customizeTitle( String title )
    {
        String appTitle = (String) systemSettingManager.getSystemSetting( SettingKey.APPLICATION_TITLE );

        if ( appTitle != null && !appTitle.isEmpty() )
        {
            title = title.replace( DEFAULT_APPLICATION_TITLE, appTitle );
        }

        return title;
    }
}
