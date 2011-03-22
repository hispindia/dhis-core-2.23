package org.hisp.dhis.dataelement;

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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.hierarchy.HierarchyViolationException;
import org.hisp.dhis.period.PeriodType;

/**
 * Defines service functionality for DataElements and DataElementGroups.
 * 
 * @author Kristian Nordal
 * @version $Id: DataElementService.java 6289 2008-11-14 17:53:24Z larshelg $
 */
public interface DataElementService
{
    String ID = DataElementService.class.getName();

    // -------------------------------------------------------------------------
    // DataElement
    // -------------------------------------------------------------------------

    /**
     * Adds a DataElement.
     * 
     * @param dataElement the DataElement to add.
     * @return a generated unique id of the added DataElement.
     */
    int addDataElement( DataElement dataElement );

    /**
     * Updates a DataElement.
     * 
     * @param dataElement the DataElement to update.
     */
    void updateDataElement( DataElement dataElement );

    /**
     * Deletes a DataElement. The DataElement is also removed from any
     * DataElementGroups it is a member of. It is not possible to delete a
     * DataElement with children.
     * 
     * @param dataElement the DataElement to delete.
     * @throws HierarchyViolationException if the DataElement has children.
     */
    void deleteDataElement( DataElement dataElement )
        throws HierarchyViolationException;

    /**
     * Returns a DataElement.
     * 
     * @param id the id of the DataElement to return.
     * @return the DataElement with the given id, or null if no match.
     */
    DataElement getDataElement( int id );

    /**
     * Returns the DataElement with the given UUID.
     * 
     * @param uuid the UUID.
     * @return the DataElement with the given UUID, or null if no match.
     */
    DataElement getDataElement( String uuid );

    /**
     * Returns a DataElement with a given name.
     * 
     * @param name the name of the DataElement to return.
     * @return the DataElement with the given name, or null if no match.
     */
    DataElement getDataElementByName( String name );

    /**
     * Returns a DataElement with a given alternative name.
     * 
     * @param alternativeName the alternative name of the DataElement to return.
     * @return the DataElement with the given alternative name, or null if no
     *         match.
     */
    DataElement getDataElementByAlternativeName( String alternativeName );
    
    /**
     * Returns List of DataElements with a given key.
     * 
     * @param key the name of the DataElement to return.
     * @return List of DataElements with a given key, or all dataelements if no match.
     */
    Collection<DataElement> searchDataElementByName( String key );
    /**
     * Returns a DataElement with a given short name.
     * 
     * @param shortName the short name of the DataElement to return.
     * @return the DataElement with the given short name, or null if no match.
     */
    DataElement getDataElementByShortName( String shortName );

    /**
     * Returns all DataElements.
     * 
     * @return a collection of all DataElements, or an empty collection if there
     *         are no DataElements.
     */
    Collection<DataElement> getAllDataElements();

    /**
     * Returns all DataElements which are not instances of
     * CalculatedDataElements.
     * 
     * @return all DataElements which are not instances of
     *         CalculatedDataElements.
     */
    Collection<DataElement> getNonCalculatedDataElements();

    /**
     * Returns all DataElements with corresponding identifiers. Returns all
     * DataElements if the given argument is null.
     * 
     * @param identifiers the collection of identifiers.
     * @return a collection of DataElements.
     */
    Collection<DataElement> getDataElements( Collection<Integer> identifiers );

    /**
     * Returns all CalculatedDataElements with corresponding identifiers.
     * Returns all CalculatedDataElements if the given argument is null.
     * 
     * @param identifiers the collection of identifiers.
     * @return a collection of CalculatedDataElements.
     */
    Collection<CalculatedDataElement> getCalculatedDataElements( Collection<Integer> identifiers );

    /**
     * Returns all non-calculated DataElements with corresponding identifiers.
     * Returns all non-calculated DataElements if the given argument is null.
     * 
     * @param identifiers the collection of identifiers.
     * @return a collection of DataElements.
     */
    Collection<DataElement> getNonCalculatedDataElements( Collection<Integer> identifiers );

