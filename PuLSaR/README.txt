
PuLSaR - Continuous Optimisation module of Broker@Cloud IST project


Prerequisites

    You must have Java 7 JDK or later installed on your system
    The JAVA_HOME environment variable pointing to the JDK installation
    The locations where you extract the various software artifacts do not contain whitespaces in their path
    A MariaDB (version 10.0) installed. Alternatively MySQL can also be used

Building PuLSaR from sources

    All software artifacts of PuLSaR are located in the following GitHub repository: https://github.com/SingularLogic/BrokerAtCloud.git
    Clone this repository locally to your machine.
    Alternatively you may download only PuLSaR project:  https://github.com/SingularLogic/BrokerAtCloud/tree/master/PuLSaR
    Create a new database in MariaDB using your favorite tool (e.g. HeidiSQL).
    Connect to the newly created database (e.g. using HeidiSQL) and execute script var/db/initialize_maria_db.sql or var/db/initialize_maria_db_with_sample_data.sql.
    Open a command prompt window (in Windows start cmd.exe; in *nix/Linux start a bash shell)
    Change directory to the location where PuLSaR files reside (e.g. cd brokeratcloud/PuLSaR).
    Edit file src/main/resources/feedback.properties and set the correct values to parameters db.conn-str, db.username, db.password.
    Give 'mvn clean package' in command prompt. (An internet connection is required the first time you run this command in order to download and cache dependencies)
    Run script bin\deps.bat (in Windows) or bin/deps.sh (in *nix/Linux). (An internet connection might be required the first time you run this command in order to download and cache dependencies)
    Unzip "fuseki.zip" file if there is no Fuseki already available.
    Run script bin\fuseki-setup.bat (in Windows) or bin/fuseki-setup.sh (in *nix/Linux).

Installing PuLSaR from binaries

    Unzip package containing PuLSaR binary files
    Create a new database in MariaDB using your favorite tool (e.g. HeidiSQL).
    Connect to the newly created database (e.g. using HeidiSQL) and execute script var/db/initialize_maria_db.sql or var/db/initialize_maria_db_with_sample_data.sql.
    Open a command prompt window (in Windows start cmd.exe; in *nix/Linux start a bash shell)
    Change directory to the location where PuLSaR files reside (e.g. cd brokeratcloud/PuLSaR).
    Edit file pulsar/classes/feedback.properties and set the correct values to parameters db.conn-str, db.username, db.password.
    Unzip "fuseki.zip" file if there is no Fuseki already available.
    Run script bin\fuseki-setup.bat (in Windows) or bin/fuseki-setup.sh (in *nix/Linux).

Re-Configure PuLSaR using source files

    To add/delete/modify pulsar users edit file jetty-users.properties.
    To change MariaDB connection settings edit file src/main/resources/feedback.properties and then recompile using command mvn package. You will need to restart PuLSaR in order the new settings to take effect.
    To change Pub/sub settings edit file src/main/resources/pubsub.properties and then recompile using command mvn package. You will need to restart PuLSaR in order the new settings to take effect.
    To change logging settings edit file src/main/resources/log4j.properties and then recompile using command mvn package. You will need to restart PuLSaR in order the new settings to take effect.

Re-Configure PuLSaR using binaries

    To add/delete/modify pulsar users edit file jetty-users.properties.
    To change MariaDB connection settings edit file pulsar/classes/feedback.properties. You will need to restart PuLSaR in order the new settings to take effect.
    To change Pub/sub settings edit file pulsar/classes/pubsub.properties. You will need to restart PuLSaR in order the new settings to take effect.
    To change logging settings edit file pulsar/classes/log4j.properties. You will need to restart PuLSaR in order the new settings to take effect.

Starting PuLSaR

    First start MariaDB if not already running.
    Run script bin\fuseki.bat (in Windows) or bin/fuseki.sh (in *nix/Linux).
    Run script bin\pulsar.bat (in Windows) or bin/pulsar.sh (in *nix/Linux).
    Run script bin\cli.bat (in Windows) or bin/cli.sh (in *nix/Linux).
    When CLI command prompt PULSAR> appers give feedback schedule 60000 and press ENTER.
    The following links should now be available on your system:
        Fuseki Control Panel:  http://localhost:3030/
        PuLSaR:  http://localhost:9090/
    You can log in using any user listed in file jetty-users.properties.

Stopping PuLSaR

    At CLI command prompt PULSAR> type q (shorthand for quit) and press ENTER.
    At PuLSaR console (bin\pulsar.bat) hit Ctrl+C in order to close console.
    At Fuseki console (bin\fuseki.bat) hit Ctrl+C in order to close console.
    Optionally, stop MariaDB.
