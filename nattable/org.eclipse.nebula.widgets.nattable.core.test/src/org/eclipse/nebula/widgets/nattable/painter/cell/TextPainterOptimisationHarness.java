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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class TextPainterOptimisationHarness {

	public static final String EMPTY = "";
	public static final String DOT = "...";

	private static Map<String,Integer> temporaryMap = new WeakHashMap<String,Integer>();
	private static Map<org.eclipse.swt.graphics.Font,FontData[]> fontDataCache = new WeakHashMap<org.eclipse.swt.graphics.Font,FontData[]>();

	private static boolean wrapText = false;
	
	private static final Pattern endOfPreviousWordPattern = Pattern.compile("\\S\\s+\\S+\\s*$");
	
	private static int expensiveMethodCallCounter = 0;
	private static boolean useOptimiser = true;
	
	
	//  you can adjust the width of the label and the label text here!!
	private static final int labelWidth = 50;
	private static final String labelMessage = "This is the text to fit into the label";
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Display display = new Display ();
		Shell shell = new Shell (display);
		Label label = new Label (shell, SWT.BORDER);
		
		GC gc = new GC (label);
		FontMetrics fm = gc.getFontMetrics ();

		int height = fm.getHeight ();
		label.setSize (label.computeSize (labelWidth, height));

		useOptimiser = true;
		expensiveMethodCallCounter = 0;
		String textUsingOptimiser  = getAvailableTextToDisplay(gc, label.getBounds(), labelMessage);
		System.out.println("number of expensive method calls when optimised     = "+expensiveMethodCallCounter);
		
		useOptimiser = false;
		expensiveMethodCallCounter = 0;
		String textWithoutUsingOptimiser  = getAvailableTextToDisplay(gc, label.getBounds(), labelMessage);
		System.out.println("number of expensive method calls when not optimised = "+expensiveMethodCallCounter);
		
		if (!textWithoutUsingOptimiser.equals(textUsingOptimiser)){
			throw new Exception("The end result strings were not consistent");
		}
		
		label.setText(textUsingOptimiser);		
		
		gc.dispose ();

		shell.pack ();
		shell.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();

	}

	private static int getWidthFromCache(GC gc, String text) {
		expensiveMethodCallCounter++;
		String originalString = text;
		StringBuilder buffer = new StringBuilder();
		buffer.append(text);
		if (gc.getFont() != null) {
			FontData[] datas = fontDataCache.get(gc.getFont());
			if (datas == null) {
				datas = gc.getFont().getFontData();
				fontDataCache.put(gc.getFont(), datas);
			}
			if (datas != null && datas.length > 0) {
				buffer.append(datas[0].getName());
				buffer.append(",");
				buffer.append(datas[0].getHeight());
				buffer.append(",");
				buffer.append(datas[0].getStyle());
			}
		}
		text = buffer.toString();
		Integer width = temporaryMap.get(text);
		if (width == null) {
			width = Integer.valueOf(gc.textExtent(originalString).x);
			temporaryMap.put(text, width);
		}
		
		return width.intValue();
	}

	private static String getAvailableTextToDisplay(GC gc, Rectangle bounds, String text) {
		StringBuilder output = new StringBuilder();

		text = text.trim();

		while (text.length() > 0) {
			String line;
			int nextLineBreakIndex;

			int indexOfNewline = text.indexOf('\n');
			if (indexOfNewline > 0) {
				nextLineBreakIndex = indexOfNewline;
				line = text.substring(0, nextLineBreakIndex);
			} else {
				nextLineBreakIndex = -1;
				line = text;
			}

			int textWidth = getWidthFromCache(gc, line);

			if (wrapText) {
				while (textWidth > bounds.width + 1) {
					Matcher matcher = endOfPreviousWordPattern.matcher(line);
					if (matcher.find()) {
						nextLineBreakIndex = matcher.start() + 1;
						line = line.substring(0, nextLineBreakIndex);
						textWidth = getWidthFromCache(gc, line);
					} else {
						nextLineBreakIndex = -1;
						break;
					}
				}
			}
 
			
			
			if (textWidth > bounds.width + 1) {
				
				expensiveMethodCallCounter = 0;
				
				if (useOptimiser) {
					
					
					
					// if we reached this bit of the code, we know we are going to have to truncate the string

					String nextTrialString = line;
					int numExtraChars = 0;
					int newStringLength = nextTrialString.length() - numExtraChars;
						
					String trialLabelText = nextTrialString + DOT;
					int newTextExtent = getWidthFromCache(gc, trialLabelText);
					
					while (newTextExtent > bounds.width + 1 && newStringLength > 0) {
						
						int avgWidthPerChar = newTextExtent / trialLabelText.length() ;
						numExtraChars = 1 + (newTextExtent - bounds.width) / avgWidthPerChar;
						
						newStringLength = nextTrialString.length() - numExtraChars;
						if (newStringLength>0){
							nextTrialString = nextTrialString.substring(0, newStringLength);
							trialLabelText = nextTrialString + DOT;
							newTextExtent = getWidthFromCache(gc, trialLabelText);
						}
					}
					
					if (numExtraChars>line.length()){
						numExtraChars = line.length();
					}

				
					// now we have gone too short, lets add chars one at a time to exceed the width...
					String testString = line;
					for (int i=0;i<line.length();i++){
						testString = line.substring(0, line.length() + i - numExtraChars) + DOT;
						textWidth = getWidthFromCache(gc, testString);
						
						if (textWidth >= bounds.width) {
							
							//  now roll back one as this was the first number that exceeded
							if (line.length() + i - numExtraChars < 1){
								line = EMPTY;
							} else {
								line = line.substring(0, line.length() + i - numExtraChars - 1 ) + DOT;
							}
							break;
						} 
					}
				} else {

				
					//  this is the expensive non-optimised codebase
					int textLen = line.length();
					for (int i = textLen - 1; i >= 0; i--) {
						String temp = line.substring(0, i) + DOT;
						
						textWidth = getWidthFromCache(gc, temp);
						if (textWidth < bounds.width) {
							line = temp;
							break;
						} else if (i == 0) {
							line = EMPTY;
						}
					} 
				}
			}

			output.append(line);

			if (nextLineBreakIndex > 0) {
				text = text.substring(nextLineBreakIndex).trim();

				if (text.length() > 0) {
					output.append("\n");
				}
			} else {
				break;
			}
		}

		return output.toString();
	}
	
}
