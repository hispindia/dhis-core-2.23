package org.hisp.dhis.setting;

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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hisp.dhis.common.ListMap;
import org.hisp.dhis.configuration.Configuration;
import org.hisp.dhis.sms.config.SmsConfiguration;

/**
 * @author Lars Helge Overland
 */
public enum Setting
{
    APPLICATION_TITLE( "applicationTitle", "District Health Information Software 2", String.class ), 
    APPLICATION_INTRO( "keyApplicationIntro" ),
    APPLICATION_NOTIFICATION( "keyApplicationNotification" ),
    APPLICATION_FOOTER( "keyApplicationFooter" ),
    APPLICATION_RIGHT_FOOTER( "keyApplicationRightFooter" ),
    FLAG( "keyFlag", "dhis2", String.class ),
    FLAG_IMAGE( "keyFlagImage" ),
    START_MODULE( "startModule", "dhis-web-dashboard-integration", String.class ),
    FACTOR_OF_DEVIATION( "factorDeviation", 2d, Double.class ),
    EMAIL_HOST_NAME( "keyEmailHostName" ),
    EMAIL_PORT( "keyEmailPort", 587, Integer.class ),
    EMAIL_USERNAME( "keyEmailUsername" ),
    EMAIL_PASSWORD( "keyEmailPassword" ),
    EMAIL_TLS( "keyEmailTls", Boolean.class ),
    EMAIL_SENDER( "keyEmailSender" ),
    INSTANCE_BASE_URL( "keyInstanceBaseUrl", ListMap.class ),
    SMS_CONFIG( "keySmsConfig", SmsConfiguration.class ),
    CACHE_STRATEGY( "keyCacheStrategy", "CACHE_6AM_TOMORROW", String.class ),
    TIME_FOR_SENDING_MESSAGE( "timeSendingMessage", "08:00", String.class ),
    SEND_MESSAGE_SCHEDULED_TASKS( "sendMessageScheduled" ),
    SCHEDULE_MESSAGE_TASKS( "scheduleMessage" ),
    PHONE_NUMBER_AREA_CODE( "phoneNumberAreaCode" ),
    MULTI_ORGANISATION_UNIT_FORMS( "multiOrganisationUnitForms", Boolean.class ),
    SCHEDULE_AGGREGATE_QUERY_BUILDER_TASKS( "scheduleAggregateQueryBuilder", Map.class ),
    SCHEDULE_AGGREGATE_QUERY_BUILDER_TASK_STRATEGY( "scheduleAggregateQueryBuilderTackStrategy", "lastMonth", String.class ),
    CONFIGURATION( "keyConfig", Configuration.class ),
    ACCOUNT_RECOVERY( "keyAccountRecovery", Boolean.class ),
    GOOGLE_ANALYTICS_UA( "googleAnalyticsUA" ),
    CREDENTIALS_EXPIRES( "credentialsExpires", Integer.class ),
    SELF_REGISTRATION_NO_RECAPTCHA( "keySelfRegistrationNoRecaptcha", Boolean.class ),
    OPENID_PROVIDER( "keyOpenIdProvider" ),
    OPENID_PROVIDER_LABEL( "keyOpenIdProviderLabel" ),
    CAN_GRANT_OWN_USER_AUTHORITY_GROUPS( "keyCanGrantOwnUserAuthorityGroups", Boolean.class ),
    HIDE_UNAPPROVED_DATA_IN_ANALYTICS( "keyHideUnapprovedDataInAnalytics", Boolean.class ),
    ANALYTICS_MAX_LIMIT( "keyAnalyticsMaxLimit", 50000, Integer.class ),
    CUSTOM_LOGIN_PAGE_LOGO( "keyCustomLoginPageLogo", Boolean.class ),
    CUSTOM_TOP_MENU_LOGO( "keyCustomTopMenuLogo", Boolean.class ),
    ANALYTICS_MAINTENANCE_MODE( "keyAnalyticsMaintenanceMode", Boolean.FALSE, Boolean.class ),
    DATABASE_SERVER_CPUS( "keyDatabaseServerCpus", 0, Integer.class ),
    LAST_SUCCESSFUL_ANALYTICS_TABLES_RUNTIME( "keyLastSuccessfulAnalyticsTablesRuntime" ),
    LAST_MONITORING_RUN( "keyLastMonitoringRun", Date.class ),
    LAST_SUCCESSFUL_DATA_SYNC( "keyLastSuccessfulDataSynch", Date.class ),
    LAST_SUCCESSFUL_ANALYTICS_TABLES_UPDATE( "keyLastSuccessfulAnalyticsTablesUpdate", Date.class ),
    LAST_SUCCESSFUL_RESOURCE_TABLES_UPDATE( "keyLastSuccessfulResourceTablesUpdate", Date.class ),
    LAST_SUCCESSFUL_MONITORING( "keyLastSuccessfulMonitoring", Date.class ),
    HELP_PAGE_LINK( "helpPageLink", "../dhis-web-commons-about/help.action", String.class ),
    ACCEPTANCE_REQUIRED_FOR_APPROVAL( "keyAcceptanceRequiredForApproval", Boolean.class ),
    SYSTEM_NOTIFICATIONS_EMAIL( "keySystemNotificationsEmail" ),
    ANALYSIS_RELATIVE_PERIOD( "keyAnalysisRelativePeriod", "LAST_12_MONTHS", String.class ),
    CORS_WHITELIST( "keyCorsWhitelist", List.class ),
    REQUIRE_ADD_TO_VIEW( "keyRequireAddToView", Boolean.class ),
    ALLOW_OBJECT_ASSIGNMENT( "keyAllowObjectAssignment", Boolean.class ),
    USE_CUSTOM_LOGO_FRONT( "keyUseCustomLogoFront", Boolean.class ),
    USE_CUSTOM_LOGO_BANNER( "keyUseCustomLogoBanner", Boolean.class ),
    METADATA_REPO_URL( "keyMetaDataRepoUrl", "http://metadata.dhis2.org", String.class ),
    DATA_IMPORT_STRICT_PERIODS( "keyDataImportStrictPeriods", Boolean.class ),
    DATA_IMPORT_STRICT_CATEGORY_OPTION_COMBOS( "keyDataImportStrictCategoryOptionCombos", Boolean.class ),
    DATA_IMPORT_STRICT_ORGANISATION_UNITS( "keyDataImportStrictOrganisationUnits", Boolean.class ),
    DATA_IMPORT_STRICT_ATTRIBUTE_OPTION_COMBOS( "keyDataImportStrictAttributeOptionCombos", Boolean.class ),
    DATA_IMPORT_REQUIRE_CATEGORY_OPTION_COMBO( "keyDataImportRequireCategoryOptionCombo", Boolean.class ),
    DATA_IMPORT_REQUIRE_ATTRIBUTE_OPTION_COMBO( "keyDataImportRequireAttributeOptionCombo", Boolean.class );
    
