SOURCES := $(wildcard *.java)
CLASSES := $(SOURCES:%.java=build/%.class)
REPORTS := $(CLASSES:build/%.class=%_jstack.txt)

all: $(REPORTS)

.PHONY: all clean

build:
	mkdir $@

$(CLASSES): build/%.class:%.java | build
	javac -cp build -d build $<

$(REPORTS): %_jstack.txt:build/%.class
	./collect_jstack.sh build $(@:%_jstack.txt=%) $@

clean:
	rm -rf build
	rm -rf *._jstack.txt
