/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.summaryrow.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;

/**
 * Command to trigger the calculation of the summary row values manually.
 * <p>
 * Usually the calculation will be triggered when the summary row moves into the
 * viewport. For large tables that should be printed or exported the calculation
 * is too late. This is because the calculation happens in a seperate thread to
 * avoid freezing of the table on calculation.
 * <p>
 * There is no special handler for this event, as it is tight coupled to the
 * summary row. Therefore the handling of this command is implemented directly
 * in the SummaryRowLayer.
 * <p>
 * Note: This is only intended for internal usage and could cause a performance
 * leak. If you want to use it for your use cases you should be careful about
 * performance issues.
 *
 * @see SummaryRowLayer
 */
public class CalculateSummaryRowValuesCommand implements ILayerCommand {

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        // no need for a check as the command simply triggers the calculation of
        // the
        // summary row values and firing a CalculationFinishedEvent
        return true;
    }

    @Override
    public ILayerCommand cloneCommand() {
        // as the command doesn't have a state, the clone is simply a new
        // instance
        return new CalculateSummaryRowValuesCommand();
    }

}
