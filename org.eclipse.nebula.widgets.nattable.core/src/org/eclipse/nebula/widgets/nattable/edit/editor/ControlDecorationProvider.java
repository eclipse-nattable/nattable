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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 * This class adds support for adding a {@link ControlDecoration} to the editor
 * controls of a {@link ICellEditor}. It is currently only used by the
 * {@link TextCellEditor}.
 * <p>
 * The default location for the {@link ControlDecoration} is the top right of
 * the editor. If the editor is located such that the {@link ControlDecoration}
 * would not be visible here (i.e. cell is at/extends beyond the right edge of
 * the NatTable) then the decoration is placed at the top left of the editor.
 * <p>
 * The location can be overridden, in which case the above behaviour does not
 * get used.
 */
public class ControlDecorationProvider {
    /**
     * The String to determine the {@link FieldDecoration} to use by the
     * {@link ControlDecoration} that is provided by this
     * {@link ControlDecorationProvider}.
     */
    private String fieldDecorationId;
    /**
     * Flag to determine whether this provider is enabled to add
     * {@link ControlDecoration}s or not.
     */
    private boolean errorDecorationEnabled;
    /**
     * The created {@link ControlDecoration} if this provider created one.
     */
    private ControlDecoration errorDecoration;
    /**
     * The text to be shown as a description for the decoration, or
     * <code>null</code> if none has been set.
     */
    private String errorDecorationText;
    /**
     * The position configuration where the decoration should be rendered
     * relative to the control that should be decorated.
     */
    private int decorationPositionOverride = SWT.DEFAULT;

    /**
     * Create a default {@link ControlDecorationProvider} for handling error
     * decorations.
     */
    public ControlDecorationProvider() {
        this(FieldDecorationRegistry.DEC_ERROR);
    }

    /**
     * @param fieldDecorationId
     *            The field decoration to use by this provider.
     * @see FieldDecorationRegistry
     */
    public ControlDecorationProvider(String fieldDecorationId) {
        this.fieldDecorationId = fieldDecorationId;
    }

    /**
     * Enables/disables the error decoration.
     *
     * @param enabled
     *            <code>true</code> if a decoration should be added,
     *            <code>false</code> if not.
     */
    public void setErrorDecorationEnabled(boolean enabled) {
        this.errorDecorationEnabled = enabled;
    }

    /**
     * @param errorText
     *            the text to be shown as a description for the decoration, or
     *            <code>null</code> if none has been set.
     * @see ControlDecoration#setDescriptionText(String)
     */
    public void setErrorDecorationText(String errorText) {
        this.errorDecorationText = errorText;
        if (this.errorDecoration != null) {
            this.errorDecoration.setDescriptionText(errorText);
        }
    }

    /**
     * Will show the control decoration adding the given text as description
     * text.
     *
     * @param errorText
     *            the text to be shown in the info hover, or <code>null</code>
     *            if no text should be shown.
     * @see ControlDecoration#show()
     * @see ControlDecoration#showHoverText(String)
     */
    public void showErrorDecorationHover(String errorText) {
        if (this.errorDecoration != null) {
            this.errorDecoration.show();
            this.errorDecoration.showHoverText(errorText);
        }
    }

    /**
     * Configure the id that should be used to retrieve the
     * {@link FieldDecoration} to be used by this
     * {@link ControlDecorationProvider}.
     *
     * @param fieldDecorationId
     *            The String to determine the {@link FieldDecoration} to use by
     *            the {@link ControlDecoration} that is provided by this
     *            {@link ControlDecorationProvider}.
     */
    public void setFieldDecorationId(String fieldDecorationId) {
        this.fieldDecorationId = fieldDecorationId;
    }

    /**
     * Set the position configuration where the decoration should be rendered
     * relative to the control that should be decorated.
     *
     * @param decorationPositionOverride
     *            bit-wise or of position constants (<code>SWT.TOP</code>,
     *            <code>SWT.BOTTOM</code>, <code>SWT.LEFT</code>,
     *            <code>SWT.RIGHT</code>, and <code>SWT.CENTER</code>).
     */
    public void setDecorationPositionOverride(int decorationPositionOverride) {
        this.decorationPositionOverride = decorationPositionOverride;
    }

    /**
     * Will show the control decoration.
     *
     * @see ControlDecoration#show()
     */
    public void showDecoration() {
        if (this.errorDecoration != null) {
            this.errorDecoration.show();
        }
    }

    /**
     * Will hide the control decoration.
     *
     * @see ControlDecoration#hide()
     */
    public void hideDecoration() {
        if (this.errorDecoration != null) {
            this.errorDecoration.hide();
        }
    }

    /**
     * Ensure to hide the decoration and dispose any resources related to the
     * {@link ControlDecoration}
     */
    public void dispose() {
        if (this.errorDecoration != null) {
            this.errorDecoration.hide();
            this.errorDecoration.dispose();
            this.errorDecoration = null;
        }
    }

    /**
     * If showing an error decoration is enabled, this method will create and
     * add a {@link ControlDecoration} for the given {@link Control} by using
     * the configured information.
     *
     * @param controlToDecorate
     *            The {@link Control} to create the decoration for.
     */
    public void createErrorDecorationIfRequired(final Control controlToDecorate) {

        if (this.errorDecorationEnabled) {

            final Image errorImage = FieldDecorationRegistry.getDefault()
                    .getFieldDecoration(this.fieldDecorationId).getImage();
            if (this.decorationPositionOverride == SWT.DEFAULT) {
                controlToDecorate.addPaintListener(new PaintListener() { // Using
                                                                         // a
                                                                         // PaintListener
                                                                         // as
                                                                         // bounds
                                                                         // are
                                                                         // only
                                                                         // set
                                                                         // AFTER
                                                                         // activateCell()

                            @Override
                            public void paintControl(PaintEvent e) {

                                controlToDecorate.removePaintListener(this);
                                int position = SWT.TOP;
                                final Rectangle textBounds = controlToDecorate
                                        .getBounds();
                                final Rectangle parentClientArea = controlToDecorate
                                        .getParent().getClientArea();
                                if ((parentClientArea.x + parentClientArea.width) > (textBounds.x
                                        + textBounds.width + errorImage
                                        .getBounds().width)) {
                                    position |= SWT.RIGHT;
                                } else {
                                    position |= SWT.LEFT;
                                }
                                ControlDecorationProvider.this.errorDecoration = newControlDecoration(
                                        controlToDecorate, errorImage, position);
                            }
                        });
            } else {
                this.errorDecoration = newControlDecoration(controlToDecorate,
                        errorImage, this.decorationPositionOverride);
            }
        }
    }

    /**
     * Will create a new {@link ControlDecoration} for the given {@link Control}
     * . Setting position, image and text to the decoration, hiding it
     * initially.
     *
     * @param controlToDecorate
     *            The {@link Control} to create the decoration for.
     * @param errorImage
     *            The image to be shown adjacent to the control. Should never be
     *            <code>null</code>.
     * @param position
     *            bit-wise or of position constants (<code>SWT.TOP</code>,
     *            <code>SWT.BOTTOM</code>, <code>SWT.LEFT</code>,
     *            <code>SWT.RIGHT</code>, and <code>SWT.CENTER</code>).
     * @return The created {@link ControlDecoration}
     */
    private ControlDecoration newControlDecoration(Control controlToDecorate,
            Image errorImage, int position) {
        final ControlDecoration errorDecoration = new ControlDecoration(
                controlToDecorate, position);
        errorDecoration.setImage(errorImage);
        errorDecoration.setDescriptionText(this.errorDecorationText);
        errorDecoration.hide();
        return errorDecoration;
    }

}
