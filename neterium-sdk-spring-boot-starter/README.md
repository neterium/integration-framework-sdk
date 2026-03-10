# Neterium Client SDK : Spring Boot starter module

The **neterium-sdk-spring-boot-starter** is a convenience **Spring** package that bundles together everything you
need to use the Neterium SDK.

Instead of adding many individual dependencies, you may just add this starter, and it is going to pull in:

- the main [library](../neterium-sdk-spring-boot/README.md)
- related supporting libraries
- sensible default versions that work well together
- etc...

**pom.xml**

```xml

<dependencies>

    <dependency>
        <groupId>com.neterium.client</groupId>
        <artifactId>neterium-sdk-spring-boot-starter</artifactId>
        <version>${neterium-sdk.version}</version>
    </dependency>

</dependencies>
```

## Build

Please refer to the documentation in the [root](../README.md) module to learn how to build the SDK.
