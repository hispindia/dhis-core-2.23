package org.hisp.dhis.gis.action.export;

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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.hisp.dhis.gis.Legend;
import org.hisp.dhis.gis.action.configuration.GISConfigurationManagerService;
import org.hisp.dhis.gis.state.SelectionManager;
import org.hisp.dhis.gis.util.SVGUtils;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;

import com.opensymphony.xwork.Action;

public class ExportExcelAction
    implements Action
{
    private FileFeatureStore fileFeatureStore;

    private OrganisationUnitSelectionManager organisationUnitSelectionManager;

    private OrganisationUnitService organisationUnitService;

    private GISConfigurationManagerService gisConfigurationManagerService;

    private SelectionManager selectionManager;

    private I18n i18n;

    private String mapFileName;

    private String outputXLS;

    private InputStream inputStream;
   
    public void setGisConfigurationManagerService( GISConfigurationManagerService gisConfigurationManagerService )
    {
        this.gisConfigurationManagerService = gisConfigurationManagerService;
    }

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
    }

    public void setOrganisationUnitSelectionManager( OrganisationUnitSelectionManager organisationUnitSelectionManager )
    {
        this.organisationUnitSelectionManager = organisationUnitSelectionManager;
    }   

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public void setInputStream( InputStream inputStream )
    {
        this.inputStream = inputStream;
    }

    public String getOutputXLS()
    {
        return outputXLS;
    }

    public void setFileFeatureStore( FileFeatureStore fileFeatureStore )
    {
        this.fileFeatureStore = fileFeatureStore;
    }

    public void setMapFileName( String mapFileName )
    {
        this.mapFileName = mapFileName;
    }

    public void setSelectionManager( SelectionManager selectionManager )
    {
        this.selectionManager = selectionManager;
    }

    private File getLegends( File directory, List<Legend> legends )
    {

        if ( fileFeatureStore.createLegends( legends ) )
        {

            String svg = directory.getAbsolutePath() + File.separator + "legend.svg";
            
            String output = directory.getAbsolutePath() + File.separator + "legend.png";
            
            SVGUtils.convertSVG2PNG( svg, output, 100, legends.size() * 100 );

//            String jarpath = "\"" + directory.getParent() + File.separator + "batik-1.7" + File.separator
//                + "batik-rasterizer.jar\" ";
//
//            String jarrun = "java -jar " + jarpath + " -w 100  -h " + legends.size() * 100 + " -q 0.9999 -m image/png "
//                + "\"" + svg + "\"";
//
//            Process pro = Runtime.getRuntime().exec( jarrun );
//
//            pro.waitFor();
//
//            pro.destroy();

            File result = new File( directory, "legend.png" );

            result.deleteOnExit();

            return result;
        }

        return null;

    }

    @SuppressWarnings( "deprecation" )
    public String execute()
        throws Exception
    {

        OrganisationUnit organisationUnit = organisationUnitSelectionManager.getSelectedOrganisationUnit();

        int titlePositionCol = 1;

        int titlePositionRow = 1;

        int mapPositionCol = 1;

        int mapPositionCRow = 5;

        int legendPositionCol = 9;

        int legendPositionRow = 5;

        int orgunitPositionCol = 13;

        int orgunitPositionRow = 5;

        if ( fileFeatureStore.createSVGTempFileNoLegend( this.mapFileName ) )
        {

            File directory = gisConfigurationManagerService.getGISTempDirectory();

            String svg = directory.getAbsolutePath() + File.separator + "temp.svg";
            
            String outputPath = directory.getAbsolutePath() + File.separator + "temp.png";

            File output = new File( directory, "ouput.xls" );
            
            SVGUtils.convertSVG2PNG( svg, outputPath, 1000, 1000);

            //String jarpath = "\"" + directory.getParent() + File.separator + "batik-1.7" + File.separator
            //    + "batik-rasterizer.jar\" ";

            //String jarrun = "java -jar " + jarpath + " -w 1024  -h 1024 -q 0.9999 -m image/png " + "\"" + svg + "\"";

           // Process pro = Runtime.getRuntime().exec( jarrun );

            //pro.waitFor();

            //pro.destroy();

            // Workbook templateWorkbook = Workbook.c;

            Indicator indicator = selectionManager.getSeleteBagSession().getIndicator();

            WritableWorkbook outputReportWorkbook = Workbook.createWorkbook( output );

            WritableSheet sheet = outputReportWorkbook.createSheet( indicator.getName(), 1 );

            sheet.mergeCells( mapPositionCol, mapPositionCRow, mapPositionCol + 6, mapPositionCRow + 25 );

            /*------------------------------------------------
             * Add Map
             *------------------------------------------------*/

            File imageFile = new File( directory, "temp.png" );

            WritableImage image = new WritableImage( mapPositionCol, mapPositionCRow, 7, 26, imageFile );

            sheet.addImage( image );

            imageFile.deleteOnExit();

            WritableCellFormat map_format = new WritableCellFormat();
            map_format.setBorder( Border.ALL, BorderLineStyle.THIN );
            map_format.setAlignment( Alignment.CENTRE );

            sheet.addCell( new Label( mapPositionCol, mapPositionCRow, "", map_format ) );

            WritableCellFormat header = new WritableCellFormat();
            header.setBackground( Colour.ICE_BLUE );
            header.setBorder( Border.ALL, BorderLineStyle.THIN );

            WritableCellFormat legend_header = new WritableCellFormat();
            legend_header.setBorder( Border.ALL, BorderLineStyle.THIN );
            legend_header.setAlignment( Alignment.CENTRE );
            legend_header.setBackground( Colour.ICE_BLUE );

            /*------------------------------------------------
             * Add Title
             *------------------------------------------------*/

            sheet.mergeCells( titlePositionCol, titlePositionRow, titlePositionCol + 1, titlePositionRow );
            sheet.mergeCells( titlePositionCol + 2, titlePositionRow, titlePositionCol + 6, titlePositionRow );
            sheet.mergeCells( titlePositionCol, titlePositionRow + 1, titlePositionCol + 1, titlePositionRow + 1 );
            sheet.mergeCells( titlePositionCol + 2, titlePositionRow + 1, titlePositionCol + 6, titlePositionRow + 1 );
            sheet.mergeCells( titlePositionCol, titlePositionRow + 2, titlePositionCol + 1, titlePositionRow + 2 );
            sheet.mergeCells( titlePositionCol + 2, titlePositionRow + 2, titlePositionCol + 6, titlePositionRow + 2 );

            sheet.addCell( new Label( titlePositionCol, titlePositionRow, i18n.getString( "indicator" ), header ) );
            sheet.addCell( new Label( titlePositionCol + 2, titlePositionRow, indicator.getName(), header ) );
            sheet.addCell( new Label( titlePositionCol, titlePositionRow + 1, i18n.getString( "organisationunit" ),
                header ) );
            sheet.addCell( new Label( titlePositionCol + 2, titlePositionRow + 1, organisationUnit.getName(), header ) );
            sheet.addCell( new Label( titlePositionCol, titlePositionRow + 2, i18n.getString( "period" ), header ) );
            sheet.addCell( new Label( titlePositionCol + 2, titlePositionRow + 2, selectionManager
                .getSeleteBagSession().getStartDate()
                + " - " + selectionManager.getSeleteBagSession().getEndDate(), header ) );

            sheet.mergeCells( legendPositionCol, legendPositionRow, legendPositionCol + 2, legendPositionRow );
            sheet
                .addCell( new Label( legendPositionCol, legendPositionRow, i18n.getString( "legend" ), legend_header ) );
            sheet.addCell( new Label( legendPositionCol, legendPositionRow + 1, i18n.getString( "color" ),
                legend_header ) );
            sheet.addCell( new Label( legendPositionCol + 1, legendPositionRow + 1, i18n.getString( "min" ),
                legend_header ) );
            sheet.addCell( new Label( legendPositionCol + 2, legendPositionRow + 1, i18n.getString( "max" ),
                legend_header ) );

            /*------------------------------------------------
             * Add Legends
             *------------------------------------------------*/

            int col = legendPositionCol;

            int row = legendPositionRow + 2;

            List<Legend> legends = selectionManager.getSeleteBagSession().getLegends();

            image = new WritableImage( col, row, 1, legends.size(), this.getLegends( directory, legends ) );

            sheet.addImage( image );

            sheet.mergeCells( col, row, col, row + legends.size() - 1 );

            WritableCellFormat legend_format = new WritableCellFormat();

            legend_format.setBorder( Border.ALL, BorderLineStyle.THIN );

            sheet.addCell( new Label( col, row, "", legend_format ) );

            int legendCount = 0;

            for ( Legend legend : legends )
            {

                legend_format = new WritableCellFormat();

                legend_format.setAlignment( Alignment.CENTRE );

                if ( legendCount == legends.size() - 1 )
                {
                    legend_format.setBorder( Border.LEFT, BorderLineStyle.THIN );
                    legend_format.setBorder( Border.RIGHT, BorderLineStyle.THIN );
                    legend_format.setBorder( Border.BOTTOM, BorderLineStyle.THIN );
                }
                else
                {
                    legend_format.setBorder( Border.LEFT, BorderLineStyle.THIN );
                    legend_format.setBorder( Border.RIGHT, BorderLineStyle.THIN );
                }

                sheet.addCell( new Number( col + 1, row, legend.getMin(), legend_format ) );

                sheet.addCell( new Number( col + 2, row, legend.getMax(), legend_format ) );

                row++;
                legendCount++;

            }

            /*------------------------------------------------
             * Add Org Unit and Value
             *------------------------------------------------*/

            sheet
                .addCell( new Label( orgunitPositionCol, orgunitPositionRow, i18n.getString( "name" ), legend_header ) );

            sheet.addCell( new Label( orgunitPositionCol + 1, orgunitPositionRow, i18n.getString( "value" ),
                legend_header ) );

            List<FeatureStructure> features = selectionManager.getSeleteBagSession().getFeatureStructure();

            Collections.sort( features );

            int rowValue = orgunitPositionRow + 1;

            for ( FeatureStructure featureStructure : features )
            {

                WritableCellFormat valCellFormat = new WritableCellFormat();

                valCellFormat.setAlignment( Alignment.CENTRE );

                valCellFormat.setBorder( Border.ALL, BorderLineStyle.THIN );

                OrganisationUnit orUnit = organisationUnitService.getOrganisationUnit( featureStructure.getOrgunit() );

                sheet
                    .addCell( new Number( orgunitPositionCol + 1, rowValue, featureStructure.getValue(), valCellFormat ) );

                valCellFormat = new WritableCellFormat();

                valCellFormat.setAlignment( Alignment.LEFT );

                valCellFormat.setBorder( Border.ALL, BorderLineStyle.THIN );

                sheet.addCell( new Label( orgunitPositionCol, rowValue, orUnit.getName(), valCellFormat ) );

                rowValue++;

            }

            outputReportWorkbook.write();

            outputReportWorkbook.close();

            outputXLS = mapFileName.replace( ".svg", "" ) + "_" + new Date().getDate() + "-"
                + (new Date().getMonth() + 1) + "-" + (new Date().getYear() + 1900) + ".xls";

            File outputXLSStream = new File( output.getAbsolutePath() );

            inputStream = new BufferedInputStream( new FileInputStream( outputXLSStream ) );

            outputXLSStream.deleteOnExit();

            return SUCCESS;
        }
        return ERROR;
    }

}
