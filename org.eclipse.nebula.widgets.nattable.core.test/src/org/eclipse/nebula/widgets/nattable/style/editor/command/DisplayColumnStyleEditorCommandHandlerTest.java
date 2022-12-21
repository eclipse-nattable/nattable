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
package org.eclipse.nebula.widgets.nattable.style.editor.command;

import static org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes.CELL_STYLE;
import static org.eclipse.nebula.widgets.nattable.style.DisplayMode.NORMAL;
import static org.eclipse.nebula.widgets.nattable.style.editor.command.DisplayColumnStyleEditorCommandHandler.USER_EDITED_COLUMN_STYLE_LABEL_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.editor.ColumnStyleEditorDialog;
import org.eclipse.nebula.widgets.nattable.test.fixture.CellStyleFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.PropertiesFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DisplayColumnStyleEditorCommandHandlerTest {

    private ColumnOverrideLabelAccumulator labelAccumulatorFixture;
    private NatTableFixture natTableFixture;
    private DisplayColumnStyleEditorCommand commandFixture;
    private DisplayColumnStyleEditorCommandHandler handlerUnderTest;
    private IConfigRegistry configRegistryFixture;

    @BeforeEach
    public void setup() {
        this.labelAccumulatorFixture = new ColumnOverrideLabelAccumulator(
                new DataLayerFixture());
        this.natTableFixture = new NatTableFixture();
        this.configRegistryFixture = this.natTableFixture.getConfigRegistry();
        this.commandFixture = new DisplayColumnStyleEditorCommand(this.natTableFixture,
                this.natTableFixture.getConfigRegistry(), 1, 1);

        final SelectionLayer selectionLayer = ((DummyGridLayerStack) this.natTableFixture
                .getLayer()).getBodyLayer().getSelectionLayer();
        this.handlerUnderTest = new DisplayColumnStyleEditorCommandHandler(
                selectionLayer, this.labelAccumulatorFixture, this.configRegistryFixture);
    }

    @Test
    public void doCommand() throws Exception {
        this.handlerUnderTest.dialog = new ColumnStyleEditorDialog(new Shell(),
                new CellStyleFixture());
        this.handlerUnderTest.applySelectedStyleToColumns(this.commandFixture,
                new int[] { 0 });

        Style selectedStyle = (Style) this.configRegistryFixture.getConfigAttribute(
                CELL_STYLE, NORMAL, this.handlerUnderTest.getConfigLabel(0));

        assertEquals(
                CellStyleFixture.TEST_BG_COLOR,
                selectedStyle
                        .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
        assertEquals(
                CellStyleFixture.TEST_FG_COLOR,
                selectedStyle
                        .getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));

        List<String> columnLableOverrides = this.handlerUnderTest.columnLabelAccumulator
                .getOverrides(Integer.valueOf(0));
        assertEquals(1, columnLableOverrides.size());
        assertEquals(USER_EDITED_COLUMN_STYLE_LABEL_PREFIX + "0",
                columnLableOverrides.get(0));
    }

    @Test
    public void parseColumnIndexFromKey() throws Exception {
        int i = this.handlerUnderTest
                .parseColumnIndexFromKey(".BODY.userDefinedColumnStyle.USER_EDITED_STYLE_FOR_INDEX_3.horizontalAlignment");
        assertEquals(3, i);

        i = this.handlerUnderTest
                .parseColumnIndexFromKey(".BODY.userDefinedColumnStyle.USER_EDITED_STYLE_FOR_INDEX_12.horizontalAlignment");
        assertEquals(12, i);
    }

    @Test
    public void saveStateForMultipleLabels() throws Exception {
        CellStyleFixture style1 = new CellStyleFixture(
                HorizontalAlignmentEnum.LEFT);
        CellStyleFixture style2 = new CellStyleFixture(
                HorizontalAlignmentEnum.RIGHT);

        this.handlerUnderTest.stylesToPersist.put("label1", style1);
        this.handlerUnderTest.stylesToPersist.put("label2", style2);

        PropertiesFixture propertiesFixture = new PropertiesFixture();
        this.handlerUnderTest.saveState("prefix", propertiesFixture);

        assertEquals(
                HorizontalAlignmentEnum.LEFT.name(),
                propertiesFixture
                        .getProperty("prefix.userDefinedColumnStyle.label1.style.horizontalAlignment"));
        assertEquals(
                HorizontalAlignmentEnum.RIGHT.name(),
                propertiesFixture
                        .getProperty("prefix.userDefinedColumnStyle.label2.style.horizontalAlignment"));
    }

    @Test
    public void shouldRemoveLabelFromPersistenceIfStyleIsCleared()
            throws Exception {
        this.handlerUnderTest.dialog = new ColumnStyleEditorDialog(new Shell(), null);
        this.handlerUnderTest.applySelectedStyleToColumns(this.commandFixture,
                new int[] { 0 });

        Style selectedStyle = (Style) this.configRegistryFixture.getConfigAttribute(
                CELL_STYLE, NORMAL, this.handlerUnderTest.getConfigLabel(0));

        DefaultNatTableStyleConfiguration defaultStyle = new DefaultNatTableStyleConfiguration();
        assertEquals(
                defaultStyle.bgColor,
                selectedStyle
                        .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
        assertEquals(
                defaultStyle.fgColor,
                selectedStyle
                        .getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));

        Properties properties = new Properties();
        this.handlerUnderTest.saveState("prefix", properties);

        assertEquals(0, properties.size());
    }

    @Test
    public void loadStateForMultipleLabels() throws Exception {
        PropertiesFixture propertiesFixture = new PropertiesFixture()
                .addStyleProperties(
                        "prefix.userDefinedColumnStyle.USER_EDITED_STYLE_FOR_INDEX_0")
                .addStyleProperties(
                        "prefix.userDefinedColumnStyle.USER_EDITED_STYLE_FOR_INDEX_1");

        this.handlerUnderTest.loadState("prefix", propertiesFixture);

        Style style = (Style) this.configRegistryFixture.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                "USER_EDITED_STYLE_FOR_INDEX_0");
        assertEquals(
                HorizontalAlignmentEnum.LEFT,
                style.getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT));

        style = (Style) this.configRegistryFixture.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                "USER_EDITED_STYLE_FOR_INDEX_1");
        assertEquals(VerticalAlignmentEnum.TOP,
                style.getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT));

    }

    @Test
    public void loadStateForMultipleMixedLabels() throws Exception {
        PropertiesFixture propertiesFixture = new PropertiesFixture()
                .addStyleProperties(
                        "prefix.userDefinedColumnStyle.USER_EDITED_STYLE_FOR_INDEX_0")
                .addStyleProperties(
                        "prefix.userDefinedColumnStyle.USER_EDITED_STYLE_FOR_INDEX_1")
                .addStyleProperties(
                        "prefix.userDefinedColumnStyle.USER_EDITED_STYLE");

        this.handlerUnderTest.loadState("prefix", propertiesFixture);

        Style style = (Style) this.configRegistryFixture.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                "USER_EDITED_STYLE_FOR_INDEX_0");
        assertEquals(
                HorizontalAlignmentEnum.LEFT,
                style.getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT));

        style = (Style) this.configRegistryFixture.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                "USER_EDITED_STYLE_FOR_INDEX_1");
        assertEquals(VerticalAlignmentEnum.TOP,
                style.getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT));

        style = (Style) this.configRegistryFixture.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                "USER_EDITED_STYLE");
        assertEquals(VerticalAlignmentEnum.TOP,
                style.getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT));
    }
}
