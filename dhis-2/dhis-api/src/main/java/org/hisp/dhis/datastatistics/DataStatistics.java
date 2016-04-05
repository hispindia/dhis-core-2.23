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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hisp.dhis.common.BaseIdentifiableObject;


/**
 * @author Julie Hill Roa
 * @author Yrjan A. F. Fraschetti
 *
 *         DataStatistics object to be saved as snapshot
 */
public class DataStatistics extends BaseIdentifiableObject
{
    Integer activeUsers;
    Double mapViews;
    Double chartViews;
    Double reportTablesViews;
    Double eventReportViews;
    Double eventChartViews;
    Double dashboardViews;
    Double indicatorsViews;
    Double totalViews;
    Double averageViews;
    Double savedMaps;
    Double savedCharts;
    Double savedReportTables;
    Double savedEventReports;
    Double savedEventCharts;
    Double savedDashboards;
    Double savedIndicators;
    Integer users;

    public DataStatistics()
    {
        super.setAutoFields();
    }

    public DataStatistics( Integer activeUsers, Double mapViews, Double chartViews, Double reportTablesViews, Double eventReportViews,
        Double eventChartViews, Double dashboardViews, Double indicatorsViews, Double totalViews, Double averageViews, Double savedMaps,
        Double savedCharts, Double savedReportTables, Double savedEventReports, Double savedEventCharts, Double savedDashboards,
        Double savedIndicators, Integer users )
    {
        super.setAutoFields();
        this.activeUsers = activeUsers;
        this.mapViews = mapViews;
        this.chartViews = chartViews;
        this.reportTablesViews = reportTablesViews;
        this.eventReportViews = eventReportViews;
        this.eventChartViews = eventChartViews;
        this.dashboardViews = dashboardViews;
        this.indicatorsViews = indicatorsViews;
        this.totalViews = totalViews;
        this.averageViews = averageViews;
        this.savedMaps = savedMaps;
        this.savedCharts = savedCharts;
        this.savedReportTables = savedReportTables;
        this.savedEventReports = savedEventReports;
        this.savedEventCharts = savedEventCharts;
        this.savedDashboards = savedDashboards;
        this.savedIndicators = savedIndicators;
        this.users = users;
    }

    @JsonProperty
    public Integer getActiveUsers()
    {
        return activeUsers;
    }

    public void setActiveUsers( Integer activeUsers )
    {
        this.activeUsers = activeUsers;
    }

    @JsonProperty
    public Double getMapViews()
    {
        return mapViews;
    }

    public void setMapViews( Double mapViews )
    {
        this.mapViews = mapViews;
    }

    @JsonProperty
    public Double getChartViews()
    {
        return chartViews;
    }

    public void setChartViews( Double chartViews )
    {
        this.chartViews = chartViews;
    }

    @JsonProperty
    public Double getReportTablesViews()
    {
        return reportTablesViews;
    }

    public void setReportTablesViews( Double reportTablesViews )
    {
        this.reportTablesViews = reportTablesViews;
    }

    @JsonProperty
    public Double getEventReportViews()
    {
        return eventReportViews;
    }

    public void setEventReportViews( Double eventReportViews )
    {
        this.eventReportViews = eventReportViews;
    }

    @JsonProperty
    public Double getEventChartViews()
    {
        return eventChartViews;
    }

    public void setEventChartViews( Double eventChartViews )
    {
        this.eventChartViews = eventChartViews;
    }

    @JsonProperty
    public Double getDashboardViews()
    {
        return dashboardViews;
    }

    public void setDashboardViews( Double dashboardViews )
    {
        this.dashboardViews = dashboardViews;
    }

    @JsonProperty
    public Double getIndicatorsViews()
    {
        return indicatorsViews;
    }

    public void setIndicatorsViews( Double indicatorsViews )
    {
        this.indicatorsViews = indicatorsViews;
    }

    @JsonProperty
    public Double getTotalViews()
    {
        return totalViews;
    }

    public void setTotalViews( Double totalViews )
    {
        this.totalViews = totalViews;
    }

    @JsonProperty
    public Double getAverageViews()
    {
        return averageViews;
    }

    public void setAverageViews( Double averageViews )
    {
        this.averageViews = averageViews;
    }

    @JsonProperty
    public Double getSavedMaps()
    {
        return savedMaps;
    }

    public void setSavedMaps( Double savedMaps )
    {
        this.savedMaps = savedMaps;
    }

    @JsonProperty
    public Double getSavedCharts()
    {
        return savedCharts;
    }

    public void setSavedCharts( Double savedCharts )
    {
        this.savedCharts = savedCharts;
    }

    @JsonProperty
    public Double getSavedReportTables()
    {
        return savedReportTables;
    }

    public void setSavedReportTables( Double savedReportTables )
    {
        this.savedReportTables = savedReportTables;
    }

    @JsonProperty
    public Double getSavedEventReports()
    {
        return savedEventReports;
    }

    public void setSavedEventReports( Double savedEventReports )
    {
        this.savedEventReports = savedEventReports;
    }

    @JsonProperty
    public Double getSavedEventCharts()
    {
        return savedEventCharts;
    }

    public void setSavedEventCharts( Double savedEventCharts )
    {
        this.savedEventCharts = savedEventCharts;
    }

    @JsonProperty
    public Double getSavedDashboards()
    {
        return savedDashboards;
    }

    public void setSavedDashboards( Double savedDashboards )
    {
        this.savedDashboards = savedDashboards;
    }

    @JsonProperty
    public Double getSavedIndicators()
    {
        return savedIndicators;
    }

    public void setSavedIndicators( Double savedIndicators )
    {
        this.savedIndicators = savedIndicators;
    }

    @JsonProperty
    public Integer getUsers()
    {
        return users;
    }

    @JsonProperty
    public void setUsers( Integer users )
    {
        this.users = users;
    }

    @Override public String toString()
    {
        return super.toString() + "DataStatistics{" +
            "activeUsers=" + activeUsers +
            ", mapViews=" + mapViews +
            ", chartViews=" + chartViews +
            ", reportTablesViews=" + reportTablesViews +
            ", eventReportViews=" + eventReportViews +
            ", eventChartViews=" + eventChartViews +
            ", dashboardViews=" + dashboardViews +
            ", indicatorsViews=" + indicatorsViews +
            ", totalViews=" + totalViews +
            ", averageViews=" + averageViews +
            ", savedMaps=" + savedMaps +
            ", savedCharts=" + savedCharts +
            ", savedReportTables=" + savedReportTables +
            ", savedEventReports=" + savedEventReports +
            ", savedEventCharts=" + savedEventCharts +
            ", savedDashboards=" + savedDashboards +
            ", savedIndicators=" + savedIndicators +
            ", users=" + users +
            '}';
    }
}
