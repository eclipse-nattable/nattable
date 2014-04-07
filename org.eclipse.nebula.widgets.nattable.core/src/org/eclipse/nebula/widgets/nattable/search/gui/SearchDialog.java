/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
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
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
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
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
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
 * Find Dialog. Borrows from <code>org.eclipse.ui.texteditor.FindReplaceDialog</code>.
 * Used internally by {@link SearchAction}.
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
	 * A stack for recording selections. In incremental mode,
	 * the stack is used when the search term is shortened. 
	 * In non-incremental mode, the stack contains only the
	 * most recent selection.
	 */
	private Stack<SelectionItem> selections = new Stack<SelectionItem>();
	
	// Dialog settings
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
//	private Button includeCollapsedButton;
//	private boolean includeCollapsedValue = true;
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
		if (natTable.equals(this.natTable)) {
			return;
		}
		this.natTable = natTable;
		if (settings == null) {
			dialogSettings = null;
			dialogBounds = null;
		} else {
			dialogSettings = settings.getSection(getClass().getName());
			if (dialogSettings == null) {
				dialogSettings = settings.addNewSection(getClass().getName());
			}
			String boundsName = getClass().getName() + "_dialogBounds"; //$NON-NLS-1$
			dialogBounds = settings.getSection(boundsName);
			if (dialogBounds == null) {
				dialogBounds = settings.addNewSection(boundsName);
			}
		}
		readConfiguration();
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	public void create() {

		super.create();
		getShell().setText(Messages.getString("Search.find")); //$NON-NLS-1$
		// set dialog position
		if (dialogPositionValue != null) {
			getShell().setBounds(dialogPositionValue);
		}

		findCombo.removeModifyListener(findComboModifyListener);
		updateCombo(findCombo, findHistory);
		findCombo.addModifyListener(findComboModifyListener);

		// Try to find a SelectionLayer
		selectionLayer = null;
		ILayer topLayer = natTable.getUnderlyingLayerByPosition(0, 0);
		if (topLayer instanceof GridLayer) {
			ILayer bodyLayer = ((GridLayer) topLayer).getBodyLayer();
			bodyLayer.getUnderlyingLayersByColumnPosition(0);
			if (bodyLayer instanceof DefaultBodyLayerStack) {
				selectionLayer = ((DefaultBodyLayerStack) bodyLayer).getSelectionLayer();
			} else if (bodyLayer instanceof ViewportLayer) {
				ILayer underlyingLayer = bodyLayer.getUnderlyingLayerByPosition(0, 0);
				if (underlyingLayer instanceof SelectionLayer) {
					selectionLayer = (SelectionLayer) underlyingLayer;
				}
			}
		}

		// Pick the user's selection, if possible
		PositionCoordinate pos = getPosition();
		final String text = getTextForSelection(pos);
		selections.push(new SelectionItem(text, pos));
		findCombo.setText(text);
	}

	private String getTextForSelection(PositionCoordinate selection) {
		if (selectionLayer == null || selection == null || selection.columnPosition == SelectionLayer.NO_SELECTION) {
			return ""; //$NON-NLS-1$
		}
		final ILayerCell cell = selectionLayer.getCellByPosition(selection.columnPosition,
				selection.rowPosition);
		return cell == null ? "" : cell.getDataValue().toString(); //$NON-NLS-1$
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
		dialogPositionValue = getShell().getBounds();
		forwardValue = forwardButton.getSelection();
		allValue = allButton.getSelection();
		caseSensitiveValue = caseSensitiveButton.getSelection();
		wrapSearchValue = wrapSearchButton.getSelection();
		wholeWordValue = wholeWordButton.getSelection();
		incrementalValue = incrementalButton.getSelection();
		regexValue = regexButton.getSelection();
		// TODO
//		includeCollapsedValue = includeCollapsedButton.getSelection();
		columnFirstValue = columnFirstButton.getSelection();
		writeConfiguration();
	}

	@Override
	protected Control createContents(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1,false));
		GridDataFactory.fillDefaults().grab(true, true)
			.applyTo(composite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false)
			.applyTo(createInputPanel(composite));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false)
			.applyTo(createOptionsPanel(composite));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
			.applyTo(createStatusPanel(composite));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM).grab(true, true)
			.applyTo(createButtonSection(composite));
		return composite;
	}

	private Composite createStatusPanel(Composite composite) {
		Composite panel = new Composite(composite, SWT.NONE);
		panel.setLayout(new GridLayout(1, false));
		statusLabel = new Label(panel, SWT.LEFT);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(statusLabel);
		return panel;
	}
	
	private Composite createButtonSection(Composite composite) {

		Composite panel = new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout(1,false);
		panel.setLayout(layout);
		
		Label label = new Label(panel, SWT.LEFT);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(label);
		
		findButton = createButton(panel, IDialogConstants.CLIENT_ID, Messages.getString("Search.findButtonLabel"), false); //$NON-NLS-1$
		int buttonWidth = getButtonWidthHint(findButton);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM).grab(false, false).hint(buttonWidth, SWT.DEFAULT).applyTo(findButton);
		
		findButton.setEnabled(false);
		getShell().setDefaultButton(findButton);
		
		findButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doFind();
			}
		});
 		
		Button closeButton = createButton(panel, IDialogConstants.CANCEL_ID,
				Messages.getString("Search.closeButtonLabel"), false); //$NON-NLS-1$
		buttonWidth = getButtonWidthHint(closeButton);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM)
				.grab(false, false).hint(buttonWidth, SWT.DEFAULT).applyTo(closeButton);

		return panel;
	}

	public static int getButtonWidthHint(Button button) {
		button.setFont(JFaceResources.getDialogFont());
		PixelConverter converter= new PixelConverter(button);
		int widthHint= converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}
	
	private Composite createInputPanel(final Composite composite) {
		final Composite row = new Composite(composite, SWT.NONE);
		row.setLayout(new GridLayout(2,false));
		
		final Label findLabel = new Label(row, SWT.NONE);
		findLabel.setText(Messages.getString("Search.findLabel") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(findLabel);
		
		findCombo = new Combo(row, SWT.DROP_DOWN | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(findCombo);
		findComboModifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (allValue && incrementalButton.isEnabled() && incrementalButton.getSelection()) {
					doIncrementalFind();
				}
				findButton.setEnabled(findCombo.getText().length() > 0);
			}
		};
		findCombo.addModifyListener(findComboModifyListener);
		findCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (findButton.isEnabled()) {
					doFind();
				}
			}
		});
		
		return row;
	}
	
	private Composite createOptionsPanel(final Composite composite) {
		final Composite row = new Composite(composite, SWT.NONE);
		row.setLayout(new GridLayout(2,true));
		
		final Group directionGroup = new Group(row, SWT.SHADOW_ETCHED_IN);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(directionGroup);
		directionGroup.setText(Messages.getString("Search.direction")); //$NON-NLS-1$
		RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.marginHeight = rowLayout.marginWidth = 3;
		directionGroup.setLayout(rowLayout);
		forwardButton = new Button(directionGroup, SWT.RADIO);
		forwardButton.setText(Messages.getString("Search.forwardButtonLabel")); //$NON-NLS-1$
		forwardButton.setSelection(forwardValue);
		forwardButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (incrementalButton.getSelection()) {
					resetIncrementalSelections();
				}
			}
		});
		final Button backwardButton = new Button(directionGroup, SWT.RADIO);
		backwardButton.setText(Messages.getString("Search.backwardButtonLabel")); //$NON-NLS-1$
		backwardButton.setSelection(!forwardValue);
		
		final Group scopeGroup = new Group(row, SWT.SHADOW_ETCHED_IN);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(scopeGroup);
		scopeGroup.setText(Messages.getString("Search.scope")); //$NON-NLS-1$
		rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.marginHeight = rowLayout.marginWidth = 3;
		scopeGroup.setLayout(rowLayout);
		allButton = new Button(scopeGroup, SWT.RADIO);
		allButton.setText(Messages.getString("Search.allLabel")); //$NON-NLS-1$
		allButton.setSelection(allValue);
		allButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
		selectionButton = new Button(scopeGroup, SWT.RADIO);
		selectionButton.setText(Messages.getString("Search.selectionLabel")); //$NON-NLS-1$
		selectionButton.setSelection(!allValue);

		final Group optionsGroup = new Group(row, SWT.SHADOW_ETCHED_IN);
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(optionsGroup);
		optionsGroup.setText(Messages.getString("Search.options")); //$NON-NLS-1$
		optionsGroup.setLayout(new GridLayout(2, true));
		caseSensitiveButton = new Button(optionsGroup, SWT.CHECK);
		caseSensitiveButton.setText(Messages.getString("Search.caseSensitiveButtonLabel")); //$NON-NLS-1$
		caseSensitiveButton.setSelection(caseSensitiveValue);
		wrapSearchButton = new Button(optionsGroup, SWT.CHECK);
		wrapSearchButton.setText(Messages.getString("Search.wrapSearchButtonLabel")); //$NON-NLS-1$
		wrapSearchButton.setSelection(wrapSearchValue);
		wholeWordButton = new Button(optionsGroup, SWT.CHECK);
		wholeWordButton.setText(Messages.getString("Search.wholeWordButtonLabel")); //$NON-NLS-1$
		wholeWordButton.setSelection(wholeWordValue);
		wholeWordButton.setEnabled(!regexValue);
		incrementalButton = new Button(optionsGroup, SWT.CHECK);
		incrementalButton.setText(Messages.getString("Search.incrementalButtonLabel")); //$NON-NLS-1$
		incrementalButton.setSelection(incrementalValue);
		incrementalButton.setEnabled(!regexValue);
		incrementalButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (incrementalButton.getSelection()) {
					resetIncrementalSelections();
				}
			}
		});
		regexButton = new Button(optionsGroup, SWT.CHECK);
		regexButton.setText(Messages.getString("Search.regexButtonLabel")); //$NON-NLS-1$
		regexButton.setSelection(regexValue);
		regexButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
		columnFirstButton = new Button(optionsGroup, SWT.CHECK);
		columnFirstButton.setText(Messages.getString("Search.columnFirstLabel")); //$NON-NLS-1$
		columnFirstButton.setSelection(columnFirstValue);
		// TODO
