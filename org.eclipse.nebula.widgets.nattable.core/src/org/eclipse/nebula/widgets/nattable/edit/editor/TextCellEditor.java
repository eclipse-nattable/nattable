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


import org.eclipse.nebula.widgets.nattable.edit.config.RenderErrorHandling;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

public class TextCellEditor extends AbstractCellEditor {

	private EditorSelectionEnum selectionMode = EditorSelectionEnum.ALL;

	private Text text = null;
	private boolean editable = true;
	private final boolean commitOnUpOrDownCursorKeyPress;
	protected final ControlDecorationProvider decorationProvider = new ControlDecorationProvider();
	
	private IEditErrorHandler inputConversionErrorHandler = new RenderErrorHandling(decorationProvider);
	private IEditErrorHandler inputValidationErrorHandler = new RenderErrorHandling(decorationProvider);

	
	public TextCellEditor() {
	    this(false);
	}
	
	public TextCellEditor(boolean commitOnUpOrDownCursorKeyPress) {
        this.commitOnUpOrDownCursorKeyPress = commitOnUpOrDownCursorKeyPress;
	}
	
	public Text getTextControl() {
		return text;
	}
	
	public ControlDecorationProvider getDecorationProvider() {
		return decorationProvider;
	}
	
	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	public final void setSelectionMode(EditorSelectionEnum selectionMode) {
		this.selectionMode = selectionMode;
	}
	
	public final EditorSelectionEnum getSelectionMode() {
		return selectionMode;
	}

	@Override
	protected Control activateCell(final Composite parent, Object originalCanonicalValue, Character initialEditValue) {
		text = createTextControl(parent);
		
		text.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_IBEAM));
		
		// If we have an initial value, then 
		if (initialEditValue != null) {
			selectionMode = EditorSelectionEnum.END;
			text.setText(initialEditValue.toString());
			selectText();
		} else {
			setCanonicalValue(originalCanonicalValue);
		}

		if (!isEditable()) {
			text.setEditable(false);
		}

		text.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent event) {
				if ((event.keyCode == SWT.CR && event.stateMask == 0)
						|| (event.keyCode == SWT.KEYPAD_CR && event.stateMask == 0)) {
					commit(MoveDirectionEnum.NONE);
				} 
				else if (event.keyCode == SWT.ESC && event.stateMask == 0){
					close();
				}
				else if (commitOnUpOrDownCursorKeyPress && editMode == EditModeEnum.INLINE) {
				    
                    if (event.keyCode == SWT.ARROW_UP) {
                        commit(MoveDirectionEnum.UP);
                    } else if (event.keyCode == SWT.ARROW_DOWN) {
                        commit(MoveDirectionEnum.DOWN);
                    }
				}
			}
		});
		
		text.addTraverseListener(new TraverseListener() {
			
			public void keyTraversed(TraverseEvent event) {
				boolean committed = false;
				if (event.keyCode == SWT.TAB && event.stateMask == SWT.SHIFT) {
					committed = commit(MoveDirectionEnum.LEFT);
				} else if (event.keyCode == SWT.TAB && event.stateMask == 0) {
					committed = commit(MoveDirectionEnum.RIGHT);
				}
				if (!committed) {
					event.doit = false;
				}
			}
		});
		
		decorationProvider.createErrorDecorationIfRequired(text);
		
		text.forceFocus(); 
		
		return text;
	}

	private void selectText() {
		int textLength = text.getText().length();
		if (textLength > 0) {
			EditorSelectionEnum selectionMode = getSelectionMode();
			if (selectionMode == EditorSelectionEnum.ALL) {
				text.setSelection(0, textLength);
			} else if (selectionMode == EditorSelectionEnum.END) {
				text.setSelection(textLength, textLength);
			}
		}
	}

	public void setErrorDecorationEnabled(boolean enabled) {
		decorationProvider.setErrorDecorationEnabled(enabled);
	}
	
	public void setErrorDecorationText(String errorText) {
		decorationProvider.setErrorDecorationText(errorText);
	}
	
	/**
	 * Force the error decoration hover to show immediately
	 * @param customErrorText  the text to show in the hover popup
	 */
	public void showErrorDecorationHover(String customErrorText) {
		decorationProvider.showErrorDecorationHover(customErrorText);
	}
	
	public void setDecorationPositionOverride(int decorationPositionOverride) {
		decorationProvider.setDecorationPositionOverride(decorationPositionOverride);
	}
	
	protected Text createTextControl(Composite parent) {
		IStyle cellStyle = getCellStyle();
		final Text textControl = new Text(parent, HorizontalAlignmentEnum.getSWTStyle(cellStyle));
		textControl.setBackground(cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		textControl.setForeground(cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
		textControl.setFont(cellStyle.getAttributeValue(CellStyleAttributes.FONT));
		
		textControl.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				validateCanonicalValue(inputConversionErrorHandler, inputValidationErrorHandler);
			}
		});
		
		return textControl;
	}

	public Object getCanonicalValue() {
		return getDataTypeConverter().displayToCanonicalValue(layerCell, configRegistry, text.getText());
	}
	
	public void setCanonicalValue(Object canonicalValue) {
		String displayValue = (String) getDataTypeConverter().canonicalToDisplayValue(layerCell, configRegistry, canonicalValue);
		text.setText(displayValue != null && displayValue.length() > 0 ? displayValue.toString() : ""); //$NON-NLS-1$
		selectText();
	}
	
	@Override
	public void close() {
		super.close();
		
		if (text != null && !text.isDisposed()) {
			text.dispose();
		}
		
		decorationProvider.dispose();
	}
	
	final protected Text getTextWidget() {
		return text;
	}
	
	public IEditErrorHandler getInputConversionErrorHandler() {
		return inputConversionErrorHandler;
	}

	public void setInputConversionErrorHandler(
			IEditErrorHandler inputConversionErrorHandler) {
		this.inputConversionErrorHandler = inputConversionErrorHandler;
	}

	public IEditErrorHandler getInputValidationErrorHandler() {
		return inputValidationErrorHandler;
	}

	public void setInputValidationErrorHandler(
			IEditErrorHandler inputValidationErrorHandler) {
		this.inputValidationErrorHandler = inputValidationErrorHandler;
	}
}
