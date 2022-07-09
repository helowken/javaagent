NAME = test
BIN = $(NAME).bin
DIR_DEP = ..

all: $(BIN)

HEADERS = $(DIR_DEP)/*.h

%.o : %.c $(wildcard $(HEADERS))
	$(CC) -o $@ -c $< 

$(BIN) : $(NAME).o $(DIR_DEP)/util.o
	$(CC) -o $@ $^

test: $(BIN)
	sudo ./$< $(ARGS)

.PHONY: clean 

clean:
	rm -f *.o *.bin

