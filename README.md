# NATS pub/sub declarative interface

What you see here is a quick hack to prove it is possible to use messaging publish&subscribe pattern with API similar to modern REST interfaces.

Publisher part is heavy inspired on Feign declarative interfaces and subscriber part is inspired by JAX-RS resources.

Note that currently it is just a quick proof of concept, toy to play with, not a library ready to use.

Keep in mind that there are already implemented spring bindings for NATS, that support similar annotation based approwach:
https://github.com/cloudfoundry-community/java-nats/tree/master/client-spring

## General TODO:

  * finalize API and remove code shortcuts and all bad code
  * add proper exception handling
  * integrate logging
  * add composite subjects
  * support request-reply pattern
  * make it messaging technology agnostic (pluggable?) to support other msg buses, eg. RabbitMQ
  * dropwizard bundle to ease configuration and integration with dropwizard framework
