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
package org.eclipse.nebula.widgets.nattable.hideshow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowShowCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllRowsCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;


public class RowHideShowLayer extends AbstractRowHideShowLayer {

	public static final String PERSISTENCE_KEY_HIDDEN_ROW_INDEXES = ".hiddenRowIndexes"; //$NON-NLS-1$
	
	private final Set<Integer> hiddenRowIndexes;
	
	public RowHideShowLayer(IUniqueIndexLayer underlyingLayer) {
		super(underlyingLayer);
		this.hiddenRowIndexes = new TreeSet<Integer>();
		
		registerCommandHandler(new MultiRowHideCommandHandler(this));
		registerCommandHandler(new RowHideCommandHandler(this));
		registerCommandHandler(new ShowAllRowsCommandHandler(this));
		registerCommandHandler(new MultiRowShowCommandHandler(this));
	}

	// Persistence
	
	@Override
	public void saveState(String prefix, Properties properties) {
		if (hiddenRowIndexes.size() > 0) {
			StringBuilder strBuilder = new StringBuilder();
			for (Integer index : hiddenRowIndexes) {
				strBuilder.append(index);
				strBuilder.append(',');
			}
			properties.setProperty(prefix + PERSISTENCE_KEY_HIDDEN_ROW_INDEXES, strBuilder.toString());
		}
		
		super.saveState(prefix, properties);
	}
	
	@Override
	public void loadState(String prefix, Properties properties) {
		String property = properties.getProperty(prefix + PERSISTENCE_KEY_HIDDEN_ROW_INDEXES);
		if (property != null) {
			hiddenRowIndexes.clear();
			
			StringTokenizer tok = new StringTokenizer(property, ","); //$NON-NLS-1$
			while (tok.hasMoreTokens()) {
				String index = tok.nextToken();
				hiddenRowIndexes.add(Integer.valueOf(index));
			}
		}
		
		super.loadState(prefix, properties);
	}
	
	// Hide/show	
	
	@Override
	public boolean isRowIndexHidden(int rowIndex) {
		return hiddenRowIndexes.contains(Integer.valueOf(rowIndex));
	}

	@Override
	public Collection<Integer> getHiddenRowIndexes() {
		return hiddenRowIndexes; 
	}
	
	public void hideRowPositions(Collection<Integer> rowPositions) {
		Set<Integer> rowIndexes = new HashSet<Integer>();
		for (Integer rowPosition : rowPositions) {
			rowIndexes.add(Integer.valueOf(getRowIndexByPosition(rowPosition.intValue())));
		}
		hiddenRowIndexes.addAll(rowIndexes);
		invalidateCache();
		fireLayerEvent(new HideRowPositionsEvent(this, rowPositions));
	}
	
	public void showRowIndexes(int[] rowIndexes) {
		Set<Integer> rowIndexesSet = new HashSet<Integer>();
		for (int i = 0; i < rowIndexes.length; i++) {
			rowIndexesSet.add(Integer.valueOf(rowIndexes[i]));
		}
		hiddenRowIndexes.removeAll(rowIndexesSet);
		invalidateCache();
		fireLayerEvent(new ShowRowPositionsEvent(this, getRowPositionsByIndexes(rowIndexes)));
	}
	
	protected void showRowIndexes(Collection<Integer> rowIndexes) {
		for (int rowIndex : rowIndexes) {
			hiddenRowIndexes.remove(Integer.valueOf(rowIndex));
		}
		invalidateCache();
		// Since we are exposing this method for showing individual rows, a structure event must be fired here.
		fireLayerEvent(new ShowRowPositionsEvent(this, rowIndexes));
	}

	public void showAllRows() {
		Collection<Integer> hiddenRows = new ArrayList<Integer>(hiddenRowIndexes);
		hiddenRowIndexes.clear();
		invalidateCache();
		fireLayerEvent(new ShowRowPositionsEvent(this, hiddenRows));
	}
	
	private Collection<Integer> getRowPositionsByIndexes(int[] rowIndexes) {
		Collection<Integer> rowPositions = new HashSet<Integer>();
		for (int rowIndex : rowIndexes) {
			rowPositions.add(Integer.valueOf(getRowPositionByIndex(rowIndex)));
		}
		return rowPositions;
	}
}
