/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.hideshow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.hideshow.IRowHideShowCommandLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowShowCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllRowsCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;

/**
 * Adds the functionality for manually hiding rows in a NatTable that is based on GlazedLists.
 * Technically it will filter items by id in the {@link FilterList}.
 * <p>
 * Note: If you want to add row hide/show AND filtering to your NatTable, you need to use the
 * 		 DefaultGlazedListsStaticFilterStrategy and add the {@link Matcher} that is used by
 * 		 this command as a static {@link MatcherEditor}. Otherwise these two functions will not work
 * 		 correctly together.
 * 
 * @author Dirk Fauth
 *
 */
public class GlazedListsRowHideShowLayer<T> extends AbstractLayerTransform 
	implements IRowHideShowCommandLayer, IUniqueIndexLayer {

	private static final Log log = LogFactory.getLog(GlazedListsRowHideShowLayer.class);
	
	/**
	 * Key for persisting the number of hidden row id's.
	 * This is necessary because the id's are serializables and reading them againd
	 * by ObjectInputStream need to know how long to read.
	 */
	public static final String PERSISTENCE_KEY_HIDDEN_ROW_IDS_COUNT = ".hiddenRowIDsCount"; //$NON-NLS-1$
	public static final String PERSISTENCE_KEY_HIDDEN_ROW_IDS = ".hiddenRowIDs"; //$NON-NLS-1$
	
	/**
	 * The collection of row id's that are hidden.
	 */
	private final Set<Serializable> rowIdsToHide = new HashSet<Serializable>();
	
	/**
	 * The {@link IRowIdAccessor} that is used to extract the id out of the row object.
	 * This is necessary to determine the row object to hide in terms of content.
	 */
	private final IRowIdAccessor<T> rowIdAccessor;
	
	/**
	 * The {@link IRowDataProvider} needed to get the object at a given position so it
	 * is possible to retrieve the id that is used for filtering.
	 */
	private final IRowDataProvider<T> rowDataProvider;
	
	/**
	 * The {@link MatcherEditor} that is used to filter the rows with the specified id's.
	 */
	private final HideRowMatcherEditor hideRowByIdMatcherEditor = new HideRowMatcherEditor();

	/**
	 * Creates a {@link GlazedListsRowHideShowLayer} for adding row hide/show for GlazedLists based
	 * NatTables. Using this constructor will only instantiate this layer. To use this correctly
	 * you need to register the local row hide {@link MatcherEditor} against the {@link FilterList} yourself.
	 * @param underlyingLayer The underlying layer.
	 * @param rowDataProvider The {@link IRowDataProvider} needed to get the object at a given position so it
	 * 			is possible to retrieve the id that is used for filtering.
	 * @param rowIdAccessor The {@link IRowIdAccessor} that is used to extract the id out of the row object.
	 * 			This is necessary to determine the row object to hide in terms of content.
	 */
	public GlazedListsRowHideShowLayer(
			ILayer underlyingLayer, IRowDataProvider<T> rowDataProvider, IRowIdAccessor<T> rowIdAccessor) {
		super(underlyingLayer);
		if (rowIdAccessor == null) {
			throw new IllegalArgumentException("rowIdAccessor can not be null!"); //$NON-NLS-1$
		}
		
		this.rowDataProvider = rowDataProvider;
		this.rowIdAccessor = rowIdAccessor;
		
		registerCommandHandlers();
	}
	
	/**
	 * Creates a {@link GlazedListsRowHideShowLayer} for adding row hide/show for GlazedLists based
	 * NatTables. Using this constructor will add the {@link MatcherEditor} to the given {@link FilterList}.
	 * This might not work correctly in combination with other filters like e.g. the filter row.
	 * @param underlyingLayer The underlying layer.
	 * @param rowDataProvider The {@link IRowDataProvider} needed to get the object at a given position so it
	 * 			is possible to retrieve the id that is used for filtering.
	 * @param rowIdAccessor The {@link IRowIdAccessor} that is used to extract the id out of the row object.
	 * 			This is necessary to determine the row object to hide in terms of content.
	 * @param filterList The {@link FilterList} to apply the local row hide {@link Matcher} to.
	 */
	public GlazedListsRowHideShowLayer(
			ILayer underlyingLayer, IRowDataProvider<T> rowDataProvider, IRowIdAccessor<T> rowIdAccessor, FilterList<T> filterList) {
		super(underlyingLayer);
		if (rowIdAccessor == null) {
			throw new IllegalArgumentException("rowIdAccessor can not be null!"); //$NON-NLS-1$
		}
		if (filterList == null) {
			throw new IllegalArgumentException("filterList can not be null!"); //$NON-NLS-1$
		}
		
		this.rowDataProvider = rowDataProvider;
		this.rowIdAccessor = rowIdAccessor;
		
		filterList.setMatcherEditor(this.hideRowByIdMatcherEditor);
		
		registerCommandHandlers();
	}

	/**
	 * Creates a {@link GlazedListsRowHideShowLayer} for adding row hide/show for GlazedLists based
	 * NatTables. Using this constructor will add the {@link MatcherEditor} to the given 
	 * {@link CompositeMatcherEditor}. This way it is possible to add more filter logic than only the 
	 * row hide filter.
	 * @param underlyingLayer The underlying layer.
	 * @param rowDataProvider The {@link IRowDataProvider} needed to get the object at a given position so it
	 * 			is possible to retrieve the id that is used for filtering.
	 * @param rowIdAccessor The {@link IRowIdAccessor} that is used to extract the id out of the row object.
	 * 			This is necessary to determine the row object to hide in terms of content.
	 * @param matcherEditor The {@link CompositeMatcherEditor} to which the local row hide {@link Matcher}
	 * 			should be added.
	 */
	public GlazedListsRowHideShowLayer(
			ILayer underlyingLayer, IRowDataProvider<T> rowDataProvider, IRowIdAccessor<T> rowIdAccessor, CompositeMatcherEditor<T> matcherEditor) {
		super(underlyingLayer);
		if (rowIdAccessor == null) {
			throw new IllegalArgumentException("rowIdAccessor can not be null!"); //$NON-NLS-1$
		}
		if (matcherEditor == null) {
			throw new IllegalArgumentException("matcherEditor can not be null!"); //$NON-NLS-1$
		}
		
		this.rowDataProvider = rowDataProvider;
		this.rowIdAccessor = rowIdAccessor;
		
		matcherEditor.getMatcherEditors().add(this.hideRowByIdMatcherEditor);
		
		registerCommandHandlers();
	}

	/**
	 * Register the {@link ILayerCommandHandler} that will handle the row hide/show events
	 * for this layer.
	 */
	protected void registerCommandHandlers() {
		registerCommandHandler(new RowHideCommandHandler(this));
		registerCommandHandler(new MultiRowHideCommandHandler(this));
		registerCommandHandler(new ShowAllRowsCommandHandler(this));
		registerCommandHandler(new MultiRowShowCommandHandler(this));
	}
	
	/**
	 * Hide the rows at the given positions. Will collect the id's of the rows at
	 * the given positions as this layer operates on content rather than positions.
	 * @param rowPositions The positions of the rows to hide.
	 */
	@Override
	public void hideRowPositions(Collection<Integer> rowPositions) {
		Collection<Serializable> rowIds = new HashSet<Serializable>();
		for (Integer rowPos : rowPositions) {
			int rowIndex = getRowIndexByPosition(rowPos);
			rowIds.add(rowIdAccessor.getRowId(rowDataProvider.getRowObject(rowIndex)));
		}
		hideRows(rowIds);
	}
	
	/**
	 * Hide the rows at the given indexes. Will collect the id's of the rows at
	 * the given positions as this layer operates on content rather than positions.
	 * @param rowIndexes The indexes of the rows to hide.
	 */
	@Override
	public void hideRowIndexes(Collection<Integer> rowIndexes) {
		Collection<Serializable> rowIds = new HashSet<Serializable>();
		for (Integer rowIndex : rowIndexes) {
			rowIds.add(rowIdAccessor.getRowId(rowDataProvider.getRowObject(rowIndex)));
		}
		hideRows(rowIds);
	}
	
	/**
	 * Show the rows with the given indexes again. Will collect the id's of the rows at
	 * the given positions as this layer operates on content rather than positions.
	 * @param rowIndexes The indexes of the rows that should be showed again.
	 */
	@Override
	public void showRowIndexes(Collection<Integer> rowIndexes) {
		Collection<Serializable> rowIds = new HashSet<Serializable>();
		for (Integer rowIndex : rowIndexes) {
			rowIds.add(rowIdAccessor.getRowId(rowDataProvider.getRowObject(rowIndex)));
		}
		showRows(rowIds);
	}
	
	/**
	 * Hide the rows that contain the objects with the given row ids.
	 * @param rowIds The row ids of the rows that should be hidden.
	 */
	public void hideRows(Collection<Serializable> rowIds) {
		this.rowIdsToHide.addAll(rowIds);
		this.hideRowByIdMatcherEditor.fireChange();
	}

	/**
	 * Show the rows that contain the objects with the given row ids.
	 * Will of course only have impact if those rows are currently hidden.
	 * @param rowIds The row ids of the rows that should be showed.
	 */
	public void showRows(Collection<Serializable> rowIds) {
		this.rowIdsToHide.removeAll(rowIds);
		this.hideRowByIdMatcherEditor.fireChange();
	}

	/**
	 * Show all rows that where previously hidden.
	 */
	@Override
	public void showAllRows() {
		this.rowIdsToHide.clear();
		this.hideRowByIdMatcherEditor.fireChange();
	}
	
	/**
	 * @return The {@link MatcherEditor} that is used to filter the rows with the specified id's.
	 */
	public MatcherEditor<T> getMatcherEditor() {
		return this.hideRowByIdMatcherEditor;
	}

	@Override
	public int getColumnPositionByIndex(int columnIndex) {
		return ((IUniqueIndexLayer) getUnderlyingLayer()).getColumnPositionByIndex(columnIndex);
	}

	@Override
	public int getRowPositionByIndex(int rowIndex) {
		return ((IUniqueIndexLayer) getUnderlyingLayer()).getRowPositionByIndex(rowIndex);
	}
	
	@Override
	public void saveState(String prefix, Properties properties) {
		if (this.rowIdsToHide.size() > 0) {
			//store the number of row id's that will be hidden
			properties.setProperty(prefix + PERSISTENCE_KEY_HIDDEN_ROW_IDS_COUNT, 
					Integer.valueOf(this.rowIdsToHide.size()).toString()); 
					
			ObjectOutputStream out = null;
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				out = new ObjectOutputStream(bos);
				for (Serializable serializable : this.rowIdsToHide) {
					out.writeObject(serializable);
				}
				properties.setProperty(prefix + PERSISTENCE_KEY_HIDDEN_ROW_IDS, 
						new String(Base64.encodeBase64(bos.toByteArray())));
			} catch (Exception e) {
				log.error("Error while persisting GlazedListsRowHideShowLayer state", e); //$NON-NLS-1$
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		super.saveState(prefix, properties);
	}
	
	
	@Override
	public void loadState(String prefix, Properties properties) {
		this.rowIdsToHide.clear();
		String property = properties.getProperty(prefix + PERSISTENCE_KEY_HIDDEN_ROW_IDS_COUNT);
		int count = property != null ? Integer.valueOf(property) : 0;
		property = properties.getProperty(prefix + PERSISTENCE_KEY_HIDDEN_ROW_IDS);
		if (property != null) {
			ObjectInputStream in = null;
			try {
				ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(property.getBytes()));
				in = new ObjectInputStream(bis);
				Serializable ser = null;
				for (int i = 0; i < count; i++) {
					ser = (Serializable)in.readObject();
					this.rowIdsToHide.add(ser);
				}
			} catch (Exception e) {
				log.error("Error while restoring GlazedListsRowHideShowLayer state", e); //$NON-NLS-1$
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		this.hideRowByIdMatcherEditor.fireChange();
		
		super.loadState(prefix, properties);
	}
	
	/**
	 * {@link MatcherEditor} implementation that will only match objects that are not hidden by
	 * id. It also enables to fire change events to indicate changes to the filter.
	 * 
	 * @author Dirk Fauth
	 *
	 */
	class HideRowMatcherEditor extends AbstractMatcherEditor<T> {
		/**
		 * The {@link Matcher} that is used to filter the rows with the specified id's.
		 */
		private final Matcher<T> hideRowByIdMatcher = new Matcher<T>() {
			@Override
			public boolean matches(T rowObject) {
				return !rowIdsToHide.contains(rowIdAccessor.getRowId(rowObject));
			}
		};
		
		/**
		 * Fire a change so the listeners can be informed about a change for the {@link Matcher}
		 */
		public void fireChange() {
			fireChanged(this.hideRowByIdMatcher);
		}
	}

}
