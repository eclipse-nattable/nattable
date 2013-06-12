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
package org.eclipse.nebula.widgets.nattable.edit;

import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;

/**
 * This class is used to store the instance of the current active cell editor
 * for inline editing. As the {@link ICellEditor} does not know about the NatTable
 * instance it is activated in, and NatTable does not have some kind of global context
 * to register this information to, it is currently not possible to solve this in
 * another way. 
 * <p>
 * On activating a cell editor for inline editing, the activated cell editor will
 * be registered here. If the editor is closed, it needs to unregister itself.
 * </p>
 * <p>
 * Any position in code that needs to be aware of an open cell editor can request it
 * from here. This is especially necessary for automatic closing of cell editors on
 * other actions, e.g. trying to scroll with an activated editor which should lead
 * to close the editor first.
 * </p>
 * <p>
 * This can cause serious side effects if you are trying to create a custom editor
 * that is based on NatTable as editor control. This needs to be solved with some
 * kind of NatTable context where such instance can be registered (e.g. Dependency
 * Injection) and therefore will be addressed in a future architecture design.
 * </p>
 * 
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=392535">bug 392535</a>
 * 
 * @author Dirk Fauth
 */
public class ActiveCellEditorRegistry {

	/**
	 * The current active {@link ICellEditor} or <code>null</code> if
	 * there is none.
	 */
	private static ICellEditor activeCellEditor;
	
	/**
	 * Register the given {@link ICellEditor} as the current active one.
	 * @param editor The editor that should be registered as the current 
	 * 			active one.
	 */
	public static void registerActiveCellEditor(ICellEditor editor) {
		activeCellEditor = editor;
	}
	
	/**
	 * Will unregister the current active cell editor if there is one.
	 */
	public static void unregisterActiveCellEditor() {
		activeCellEditor = null;
	}
	
	/**
	 * 
	 * @return The current active {@link ICellEditor} or <code>null</code> if
	 * 			there is none.
	 */
	public static ICellEditor getActiveCellEditor() {
		return activeCellEditor;
	}
}