    /**
     * Returns all DataElements with types that are possible to aggregate. The
     * types are currently INT and BOOL.
     * 
     * @return all DataElements with types that are possible to aggregate.
     */
    Collection<DataElement> getAggregateableDataElements();

    /**
     * Returns all active DataElements.
     * 
     * @return a collection of all active DataElements, or an empty collection
     *         if there are no active DataElements.
     */
    Collection<DataElement> getAllActiveDataElements();

    /**
     * Returns all DataElements with a given aggregantion operator.
     * 
     * @param aggregationOperator the aggregation operator of the DataElements
     *        to return.
     * @return a collection of all DataElements with the given aggregation
     *         operator, or an empty collection if no DataElements have the
     *         aggregation operator.
     */
    Collection<DataElement> getDataElementsByAggregationOperator( String aggregationOperator );

    /**
     * Returns all DataElements with the given domain type.
     * 
     * @param domainType the domainType.
     * @return all DataElements with the given domainType.
     */
    Collection<DataElement> getDataElementsByDomainType( String domainType );

    /**
     * Returns all DataElements with the given type.
     * 
     * @param type the type.
     * @return all DataElements with the given type.
     */

    Collection<DataElement> getDataElementsByType( String type );

    /**
     * Returns the DataElements with the given PeriodType.
     * 
     * @param periodType the PeriodType.
     * @return a Collection of DataElements.
     */
    Collection<DataElement> getDataElementsByPeriodType( PeriodType periodType );
    
    /**
     * Returns all DataElements with the given category combo.
     * 
     * @param categoryCombo the DataElementCategoryCombo.
     * @return all DataElements with the given category combo.
     */
    Collection<DataElement> getDataElementByCategoryCombo( DataElementCategoryCombo categoryCombo );

    /**
     * Returns a Map with DataElementCategoryCombo as key and a Collection of
     * the DataElements belonging to the DataElementCategoryCombo from the given
     * argument List of DataElements as value.
     * 
     * @param dataElements the DataElements to include.
     * @return grouped DataElements based on their DataElementCategoryCombo.
     */
    Map<DataElementCategoryCombo, List<DataElement>> getGroupedDataElementsByCategoryCombo(
        List<DataElement> dataElements );

    /**
     * Returns the DataElementCategoryCombos associated with the given argument
     * list of DataElements.
     * 
     * @param dataElements the DataElements.
     * @return a list of DataElements.
     */
    List<DataElementCategoryCombo> getDataElementCategoryCombos( List<DataElement> dataElements );

    /**
     * Returns all DataElements which are associated with one or more
     * DataElementGroupSets.
     * 
     * @return all DataElements which are associated with one or more
     *         DataElementGroupSets.
     */
    Collection<DataElement> getDataElementsWithGroupSets();

    /**
     * Returns all DataElements which are not member of any DataElementGroups.
     * 
     * @return all DataElements which are not member of any DataElementGroups.
     */
    Collection<DataElement> getDataElementsWithoutGroups();
    
    /**
     * Returns all DataElements which are not assigned to any DataSets.
     * 
     * @return all DataElements which are not assigned to any DataSets.
     */
    Collection<DataElement> getDataElementsWithoutDataSets();
    
    /**
     * Checks whether a DataElement with the given identifier exists.
     * 
     * @param id the DataElement identifier.
     * @return true or false.
     */
    boolean dataElementExists( int id );

    /**
     * Checks whether a DataElementCategoryOptionCombo with the given identifier exists.
     * 
     * @param id the DataElementCategoryOptionCombo identifier.
     * @return true or false.
     */
    boolean dataElementCategoryOptionComboExists( int id );

    Collection<DataElement> getDataElementsByDataSets( Collection<DataSet> dataSets );
    
    Collection<DataElement> getDataElementsLikeName( String name );
    
    Collection<DataElement> getDataElementsBetween( int first, int max );
    
    Collection<DataElement> getDataElementsBetweenByName( String name, int first, int max );
    
