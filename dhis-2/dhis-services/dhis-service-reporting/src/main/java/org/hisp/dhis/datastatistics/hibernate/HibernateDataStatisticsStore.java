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

import org.hisp.dhis.datastatistics.AggregatedStatistics;
import org.hisp.dhis.datastatistics.DataStatistics;
import org.hisp.dhis.datastatistics.DataStatisticsStore;
import org.hisp.dhis.datastatistics.EventInterval;
import org.hisp.dhis.hibernate.HibernateGenericStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * @author Yrjan A. F. Fraschetti
 * @author Julie Hill Roa
 */
public class HibernateDataStatisticsStore 
    extends HibernateGenericStore<DataStatistics> 
    implements DataStatisticsStore
{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Retrieves data from database and maps aggregated data to 
     * AggregatedStatistic object.
     *
     * @param sql for data to be retrieved
     * @param eventInterval interval: DAY,MONTH,WEEK,YEAR
     * @return List of aggregated data
     */
    @Override
    public List<AggregatedStatistics> getSnapshotsInInterval( String sql, EventInterval eventInterval )
    {
        return jdbcTemplate.query( sql, ( resultSet, i ) -> {

            AggregatedStatistics ads = new AggregatedStatistics();
            ads.setYear( resultSet.getInt( "yr" ) );
            
            switch ( eventInterval )
            {
                case DAY:
                    ads.setDay( resultSet.getInt( "day" ) );
                    ads.setMonth( resultSet.getInt( "mnt" ) );
                    break;
                case WEEK:
                    ads.setWeek( resultSet.getInt( "week" ) );
                    break;
                case MONTH:
                    ads.setMonth( resultSet.getInt( "mnt" ) );
                    break;
            }
            ads.setActiveUsers( resultSet.getInt( "activeUsers" ) );
            ads.setMapViews( resultSet.getInt( "mapViews" ) );
            ads.setChartViews( resultSet.getInt( "chartViews" ) );
            ads.setReportTablesViews( resultSet.getInt( "reportTablesViews" ) );
            ads.setEventReportViews( resultSet.getInt( "reportTablesViews" ) );
            ads.setEventChartViews( resultSet.getInt( "eventChartViews" ) );
            ads.setDashboardViews( resultSet.getInt( "dashboardViews" ) );
            ads.setIndicatorsViews( resultSet.getInt( "indicatorsViews" ) );
            ads.setTotalViews( resultSet.getInt( "totalViews" ) );
            ads.setAverageViews( resultSet.getInt( "averageViews" ) );
            ads.setSavedMaps( resultSet.getInt( "savedMaps" ) );
            ads.setSavedCharts( resultSet.getInt( "savedCharts" ) );
            ads.setSavedReportTables( resultSet.getInt( "savedReportTables" ) );
            ads.setSavedEventReports( resultSet.getInt( "savedEventReports" ) );
            ads.setSavedEventCharts( resultSet.getInt( "savedEventCharts" ) );
            ads.setSavedDashboards( resultSet.getInt( "savedDashboards" ) );
            ads.setSavedIndicators( resultSet.getInt( "savedIndicators" ) );
            ads.setusers( resultSet.getInt( "users" ) );
            
            return ads;
        } );
    }
}
