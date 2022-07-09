#include <stdio.h>
#include <stdlib.h>
#include "../util.h"

int main(int argc, char *argv[]) {
	if (argc < 2) {
		fprintf(stderr, "Usage: %s pid...\n", argv[0]);
		exit(EXIT_FAILURE);
	}
	int i;
	pid_t pid;
	for (i = 1; i < argc; ++i) {
		pid = atoi(argv[i]);
		printf("Change credential %d:", pid);
		if (changeCredential(pid) == 0) {
		  printf("success.\n");
		  resetCredential();
		}
	}
	return EXIT_SUCCESS;
}
