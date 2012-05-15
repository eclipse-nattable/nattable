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

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 * The default location for the ControlDecoration is the top right of the editor. If the editor is located
 * such that the ControlDecoration would not be visible here (i.e. cell is at/extends beyond the right edge of
 * the NatTable) then the decoration is placed at the top left of the editor.
 *
 * The location can be overridden, in which case the above behaviour does not get used.
 */
public class ControlDecorationProvider
{
	private final String fieldDecorationId;
	private boolean errorDecorationEnabled;
	private ControlDecoration errorDecoration;
	private String errorDecorationText;
	private int decorationPositionOverride;
	
	public ControlDecorationProvider() {
		this(FieldDecorationRegistry.DEC_ERROR);
	}
	
	/**
	 * @param fieldDecorationId see #FieldDecorationRegistry
	 */
	public ControlDecorationProvider(String fieldDecorationId) {
		this.fieldDecorationId = fieldDecorationId;
		this.decorationPositionOverride = SWT.DEFAULT;
	}
	
	public void setErrorDecorationEnabled(boolean enabled) {
		errorDecorationEnabled = enabled;
	}
	
	public void setErrorDecorationText(String errorText) {
		errorDecorationText = errorText;
		if (errorDecoration != null) {
			errorDecoration.setDescriptionText(errorText);
		}
	}
	
	public void showErrorDecorationHover(String errorText) {
		if (errorDecoration != null) {
			errorDecoration.show();
			errorDecoration.showHoverText(errorText);
		}
	}
	
	public void setDecorationPositionOverride(int decorationPositionOverride) {
		this.decorationPositionOverride = decorationPositionOverride;
	}

	public void showDecoration() {
		if (errorDecoration != null) {
			errorDecoration.show();
		}
	}
	
	public void hideDecoration() {
		if (errorDecoration != null) {
			errorDecoration.hide();
		}
	}
	
	public void dispose() {
		if (errorDecoration != null) {
			errorDecoration.hide();
			errorDecoration.dispose();
			errorDecoration = null;
		}
	}

	public void createErrorDecorationIfRequired(final Control controlToDecorate) {
		
		if (errorDecorationEnabled) {
			
			final Image errorImage = FieldDecorationRegistry.getDefault().getFieldDecoration(fieldDecorationId).getImage();
			if (decorationPositionOverride == SWT.DEFAULT) {
				
				controlToDecorate.addPaintListener(new PaintListener() { // Using a PaintListener as bounds are only set AFTER activateCell()
					
					public void paintControl(PaintEvent e) {
						
						controlToDecorate.removePaintListener(this);
						int position = SWT.TOP;
						final Rectangle textBounds = controlToDecorate.getBounds();
						final Rectangle parentClientArea = controlToDecorate.getParent().getClientArea();
						if ((parentClientArea.x + parentClientArea.width) > (textBounds.x + textBounds.width + errorImage.getBounds().width)) {
							position |= SWT.RIGHT;
						} else {
							position |= SWT.LEFT;
						}
						errorDecoration = newControlDecoration(controlToDecorate, errorImage, position);
					}
				});
			} else {
				errorDecoration = newControlDecoration(controlToDecorate, errorImage, decorationPositionOverride);
			}
		}
	}

	private ControlDecoration newControlDecoration(Control controlToDecorate, Image errorImage, int position) {
		final ControlDecoration errorDecoration = new ControlDecoration(controlToDecorate, position);
		errorDecoration.setImage(errorImage);
		errorDecoration.setDescriptionText(errorDecorationText);
		errorDecoration.hide();
		return errorDecoration;
	}
	
}
