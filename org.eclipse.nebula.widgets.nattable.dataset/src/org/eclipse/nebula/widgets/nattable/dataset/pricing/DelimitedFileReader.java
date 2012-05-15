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
package org.eclipse.nebula.widgets.nattable.dataset.pricing;

import java.io.FilterReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class DelimitedFileReader extends FilterReader {
	private char delimChar;
	private StringTokenizer tabbedLineRead;

	DelimitedFileReader(Reader reader, char delimeter) {
		super(reader);
		this.delimChar = delimeter;		
	}

	/**
	 * This method will read until it finds a return character.
	 */
	@Override
	public int read() throws IOException {
		return readLine(new char[1], 0, 1);
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		return readLine(cbuf, off, len);
	}

	/**
	 * Return when a line is read.  The line can then be processed by accessing the tabbedLineRead 
	 * tokenizer.  The tokenzier is built using the delimChar
	 * @param readBuffer
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 */
	private int readLine(char[] readBuffer, int off, int len) throws IOException {
		int read = -1;
		boolean hasLineBeenRead = false;
		if (tabbedLineRead != null) {
			in.reset();
		}
		char prevChar = delimChar;
		List<Character> charBuffer = new ArrayList<Character>();
		PushbackReader pushBackReader = new PushbackReader(in, len);
		while ((read = pushBackReader.read(readBuffer, off, len)) >= 0) {
			//Read until new line is found.  This allows users to handle line by line.
			for(int charIndex = 0; charIndex < readBuffer.length; charIndex++) {
				char readChar = readBuffer[charIndex];
				if (readChar == '\n') {
					in.mark(read);
					pushBackReader.unread(readBuffer, 0, readBuffer.length - (charIndex + 1));
					hasLineBeenRead = true;
					break;
				} else {
					if (readChar == delimChar && delimChar == prevChar) {
						charBuffer.add(Character.valueOf(' '));
					}
					prevChar = readChar;
					charBuffer.add(Character.valueOf(readChar));
				}
			}
			//If line has been read, return control to caller
			if(hasLineBeenRead){
				hasLineBeenRead = false;
				break;
			}
		}
		if (read >= 0) {
			tabbedLineRead = new StringTokenizer(parseCharactersToString(charBuffer), String.valueOf(delimChar));
		}
		return read;
	}

	public StringTokenizer getTabbedLineRead() {
		return tabbedLineRead;
	}

	private String parseCharactersToString(List<Character> chars) {
		char[] dataRead = new char[chars.size()];
		int charCounter = 0;
		for (char charRead : chars) {
			dataRead[charCounter++] = charRead;
		}

		return new String(dataRead);
	}
}
