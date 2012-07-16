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
package org.eclipse.nebula.widgets.nattable.edit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Controls edit behavior when a cell is being edited in place where it is
 * normally displayed in the NatTable. An edit control will be rendered on top
 * of the of the cell to be edited and will handle the edit behavior.
 */
public class InlineCellEditController {

	private static Map<ILayer, ILayerListener> layerListenerMap = new HashMap<ILayer, ILayerListener>();

	public static boolean editCellInline(ILayerCell cell, Character initialEditValue, final Composite parent, IConfigRegistry configRegistry) {
		try {
			ActiveCellEditor.commit();

			final List<String> configLabels = cell.getConfigLabels().getLabels();
			Rectangle cellBounds = cell.getBounds();

			ILayer layer = cell.getLayer();

			int columnPosition = layer.getColumnPositionByX(cellBounds.x);
			int rowPosition = layer.getRowPositionByY(cellBounds.y);

			boolean editable = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, DisplayMode.EDIT, configLabels).isEditable(cell, configRegistry);
			if (!editable) {
				return false;
			}

			ICellEditor cellEditor = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITOR, DisplayMode.EDIT, configLabels);
			ICellEditHandler editHandler = new SingleEditHandler(
					cellEditor,
					layer,
					columnPosition,
					rowPosition);

			final Rectangle editorBounds = layer.getLayerPainter().adjustCellBounds(columnPosition, rowPosition, new Rectangle(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height));

			Object originalCanonicalValue = cell.getDataValue();

			ActiveCellEditor.activate(cellEditor, parent, originalCanonicalValue, initialEditValue, EditModeEnum.INLINE, editHandler, cell, configRegistry);
			final Control editorControl = ActiveCellEditor.getControl();

			if (editorControl != null) {
				editorControl.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						if (!ActiveCellEditor.commit()) {
							if (e.widget instanceof Control && !e.widget.isDisposed()) {
								((Control)e.widget).forceFocus();
							}
						}
						else {
							parent.forceFocus();
						}
					}
				});

				editorControl.setBounds(editorBounds);
				ILayerListener layerListener = layerListenerMap.get(layer);
				if (layerListener == null) {
					layerListener = new InlineCellEditLayerListener(layer);
					layerListenerMap.put(layer, layerListener);

					layer.addLayerListener(layerListener);
				}
			}
		} catch (Exception e) {
			if(cell == null){
				System.err.println("Cell being edited is no longer available. Character: " + initialEditValue); //$NON-NLS-1$
			} else {
				System.err.println("Error while editing cell (inline): Cell: " + cell + "; Character: " + initialEditValue); //$NON-NLS-1$ //$NON-NLS-2$
			}
			e.printStackTrace();
		}

		return true;
	}

	public static void dispose() {
		layerListenerMap.clear();
	}

	static class InlineCellEditLayerListener implements ILayerListener {

		private final ILayer layer;

		InlineCellEditLayerListener(ILayer layer) {
			this.layer = layer;
		}

		public void handleLayerEvent(ILayerEvent event) {
			
			if (event instanceof CellSelectionEvent && ActiveCellEditor.isValid()) {
				
				int selectedCellColumnPosition = 0;
				int selectedCellRowPosition = 0;
				
				CellSelectionEvent csEvent = (CellSelectionEvent) event;
				selectedCellColumnPosition = csEvent.getColumnPosition();
				selectedCellRowPosition = csEvent.getRowPosition();
				
				int editorColumnPosition = ActiveCellEditor.getColumnPosition();
				int editorRowPosition = ActiveCellEditor.getRowPosition();
				int editorColumnIndex = ActiveCellEditor.getColumnIndex();
				int editorRowIndex = ActiveCellEditor.getRowIndex();
				
				int selectedColumnIndex = layer.getColumnIndexByPosition(selectedCellColumnPosition);
				int selectedRowIndex = layer.getRowIndexByPosition(selectedCellRowPosition);

				Control editorControl = ActiveCellEditor.getControl();

				if (selectedColumnIndex != editorColumnIndex || selectedRowIndex != editorRowIndex) {
					ActiveCellEditor.close();
				}
				else if (editorControl != null && !editorControl.isDisposed()) {
					Rectangle cellBounds = layer.getBoundsByPosition(editorColumnPosition, editorRowPosition);
					Rectangle adjustedCellBounds = layer.getLayerPainter().adjustCellBounds(editorColumnPosition, editorRowPosition, cellBounds);
					editorControl.setBounds(adjustedCellBounds);
				}
			}
		}

	}

}
