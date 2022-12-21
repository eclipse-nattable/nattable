/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.test.fixture;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.DataCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.junit.jupiter.api.Disabled;

@Disabled
public class TestLayerCell extends LayerCell {

    public TestLayerCell(ILayer layer, int columnPosition, int rowPosition) {
        super(layer, columnPosition, rowPosition);
    }

    public TestLayerCell(ILayer layer, int columnPosition, int rowPosition,
            DataCell cell) {
        super(layer, columnPosition, rowPosition, cell);
    }

    public TestLayerCell(ILayer layer, int originColumnPosition,
            int originRowPosition, int columnPosition, int rowPosition,
            int columnSpan, int rowSpan) {
        super(layer, originColumnPosition, originRowPosition, columnPosition,
                rowPosition, columnSpan, rowSpan);
    }

    public TestLayerCell(ILayerCell cell) {
        super(cell.getLayer(), cell.getOriginColumnPosition(), cell
                .getOriginRowPosition(), cell.getColumnPosition(),
                cell
                        .getRowPosition(),
                cell.getColumnSpan(), cell.getRowSpan());
    }

    @Override
    public boolean equals(Object obj) {
        // no checks for class and layer to be able to perform automated tests
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ILayerCell))
            return false;
        ILayerCell other = (ILayerCell) obj;
        if (this.getColumnSpan() != other.getColumnSpan())
            return false;
        if (this.getOriginColumnPosition() != other.getOriginColumnPosition())
            return false;
        if (this.getOriginRowPosition() != other.getOriginRowPosition())
            return false;
        if (this.getRowSpan() != other.getRowSpan())
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.getColumnSpan();
        result = prime * result + this.getOriginColumnPosition();
        result = prime * result + this.getOriginRowPosition();
        result = prime * result + this.getRowSpan();
        return result;
    }

}
