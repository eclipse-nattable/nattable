/*****************************************************************************
 * Copyright (c) 2016 Loris Securo.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Loris Securo <lorissek@gmail.com> - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.BorderModeEnum;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Draws borders based on a 2D array of {@link BorderCell} and styled
 * accordingly to a {@link BorderStyle}.
 *
 * @author Loris Securo
 *
 * @since 1.5
 */
public class BorderPainter {

    protected BorderCell borderCells[][];
    protected BorderStyle borderStyle;
    protected PaintModeEnum paintMode;

    public enum PaintModeEnum {
        /**
         * The border will be painted completely for all the cells.
         */
        ALL,

        /**
         * The border will be painted completely for the internal cells. The
         * border will not be painted for the external cells.
         */
        NO_EXTERNAL_CELLS,

        /**
         * The border will be painted completely for the internal cells. For the
         * external cells, only the inner borders will be painted.
         */
        NO_EXTERNAL_BORDERS;
    }

    /**
     * Utility class used to return coordinates and lengths at once.
     */
    public static class LineValues {
        public int x;
        public int y;
        public int lenght;

        public LineValues(int x, int y, int lenght) {
            this.x = x;
            this.y = y;
            this.lenght = lenght;
        }
    }

    /**
     * Stores the bounds and the state of a cell that will be used to draw
     * borders.
     */
    public static class BorderCell {
        public Rectangle bounds;
        public boolean isInsideBorder;

        public BorderCell(Rectangle bounds) {
            this(bounds, true);
        }

        public BorderCell(Rectangle bounds, boolean isInsideBorder) {
            this.bounds = bounds;
            this.isInsideBorder = isInsideBorder;
        }
    }

    public BorderPainter(BorderCell borderCells[][], BorderStyle borderStyle) {
        this(borderCells, borderStyle, PaintModeEnum.ALL);
    }

    public BorderPainter(BorderCell borderCells[][], BorderStyle borderStyle, PaintModeEnum paintMode) {
        this.borderCells = borderCells;
        this.borderStyle = borderStyle;
        this.paintMode = paintMode;
    }

    public void paintBorder(GC gc) {

        // apply the border style
        if (this.borderStyle != null) {
            gc.setLineStyle(LineStyleEnum.toSWT(this.borderStyle.getLineStyle()));
            gc.setLineWidth(this.borderStyle.getThickness());
            gc.setForeground(this.borderStyle.getColor());
        }

        for (int iy = 0; iy < this.borderCells.length; iy++) {
            for (int ix = 0; ix < this.borderCells[iy].length; ix++) {

                // if we don't have to draw borders of external cells, we ignore
                // them
                if (this.paintMode == PaintModeEnum.NO_EXTERNAL_CELLS) {
                    if (ix == 0 || iy == 0 || ix == this.borderCells[iy].length - 1 || iy == this.borderCells.length - 1) {
                        continue;
                    }
                }

                if (isInside(ix, iy)) {

                    // if we are in an external cell and we don't have to draw
                    // its external borders, we check if the border to draw is
                    // internal or external

                    // left is not in, draw left border
                    if ((this.paintMode != PaintModeEnum.NO_EXTERNAL_BORDERS || ix > 0) && !isInside(ix - 1, iy)) {
                        drawLineLeft(gc, ix, iy);
                    }
                    // right is not in, draw right border
                    if ((this.paintMode != PaintModeEnum.NO_EXTERNAL_BORDERS || ix < this.borderCells[iy].length - 1) && !isInside(ix + 1, iy)) {
                        drawLineRight(gc, ix, iy);
                    }
                    // top is not in, draw top border
                    if ((this.paintMode != PaintModeEnum.NO_EXTERNAL_BORDERS || iy > 0) && !isInside(ix, iy - 1)) {
                        drawLineTop(gc, ix, iy);
                    }
                    // bottom is not in, draw bottom border
                    if ((this.paintMode != PaintModeEnum.NO_EXTERNAL_BORDERS || iy < this.borderCells.length - 1) && !isInside(ix, iy + 1)) {
                        drawLineBottom(gc, ix, iy);
                    }
                }
            }
        }
    }

    // The methods getLineValues... deal with spanned cells by looking at
    // adjacent cells to get the correct line length.
    // For example:
    //
    // -----
    // |I|O| <- 1 cell inside, 1 cell outside
    // -----
    // |I I| <- inside spanned cell for 2
    // -----
    //
    // when considering the cell in (1,1) it should draw a top border between it
    // and the outside cell at (1,0);
    // but it can't use the width of cell (1,1) otherwise it will draw a border
    // also under the inside cell (0,0);
    // therefore its upper cell (1,0) is considered to find the correct position
    // and width of the border line;
    // in this case:
    // x = max((1,1).x0, (1,0).x0)
    // l = min((1,1).x1, (1,0).x1) - x

