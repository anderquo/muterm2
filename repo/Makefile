PROJECT     := muterm2
MAIN        := muterm.MuTerm

MSG         ?= none
USER        ?= edartuz

DIR_REPO    := ./repo
DIR_DIST    := ./dist
DIR_RELEASE := ./release
DIR_LIB     := ./lib
DIR_LOCALE  := ./locale
DIR_SCRIPTS := ./scripts
DIR_CSS     := ./css
DIR_BIN     := ./bin
DIR_SRC     := ./src
DIR_TOOLS   := ./tools
DIR_DOC     := ./doc

.PHONY: dist repo push release proguard locale run

dist:
	cd $(DIR_DIST) && rm -rf *
	cp -R -L $(DIR_LIB) $(DIR_DIST)/
	cp -R -L $(DIR_LOCALE) $(DIR_DIST)/
	cp -R -L $(DIR_SCRIPTS) $(DIR_DIST)/
	cp -R -L $(DIR_CSS) $(DIR_DIST)/
	cp -R -L $(DIR_DOC) $(DIR_DIST)/
	cp -R -L $(DIR_TOOLS)/*.sh $(DIR_DIST)/
	cp -R -L $(DIR_TOOLS)/*.bat $(DIR_DIST)/
	cd $(DIR_BIN) && $(JAVA_HOME)/../bin/jar -cf ../$(DIR_DIST)/$(PROJECT).jar .

release: dist
	rm -rf $(DIR_RELEASE)/*
	mkdir $(DIR_RELEASE)/$(PROJECT)
	cp -R -L $(DIR_DIST)/* $(DIR_RELEASE)/$(PROJECT)
	cd $(DIR_RELEASE); zip -r $(PROJECT)_`date +%Y.%m.%d-%H.%M`.zip $(PROJECT)
	rm -rf $(DIR_RELEASE)/$(PROJECT)
	
repo: release
	cd $(DIR_REPO) && rm -rf *
	cp -R -L $(DIR_LIB) $(DIR_REPO)
	cp -R -L $(DIR_LOCALE) $(DIR_REPO)
	cp -R -L $(DIR_SCRIPTS) $(DIR_REPO)
	cp -R -L $(DIR_CSS) $(DIR_REPO)
	cp -R -L $(DIR_TOOLS) $(DIR_REPO)
	cp -R -L $(DIR_SRC) $(DIR_REPO)
	#cp -R -L $(DIR_BIN) $(DIR_REPO)
	cp -R -L $(DIR_DOC) $(DIR_REPO)
	cp -R -L $(DIR_RELEASE) $(DIR_REPO)
	cp -R -L .settings $(DIR_REPO)
	- cp .* $(DIR_REPO)
	- cp * $(DIR_REPO)

push: repo
	git add $(DIR_REPO)
	git add $(DIR_RELEASE)
	git add $(DIR_DIST)
	git commit -m $(MSG)
	git push -u origin master
	
proguard: dist
	cp $(DIR_TOOLS)/$(PROJECT).pro $(DIR_DIST)
	cd $(DIR_DIST); java -jar ../tools/proguard.jar @$(PROJECT).pro -verbose
	
locale:
	@xgettext -o $(DIR_LOCALE)/$(PROJECT).pot -L Java --from-code=utf-8 --keyword=tr `find -L $(DIR_SRC) -name *.scala`

run: dist
	cd $(DIR_DIST) && ./$(PROJECT).sh; cd ..