    int getDataElementCount();
    
    int getDataElementCountByName( String name );
    
    // -------------------------------------------------------------------------
    // Calculated Data Elements
    // -------------------------------------------------------------------------

    /**
     * Returns a CalclulatedDataElement which contains a given dataElement
     * 
     * @param dataElement the DataElement which is contained by the
     *        CalculatedDataElement to return.
     * @return a CalculatedDataElement which contains the given DataElement, or
     *         null if the DataElement is not part of a CalculatedDataElement.
     */
    CalculatedDataElement getCalculatedDataElementByDataElement( DataElement dataElement );

    /**
     * Returns CalculatedDataElements which contain any of the given
     * DataElements
     * 
     * @param dataElements Collection of DataElements which can be contained by
     *        the returned CalculatedDataElements
     * @return a collection of CalculatedDataElements which contain any of the
     *         given DataElements, or an empty collection if no
     *         CalculatedDataElements contain any of the DataElements.
     */
    Collection<CalculatedDataElement> getCalculatedDataElementsByDataElements( Collection<DataElement> dataElements );

    /**
     * Returns all CalculatedDataElements
     * 
     * @return a collection of all CalculatedDataElements, or an empty
     *         collection if there are no CalculcatedDataELements
     */
    Collection<CalculatedDataElement> getAllCalculatedDataElements();

    /**
     * Returns a Map of factors for the DataElements in the given
     * CalculatedDataElement
     * 
     * @param calculatedDataElement CalculatedDataElement to get factors for
     * @return a map of factors for the DataElements in the given
     *         CalculatedDataElement
     */
    Map<DataElement, Double> getDataElementFactors( CalculatedDataElement calculatedDataElement );

    /**
     * Returns a Map of factors for the Operands in the given
     * CalculatedDataElement
     * 
     * @param calculatedDataElement CalculatedDataElement to get factors for
     * @return a map of factors for the Operands in the given
     *         CalculatedDataElement
     */
    Map<String, Double> getOperandFactors( CalculatedDataElement calculatedDataElement );

    /**
     * Returns a collection of OperandIds in the given CalculatedDataElement
     * 
     * @param calculatedDataElement CalculatedDataElement to get operands for
     * @return a collection of operands (actually string) for the expression in
     *         the given CalculatedDataElement
     */
    Collection<String> getOperandIds( CalculatedDataElement calculatedDataElement );

    /**
     * 
     * @param identifiers
     * @return
     */
    Map<Integer, String> getCalculatedDataElementExpressionMap( Collection<Integer> identifiers );

    // -------------------------------------------------------------------------
    // DataElementGroup
    // -------------------------------------------------------------------------

    /**
     * Adds a DataElementGroup.
     * 
     * @param dataElementGroup the DataElementGroup to add.
     * @return a generated unique id of the added DataElementGroup.
     */
    int addDataElementGroup( DataElementGroup dataElementGroup );

    /**
     * Updates a DataElementGroup.
     * 
     * @param dataElementGroup the DataElementGroup to update.
     */
    void updateDataElementGroup( DataElementGroup dataElementGroup );

    /**
     * Deletes a DataElementGroup.
     * 
     * @param dataElementGroup the DataElementGroup to delete.
     */
    void deleteDataElementGroup( DataElementGroup dataElementGroup );

    /**
     * Returns a DataElementGroup.
     * 
     * @param id the id of the DataElementGroup to return.
     * @return the DataElementGroup with the given id, or null if no match.
     */
    DataElementGroup getDataElementGroup( int id );

    /**
     * Returns data elements with identifiers in the given collection.
     * 
     * @param identifiers the id collection.
     * @return data elements with identifiers in the given collection.
     */
    Collection<DataElementGroup> getDataElementGroups( Collection<Integer> identifiers );

    /**
     * Returns the DataElementGroup with the given UUID.
     * 
     * @param id the UUID of the DataElementGroup to return.
     * @return the DataElementGroup with the given UUID, or null if no match.
     */
    DataElementGroup getDataElementGroup( String uuid );

