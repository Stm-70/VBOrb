# Copyright (c) 2000 Martin.Both
#

.SUFFIXES:
.SUFFIXES: .jp .java .class

.jp.java:
	gcc -E -P -C -xc $*.jp > $*.java

.java.class:
	javac $*.java

SHELL   = /usr/bin/ksh

IDL_OBJS = DB/ColumnData.class DB/ColumnDataHelper.class \
	DB/ColumnDataHolder.class

DBSERVER_OBJS = db/ManagerImpl.class db/ConnectionImpl.class \
	db/StatementImpl.class db/CursorImpl.class
DBSERVER_SRCS = $(DBSERVER_OBJS:.class=.java)


DBSERVER:		db/Server.class

db/Server.class:		db/Server.java \
	${IDL_OBJS} ${DBSERVER_SRCS} ${DBSERVER_OBJS}

run:
	java db.Server /usr/local/apache/htdocs/IOR

clean:
	rm -f `find ./db -name '*.class'`

classes:	db/Server.class
	rm -rf ./classes
	mkdir ./classes
	cp -R ./free ./classes
