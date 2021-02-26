#include <fcntl.h>

void outputError(const char*, ...);

int changeCredential(pid_t pid);

int resetCredential();
