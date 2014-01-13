/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit.editor;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.config.RenderErrorHandling;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * {@link ICellEditor} implementation that wraps a SWT {@link Text} control to support
 * text editing. This is also the default editor in NatTable if you didn't configure
 * something else.
 */
public class TextCellEditor extends AbstractCellEditor {

	/**
	 * The Text control which is the editor wrapped by this TextCellEditor.
	 */
	private Text text = null;
	
	/**
	 * Flag to configure if the wrapped text editor control is editable or not.
	 */
	private boolean editable = true;
	
	/**
	 * Flag to configure whether the editor should commit and move the selection
	 * in the corresponding way if the up or down key is pressed.
	 */
	private final boolean commitOnUpDown;
	
	/**
	 * Flag to configure whether the selection should move after a value was
	 * committed after pressing enter.
	 */
	private final boolean moveSelectionOnEnter;
	
	/**
	 * The selection mode that should be used on activating the wrapped text control.
	 * By default the behaviour is to set the selection at the end of the containing text
	 * if the text editor control is activated with an initial value. If it is activated
	 * only specifying the original canonical value, the default behaviour is to select
	 * the whole text contained in the text editor control.
	 * 
	 * <p>You can override this default behaviour by setting an {@link EditorSelectionEnum}
	 * explicitly. With this you are able e.g. to set the selection at the beginning of 
	 * the contained text, so writing in the text control will result in prefixing.
	 * 
	 * <p>Note that on overriding the behaviour, you override both activation cases.
	 */
	private EditorSelectionEnum selectionMode;

	/**
	 * The {@link ControlDecorationProvider} responsible for adding a {@link ControlDecoration}
	 * to the wrapped editor control. Can be configured via convenience methods of this TextCellEditor.
	 */
	protected final ControlDecorationProvider decorationProvider = new ControlDecorationProvider();
	
	/**
	 * The {@link IEditErrorHandler} that is used for showing conversion errors on typing into
	 * this editor. By default this is the {@link RenderErrorHandling} which will render the
	 * content in the editor red to indicate a conversion error.
	 */
	private IEditErrorHandler inputConversionErrorHandler = new RenderErrorHandling(decorationProvider);

	/**
	 * The {@link IEditErrorHandler} that is used for showing validation errors on typing into
	 * this editor. By default this is the {@link RenderErrorHandling} which will render the
	 * content in the editor red to indicate a validation error.
	 */
	private IEditErrorHandler inputValidationErrorHandler = new RenderErrorHandling(decorationProvider);

	/**
	 * Flag to determine whether this editor should try to commit and close on pressing the enter key.
	 * The default is of course <code>true</code>, but for a multi line text editor, the enter key
	 * should be treated as inserting a new line instead of committing. 
	 */
	protected boolean commitOnEnter = true;
	
	/**
	 * Creates the default TextCellEditor that does not commit on pressing the up/down arrow keys
	 * and will not move the selection on committing a value by pressing enter.
	 */
	public TextCellEditor() {
	    this(false);
	}
	
	/**
	 * Creates a TextCellEditor that will not move the selection on committing a value by pressing enter.
	 * @param commitOnUpDown Flag to configure whether the editor should commit 
	 * 			and move the selection in the corresponding way if the up or down key is pressed.
	 */
	public TextCellEditor(boolean commitOnUpDown) {
        this(commitOnUpDown, false);
	}
	
	/**
	 * Creates a TextCellEditor.
	 * @param commitOnUpDown Flag to configure whether the editor should commit 
	 * 			and move the selection in the corresponding way if the up or down key is pressed.
	 * @param moveSelectionOnEnter Flag to configure whether the selection should move after a value was
	 * 			committed after pressing enter.
	 */
	public TextCellEditor(boolean commitOnUpDown, boolean moveSelectionOnEnter) {
        this.commitOnUpDown = commitOnUpDown;
        this.moveSelectionOnEnter = moveSelectionOnEnter;
	}

