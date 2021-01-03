/*
 * Copyright (c) 2000, 2017 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

/*
 * @(#)EventListener.java	1.5 06/29/07
 */

package com.sun.messaging.jmq.util.lists;

/**
 * Callback interface called when an event has occurred on a list which implements EventBroadcaster
 *
 * @see EventBroadcaster
 */

public interface EventListener extends java.util.EventListener {
    /**
     * Called when a specific event occurs
     *
     * @param e the event that occured
     */

    void eventOccured(EventType type, Reason reason, Object source, Object OrigValue, Object NewValue, Object userdata);

}
