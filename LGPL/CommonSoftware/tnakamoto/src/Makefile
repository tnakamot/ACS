# 
# IDL Files and flags
# 
IDL_FILES = HolaDemo
IDL_TAO_FLAGS =
USER_IDL =
HolaDemoStubs_LIBS = acscomponentStubs

#
# Jarfiles: We must distinguish between component implementation classes and other classes.
#
COMPONENTS_JARFILES = tnakamotoComp
tnakamotoComp_DIRS = alma/tnakamoto/HolaDemoImpl

DEBUG = on
#COMPONENT_HELPERS=on
XML_IDL="ObsProposal=alma.xmljbind.test.obsproposal.ObsProposal; \
         SchedBlock=alma.xmljbind.test.schedblock.SchedBlock"

#
# Scripts (public and local)
# ----------------------------
SCRIPTS         =
SCRIPTS_L       =

#>>>>> END OF standard rules

#
# INCLUDE STANDARDS
# -----------------

MAKEDIRTMP := $(shell searchFile include/acsMakefile)
ifneq ($(MAKEDIRTMP),\#error\#)
   MAKEDIR := $(MAKEDIRTMP)/include
    include $(MAKEDIR)/acsMakefile
endif

#
# TARGETS
# -------

all:	do_all
	@echo " . . . 'all' done" 

clean : clean_all 
	@echo " . . . clean done"

clean_dist : clean_all clean_dist_all 
	@echo " . . . clean_dist done"

man   : do_man 
	cp mainDoxyPage.html ../doc/html/main.html
	@echo " . . . man page(s) done"

install : install_all
	@echo " . . . installation done"

#___oOo___


