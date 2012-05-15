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
package org.eclipse.nebula.widgets.nattable.dataset.generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.nebula.widgets.nattable.dataset.valuegenerator.UppercaseStringValueGenerator;


public class NatTableDataGenerator {

	private int numRows = 0;

	private int numCols = 0;

	private List<ColumnDataType> columnDataTypes;

	private static DataGenerator<DoubleColumnValueBean> doubleGenerator = new DataGenerator<DoubleColumnValueBean>();

	private static DataGenerator<StringColumnValueBean> stringGenerator = new DataGenerator<StringColumnValueBean>();

	enum ColumnDataType {
		STRING_DATA, DOUBLE_DATA
	};

	public NatTableDataGenerator(int numCols, final int numRows) {
		this.numRows = numRows;
		this.numCols = numCols;
		initColumnDataTypes();
	}

	public NatTableDataGenerator() {
	}

	private void initColumnDataTypes() {
		columnDataTypes = new ArrayList<ColumnDataType>();
		for (int i = 0; i < numCols; i++) {
			ColumnDataType dataType = ColumnDataType.values()[(int) (Math.random() * ColumnDataType.values().length)];
			columnDataTypes.add(dataType);
		}
	}

	public void persistData(final String fileName) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			// first write the data types
			for (int i = 0; i < columnDataTypes.size(); i++) {
				ColumnDataType dataType = columnDataTypes.get(i);
				out.write(dataType.toString());
				out.write(i == columnDataTypes.size() - 1 ? "" : ",");
			}
			
			out.newLine();
			String text = "Writing to " + fileName;
			int charsWritten = text.length();
			System.out.print(text);
			for (int i = 0; i < numRows; i++) {
				for (int j = 0; j < numCols; j++) {
					ColumnDataType dataType= columnDataTypes.get(j);
					ColumnValueBean<?> value = null;
					switch (dataType) {
					case STRING_DATA:
						value = stringGenerator.generate(StringColumnValueBean.class);
						break;
					case DOUBLE_DATA:
						value = doubleGenerator.generate(DoubleColumnValueBean.class);
						break;
					}
					String stringValue = value.getValue() == null ? " " : value.getValue().toString();
					stringValue = "".equals(stringValue) ? " " : stringValue;
					out.write(stringValue);
					out.write(j == numCols - 1 ? "" : ",");
				}
				if (i % numCols == 0) {
					System.out.print(".");
					charsWritten++;
				}
				if (charsWritten > 80) {
					System.out.println();
					charsWritten = 0;
				}
				out.newLine();
			}
			out.close();
			System.out.println("done");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (GeneratorException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
	}

	// TODO: file dimensions should be embedded in the file
	public void getNumRows(String fileName)  {
		try {
			FileReader fro = new FileReader(fileName);
			BufferedReader bro = new BufferedReader(fro);

			String stringFromFile = bro.readLine();
			while (stringFromFile != null) // end of the file
			{
				numRows++;
				stringFromFile = bro.readLine(); // read next line
			}
			bro.close();
		}
		catch (FileNotFoundException filenotfoundexxption) {
			System.out.println(fileName + ", does not exist");
			System.exit(-1);
		}
		catch (IOException ioexception) {
			ioexception.printStackTrace();
			System.exit(-1);
		}
		numRows--; // first row contains the data types of the columns 
	}

	public TableDataProvider loadData(final String fileName) {
		
		// need to pass through file first to get matrix dimensions
		getNumRows(fileName);
		
		Object[][] tableData = null;
		try {
			FileReader fro = new FileReader(fileName);
			BufferedReader bro = new BufferedReader(fro);

			// declare String variable and prime the read
			String stringFromFile = bro.readLine();
			boolean columnDataTypesRead = false;
			List<ColumnDataType> dataTypes = null;
			int curRow = 0;
			System.out.print("Loading data");
			while (stringFromFile != null) // end of the file
			{
				if (!columnDataTypesRead) {
					columnDataTypesRead = true;
					dataTypes = parseDataTypes(stringFromFile);
					numCols = dataTypes.size();
					tableData = new Object[numCols][numRows];
				} else {
					if (dataTypes == null || dataTypes.size() == 0)
						throw new IllegalStateException("Data description line missing");

					addRow(tableData, curRow++, dataTypes, stringFromFile);
				}
				stringFromFile = bro.readLine(); // read next line
				if (curRow % 1000 == 0)
					System.out.print(".");
			}
			System.out.println("done");
			bro.close();
		}
		catch (FileNotFoundException filenotfoundexxption) {
			System.out.println(fileName + ", does not exist");
			System.exit(-1);
		}

		catch (IOException ioexception) {
			ioexception.printStackTrace();
			System.exit(-1);
		}

		return new TableDataProvider(tableData, numCols, numRows);
	}

	private void addRow(Object[][] tableData, int curRow, List<ColumnDataType> dataTypes, String stringFromFile) {
		StringTokenizer tokenizer = new StringTokenizer(stringFromFile, ",");
		String token;
		for (int i = 0; i < dataTypes.size(); i++) {
			ColumnDataType dataType = dataTypes.get(i);
			token = tokenizer.nextToken().trim();
			switch (dataType) {
			case STRING_DATA:
				tableData[i][curRow] = token;
				break;
			case DOUBLE_DATA:
				tableData[i][curRow] = Double.valueOf(token);
				break;
			}
		}
	}

	private List<ColumnDataType> parseDataTypes(String stringFromFile) {
		List<ColumnDataType> dataTypes = new ArrayList<ColumnDataType>();
		StringTokenizer tokenizer = new StringTokenizer(stringFromFile, ",");
		int numDataTypes = tokenizer.countTokens();
		String token;
		for (int i = 0; i < numDataTypes; i++) {
			token = tokenizer.nextToken().trim();
			ColumnDataType dataType = ColumnDataType.valueOf(token);
			dataTypes.add(dataType);
		}
		return dataTypes;
	}


	public static class ColumnValueBean<T> {
		private T value;

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}
	}

	public static class DoubleColumnValueBean extends ColumnValueBean<Double> {
		@GenerateDouble(range = 1000)
		private Double value;

		public Double getValue() {
			return value;
		}

		public void setValue(Double value) {
			this.value = value;
		}
	};

	public static class StringColumnValueBean extends ColumnValueBean<String> {
		@DataValueGenerator(UppercaseStringValueGenerator.class)
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	};

	public static void main(String[] args) {
		NatTableDataGenerator dataGenerator;
		String fileName;

		dataGenerator = new NatTableDataGenerator(100, 10000);
		fileName = "c:/temp/100x10K_tableData.txt";
		dataGenerator.persistData(fileName);
		dataGenerator = new NatTableDataGenerator(100, 100000);
		fileName = "c:/temp/100x100K_tableData.txt";
		dataGenerator.persistData(fileName);
		dataGenerator = new NatTableDataGenerator(100, 1000000);
		fileName = "c:/temp/100x1mil_tableData.txt";
		dataGenerator.persistData(fileName);
	}
}
