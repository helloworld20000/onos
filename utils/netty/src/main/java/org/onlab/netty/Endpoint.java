/*
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onlab.netty;

import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Representation of a TCP/UDP communication end point.
 */
public final class Endpoint {

    private final int port;
    private final String host;

    public Endpoint(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("host", host)
                .add("port", port)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Endpoint that = (Endpoint) obj;
        return Objects.equals(this.port, that.port) &&
               Objects.equals(this.host, that.host);
    }
}
