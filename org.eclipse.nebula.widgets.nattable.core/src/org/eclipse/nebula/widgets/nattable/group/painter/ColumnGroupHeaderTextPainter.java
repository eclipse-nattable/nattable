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
package org.eclipse.nebula.widgets.nattable.group.painter;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.swt.graphics.GC;

public class ColumnGroupHeaderTextPainter extends CellPainterWrapper {

    /**
     * Creates the default {@link ColumnGroupHeaderTextPainter} that uses a
     * {@link TextPainter} as base {@link ICellPainter} and decorate it with the
     * {@link ColumnGroupExpandCollapseImagePainter} on the right edge of the
     * cell.
     *
     * @param columnGroupModel
     *            the column group model that is used by the grid
     *
     * @deprecated Use constructor without ColumnGroupModel reference
     */
    @Deprecated
    public ColumnGroupHeaderTextPainter(ColumnGroupModel columnGroupModel) {
        this(columnGroupModel, new TextPainter());
    }

    /**
     * Creates a {@link ColumnGroupHeaderTextPainter} that uses the given
     * {@link ICellPainter} as base {@link ICellPainter} and decorate it with
     * the {@link ColumnGroupExpandCollapseImagePainter} on the right edge of
     * the cell.
     *
     * @param columnGroupModel
     *            the column group model that is used by the grid
     * @param interiorPainter
     *            the base {@link ICellPainter} to use
     *
     * @deprecated Use constructor without ColumnGroupModel reference
     */
    @Deprecated
    public ColumnGroupHeaderTextPainter(ColumnGroupModel columnGroupModel,
            ICellPainter interiorPainter) {
        this(columnGroupModel, interiorPainter, CellEdgeEnum.RIGHT);
    }

    /**
     * Creates a {@link ColumnGroupHeaderTextPainter} that uses the given
     * {@link ICellPainter} as base {@link ICellPainter} and decorate it with
     * the {@link ColumnGroupExpandCollapseImagePainter} on the specified edge
     * of the cell.
     *
     * @param columnGroupModel
     *            the column group model that is used by the grid
     * @param interiorPainter
     *            the base {@link ICellPainter} to use
     * @param cellEdge
     *            the edge of the cell on which the column group indicator
     *            decoration should be applied
     *
     * @deprecated Use constructor without ColumnGroupModel reference
     */
    @Deprecated
    public ColumnGroupHeaderTextPainter(ColumnGroupModel columnGroupModel,
            ICellPainter interiorPainter, CellEdgeEnum cellEdge) {
        this(interiorPainter, cellEdge,
                new ColumnGroupExpandCollapseImagePainter(true));
    }

    /**
     * Creates the default {@link ColumnGroupHeaderTextPainter} that uses a
     * {@link TextPainter} as base {@link ICellPainter} and decorate it with the
     * {@link ColumnGroupExpandCollapseImagePainter} on the right edge of the
     * cell.
     */
    public ColumnGroupHeaderTextPainter() {
        this(new TextPainter());
    }

    /**
     * Creates a {@link ColumnGroupHeaderTextPainter} that uses the given
     * {@link ICellPainter} as base {@link ICellPainter} and decorate it with
     * the {@link ColumnGroupExpandCollapseImagePainter} on the right edge of
     * the cell.
     *
     * @param interiorPainter
     *            the base {@link ICellPainter} to use
     */
    public ColumnGroupHeaderTextPainter(ICellPainter interiorPainter) {
        this(interiorPainter, CellEdgeEnum.RIGHT);
    }

    /**
     * Creates a {@link ColumnGroupHeaderTextPainter} that uses the given
     * {@link ICellPainter} as base {@link ICellPainter} and decorate it with
     * the {@link ColumnGroupExpandCollapseImagePainter} on the specified edge
     * of the cell.
     *
     * @param interiorPainter
     *            the base {@link ICellPainter} to use
     * @param cellEdge
     *            the edge of the cell on which the column group indicator
     *            decoration should be applied
     */
    public ColumnGroupHeaderTextPainter(ICellPainter interiorPainter,
            CellEdgeEnum cellEdge) {
        this(interiorPainter, cellEdge,
                new ColumnGroupExpandCollapseImagePainter(true));
    }

    /**
     * Creates a {@link ColumnGroupHeaderTextPainter} that uses the given
     * {@link ICellPainter} as base {@link ICellPainter} and decorate it with
     * the given {@link ICellPainter} to use for column group related decoration
     * on the specified edge of the cell.
     *
     * @param interiorPainter
     *            the base {@link ICellPainter} to use
     * @param cellEdge
     *            the edge of the cell on which the column group indicator
     *            decoration should be applied
     * @param decoratorPainter
     *            the {@link ICellPainter} that should be used to paint the
     *            column group related decoration (by default the
     *            {@link ColumnGroupExpandCollapseImagePainter} will be used)
     */
    public ColumnGroupHeaderTextPainter(ICellPainter interiorPainter,
            CellEdgeEnum cellEdge, ICellPainter decoratorPainter) {
        setWrappedPainter(new CellPainterDecorator(interiorPainter, cellEdge,
                decoratorPainter));
    }

