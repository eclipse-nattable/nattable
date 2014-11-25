/*******************************************************************************
 * Copyright (c) 2012, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 453219
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.menu;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;

public class BodyMenuConfiguration extends AbstractUiBindingConfiguration {

    private final Menu colHeaderMenu;

    public BodyMenuConfiguration(NatTable natTable, ILayer bodyLayer) {
        this.colHeaderMenu =
                new PopupMenuBuilder(natTable)
                        .withColumnStyleEditor(Messages.getString("ColumnStyleEditorDialog.shellTitle")) //$NON-NLS-1$
                            .build();
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        uiBindingRegistry.registerMouseDownBinding(
                new MouseEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, 3),
                new PopupMenuAction(this.colHeaderMenu));
    }

}
