APP = example
VERSION = 1.0-SNAPSHOT

QA_SERVER = demo.cast.org
PROD_SERVER = demo.cast.org

## If POM is pointing to a stable version, then no dependencies need
## to be locally built, and this should be set to a true (non-empty) value
BUILD_SNAPSHOTS = 1

# Path to base of SVN tree
BASEDIR = ../..

# Path to base of SVN tree from Google Code
# FIXME: is there a way to avoid having to just assume people might check it out at this location?
GCBASEDIR = ../../../gsvn

# Path to CWM modules
CWM = $(GCBASEDIR)/cast-wicket-modules/trunk

# Path to ISI module
ISI = $(GCBASEDIR)/udl-curriculum-toolkit/trunk/isi

## List of dependency modules to be built, in order (if not stable)
MODULES = $(if $(BUILD_SNAPSHOTS), \
	$(CWM) \
	$(ISI))

# URL for checking out CWM modules
CWMURL = https://cast-wicket-modules.googlecode.com/svn

# URL for checking out Curriculum Toolkit modules
UCTURL = https://udl-curriculum-toolkit.googlecode.com/svn

# Command line for copying WAR file to server
RSYNC ?= rsync -a -L --log-format='%f' --exclude '.svn' --exclude '*~' 

install: $(patsubst %, %-install, $(MODULES))
	mvn clean install

%-install:
	cd $* && mvn clean install
.PHONY: %-install

svnup: $(patsubst %, %-svnup, $(MODULES))
	svn up
.PHONY: %-svnup

%-svnup:
	cd $* && svn up --set-depth=infinity

checkout:
	svn checkout -N $(CWMURL) $(GCBASEDIR)/cast-wicket-modules
	svn up --set-depth=infinity $(CWM)
	svn checkout -N $(UCTURL) $(GCBASEDIR)/udl-curriculum-toolkit
	svn up -N $(GCBASEDIR)/udl-curriculum-toolkit/trunk
	svn up --set-depth=infinity $(ISI)

upload-qa:
	@echo "Uploading to $(QA_SERVER)"
	@$(RSYNC) --progress target/$(APP)-$(VERSION).war $(QA_SERVER):$(APP).war

upload-prod:
	@echo "Uploading to $(PROD_SERVER)"
	@$(RSYNC) --progress target/$(APP)-$(VERSION).war $(PROD_SERVER):$(APP).war