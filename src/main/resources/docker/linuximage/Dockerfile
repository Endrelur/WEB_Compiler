FROM archlinux

RUN pacman -Syuu --noconfirm

RUN pacman -S --noconfirm gcc

CMD gcc -x c++ - -o main && ./main