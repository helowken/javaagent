JDK_BIN = $(JAVA_HOME)/bin
CLASS = Test
CWD = $(shell pwd)
CR_CWD = /usr/src
BUILD_DIR = build
CR_BUILD_DIR = $(CR_CWD)/$(BUILD_DIR)
APP_NAME = javaApp
DIR_DEP = ../jni
NAME = attach
BIN = $(NAME).bin
DST_DIR = ../../../../packaging/resources/server/bin

all: $(BIN)

HEADERS = $(DIR_DEP)/*.h

%.o : %.c $(wildcard $(HEADERS))
	$(CC) -o $@ -c $<

$(BIN) : $(NAME).o $(DIR_DEP)/util.o
	$(CC) -o $@ $^
	cp $@ $(DST_DIR)

test: $(BIN)
	sudo ./$(BIN) $(ARGS)	


$(CLASS).class : $(CLASS).java
	mkdir -p $(BUILD_DIR)
	$(JDK_BIN)/javac -d $(BUILD_DIR) $<

run_app: $(CLASS).class
	docker run --rm \
		   --name $(APP_NAME) \
		   -w $(CR_CWD) \
		   -v "$(JAVA_HOME)":$(JAVA_HOME) \
		   -v "$(CWD)/$(BUILD_DIR)":$(CR_BUILD_DIR) \
		   ubuntu $(JDK_BIN)/java -cp $(CR_BUILD_DIR) $(CLASS)


.PHONY: clean 

clean:
	rm -rf *.o *.bin $(BUILD_DIR)
	rm -f $(DST_DIR)/$(BIN)



