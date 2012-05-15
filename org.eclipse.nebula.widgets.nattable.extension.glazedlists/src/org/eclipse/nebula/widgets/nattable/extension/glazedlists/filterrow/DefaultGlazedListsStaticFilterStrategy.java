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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.DefaultGlazedListsFilterStrategy;
import org.eclipse.nebula.widgets.nattable.filterrow.IFilterStrategy;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;

/**
 * Default implementation of an {@link IFilterStrategy} for the filter row which can
 * also take static filters and combine them with the filter logic from the filter row.
 * 
 * @author fipro
 *
 * @param <T> the type of the objects shown within the NatTable
 */
public class DefaultGlazedListsStaticFilterStrategy<T> extends
		DefaultGlazedListsFilterStrategy<T> {

	protected Map<Matcher<T>, MatcherEditor<T>> staticMatcherEditor = new HashMap<Matcher<T>, MatcherEditor<T>>();

	public DefaultGlazedListsStaticFilterStrategy(
			CompositeMatcherEditor<T> matcherEditor,
			IColumnAccessor<T> columnAccessor, IConfigRegistry configRegistry) {
		super(matcherEditor, columnAccessor, configRegistry);
	}

	/**
	 * {@inheritDoc}
	 * Always adds the static matchers.
	 */
	@Override
	public void applyFilter(Map<Integer, Object> filterObjectByIndex) {
		super.applyFilter(filterObjectByIndex);
		this.matcherEditor.getMatcherEditors().addAll(staticMatcherEditor.values());
	}
	
	/**
	 * Add a static filter to this filter strategy which will always be applied additionally
	 * to any other filter.
	 * @param matcher the static filter to add
	 */
	public void addStaticFilter(final Matcher<T> matcher) {
		//create a new MatcherEditor
		MatcherEditor<T> matcherEditor = new AbstractMatcherEditor<T>() {
			@Override
			public Matcher<T> getMatcher() {
				return matcher;
			}
		};
		
		addStaticFilter(matcherEditor);
	}
	
	/**
	 * Add a static filter to this filter strategy which will always be applied additionally
	 * to any other filter.
	 * @param matcherEditor the static filter to add
	 */
	public void addStaticFilter(final MatcherEditor<T> matcherEditor) {
		//add the new MatcherEditor to the CompositeMatcherEditor
		this.matcherEditor.getMatcherEditors().add(matcherEditor);
		
		//remember the MatcherEditor so it can be restored after new MatcherEditors are added by the FilterRow
		staticMatcherEditor.put(matcherEditor.getMatcher(), matcherEditor);
	}
	
	/**
	 * Remove the static filter from this filter strategy.
	 * @param matcher the filter to remove
	 */
	public void removeStaticFilter(final Matcher<T> matcher) {
		staticMatcherEditor.remove(matcher);
	}
	
	/**
	 * Remove the static filter from this filter strategy.
	 * @param matcherEditor the filter to remove
	 */
	public void removeStaticFilter(final MatcherEditor<T> matcherEditor) {
		staticMatcherEditor.remove(matcherEditor.getMatcher());
	}
}