	@Override
	protected Control activateCell(final Composite parent, Object originalCanonicalValue) {
		this.text = createEditorControl(parent);
		
		// If the originalCanonicalValue is a Character it is possible the editor is activated by keypress
		if (originalCanonicalValue instanceof Character) {
			this.text.setText(originalCanonicalValue.toString());
			selectText(this.selectionMode != null ? this.selectionMode : EditorSelectionEnum.END);
		} 
		//if there is no initial value, handle the original canonical value to transfer it to the text control
		else {
			setCanonicalValue(originalCanonicalValue);
			selectText(this.selectionMode != null ? this.selectionMode : EditorSelectionEnum.ALL);
		}

		if (!isEditable()) {
			this.text.setEditable(false);
		}

		//show an error decoration if this is enabled
		this.decorationProvider.createErrorDecorationIfRequired(this.text);
		
		//if the input error handlers are of type RenderErrorHandler (default) than
		//we also check for a possible configured error styling in the configuration
		//Note: this is currently only implemented in here, as the TextCellEditor is
		//		the only editor that supports just in time conversion/validation
		if (this.inputConversionErrorHandler instanceof RenderErrorHandling) {
			IStyle conversionErrorStyle = this.configRegistry.getConfigAttribute(
					EditConfigAttributes.CONVERSION_ERROR_STYLE, 
					DisplayMode.EDIT, 
					this.labelStack.getLabels());
			
			((RenderErrorHandling) this.inputConversionErrorHandler).setErrorStyle(conversionErrorStyle);
		}

		if (this.inputValidationErrorHandler instanceof RenderErrorHandling) {
			IStyle validationErrorStyle = this.configRegistry.getConfigAttribute(
					EditConfigAttributes.VALIDATION_ERROR_STYLE, 
					DisplayMode.EDIT, 
					this.labelStack.getLabels());
			
			((RenderErrorHandling) this.inputValidationErrorHandler).setErrorStyle(validationErrorStyle);
		}
		
		this.text.forceFocus(); 
		
		return this.text;
	}
	
	@Override
	public String getEditorValue() {
		return this.text.getText();
	}

	@Override
	public void setEditorValue(Object value) {
		this.text.setText(value != null && value.toString().length() > 0 ? value.toString() : ""); //$NON-NLS-1$
	}
	
	@Override
	public Text getEditorControl() {
		return this.text;
	}

	@Override
	public Text createEditorControl(Composite parent) {
		int style = HorizontalAlignmentEnum.getSWTStyle(this.cellStyle);
		if (this.editMode == EditModeEnum.DIALOG) {
			style = style | SWT.BORDER;
		}
		return createEditorControl(parent, style);
	}
	
