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

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;

public class DebugMenuConfiguration extends AbstractUiBindingConfiguration {

    private final Menu debugMenu;

    public DebugMenuConfiguration(NatTable natTable) {
        this.debugMenu =
                new PopupMenuBuilder(natTable)
                        .withInspectLabelsMenuItem()
                        .build();
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        uiBindingRegistry.registerMouseDownBinding(
                new MouseEventMatcher(SWT.NONE, null, 3),
                new PopupMenuAction(this.debugMenu));
    }

}
