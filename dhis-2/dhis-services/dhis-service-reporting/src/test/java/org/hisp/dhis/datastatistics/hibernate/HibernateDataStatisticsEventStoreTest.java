package org.hisp.dhis.datastatistics.hibernate;

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

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.datastatistics.DataStatisticsEvent;
import org.hisp.dhis.datastatistics.DataStatisticsEventStore;
import org.hisp.dhis.datastatistics.EventType;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


import static org.junit.Assert.*;

/**
 * @author Yrjan A. F. Fraschetti
 * @author Julie Hill Roa
 */
public class HibernateDataStatisticsEventStoreTest 
    extends DhisSpringTest
{
    @Autowired
    private DataStatisticsEventStore dataStatisticsEventStore;

    private DataStatisticsEvent dse1;
    private DataStatisticsEvent dse2;
    private DataStatisticsEvent dse3;
    private DataStatisticsEvent dse4;

    private int dse1Id;
    private int dse2Id;

    private Date endDate;
    private Date testDate;

    private String start;
    private String end;

    @Override
    public void setUpTest() 
        throws Exception
    {
        endDate = new Date();

        end = "2016-03-21";
        start = "2016-03-19";

        SimpleDateFormat fm = new SimpleDateFormat( "yyyy/MM/dd" );
        endDate = fm.parse( "2016/03/20" );

        testDate = fm.parse( "2016/03/16" );

        dse1 = new DataStatisticsEvent( EventType.REPORT_TABLE_VIEW, endDate, "Testuser" );
        dse2 = new DataStatisticsEvent( EventType.EVENT_CHART_VIEW, endDate, "TestUser" );
        dse3 = new DataStatisticsEvent( EventType.INDICATOR_VIEW, testDate, "Testuser" );
        dse4 = new DataStatisticsEvent( EventType.DASHBOARD_VIEW, endDate, "TestUser" );

        dse1Id = 0;
        dse2Id = 0;
    }

    @Test
    public void addDataStatisticsEventTest()
    {
        dse1Id = dataStatisticsEventStore.save( dse1 );
        dse2Id = dataStatisticsEventStore.save( dse2 );
        assertTrue( dse1Id != 0 );
        assertTrue( dse2Id != 0 );
    }

    @Test
    public void getDataStatisticsEventCountTest()
    {
        String sql = "select eventtype as eventtype, count(eventtype) as numberofviews from datastatisticsevent where (timestamp between '" + 
            start + "' and '" + end + "') group by eventtype;";

        dataStatisticsEventStore.save( dse1 );
        dataStatisticsEventStore.save( dse4 );

        List<int[]> dsList = dataStatisticsEventStore.getDataStatisticsEventCount( sql );

        assertTrue( dsList.size() == 2 );
    }

    @Test
    public void getDataStatisticsEventCountCorrectContentTest()
    {
        String sql = "select eventtype as eventtype, count(eventtype) as numberofviews from datastatisticsevent where (timestamp between '" +
            start + "' and '" + end + "') group by eventtype;";

        dataStatisticsEventStore.save( dse1 );
        dataStatisticsEventStore.save( dse4 );

        List<int[]> dsList = dataStatisticsEventStore.getDataStatisticsEventCount( sql );
        assertEquals( 2, dsList.get( 0 )[0] );
        assertEquals( 3, dsList.get( 1 )[0] );
    }

    @Test
    public void getDataStatisticsEventCountCorrectDatesTest()
    {
        String sql = "select eventtype as eventtype, count(eventtype) as numberofviews from datastatisticsevent where (timestamp between '" +
            start + "' and '" + end + "') group by eventtype;";

        dataStatisticsEventStore.save( dse1 );
        dataStatisticsEventStore.save( dse4 );
        dataStatisticsEventStore.save( dse2 );

        List<int[]> dsList = dataStatisticsEventStore.getDataStatisticsEventCount( sql );
        assertTrue( dsList.size() == 3 );
    }

    @Test
    public void getDataStatisticsEventCountWrongDatesTest()
    {
        String sql = "select eventtype as eventtype, count(eventtype) as numberofviews from datastatisticsevent where (timestamp between '" + 
            start + "' and '" + end + "') group by eventtype;";

        dataStatisticsEventStore.save( dse1 );
        dataStatisticsEventStore.save( dse4 );
        dataStatisticsEventStore.save( dse3 );

        List<int[]> dsList = dataStatisticsEventStore.getDataStatisticsEventCount( sql );
        assertTrue( dsList.size() == 2 );
    }
}