	/**
	 * Creates the editor control that is wrapped by this ICellEditor.
	 * Will use the style configurations in ConfigRegistry for styling the control.
	 * @param parent The Composite that will be the parent of the new editor control. 
	 * 			Can not be <code>null</code> 
	 * @param style The SWT style of the text control to create.
	 * @return The created editor control that is wrapped by this ICellEditor.
	 */
	protected Text createEditorControl(final Composite parent, int style) {
		//create the Text control based on the specified style
		final Text textControl = new Text(parent, style);
		
		//set style information configured in the associated cell style
		textControl.setBackground(this.cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		textControl.setForeground(this.cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
		textControl.setFont(this.cellStyle.getAttributeValue(CellStyleAttributes.FONT));
		
		textControl.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_IBEAM));
		
		//add a key listener that will commit or close the editor for special key strokes
		//and executes conversion/validation on input to the editor
		textControl.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent event) {
				if (commitOnEnter && 
						(event.keyCode == SWT.CR
							|| event.keyCode == SWT.KEYPAD_CR)) {
					
					boolean commit = (event.stateMask == SWT.ALT) ? false : true;
					MoveDirectionEnum move = MoveDirectionEnum.NONE;
					if (moveSelectionOnEnter && editMode == EditModeEnum.INLINE) {
						if (event.stateMask == 0) {
							move = MoveDirectionEnum.DOWN;
						} else if (event.stateMask == SWT.SHIFT) {
							move = MoveDirectionEnum.UP;
						}
					}
					
					if (commit)
						commit(move);
					
					if (editMode == EditModeEnum.DIALOG) {
						parent.forceFocus();
					}
				} 
				else if (event.keyCode == SWT.ESC && event.stateMask == 0){
					close();
				}
				else if (commitOnUpDown && editMode == EditModeEnum.INLINE) {
                    if (event.keyCode == SWT.ARROW_UP) {
                        commit(MoveDirectionEnum.UP);
                    } else if (event.keyCode == SWT.ARROW_DOWN) {
                        commit(MoveDirectionEnum.DOWN);
                    }
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				try {
					//always do the conversion
					Object canonicalValue = getCanonicalValue(inputConversionErrorHandler); 
					//and always do the validation
					//even if for commiting the validation should be skipped, on editing
					//a validation failure should be made visible
					//otherwise there would be no need for validation!
					validateCanonicalValue(canonicalValue, inputValidationErrorHandler);
				}
				catch (Exception ex) {
					//do nothing as exceptions caused by conversion or validation are handled already
					//we just need this catch block for stopping the process if conversion failed with
					//an exception
				}
			}
		});
		
		return textControl;
	}
	
	@Override
	public void close() {
		super.close();
		
		this.decorationProvider.dispose();
	}
	
	/**
	 * @return <code>true</code> if the wrapped Text control is editable,
	 * 			<code>false</code> if not.
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * 
	 * @param editable <code>true</code> if the wrapped Text control should be editable,
	 * 			<code>false</code> if not.
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	/**
	 * Returns the current configured selection mode that is used on activating the wrapped
	 * text editor control. By default this is <code>null</code> which causes the following
	 * default behaviour. If the text editor control is activated with an initial value then
	 * the selection is set at the end of the containing text. If it is activated
	 * only specifying the original canonical value, the default behaviour is to select
	 * the whole text contained in the text editor control.
	 * @return The current configured selection mode, <code>null</code> for default behaviour.
	 */
	public final EditorSelectionEnum getSelectionMode() {
		return selectionMode;
	}
	
	/**
	 * Set the selection mode that should be used on the content of the wrapped text editor control
	 * when it gets activated. By setting a value explicitly you configure the selection mode for
	 * both cases, activating the wrapped text editor control with and without an initial value.
	 * Setting this value to <code>null</code> will reactivate the default behaviour like described
	 * here {@link TextCellEditor#getSelectionMode()}.
	 * @param selectionMode The selection mode that should be used on the content of the
	 * 			wrapped text editor control when it gets activated.
	 */
	public final void setSelectionMode(EditorSelectionEnum selectionMode) {
		this.selectionMode = selectionMode;
	}

	/**
	 * Will set the selection to the wrapped text control regarding the configured
	 * {@link EditorSelectionEnum}. 
	 * 
	 * <p>This method is called 
	 * 
	 * @see Text#setSelection(int, int)
	 */
	private void selectText(EditorSelectionEnum selectionMode) {
		int textLength = this.text.getText().length();
		if (textLength > 0) {
			if (selectionMode == EditorSelectionEnum.ALL) {
				this.text.setSelection(0, textLength);
			} else if (selectionMode == EditorSelectionEnum.END) {
				this.text.setSelection(textLength, textLength);
			} else if (selectionMode == EditorSelectionEnum.START) {
				this.text.setSelection(0);
			}
		}
	}
	
	/**
	 * @return The {@link ControlDecorationProvider} responsible for adding a {@link ControlDecoration}
	 * 			to the wrapped editor control.
	 */
	public ControlDecorationProvider getDecorationProvider() {
		return this.decorationProvider;
	}

	/**
	 * Enables/disables the error decoration for the wrapped text control.
	 * @param enabled <code>true</code> if an error decoration should be added to 
	 * 			the wrapped text control, <code>false</code> if not.
	 */
	public void setErrorDecorationEnabled(boolean enabled) {
		this.decorationProvider.setErrorDecorationEnabled(enabled);
	}
	
	/**
	 * Set the error description text that will be shown in the decoration hover.
	 * @param errorText The text to be shown as a description for the decoration, or
	 *            <code>null</code> if there should be no description.
	 *            
	 * @see ControlDecoration#setDescriptionText(String)
	 */
	public void setErrorDecorationText(String errorText) {
		this.decorationProvider.setErrorDecorationText(errorText);
	}
	
	/**
	 * Force the error decoration hover to show immediately.
	 * @param customErrorText  The text to show in the hover popup.
	 * 
	 * @see ControlDecoration#show()
	 * @see ControlDecoration#showHoverText(String)
	 */
	public void showErrorDecorationHover(String customErrorText) {
		this.decorationProvider.showErrorDecorationHover(customErrorText);
	}
	
	/**
	 * Set the id of the {@link FieldDecoration} to be used by the local {@link ControlDecorationProvider}.
	 * @param fieldDecorationId The String to determine the {@link FieldDecoration} to use by 
	 * 			the {@link ControlDecoration} that is provided by this {@link ControlDecorationProvider}.
	 * 
	 * @see FieldDecorationRegistry#getFieldDecoration(String)
	 */
	public void setFieldDecorationId(String fieldDecorationId) {
		this.decorationProvider.setFieldDecorationId(fieldDecorationId);
	}
	
	/**
	 * Set the position of the control decoration relative to the control.
	 * It should include style bits describing both the vertical and horizontal orientation.
	 * @param decorationPositionOverride
	 *            bit-wise or of position constants (<code>SWT.TOP</code>,
	 *            <code>SWT.BOTTOM</code>, <code>SWT.LEFT</code>,
	 *            <code>SWT.RIGHT</code>, and <code>SWT.CENTER</code>).
	 *            
	 * @see ControlDecoration#ControlDecoration(Control, int)
	 */
	public void setDecorationPositionOverride(int decorationPositionOverride) {
		this.decorationProvider.setDecorationPositionOverride(decorationPositionOverride);
	}
	
	/**
	 * @return The {@link IEditErrorHandler} that is used for showing conversion errors on typing into
	 * 			this editor. By default this is the {@link RenderErrorHandling} which will render the
	 * 			content in the editor red to indicate a conversion error.
	 */
	public IEditErrorHandler getInputConversionErrorHandler() {
		return this.inputConversionErrorHandler;
	}

	/**
	 * @param inputConversionErrorHandler The {@link IEditErrorHandler} that is should be used for showing 
	 * 			conversion errors on typing into this editor.
	 */
	public void setInputConversionErrorHandler(IEditErrorHandler inputConversionErrorHandler) {
		this.inputConversionErrorHandler = inputConversionErrorHandler;
	}

	/**
	 * @return The {@link IEditErrorHandler} that is used for showing validation errors on typing into
	 * 			this editor. By default this is the {@link RenderErrorHandling} which will render the
	 * 			content in the editor red to indicate a validation error.
	 */
	public IEditErrorHandler getInputValidationErrorHandler() {
		return this.inputValidationErrorHandler;
	}

	/**
	 * @param inputValidationErrorHandler The {@link IEditErrorHandler} that is should used for showing 
	 * 			validation errors on typing into this editor.
	 */
	public void setInputValidationErrorHandler(IEditErrorHandler inputValidationErrorHandler) {
		this.inputValidationErrorHandler = inputValidationErrorHandler;
	}
}
