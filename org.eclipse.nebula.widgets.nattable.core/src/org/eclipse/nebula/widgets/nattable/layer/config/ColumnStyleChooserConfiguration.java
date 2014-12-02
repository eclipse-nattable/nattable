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
package org.eclipse.nebula.widgets.nattable.layer.config;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.editor.command.DisplayColumnStyleEditorCommandHandler;

/**
 * Registers the {@link DisplayColumnStyleEditorCommandHandler}
 *
 */
public class ColumnStyleChooserConfiguration extends
        AbstractRegistryConfiguration {

    private AbstractLayer bodyLayer;
    private ColumnOverrideLabelAccumulator labelAccumulator;
    private final SelectionLayer selectionLayer;

    public ColumnStyleChooserConfiguration(AbstractLayer bodyLayer,
            SelectionLayer selectionLayer) {
        this.bodyLayer = bodyLayer;
        this.selectionLayer = selectionLayer;
        this.labelAccumulator = new ColumnOverrideLabelAccumulator(bodyLayer);
        bodyLayer.setConfigLabelAccumulator(this.labelAccumulator);
    }

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        DisplayColumnStyleEditorCommandHandler columnChooserCommandHandler = new DisplayColumnStyleEditorCommandHandler(
                this.selectionLayer, this.labelAccumulator, configRegistry);

        this.bodyLayer.registerCommandHandler(columnChooserCommandHandler);
    }
}
