This is an example application for using the UDL Curriculum Toolkit,
developed as part of a National Science Foundation collaborative grant
to CAST, Educational Development Corporation, and the University of Michigan,
in which we explored the application of Universal Design for Learning to 
online inquiry science curricula.

QUICK INSTALLATION INSTRUCTIONS

If you do not intend to change the Java code, you may want to follow the
simple installation instructions at [TBD] http://code.google.com/p/udl-curriculum-toolkit

GETTING SET UP TO EDIT / DEVELOP THIS CODE

You will need to check out the CAST Wicket Modules libraries from:
	http://cast-wicket-modules.googlecode.com/svn/trunk
and build them with Maven.
If using Eclipse, use 'Check out as Maven Project' via the Maven2 Eclipse plugin.

Then, check out the source code for the ISI module and Example Application from:
	http://udl-curriculum-toolkit.googlecode.com/svn/trunk/isi
	http://udl-curriculum-toolkit.googlecode.com/svn/trunk/example
and build these with Maven.

The result should be an example.war file that you can install into a Java webapp server.
It has only been tested with Tomcat version 6.

To configure and run project:

 1. Build all modules, as above.

 2. Create an empty DB and a DB user account that has full access to that DB.
    Only tested with the PostgreSQL database so far.

 3. Create hibernate XML config file.
    Sample of what this should contain is available in this directory
    example-hibernate.xml
    This file must be modified to contain the correct link to the database 
    and user/password created in the previous step.

    Note: This file normally lives outside of your Eclipse
    project directories. 

 4. Create a configuration file that sets system-dependent values for
    the app.  A sample is available in this directory, called
    example.config.

    Set isi.contentDir to the full path name of the directory where your
    XML content can be found. To use the sample content included with this
    application, point this to the "content" directory which is
    in the same directory as this README file.  [FIXME: Not uploaded to Google Code yet]

    Set isi.skinDir to the theme directory to use; the one to start with
    is also in the directory of this README.

    Set "cwm.hibernateConfig" to the file created in step 3.

    Note: The config file normally lives outside of your Eclipse
    project directories.

 5. Configure location of the config file in either in Tomcat's
    conf/server.xml, or conf/Catalina/localhost/example.xml, containing
    something like this: 
  
    <Context path="/example" .... >
      <Parameter name="propertiesFile" value="/opt/tomcat/example.config"/>
    </Context>
    
    The pathname after value=" should be the full pathname of the app.properties file
    you created.

  * Start up app on configured server

  * Log in as admin / admin

Once you are logged in to the administrative interface, you can either
use the link to see the "student" view (which includes the content),
or you can create student and teacher accounts.
