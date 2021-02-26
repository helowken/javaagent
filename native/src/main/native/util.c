#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <stdarg.h>
#include "header/util.h"
#include "header/ename.c.inc"

#define MAX_PATH_SIZE 100

static char *procStatusTmp = "/proc/%d/status";


static char* getProcStatusPath(pid_t pid) {
	char *path = malloc(MAX_PATH_SIZE);
	snprintf(path, MAX_PATH_SIZE, procStatusTmp, pid);
	return path;
}

static int getProcEuidAndEgid(pid_t pid, uid_t* euid, gid_t* egid) {
	char *statusPath = getProcStatusPath(pid);
	FILE *statusFile = fopen(statusPath, "r");
	if (statusFile == NULL) {
		outputError("Failed to open status file.");
		free(statusPath);
		return 0;
	}
	char *line = NULL;
	size_t lineSize;
	int euidFound = 0, egidFound = 0;
	while (getline(&line, &lineSize, statusFile) != -1) {
		if (!euidFound && strncmp(line, "Uid:", 4) == 0) {
			euidFound = 1;
			*euid = atoi(strchr(line + 5, '\t'));
		} else if (!egidFound && strncmp(line, "Gid:", 4) == 0) {
			egidFound = 1;
			*egid = atoi(strchr(line + 5, '\t'));
		}
		if (euidFound && egidFound)
		  break;
	}
	free(statusPath);
	free(line);
	if (!euidFound || !egidFound)
	  return 0;
	fclose(statusFile);
	return 1;
}

void outputError(const char *format, ...) {
	int savedErrno = errno;
#define BUF_SIZE 500
	char userMsg[BUF_SIZE], errText[BUF_SIZE], buf[BUF_SIZE];

	va_list argList;
	va_start(argList, format);
	vsnprintf(userMsg, BUF_SIZE, format, argList);
	va_end(argList);

	snprintf(errText, BUF_SIZE, "[%s, %s]", 
				(errno > 0 && errno <= MAX_ENAME)? ename[errno] : "?UNKNOWN",
				strerror(errno));

	snprintf(buf, BUF_SIZE, "ERROR%s %s\n", errText, userMsg);

	fflush(stdout);
	fputs(buf, stderr);
	fflush(stderr);

	errno = savedErrno;
}

static int tryToSetEuidAndEgid(uid_t euid, gid_t egid) {
	if (getegid() != egid && setegid(egid) == -1) {
		outputError("Set egid failed.\n");
		return -1;
	}
	if (geteuid() != euid && seteuid(euid) == -1) {
		outputError("Set euid failed.\n");
		return -1;
	}
	return 0;
}

int changeCredential(pid_t pid) {
	if (pid <= 0) {
		outputError("Invalid pid: %d\n", pid);
		return -1;
	}
	uid_t euid;
	gid_t egid;
	if (getProcEuidAndEgid(pid, &euid, &egid)) {
		if (tryToSetEuidAndEgid(euid, egid) == 0)
		  return 0;
	} else
	  outputError("No euid and egid found from pid: %d\n", pid);
	return -1;
}

int resetCredential() {
	return tryToSetEuidAndEgid(getuid(), getgid()); 
}
