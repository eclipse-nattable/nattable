/*******************************************************************************
 * Copyright (c) 2024 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.poi;

import java.util.HashMap;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.nebula.widgets.nattable.export.FileOutputStreamProvider;
import org.eclipse.nebula.widgets.nattable.export.IOutputStreamProvider;
import org.eclipse.swt.graphics.Color;

public class XSSFExcelExporter extends PoiExcelExporter {

    private HashMap<Color, XSSFColor> colorIndex = new HashMap<>();

    public XSSFExcelExporter() {
        super(new FileOutputStreamProvider("table_export.xlsx", new String[] { "Excel Workbook (*.xlsx)" }, new String[] { "*.xlsx" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public XSSFExcelExporter(IOutputStreamProvider outputStreamProvider) {
        super(outputStreamProvider);
    }

    @Override
    protected Workbook createWorkbook() {
        this.colorIndex = new HashMap<>();
        return new XSSFWorkbook();
    }

    @Override
    protected void setFillForegroundColor(CellStyle xlCellStyle, Color swtColor) {
        xlCellStyle.setFillForegroundColor(getColor(swtColor));
    }

    @Override
    protected void setFontColor(Font xlFont, Color swtColor) {
        ((XSSFFont) xlFont).setColor(getColor(swtColor));
    }

    private XSSFColor getColor(Color swtColor) {
        return this.colorIndex.computeIfAbsent(swtColor, c -> {
            byte[] rgb = {
                    (byte) swtColor.getRed(),
                    (byte) swtColor.getGreen(),
                    (byte) swtColor.getBlue()
            };
            XSSFColor xc = new XSSFColor(rgb, null);
            return xc;
        });
    }

}
