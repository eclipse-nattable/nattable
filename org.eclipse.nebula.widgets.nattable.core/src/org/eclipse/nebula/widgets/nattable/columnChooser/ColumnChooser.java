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
package org.eclipse.nebula.widgets.nattable.columnChooser;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.columnChooser.gui.ColumnChooserDialog;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.command.ColumnGroupExpandCollapseCommand;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnsAndGroupsCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.swt.widgets.Shell;

public class ColumnChooser {

	private static final Comparator<ColumnEntry> COLUMN_ENTRY_LABEL_COMPARATOR = new Comparator<ColumnEntry>() {
		public int compare(ColumnEntry o1, ColumnEntry o2) {
			return o1.getLabel().compareToIgnoreCase(o2.getLabel());
		}
	};
	
	protected final ColumnChooserDialog columnChooserDialog;
	protected final ColumnHideShowLayer columnHideShowLayer;
	protected final DataLayer columnHeaderDataLayer;
	protected final ColumnHeaderLayer columnHeaderLayer;
	protected List<ColumnEntry> hiddenColumnEntries;
	protected List<ColumnEntry> visibleColumnsEntries;
	protected final ColumnGroupModel columnGroupModel;
	protected final SelectionLayer selectionLayer;
	protected final boolean sortAvailableColumns;

