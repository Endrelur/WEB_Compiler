FROM archlinux

RUN pacman -Syuu --noconfirm

RUN pacman -S --noconfirm gcc
#timeout after 10s
CMD g++ -x c++ - -o main && timeout 10 ./main