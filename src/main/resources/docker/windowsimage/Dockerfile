FROM bitnami/minideb

RUN apt update

RUN apt install -y build-essential

CMD g++ -x c++ - -o main && ./main