//		includeCollapsedButton = new Button(optionsGroup, SWT.CHECK);
//		includeCollapsedButton.setText(Messages.getString("Search.includeCollapsedLabel")); //$NON-NLS-1$
//		includeCollapsedButton.setSelection(includeCollapsedValue);
		
		return row;
	}
	
	private void doFind() {
		doFindInit();
		doFind0(false, findCombo.getText());
	}

	protected void doIncrementalFind() {
		doFindInit();
		final String text = findCombo.getText();
		String lastText = selections.peek().text;
		if (lastText.startsWith(text)) {
			while (selections.size() > 1
					&& selections.peek().text.length() > text.length()) {
				selections.pop();
			}
			doSelect(selections.peek());
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
			if (wholeWordValue) {
				doFind0(true, text);
			} else {
				for (int i = pos, n = text.length(); i < n; ++i) {
					doFind0(true, text.substring(0, i + 1));
				}
			}
		}
	}
	
	private void doFindInit() {
		statusLabel.setText(""); //$NON-NLS-1$
		statusLabel.setForeground(null);
		if (selectionLayer != null) {
			// If the current selection is different, the user must have
			// clicked a new selection, and we need to update to that.
			PositionCoordinate pos = getPosition();
			if (!pos.equals(selections.peek().pos)) {
				selections.clear();
				selections.push(new SelectionItem(getTextForSelection(pos), pos));
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
				natTable.doCommand(finalCommand);
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
			pos = searchEvent.getCellCoordinate();
		}
	}
	
	private void doFind0(final boolean isIncremental, final String text) {
			
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			
			@Override
			public void run() {
				PositionCoordinate previous = new PositionCoordinate(selections.peek().pos);
				try {
					final SearchCommand searchCommand = createSearchCommand(text, isIncremental);
					final SearchEventListener searchEventListener = new SearchEventListener();
					searchCommand.setSearchEventListener(searchEventListener);
					natTable.doCommand(searchCommand);
					if (searchEventListener.pos == null) {
						// Beep and show status if not found
						statusLabel.setText(Messages.getString("Search.textNotFound")); //$NON-NLS-1$
						getShell().getDisplay().beep();
					} else {
						SelectionItem selection = new SelectionItem(text, searchEventListener.pos);
						selections.push(selection);
						if (!isIncremental) {
							resetIncrementalSelections();
						}
						// Beep and show status if wrapped
						if (previous != null && previous.columnPosition > -1) {
							int columnDelta = selection.pos.columnPosition - previous.columnPosition;
							int rowDelta = selection.pos.rowPosition - previous.rowPosition;
							if (!forwardValue) {
								columnDelta = -columnDelta;
								rowDelta = -rowDelta;
							}
							int primaryDelta = columnFirstValue ? columnDelta : rowDelta;
							int secondaryDelta = columnFirstValue ? rowDelta : columnDelta;
							if (primaryDelta < 0 || !isIncremental && primaryDelta == 0 && secondaryDelta <= 0) {
								statusLabel.setText(Messages.getString("Search.wrappedSearch")); //$NON-NLS-1$
								getShell().getDisplay().beep();
							}
						}
					}
					if (!isIncremental) {
						updateFindHistory();
					}
				} catch (PatternSyntaxException e) {
					statusLabel.setText(e.getLocalizedMessage());
					statusLabel.setForeground(JFaceColors.getErrorText(statusLabel.getDisplay()));
					getShell().getDisplay().beep();
				}
			}
		});
	}

	private PositionCoordinate getPosition() {
		if (selectionLayer == null) {
			return new PositionCoordinate(null, SelectionLayer.NO_SELECTION, SelectionLayer.NO_SELECTION);
		}
		// The SelectionLayer keeps its anchor even if it has no selection.
		// Seems wrong to me, so here I clear out the anchor.
		PositionCoordinate pos = new PositionCoordinate(selectionLayer.getSelectionAnchor());
		if (selectionLayer.getSelectedCellPositions().length == 0
				&& pos.rowPosition != SelectionLayer.NO_SELECTION) {
			selectionLayer.clear(false);
			pos = new PositionCoordinate(selectionLayer.getSelectionAnchor());
		}
		return pos;
	}

	private void resetIncrementalSelections() {
		SelectionItem selection = selections.peek();
		selections.clear();
		selections.push(selection);
	}

	private SelectCellCommand createSelectCellCommand(PositionCoordinate selection) {
		SelectCellCommand selectCellCommand = new SelectCellCommand(
				selection.getLayer(), selection.columnPosition,
				selection.rowPosition, false, false);
		selectCellCommand.setForcingEntireCellIntoViewport(true);
		return selectCellCommand;
	}

	private SearchCommand createSearchCommand(String text, boolean isIncremental) {
		forwardValue = forwardButton.getSelection();
		allValue = allButton.getSelection();
		caseSensitiveValue = caseSensitiveButton.getSelection();
		wrapSearchValue = wrapSearchButton.getSelection();
		wholeWordValue = wholeWordButton.getSelection();
		incrementalValue = incrementalButton.getSelection();
		regexValue = regexButton.getSelection();
		// TODO
//		includeCollapsedValue = includeCollapsedButton.getSelection();
		columnFirstValue = columnFirstButton.getSelection();

		String searchDirection = forwardValue
				? ISearchDirection.SEARCH_FORWARD
				: ISearchDirection.SEARCH_BACKWARDS;
		ISearchStrategy searchStrategy;
		if (allValue) {
			searchStrategy = new GridSearchStrategy(natTable.getConfigRegistry(),
					true, columnFirstValue);
		} else {
			searchStrategy = new SelectionSearchStrategy(natTable.getConfigRegistry(),
					columnFirstValue);
		}
		return new SearchCommand(text,
				natTable, searchStrategy, 
				searchDirection,
				wrapSearchValue,
				caseSensitiveValue,
				!regexValue && wholeWordValue,
				!regexValue && allValue && isIncremental,
				regexValue,
				// TODO
//				includeCollapsedValue, comparator);
				false, comparator);
	}
	
	/**
	 * Called after executed find action to update the history.
	 */
	private void updateFindHistory() {
		findCombo.removeModifyListener(findComboModifyListener);
		updateHistory(findCombo, findHistory);
		findCombo.addModifyListener(findComboModifyListener);
	}

	/**
	 * Updates the combo with the history.
	 * @param findCombo to be updated
	 * @param history to be put into the combo
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
		Point selection= findCombo.getSelection();
		updateCombo(findCombo, history);
		findCombo.setText(findString);
		findCombo.setSelection(selection);
	}

	/**
	 * Updates the given combo with the given content.
	 * @param combo combo to be updated
	 * @param content to be put into the combo
	 */
	private static void updateCombo(Combo combo, List<String> content) {
		combo.removeAll();
		for (int i= 0; i < content.size(); i++) {
			combo.add(content.get(i).toString());
		}
	}

	private void updateButtons() {
		final boolean regex = regexButton.getSelection();
		final boolean allMode = allButton.getSelection();
		wholeWordButton.setEnabled(!regex);
		incrementalButton.setEnabled(!regex && allMode);
	}

	/**
	 * Returns the dialog settings object used to share state
	 * between several find/replace dialogs.
	 *
	 * @return the dialog settings to be used
	 */
	private IDialogSettings getDialogSettings() {
		return dialogSettings;
	}

	/*
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
	 */
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return dialogBounds;
	}

	/*
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsStrategy()
	 */
	@Override
	protected int getDialogBoundsStrategy() {
		return DIALOG_PERSISTLOCATION | DIALOG_PERSISTSIZE;
	}

	/**
	 * Initializes itself from the dialog settings with the same state
	 * as at the previous invocation.
	 */
	private void readConfiguration() {
		IDialogSettings s = getDialogSettings();
		if (s == null) {
			return;
		}
		
		wrapSearchValue = s.get("wrap") == null || s.getBoolean("wrap"); //$NON-NLS-1$ //$NON-NLS-2$
		caseSensitiveValue = s.getBoolean("casesensitive"); //$NON-NLS-1$
		wholeWordValue = s.getBoolean("wholeword"); //$NON-NLS-1$
		incrementalValue = s.getBoolean("incremental"); //$NON-NLS-1$
		regexValue = s.getBoolean("isRegEx"); //$NON-NLS-1$
		// TODO
//		includeCollapsedValue = s.get("includeCollapsed") == null //$NON-NLS-1$
//				|| s.getBoolean("includeCollapsed"); //$NON-NLS-1$
		columnFirstValue = s.getBoolean("columnFirst"); //$NON-NLS-1$

		String[] findHistoryConfig = s.getArray("findhistory"); //$NON-NLS-1$
		if (findHistoryConfig != null) {
			findHistory.clear();
			for (String history : findHistoryConfig) {
				findHistory.add(history);
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

		s.put("wrap", wrapSearchValue); //$NON-NLS-1$
		s.put("casesensitive", caseSensitiveValue); //$NON-NLS-1$
		s.put("wholeword", wholeWordValue); //$NON-NLS-1$
		s.put("incremental", incrementalValue); //$NON-NLS-1$
		s.put("isRegEx", regexValue); //$NON-NLS-1$
		// TODO
//		s.put("includeCollapsed", includeCollapsedValue); //$NON-NLS-1$
		s.put("columnFirst", columnFirstValue); //$NON-NLS-1$

		String findString = findCombo.getText();
		if (findString.length() > 0) {
			findHistory.add(0, findString);
		}
		writeHistory(findHistory, s, "findhistory"); //$NON-NLS-1$
	}

	/**
	 * Writes the given history into the given dialog store.
	 *
	 * @param history the history
	 * @param settings the dialog settings
	 * @param sectionName the section name
	 */
	private void writeHistory(List<String> history, IDialogSettings settings, String sectionName) {
		int itemCount= history.size();
		Set<String> distinctItems= new HashSet<String>(itemCount);
		for (int i= 0; i < itemCount; i++) {
			String item= history.get(i);
			if (distinctItems.contains(item)) {
				history.remove(i--);
				itemCount--;
			} else {
				distinctItems.add(item);
			}
		}

		while (history.size() > 8)
			history.remove(8);

		String[] names= new String[history.size()];
		history.toArray(names);
		settings.put(sectionName, names);

	}
}
