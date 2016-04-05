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
import org.hisp.dhis.datastatistics.AggregatedStatistics;
import org.hisp.dhis.datastatistics.DataStatistics;
import org.hisp.dhis.datastatistics.DataStatisticsStore;

import org.hisp.dhis.datastatistics.EventInterval;
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
public class HibernateDataStatisticsStoreTest 
    extends DhisSpringTest
{
    @Autowired
    private DataStatisticsStore dataStatisticsStore;

    private DataStatistics ds1;
    private DataStatistics ds2;
    private DataStatistics ds3;
    private DataStatistics ds4;
    private DataStatistics ds5;
    private DataStatistics ds6;

    private int ds1Id;
    private int ds2Id;

    private String testBefore;
    private String testAfter;
    private SimpleDateFormat fm;

    private Date date;

    @Override
    public void setUpTest() throws Exception
    {
        ds1 = new DataStatistics();
        ds2 = new DataStatistics( 10, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18 );
        ds3 = new DataStatistics();
        ds4 = new DataStatistics( 10, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19 );
        ds5 = new DataStatistics( 3, 2.0, 1.0, 6.0, 5.0, 4.0, 8.0, 7.0, 6.0, 3.0, 4.0, 4.0, 5.0, 9.0, 7.0, 6.0, 4.0, 2 );
        ds6 = new DataStatistics( 5, 6.0, 4.0, 3.0, 5.0, 7.0, 8.0, 5.0, 3.9, 2.8, 1.6, 5.5, 6.4, 8.3, 8.2, 9.4, 9.6, 9 );

        ds1Id = 0;
        ds2Id = 0;

        fm = new SimpleDateFormat( "yyyy/MM/dd" );
        date = fm.parse( "2016/03/21" );

        ds1.setCreated( date );
        ds2.setCreated( date );
        ds3.setCreated( date );
        ds4.setCreated( date );
        ds5.setCreated( date );
        ds6.setCreated( date );
    }

    @Test
    public void saveSnapshotTest() throws Exception
    {
        ds1Id = dataStatisticsStore.save( ds1 );
        ds2Id = dataStatisticsStore.save( ds2 );

        assertTrue( ds1Id != 0 );
        assertTrue( ds2Id != 0 );
    }

    @Test
    public void getSnapshotsInIntervalGetInDAYTest()
    {
        testBefore="2015-03-21";
        testAfter="2016-03-21";
        dataStatisticsStore.save( ds2 );
        dataStatisticsStore.save( ds4 );
        dataStatisticsStore.save( ds5 );
        dataStatisticsStore.save( ds6 );
        
        double expected = ds2.getSavedMaps() + ds4.getSavedMaps() + ds5.getSavedMaps() + ds6.getSavedMaps();

        String sql = "select extract(year from created) as yr, extract(month from created) as mnt, extract(day from created) as day, " +
            "max(active_users) as activeUsers," +
            "sum(mapviews) as mapViews," +
            "sum(chartviews) as chartViews," +
            "sum(reporttableviews) as reportTablesViews," +
            "sum(eventreportviews) as eventReportViews," +
            "sum(eventchartviews) as eventChartViews," +
            "sum(dashboardviews) as dashboardViews," +
            "sum(indicatorviews) as indicatorsViews," +
            "sum(totalviews) as totalViews," +
            "sum(average_views) as averageViews," +
            "sum(maps) as savedMaps," +
            "sum(charts) as savedCharts," +
            "sum(reporttables) as savedReportTables," +
            "sum(eventreports) as savedEventReports," +
            "sum(eventcharts) as savedEventCharts," +
            "sum(dashborards) as savedDashboards," +
            "sum(indicators) as savedIndicators," +
            "max(users) as users from datastatistics " +
            "where (created between '" + testBefore + "' and '" + testAfter + "') group by yr, mnt, day;";
        
        List<AggregatedStatistics> asList = dataStatisticsStore.getSnapshotsInInterval( sql, EventInterval.DAY );
        assertTrue( asList.size() == 1 );
    }

    @Test
    public void getSnapshotsInIntervalGetInDAY_DifferenDayesSavedTest() throws Exception
    {
        fm = new SimpleDateFormat( "yyyy/MM/dd" );
        date = fm.parse( "2016/03/20" );
        ds2.setCreated( date );

        testBefore="2015-03-19";
        testAfter="2016-03-21";

        dataStatisticsStore.save( ds2 );
        dataStatisticsStore.save( ds4 );
        dataStatisticsStore.save( ds5 );
        dataStatisticsStore.save( ds6 );

        String sql = "select extract(year from created) as yr, extract(month from created) as mnt, extract(day from created) as day, " +
            "max(active_users) as activeUsers," +
            "cast(round(cast(sum(mapviews) as numeric),0) as int) as mapViews," +
            "cast(round(cast(sum(chartviews) as numeric),0) as int) as chartViews," +
            "cast(round(cast(sum(reporttableviews) as numeric),0) as int) as reportTablesViews, " +
            "cast(round(cast(sum(eventreportviews) as numeric),0) as int) as eventReportViews, " +
            "cast(round(cast(sum(eventchartviews) as numeric),0) as int) as eventChartViews," +
            "cast(round(cast(sum(dashboardviews) as numeric),0) as int) as dashboardViews, " +
            "cast(round(cast(sum(indicatorviews) as numeric),0) as int) as indicatorsViews, " +
            "cast(round(cast(sum(totalviews) as numeric),0) as int) as totalViews," +
            "cast(round(cast(sum(average_views) as numeric),0) as int) as averageViews, " +
            "cast(round(cast(sum(maps) as numeric),0) as int) as savedMaps," +
            "cast(round(cast(sum(charts) as numeric),0) as int) as savedCharts," +
            "cast(round(cast(sum(reporttables) as numeric),0) as int) as savedReportTables," +
            "cast(round(cast(sum(eventreports) as numeric),0) as int) as savedEventReports," +
            "cast(round(cast(sum(eventcharts) as numeric),0) as int) as savedEventCharts," +
            "cast(round(cast(sum(dashborards) as numeric),0) as int) as savedDashboards, " +
            "cast(round(cast(sum(indicators) as numeric),0) as int) as savedIndicators," +
            "max(users) as users from datastatistics " +
            "where (created between '" + testBefore + "' and '" + testAfter + "') group by yr, mnt,day;";
        List<AggregatedStatistics> asList = dataStatisticsStore.getSnapshotsInInterval( sql, EventInterval.DAY );
        assertTrue( asList.size() == 2 );

    }

    @Test
    public void getSnapshotsInIntervalGetInDAY_GEDatesTest()
    {
        testBefore="2017-03-21";
        testAfter="2017-03-22";


        dataStatisticsStore.save( ds2 );
        dataStatisticsStore.save( ds4 );
        dataStatisticsStore.save( ds5 );
        dataStatisticsStore.save( ds6 );
        double expected = ds2.getSavedMaps() + ds4.getSavedMaps() + ds5.getSavedMaps() + ds6.getSavedMaps();

        String sql = "select extract(year from created) as yr, extract(month from created) as mnt, extract(day from created) as day, " +
            "max(active_users) as activeUsers," +
            "cast(round(cast(sum(mapviews) as numeric),0) as int) as mapViews," +
            "cast(round(cast(sum(chartviews) as numeric),0) as int) as chartViews," +
            "cast(round(cast(sum(reporttableviews) as numeric),0) as int) as reportTablesViews, " +
            "cast(round(cast(sum(eventreportviews) as numeric),0) as int) as eventReportViews, " +
            "cast(round(cast(sum(eventchartviews) as numeric),0) as int) as eventChartViews," +
            "cast(round(cast(sum(dashboardviews) as numeric),0) as int) as dashboardViews, " +
            "cast(round(cast(sum(indicatorviews) as numeric),0) as int) as indicatorsViews, " +
            "cast(round(cast(sum(totalviews) as numeric),0) as int) as totalViews," +
            "cast(round(cast(sum(average_views) as numeric),0) as int) as averageViews, " +
            "cast(round(cast(sum(maps) as numeric),0) as int) as savedMaps," +
            "cast(round(cast(sum(charts) as numeric),0) as int) as savedCharts," +
            "cast(round(cast(sum(reporttables) as numeric),0) as int) as savedReportTables," +
            "cast(round(cast(sum(eventreports) as numeric),0) as int) as savedEventReports," +
            "cast(round(cast(sum(eventcharts) as numeric),0) as int) as savedEventCharts," +
            "cast(round(cast(sum(dashborards) as numeric),0) as int) as savedDashboards, " +
            "cast(round(cast(sum(indicators) as numeric),0) as int) as savedIndicators," +
            "max(users) as users from datastatistics " +
            "where (created between '" + testBefore + "' and '" + testAfter + "') group by yr, mnt, day;";
        List<AggregatedStatistics> asList = dataStatisticsStore.getSnapshotsInInterval( sql, EventInterval.DAY );
        assertTrue( asList.size() == 0);

    }

    @Test
    public void getSnapshotsInIntervalGetInWEEKTest()
    {
        testBefore="2015-03-21";
        testAfter="2016-03-21";

        dataStatisticsStore.save( ds2 );
        dataStatisticsStore.save( ds4 );
        dataStatisticsStore.save( ds5 );
        dataStatisticsStore.save( ds6 );
        double expected = ds2.getSavedMaps() + ds4.getSavedMaps() + ds5.getSavedMaps() + ds6.getSavedMaps();

        String sql = "select extract(year from created) as yr, extract(week from created) as week, " +
            "max(active_users) as activeUsers," +
            "cast(round(cast(sum(mapviews) as numeric),0) as int) as mapViews," +
            "cast(round(cast(sum(chartviews) as numeric),0) as int) as chartViews," +
            "cast(round(cast(sum(reporttableviews) as numeric),0) as int) as reportTablesViews, " +
            "cast(round(cast(sum(eventreportviews) as numeric),0) as int) as eventReportViews, " +
            "cast(round(cast(sum(eventchartviews) as numeric),0) as int) as eventChartViews," +
            "cast(round(cast(sum(dashboardviews) as numeric),0) as int) as dashboardViews, " +
            "cast(round(cast(sum(indicatorviews) as numeric),0) as int) as indicatorsViews, " +
            "cast(round(cast(sum(totalviews) as numeric),0) as int) as totalViews," +
            "cast(round(cast(sum(average_views) as numeric),0) as int) as averageViews, " +
            "cast(round(cast(sum(maps) as numeric),0) as int) as savedMaps," +
            "cast(round(cast(sum(charts) as numeric),0) as int) as savedCharts," +
            "cast(round(cast(sum(reporttables) as numeric),0) as int) as savedReportTables," +
            "cast(round(cast(sum(eventreports) as numeric),0) as int) as savedEventReports," +
            "cast(round(cast(sum(eventcharts) as numeric),0) as int) as savedEventCharts," +
            "cast(round(cast(sum(dashborards) as numeric),0) as int) as savedDashboards, " +
            "cast(round(cast(sum(indicators) as numeric),0) as int) as savedIndicators," +
            "max(users) as users from datastatistics " +
            "where (created between '" + testBefore + "' and '" + testAfter + "') group by yr, week;";
        List<AggregatedStatistics> asList = dataStatisticsStore.getSnapshotsInInterval( sql, EventInterval.WEEK );
        assertTrue( asList.size() == 1 );
    }

    @Test
    public void getSnapshotsInIntervalGetInMONTHTest()
    {
        testBefore="2015-03-21";
        testAfter="2016-03-21";
        dataStatisticsStore.save( ds2 );
        dataStatisticsStore.save( ds4 );
        dataStatisticsStore.save( ds5 );
        dataStatisticsStore.save( ds6 );
        double expected = ds2.getSavedMaps() + ds4.getSavedMaps() + ds5.getSavedMaps() + ds6.getSavedMaps();

        String sql = "select extract(year from created) as yr, extract(month from created) as mnt, " +
            "max(active_users) as activeUsers," +
            "cast(round(cast(sum(mapviews) as numeric),0) as int) as mapViews," +
            "cast(round(cast(sum(chartviews) as numeric),0) as int) as chartViews," +
            "cast(round(cast(sum(reporttableviews) as numeric),0) as int) as reportTablesViews, " +
            "cast(round(cast(sum(eventreportviews) as numeric),0) as int) as eventReportViews, " +
            "cast(round(cast(sum(eventchartviews) as numeric),0) as int) as eventChartViews," +
            "cast(round(cast(sum(dashboardviews) as numeric),0) as int) as dashboardViews, " +
            "cast(round(cast(sum(indicatorviews) as numeric),0) as int) as indicatorsViews, " +
            "cast(round(cast(sum(totalviews) as numeric),0) as int) as totalViews," +
            "cast(round(cast(sum(average_views) as numeric),0) as int) as averageViews, " +
            "cast(round(cast(sum(maps) as numeric),0) as int) as savedMaps," +
            "cast(round(cast(sum(charts) as numeric),0) as int) as savedCharts," +
            "cast(round(cast(sum(reporttables) as numeric),0) as int) as savedReportTables," +
            "cast(round(cast(sum(eventreports) as numeric),0) as int) as savedEventReports," +
            "cast(round(cast(sum(eventcharts) as numeric),0) as int) as savedEventCharts," +
            "cast(round(cast(sum(dashborards) as numeric),0) as int) as savedDashboards, " +
            "cast(round(cast(sum(indicators) as numeric),0) as int) as savedIndicators," +
            "max(users) as users from datastatistics " +
            "where (created between '" + testBefore + "' and '" + testAfter + "') group by yr, mnt;";
        List<AggregatedStatistics> asList = dataStatisticsStore.getSnapshotsInInterval( sql, EventInterval.MONTH );
        assertTrue( asList.size() == 1 );
    }

    @Test
    public void getSnapshotsInIntervalGetInYEARTest()
    {
        testBefore="2015-03-21";
        testAfter="2016-03-21";
        dataStatisticsStore.save( ds2 );
        dataStatisticsStore.save( ds4 );
        dataStatisticsStore.save( ds5 );
        dataStatisticsStore.save( ds6 );
        double expected = ds2.getSavedMaps() + ds4.getSavedMaps() + ds5.getSavedMaps() + ds6.getSavedMaps();

        String sql = "select extract(year from created) as yr, " +
            "max(active_users) as activeUsers," +
            "cast(round(cast(sum(mapviews) as numeric),0) as int) as mapViews," +
            "cast(round(cast(sum(chartviews) as numeric),0) as int) as chartViews," +
            "cast(round(cast(sum(reporttableviews) as numeric),0) as int) as reportTablesViews, " +
            "cast(round(cast(sum(eventreportviews) as numeric),0) as int) as eventReportViews, " +
            "cast(round(cast(sum(eventchartviews) as numeric),0) as int) as eventChartViews," +
            "cast(round(cast(sum(dashboardviews) as numeric),0) as int) as dashboardViews, " +
            "cast(round(cast(sum(indicatorviews) as numeric),0) as int) as indicatorsViews, " +
            "cast(round(cast(sum(totalviews) as numeric),0) as int) as totalViews," +
            "cast(round(cast(sum(average_views) as numeric),0) as int) as averageViews, " +
            "cast(round(cast(sum(maps) as numeric),0) as int) as savedMaps," +
            "cast(round(cast(sum(charts) as numeric),0) as int) as savedCharts," +
            "cast(round(cast(sum(reporttables) as numeric),0) as int) as savedReportTables," +
            "cast(round(cast(sum(eventreports) as numeric),0) as int) as savedEventReports," +
            "cast(round(cast(sum(eventcharts) as numeric),0) as int) as savedEventCharts," +
            "cast(round(cast(sum(dashborards) as numeric),0) as int) as savedDashboards, " +
            "cast(round(cast(sum(indicators) as numeric),0) as int) as savedIndicators," +
            "max(users) as users from datastatistics " +
            "where (created between '" + testBefore + "' and '" + testAfter + "') group by yr;";
        List<AggregatedStatistics> asList = dataStatisticsStore.getSnapshotsInInterval( sql, EventInterval.YEAR );
        assertTrue( asList.size() == 1 );
    }


}