/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 453914
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.search.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.cell.CellDisplayConversionUtils;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.search.ISearchDirection;
import org.eclipse.nebula.widgets.nattable.search.action.SearchAction;
import org.eclipse.nebula.widgets.nattable.search.command.SearchCommand;
import org.eclipse.nebula.widgets.nattable.search.event.SearchEvent;
import org.eclipse.nebula.widgets.nattable.search.strategy.GridSearchStrategy;
import org.eclipse.nebula.widgets.nattable.search.strategy.ISearchStrategy;
import org.eclipse.nebula.widgets.nattable.search.strategy.SelectionSearchStrategy;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.ClearAllSelectionsCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Find Dialog. Borrows from
 * <code>org.eclipse.ui.texteditor.FindReplaceDialog</code>. Used internally by
 * {@link SearchAction}.
 */
public class SearchDialog extends Dialog {

    private class SelectionItem {
        String text;
        PositionCoordinate pos;

        SelectionItem(String text, PositionCoordinate pos) {
            this.text = text;
            this.pos = new PositionCoordinate(pos);
        }
    }

    private NatTable natTable;
    private SelectionLayer selectionLayer;
    private Comparator<?> comparator;

    /**
     * A stack for recording selections. In incremental mode, the stack is used
     * when the search term is shortened. In non-incremental mode, the stack
     * contains only the most recent selection.
     */
    private Stack<SelectionItem> selections = new Stack<SelectionItem>();

    // Dialog settings
    private IDialogSettings originalSettings;
    private IDialogSettings dialogSettings;
    private IDialogSettings dialogBounds;
    private Rectangle dialogPositionValue;

    // Find Combo box
    private Combo findCombo;
    private List<String> findHistory = new ArrayList<String>(5);

    // Direction radio
    private Button forwardButton;
    private boolean forwardValue = true;

    // Scope radio
    private Button allButton;
    private boolean allValue = true;
    private Button selectionButton;

    // Options and cached values.
    private Button caseSensitiveButton;
    private boolean caseSensitiveValue;
    private Button wrapSearchButton;
    private boolean wrapSearchValue = true;
    private Button wholeWordButton;
    private boolean wholeWordValue;
    private Button incrementalButton;
    private boolean incrementalValue;
    private Button columnFirstButton;
    private boolean columnFirstValue;
    // TODO
    // private Button includeCollapsedButton;
    // private boolean includeCollapsedValue = true;
    private Button regexButton;
    private boolean regexValue;

    // Status label
    private Label statusLabel;

    // Find button
    private Button findButton;
    private ModifyListener findComboModifyListener;

    public SearchDialog(Shell shell, Comparator<?> comparator, int style) {
        super(shell);
        this.comparator = comparator;
        setShellStyle(getShellStyle() ^ style | SWT.MODELESS);
        setBlockOnOpen(false);
    }