	public ColumnChooser(Shell shell,
			SelectionLayer selectionLayer,
			ColumnHideShowLayer columnHideShowLayer,
			ColumnHeaderLayer columnHeaderLayer,
			DataLayer columnHeaderDataLayer,
			ColumnGroupHeaderLayer columnGroupHeaderLayer,
			ColumnGroupModel columnGroupModel,
			boolean sortAvailableColumns) {
		this.selectionLayer = selectionLayer;
		this.columnHideShowLayer = columnHideShowLayer;
		this.columnHeaderLayer = columnHeaderLayer;
		this.columnHeaderDataLayer = columnHeaderDataLayer;
		this.columnGroupModel = columnGroupModel;
		this.sortAvailableColumns = sortAvailableColumns;

		columnChooserDialog = new ColumnChooserDialog(shell, Messages.getString("ColumnChooser.availableColumns"), Messages.getString("ColumnChooser.selectedColumns")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void setDialogSettings(IDialogSettings dialogSettings) {
		columnChooserDialog.setDialogSettings(dialogSettings);
	}

	public void openDialog() {
		columnChooserDialog.create();

		hiddenColumnEntries = getHiddenColumnEntries();
		columnChooserDialog.populateAvailableTree(hiddenColumnEntries, columnGroupModel);

		visibleColumnsEntries = ColumnChooserUtils.getVisibleColumnsEntries(columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer);
		columnChooserDialog.populateSelectedTree(visibleColumnsEntries, columnGroupModel);

		columnChooserDialog.expandAllLeaves();

		addListenersOnColumnChooserDialog();
		columnChooserDialog.open();
	}

	private void addListenersOnColumnChooserDialog() {

		columnChooserDialog.addListener(new ISelectionTreeListener() {

			public void itemsRemoved(List<ColumnEntry> removedItems) {
				ColumnChooserUtils.hideColumnEntries(removedItems, columnHideShowLayer);
				refreshColumnChooserDialog();
			}

			public void itemsSelected(List<ColumnEntry> addedItems) {
				ColumnChooserUtils.showColumnEntries(addedItems, columnHideShowLayer);
				refreshColumnChooserDialog();
				columnChooserDialog.setSelectionIncludingNested(ColumnChooserUtils.getColumnEntryIndexes(addedItems));
			}

			public void itemsMoved(MoveDirectionEnum direction, List<ColumnGroupEntry> movedColumnGroupEntries, List<ColumnEntry> movedColumnEntries, List<List<Integer>> fromPositions, List<Integer> toPositions) {
				moveItems(direction, movedColumnGroupEntries, movedColumnEntries, fromPositions, toPositions);
			}

			/**
			 * Fire appropriate commands depending on the events received from the column chooser dialog
			 * @param direction
			 * @param movedColumnGroupEntries
			 * @param movedColumnEntries
			 * @param fromPositions
			 * @param toPositions
			 */
			private void moveItems(MoveDirectionEnum direction, List<ColumnGroupEntry> movedColumnGroupEntries, List<ColumnEntry> movedColumnEntries, List<List<Integer>> fromPositions, List<Integer> toPositions) {

				for (int i = 0; i < fromPositions.size(); i++) {
					boolean columnGroupMoved = columnGroupMoved(fromPositions.get(i), movedColumnGroupEntries);
					boolean multipleColumnsMoved = fromPositions.get(i).size() > 1;

					ILayerCommand command = null;
					if (!columnGroupMoved && !multipleColumnsMoved) {
						int fromPosition = fromPositions.get(i).get(0).intValue();
						int toPosition = adjustToPosition(direction, toPositions.get(i).intValue());
						command = new ColumnReorderCommand(columnHideShowLayer, fromPosition, toPosition);
					} else if (columnGroupMoved && multipleColumnsMoved) {
						command = new ReorderColumnsAndGroupsCommand(columnHideShowLayer, fromPositions.get(i), adjustToPosition(direction, toPositions.get(i)));
					} else if (!columnGroupMoved && multipleColumnsMoved) {
						command = new MultiColumnReorderCommand(columnHideShowLayer, fromPositions.get(i), adjustToPosition(direction, toPositions.get(i)));
					} else if (columnGroupMoved && !multipleColumnsMoved) {
						command = new ReorderColumnGroupCommand(columnHideShowLayer, fromPositions.get(i).get(0), adjustToPosition(direction, toPositions.get(i)));
					}
					columnHideShowLayer.doCommand(command);
				}

				refreshColumnChooserDialog();
				columnChooserDialog.setSelectionIncludingNested(ColumnChooserUtils.getColumnEntryIndexes(movedColumnEntries));
			}

			private int adjustToPosition(MoveDirectionEnum direction, Integer toColumnPosition) {
				if (MoveDirectionEnum.DOWN == direction) {
					return toColumnPosition + 1;
				} else {
					return toColumnPosition;
				}
			}

			private boolean columnGroupMoved(List<Integer> fromPositions, List<ColumnGroupEntry> movedColumnGroupEntries) {
				for (ColumnGroupEntry columnGroupEntry : movedColumnGroupEntries) {
					if(fromPositions.contains(columnGroupEntry.getFirstElementPosition())) return true;
				}
				return false;
			}

			public void itemsCollapsed(ColumnGroupEntry columnGroupEntry) {
				int index = columnGroupEntry.getFirstElementIndex().intValue();
				int position = selectionLayer.getColumnPositionByIndex(index);
				selectionLayer.doCommand(new ColumnGroupExpandCollapseCommand(selectionLayer, position));
			}

			public void itemsExpanded(ColumnGroupEntry columnGroupEntry) {
				int index = columnGroupEntry.getFirstElementIndex().intValue();
				int position = selectionLayer.getColumnPositionByIndex(index);
				selectionLayer.doCommand(new ColumnGroupExpandCollapseCommand(selectionLayer, position));
			}
		});
	}

	private void refreshColumnChooserDialog() {
		hiddenColumnEntries = getHiddenColumnEntries();
		visibleColumnsEntries = ColumnChooserUtils.getVisibleColumnsEntries(columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer);

		columnChooserDialog.removeAllLeaves();

		columnChooserDialog.populateSelectedTree(visibleColumnsEntries, columnGroupModel);
		columnChooserDialog.populateAvailableTree(hiddenColumnEntries, columnGroupModel);
		columnChooserDialog.expandAllLeaves();
	}

	protected List<ColumnEntry> getHiddenColumnEntries() {
		List<ColumnEntry> columnEntries = ColumnChooserUtils.getHiddenColumnEntries(columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer);
		if (sortAvailableColumns) {
			Collections.sort(columnEntries, COLUMN_ENTRY_LABEL_COMPARATOR);
		}
		return columnEntries;
	}

}
