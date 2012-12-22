#!/bin/sh

################################################################################
##  Balsa application startup script                                          ##
################################################################################

# Get the application dir
BIN_DIR=`dirname $0`
APP_DIR=`cd "$BIN_DIR/.."; pwd`

## Expected environment vars
#JAVA_HOME=""
#JAVA_OPTS="-Xmx512M"

## Balsa variables
BALSA_APP="${package}.App"
BALSA_ARGS=$@

## Construct the classpath
CLASSPATH=""
SEP=""

for FILE in `ls "${APP_DIR}/lib"`
do
	CLASSPATH="${CLASSPATH}${SEP}${APP_DIR}/lib/${FILE}";
	SEP=":"
done

## Debug information (checks for --debug flag)
for ARG in $BALSA_ARGS
do
	if [ "$ARG" == "--debug" -o "$ARG" == "--trace" ]
	then
		echo "Using application directory: ${APP_DIR}"
		echo "Using Java Home: ${JAVA_HOME}, with"
		echo "      classpath: ${CLASSPATH}"
		echo "        options: ${JAVA_OPTS}"
		echo "     Balsa  app: ${BALSA_APP}"
		echo "     Balsa args: ${BALSA_ARGS}"
	fi
done

# Start the Balsa application
cd $APP_DIR
$JAVA_HOME/bin/java -classpath "$CLASSPATH" $JAVA_OPTS com.intrbiz.Balsa --app "${BALSA_APP}" $BALSA_ARGS