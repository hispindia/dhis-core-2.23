package org.hisp.dhis.period;

/*
 * Copyright (c) 2004-2010, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * PeriodType for quarterly Periods. A valid quarterly Period has startDate set
 * to the first day of a calendar quarter, and endDate set to the last day of
 * the same quarter.
 * 
 * @author Torgeir Lorange Ostby
 * @version $Id: QuarterlyPeriodType.java 2971 2007-03-03 18:54:56Z torgeilo $
 */
public class QuarterlyPeriodType
    extends CalendarPeriodType
{
    /**
     * The name of the QuarterlyPeriodType, which is "Quarterly".
     */
    public static final String NAME = "Quarterly";

    public static final int FREQUENCY_ORDER = 92;

    // -------------------------------------------------------------------------
    // PeriodType functionality
    // -------------------------------------------------------------------------

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public Period createPeriod()
    {
        return createPeriod( createCalendarInstance() );
    }

    @Override
    public Period createPeriod( Date date )
    {
        return createPeriod( createCalendarInstance( date ) );
    }

    private Period createPeriod( Calendar cal )
    {
        cal.set( Calendar.MONTH, cal.get( Calendar.MONTH ) - cal.get( Calendar.MONTH ) % 3 );
        cal.set( Calendar.DAY_OF_MONTH, 1 );

        Date startDate = cal.getTime();

        cal.add( Calendar.MONTH, 2 );
        cal.set( Calendar.DAY_OF_MONTH, cal.getActualMaximum( Calendar.DAY_OF_MONTH ) );

        return new Period( this, startDate, cal.getTime() );
    }

    @Override
    public int getFrequencyOrder()
    {
        return FREQUENCY_ORDER;
    }

    // -------------------------------------------------------------------------
    // CalendarPeriodType functionality
    // -------------------------------------------------------------------------

    @Override
    public Period getNextPeriod( Period period )
    {
        Calendar cal = createCalendarInstance( period.getStartDate() );
        cal.add( Calendar.MONTH, 3 );
        return createPeriod( cal );
    }

    @Override
    public Period getPreviousPeriod( Period period )
    {
        Calendar cal = createCalendarInstance( period.getStartDate() );
        cal.add( Calendar.MONTH, -3 );
        return createPeriod( cal );
    }

    /**
     * Generates quarterly Periods for the whole year in which the given
     * Period's startDate exists.
     */
    @Override
    public List<Period> generatePeriods( Date date )
    {
        Calendar cal = createCalendarInstance( date );
        cal.set( Calendar.DAY_OF_YEAR, 1 );

        int year = cal.get( Calendar.YEAR );
        ArrayList<Period> periods = new ArrayList<Period>();

        while ( cal.get( Calendar.YEAR ) == year )
        {
            periods.add( createPeriod( cal ) );
            cal.add( Calendar.MONTH, 3 );
        }

        return periods;
    }

    @Override
    public String getIsoDate( Period period )
    {
        Calendar cal = createCalendarInstance( period.getStartDate() );
        int year = cal.get( Calendar.YEAR );
        int month = cal.get( Calendar.MONTH );

        return year + Quarter.getByMonth( month ).name();
    }

    @Override
    public Period createPeriod( String isoDate )
    {
        int year = Integer.parseInt( isoDate.substring( 0, 4 ) );
        int month = Quarter.valueOf( isoDate.substring( 4, 6 ) ).getMonth();
        
        Calendar cal = createCalendarInstance();
        cal.set( year, month, 1 );
        return createPeriod( cal );
    }

    @Override
    public String getIsoFormat()
    {
        return "yyyyQn (n: 1-4)";
    }

    public enum Quarter
    {
        Q1( Calendar.JANUARY ), Q2( Calendar.APRIL ), Q3( Calendar.JULY ), Q4( Calendar.OCTOBER );

        private final int month;

        Quarter( int month )
        {
            this.month = month;
        }

        public int getMonth()
        {
            return month;
        }

        public static Quarter getByMonth( int month )
        {
            switch ( month )
            {
            case Calendar.JANUARY:
                return Q1;
            case Calendar.APRIL:
                return Q2;
            case Calendar.JULY:
                return Q3;
            case Calendar.OCTOBER:
                return Q4;
            default:
                throw new IllegalArgumentException( "Not a valid quarterly starting month" );
            }
        }
    }
}
