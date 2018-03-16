/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.dataset.car;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CarService {

    public static String[] PROPERTY_NAMES = {
            "manufacturer",
            "model",
            "motors.identifier",
            "motors.capacity",
            "motors.capacityUnit",
            "motors.maximumSpeed",
            "motors.feedbacks.creationTime",
            "motors.feedbacks.classification",
            "motors.feedbacks.comment"
    };

    public static String[] PROPERTY_NAMES_COMPACT = {
            "manufacturer",
            "model",
            "motors.identifier",
            "motors.capacity",
            "motors.feedbacks.classification",
            "motors.feedbacks.comment"
    };

    public static List<Car> getInput() {
        List<Car> input = new ArrayList<Car>();

        Car car = new Car("Mercedes", "C Klasse");
        Motor motor1 = new Motor("C320", "160", "KW", 250);
        Feedback order1 = new Feedback(new Date(), Classification.POSITIVE, "Blubb");
        Feedback order2 = new Feedback(new Date(), Classification.NEUTRAL, "Dingens");
        motor1.setFeedbacks(Arrays.asList(order1, order2));

        Motor motor2 = new Motor("C220", "125", "KW", 229);

        Motor motor3 = new Motor("C200", "100", "KW", 215);
        Feedback order31 = new Feedback(new Date(), Classification.POSITIVE, "bar");
        Feedback order32 = new Feedback(new Date(), Classification.NEUTRAL, "foo");
        motor3.setFeedbacks(Arrays.asList(order31, order32));

        car.setMotors(Arrays.asList(motor1, motor2, motor3));

        input.add(car);

        Car car2 = new Car("McLaren", "Senna");
        car2.setMotors(Arrays.asList(new Motor("Senna", "667", "PS", 340)));

        input.add(car2);

        Car car3 = new Car("BMW", "3er");
        Motor motor31 = new Motor("320", "135", "KW", 235);
        Feedback order311 = new Feedback(new Date(), Classification.POSITIVE, "cool");
        Feedback order312 = new Feedback(new Date(), Classification.POSITIVE, "awesome");
        motor31.setFeedbacks(Arrays.asList(order311, order312));

        Motor motor32 = new Motor("318", "100", "KW", 210);

        Motor motor33 = new Motor("330", "185", "KW", 250);
        Feedback order331 = new Feedback(new Date(), Classification.POSITIVE, "blabla");
        Feedback order332 = new Feedback(new Date(), Classification.POSITIVE, "singsingsing");
        motor33.setFeedbacks(Arrays.asList(order331, order332));

        car3.setMotors(Arrays.asList(motor31, motor32, motor33));

        input.add(car3);

        return input;
    }

}
