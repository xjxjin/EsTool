#!/bin/bash

#jar包名称
Jar_Name=EsTool

#java启动参数
Jvm="-Xmx512m -Xms512m"

#服务安装目录
Work_Dir=$(cd $(dirname $0);cd ..;pwd)
mkdir -p ${Work_Dir}/logs
#判断jar文件是否存在
if [[ ! -e ${Work_Dir}/lib/${Jar_Name}.jar ]];then
    echo "jar package does not exist"
    exit 1
fi

#启动服务命令
CMD="java -server -Dname=${Jar_Name} -Dloader.path=${Work_Dir}/lib/libs/ -Dspring.profiles.active=dev -Dspring.config.location=${Work_Dir}/config/ ${Jvm} -jar ${Work_Dir}/lib/${Jar_Name}.jar"

#启动服务
start()
{
    Service_PID=$(ps -ef |grep ${Jar_Name}|grep -v grep|awk '{print $2}')
    if [[ -n $Service_PID ]];then
        echo -e "${Jar_Name} is \033[1;32mExist\033[0m"
    else
        nohup $CMD > ${Work_Dir}/logs/console.log 2>&1 &
        echo -e "${Jar_Name} is \033[1;32mRunning\033[0m"
    fi
}

#关闭服务
stop()
{
    Service_PID=$(ps -ef |grep ${Jar_Name}|grep -v grep|awk '{print $2}')
    if [[ -n $Service_PID ]];then
        kill -9 $Service_PID
        echo -e "${Jar_Name} is \033[1;31mDown\033[0m"
        sleep 1
    fi
}

#重启服务
restart()
{
    stop
    sleep 5
    start
}

case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    *)
        echo $"Usage: $0 {start|stop|restart}"
        exit 1
esac