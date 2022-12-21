/*****************************************************************************
 * Copyright (c) 2018, 2022 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.dataset.car.Car;
import org.eclipse.nebula.widgets.nattable.dataset.car.CarService;
import org.eclipse.nebula.widgets.nattable.dataset.car.Classification;
import org.eclipse.nebula.widgets.nattable.dataset.car.Feedback;
import org.eclipse.nebula.widgets.nattable.dataset.car.Motor;
import org.junit.jupiter.api.Test;

public class HierarchicalTest {

    @Test
    public void testCreationWithCarOnly() {
        List<Car> input = new ArrayList<>();
        input.add(new Car("Mercedes", "C Klasse"));

        List<HierarchicalWrapper> result = HierarchicalHelper.deNormalize(input, false, CarService.getPropertyNames());

        assertEquals(1, result.size());
    }

    @Test
    public void testCreationWithCarAndMotor() {
        List<Car> input = new ArrayList<>();

        Car car = new Car("Mercedes", "C Klasse");
        Motor motor1 = new Motor("C320", "160", "KW", 250);
        Motor motor2 = new Motor("C200", "100", "KW", 215);
        car.setMotors(Arrays.asList(motor1, motor2));

        input.add(car);

        List<HierarchicalWrapper> result = HierarchicalHelper.deNormalize(input, false, Arrays.asList(CarService.getPropertyNames()));

        assertEquals(2, result.size());
    }

    @Test
    public void testCreationWithCarAndMotorWithOrders() {
        List<Car> input = new ArrayList<>();

        Car car = new Car("Mercedes", "C Klasse");
        Motor motor1 = new Motor("C320", "160", "KW", 250);
        Feedback order1 = new Feedback(new Date(), Classification.POSITIVE, "Blubb");
        Feedback order2 = new Feedback(new Date(), Classification.NEUTRAL, "Dingens");
        motor1.setFeedbacks(Arrays.asList(order1, order2));

        Motor motor2 = new Motor("C200", "100", "KW", 215);
        car.setMotors(Arrays.asList(motor1, motor2));

        input.add(car);

        List<HierarchicalWrapper> result = HierarchicalHelper.deNormalize(input, false, Arrays.asList(CarService.getPropertyNames()));

        assertEquals(3, result.size());

        // comment in for bugfixing
        // System.out.println("Without parent structure objects");
        // result.forEach(wrapper -> {
        // Car c = (Car) wrapper.getObject(0);
        // Motor m = (Motor) wrapper.getObject(1);
        // Feedback o = (Feedback) wrapper.getObject(2);
        // StringBuilder builder = new StringBuilder();
        // builder.append(c.getManufacturer()).append(" ").append(c.getModel());
        // if (m != null) {
        // builder.append(" ").append(m.getIdentifier())
        // .append(" ").append(m.getCapacity()).append("
        // ").append(m.getCapacityUnit())
        // .append(" ").append(m.getMaximumSpeed()).append("km/h");
        // }
        // if (o != null) {
        // builder.append(" ").append(o.getCreationTime())
        // .append(" ").append(o.getClassification()).append("
        // ").append(o.getComment());
        // }
        // System.out.println(builder.toString());
        // });
        // System.out.println();

        // test get data value
        HierarchicalReflectiveColumnPropertyAccessor accessor = new HierarchicalReflectiveColumnPropertyAccessor(CarService.getPropertyNames());
        assertEquals(Classification.NEUTRAL, accessor.getDataValue(result.get(1), 7));

        // test set data value
        assertEquals("Dingens", accessor.getDataValue(result.get(1), 8));
        accessor.setDataValue(result.get(1), 8, "Something");
        assertEquals("Something", accessor.getDataValue(result.get(1), 8));

        // test non existing child
        assertNull(accessor.getDataValue(result.get(2), 8));
        accessor.setDataValue(result.get(2), 8, "Something");
        assertNull(accessor.getDataValue(result.get(2), 8));
    }

    @Test
    public void testCreationWithCarOnlyAndRootObjects() {
        List<Car> input = new ArrayList<>();
        input.add(new Car("Mercedes", "C Klasse"));

        List<HierarchicalWrapper> result = HierarchicalHelper.deNormalize(input, true, Arrays.asList(CarService.getPropertyNames()));

        assertEquals(1, result.size());
    }

    @Test
    public void testCreationWithCarAndMotorAndRootObjects() {
        List<Car> input = new ArrayList<>();

        Car car = new Car("Mercedes", "C Klasse");
        Motor motor1 = new Motor("C320", "160", "KW", 250);
        Motor motor2 = new Motor("C200", "100", "KW", 215);
        car.setMotors(Arrays.asList(motor1, motor2));

        input.add(car);

        List<HierarchicalWrapper> result = HierarchicalHelper.deNormalize(input, true, Arrays.asList(CarService.getPropertyNames()));

        assertEquals(3, result.size());
    }

    @Test
    public void testCreationWithCarAndMotorWithOrdersAndRootObjects() {
        List<Car> input = new ArrayList<>();

        Car car = new Car("Mercedes", "C Klasse");
        Motor motor1 = new Motor("C320", "160", "KW", 250);
        Feedback order1 = new Feedback(new Date(), Classification.POSITIVE, "Blubb");
        Feedback order2 = new Feedback(new Date(), Classification.NEUTRAL, "Dingens");
        motor1.setFeedbacks(Arrays.asList(order1, order2));

        Motor motor2 = new Motor("C200", "100", "KW", 215);
        car.setMotors(Arrays.asList(motor1, motor2));

        input.add(car);

        List<HierarchicalWrapper> result = HierarchicalHelper.deNormalize(input, true, Arrays.asList(CarService.getPropertyNames()));

        assertEquals(5, result.size());

        // comment in for bugfixing
        // System.out.println("With parent structure objects");
        // result.forEach(wrapper -> {
        // Car c = (Car) wrapper.getObject(0);
        // Motor m = (Motor) wrapper.getObject(1);
        // Feedback o = (Feedback) wrapper.getObject(2);
        // StringBuilder builder = new StringBuilder();
        // builder.append(c.getManufacturer()).append(" ").append(c.getModel());
        // if (m != null) {
        // builder.append(" ").append(m.getIdentifier())
        // .append(" ").append(m.getCapacity()).append("
        // ").append(m.getCapacityUnit())
        // .append(" ").append(m.getMaximumSpeed()).append("km/h");
        // }
        // if (o != null) {
        // builder.append(" ").append(o.getCreationTime())
        // .append(" ").append(o.getClassification()).append("
        // ").append(o.getComment());
        // }
        // System.out.println(builder.toString());
        // });
        // System.out.println();

        // test get data value
        // with structure parent objects, first object is Car, second Car +
        // Motor, third Car + Motor + Feedback
        HierarchicalReflectiveColumnPropertyAccessor accessor = new HierarchicalReflectiveColumnPropertyAccessor(CarService.getPropertyNames());
        HierarchicalWrapper first = result.get(0);
        HierarchicalWrapper second = result.get(1);
        HierarchicalWrapper third = result.get(2);

        assertEquals("Mercedes", accessor.getDataValue(first, 0));
        assertNull(accessor.getDataValue(first, 2));
        assertNull(accessor.getDataValue(first, 4));

        assertEquals("Mercedes", accessor.getDataValue(second, 0));
        assertEquals("C320", accessor.getDataValue(second, 2));
        assertNull(accessor.getDataValue(second, 6));

        assertEquals("Mercedes", accessor.getDataValue(third, 0));
        assertEquals("C320", accessor.getDataValue(third, 2));
        assertEquals("KW", accessor.getDataValue(third, 4));

        // test set data value
        assertEquals("Blubb", accessor.getDataValue(third, 8));
        accessor.setDataValue(third, 8, "Something");
        assertEquals("Something", accessor.getDataValue(third, 8));

        // test non existing child
        assertNull(accessor.getDataValue(result.get(4), 8));
        accessor.setDataValue(result.get(4), 8, "Something");
        assertNull(accessor.getDataValue(result.get(4), 8));
    }

    @Test
    public void testCreationWithMultipleCarAndMotorWithOrdersAndRootObjects() {
        List<Car> input = new ArrayList<>();

        Car car = new Car("Mercedes", "C Klasse");
        Motor motor1 = new Motor("C320", "160", "KW", 250);
        Feedback order1 = new Feedback(new Date(), Classification.POSITIVE, "Blubb");
        Feedback order2 = new Feedback(new Date(), Classification.NEUTRAL, "Dingens");
        motor1.setFeedbacks(Arrays.asList(order1, order2));

        Motor motor2 = new Motor("C200", "100", "KW", 215);
        car.setMotors(Arrays.asList(motor1, motor2));

        input.add(car);

        Car car2 = new Car("McLaren", "Senna");
        car2.setMotors(Arrays.asList(new Motor("Senna", "667", "PS", 340)));

        input.add(car2);

        List<HierarchicalWrapper> result = HierarchicalHelper.deNormalize(input, true, Arrays.asList(CarService.getPropertyNames()));

        assertEquals(7, result.size());

        // comment in for bugfixing
        // System.out.println("Multiple Car with parent structure objects");
        // result.forEach(wrapper -> {
        // Car c = (Car) wrapper.getObject(0);
        // Motor m = (Motor) wrapper.getObject(1);
        // Feedback o = (Feedback) wrapper.getObject(2);
        // StringBuilder builder = new StringBuilder();
        // builder.append(c.getManufacturer()).append(" ").append(c.getModel());
        // if (m != null) {
        // builder.append(" ").append(m.getIdentifier())
        // .append(" ").append(m.getCapacity()).append("
        // ").append(m.getCapacityUnit())
        // .append(" ").append(m.getMaximumSpeed()).append("km/h");
        // }
        // if (o != null) {
        // builder.append(" ").append(o.getCreationTime())
        // .append(" ").append(o.getClassification()).append("
        // ").append(o.getComment());
        // }
        // System.out.println(builder.toString());
        // });
        // System.out.println();

    }

    @Test
    public void testCreationWithEmptyChildCollection() {
        List<Car> input = new ArrayList<>();

        Car car1 = new Car("BMW", "3er");
        car1.setMotors(new ArrayList<Motor>());

        Car car = new Car("Mercedes", "C Klasse");
        Motor motor1 = new Motor("C320", "160", "KW", 250);
        Motor motor2 = new Motor("C200", "100", "KW", 215);
        car.setMotors(Arrays.asList(motor1, motor2));

        input.add(car1);
        input.add(car);

        List<HierarchicalWrapper> result = HierarchicalHelper.deNormalize(input, false, Arrays.asList(CarService.getPropertyNames()));

        assertEquals(3, result.size());
    }

}
