## This Makefile is included for ease of building multiple projects
## consecutively, and creating distribution bundles.  It should not be
## necessary for normal use of the UDL Curriculum Toolkit.
## Source code compilation is done through maven, not make.

APP = example
VERSION = 1.0-SNAPSHOT

ISI_DIR = ../isi
CWM_DIR = ../../../cast-wicket-modules/trunk
DRAW_DIR = ../../../cwm-drawtool/trunk

MODULES = $(DRAW_DIR) $(CWM_DIR) $(ISI_DIR)

## Compile Example app
build:
	mvn clean install
.PHONY: build

## Compile Example app and its dependencies
install: $(patsubst %, %-install, $(MODULES)) build
.PHONY: install

%-install:
	cd $* && mvn clean install
.PHONY: %-install

## Update example app and dependencies
svnup: $(patsubst %, %-svnup, $(MODULES))
	svn up
.PHONY: %-svnup

%-svnup:
	cd $* && svn up --set-depth=infinity

CONF_FILES = example.xml example.config example-hibernate.xml example-logback.xml 

## Make a distribution bundle (for posting on Google Code site)
toolkit-bundle.zip:
	if [ -e toolkit-bundle ]; then rm -rf toolkit-bundle; fi
	mkdir toolkit-bundle
	rsync -Ca theme toolkit-bundle
	rsync -ta xxe.cast.org:/var/xdr/udlct-demo/ toolkit-bundle/content
	cp $(CONF_FILES) toolkit-bundle
	cp target/$(APP)-$(VERSION).war toolkit-bundle/$(APP).war
	chmod -R u+rwX toolkit-bundle
	zip -r toolkit-bundle.zip toolkit-bundle
.PHONY: toolkit-bundle.zip

## Command line for copying WAR file to server
RSYNC ?= rsync -a -L --log-format='%f' --exclude '.svn' --exclude '*~' 

upload-%:
	@echo "Uploading to $*"
	@$(RSYNC) --progress target/$(APP)-$(VERSION).war $*:$(APP).war

