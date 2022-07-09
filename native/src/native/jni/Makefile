JDK_BIN = $(JAVA_HOME)/bin
CLASS = JvmtiUtils
FULL_CLASS = agent.jvmti.$(CLASS)
DIR_CLASSES = classes
DIR_SO = ../../../../packaging/resources/common/lib/so
LIB_SO = lib$(shell echo $(FULL_CLASS) | sed -e 's/\./_/g').so
OPTS = -g -fno-strict-aliasing -fPIC -fno-omit-frame-pointer -W -Wall \
	   -Wno-unused -Wno-parentheses -I. -I$(JAVA_HOME)/include \
	   -I$(JAVA_HOME)/include/linux

SOURCES = $(wildcard *.c)

all: $(LIB_SO)

$(CLASS).class : $(CLASS).java
	mkdir -p $(DIR_CLASSES)
	$(JDK_BIN)/javac -d $(DIR_CLASSES) $<
	$(JDK_BIN)/javah -d . -cp $(DIR_CLASSES) $(FULL_CLASS)

$(LIB_SO): $(SOURCES)
	$(CC) $(OPTS) -Wl,-soname=$@ -static-libgcc -z noexecstack \
		-shared -o $@ $^ -lc
	mkdir -p $(DIR_SO)
	cp $@ $(DIR_SO)


.PHONY: clean 

clean:
	rm -rf *.o *.so $(DIR_CLASSES)
	rm -f $(DIR_SO)/$(LIB_SO)

