# akka-streams-nats

Start Local Docker 

```sh
docker run --name nats -d -p 4223:4223 -p 8223:8223 nats-streaming -p 4223 -m 8223
```

Run unit tests for API implementation.