    // the following constructors are intended to configure the
    // CellPainterDecorator that is created as
    // the wrapped painter of this ColumnGroupHeaderTextPainter

    /**
     * Creates a {@link ColumnGroupHeaderTextPainter} that uses the given
     * {@link ICellPainter} as base {@link ICellPainter}. It will use the
     * {@link ColumnGroupExpandCollapseImagePainter} as decorator for column
     * group related decorations at the specified cell edge, which can be
     * configured to render the background or not via method parameter. With the
     * additional parameters, the behaviour of the created
     * {@link CellPainterDecorator} can be configured in terms of rendering.
     *
     * @param columnGroupModel
     *            the column group model that is used by the grid
     * @param interiorPainter
     *            the base {@link ICellPainter} to use
     * @param cellEdge
     *            the edge of the cell on which the column group indicator
     *            decoration should be applied
     * @param paintBg
     *            flag to configure whether the
     *            {@link ColumnGroupExpandCollapseImagePainter} should paint the
     *            background or not
     * @param spacing
     *            the number of pixels that should be used as spacing between
     *            cell edge and decoration
     * @param paintDecorationDependent
     *            flag to configure if the base {@link ICellPainter} should
     *            render decoration dependent or not. If it is set to
     *            <code>false</code>, the base painter will always paint at the
     *            same coordinates, using the whole cell bounds,
     *            <code>true</code> will cause the bounds of the cell to shrink
     *            for the base painter.
     *
     * @deprecated Use constructor without ColumnGroupModel reference
     */
    @Deprecated
    public ColumnGroupHeaderTextPainter(ColumnGroupModel columnGroupModel,
            ICellPainter interiorPainter, CellEdgeEnum cellEdge,
            boolean paintBg, int spacing, boolean paintDecorationDependent) {

        ICellPainter sortPainter = new ColumnGroupExpandCollapseImagePainter(
                paintBg);
        CellPainterDecorator painter = new CellPainterDecorator(
                interiorPainter, cellEdge, spacing, sortPainter,
                paintDecorationDependent, paintBg);
        setWrappedPainter(painter);
    }

    /**
     * Creates a {@link ColumnGroupHeaderTextPainter} that uses the given
     * {@link ICellPainter} as base {@link ICellPainter} and decorate it with
     * the {@link ColumnGroupExpandCollapseImagePainter} on the right edge of
     * the cell. This constructor gives the opportunity to configure the
     * behaviour of the {@link ColumnGroupExpandCollapseImagePainter} and the
     * {@link CellPainterDecorator} for some attributes. Remains because of
     * downwards compatibility.
     *
     * @param columnGroupModel
     *            the column group model that is used by the grid
     * @param interiorPainter
     *            the base {@link ICellPainter} to use
     * @param paintBg
     *            flag to configure whether the
     *            {@link ColumnGroupExpandCollapseImagePainter} should paint the
     *            background or not
     * @param interiorPainterToSpanFullWidth
     *            flag to configure how the bounds of the base painter should be
     *            calculated
     *
     * @deprecated Use constructor without ColumnGroupModel reference
     */
    @Deprecated
    public ColumnGroupHeaderTextPainter(ColumnGroupModel columnGroupModel,
            ICellPainter interiorPainter, boolean paintBg,
            boolean interiorPainterToSpanFullWidth) {
        ICellPainter sortPainter = new ColumnGroupExpandCollapseImagePainter(
                paintBg);
        CellPainterDecorator painter = new CellPainterDecorator(
                interiorPainter, CellEdgeEnum.RIGHT, 0, sortPainter,
                !interiorPainterToSpanFullWidth, paintBg);
        setWrappedPainter(painter);
    }

