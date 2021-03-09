FROM archlinux

RUN pacman -Syuu --noconfirm

RUN pacman -S --noconfirm gcc

CMD g++ -x c++ - -o main && ./main