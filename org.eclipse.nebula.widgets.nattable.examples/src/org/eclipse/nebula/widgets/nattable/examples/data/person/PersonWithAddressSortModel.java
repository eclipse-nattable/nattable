/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.data.person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.sort.command.SortCommandHandler;

/**
 * A simple implementation for {@link ISortModel} that can be used to sort
 * {@link PersonWithAddress} objects. It uses it's own {@link Comparator} and doesn't
 * support multiple column sorting and it won't be possible to remove the sorting.
 * Of course this can be implemented to work like the default behaviour of the NatTable.
 * @author Dirk Fauth
 */
public class PersonWithAddressSortModel implements ISortModel {

	/**
	 * Array that contains the sort direction for every column.
	 * Needed to access the current sort state of a column.
	 */
	protected SortDirectionEnum[] sortDirections;
	
	/**
	 * Array that contains the sorted flags for every column.
	 * Needed to access the current sort state of a column.
	 */
	protected boolean[] sorted;

	/**
	 * As this implementation only supports single column sorting,
	 * this property contains the the column index of the column that
	 * is currently used for sorting.
	 * Initial value = -1 for no sort column
	 */
	protected int currentSortColumn = -1;
	
	/**
	 * As this implementation only supports single column sorting,
	 * this property contains the current sort direction of the column that
	 * is currently used for sorting.
	 */
	protected SortDirectionEnum currentSortDirection = SortDirectionEnum.ASC;

	/**
	 * Data list that is sorted
	 */
	private List<PersonWithAddress> persons;

	/**
	 * Creates a new {@link PersonWithAddressSortModel} for the list of objects.
	 * @param persons the list of objects that should be sorted
	 */
	public PersonWithAddressSortModel(List<PersonWithAddress> persons) {
		this.persons = persons;

		sortDirections = new SortDirectionEnum[DataModelConstants.PERSONWITHADDRESS_NUMBER_OF_COLUMNS];
		Arrays.fill(sortDirections, SortDirectionEnum.NONE);

		sorted = new boolean[DataModelConstants.PERSONWITHADDRESS_NUMBER_OF_COLUMNS];
		Arrays.fill(sorted, false);

		//call initial sorting
		sort(0, SortDirectionEnum.ASC, false);
	}
	
	/**
	 * As this is a simple implementation of an {@link ISortModel} and we don't
	 * support multiple column sorting, this list returns either a list with one
	 * entry for the current sort column or an empty list.
	 */
	@Override
	public List<Integer> getSortedColumnIndexes() {
		List<Integer> indexes = new ArrayList<Integer>();
		if (currentSortColumn > -1) {
			indexes.add(Integer.valueOf(currentSortColumn));
		}
		return indexes;
	}

	/**
	 * @return TRUE if the column with the given index is sorted at the moment.
	 */
	@Override
	public boolean isColumnIndexSorted(int columnIndex) {
		return sorted[columnIndex];
	}

	/**
	 * @return the direction in which the column with the given index is 
	 * currently sorted
	 */
	@Override
	public SortDirectionEnum getSortDirection(int columnIndex) {
		return sortDirections[columnIndex];
	}

	/**
	 * @return 0 as we don't support multiple column sorting.
	 */
	@Override
	public int getSortOrder(int columnIndex) {
		return 0;
	}

	/**
	 * Remove all sorting
	 */
	@Override
	public void clear() {
		Arrays.fill(sortDirections, SortDirectionEnum.NONE);
		Arrays.fill(sorted, false);
		this.currentSortColumn = -1;
	}

	/**
	 * This method is called by the {@link SortCommandHandler} in response to a sort command.
	 * It is responsible for sorting the requested column.
	 */
	@Override
	public void sort(int columnIndex, SortDirectionEnum sortDirection, boolean accumulate) {
		if (!isColumnIndexSorted(columnIndex)) {
			clear();
		}

		if (sortDirection.equals(SortDirectionEnum.NONE)) {
			//we don't support NONE as user action
			sortDirection = SortDirectionEnum.ASC;
		}

        Collections.sort(persons, new PersonWithAddressComparator(columnIndex, sortDirection));
		sortDirections[columnIndex] = sortDirection;
		sorted[columnIndex] = sortDirection.equals(SortDirectionEnum.NONE) ? false : true;

		currentSortColumn = columnIndex;
		currentSortDirection = sortDirection;
	}

	/**
	 * Custom comparator for {@link PersonWithAddress} objects
	 */
	class PersonWithAddressComparator implements Comparator<PersonWithAddress> {

		int colIdx = 0;
		SortDirectionEnum sortDirection;

		public PersonWithAddressComparator(int columnIndex, SortDirectionEnum sortDirection) {
			this.colIdx = columnIndex;
			this.sortDirection = sortDirection;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(PersonWithAddress pwa1, PersonWithAddress pwa2) {
	        Comparable compareObject1 = null;
	        Comparable compareObject2 = null;
            Address address1 = pwa1.getAddress();
            Address address2 = pwa2.getAddress();

			switch (colIdx) {
		        case DataModelConstants.FIRSTNAME_COLUMN_POSITION:
		        	compareObject1 = pwa1.getFirstName();
		        	compareObject2 = pwa2.getFirstName();
		        	break;
		        case DataModelConstants.LASTNAME_COLUMN_POSITION:
		        	compareObject1 = pwa1.getLastName();
		        	compareObject2 = pwa2.getLastName();
		        	break;
		        case DataModelConstants.GENDER_COLUMN_POSITION:
		        	compareObject1 = pwa1.getGender().ordinal();
		        	compareObject2 = pwa2.getGender().ordinal();
		        	break;
		        case DataModelConstants.MARRIED_COLUMN_POSITION:
		        	compareObject1 = pwa1.isMarried();
		        	compareObject2 = pwa2.isMarried();
		        	break;
		        case DataModelConstants.BIRTHDAY_COLUMN_POSITION:
		        	compareObject1 = pwa1.getBirthday();
		        	compareObject2 = pwa2.getBirthday();
		        	break;
		        case DataModelConstants.STREET_COLUMN_POSITION:
		        	compareObject1 = address1.getStreet();
		        	compareObject2 = address2.getStreet();
		        	break;
		        case DataModelConstants.HOUSENUMBER_COLUMN_POSITION:
		        	compareObject1 = address1.getHousenumber();
		        	compareObject2 = address2.getHousenumber();
		        	break;
		        case DataModelConstants.POSTALCODE_COLUMN_POSITION:
		        	compareObject1 = address1.getPostalCode();
		        	compareObject2 = address2.getPostalCode();
		        	break;
		        case DataModelConstants.CITY_COLUMN_POSITION:
		        	compareObject1 = address1.getCity();
		        	compareObject2 = address2.getCity();
		        	break;
	        }

			int result = 0;

			//make null safe compare
			if (compareObject1 == null && compareObject2 != null) {
				result = -1;
			}
			else if (compareObject1 == null && compareObject2 == null) {
				result = 0;
			}
			else if (compareObject1 != null && compareObject2 == null) {
				result = 1;
			}
			else {
				result = compareObject1.compareTo(compareObject2);
			}

			//negate compare result if sort direction is descending
        	if (sortDirection.equals(SortDirectionEnum.DESC)) {
        		result = result * -1;
        	}

        	return result;
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.sort.ISortModel#getComparatorsForColumnIndex(int)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List<Comparator> getComparatorsForColumnIndex(int columnIndex) {
		return null;
	}
}
