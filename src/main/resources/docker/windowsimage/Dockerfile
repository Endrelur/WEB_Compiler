FROM bitnami/minideb

RUN apt update

RUN apt install -y build-essential
#times out after 10s
CMD g++ -x c++ - -o main && timeout 10 ./main