    public void setInput(NatTable natTable, IDialogSettings settings) {
        if (natTable != null && natTable.equals(this.natTable)) {
            return;
        }
        this.natTable = natTable;
        if (natTable != null) {
            ILayer result = findSelectionLayer(this.natTable.getLayer());
            if (result != null && result instanceof SelectionLayer) {
                this.selectionLayer = (SelectionLayer) result;
                if (this.findButton != null && !this.findButton.isDisposed()) {
                    this.findButton.setEnabled(true);
                }
            }
        } else {
            this.selectionLayer = null;
            if (this.findButton != null && !this.findButton.isDisposed()) {
                this.findButton.setEnabled(false);
            }
        }

        this.originalSettings = settings;
        if (settings == null) {
            this.dialogSettings = null;
            this.dialogBounds = null;
        } else {
            this.dialogSettings = settings.getSection(getClass().getName());
            if (this.dialogSettings == null) {
                this.dialogSettings = settings.addNewSection(getClass().getName());
            }
            String boundsName = getClass().getName() + "_dialogBounds"; //$NON-NLS-1$
            this.dialogBounds = settings.getSection(boundsName);
            if (this.dialogBounds == null) {
                this.dialogBounds = settings.addNewSection(boundsName);
            }
        }
        readConfiguration();
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    /**
     * @since 1.5
     */
    public boolean isModal() {
        return (getShellStyle() & SWT.APPLICATION_MODAL) != 0;
    }

    /**
     * @since 1.5
     */
    public NatTable getNatTable() {
        return this.natTable;
    }

    /**
     * @since 1.5
     */
    public IDialogSettings getOriginalDialogSettings() {
        return this.originalSettings;
    }

    @Override
    public void create() {

        super.create();
        getShell().setText(Messages.getString("Search.find")); //$NON-NLS-1$
        // set dialog position
        if (this.dialogPositionValue != null) {
            getShell().setBounds(this.dialogPositionValue);
        }

        this.findCombo.removeModifyListener(this.findComboModifyListener);
        updateCombo(this.findCombo, this.findHistory);
        this.findCombo.addModifyListener(this.findComboModifyListener);

        // search SelectionLayer in layer stack
        ILayer result = findSelectionLayer(this.natTable.getLayer());
        if (result != null && result instanceof SelectionLayer) {
            this.selectionLayer = (SelectionLayer) result;
        }

        // Pick the user's selection, if possible
        PositionCoordinate pos = getPosition();
        final String text = getTextForSelection(pos);
        this.selections.push(new SelectionItem(text, pos));
        this.findCombo.setText(text);
    }

    private ILayer findSelectionLayer(ILayer layer) {
        if (layer == null || layer instanceof SelectionLayer) {
            return layer;
        } else if (layer instanceof CompositeLayer) {
            // if the layer is a CompositeLayer, search for the SelectionLayer
            // in every region as the SelectionLayer is typically placed in the
            // bottom/right most region (e.g. the body in a grid, the search is
            // performed backwards
            CompositeLayer composite = (CompositeLayer) layer;
            for (int x = composite.getLayoutXCount(); x >= 0; x--) {
                for (int y = composite.getLayoutYCount(); y >= 0; y--) {
                    ILayer childStack = composite.getChildLayerByLayoutCoordinate(x, y);
                    ILayer result = findSelectionLayer(childStack);
                    if (result instanceof SelectionLayer) {
                        return result;
                    }
                }
            }
            return null;
        } else {
            return findSelectionLayer(layer.getUnderlyingLayerByPosition(0, 0));
        }
    }

    private String getTextForSelection(PositionCoordinate selection) {
        if (this.selectionLayer == null
                || selection == null
                || selection.columnPosition == SelectionLayer.NO_SELECTION) {
            return ""; //$NON-NLS-1$
        }
        final ILayerCell cell = this.selectionLayer.getCellByPosition(selection.columnPosition, selection.rowPosition);
        if (cell == null || cell.getDataValue() == null) {
            return ""; //$NON-NLS-1$
        } else if (this.natTable != null) {
            return CellDisplayConversionUtils.convertDataType(cell, this.natTable.getConfigRegistry());
        }
        return cell.getDataValue().toString();
    }

    @Override
    public boolean close() {
        storeSettings();
        return super.close();
    }

    /**
     * Stores the current state in the dialog settings.
     */
    private void storeSettings() {
        if (getShell() == null || getShell().isDisposed()) {
            return;
        }
        this.dialogPositionValue = getShell().getBounds();
        this.forwardValue = this.forwardButton.getSelection();
        this.allValue = this.allButton.getSelection();
        this.caseSensitiveValue = this.caseSensitiveButton.getSelection();
        this.wrapSearchValue = this.wrapSearchButton.getSelection();
        this.wholeWordValue = this.wholeWordButton.getSelection();
        this.incrementalValue = this.incrementalButton.getSelection();
        this.regexValue = this.regexButton.getSelection();
        // TODO
        // includeCollapsedValue = includeCollapsedButton.getSelection();
        this.columnFirstValue = this.columnFirstButton.getSelection();
        writeConfiguration();
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
                .grab(true, false).applyTo(createInputPanel(composite));
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
                .grab(true, false).applyTo(createOptionsPanel(composite));
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
                .grab(true, true).applyTo(createStatusPanel(composite));
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM)
                .grab(true, true).applyTo(createButtonSection(composite));
        return composite;
    }

