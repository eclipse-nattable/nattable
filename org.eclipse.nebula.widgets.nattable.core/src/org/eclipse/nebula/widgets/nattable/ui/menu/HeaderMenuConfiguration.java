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
package org.eclipse.nebula.widgets.nattable.ui.menu;

import org.eclipse.nebula.widgets.nattable.NatTable;

public class HeaderMenuConfiguration extends AbstractHeaderMenuConfiguration {

    public HeaderMenuConfiguration(NatTable natTable) {
        super(natTable);
    }

    @Override
    protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
        return super.createColumnHeaderMenu(natTable).withHideColumnMenuItem()
                .withShowAllColumnsMenuItem().withCreateColumnGroupsMenuItem()
                .withUngroupColumnsMenuItem()
                .withAutoResizeSelectedColumnsMenuItem()
                .withColumnStyleEditor().withColumnRenameDialog()
                .withClearAllFilters();
    }

    @Override
    protected PopupMenuBuilder createRowHeaderMenu(NatTable natTable) {
        return super.createRowHeaderMenu(natTable)
                .withAutoResizeSelectedRowsMenuItem();
    }

    @Override
    protected PopupMenuBuilder createCornerMenu(NatTable natTable) {
        return super.createCornerMenu(natTable).withShowAllColumnsMenuItem();
    }
}
