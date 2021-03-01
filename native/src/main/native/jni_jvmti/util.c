#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#define MAX_PATH_SIZE 100

static char *procStatusTmp = "/proc/%d/status";

static char* getProcStatusPath(pid_t pid) {
	char *path = malloc(MAX_PATH_SIZE);
	snprintf(path, MAX_PATH_SIZE, procStatusTmp, pid);
	return path;
}

static int getProcEuidAndEgid(pid_t pid, uid_t* uid, gid_t* gid) {
	char *statusPath = getProcStatusPath(pid);
	FILE *statusFile = fopen(statusPath, "r");
	if (statusFile == NULL) {
		perror("Failed to open status file.");
		return 0;
	}
	char *line = NULL;
	size_t lineSize;
	int uidFound = 0, gidFound = 0;
	while (getline(&line, &lineSize, statusFile) != -1) {
		if (!uidFound && strncmp(line, "Uid:", 4) == 0) {
			uidFound = 1;
			*uid = atoi(strchr(line + 5, '\t'));
		} else if (!gidFound && strncmp(line, "Gid:", 4) == 0) {
			gidFound = 1;
			*gid = atoi(strchr(line + 5, '\t'));
		}
		if (uidFound && gidFound)
		  break;
	}
	if (!uidFound || !gidFound)
	  return 0;
	free(line);
	fclose(statusFile);
	return 1;
}

int tryToSetEuidAndEgid(pid_t pid) {
	if (pid <= 0) {
		fprintf(stderr, "Invalid pid: %d\n", pid);
		return -1;
	}
	uid_t uid;
	gid_t gid;
	printf("pid: %d\n", pid);
	if (getProcEuidAndEgid(pid, &uid, &gid)) {
		printf("uid: %d, gid: %d\n", uid, gid);
		if (seteuid(uid) == -1) {
			fprintf(stderr, "Set euid failed for pid: %d\n", pid);
			return -1;
		}
		if (setegid(gid) == -1) {
			fprintf(stderr, "Set egid failed for pid: %d\n", pid);
			return -1;
		}
		return 0;
	} else
	  fprintf(stderr, "No uid and gid found for pid: %d\n", pid);
	return -1;
}

