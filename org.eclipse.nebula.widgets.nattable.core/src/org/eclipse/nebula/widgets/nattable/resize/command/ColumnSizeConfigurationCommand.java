/*****************************************************************************
 * Copyright (c) 2015, 2016 CEA LIST.
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
package org.eclipse.nebula.widgets.nattable.resize.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractRegionCommand;

/**
 * Command to configure column widths. Instead of knowing the column indexes to
 * resize, it can be used to search for columns that have a special label
 * applied.
 *
 * @since 1.4
 */
public class ColumnSizeConfigurationCommand extends AbstractRegionCommand {

    /**
     * The width to set. Can be <code>null</code> in combination with
     * {@link #percentageSizing} == <code>true</code> to enable percentage
     * sizing with equal column widths.
     */
    public final Integer newColumnWidth;
    /**
     * Boolean flag to specify whether the width should be set as percentage or
     * as pixel value.
     */
    public final boolean percentageSizing;

    /**
     *
     * @param label
     *            The label that needs to be applied to a column in order to
     *            process the resize command. Is used to determine the column
     *            index. If <code>null</code> the default size will be set.
     * @param newColumnWidth
     *            The width to set. Can be <code>null</code> in combination with
     *            {@link #percentageSizing} == <code>true</code> to enable
     *            percentage sizing with equal column widths.
     * @param percentageSizing
     *            Boolean flag to specify whether the width should be set as
     *            percentage or as pixel value.
     */
    public ColumnSizeConfigurationCommand(String label, Integer newColumnWidth, boolean percentageSizing) {
        super(label);
        this.newColumnWidth = newColumnWidth;
        this.percentageSizing = percentageSizing;
    }

    @Override
    public AbstractRegionCommand cloneForRegion() {
        return new ColumnSizeConfigurationCommand(null, this.newColumnWidth, this.percentageSizing);
    }
}
