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
package org.eclipse.nebula.widgets.nattable.search.action;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.search.CellValueAsStringComparator;
import org.eclipse.nebula.widgets.nattable.search.gui.SearchDialog;
import org.eclipse.nebula.widgets.nattable.search.strategy.GridSearchStrategy;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.events.KeyEvent;

public class SearchAction implements IKeyAction {

	private SearchDialog searchDialog;
	
	public void run(NatTable natTable, KeyEvent event) {
		if (searchDialog == null) {
			searchDialog =  SearchDialog.createDialog(natTable.getShell(), natTable);
		}
		GridSearchStrategy searchStrategy = new GridSearchStrategy(natTable.getConfigRegistry(), true);
		searchDialog.setSearchStrategy(searchStrategy, new CellValueAsStringComparator<String>());
		searchDialog.open();
	}
}
