/*
 * Copyright (c) 2004-2007, University of Oslo
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

package org.hisp.dhis.vn.admin.action;

import java.util.ArrayList;
import java.util.Collection;

import org.hisp.dhis.user.UserStore;
import org.hisp.dhis.vn.report.ReportExcelInterface;
import org.hisp.dhis.vn.report.ReportExcelService;

import com.opensymphony.xwork2.Action;

public class SaveReportUserRolesAction
    implements Action
{
    private ReportExcelService reportService;

    public void setReportService( ReportExcelService reportService )
    {
        this.reportService = reportService;
    }

    private UserStore userStore;

    public void setUserStore( UserStore userStore )
    {
        this.userStore = userStore;
    }

    private Integer reportId;

    public void setReportId( Integer reportId )
    {
        this.reportId = reportId;
    }

    private Collection<Integer> userRoleId = new ArrayList<Integer>();

    public void setUserRoleId( Collection<Integer> userRoleId )
    {
        this.userRoleId = userRoleId;
    }
    
    public String execute()
    {
        ReportExcelInterface report = reportService.getReport( reportId );
        
        report.getUserRoles().clear();
        
        for ( Integer id : userRoleId )
        {
            report.getUserRoles().add( userStore.getUserAuthorityGroup( id ) );
        }
        
        reportService.updateReport( report );
        
        return SUCCESS;
    }
}
