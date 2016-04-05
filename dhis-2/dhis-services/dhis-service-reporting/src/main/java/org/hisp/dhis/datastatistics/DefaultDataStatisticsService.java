package org.hisp.dhis.datastatistics;

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

import org.hisp.dhis.chart.Chart;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.dashboard.Dashboard;
import org.hisp.dhis.eventchart.EventChart;
import org.hisp.dhis.eventreport.EventReport;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.mapping.Map;
import org.hisp.dhis.reporttable.ReportTable;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Yrjan A. F. Fraschetti
 * @author Julie Hill Roa
 */
@Transactional
public class DefaultDataStatisticsService 
    implements DataStatisticsService
{
    @Autowired
    private DataStatisticsStore dataStatisticsStore;

    @Autowired
    private DataStatisticsEventStore dataStatisticsEventStore;

    @Autowired
    private UserService userService;

    @Autowired
    private IdentifiableObjectManager identifiableObjectManager;

    /**
     * Adds an DataStatistics event in the database
     *
     * @param event object to be saved
     * @return id of the object in the database
     */
    public int addEvent( DataStatisticsEvent event )
    {
        return dataStatisticsEventStore.save( event );
    }

    /**
     * Gets number of saved Reports from a start date too a end date
     *
     * @param startDate start date
     * @param endDate end date
     * @param eventInterval event interval.
     * @return list of reports
     */
    @Override
    public List<AggregatedStatistics> getReports( Date startDate, Date endDate, EventInterval eventInterval )
    {
        return dataStatisticsStore.getSnapshotsInInterval( eventInterval, startDate, endDate );
    }

    /**
     * Gets all important information and creates a DataStatistics object 
     * and persists it.
     */
    @Override
    public int saveSnapshot( )
    {
        Date now = new Date();
        Date startDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime( startDate );
        c.add( Calendar.DATE, -1 );
        startDate = c.getTime();

        int totalUsers = identifiableObjectManager.getCount( User.class );
        double savedMaps = identifiableObjectManager.getCountByCreated( Map.class, startDate );
        double savedCharts = identifiableObjectManager.getCountByCreated( Chart.class, startDate );
        double savedReportTables = identifiableObjectManager.getCountByCreated( ReportTable.class, startDate );
        double savedEventReports = identifiableObjectManager.getCountByCreated( EventReport.class, startDate );
        double savedEventCharts = identifiableObjectManager.getCountByCreated( EventChart.class, startDate );
        double savedDashboards = identifiableObjectManager.getCountByCreated( Dashboard.class, startDate );
        double savedIndicators = identifiableObjectManager.getCountByCreated( Indicator.class, startDate );
        int activeUsers = userService.getActiveUsersCount( 1 );

        double chartViews = 0;
        double mapViews = 0;
        double dashboardViews = 0;
        double reportTablesViews = 0;
        double eventReportViews = 0;
        double eventChartViews = 0;
        double indicatorsViews = 0;
        double totalNumberOfViews = 0;
        double averageNumberofViews = 0;

        List<int[]> list = dataStatisticsEventStore.getDataStatisticsEventCount( startDate, now );

        for ( int i = 0; i < list.size(); i++ )
        {
            int[] temp = (int[]) list.get( i );
            
            switch ( temp[0] )
            {
                case 0:
                    chartViews = temp[1];
                    totalNumberOfViews += chartViews;
                    break;
                case 1: 
                    mapViews = temp[1];
                    totalNumberOfViews += mapViews;
                    break;
                case 2: 
                    dashboardViews = temp[1];
                    totalNumberOfViews += dashboardViews;
                    break;
                case 3: 
                    reportTablesViews = temp[1];
                    totalNumberOfViews += reportTablesViews;
                    break;
                case 4: 
                    eventReportViews = temp[1];
                    totalNumberOfViews += eventReportViews;
                    break;
                case 5: 
                    eventChartViews = temp[1];
                    totalNumberOfViews += eventChartViews;
                    break;
                case 6: 
                    indicatorsViews = temp[1];
                    totalNumberOfViews += indicatorsViews;
                    break;
            }
        }
        
        if ( activeUsers != 0 )
        {
            averageNumberofViews = totalNumberOfViews/activeUsers;
        }
        else
        {
            averageNumberofViews = totalNumberOfViews;
        }

        DataStatistics dataStatistics = new DataStatistics( activeUsers, mapViews, chartViews,
            reportTablesViews, eventReportViews, eventChartViews, dashboardViews,
            indicatorsViews, totalNumberOfViews, averageNumberofViews, savedMaps,
            savedCharts, savedReportTables, savedEventReports,
            savedEventCharts, savedDashboards, savedIndicators,
            totalUsers );

        return dataStatisticsStore.save( dataStatistics );
    }
}