    protected LineValues getLineValuesLeft(int ix, int iy) {
        int y;
        int l;

        int x = this.borderCells[iy][ix].bounds.x;

        if (isValid(ix - 1, iy)) {
            y = Math.max(this.borderCells[iy][ix].bounds.y, this.borderCells[iy][ix - 1].bounds.y);
            l = Math.min(this.borderCells[iy][ix].bounds.y + this.borderCells[iy][ix].bounds.height, this.borderCells[iy][ix - 1].bounds.y + this.borderCells[iy][ix - 1].bounds.height) - y;

        } else {
            y = this.borderCells[iy][ix].bounds.y;
            l = this.borderCells[iy][ix].bounds.height;
        }

        return new LineValues(x, y, l);
    }

    protected LineValues getLineValuesRight(int ix, int iy) {
        int y;
        int l;

        int x = this.borderCells[iy][ix].bounds.x + this.borderCells[iy][ix].bounds.width - 1;

        if (isValid(ix + 1, iy)) {
            y = Math.max(this.borderCells[iy][ix].bounds.y, this.borderCells[iy][ix + 1].bounds.y);
            l = Math.min(this.borderCells[iy][ix].bounds.y + this.borderCells[iy][ix].bounds.height, this.borderCells[iy][ix + 1].bounds.y + this.borderCells[iy][ix + 1].bounds.height) - y;

        } else {
            y = this.borderCells[iy][ix].bounds.y;
            l = this.borderCells[iy][ix].bounds.height;
        }

        return new LineValues(x, y, l);
    }

    protected LineValues getLineValuesTop(int ix, int iy) {
        int x;
        int l;

        if (isValid(ix, iy - 1)) {
            x = Math.max(this.borderCells[iy][ix].bounds.x, this.borderCells[iy - 1][ix].bounds.x);
            l = Math.min(this.borderCells[iy][ix].bounds.x + this.borderCells[iy][ix].bounds.width, this.borderCells[iy - 1][ix].bounds.x + this.borderCells[iy - 1][ix].bounds.width) - x;
        } else {
            x = this.borderCells[iy][ix].bounds.x;
            l = this.borderCells[iy][ix].bounds.width;
        }

        int y = this.borderCells[iy][ix].bounds.y;

        return new LineValues(x, y, l);
    }

    protected LineValues getLineValuesBottom(int ix, int iy) {
        int x;
        int l;

        if (isValid(ix, iy + 1)) {
            x = Math.max(this.borderCells[iy][ix].bounds.x, this.borderCells[iy + 1][ix].bounds.x);
            l = Math.min(this.borderCells[iy][ix].bounds.x + this.borderCells[iy][ix].bounds.width, this.borderCells[iy + 1][ix].bounds.x + this.borderCells[iy + 1][ix].bounds.width) - x;
        } else {
            x = this.borderCells[iy][ix].bounds.x;
            l = this.borderCells[iy][ix].bounds.width;
        }

        int y = this.borderCells[iy][ix].bounds.y + this.borderCells[iy][ix].bounds.height - 1;

        return new LineValues(x, y, l);
    }

