# Notice:
# The work in this repo is continued in a new mono repo here: https://github.com/ELIXIR-NO/FEGA-Norway

# Direct link inside mono repo: https://github.com/ELIXIR-NO/FEGA-Norway/tree/main/lib/clearinghouse

# clearinghouse
[![Build Status](https://github.com/elixir-oslo/clearinghouse/workflows/Java%20CI/badge.svg)](https://github.com/elixir-oslo/clearinghouse/actions)
[![CodeFactor](https://www.codefactor.io/repository/github/elixir-oslo/clearinghouse/badge)](https://www.codefactor.io/repository/github/elixir-oslo/clearinghouse)
[![Download](https://img.shields.io/badge/GitHub%20Packages-Download-GREEN)](https://maven.pkg.github.com/elixir-oslo/clearinghouse/no.uio.ifi.clearinghouse/1.1.0/clearinghouse-1.1.0.jar)

## Maven Installation
To include this library to your Maven project add following to the `pom.xml`:

```xml

...

    <dependencies>
        <dependency>
            <groupId>no.elixir</groupId>
            <artifactId>clearinghouse</artifactId>
            <version>VERSION</version>
        </dependency>
    </dependencies>

...

    <repositories>
        <repository>
            <id>github</id>
            <name>elixir-oslo-clearinghouse</name>
            <url>https://maven.pkg.github.com/elixir-oslo/clearinghouse</url>
        </repository>
    </repositories>

...

```
