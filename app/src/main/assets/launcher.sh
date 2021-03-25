#!/system/bin/sh

pkill -f server
rootDir=/data/local/tmp

#exec
echo "exec~~~"
#path=${rootDir}/libfairy.so
fairy=${rootDir}/server

#rm -rf ${fairy}
#cp ${path} ${fairy}

exec ${fairy}