    protected void drawLineLeft(GC gc, int ix, int iy) {
        LineValues lineValues = getLineValuesLeft(ix, iy);
        int x = lineValues.x;
        int y = lineValues.y;
        int l = lineValues.lenght;

        boolean drawTopCorner = false;
        boolean drawBottomCorner = false;

        BorderModeEnum borderMode = this.borderStyle.getBorderMode();

        // check if we need to draw the top corner
        if (!isInside(ix, iy - 1)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.EXTERNAL) {
                drawTopCorner = true;
            }
        } else if (isInside(ix - 1, iy - 1)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.INTERNAL) {
                drawTopCorner = true;
            }
        }

        // check if we need to draw the bottom corner
        if (!isInside(ix, iy + 1)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.EXTERNAL) {
                drawBottomCorner = true;
            }
        } else if (isInside(ix - 1, iy + 1)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.INTERNAL) {
                drawBottomCorner = true;
            }
        }

        switch (borderMode) {
            case CENTERED:
                GraphicsUtils.drawLineVertical(gc, x, y, l, drawTopCorner, drawBottomCorner);
                break;
            case EXTERNAL:
                GraphicsUtils.drawLineVerticalBorderLeft(gc, x, y, l, drawTopCorner, drawBottomCorner);
                break;
            case INTERNAL:
                GraphicsUtils.drawLineVerticalBorderRight(gc, x, y, l, drawTopCorner, drawBottomCorner);
                break;
        }

    }

    protected void drawLineRight(GC gc, int ix, int iy) {
        LineValues lineValues = getLineValuesRight(ix, iy);
        int x = lineValues.x;
        int y = lineValues.y;
        int l = lineValues.lenght;

        boolean drawTopCorner = false;
        boolean drawBottomCorner = false;

        BorderModeEnum borderMode = this.borderStyle.getBorderMode();

        // check if we need to draw the top corner
        if (!isInside(ix, iy - 1)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.EXTERNAL) {
                drawTopCorner = true;
            }
        } else if (isInside(ix + 1, iy - 1)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.INTERNAL) {
                drawTopCorner = true;
            }
        }

        // check if we need to draw the bottom corner
        if (!isInside(ix, iy + 1)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.EXTERNAL) {
                drawBottomCorner = true;
            }
        } else if (isInside(ix + 1, iy + 1)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.INTERNAL) {
                drawBottomCorner = true;
            }
        }

        switch (this.borderStyle.getBorderMode()) {
            case CENTERED:
                GraphicsUtils.drawLineVertical(gc, x, y, l, drawTopCorner, drawBottomCorner);
                break;
            case EXTERNAL:
                GraphicsUtils.drawLineVerticalBorderRight(gc, x, y, l, drawTopCorner, drawBottomCorner);
                break;
            case INTERNAL:
                GraphicsUtils.drawLineVerticalBorderLeft(gc, x, y, l, drawTopCorner, drawBottomCorner);
                break;
        }

    }

    protected void drawLineTop(GC gc, int ix, int iy) {
        LineValues lineValues = getLineValuesTop(ix, iy);
        int x = lineValues.x;
        int y = lineValues.y;
        int l = lineValues.lenght;

        boolean drawLeftCorner = false;
        boolean drawRightCorner = false;

        BorderModeEnum borderMode = this.borderStyle.getBorderMode();

        // check if we need to draw the left corner
        if (!isInside(ix - 1, iy)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.EXTERNAL) {
                drawLeftCorner = true;
            }
        } else if (isInside(ix - 1, iy - 1)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.INTERNAL) {
                drawLeftCorner = true;
            }
        }

        // check if we need to draw the right corner
        if (!isInside(ix + 1, iy)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.EXTERNAL) {
                drawRightCorner = true;
            }
        } else if (isInside(ix + 1, iy - 1)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.INTERNAL) {
                drawRightCorner = true;
            }
        }

        switch (borderMode) {
            case CENTERED:
                GraphicsUtils.drawLineHorizontal(gc, x, y, l, drawLeftCorner, drawRightCorner);
                break;
            case EXTERNAL:
                GraphicsUtils.drawLineHorizontalBorderTop(gc, x, y, l, drawLeftCorner, drawRightCorner);
                break;
            case INTERNAL:
                GraphicsUtils.drawLineHorizontalBorderBottom(gc, x, y, l, drawLeftCorner, drawRightCorner);
                break;
        }

    }

    protected void drawLineBottom(GC gc, int ix, int iy) {
        LineValues lineValues = getLineValuesBottom(ix, iy);
        int x = lineValues.x;
        int y = lineValues.y;
        int l = lineValues.lenght;

        boolean drawLeftCorner = false;
        boolean drawRightCorner = false;

        BorderModeEnum borderMode = this.borderStyle.getBorderMode();

        // check if we need to draw the left corner
        if (!isInside(ix - 1, iy)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.EXTERNAL) {
                drawLeftCorner = true;
            }
        } else if (isInside(ix - 1, iy + 1)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.INTERNAL) {
                drawLeftCorner = true;
            }
        }

        // check if we need to draw the right corner
        if (!isInside(ix + 1, iy)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.EXTERNAL) {
                drawRightCorner = true;
            }
        } else if (isInside(ix + 1, iy + 1)) {
            if (borderMode == BorderModeEnum.CENTERED || borderMode == BorderModeEnum.INTERNAL) {
                drawRightCorner = true;
            }
        }

        switch (borderMode) {
            case CENTERED:
                GraphicsUtils.drawLineHorizontal(gc, x, y, l, drawLeftCorner, drawRightCorner);
                break;
            case EXTERNAL:
                GraphicsUtils.drawLineHorizontalBorderBottom(gc, x, y, l, drawLeftCorner, drawRightCorner);
                break;
            case INTERNAL:
                GraphicsUtils.drawLineHorizontalBorderTop(gc, x, y, l, drawLeftCorner, drawRightCorner);
                break;
        }

    }

    protected boolean isInside(int ix, int iy) {
        if (!isValid(ix, iy)) {
            return false;
        }

        return this.borderCells[iy][ix].isInsideBorder;
    }

    protected boolean isValid(int ix, int iy) {
        if (iy < 0) {
            return false;
        }
        if (ix < 0) {
            return false;
        }
        if (iy >= this.borderCells.length) {
            return false;
        }
        if (ix >= this.borderCells[iy].length) {
            return false;
        }
        if (this.borderCells[iy][ix] == null) {
            return false;
        }
        if (this.borderCells[iy][ix].bounds == null) {
            return false;
        }
        return true;
    }
}
