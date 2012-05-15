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
package org.eclipse.nebula.widgets.nattable.columnChooser.command;


import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnChooser;
import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

public class DisplayColumnChooserCommandHandler extends AbstractLayerCommandHandler<DisplayColumnChooserCommand> {

	private final ColumnHideShowLayer columnHideShowLayer;
	private final ColumnGroupHeaderLayer columnGroupHeaderLayer;
	private final ColumnGroupModel columnGroupModel;
	private final SelectionLayer selectionLayer;
	private final DataLayer columnHeaderDataLayer;
	private final ColumnHeaderLayer columnHeaderLayer;
	private final boolean sortAvailableColumns;
	private IDialogSettings dialogSettings;

	public DisplayColumnChooserCommandHandler(
			SelectionLayer selectionLayer,
			ColumnHideShowLayer columnHideShowLayer,
			ColumnHeaderLayer columnHeaderLayer,
			DataLayer columnHeaderDataLayer,
			ColumnGroupHeaderLayer cgHeader,
			ColumnGroupModel columnGroupModel) {

		this(selectionLayer, columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer, cgHeader, columnGroupModel, false);
	}

	public DisplayColumnChooserCommandHandler(
			SelectionLayer selectionLayer,
			ColumnHideShowLayer columnHideShowLayer,
			ColumnHeaderLayer columnHeaderLayer,
			DataLayer columnHeaderDataLayer,
			ColumnGroupHeaderLayer cgHeader,
			ColumnGroupModel columnGroupModel,
			boolean sortAvalableColumns) {
		
		this.selectionLayer = selectionLayer;
		this.columnHideShowLayer = columnHideShowLayer;
		this.columnHeaderLayer = columnHeaderLayer;
		this.columnHeaderDataLayer = columnHeaderDataLayer;
		this.columnGroupHeaderLayer = cgHeader;
		this.columnGroupModel = columnGroupModel;
		this.sortAvailableColumns = sortAvalableColumns;
	}
	
	@Override
	public boolean doCommand(DisplayColumnChooserCommand command) {
		ColumnChooser columnChooser = new ColumnChooser(
				command.getNatTable().getShell(),
				selectionLayer,
				columnHideShowLayer,
				columnHeaderLayer,
				columnHeaderDataLayer,
				columnGroupHeaderLayer,
				columnGroupModel,
				sortAvailableColumns);

		columnChooser.setDialogSettings(dialogSettings);
		columnChooser.openDialog();
		return true;
	}

	public void setDialogSettings(IDialogSettings dialogSettings) {
		this.dialogSettings = dialogSettings;
	}
	
	public Class<DisplayColumnChooserCommand> getCommandClass() {
		return DisplayColumnChooserCommand.class;
	}

}
