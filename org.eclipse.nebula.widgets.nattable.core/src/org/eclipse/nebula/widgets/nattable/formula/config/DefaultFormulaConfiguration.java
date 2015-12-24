/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.formula.config;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.copy.InternalClipboardStructuralChangeListener;
import org.eclipse.nebula.widgets.nattable.copy.action.ClearClipboardAction;
import org.eclipse.nebula.widgets.nattable.copy.action.PasteDataAction;
import org.eclipse.nebula.widgets.nattable.copy.action.PasteOrMoveSelectionAction;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.action.DeleteSelectionAction;
import org.eclipse.nebula.widgets.nattable.edit.command.DeleteSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.export.command.ExportCommandHandler;
import org.eclipse.nebula.widgets.nattable.fillhandle.FillHandleLayerPainter;
import org.eclipse.nebula.widgets.nattable.fillhandle.event.FillHandleEventMatcher;
import org.eclipse.nebula.widgets.nattable.formula.FormulaDataProvider;
import org.eclipse.nebula.widgets.nattable.formula.FormulaEditDisplayConverter;
import org.eclipse.nebula.widgets.nattable.formula.FormulaResultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.formula.action.FormulaFillHandleDragMode;
import org.eclipse.nebula.widgets.nattable.formula.command.DisableFormulaEvaluationCommandHandler;
import org.eclipse.nebula.widgets.nattable.formula.command.EnableFormulaEvaluationCommandHandler;
import org.eclipse.nebula.widgets.nattable.formula.command.FormulaCopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.formula.command.FormulaFillHandlePasteCommandHandler;
import org.eclipse.nebula.widgets.nattable.formula.command.FormulaPasteDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;

/**
 * The default configuration for a formula supporting grid.
 *
 * @since 1.4
 */
public class DefaultFormulaConfiguration implements IConfiguration {

    private FormulaDataProvider dataProvider;
    private SelectionLayer selectionLayer;

    private InternalCellClipboard clipboard;

    public DefaultFormulaConfiguration(FormulaDataProvider dataProvider, SelectionLayer selectionLayer, InternalCellClipboard clipboard) {
        this.dataProvider = dataProvider;
        this.selectionLayer = selectionLayer;
        this.clipboard = clipboard;
    }

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(
                EditConfigAttributes.CELL_EDITABLE_RULE,
                IEditableRule.ALWAYS_EDITABLE);

        // register converter to make editing of functions work
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                new FormulaEditDisplayConverter(this.dataProvider),
                DisplayMode.EDIT);

        // register converter to show decimal values localized
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                new FormulaResultDisplayConverter(this.dataProvider),
                DisplayMode.NORMAL,
                GridRegion.BODY);

        // register TextCellEditor that moves on arrow keys and enter
        configRegistry.registerConfigAttribute(
                EditConfigAttributes.CELL_EDITOR,
                new TextCellEditor(true, true, true));

        // register the border style to use for copy border
        IStyle copyBorderStyle = new Style();
        copyBorderStyle.setAttributeValue(
                CellStyleAttributes.BORDER_STYLE,
                new BorderStyle(1, GUIHelper.COLOR_BLACK, LineStyleEnum.DASHED));
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                copyBorderStyle,
                DisplayMode.NORMAL,
                SelectionStyleLabels.COPY_BORDER_STYLE);
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        // ui binding for deleting a cell value on pressing DEL
        uiBindingRegistry.registerFirstKeyBinding(
                new KeyEventMatcher(SWT.DEL),
                new DeleteSelectionAction());

        // ui binding to perform a paste action on pressing CTRL+V
        uiBindingRegistry.registerFirstKeyBinding(
                new KeyEventMatcher(SWT.MOD1, 'v'),
                new PasteDataAction());

        // ui binding to perform paste or selection movement on ENTER
        uiBindingRegistry.registerFirstKeyBinding(
                new KeyEventMatcher(SWT.NONE, SWT.CR),
                new PasteOrMoveSelectionAction(this.clipboard));

        // ui binding to clear the InternalCellClipboard
        uiBindingRegistry.registerFirstKeyBinding(
                new KeyEventMatcher(SWT.NONE, SWT.ESC),
                new ClearClipboardAction(this.clipboard));

        // Mouse drag
        // trigger the handle drag operations
        // Note: we ensure a FillHandleLayerPainter is set in configureLayer
        uiBindingRegistry.registerFirstMouseDragMode(
                new FillHandleEventMatcher((FillHandleLayerPainter) this.selectionLayer.getLayerPainter()),
                new FormulaFillHandleDragMode(this.selectionLayer, this.clipboard, this.dataProvider));

    }

    @Override
    public void configureLayer(ILayer layer) {
        // register the command handler for deleting values
        layer.registerCommandHandler(new DeleteSelectionCommandHandler(this.selectionLayer));

        // register the ExportCommandHandler to the current layer (should be a
        // layer on top of the GridLayer to override default GridLayer behavior)
        // that exports from the SelectionLayer downwards. This way the column
        // and row headers won't be exported.
        layer.registerCommandHandler(new ExportCommandHandler(this.selectionLayer));

        // register the command handler for enabling/disabling formula
        // evaluation
        // register on the SelectionLayer so it works on export
        this.selectionLayer.registerCommandHandler(new DisableFormulaEvaluationCommandHandler(this.dataProvider));
        this.selectionLayer.registerCommandHandler(new EnableFormulaEvaluationCommandHandler(this.dataProvider));

        // add a layer listener that clears the internal clipboard on structural
        // changes
        this.selectionLayer.addLayerListener(new InternalClipboardStructuralChangeListener(this.clipboard));

        // add the layer painter that renders a border around copied cells
        if (!(this.selectionLayer.getLayerPainter() instanceof FillHandleLayerPainter)) {
            this.selectionLayer.setLayerPainter(new FillHandleLayerPainter(this.clipboard));
        } else {
            // ensure the clipboard is set
            ((FillHandleLayerPainter) this.selectionLayer.getLayerPainter()).setClipboard(this.clipboard);
        }

        // register special copy+paste command handlers
        layer.registerCommandHandler(
                new FormulaCopyDataCommandHandler(this.selectionLayer, this.clipboard));
        layer.registerCommandHandler(
                new FormulaPasteDataCommandHandler(this.selectionLayer, this.clipboard, this.dataProvider));
        layer.registerCommandHandler(
                new FormulaFillHandlePasteCommandHandler(this.selectionLayer, this.clipboard, this.dataProvider));
    }
}