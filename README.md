
> [!WARNING]
> This README is provided for informational purposes only and does not create any support, maintenance, service level, or other service commitment by Neterium.
> 
> Please read carefully the [legal notice](#Legal-Notice) section below before using this repository.

> [!NOTE]
> The contents of this repository are intended for technical users (i.e., developers) who are assumed to be familiar with Neterium concepts, 
> as well as with the key features and functionality of the Neterium APIs. 
>
> More information on these aspects can be found at [https://portal.neterium.io](https://portal.neterium.io) 

# Neterium SDK

The Neterium Software Development Kit (SDK) is a kit for developing client applications leveraging the Neterium API.
It includes building blocks that can be used in a **Spring-based** application which needs to interface with JetScan™ & JetFlow™ API.

Basically it consists of a collection of ready-to-use and configurable components you can integrate in your java application, leveraging the development by focusing on data and business logic, 
keeping you away from technical considerations such as authentication, batching, throttling, etc...

## Content

- [neterium-sdk-spring-boot](neterium-sdk-spring-boot/README.md) : the SpringBoot module
- [neterium-sdk-spring-boot-starter](neterium-sdk-spring-boot-starter/README.md) : the SpringBoot starter module

## Requirements

- Java 21 or above
- Maven 3.9.10 or above
- Public internet access (no VPN required)

## Build

First clone Git repository:

```shell
git clone git@github.com:neterium/integration-framework-sdk.git
```

Then build the whole project (with all Maven modules) using the root [pom](pom.xml) :

```shell
cd integration-framework-sdk
mvn clean install
```

The build output should then look like:

```text
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for Neterium Client SDK 1.0.0-RC1:
[INFO] 
[INFO] Neterium Client SDK ................................ SUCCESS [  0.053 s]
[INFO] Neterium Client SDK :: SpringBoot .................. SUCCESS [  4.647 s]
[INFO] Neterium Client SDK :: SpringBoot Starter .......... SUCCESS [  0.019 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  4.952 s
[INFO] Finished at: 2026-01-27T11:40:05+01:00
[INFO] ------------------------------------------------------------------------
```

## Usage

Typical usages of available building blocks can be found in the provided
[sample applications](https://github.com/neterium/integration-framework-samples).

## IDE support

SDK jar includes Spring Boot configuration metadata which give developers useful information
on how to use the properties. The nice thing about it is that most IDEs can read them too,
giving us **autocomplete** of Spring properties, as well as other configuration **hints**.

## Contributing

See [CONTRIBUTING](CONTRIBUTING.md)

## Security

See [SECURITY](SECURITY.md)

# Legal Notice

## Purpose

This repository is intended to facilitate and accelerate integration with the Neterium screening API by providing reusable SDK components, wrappers, and reference integration examples. It is published to reduce implementation friction and to help developers build their own applications and workflows around the Neterium API.

This repository is intended for:
- Neterium customers,
- Neterium integration partners and other solution providers integrating the Neterium API, and
- Neterium internal teams supporting product, engineering, compliance, and go-to-market activities.

### What’s in this repository

This repository contains the following materials (collectively, the “Open Source Components”):
- SDKs, wrappers and helper modules enabling interaction with the Neterium API;
- Integration samples and reference implementations illustrating common integration patterns;
- A minimal demonstrator user interface (UI) provided for illustration purposes only; and
- Documentation and examples, including installation guidance and usage instructions.

### What this repository is not

For the avoidance of doubt, this repository and the Open Source Components are not:
- an end-to-end product;
- a full-featured user interface; or
- a managed service (including hosting, operations, monitoring, support, or incident handling).

End-to-end implementations (including alert/case management, storage, workflows and operationalisation) are typically delivered by Neterium’s integration partners and/or implemented by users within their own environments.

## License

Licensed under the Apache License, Version 2.0 - see the [LICENSE](LICENSE) file.

## Open Source Policy

See [POLICY](POLICY.md)

## Disclaimer

### Examples only / no product confusion

The Open Source Components are provided as generic components and integration examples only. They are not intended to constitute a complete product or a production-ready solution.

### No support

Neterium does not provide helpdesk support, incident handling, troubleshooting, consulting, or implementation assistance for the Open Source Components.

### No SLA / no maintenance commitment

Neterium makes no commitment to maintain, update, patch, correct, or otherwise improve the Open Source Components and provides no service level agreement (SLA), response times, availability commitments, or remediation timelines. Neterium may modify, suspend, or discontinue this repository (in whole or in part) at any time.

### AS IS / no warranty

The Open Source Components are provided on an “AS IS” basis, without warranties or conditions of any kind, whether express or implied, to the extent permitted by the applicable open-source license.

### Production use at user’s sole responsibility

Any use of the Open Source Components in production (including regulated or compliance-critical contexts) is undertaken solely at the user’s own risk and responsibility. Users remain responsible for determining suitability, testing, validation, security hardening, operational controls, and regulatory/compliance assessments.

### Partner positioning

This repository is intended to be complementary to Neterium’s integration partners and does not aim to provide an end-to-end solution.

### Trademarks

“Neterium” and related names and logos are trademarks and/or service marks of Neterium SRL. This repository does not grant any rights to use Neterium’s trademarks, except as necessary for reasonable and customary 
use in describing the origin of the Open Source Components and reproducing attribution notices.

## Contact

- Product / repository inquiries: product@neterium.com
- Security reports: security@neterium.com
- Legal / licensing questions: legal@neterium.com
