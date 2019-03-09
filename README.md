# aws-lambda-java8-runtime

A decompiled version of the AWS lambda java 8 runtime.

Specifically, it is a [fernflower](https://github.com/fesh0r/fernflower) decompiled version of `LambdaSandboxJava-1.0.jar`, sourced from the [lambci/docker-lambda java8 runtime](https://github.com/lambci/docker-lambda/tree/master/java8/run) which was [dumped from the AWS lambda environment](https://github.com/lambci/docker-lambda/blob/master/base/dump-java8/src/main/java/org/lambci/lambda/DumpJava8.java) to [s3](https://lambci.s3.amazonaws.com/fs/java8.tgz).

See the [Makefile](Makefile) to understand how this repo was built.