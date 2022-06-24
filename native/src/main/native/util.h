#include <fcntl.h>

void outputError(const char*, ...);

int changeCredential(pid_t pid);

int resetCredential();

int attachJvm(int pid, const char *jarPath, const char *agentArgs);
