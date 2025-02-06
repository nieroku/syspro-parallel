SOURCES := $(wildcard *.java)
CLASSES := $(SOURCES:%.java=build/%.class)
RUNNABLES := $(CLASSES:build/%.class=%)

all: $(RUNNABLES)

.PHONY: all clean

build:
	mkdir $@

$(CLASSES): build/%.class:%.java | build
	javac -cp build -d build $<

$(RUNNABLES): %:build/%.class
	java -cp build $@

clean:
	rm -rf build
