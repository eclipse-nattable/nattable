/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import static org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX;
import static org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes.FILTER_COMPARATOR;
import static org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER;
import static org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes.TEXT_DELIMITER;
import static org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes.TEXT_MATCHING_MODE;
import static org.eclipse.nebula.widgets.nattable.style.DisplayMode.NORMAL;
import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.filterrow.IFilterStrategy;
import org.eclipse.nebula.widgets.nattable.filterrow.ParseResult;
import org.eclipse.nebula.widgets.nattable.filterrow.ParseResult.MatchType;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.FunctionList.Function;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.matchers.ThresholdMatcherEditor;

public class DefaultGlazedListsFilterStrategy<T> implements IFilterStrategy<T> {

	private final IColumnAccessor<T> columnAccessor;
	private final IConfigRegistry configRegistry;
	protected final CompositeMatcherEditor<T> matcherEditor;
	

	public DefaultGlazedListsFilterStrategy(CompositeMatcherEditor<T> matcherEditor, IColumnAccessor<T> columnAccessor, IConfigRegistry configRegistry) {
		this.columnAccessor = columnAccessor;
		this.configRegistry = configRegistry;
		this.matcherEditor = matcherEditor;
	}

	/**
	 * Create GlazedLists matcher editors and apply them to facilitate filtering.
	 */
	@SuppressWarnings("unchecked")
	public void applyFilter(Map<Integer, Object> filterObjectByIndex) {
		try {
			matcherEditor.getMatcherEditors().clear();
			
			if (filterObjectByIndex.isEmpty()) {
				return;
			}
			
			EventList<MatcherEditor<T>> matcherEditors = new BasicEventList<MatcherEditor<T>>();

			for (Entry<Integer, Object> mapEntry : filterObjectByIndex.entrySet()) {
				Integer columnIndex = mapEntry.getKey();
				String filterText = getStringFromColumnObject(columnIndex, mapEntry.getValue());
				
				String textDelimiter = configRegistry.getConfigAttribute(
						TEXT_DELIMITER, NORMAL, FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);
				TextMatchingMode textMatchingMode = configRegistry.getConfigAttribute(
						TEXT_MATCHING_MODE, NORMAL, FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);
				IDisplayConverter displayConverter = configRegistry.getConfigAttribute(
						FILTER_DISPLAY_CONVERTER, NORMAL, FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);
				Comparator comparator = configRegistry.getConfigAttribute(
						FILTER_COMPARATOR, NORMAL, FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);
				final Function<T, Object> columnValueProvider = getColumnValueProvider(columnIndex);
				
				List<ParseResult> parseResults = FilterRowUtils.parse(filterText, textDelimiter, textMatchingMode);

				EventList<MatcherEditor<T>> stringMatcherEditors = new BasicEventList<MatcherEditor<T>>();
				for (ParseResult parseResult : parseResults)
				{
					MatchType matchOperation = parseResult.getMatchOperation();
					if (matchOperation == MatchType.NONE) {
						stringMatcherEditors.add(getTextMatcherEditor(columnIndex, textMatchingMode, displayConverter, parseResult.getValueToMatch()));
					} else {
						Object threshold = displayConverter.displayToCanonicalValue(parseResult.getValueToMatch());
						matcherEditors.add(getThresholdMatcherEditor(columnIndex, threshold, comparator, columnValueProvider, matchOperation));
					}
				}
				
				if (stringMatcherEditors.size()>0){
					CompositeMatcherEditor<T> stringCompositeMatcherEditor = new CompositeMatcherEditor<T>(stringMatcherEditors);
					stringCompositeMatcherEditor.setMode(CompositeMatcherEditor.OR);
					matcherEditors.add(stringCompositeMatcherEditor);
				}
			}
			
			matcherEditor.getMatcherEditors().addAll(matcherEditors);
			matcherEditor.setMode(CompositeMatcherEditor.AND);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private String getStringFromColumnObject(final int columnIndex, final Object object) {
		final IDisplayConverter displayConverter = this.configRegistry.getConfigAttribute(FILTER_DISPLAY_CONVERTER, NORMAL, FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);
		return displayConverter.canonicalToDisplayValue(object).toString();
	}
	
	/**
	 * Set up a threshold matcher for tokens like '>20', '<=10' etc.
	 * @param columnIndex of the column for which the matcher editor is being set up
	 */
	protected ThresholdMatcherEditor<T, Object> getThresholdMatcherEditor(Integer columnIndex, Object threshold, Comparator<Object> comparator, Function<T, Object> columnValueProvider, MatchType matchOperation) {
		ThresholdMatcherEditor<T, Object> thresholdMatcherEditor = new ThresholdMatcherEditor<T, Object>(
				threshold,
				null,
				comparator,
				columnValueProvider);

		FilterRowUtils.setMatchOperation(thresholdMatcherEditor, matchOperation);
		return thresholdMatcherEditor;
	}

	/**
	 * @return Function which exposes the content of the given column index from the row object
	 */
	protected FunctionList.Function<T, Object> getColumnValueProvider(final int columnIndex) {
		return new FunctionList.Function<T, Object>() {
			public Object evaluate(T rowObject) {
				return columnAccessor.getDataValue(rowObject, columnIndex);
			}
		};
	}

	/**
	 * Sets up a text matcher editor for String tokens
	 * @param columnIndex of the column for which the matcher editor is being set up
	 * @param filterText text entered by the user in the filter row
	 */
	protected TextMatcherEditor<T> getTextMatcherEditor(Integer columnIndex, TextMatchingMode textMatchingMode, IDisplayConverter converter, String filterText) {
		TextMatcherEditor<T> textMatcherEditor = new TextMatcherEditor<T>(getTextFilterator(columnIndex, converter));
		textMatcherEditor.setFilterText(new String[] { filterText });
		textMatcherEditor.setMode(getGlazedListsTextMatcherEditorMode(textMatchingMode));
		return textMatcherEditor;
	}

	/**
	 * @return {@link TextFilterator} which exposes the contents of the column as a {@link String}
	 */
	protected TextFilterator<T> getTextFilterator(final Integer columnIndex, final IDisplayConverter converter) {
		return new TextFilterator<T>() {
			public void getFilterStrings(List<String> objectAsListOfStrings, T rowObject) {
				Object cellData = columnAccessor.getDataValue(rowObject, columnIndex);
				Object displayValue = converter.canonicalToDisplayValue(cellData);
				displayValue = isNotNull(displayValue) ? displayValue : ""; //$NON-NLS-1$
				objectAsListOfStrings.add(displayValue.toString());
			}
		};
	}

	/**
	 * @return the equivalent for GlazedLists TextMatcherEditor.
	 */
	public int getGlazedListsTextMatcherEditorMode(TextMatchingMode textMatchingMode) {
		switch (textMatchingMode) {
		case EXACT:
			return TextMatcherEditor.EXACT;
		case STARTS_WITH:
			return TextMatcherEditor.STARTS_WITH;
		case REGULAR_EXPRESSION:
			return TextMatcherEditor.REGULAR_EXPRESSION;
		default:
			return TextMatcherEditor.CONTAINS;
		}
	}

}
