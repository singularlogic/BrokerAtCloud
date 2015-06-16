
Prerequisites

    You have Java 7 JDK installed on your system
    The JAVA_HOME environmental variable is pointing to the Java 7 installation
    The locations where you extract the various software artifacts do not contain whitespaces in their path
    You have MariaDB (version 10.0) installed

PuLSaR Installation (Continuous Optimisation module)

    All the software artifacts of PuLSaR are located in the following GitHub repository: https://github.com/SingularLogic/BrokerAtCloud.git
    Clone this repository locally to your machine.
    Alternatively you may download only PuLSaR project:  https://github.com/SingularLogic/BrokerAtCloud/tree/master/PuLSaR
    Create a new database in MariaDB using your favorite tool (e.g. HeidiSQL).
    Connect to the newly created database (e.g. using HeidiSQL) and execute script var/maria_db/initialize_db.sql or var/maria_db/initialize_db_with_sample_data.sql.
    Open a command prompt window (in Windows start cmd.exe; in *nix/Linux start a bash shell)
    Change directory to the location where PuLSaR files reside (e.g. cd brokeratcloud/PuLSaR).
    Edit file src/main/resources/feedback.properties and set the correct values to parameters db.conn-str, db.username, db.password.
    Give mvn clean package. (An internet connection is required the first time you run this command in order to download and cache dependencies)
    Run script bin\deps.bat (in Windows) or bin/deps.sh (in *nix/Linux). (An internet connection might be required the first time you run this command in order to download and cache dependencies)
    Unzip "UNZIP_this_first - fuseki.zip" file
    Run script bin\fuseki-setup.bat (in Windows) or bin/fuseki-setup.sh (in *nix/Linux).

Configure PuLSaR

    To add/delete/modify pulsar users edit file jetty-users.properties. After saving changes copy file into bin directory and overwrite any existing file.
    To change MariaDB connection settings edit file src/main/resources/feedback.properties and then recompile using command mvn package. You will need to restart PuLSaR in order the new settings to take effect.
    To change Pub/sub settings edit file src/main/resources/pubsub.properties and then recompile using command mvn package. You will need to restart PuLSaR in order the new settings to take effect.
    To change logging settings edit file src/main/resources/log4j.properties and then recompile using command mvn package. You will need to restart PuLSaR in order the new settings to take effect.

Starting PuLSaR

    First start MariaDB if not already running.
    Run script bin\fuseki.bat (in Windows) or bin/fuseki.sh (in *nix/Linux).
    Run script bin\pulsar.bat (in Windows) or bin/pulsar.sh (in *nix/Linux).
    Run script bin\cli.bat (in Windows) or bin/cli.sh (in *nix/Linux).
    When CLI command prompt PULSAR> write give feedback schedule 60000 and press ENTER.
    The following links should now be available on your system:
        Fuseki Control Panel:  http://localhost:3030/
        PuLSaR:  http://localhost:9090/
    You can logging using any user listed in file jetty-users.properties.

Stopping PuLSaR

    At CLI command prompt PULSAR> type q (shorthand for quit) and press ENTER.
    At PuLSaR console (bin\pulsar.bat) hit Ctrl+C in order to close console.
    At Fuseki console (bin\fuseki.bat) hit Ctrl+C in order to close console.
    Optionally, stop MariaDB.
