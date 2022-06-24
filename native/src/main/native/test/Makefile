SOURCES := $(wildcard *.c)
SO := agent_jvmti_JvmtiUtils
DEST := test

.PHONY: all clean build

build: $(SOURCES)
	gcc -fPIC -o $(DEST) $(SOURCES) -L.. -l$(SO)

clean:
	rm -f $(DEST)
