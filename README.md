WizToWar - Have your cake and eat it too
========================================

WizToWar is a simple library that enables a [Dropwizard](http://dropwizard.io) service to also be deployable in a WAR container such as Tomcat.

By following the steps in the usage section below you will be able to create both a Dropwizard jar and a WAR of the same
service.


Caveat emptor:
--------------

* Only tested on Tomcat 7
* No support for bundles
* Many features untested
* Goes against the whole philosophy of Dropwizard...

Usage
------

Include the wiztowar jar as a dependency:

	<dependency>
		<groupId>com.twilio</groupId>
		<artifactId>wiztowar</artifactId>	
		<version>1.3</version>
	</dependency>

Create a new class for your application like this:

	package com.twilio.mixerstate;

	import com.google.common.io.Resources;
	import com.twilio.wiztowar.DWAdapter;
	import com.yammer.dropwizard.Service;

	import java.io.File;
	import java.net.URISyntaxException;
	import java.net.URL;


	public class MixerStateDWApplication extends DWAdapter<MixerStateServiceConfiguration> {
		final static MixerStateService service = new MixerStateService();
		
		/**
		* Return the Dropwizard service you want to run.
		*/
		public Service getSingletonService(){
			 return service;
		}
    		
		/**
		* Return the File where the configuration lives.
		*/
		@Override
		public File getConfigurationFile() {

			URL url = Resources.getResource("mixer-state-server.yml");
			try {
				return new File(url.toURI());
			} catch (URISyntaxException e) {
				throw new IllegalStateException(e);
			}	
		}
	}


Create a main/webapp/WEB-INF/web.xml file:
------------------------------------------

	<?xml version="1.0" encoding="UTF-8"?>
	<web-app>
		<!--- This listener is required to hook in to the lifecycle of the WAR -->
		<listener>
			<listener-class>com.twilio.wiztowar.ServletContextCallback</listener-class>
		</listener>
		<servlet>
			<servlet-name>Jersey REST Service</servlet-name>
			<servlet-class>com.sun.jersey.spi.container.servlet.ServletContainer</servlet-class>

			<!--- Replace this with your DWAdapter derived application -->
			<init-param>
				<param-name>javax.ws.rs.Application</param-name>
				<param-value>com.twilio.mixerstate.MixerStateDWApplication</param-value>
			</init-param>
			<init-param>
				<param-name>com.sun.jersey.api.json.POJOMappingFeature</param-name>
				<param-value>true</param-value>
			</init-param>
			<load-on-startup>1</load-on-startup>
		</servlet>

		<servlet-mapping>
			<servlet-name>Jersey REST Service</servlet-name>
			<url-pattern>/*</url-pattern>
		</servlet-mapping>
	</web-app>

Make sure you also build a WAR artifact
---------------------------------------------

There are two alternatives to building a war:

### Add instructions to also build a WAR

This goes in `<build><plugins>` section:

	<plugin>
		<groupId>org.apache.maven.plugins</groupId>
		<artifactId>maven-war-plugin</artifactId>
		<version>2.4</version>
		<executions>
			<execution>
				<id>default-war</id>
				<phase>package</phase>
				<goals>
					<goal>war</goal>
				</goals>
			</execution>
		</executions>
		<configuration>
            <webappDirectory>target/webapp</webappDirectory>
		</configuration>
	</plugin>

### Change packaging of your Dropwizard service

If you do not intend to run the Dropwizard service standalone, you can simply change the "packaging" element in pom.xml to be "war" instead of "jar".


