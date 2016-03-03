package org.hisp.dhis.schema.validation;

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

import org.apache.commons.validator.GenericValidator;
import org.hisp.dhis.feedback.ErrorCode;
import org.hisp.dhis.feedback.ErrorReport;
import org.hisp.dhis.schema.Property;
import org.hisp.dhis.schema.PropertyType;
import org.hisp.dhis.schema.Schema;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.system.util.ReflectionUtils;
import org.hisp.dhis.system.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class DefaultSchemaValidator implements SchemaValidator
{
    @Autowired
    private SchemaService schemaService;

    @Override
    public List<ErrorReport> validate( Object object )
    {
        return validate( object, true );
    }

    @Override
    public List<ErrorReport> validate( Object object, boolean persisted )
    {
        if ( object == null || schemaService.getSchema( object.getClass() ) == null )
        {
            return new ArrayList<>();
        }

        Schema schema = schemaService.getSchema( object.getClass() );

        List<ErrorReport> errorReports = new ArrayList<>();

        for ( Property property : schema.getProperties() )
        {
            if ( persisted && !property.isPersisted() )
            {
                continue;
            }

            Object value = ReflectionUtils.invokeMethod( object, property.getGetterMethod() );

            if ( value == null )
            {
                if ( property.isRequired() )
                {
                    errorReports.add( new ErrorReport( object.getClass(), ErrorCode.E4000, property.getName() ).setErrorKlass( property.getKlass() ) );
                }

                continue;
            }

            errorReports.addAll( validateString( value, property ) );
            errorReports.addAll( validateCollection( value, property ) );
            errorReports.addAll( validateInteger( value, property ) );
            errorReports.addAll( validateFloat( value, property ) );
            errorReports.addAll( validateDouble( value, property ) );
        }

        return errorReports;
    }

    private List<? extends ErrorReport> validateString( Object object, Property property )
    {
        List<ErrorReport> errorReports = new ArrayList<>();

        // TODO How should empty strings be handled? they are not valid color, password, url, etc of course.
        if ( !String.class.isInstance( object ) || StringUtils.isEmpty( object ) )
        {
            return errorReports;
        }

        String value = (String) object;

        // check column max length
        if ( value.length() > property.getLength() )
        {
            errorReports.add( new ErrorReport( object.getClass(), ErrorCode.E4001, property.getName(), property.getLength(), value.length() )
                .setErrorKlass( property.getKlass() ) );
            return errorReports;
        }

        if ( value.length() < property.getMin() || value.length() > property.getMax() )
        {
            errorReports.add( new ErrorReport( object.getClass(), ErrorCode.E4002, property.getName(), property.getMin(), property.getMax(), value.length() )
                .setErrorKlass( property.getKlass() ) );
        }

        if ( PropertyType.EMAIL == property.getPropertyType() && !GenericValidator.isEmail( value ) )
        {
            errorReports.add( new ErrorReport( object.getClass(), ErrorCode.E4003, property.getName(), value )
                .setErrorKlass( property.getKlass() ) );
        }
        else if ( PropertyType.URL == property.getPropertyType() && !isUrl( value ) )
        {
            errorReports.add( new ErrorReport( object.getClass(), ErrorCode.E4004, property.getName(), value )
                .setErrorKlass( property.getKlass() ) );
        }
        else if ( PropertyType.PASSWORD == property.getPropertyType() && !ValidationUtils.passwordIsValid( value ) )
        {
            errorReports.add( new ErrorReport( object.getClass(), ErrorCode.E4005, property.getName(), value )
                .setErrorKlass( property.getKlass() ) );
        }
        else if ( PropertyType.COLOR == property.getPropertyType() && !ValidationUtils.isValidHexColor( value ) )
        {
            errorReports.add( new ErrorReport( object.getClass(), ErrorCode.E4006, property.getName(), value )
                .setErrorKlass( property.getKlass() ) );
        }

        /* TODO add proper validation for both Points and Polygons, ValidationUtils only supports points at this time
        if ( PropertyType.GEOLOCATION == property.getPropertyType() && !ValidationUtils.coordinateIsValid( value ) )
        {
            validationViolations.add( new ValidationViolation( "Value is not a valid coordinate pair [lon, lat]." ) );
        }
        */

        return errorReports;
    }

    // Commons validator have some issues in latest version, replacing with a very simple test for now
    private boolean isUrl( String url )
    {
        return !StringUtils.isEmpty( url ) && (url.startsWith( "http://" ) || url.startsWith( "https://" ));
    }

    private List<? extends ErrorReport> validateCollection( Object object, Property property )
    {
        List<ErrorReport> errorReports = new ArrayList<>();

        if ( !Collection.class.isInstance( object ) )
        {
            return errorReports;
        }

        Collection<?> value = (Collection<?>) object;

        if ( value.size() < property.getMin() || value.size() > property.getMax() )
        {
            errorReports.add( new ErrorReport( object.getClass(), ErrorCode.E4007, property.getName(), property.getMin(), property.getMax(), value.size() )
                .setErrorKlass( property.getKlass() ) );
        }

        return errorReports;
    }

    private List<? extends ErrorReport> validateInteger( Object object, Property property )
    {
        List<ErrorReport> errorReports = new ArrayList<>();

        if ( !Integer.class.isInstance( object ) )
        {
            return errorReports;
        }

        Integer value = (Integer) object;

        if ( !GenericValidator.isInRange( value, property.getMin(), property.getMax() ) )
        {
            errorReports.add( new ErrorReport( object.getClass(), ErrorCode.E4008, property.getName(), property.getMin(), property.getMax(), value )
                .setErrorKlass( property.getKlass() ) );
        }

        return errorReports;
    }

    private List<? extends ErrorReport> validateFloat( Object object, Property property )
    {
        List<ErrorReport> errorReports = new ArrayList<>();

        if ( !Float.class.isInstance( object ) )
        {
            return errorReports;
        }

        Float value = (Float) object;

        if ( !GenericValidator.isInRange( value, property.getMin(), property.getMax() ) )
        {
            errorReports.add( new ErrorReport( object.getClass(), ErrorCode.E4008, property.getName(), property.getMin(), property.getMax(), value )
                .setErrorKlass( property.getKlass() ) );
        }

        return errorReports;
    }

    private List<? extends ErrorReport> validateDouble( Object object, Property property )
    {
        List<ErrorReport> errorReports = new ArrayList<>();

        if ( !Double.class.isInstance( object ) )
        {
            return errorReports;
        }

        Double value = (Double) object;

        if ( !GenericValidator.isInRange( value, property.getMin(), property.getMax() ) )
        {
            errorReports.add( new ErrorReport( object.getClass(), ErrorCode.E4008, property.getName(), property.getMin(), property.getMax(), value )
                .setErrorKlass( property.getKlass() ) );
        }

        return errorReports;
    }
}
