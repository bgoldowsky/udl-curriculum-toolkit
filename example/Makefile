APP = example
VERSION = 1.0-SNAPSHOT

QA_SERVER = demo.cast.org
PROD_SERVER = demo.cast.org

## If POM is pointing to a stable version, then no dependencies need
## to be locally built, and this should be set to a true (non-empty) value
BUILD_SNAPSHOTS = 1

# Path to base of SVN tree
BASEDIR = ../../../..

# Path to CWM modules
CWM = $(BASEDIR)/cwm

# Path to ISI module
ISIDIR = ..

MODULES = $(if $(BUILD_SNAPSHOTS), \
	$(BASEDIR)/audioapplet/trunk \
	$(CWM)/cwm-base/trunk \
	$(CWM)/cwm-agents/trunk \
	$(CWM)/cwm-drawtool/trunk \
	$(CWM)/cwm-components/trunk \
	$(CWM)/cwm-audioapplet/trunk \
	$(CWM)/cwm-applets/trunk \
	$(CWM)/cwm-data/trunk \
	$(CWM)/cwm-mediaplayer/trunk \
	$(CWM)/cwm-xml/trunk \
	$(CWM)/cwm-glossary/trunk \
	$(CWM)/cwm-indira/trunk \
	$(CWM)/cwm-tag/trunk \
	$(ISIDIR)/isi)

RSYNC ?= rsync -a -L --log-format='%f' --exclude '.svn' --exclude '*~' 

install: $(patsubst %,%-install, $(MODULES))
	mvn clean install

%-install:
	cd $* && mvn clean install
.PHONY: %-install

svnup: $(patsubst %, %-svnup, $(MODULES))
	svn up
.PHONY: %-svnup

%-svnup:
	cd $* && svn up --set-depth=infinity

checkout: checkout-parent-dirs $(patsubst %, %-checkout, $(MODULES)) svnup

%-checkout:
	svn up -N $(subst /trunk, , $*)
	svn up -N $*

checkout-parent-dirs:
	svn up -N $(CWM)
	svn up -N $(CWM)/audioapplet
	svn up -N $(CWM)/audioapplet/trunk
	svn up -N $(CWM)/nsfc
	svn up -N $(CWM)/nsfc/trunk
	svn up -N $(CWM)/nsfc/trunk/app
	svn up -N $(CWM)/nsfc/trunk/app/isi

upload-qa:
	@echo "Uploading to $(QA_SERVER)"
	@$(RSYNC) --progress target/$(APP)-$(VERSION).war $(QA_SERVER):$(APP).war

upload-prod:
	@echo "Uploading to $(PROD_SERVER)"
	@$(RSYNC) --progress target/$(APP)-$(VERSION).war $(PROD_SERVER):$(APP).war
