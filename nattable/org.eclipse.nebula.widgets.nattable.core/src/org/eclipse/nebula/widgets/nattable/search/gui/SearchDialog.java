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

import java.util.Comparator;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.search.ISearchDirection;
import org.eclipse.nebula.widgets.nattable.search.command.SearchCommand;
import org.eclipse.nebula.widgets.nattable.search.event.SearchEvent;
import org.eclipse.nebula.widgets.nattable.search.strategy.ISearchStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SearchDialog extends Dialog {
	
	private Text findText;
	private Button findButton;
	private Button caseSensitiveButton;
	private Label statusLabel;
	private Button wrapSearchButton;
	private Button forwardButton;
	private final ILayer layer;
	private ISearchStrategy searchStrategy;
	private Comparator<?> comparator;

	private SearchDialog(Shell shell, ILayer layer) {
		super(shell);
		this.layer = layer;
		setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		setBlockOnOpen(false);
	}
	
	public static SearchDialog createDialog(Shell shell, ILayer layer) {
		return new SearchDialog(shell, layer);
	}

	public void setSearchStrategy(ISearchStrategy searchStrategy,  Comparator<?> comparator) {
		this.searchStrategy = searchStrategy;
		this.comparator = comparator;
	}
	
	@Override
	public void create() {

		super.create();
		getShell().setText(Messages.getString("Search.find")); //$NON-NLS-1$

	}
	
	@Override
	protected Control createContents(final Composite parent) {
		
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1,false));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(createInputPanel(composite));

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(createOptionsPanel(composite));

		Composite buttonPanel = createButtonSection(composite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM).grab(true, true).applyTo(buttonPanel);
		
		return composite;
	}

	private Composite createButtonSection(Composite composite) {

		Composite panel = new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout(1,false);
		panel.setLayout(layout);
		
		statusLabel = new Label(panel, SWT.LEFT);
		statusLabel.setForeground(statusLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(statusLabel);
		
		findButton = createButton(panel, IDialogConstants.CLIENT_ID, Messages.getString("Search.findButtonLabel"), false); //$NON-NLS-1$
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM).grab(false, false).hint(52, SWT.DEFAULT).applyTo(findButton);
		
		findButton.setEnabled(false);
		getShell().setDefaultButton(findButton);
		
		findButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doFind();
			}
		});
 		
		Button closeButton = createButton(panel, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM).grab(false, false).hint(52, SWT.DEFAULT).applyTo(closeButton);
		
		return panel;
	}

	private Composite createInputPanel(final Composite composite) {
		final Composite row = new Composite(composite, SWT.NONE);
		row.setLayout(new GridLayout(2,false));
		
		final Label findLabel = new Label(row, SWT.NONE);
		findLabel.setText(Messages.getString("Search.findLabel") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(findLabel);
		
		findText = new Text(row, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(findText);
		findText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				findButton.setEnabled(findText.getText().length() > 0);
			}
		});
		findText.addSelectionListener(new SelectionAdapter() {
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
		final RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.marginHeight = rowLayout.marginWidth = 3;
		directionGroup.setLayout(rowLayout);
		forwardButton = new Button(directionGroup, SWT.RADIO);
		forwardButton.setText(Messages.getString("Search.forwardButtonLabel")); //$NON-NLS-1$
		forwardButton.setSelection(true);
		final Button backwardButton = new Button(directionGroup, SWT.RADIO);
		backwardButton.setText(Messages.getString("Search.backwardButtonLabel")); //$NON-NLS-1$

		final Group optionsGroup = new Group(row, SWT.SHADOW_ETCHED_IN);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(optionsGroup);
		optionsGroup.setText(Messages.getString("Search.options")); //$NON-NLS-1$
		optionsGroup.setLayout(rowLayout);
		caseSensitiveButton = new Button(optionsGroup, SWT.CHECK);
		caseSensitiveButton.setText(Messages.getString("Search.caseSensitiveButtonLabel")); //$NON-NLS-1$
		wrapSearchButton = new Button(optionsGroup, SWT.CHECK);
		wrapSearchButton.setText(Messages.getString("Search.wrapSearchButtonLabel")); //$NON-NLS-1$
		wrapSearchButton.setSelection(true);
		
		return row;
	}

	private void doFind() {
		
		BusyIndicator.showWhile(super.getShell().getDisplay(), new Runnable() {
			
			private PositionCoordinate searchResultCoordinate;
			
			public void run() {
				searchResultCoordinate = null;
				statusLabel.setText(""); //$NON-NLS-1$
				
				String searchDirection = forwardButton.getSelection() ? ISearchDirection.SEARCH_FORWARD : ISearchDirection.SEARCH_BACKWARDS;
				final SearchCommand command = new SearchCommand(findText.getText(), layer, searchStrategy, searchDirection, wrapSearchButton.getSelection(), caseSensitiveButton.getSelection(), comparator);
				
				final ILayerListener searchEventListener = initSearchEventListener();
				command.setSearchEventListener(searchEventListener);
				
				try {
					// Fire command
					layer.doCommand(command);
					
					if (searchResultCoordinate == null || (searchResultCoordinate.columnPosition < 0  && searchResultCoordinate.rowPosition < 0)) {
						statusLabel.setText(Messages.getString("Search.textNotFound")); //$NON-NLS-1$
					}
				} finally {
					command.getContext().removeLayerListener(searchEventListener);
				}
			}

			private ILayerListener initSearchEventListener() {
				// Register event listener
				final ILayerListener searchEventListener = new ILayerListener() {
					public void handleLayerEvent(ILayerEvent event) {
						if (event instanceof SearchEvent) {
							SearchEvent searchEvent = (SearchEvent) event;
							searchResultCoordinate = searchEvent.getCellCoordinate();
						}
					}
				};
				return searchEventListener;
			}
		});
	}
}
