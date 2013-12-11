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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import static org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX;
import static org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes.FILTER_COMPARATOR;
import static org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER;
import static org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes.TEXT_DELIMITER;
import static org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes.TEXT_MATCHING_MODE;
import static org.eclipse.nebula.widgets.nattable.style.DisplayMode.NORMAL;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.filterrow.IFilterStrategy;
import org.eclipse.nebula.widgets.nattable.filterrow.ParseResult;
import org.eclipse.nebula.widgets.nattable.filterrow.ParseResult.MatchType;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.FunctionList.Function;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.matchers.ThresholdMatcherEditor;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

public class DefaultGlazedListsFilterStrategy<T> implements IFilterStrategy<T> {

	private static final Log log = LogFactory.getLog(DefaultGlazedListsFilterStrategy.class);

	protected final IColumnAccessor<T> columnAccessor;
	protected final IConfigRegistry configRegistry;
	private final CompositeMatcherEditor<T> matcherEditor;
	
	protected FilterList<T> filterList;
	protected ReadWriteLock filterLock;
	
	/**
	 * Create a new DefaultGlazedListsFilterStrategy on top of the given FilterList.
	 * <p>
	 * Note: Using this constructor you don't need to create and set the CompositeMatcherEditor as MatcherEditor on 
	 * 		 the FilterList yourself! The necessary steps to get it working is done within this constructor.
	 * @param filterList The FilterList that is used within the GlazedLists based NatTable for filtering. 
	 * @param columnAccessor The IColumnAccessor necessary to access the column data of the row objects in the FilterList.
	 * @param configRegistry The IConfigRegistry necessary to retrieve filter specific configurations.
	 */
	public DefaultGlazedListsFilterStrategy(FilterList<T> filterList, IColumnAccessor<T> columnAccessor, IConfigRegistry configRegistry) {
		this(filterList, new CompositeMatcherEditor<T>(), columnAccessor, configRegistry);
		this.matcherEditor.setMode(CompositeMatcherEditor.AND);		
	}
	
	/**
	 * Create a new DefaultGlazedListsFilterStrategy on top of the given FilterList using the given CompositeMatcherEditor.
	 * This is necessary to support connection of multiple filter rows.
	 * <p>
	 * Note: Using this constructor you need to create the CompositeMatcherEditor yourself. It will be added automatically
	 * 		 to the given FilterList, so you can skip that step. 
	 * @param filterList The FilterList that is used within the GlazedLists based NatTable for filtering. 
	 * @param matcherEditor The CompositeMatcherEditor that should be used by this DefaultGlazedListsFilterStrategy. 
	 * @param columnAccessor The IColumnAccessor necessary to access the column data of the row objects in the FilterList.
	 * @param configRegistry The IConfigRegistry necessary to retrieve filter specific configurations.
	 */
	public DefaultGlazedListsFilterStrategy(FilterList<T> filterList, CompositeMatcherEditor<T> matcherEditor, 
			IColumnAccessor<T> columnAccessor, IConfigRegistry configRegistry) {
		this.columnAccessor = columnAccessor;
		this.configRegistry = configRegistry;
		
		this.matcherEditor = matcherEditor;

		this.filterList = filterList;
		this.filterList.setMatcherEditor(this.matcherEditor);
		
		this.filterLock = filterList.getReadWriteLock();
	}

	/**
	 * Create GlazedLists matcher editors and apply them to facilitate filtering.
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void applyFilter(Map<Integer, Object> filterIndexToObjectMap) {
		
		//wait until all listeners had the chance to handle the clear event
		try {
			this.filterLock.writeLock().lock();
			this.matcherEditor.getMatcherEditors().clear();
		}
		finally {
			this.filterLock.writeLock().unlock();
		}
		
		if (filterIndexToObjectMap.isEmpty()) {
			return;
		}
		
		try {
			EventList<MatcherEditor<T>> matcherEditors = new BasicEventList<MatcherEditor<T>>();

			for (Entry<Integer, Object> mapEntry : filterIndexToObjectMap.entrySet()) {
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
			
			//wait until all listeners had the chance to handle the clear event
			try {
				this.filterLock.writeLock().lock();
				this.matcherEditor.getMatcherEditors().addAll(matcherEditors);
			}
			finally {
				this.filterLock.writeLock().unlock();
			}

		} catch (Exception e) {
			log.error("Error on applying a filter", e); //$NON-NLS-1$
		}
	}

	/**
	 * Converts the object inserted to the filter cell at the given column position to the corresponding String.
	 * @param columnIndex The column index of the filter cell that should be processed.
	 * @param object The value set to the filter cell that needs to be converted
	 * @return The String value for the given filter value.
	 */
	protected String getStringFromColumnObject(final int columnIndex, final Object object) {
		final IDisplayConverter displayConverter = 
				this.configRegistry.getConfigAttribute(
						FILTER_DISPLAY_CONVERTER, NORMAL, FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);
		return displayConverter.canonicalToDisplayValue(object).toString();
	}
	
	/**
	 * Set up a threshold matcher for tokens like '&gt;20', '&lt;=10' etc.
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
			@Override
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
			@Override
			public void getFilterStrings(List<String> objectAsListOfStrings, T rowObject) {
				Object cellData = columnAccessor.getDataValue(rowObject, columnIndex);
				Object displayValue = converter.canonicalToDisplayValue(cellData);
				displayValue = (displayValue != null) ? displayValue : ""; //$NON-NLS-1$
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

	/**
	 * Returns the CompositeMatcherEditor that is created and used by this IFilterStrategy.
	 * In prior versions it was necessary to create the CompositeMatcherEditor outside this
	 * class and use it as constructor parameter. We changed this to hide that implementation
	 * from users and to ensure that filter operations and possible listeners are executed
	 * thread safe. Otherwise there might be concurrency issues while filtering.
	 * <p> 
	 * If you want to use additional filtering you should now use this method to work on the 
	 * created CompositeMatcherEditor instead of creating one outside. For static filtering
	 * additional to the filter row you might want to consider using the 
	 * DefaultGlazedListsStaticFilterStrategy.
	 * 
	 * @return The CompositeMatcherEditor that is created and used by this IFilterStrategy.
	 */
	public CompositeMatcherEditor<T> getMatcherEditor() {
		return matcherEditor;
	}
}
