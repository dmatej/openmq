/*
 * Copyright (c) 2000, 2017 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)InvalidJMSSelectorException.java	1.4 06/28/07
 */

package com.sun.messaging.jmq.jmsselector;

/**
 * This exception is thrown when an invalid selector has been set
 */
public class InvalidJMSSelectorException extends java.lang.Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -5176195902774519231L;

    /**
     * Construct a InvalidJMSSelectorException
     */
    public InvalidJMSSelectorException() {
        super();
    }

    /**
     * Construct a InvalidJMSSelectorException
     */
    public InvalidJMSSelectorException(String reason) {
        super(reason);
    }

}
