/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sshd.common.auth;

import org.apache.sshd.common.NamedResource;
import org.apache.sshd.common.session.Session;

/**
 * Represents an authentication-in-progress tracker for a specific session
 *
 * @param <S> The type of session being tracked by the instance
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
//CHECKSTYLE:OFF
public interface UserAuthInstance<S extends Session> extends NamedResource {
    /**
     * @return The current session for which the authentication is being
     * tracked. <B>Note:</B> may be {@code null} if the instance has not
     * been initialized yet
     */
    S getSession();
}
//CHECKSTYLE:ON
