/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.framework.junit.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Disabled;

/**
 * Challenge metadata to track where challenges from GitHub came from,
 * what version was affected by the challenge,
 * and if the issue has been fixed.
 *
 * Aggregates the @Disabled annotation from Junit
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Disabled
public @interface Challenge {
    
    /**
     * REQUIRED: Link to the challenge on GitHub
     */
    String link();
    
    /**
     * REQUIRED: The version of the TCK where the challenge was found.
     */
    String version();
    
    /**
     * OPTIONAL: Link to the pull request on GitHub that fixes the challenge
     */
    String fix() default "";
    
    /**
     * OPTIONAL: The version of the TCK where the test can be re-enabled
     */
    String reintroduce() default "";
    
    /**
     * OPTIONAL: The reason the TCK was disabled
     */
    String reason() default "";


}
