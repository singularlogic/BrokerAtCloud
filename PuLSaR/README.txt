
				==============================================
				  "PuLSaR" INSTALLATION & USAGE INSTRUCTIONS  
				==============================================
						( 26 / 01 / 2015 )


INSTALLATION  (on Windows)
============

1. Uncompress package

2. Uncompress "fuseki.zip"

3. Run file "bin\fuseki-setup.bat" (double click on it)


COMPILE - BUILD
===============

1. Run "bin\command.bat" (a console window with command prompt should open)

2. Give "mvn clean package" at command prompt (normally should report success)

3. Give "bin\deps" at command prompt (normally should report success)

After this process the "target" directory should have been created and contain file "target\PuLSaR.war"


CHECK INSTALLATION
==================

1. Run "bin\fuseki.bat"  (a console window should open, and stay open :))
	Use Ctrl+C to close console window

2. Run "bin\fuseki-mem.bat"
	Use Ctrl+C to close console window

3. Run "bin\pulsar.bat"
	Use Ctrl+C to close console window

4. Run "bin\pulsar_output.bat" (a console window should open and file OUTPUT.txt created in base directory)
	Use Ctrl+C to close console window


CONFIGURE USERS
===============

1. Edit "bin\jetty-users.properties" file

2. Add/Remove/Change file contents

3. Save and close file (it takes about 5 seconds for pulsar to sync, if it is running)

EACH LINE contains the credentials for ONE user

FORMAT:
<username> : <password-in-plain-text>, <role>[,<role>]*

Roles can be:
    admin	administrator role (grants access to all pulsar features)
    sc		service consumer role (grants access to Consumer preference profile & Recommendations forms)
    sp		service provider role (grants access to provider notification form)
    broker	broker role (not currently used)


START PULSAR  (using persistent RDF repository)
============

1. Run "bin\fuseki.bat" (double click on it)

2. Run "bin\pulsar.bat" or "bin\pulsar_output.bat"

3. Direct your browser to "http://localhost:9090/" or double click "Pulsar Demo.url" shortcut


START PULSAR  (using memory-based, non-persistent RDF repository)
============

1. Run "bin\fuseki-mem.bat"

2. Run "bin\pulsar.bat" or "bin\pulsar_output.bat"

3. Direct your browser to "http://localhost:3030/"  or double click "Fuseki.url" shortcut

4. Click "Select" on Fuseki Control Panel webpage

5. Go to the bototm of the "Fuseki Query" webpage, at "File Upload" section and load the desired TTL files. 
   TTL files with Broker@Cloud ontologies, CAS broker policy (partial) and ICCS service attributes hierarchy
   are provided in "var\TTL files + exports" directory. Some of these files contain a few corrections and therefore
   differ from those in ERoom (https://project.sintef.no/eRoom) [24/11/2014].

6. Direct your browser to "http://localhost:9090/"  or double click "Pulsar Demo.url" shortcut


EXIT PULSAR
===========

1. Press Ctrl+C at the console window of "pulsar.bat" or "pulsar_output.bat"

2. Press Ctrl+C at the console window of "fuseki.bat" or "fuseki-mem.bat"

   Note: in case of "fuseki-mem.bat" the contents of RDF repository will be lost
         unless you have exported them first (see next)


EXPORT RDF REPOSITORY CONTENTS
==============================

1. Direct your browser to "http://localhost:3030/"  or double click "Fuseki.url" shortcut

2. Click "Select" on control panel webpage

3. Go to the top of the webpage, at "SPARQL Query" section and paste the following commands in text area.
   At Output drop-down list select "Text" and press "Get Results" button.
   When prompted, select a directory and a filename (usually with .TTL extension) to save the export file.
   Note that in some browser configurations, download will start automatically and store export file in a default location.

prefix cas: <http://www.broker-cloud.eu/service-descriptions/CAS/broker#>
prefix cas-pref-att: <http://www.broker-cloud.eu/service-descriptions/CAS/broker#>
prefix sp: <http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#> 
prefix fc: <http://www.broker-cloud.eu/service-descriptions/CAS/categories#>
prefix usdl-sla: <http://www.linked-usdl.org/ns/usdl-sla#> 
prefix usdl-sla-cb: <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#> 
prefix usdl-core: <http://www.linked-usdl.org/ns/usdl-core#> 
prefix usdl-core-cb: <http://www.linked-usdl.org/ns/usdl-core/cloud-broker>
prefix usdl-business-roles: <http://www.linked-usdl.org/ns/usdl-business-roles#> 
prefix usdl-pref: <http://www.linked-usdl.org/ns/usdl-pref#> 
prefix pref-att:  <http://www.brokeratcloud.eu/v1/opt/SERVICE-ATTRIBUTE/> 
prefix owl: <http://www.w3.org/2002/07/owl#>  
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  
prefix xml: <http://www.w3.org/XML/1998/namespace> 
prefix xsd: <http://www.w3.org/2001/XMLSchema#> 
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
prefix foaf: <http://xmlns.com/foaf/0.1/> 
prefix dcterms: <http://purl.org/dc/terms/> 
prefix gr: <http://purl.org/goodrelations/v1> 
prefix s: <http://schema.org/> 
prefix skos: <http://www.w3.org/2004/02/skos/core#>
prefix attr: <http://www.brokeratcloud.eu/v1/opt/SERVICE-ATTRIBUTE#>
prefix gr: <http://purl.org/goodrelations/v1#>
prefix skos: <http://www.w3.org/2004/02/skos/core#>

prefix cpp-profile: <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE-PROFILE#>
prefix cpp-pref: <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE#>
prefix cpp-expr: <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE-EXPRESSION#>
prefix cpp-fld-profile: <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE-PROFILE/>
prefix cpp-fld-pref: <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE/>
prefix cpp-fld-expr: <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE-EXPRESSION/>

construct where {?s ?p ?o }


ENTER PULSAR
============

1. Direct your browser to "http://localhost:9090/"  or double click "Pulsar Demo.url" shortcut

2. Give your credentials (or click on one of the 3 preconfigurd users :))

3. Click Login


					==========================
					***  IMPORTANT NOTICE  ***
					==========================

	When editing the Broker Policy ("Policy editor" form) several things change in RDF repository
	including property names, class names and individual names. Any artifacts referring to them 
	will become inconsistent and tools using them will not work correctly.

	* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	*											      *
	*   THEREFORE, IT IS CRUCIAL TO COMPLETE BROKER POLICY LOAD / EDTING BEFORE START USING IT!!  *
	*											      *
	* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

	In Pulsar this means that you first need to complete loading/editing broker policy and then
	start creating consumer preference profiles and recommendations.

	The suggested approach for Pulsar is:
	a.	Login an "admin"
	b.	Load ontology files and ICCS service attribute hierarchy file
	c.	Edit service attribute hierarchy (Extended SMI) at "Serv. Attr. Mgnt" form
	d.	Load any pre-existing broker policy files
	e.	Logout and Login as a consumer (e.g. "sc1")
	f.	Create a new consumer preference profile
	g.	Request recommendations

