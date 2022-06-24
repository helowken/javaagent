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
#include "util.h"

#define MAX_PATH 1024
#define TMP_PATH (MAX_PATH - 64)

static char tmp_path[TMP_PATH] = {0};

// Send command with arguments to socket
static int write_command(int fd, int argc, const char **argv) {
    // Protocol version
    if (write(fd, "1", 2) <= 0) {
        return 0;
    }
    int i;
    for (i = 0; i < 4; i++) {
        const char *arg = i < argc ? argv[i] : "";
        if (write(fd, arg, strlen(arg) + 1) <= 0) {
            return 0;
        }
    }
    return 1;
}

// Connect to UNIX domain socket created by JVM for Dynamic Attach
static int connect_socket(int pid) {
    int fd = socket(PF_UNIX, SOCK_STREAM, 0);
    if (fd == -1) {
        return -1;
    }

    struct sockaddr_un addr;
    addr.sun_family = AF_UNIX;
    int bytes = snprintf(addr.sun_path, sizeof(addr.sun_path), "%s/.java_pid%d", tmp_path, pid);
    if (bytes < 0) {
        perror("snprintf failed");
        return -1;
    }
    if (bytes >= sizeof(addr.sun_path)) {
        addr.sun_path[sizeof(addr.sun_path) - 1] = 0;
    }

    if (connect(fd, (struct sockaddr *) &addr, sizeof(addr)) == -1) {
        close(fd);
        return -1;
    }
    return fd;
}

// Check if remote JVM has already opened socket for Dynamic Attach
static int check_socket(int pid) {
    char path[MAX_PATH];
    snprintf(path, sizeof(path), "%s/.java_pid%d", tmp_path, pid);
    struct stat stats;
    return stat(path, &stats) == 0 && S_ISSOCK(stats.st_mode);
}

// Check if a file is owned by current user
static int check_file_owner(const char *path) {
    struct stat stats;
    if (stat(path, &stats) == 0 && stats.st_uid == geteuid()) {
        return 1;
    }
    // Some mounted filesystems may change the ownership of the file.
    // JVM will not trust such file, so it's better to remove it and try a different path
    unlink(path);
    return 0;
}

// Force remote JVM to start Attach listener.
// HotSpot will start Attach listener in response to SIGQUIT if it sees .attach_pid file
static int start_attach_mechanism(int pid, int nspid) {
    char path[MAX_PATH];
    snprintf(path, sizeof(path), "/proc/%d/cwd/.attach_pid%d", nspid, nspid);
    int fd = creat(path, 0660);
    if (fd == -1 || (close(fd) == 0 && !check_file_owner(path))) {
        // Failed to create attach trigger in current directory. Retry in /tmp
        snprintf(path, sizeof(path), "%s/.attach_pid%d", tmp_path, nspid);
        fd = creat(path, 0660);
        if (fd == -1) {
            return 0;
        }
        close(fd);
    }
    // We have to still use the host namespace pid here for the kill() call
    kill(pid, SIGQUIT);
    // Start with 20 ms sleep and increment delay each iteration
    struct timespec ts = {0, 20000000};
    int result;
    do {
        nanosleep(&ts, NULL);
        result = check_socket(nspid);
    } while (!result && (ts.tv_nsec += 20000000) < 300000000);

    unlink(path);
    return result;
}

// Mirror response from remote JVM to stdout
static int read_response(int fd) {
    char buf[8192];
    ssize_t bytes = read(fd, buf, sizeof(buf) - 1);
    if (bytes <= 0) {
        perror("Error reading response");
        return 1;
    }
    // First line of response is the command result code
    buf[bytes] = 0;
    int result = atoi(buf);
    do {
        fwrite(buf, 1, bytes, stdout);
        bytes = read(fd, buf, sizeof(buf));
    } while (bytes > 0);
    return result;
}

int attachJvm(int pid, const char *jarPath, const char *agentArgs) {
    const char *argv[] = {"load", jarPath, "true", agentArgs};

    signal(SIGPIPE, SIG_IGN);

    if (!check_socket(pid) && !start_attach_mechanism(pid, pid)) {
        perror("Could not start attach mechanism");
        return 1;
    }

    int fd = connect_socket(pid);
        if (fd == -1) {
            perror("Could not connect to socket");
            return 1;
        }

        printf("Connected to remote JVM\n");
        if (!write_command(fd, 4, argv)) {
            perror("Error writing to socket");
            close(fd);
            return 1;
        }

        printf("Response code = ");
        fflush(stdout);

        int result = read_response(fd);
        printf("\n");
        close(fd);
        printf("Finish!\n");

        return result;
}
