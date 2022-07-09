#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <sys/syscall.h>
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <signal.h>
#include <time.h>
#include <unistd.h>
#include <sys/types.h>
#include "../jni/util.h"

#define MAX_PATH 1024
#define TMP_PATH (MAX_PATH - 64)

static char tmpPath[TMP_PATH] = {0};
static int initResult = -1;

static int getTmpPath(int pid) {
/* A process may have its own root path (when running in chroot environment) */
	char path[64];
	ssize_t pathSize;

	snprintf(path, sizeof(path), "/proc/%d/root", pid);

	/* Append /tmp to the resolved root symlink */
	pathSize = readlink(path, tmpPath, sizeof(tmpPath) - 10);
	strcpy(tmpPath + (pathSize > 1 ? pathSize : 0), "/tmp");

	return 1;
}

static int enterMountNS(int pid) {
#ifdef __NR_setns
	char path[128];
	struct stat oldNsStat, newNsStat;
	int newNs;
	int r;

	snprintf(path, sizeof(path), "/proc/%d/ns/mnt", pid);
	if (stat("/proc/self/ns/mnt", &oldNsStat) == 0 && 
				stat(path, &newNsStat) == 0) {
		/* Don't try to call setns() if we're in the same namespace already */
		printf("Check NS, oldNS ino: %lu, newNS ino: %lu, tid: %ld\n", oldNsStat.st_ino, newNsStat.st_ino, syscall(__NR_gettid));
		if (oldNsStat.st_ino != newNsStat.st_ino) {
			newNs = open(path, O_RDONLY);
			if (newNs < 0) {
				outputError("Open %s failed", path);
				return 0;
			}

			/* Some ancient Linux distributions do not have setns() function */
			r = syscall(__NR_setns, newNs, 0);
			close(newNs);
			return r < 0 ? 0 : 1;
		}
	}
#endif
	return 1;
}

static int checkSocket(int pid) {
/* Check if remote JVM has already opened socket for Dynamic Attach. */
	char path[MAX_PATH];
	struct stat stats;

	snprintf(path, sizeof(path), "%s/.java_pid%d", tmpPath, pid);
	printf("Checking socket file: %s\n", path);
	return stat(path, &stats) == 0 && S_ISSOCK(stats.st_mode);
}

static int checkFileOwner(char *path) {
	struct stat stats;

	if (stat(path, &stats) == 0 && stats.st_uid == geteuid()) {
		printf("Check owner of trigger file: success\n");
		return 1;
	}

	printf("Check owner of trigger file: failed\n");

	/* Some mounted filesystems may change the ownership of the file.
	 * JVM will not trust such file, so it's better to remove it and try a different path
	 */
	unlink(path);
	return 0;
}

static int startAttach(int pid, int nsPid, char *path) {
/* Force remote JVM to start Attach listener.
 * HotSpot will start Attach listener in response to SIGQUIT if it sees .attach_pid file. 
 */
	int fd;
	int r;

	printf("Try creating trigger file: %s\n", path);
	if ((fd = creat(path, 0660)) == -1) {
		outputError("Error creating trigger file: %s\n", path);
		return 0;
	} else if (close(fd) == 0 && !checkFileOwner(path)) {
	  return 0;
	}

	/* We have to still use the host namespace pid here for the kill() call */
	printf("Notify target JVM\n");
	kill(pid, SIGQUIT);

	struct timespec ts = {0, 20000000};
	do {
		nanosleep(&ts, NULL);
		r = checkSocket(nsPid);
	} while (!r && (ts.tv_nsec += 20000000) < 300000000);

	unlink(path);
	return r;
}

static int startAttachMechanism(int pid, int nsPid) {
	char path[MAX_PATH];

	snprintf(path, sizeof(path), "/proc/%d/cwd/.attach_pid%d", nsPid, nsPid);

	if (!startAttach(pid, nsPid, path)) {
		/* Failed to create attach trigger in current directory. Retry in /tmp */
		snprintf(path, sizeof(path), "%s/.attach_pid%d", tmpPath, nsPid);
		return startAttach(pid, nsPid, path);
	}
	return 1;
}

static int connectSocket(int pid) {
/* Connect to UNIX domain socket created by JVM for Dynamic Attach. */
	int fd;
	struct sockaddr_un addr;
	int bytes;

	fd = socket(PF_UNIX, SOCK_STREAM, 0);
	if (fd == -1)
	  return -1;

	addr.sun_family = AF_UNIX;
	bytes = snprintf(addr.sun_path, sizeof(addr.sun_path), "%s/.java_pid%d", tmpPath, pid);
	if (bytes <= 0) {
		outputError("Filling socket address failed.");
		return -1;	
	}
	if ((unsigned) bytes >= sizeof(addr.sun_path)) 
	  addr.sun_path[sizeof(addr.sun_path) - 1] = 0;
	
	if (connect(fd, (struct sockaddr *) &addr, sizeof(addr)) == -1) {
		close(fd);
		return -1;
	}
	return fd;
}

