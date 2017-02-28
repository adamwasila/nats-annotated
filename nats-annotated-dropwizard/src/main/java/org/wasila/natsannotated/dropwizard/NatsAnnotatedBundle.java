/**
 * (C) Copyright 2016 Adam Wasila.
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
package org.wasila.natsannotated.dropwizard;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.nats.client.ConnectionFactory;
import org.wasila.nats.router.Router;

import java.util.ArrayList;
import java.util.List;

public abstract class NatsAnnotatedBundle<T extends Configuration> implements Managed, ConfiguredBundle<T> {

    private NatsConfiguration config;

    private Router router;
    private List<Object> resources;

    private String healthcheckName = "nats";

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        config = getNatsConfiguration(configuration);
        resources = new ArrayList<>();
        environment.lifecycle().manage(this);
        environment.healthChecks().register(healthcheckName, createHealthcheck());
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    public abstract NatsConfiguration getNatsConfiguration(T configuration);

    public void addResource(Object resource) {
        resources.add(resource);
    }

    public void addResource(Class<?> resourceClass) {
        resources.add(resourceClass);
    }

    @Override
    public void start() throws Exception {
        this.router = new Router(getUrl());
        for (Object resource : resources) {
            if (resource instanceof Class) {
                router.register((Class<?>)resource);
            } else {
                router.register(resource);
            }
        }
    }

    @Override
    public void stop() throws Exception {
        this.router.close();
        this.router = null;
    }

    private HealthCheck createHealthcheck() {
        return new NatsHealthcheck(new ConnectionFactory(getUrl()));
    }

    private String getUrl() {
        return String.format("nats://%s:%d", config.getHost(), config.getPort());
    }
}
