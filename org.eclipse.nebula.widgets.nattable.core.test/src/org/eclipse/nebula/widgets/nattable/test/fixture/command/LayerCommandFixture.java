/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.fixture.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class LayerCommandFixture implements ILayerCommand {

    private ILayer targetLayer;

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        this.targetLayer = targetLayer;
        return true;
    }

    public ILayer getTargetLayer() {
        return this.targetLayer;
    }

    @Override
    public LayerCommandFixture cloneCommand() {
        return this;
    }

}
