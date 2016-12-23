#!/bin/bash

docker run -it --name nats-tools --link nats --network nats_default touchify/nats-tools
