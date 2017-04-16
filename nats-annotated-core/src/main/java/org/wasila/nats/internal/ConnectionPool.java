/**
 * (C) Copyright 2017 Adam Wasila.
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
package org.wasila.nats.internal;

import io.nats.client.Connection;
import io.nats.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class ConnectionPool {

    private final Logger log = LoggerFactory.getLogger(ConnectionPool.class);

    private Map<String, ConnectionFactory> factories = new HashMap<>();

    private Map<String, Connection> connections = new HashMap<>();

    private Thread shutdownHook;

    private static ConnectionPool instance;

    private ConnectionPool() {
        registerCleanupTask();
    }

    public ConnectionFactory getConnectionFactory(final String connectionUri) {
        return factories.computeIfAbsent(connectionUri, ConnectionFactory::new);
    }

    public static Connection getConnectionForUrl(String connectionUrl) throws IOException, TimeoutException {
        return getInstance().getOrCreateConnection(connectionUrl);
    }

    protected synchronized Connection getOrCreateConnection(final String connectionUri) throws IOException, TimeoutException {
        final ConnectionFactory factory = getConnectionFactory(connectionUri);
        Connection connection = connections.get(connectionUri);
        if (connection == null) {
            connection = factory.createConnection();
            connections.put(connectionUri, connection);
        }
        return connection;
    }

    private synchronized void closeAllConnections() {
        log.info("Starting connection pool cleanup task");
        connections.values().forEach(Connection::close);
        connections.clear();
    }

    private void registerCleanupTask() {
        if (this.shutdownHook == null) {
            this.shutdownHook = new Thread(() -> {
                closeAllConnections();
            });
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }

    private synchronized static ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

}