    private Composite createStatusPanel(Composite composite) {
        Composite panel = new Composite(composite, SWT.NONE);
        panel.setLayout(new GridLayout(1, false));
        this.statusLabel = new Label(panel, SWT.LEFT);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
                .grab(true, false).applyTo(this.statusLabel);
        return panel;
    }

    private Composite createButtonSection(Composite composite) {

        Composite panel = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        panel.setLayout(layout);

        Label label = new Label(panel, SWT.LEFT);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
                .grab(true, false).applyTo(label);

        this.findButton = createButton(
                panel,
                IDialogConstants.CLIENT_ID,
                Messages.getString("Search.findButtonLabel"), false); //$NON-NLS-1$
        int buttonWidth = getButtonWidthHint(this.findButton);
        GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM)
                .grab(false, false).hint(buttonWidth, SWT.DEFAULT)
                .applyTo(this.findButton);

        this.findButton.setEnabled(false);
        getShell().setDefaultButton(this.findButton);

        this.findButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                doFind();
            }
        });

        Button closeButton = createButton(panel, IDialogConstants.CANCEL_ID,
                Messages.getString("Search.closeButtonLabel"), false); //$NON-NLS-1$
        buttonWidth = getButtonWidthHint(closeButton);
        GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM)
                .grab(false, false).hint(buttonWidth, SWT.DEFAULT)
                .applyTo(closeButton);

        return panel;
    }

    public static int getButtonWidthHint(Button button) {
        button.setFont(JFaceResources.getDialogFont());
        PixelConverter converter = new PixelConverter(button);
        int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
    }

    private Composite createInputPanel(final Composite composite) {
        final Composite row = new Composite(composite, SWT.NONE);
        row.setLayout(new GridLayout(2, false));

        final Label findLabel = new Label(row, SWT.NONE);
        findLabel.setText(Messages.getString("Search.findLabel") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
        GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER)
                .applyTo(findLabel);

        this.findCombo = new Combo(row, SWT.DROP_DOWN | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(this.findCombo);
        this.findComboModifyListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (SearchDialog.this.allValue && SearchDialog.this.incrementalButton.isEnabled()
                        && SearchDialog.this.incrementalButton.getSelection()) {
                    doIncrementalFind();
                }
                SearchDialog.this.findButton.setEnabled(SearchDialog.this.findCombo.getText().length() > 0 && SearchDialog.this.selectionLayer != null);
            }
        };
        this.findCombo.addModifyListener(this.findComboModifyListener);
        this.findCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (SearchDialog.this.findButton.isEnabled()) {
                    doFind();
                }
            }
        });

        return row;
    }

    private Composite createOptionsPanel(final Composite composite) {
        final Composite row = new Composite(composite, SWT.NONE);
        row.setLayout(new GridLayout(2, true));

        final Group directionGroup = new Group(row, SWT.SHADOW_ETCHED_IN);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(directionGroup);
        directionGroup.setText(Messages.getString("Search.direction")); //$NON-NLS-1$
        RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
        rowLayout.marginHeight = rowLayout.marginWidth = 3;
        directionGroup.setLayout(rowLayout);
        this.forwardButton = new Button(directionGroup, SWT.RADIO);
        this.forwardButton.setText(Messages.getString("Search.forwardButtonLabel")); //$NON-NLS-1$
        this.forwardButton.setSelection(this.forwardValue);
        this.forwardButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (SearchDialog.this.incrementalButton.getSelection()) {
                    resetIncrementalSelections();
                }
            }
        });
        final Button backwardButton = new Button(directionGroup, SWT.RADIO);
        backwardButton.setText(Messages.getString("Search.backwardButtonLabel")); //$NON-NLS-1$
        backwardButton.setSelection(!this.forwardValue);

        final Group scopeGroup = new Group(row, SWT.SHADOW_ETCHED_IN);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(scopeGroup);
        scopeGroup.setText(Messages.getString("Search.scope")); //$NON-NLS-1$
        rowLayout = new RowLayout(SWT.VERTICAL);
        rowLayout.marginHeight = rowLayout.marginWidth = 3;
        scopeGroup.setLayout(rowLayout);
        this.allButton = new Button(scopeGroup, SWT.RADIO);
        this.allButton.setText(Messages.getString("Search.allLabel")); //$NON-NLS-1$
        this.allButton.setSelection(this.allValue);
        this.allButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtons();
            }
        });
        this.selectionButton = new Button(scopeGroup, SWT.RADIO);
        this.selectionButton.setText(Messages.getString("Search.selectionLabel")); //$NON-NLS-1$
        this.selectionButton.setSelection(!this.allValue);

        final Group optionsGroup = new Group(row, SWT.SHADOW_ETCHED_IN);
        GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(optionsGroup);
        optionsGroup.setText(Messages.getString("Search.options")); //$NON-NLS-1$
        optionsGroup.setLayout(new GridLayout(2, true));
        this.caseSensitiveButton = new Button(optionsGroup, SWT.CHECK);
        this.caseSensitiveButton.setText(Messages.getString("Search.caseSensitiveButtonLabel")); //$NON-NLS-1$
        this.caseSensitiveButton.setSelection(this.caseSensitiveValue);
        this.wrapSearchButton = new Button(optionsGroup, SWT.CHECK);
        this.wrapSearchButton.setText(Messages.getString("Search.wrapSearchButtonLabel")); //$NON-NLS-1$
        this.wrapSearchButton.setSelection(this.wrapSearchValue);
        this.wholeWordButton = new Button(optionsGroup, SWT.CHECK);
        this.wholeWordButton.setText(Messages.getString("Search.wholeWordButtonLabel")); //$NON-NLS-1$
        this.wholeWordButton.setSelection(this.wholeWordValue);
        this.wholeWordButton.setEnabled(!this.regexValue);
        this.incrementalButton = new Button(optionsGroup, SWT.CHECK);
        this.incrementalButton.setText(Messages.getString("Search.incrementalButtonLabel")); //$NON-NLS-1$
        this.incrementalButton.setSelection(this.incrementalValue);
        this.incrementalButton.setEnabled(!this.regexValue);
        this.incrementalButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (SearchDialog.this.incrementalButton.getSelection()) {
                    resetIncrementalSelections();
                }
            }
        });
        this.regexButton = new Button(optionsGroup, SWT.CHECK);
        this.regexButton.setText(Messages.getString("Search.regexButtonLabel")); //$NON-NLS-1$
        this.regexButton.setSelection(this.regexValue);
        this.regexButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtons();
            }
        });
        this.columnFirstButton = new Button(optionsGroup, SWT.CHECK);
        this.columnFirstButton.setText(Messages.getString("Search.columnFirstLabel")); //$NON-NLS-1$
        this.columnFirstButton.setSelection(this.columnFirstValue);
        // TODO
        // includeCollapsedButton = new Button(optionsGroup, SWT.CHECK);
        // includeCollapsedButton.setText(Messages.getString("Search.includeCollapsedLabel"));
        // //$NON-NLS-1$
        // includeCollapsedButton.setSelection(includeCollapsedValue);

        return row;
    }

    private void doFind() {
        doFindInit();
        doFind0(false, this.findCombo.getText());
    }

    protected void doIncrementalFind() {
        doFindInit();
        final String text = this.findCombo.getText();
        String lastText = this.selections.peek().text;
        if (lastText.startsWith(text)) {
            while (this.selections.size() > 1
                    && this.selections.peek().text.length() > text.length()) {
                this.selections.pop();
            }
            doSelect(this.selections.peek());
        } else {
            int pos;
            if (text.startsWith(lastText)) {
                pos = lastText.length();
            } else {
                pos = 0;
                resetIncrementalSelections();
            }
            // Incremental search is performed with a loop to properly
            // handle a paste, as if each character of the paste were typed
            // separately, unless the search is whole word.
            if (this.wholeWordValue) {
                doFind0(true, text);
            } else {
                for (int i = pos, n = text.length(); i < n; ++i) {
                    doFind0(true, text.substring(0, i + 1));
                }
            }
        }
    }

    private void doFindInit() {
        this.statusLabel.setText(""); //$NON-NLS-1$
        this.statusLabel.setForeground(null);
        if (this.selectionLayer != null) {
            // If the current selection is different, the user must have
            // clicked a new selection, and we need to update to that.
            PositionCoordinate pos = getPosition();
            if (!pos.equals(this.selections.peek().pos)) {
                this.selections.clear();
                this.selections.push(new SelectionItem(getTextForSelection(pos), pos));
            }
        }
    }

    private void doSelect(SelectionItem selection) {
        ILayerCommand command;
        if (selection.pos.columnPosition != SelectionLayer.NO_SELECTION) {
            command = createSelectCellCommand(selection.pos);
        } else {
            command = new ClearAllSelectionsCommand();
        }
        final ILayerCommand finalCommand = command;
        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
            @Override
            public void run() {
                SearchDialog.this.natTable.doCommand(finalCommand);
            }
        });
    }

    private class SearchEventListener implements ILayerListener {
        private PositionCoordinate pos;

        @Override
        public void handleLayerEvent(ILayerEvent event) {
            if (!(event instanceof SearchEvent)) {
                return;
            }
            SearchEvent searchEvent = (SearchEvent) event;
            this.pos = searchEvent.getCellCoordinate();
        }
    }

    private void doFind0(final boolean isIncremental, final String text) {

        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {

            @Override
            public void run() {
                PositionCoordinate previous =
                        new PositionCoordinate(SearchDialog.this.selections.peek().pos);
                try {
                    final SearchCommand searchCommand = createSearchCommand(text, isIncremental);
                    final SearchEventListener searchEventListener = new SearchEventListener();
                    searchCommand.setSearchEventListener(searchEventListener);
                    SearchDialog.this.natTable.doCommand(searchCommand);
                    if (searchEventListener.pos == null) {
                        // Beep and show status if not found
                        SearchDialog.this.statusLabel.setText(Messages.getString("Search.textNotFound")); //$NON-NLS-1$
                        getShell().getDisplay().beep();
                    } else {
                        SelectionItem selection = new SelectionItem(text, searchEventListener.pos);
                        SearchDialog.this.selections.push(selection);
                        if (!isIncremental) {
                            resetIncrementalSelections();
                        }
                        // Beep and show status if wrapped
                        if (previous != null && previous.columnPosition > -1) {
                            int columnDelta = selection.pos.columnPosition - previous.columnPosition;
                            int rowDelta = selection.pos.rowPosition - previous.rowPosition;
                            if (!SearchDialog.this.forwardValue) {
                                columnDelta = -columnDelta;
                                rowDelta = -rowDelta;
                            }
                            int primaryDelta = SearchDialog.this.columnFirstValue ? columnDelta : rowDelta;
                            int secondaryDelta = SearchDialog.this.columnFirstValue ? rowDelta : columnDelta;
                            if (primaryDelta < 0 || !isIncremental
                                    && primaryDelta == 0 && secondaryDelta <= 0) {
                                SearchDialog.this.statusLabel.setText(Messages.getString("Search.wrappedSearch")); //$NON-NLS-1$
                                getShell().getDisplay().beep();
                            }
                        }
                    }
                    if (!isIncremental) {
                        updateFindHistory();
                    }
                } catch (PatternSyntaxException e) {
                    SearchDialog.this.statusLabel.setText(e.getLocalizedMessage());
                    SearchDialog.this.statusLabel.setForeground(JFaceColors.getErrorText(SearchDialog.this.statusLabel.getDisplay()));
                    getShell().getDisplay().beep();
                }
            }
        });
    }

    private PositionCoordinate getPosition() {
        if (this.selectionLayer == null) {
            return new PositionCoordinate(
                    null,
                    SelectionLayer.NO_SELECTION,
                    SelectionLayer.NO_SELECTION);
        }
        // The SelectionLayer keeps its anchor even if it has no selection.
        // Seems wrong to me, so here I clear out the anchor.
        PositionCoordinate pos = new PositionCoordinate(this.selectionLayer.getSelectionAnchor());
        if (this.selectionLayer.getSelectedCellPositions().length == 0
                && pos.rowPosition != SelectionLayer.NO_SELECTION) {
            this.selectionLayer.clear(false);
            pos = new PositionCoordinate(this.selectionLayer.getSelectionAnchor());
        }
        return pos;
    }

    private void resetIncrementalSelections() {
        SelectionItem selection = this.selections.peek();
        this.selections.clear();
        this.selections.push(selection);
    }

    private SelectCellCommand createSelectCellCommand(PositionCoordinate selection) {
        SelectCellCommand selectCellCommand =
                new SelectCellCommand(
                        selection.getLayer(),
                        selection.columnPosition,
                        selection.rowPosition,
                        false,
                        false);
        selectCellCommand.setForcingEntireCellIntoViewport(true);
        return selectCellCommand;
    }

    private SearchCommand createSearchCommand(String text, boolean isIncremental) {
        this.forwardValue = this.forwardButton.getSelection();
        this.allValue = this.allButton.getSelection();
        this.caseSensitiveValue = this.caseSensitiveButton.getSelection();
        this.wrapSearchValue = this.wrapSearchButton.getSelection();
        this.wholeWordValue = this.wholeWordButton.getSelection();
        this.incrementalValue = this.incrementalButton.getSelection();
        this.regexValue = this.regexButton.getSelection();
        // TODO
        // includeCollapsedValue = includeCollapsedButton.getSelection();
        this.columnFirstValue = this.columnFirstButton.getSelection();

        String searchDirection = this.forwardValue ? ISearchDirection.SEARCH_FORWARD : ISearchDirection.SEARCH_BACKWARDS;
        ISearchStrategy searchStrategy;
        if (this.allValue) {
            searchStrategy = new GridSearchStrategy(this.natTable.getConfigRegistry(), true, this.columnFirstValue);
        } else {
            searchStrategy = new SelectionSearchStrategy(this.natTable.getConfigRegistry(), this.columnFirstValue);
        }
        return new SearchCommand(
                text,
                this.natTable,
                searchStrategy,
                searchDirection,
                this.wrapSearchValue,
                this.caseSensitiveValue,
                !this.regexValue && this.wholeWordValue,
                !this.regexValue && this.allValue && isIncremental, this.regexValue,
                // TODO
                // includeCollapsedValue, comparator);
                false,
                this.comparator);
    }

    /**
     * Called after executed find action to update the history.
     */
    private void updateFindHistory() {
        this.findCombo.removeModifyListener(this.findComboModifyListener);
        updateHistory(this.findCombo, this.findHistory);
        this.findCombo.addModifyListener(this.findComboModifyListener);
    }

    /**
     * Updates the combo with the history.
     *
     * @param findCombo
     *            to be updated
     * @param history
     *            to be put into the combo
     */
    private static void updateHistory(Combo findCombo, List<String> history) {
        String findString = findCombo.getText();
        int index = history.indexOf(findString);
        if (index == 0) {
            return;
        }
        if (index != -1) {
            history.remove(index);
        }
        history.add(0, findString);
        Point selection = findCombo.getSelection();
        updateCombo(findCombo, history);
        findCombo.setText(findString);
        findCombo.setSelection(selection);
    }

    /**
     * Updates the given combo with the given content.
     *
     * @param combo
     *            combo to be updated
     * @param content
     *            to be put into the combo
     */
    private static void updateCombo(Combo combo, List<String> content) {
        combo.removeAll();
        for (int i = 0; i < content.size(); i++) {
            combo.add(content.get(i).toString());
        }
    }

    private void updateButtons() {
        final boolean regex = this.regexButton.getSelection();
        final boolean allMode = this.allButton.getSelection();
        this.wholeWordButton.setEnabled(!regex);
        this.incrementalButton.setEnabled(!regex && allMode);
    }

    /**
     * Returns the dialog settings object used to share state between several
     * find/replace dialogs.
     *
     * @return the dialog settings to be used
     */
    private IDialogSettings getDialogSettings() {
        return this.dialogSettings;
    }

    /*
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
     */
    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        return this.dialogBounds;
    }

    /*
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsStrategy()
     */
    @Override
    protected int getDialogBoundsStrategy() {
        return DIALOG_PERSISTLOCATION | DIALOG_PERSISTSIZE;
    }

    /**
     * Initializes itself from the dialog settings with the same state as at the
     * previous invocation.
     */
    private void readConfiguration() {
        IDialogSettings s = getDialogSettings();
        if (s == null) {
            return;
        }

        this.wrapSearchValue = s.get("wrap") == null || s.getBoolean("wrap"); //$NON-NLS-1$ //$NON-NLS-2$
        this.caseSensitiveValue = s.getBoolean("casesensitive"); //$NON-NLS-1$
        this.wholeWordValue = s.getBoolean("wholeword"); //$NON-NLS-1$
        this.incrementalValue = s.getBoolean("incremental"); //$NON-NLS-1$
        this.regexValue = s.getBoolean("isRegEx"); //$NON-NLS-1$
        // TODO
        // includeCollapsedValue = s.get("includeCollapsed") == null
        // //$NON-NLS-1$
        // || s.getBoolean("includeCollapsed"); //$NON-NLS-1$
        this.columnFirstValue = s.getBoolean("columnFirst"); //$NON-NLS-1$

        String[] findHistoryConfig = s.getArray("findhistory"); //$NON-NLS-1$
        if (findHistoryConfig != null) {
            this.findHistory.clear();
            for (String history : findHistoryConfig) {
                this.findHistory.add(history);
            }
        }
    }

    /**
     * Stores its current configuration in the dialog store.
     */
    private void writeConfiguration() {
        IDialogSettings s = getDialogSettings();
        if (s == null) {
            return;
        }

        s.put("wrap", this.wrapSearchValue); //$NON-NLS-1$
        s.put("casesensitive", this.caseSensitiveValue); //$NON-NLS-1$
        s.put("wholeword", this.wholeWordValue); //$NON-NLS-1$
        s.put("incremental", this.incrementalValue); //$NON-NLS-1$
        s.put("isRegEx", this.regexValue); //$NON-NLS-1$
        // TODO
        // s.put("includeCollapsed", includeCollapsedValue); //$NON-NLS-1$
        s.put("columnFirst", this.columnFirstValue); //$NON-NLS-1$

        String findString = this.findCombo.getText();
        if (findString.length() > 0) {
            this.findHistory.add(0, findString);
        }
        writeHistory(this.findHistory, s, "findhistory"); //$NON-NLS-1$
    }

    /**
     * Writes the given history into the given dialog store.
     *
     * @param history
     *            the history
     * @param settings
     *            the dialog settings
     * @param sectionName
     *            the section name
     */
    private void writeHistory(List<String> history, IDialogSettings settings, String sectionName) {
        int itemCount = history.size();
        Set<String> distinctItems = new HashSet<String>(itemCount);
        for (int i = 0; i < itemCount; i++) {
            String item = history.get(i);
            if (distinctItems.contains(item)) {
                history.remove(i--);
                itemCount--;
            } else {
                distinctItems.add(item);
            }
        }

        while (history.size() > 8)
            history.remove(8);

        String[] names = new String[history.size()];
        history.toArray(names);
        settings.put(sectionName, names);

    }
}