    /**
     * Creates a {@link ColumnGroupHeaderTextPainter} that uses the given
     * {@link ICellPainter} as base {@link ICellPainter}. It will use the
     * {@link ColumnGroupExpandCollapseImagePainter} as decorator for column
     * group related decorations at the specified cell edge, which can be
     * configured to render the background or not via method parameter. With the
     * additional parameters, the behaviour of the created
     * {@link CellPainterDecorator} can be configured in terms of rendering.
     *
     * @param interiorPainter
     *            the base {@link ICellPainter} to use
     * @param cellEdge
     *            the edge of the cell on which the column group indicator
     *            decoration should be applied
     * @param paintBg
     *            flag to configure whether the
     *            {@link ColumnGroupExpandCollapseImagePainter} should paint the
     *            background or not
     * @param spacing
     *            the number of pixels that should be used as spacing between
     *            cell edge and decoration
     * @param paintDecorationDependent
     *            flag to configure if the base {@link ICellPainter} should
     *            render decoration dependent or not. If it is set to
     *            <code>false</code>, the base painter will always paint at the
     *            same coordinates, using the whole cell bounds,
     *            <code>true</code> will cause the bounds of the cell to shrink
     *            for the base painter.
     */
    public ColumnGroupHeaderTextPainter(ICellPainter interiorPainter,
            CellEdgeEnum cellEdge, boolean paintBg, int spacing,
            boolean paintDecorationDependent) {

        ICellPainter sortPainter = new ColumnGroupExpandCollapseImagePainter(
                paintBg);
        CellPainterDecorator painter = new CellPainterDecorator(
                interiorPainter, cellEdge, spacing, sortPainter,
                paintDecorationDependent, paintBg);
        setWrappedPainter(painter);
    }

    /**
     * Creates a {@link ColumnGroupHeaderTextPainter} that uses the given
     * {@link ICellPainter} as base {@link ICellPainter}. It will use the given
     * {@link ICellPainter} as decorator for column group related decorations at
     * the specified cell edge, which can be configured to render the background
     * or not via method parameter. With the additional parameters, the
     * behaviour of the created {@link CellPainterDecorator} can be configured
     * in terms of rendering.
     *
     * @param interiorPainter
     *            the base {@link ICellPainter} to use
     * @param cellEdge
     *            the edge of the cell on which the column group indicator
     *            decoration should be applied
     * @param decoratorPainter
     *            the {@link ICellPainter} that should be used to paint the
     *            column group related decoration
     * @param paintBg
     *            flag to configure whether the {@link CellPainterDecorator}
     *            should paint the background or not
     * @param spacing
     *            the number of pixels that should be used as spacing between
     *            cell edge and decoration
     * @param paintDecorationDependent
     *            flag to configure if the base {@link ICellPainter} should
     *            render decoration dependent or not. If it is set to
     *            <code>false</code>, the base painter will always paint at the
     *            same coordinates, using the whole cell bounds,
     *            <code>true</code> will cause the bounds of the cell to shrink
     *            for the base painter.
     */
    public ColumnGroupHeaderTextPainter(ICellPainter interiorPainter,
            CellEdgeEnum cellEdge, ICellPainter decoratorPainter,
            boolean paintBg, int spacing, boolean paintDecorationDependent) {

        CellPainterDecorator painter = new CellPainterDecorator(
                interiorPainter, cellEdge, spacing, decoratorPainter,
                paintDecorationDependent, paintBg);
        setWrappedPainter(painter);
    }

    /**
     * Creates a {@link ColumnGroupHeaderTextPainter} that uses the given
     * {@link ICellPainter} as base {@link ICellPainter} and decorate it with
     * the {@link ColumnGroupExpandCollapseImagePainter} on the right edge of
     * the cell. This constructor gives the opportunity to configure the
     * behaviour of the {@link ColumnGroupExpandCollapseImagePainter} and the
     * {@link CellPainterDecorator} for some attributes. Remains because of
     * downwards compatibility.
     *
     * @param interiorPainter
     *            the base {@link ICellPainter} to use
     * @param paintBg
     *            flag to configure whether the
     *            {@link ColumnGroupExpandCollapseImagePainter} should paint the
     *            background or not
     * @param interiorPainterToSpanFullWidth
     *            flag to configure how the bounds of the base painter should be
     *            calculated
     */
    public ColumnGroupHeaderTextPainter(ICellPainter interiorPainter,
            boolean paintBg, boolean interiorPainterToSpanFullWidth) {
        ICellPainter sortPainter = new ColumnGroupExpandCollapseImagePainter(
                paintBg);
        CellPainterDecorator painter = new CellPainterDecorator(
                interiorPainter, CellEdgeEnum.RIGHT, 0, sortPainter,
                !interiorPainterToSpanFullWidth, paintBg);
        setWrappedPainter(painter);
    }

    /**
     * Preferred width is used during auto resize. Column groups do not
     * participate in auto resize, since auto resizing is done by the column
     * width. Hence, always return 0
     */
    @Override
    public int getPreferredWidth(ILayerCell cell, GC gc,
            IConfigRegistry configRegistry) {
        return 0;
    }

    @Override
    public int getPreferredHeight(ILayerCell cell, GC gc,
            IConfigRegistry configRegistry) {
        return 25;
    }

}
