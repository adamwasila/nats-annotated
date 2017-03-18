# NATS pub/sub declarative interface

## Foreword

Simple library with minimal dependencies that uses concept simmilar to JAX-RS resources to subscribe to handle NATS messages directly. From the other side of message bus: publisher interfaces are inspired by Feign declarative interfaces.

## Quick start

Prebuilt releases are available here: https://bintray.com/adamwasila/maven/

Eg. to use with gradle, simply add following line to your dependencies:

```
compile 'org.wasila:nats-annotated-core:0.1.0'
```

Then you're ready to fly. Simplest receiver of given message:

```
@Subject("my")
public class Application {

    static class MyMessage {
        public String comment;
    }

    @Subscribe
    @Subject("subject")
    public void handle(MyMessage message) {
        System.out.println("received message: " + message.comment   );
    }

    public void start() throws IOException, TimeoutException {
        Router router = new Router();
        router.register(this);
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        new Application().start();
    }
}
```

For tesing you may use nats-pub tool from nats-go project:

```
$ nats-pub my.subject '{"comment" : "hello world!"}'
Published [my.subject] : '{"comment" : "hello world!"}'
```

Publisher that uses nats-annotated is as simple as:

```
public class PublisherApplication {

    static class MyMessage {
        public String comment;
        public MyMessage(String comment) {
            this.comment = comment;
        }
    }

    public interface SendMyMessage {
        @Publish(subject="my.subject")
        void sendMessage(MyMessage message);
    }

    public static void main(String[] args) {
        SendMyMessage sender = Publisher.builder().target(SendMyMessage.class, "nats://localhost:4222");
        sender.sendMessage(new MyMessage("hello world!"));
    }

}
```

After succesful execution you may now observe that message is properly handled:

```
received message: hello world!
```

## Disclaimer

Note that this is still 0.x version. Everything, including public API is subject to change.

If you use spring you may consider using this library instead:

https://github.com/cloudfoundry-community/java-nats/tree/master/client-spring

## General TODO:

  * finalize API and remove code shortcuts and all bad code
  * add proper exception handling
  * ~~integrate logging~~
  * ~~add composite subjects~~
  * ~~support request-reply pattern~~
  * make it messaging technology agnostic (pluggable?) to support other msg buses, eg. RabbitMQ
  * dropwizard bundle to ease configuration and integration with dropwizard framework
