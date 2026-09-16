# COMP3011 Assignment 1

Java Spring Boot speech-to-text web application.

## Author

Harleen Kaur Dhillon

## Description

A web application that records microphone audio and sends it
to a Java backend for speech-to-text transcription using a
cloud speech-to-text API.

## Testing

The application includes automated regression tests for the REST API.

The tests verify:

- The uptime endpoint returns HTTP 200.
- The uptime response contains the required server timing fields.
- The global statistics endpoint returns HTTP 200.
- The global statistics response contains input and output token counters.
- More than 200 concurrent requests can be handled successfully.

The concurrency test sends 250 simultaneous requests to the uptime
endpoint and verifies that all requests return HTTP 200.

### Concurrency Design

The application uses Java's asynchronous HttpClient `sendAsync()`
method when communicating with the OpenAI Cloud API.

The transcription controller returns a `CompletableFuture`, allowing
the request to complete asynchronously after the external Cloud STT
service responds.

This design avoids unnecessarily blocking server request threads
while waiting for the external API.

### API Key Security

The OpenAI API key is provided through the `OPENAI_API_KEY`
environment variable on TITAN.

The API key is not hard-coded in the source code and is not printed
or returned by the application.

### Shutdown Safety

The shutdown endpoint uses an `AtomicBoolean` to ensure that only one
shutdown operation can begin at a time. Concurrent shutdown requests
receive HTTP 409 when a shutdown is already in progress.