static int writeCommand(int fd, int argc, const char **argv) {
/* Send command wi arguments to socket */
	int i;

	if (write(fd, "1", 2) <= 0) {
		outputError("Error writing protocol");
		return 0;
	}
	
	for (i = 0; i < 4; ++i) {
		const char *arg = i < argc ? argv[i] : "";
		if (write(fd, arg, strlen(arg) + 1) <= 0) {
			outputError("Error writing argument: %d=%s", i, arg);
			return 0;
		}
	}
	return 1;
}

static int readResponse(int fd) {
/* Mirror response from remote JVM to stdout */
	char buf[8192];
	ssize_t bytes;
	int r;

	bytes = read(fd, buf, sizeof(buf) - 1);
	if (bytes <= 0) {
		outputError("Error reading response");
		return 1;
	}

	buf[bytes] = 0;
	r = atoi(buf);
	
	do {
		fwrite(buf, 1, bytes, stdout);
		bytes = read(fd, buf, sizeof(buf));
	} while (bytes > 0);

	return r;
}

static int doPrepareEnv(int pid, int *nsPidPtr) {
	uid_t euid;
	gid_t egid;
	int nsPid = -1;

	/* Get target uid, gid, and nspid */
	if (!getProcInfo(pid, &euid, &egid, &nsPid)) {
		outputError("No euid and egid found from pid: %d\n", pid);
		return 0;
	}
	if (nsPid < 0) {
		outputError("No nsPid found from pid: %d\n", pid);
		return 0;
	}
	printf("Target process: %d, uid: %d, gid: %d, nstgid: %d\n", pid, euid, egid, nsPid);
	printf("Current process %d, uid: %d, gid: %d\n", getpid(), geteuid(), getegid());

	/* Get target tmp path */
	if (!getTmpPath(pid))
	  strcpy(tmpPath, "/tmp");
	printf("Get tmp path: %s\n", tmpPath);

	/* Enter target namespace */
	if (!enterMountNS(pid))
	  outputError("Couldn't enter target process mnt namespace\n");
	else
	  printf("Enter target mnt namespace: success\n");

	/* Change to target credential */
	if (!tryToSetEuidAndEgid(euid, egid))
	  return 0;
	printf("Change to target credential: success\n");
	printf("Current process uid: %d, gid: %d\n", geteuid(), getegid());

	/* Make write() return EPIPE instead of silent process termination. */
    signal(SIGPIPE, SIG_IGN);

	if (!checkSocket(nsPid)) {
		printf("Socket: not open\n");
		if (!startAttachMechanism(pid, nsPid)) {
			outputError("Couldn't start attach mechanism.");
			return 0;
		} else {
			printf("Socket now: opened\n");
		}
	} else {
		printf("Socket: opened\n");
	}

	*nsPidPtr = nsPid;
	return 1;
}

static int prepareEnv(int pid, int *nsPidPtr) {
	if (initResult == -1) 
	  initResult = doPrepareEnv(pid, nsPidPtr);
	return initResult;
}

int attachJvm(int pid, const char *jarPath, const char *options) {
	int fd;
	char buf[1024];
	int r;
	ssize_t n;
	int nsPid;

	if (! prepareEnv(pid, &nsPid)) 
	  return 1;

	n = snprintf(buf, sizeof(buf), "%s=%s", jarPath, options);
	if (n <= 0) {
		outputError("Filling agent argument failed.");
		return 1;
	} else if ((unsigned) n >= sizeof(buf)) {
		perror("JarPath=Options is too long.");
		return 1;
	}

	printf("Try connecting socket: ");
	fd = connectSocket(nsPid);
	if (fd == -1) {
		outputError("Couldn't connect to socket");
		return 1;
	} else {
		printf("connected\n");
	}

	printf("Try writing command to socket: ");
    const char *argv[] = {"load", "instrument", "false", buf};
	if (! writeCommand(fd, 4, argv)) {
		outputError("Error writing to socket");
		close(fd);
		return 1;
	} else {
		printf("success\n");
	}

	fflush(stdout);

	printf("Read response: \n");
	r = readResponse(fd);
	close(fd);

	return r;
}

int main(int argc, char *argv[]) {
	if (argc < 4) {
		fprintf(stderr, "Usage: %s PID JAR_PATH JAR_OPTION\n", argv[0]);
		exit(EXIT_FAILURE);
	}

	int pid = atoi(argv[1]);
	return attachJvm(pid, argv[2], argv[3]);
}
