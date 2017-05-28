Plugin Utils
=======

A collection of useful plugin libraries


Maven repo: 
```
<repositories>
    <repository>
        <id>think-repo</id>
        <url>http://repo.thinkofdeath.co.uk/</url>
    </repository>
</repositories>
```

Command Base
----

A general command library

Javadocs: http://ci.thinkofdeath.co.uk/job/plugin-utils/ws/command-base/target/site/apidocs/index.html

Usage examples: http://ci.thinkofdeath.co.uk/job/plugin-utils/ws/command-base/target/site/apidocs/uk/co/thinkofdeath/command/CommandManager.html

Maven:
```
<dependencies>
    <dependency>
        <groupId>uk.co.thinkofdeath</groupId>
        <artifactId>command-base</artifactId>
        <version>0.2-SNAPSHOT</version>
    </dependency>
</dependencies>
```

Command Bukkit
------

A command library with bukkit specific features.
Depends on command-base

Javadocs: http://ci.thinkofdeath.co.uk/job/plugin-utils/ws/command-bukkit/target/site/apidocs/index.html   
     
Maven:
```
<dependencies>
    <dependency>
        <groupId>uk.co.thinkofdeath</groupId>
        <artifactId>command-bukkit</artifactId>
        <version>0.2-SNAPSHOT</version>
    </dependency>
</dependencies>
```
