This is an example application for using the CAST Wicket Components
and the ISI base application - developed as part of the NSF
collaborative grant to apply UDL to inquiry science curriculum.

Source code location:
     https://xander.cast.org/svn/nsfc/trunk/app/example
Theme (HTML, CSS, Images):
     https://xander.cast.org/svn/nsfc/trunk/app/example/theme
Content (Example content is "The Call of the Wild"):
     https://xander.cast.org/svn/nsfc/trunk/app/example/content

To compile, you will need the following dependencies.  
Check pom.xml to see whether they are currently listed with a
SNAPSHOT version number.  If so, you will need to download the source
code and build it yourself.  If not, then maven ought to be able to
download and install the dependency for you.
   cwm-model
   cwm-xml
   cwm-indira
   cwm-tag
   cwm-glossary
   cwm-applets
   cwm-components
   cwm-wordconnections
   isi
Source code for all of the above is available under
     https://xander.cast.org/svn/cwm
except for isi, which is at
	 https://xander.cast.org/svn/nsfc/trunk/app/isi

In addition, cwm-wordconnections depends on the wordconnections project at
     https://xander.cast.org/svn/wordconnections/trunk
If a SNAPSHOT version is currently in use, you'll need to check that
out as well.

To configure and run project:

 1. Check out all required cwm projects and isi projects (see URLs above)
    If using Eclipse, use 'Check out as Maven Project' via the Maven2
    Eclipse plugin.

 2. Create an empty DB and a DB user account that has full access to that DB.
    Only tested with PostgreSQL so far.

 3. Create hibernate XML config file.
    Sample of what this should contain is available in this directory
    example-hibernate.xml
    This file must point to the database and user created in the
    previous step.

    Note: This file normally lives outside of your Eclipse
    project directories. 

 4. Create app.properties file that sets system-dependent values for
    the app.  A sample is available in this directory, called
    example.properties.

    Set isi.contentDir to the full path name of the content
    directory. To use the sample content included with this
    application, point this to the "content" directory which is
    in the same directory as this README file.

    Set isi.skinDir to the theme directory to use; the one to start with
    is also in the directory of this README.

    Set "hibernate.configFilePath" to the file created in step 3.

    Note: The app.properties file normally lives outside of your Eclipse
    project directories.

 5. Configure location of app.properties file in either in Tomcat's
    conf/server.xml, or conf/Catalina/localhost/example.xml, containing
    something like this: 
  
    <Context path="/example" .... >
      <Parameter name="propertiesFile" value="/opt/tomcat/app.properties"/>
    </Context>
    
    The pathname after value=" should point to the app.properties file
    you created.

  * Start up app on configured server

  * Log in as admin / admin

Once you are logged in to the administrative interface, you can either
use the link to see the "student" view (which includes the content),
or you can create student and teacher accounts.
