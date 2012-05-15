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
package org.eclipse.nebula.widgets.nattable.edit.editor;


import org.eclipse.nebula.widgets.nattable.edit.ActiveCellEditor;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CheckBoxCellEditor extends AbstractCellEditor {

	private boolean checked;
	private Canvas canvas;

	/**
	 * As soon as the editor is activated, flip the current data value and commit it.<br/>
	 * The repaint will pick up the new value and flip the image.<br/>
	 * This is only done if the mouse click is done within the rectangle of the painted 
	 * checkbox image.
	 */
	@Override
	protected Control activateCell(Composite parent, Object originalCanonicalValue, Character initialEditValue) {
		setCanonicalValue(originalCanonicalValue);

		checked = !checked;

		canvas = new Canvas(parent, SWT.NONE);

		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				checked = !checked;
				canvas.redraw();
			}
		});

		commit(MoveDirectionEnum.NONE, false);

		if (editMode == EditModeEnum.INLINE) {
			// Close editor so will react to subsequent clicks on the cell
			if (canvas != null && !canvas.isDisposed()) {
				canvas.getDisplay().asyncExec(new Runnable() {
					public void run()
					{
						ActiveCellEditor.close();
					}
				});
			}
		}
		
		return canvas;
	}

	public Object getCanonicalValue() {
		return getDataTypeConverter().displayToCanonicalValue(layerCell, configRegistry, Boolean.valueOf(checked));
	}

	public void setCanonicalValue(Object canonicalValue) {
		if (canonicalValue == null) {
			checked = false;
		} else {
			if (canonicalValue instanceof Boolean) {
				checked = ((Boolean)canonicalValue).booleanValue();
			}
			Object convertedValue = null;
			if (getDataTypeConverter() != null) {
				convertedValue = getDataTypeConverter().canonicalToDisplayValue(layerCell, configRegistry, canonicalValue);
			}
			if (convertedValue instanceof String) {
				checked = Boolean.valueOf((String) convertedValue).booleanValue();
			} else if (convertedValue instanceof Boolean) {
				checked = ((Boolean)convertedValue).booleanValue();
			} else {
				checked = false;
			}
		}
	}

	@Override
	public void close() {
		super.close();

		if (canvas != null && !canvas.isDisposed()) {
			canvas.dispose();
		}
	}

}
