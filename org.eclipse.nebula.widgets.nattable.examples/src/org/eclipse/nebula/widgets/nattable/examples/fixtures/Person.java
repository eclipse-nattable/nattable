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
package org.eclipse.nebula.widgets.nattable.examples.fixtures;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Object representation of a row in the table
 */
public class Person {
	private int id;
	private String name;
	private Date birthDate;

	public Person(int id, String name, Date birthDate) {
		this.id = id;
		this.name = name;
		this.birthDate = birthDate;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Date getBirthDate() {
		return birthDate;
	}
	
	public static List<Person> getList(){
		return Arrays.asList(
				new Person(100, "Mickey Mouse", new Date(1000000)), 
				new Person(110, "Batman", new Date(2000000)), 
				new Person(120, "Bender", new Date(3000000)), 
				new Person(130, "Cartman", new Date(4000000)), 
				new Person(140, "Dogbert", new Date(5000000)));
	}
}
