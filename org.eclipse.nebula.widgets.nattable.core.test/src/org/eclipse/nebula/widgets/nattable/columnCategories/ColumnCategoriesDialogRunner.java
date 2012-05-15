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
package org.eclipse.nebula.widgets.nattable.columnCategories;

import java.util.List;


import org.eclipse.nebula.widgets.nattable.columnCategories.IColumnCategoriesDialogListener;
import org.eclipse.nebula.widgets.nattable.columnCategories.gui.ColumnCategoriesDialog;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.test.fixture.ColumnCategoriesModelFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.ColumnEntriesFixture;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.widgets.Shell;

public class ColumnCategoriesDialogRunner {

	public static void main(String[] args) {
		ColumnCategoriesDialog dialog = new ColumnCategoriesDialog(
				new Shell(),
				new ColumnCategoriesModelFixture(),
				ColumnEntriesFixture.getEntriesWithEvenIndexes(),
				ColumnEntriesFixture.getEntriesWithOddIndexes());

		dialog.addListener(new Listener());
		dialog.open();
	}
}

class Listener implements IColumnCategoriesDialogListener {

	public void itemsRemoved(List<Integer> removedColumnPositions) {
		System.out.println("Removed positions: " + ObjectUtils.toString(removedColumnPositions));
	}

	public void itemsSelected(List<Integer> addedColumnIndexes) {
		System.out.println("Added indexes: " + ObjectUtils.toString(addedColumnIndexes));
	}

	public void itemsMoved(MoveDirectionEnum direction, List<Integer> toPositions) {
		System.out.println("Moved: " + direction + ", Positions: " + ObjectUtils.toString(toPositions));
	}
}
