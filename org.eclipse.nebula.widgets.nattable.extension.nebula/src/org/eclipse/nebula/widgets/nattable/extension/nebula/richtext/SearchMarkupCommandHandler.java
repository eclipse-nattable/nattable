/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.nebula.richtext;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.search.command.SearchCommand;

/**
 * TODO maybe we can create a default configuration that creates and registers
 * the following configuration.
 *
 * <pre>
 * MarkupDisplayConverter converter = new MarkupDisplayConverter();
 * SearchMarkupCommandHandler handler = new SearchMarkupCommandHandler();
 * handler.registerMarkupDisplayConverter(converter);
 * natTable.registerCommandHandler(handler);
 *
 * configRegistry.registerConfigAttribute(
 *         CellConfigAttributes.DISPLAY_CONVERTER,
 *         converter);
 * </pre>
 */
public class SearchMarkupCommandHandler implements ILayerCommandHandler<SearchCommand> {

    protected Collection<MarkupDisplayConverter> markupConverter = new ArrayList<>();

    protected String currentSearchValue;

    @Override
    public boolean doCommand(ILayer targetLayer, SearchCommand command) {
        for (MarkupDisplayConverter mdc : this.markupConverter) {
            mdc.unregisterMarkup(this.currentSearchValue);
            this.currentSearchValue = command.getSearchText();
            mdc.registerMarkup(
                    this.currentSearchValue,
                    "<span style=\"color:rgb(0, 0, 0);background-color:rgb(255, 255, 0)\">",
                    "</span>");
        }

        // don't consume the command as we need to trigger the search too
        return false;
    }

    public void registerMarkupDisplayConverter(MarkupDisplayConverter mdc) {
        this.markupConverter.add(mdc);
    }

    public void unregisterMarkupDisplayConverter(MarkupDisplayConverter mdc) {
        this.markupConverter.remove(mdc);
    }

    @Override
    public Class<SearchCommand> getCommandClass() {
        return SearchCommand.class;
    }
}
