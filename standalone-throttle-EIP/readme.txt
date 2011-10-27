Proof of concept test.
----------------------
Test Enterprise patterns:
  * Throttle: http://camel.apache.org/throttler.html
  * Message Router: http://camel.apache.org/message-router.html

Plan:
Crate a test that will generate 100 messages. It will use ThrottleDemo to put 100 msgs on a throttled queue and 100
msgs on a non-throttled queue. The test will then verify that messages sent over the throttled route was indeed throttled.


