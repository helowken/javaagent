#include <stdio.h>
#include <stdlib.h>
#include "../header/util.h"

int main(int argc, char *argv[]) {
	pid_t pid = atoi(argv[1]);
	if (tryToSetEuidAndEgid(pid) == 0)
	  printf("Set euid successfully.\n");
	return EXIT_SUCCESS;
}
