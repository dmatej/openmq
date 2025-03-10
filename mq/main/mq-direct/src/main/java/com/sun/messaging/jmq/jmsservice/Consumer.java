/*
 * Copyright (c) 2000, 2017 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.messaging.jmq.jmsservice;

import com.sun.messaging.jmq.io.JMSPacket;

public interface Consumer {

    /**
     * Deliver a message to the consumer to be processed.
     * <p>
     * Except in the special case of consumer in closing and the message is neither delivered nor acked to throw a
     * ConsumerClosedNoDeliveryException, the implementation of this method must not throw any exceptions, and it must catch
     * any exceptions, and either redeliver the message according to the redelivery configuration or it must acknowledge
     * this message. It can acknowledge this message to the dead message queue if the redelivery attempts are unsuccessful.
     *
     * @param msgPkt The JMSPacket to be processed
     *
     * @return The JMSAck if the delivered message is to be acknowledged upon returning from this method.<br>
     * If this is {@code null} then the message will be acknowledged separately.
     */
    JMSAck deliver(JMSPacket msgPkt) throws ConsumerClosedNoDeliveryException;

}