    /**
     * Returns a DataElementGroup with a given name.
     * 
     * @param name the name of the DataElementGroup to return.
     * @return the DataElementGroup with the given name, or null if no match.
     */
    DataElementGroup getDataElementGroupByName( String name );

    /**
     * Returns all DataElementGroups.
     * 
     * @return a collection of all DataElementGroups, or an empty collection if
     *         no DataElementGroups exist.
     */
    Collection<DataElementGroup> getAllDataElementGroups();

    /**
     * Returns all DataElementGroups which contain the given DataElement.
     * 
     * @param dataElement the DataElement which the DataElementGroups must
     *        contain.
     * @return a collection of all DataElementGroups that contain the given
     *         DataElement.
     */
    Collection<DataElementGroup> getGroupsContainingDataElement( DataElement dataElement );

    
    /**
     * Returns data elements with identifier in the given id.
     * 
     * @param groupId is the id of data element group.
     * @return data elements with identifier in the given id.
     */
    Collection<DataElement> getDataElementsByGroupId( int groupId );

    /**
     * Defines the given data elements as zero is significant. All other data elements
     * are defined as zero is in-significant.
     * 
     * @param dataElementIds identifiers of data elements where zero is significant.
     */
    void setZeroIsSignificantForDataElements( Collection<Integer> dataElementIds );

    /**
     * Returns all DataElement which zeroIsSignificant property is true or false
     * 
     * @param zeroIsSignificant is zeroIsSignificant property
     * @return a collection of all DataElement
     */
    Collection<DataElement> getDataElementsByZeroIsSignificant( boolean zeroIsSignificant );

    /**
     * Returns all DataElement which zeroIsSignificant property is true or false
     * 
     * @param zeroIsSignificant is zeroIsSignificant property
     * @param dataElementGroup is group contain data elements
     * @return a collection of all DataElement
     */
    Collection<DataElement> getDataElementsByZeroIsSignificantAndGroup( boolean zeroIsSignificant, DataElementGroup dataElementGroup );

    Collection<DataElementGroup> getDataElementGroupsBetween( int first, int max );
    
    Collection<DataElementGroup> getDataElementGroupsBetweenByName( String name, int first, int max );
    
    int getDataElementGroupCount();
    
    int getDataElementGroupCountByName( String name );
    
    // -------------------------------------------------------------------------
    // DataElementGroupSet
    // -------------------------------------------------------------------------

    int addDataElementGroupSet( DataElementGroupSet groupSet );

    void updateDataElementGroupSet( DataElementGroupSet groupSet );

    void deleteDataElementGroupSet( DataElementGroupSet groupSet );

    DataElementGroupSet getDataElementGroupSet( int id );

    DataElementGroupSet getDataElementGroupSetByName( String name );

    Collection<DataElementGroupSet> getAllDataElementGroupSets();

    Collection<DataElementGroupSet> getDataElementGroupSets( Collection<Integer> identifiers );
    
    Collection<DataElementGroupSet> getDataElementGroupSetsBetween( int first, int max );
    
    Collection<DataElementGroupSet> getDataElementGroupSetsBetweenByName( String name, int first, int max );
    
    int getDataElementGroupSetCount();
    
    int getDataElementGroupSetCountByName( String name );

    // -------------------------------------------------------------------------
    // DataElementOperand
    // -------------------------------------------------------------------------

    /**
     * Returns all Operands. Requires the categoryoptioncomboname resource table
     * to be populated.
     * 
     * @return a collection of all Operands.
     */
    Collection<DataElementOperand> getAllGeneratedOperands();

    /**
     * Returns all generated permutations of Operands for the given collection of
     * DataElements. Requires the categoryoptioncomboname resource table to be populated.
     * 
     * @param dataElements the DataElements.
     * @return a collection of all Operands.
     */
    Collection<DataElementOperand> getAllGeneratedOperands( Collection<DataElement> dataElements );
    
}
