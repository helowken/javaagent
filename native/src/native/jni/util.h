#include <fcntl.h>

void outputError(const char*, ...);
int getProcInfo(pid_t pid, uid_t* euid, gid_t* egid, int *nsPid);
int tryToSetEuidAndEgid(uid_t euid, gid_t egid);
int changeCredential(pid_t pid);
int resetCredential();