    private final String name;
    
    private final Serializable defaultValue;
    
    private final Class<?> clazz;

    private Setting( String name )
    {
        this.name = name;
        this.defaultValue = null;
        this.clazz = String.class;
    }
    
    private Setting( String name, Class<?> clazz )
    {
        this.name = name;
        this.defaultValue = null;
        this.clazz = clazz;
    }
    
    private Setting( String name, Serializable defaultValue, Class<?> clazz )
    {
        this.name = name;
        this.defaultValue = defaultValue;
        this.clazz = clazz;
    }
    
    public static Optional<Setting> getByName( String name )
    {
        for ( Setting setting : Setting.values() )
        {
            if ( setting.getName().equals( name ) )
            {
                return Optional.of( setting );
            }
        }
        
        return Optional.empty();
    }

    public static Serializable getAsRealClass( String name, String value )
    {
        Optional<Setting> setting = getByName( name );
                
        if ( setting.isPresent() )
        {            
            Class<?> settingClazz = setting.get().getClazz();
            
            if ( Double.class.isAssignableFrom( settingClazz ) )
            {
                return Double.valueOf( value );
            }
            else if ( Integer.class.isAssignableFrom( settingClazz ) )
            {
                return Integer.valueOf( value );
            }
            else if ( Boolean.class.isAssignableFrom( settingClazz ) )
            {
                return Boolean.valueOf( value );
            }
            
            //TODO handle Dates
        }
        
        return value;
    }
    
    public String getName()
    {
        return name;
    }

    public Serializable getDefaultValue()
    {
        return defaultValue;
    }

    public Class<?> getClazz()
    {
        return clazz;
    }
}
