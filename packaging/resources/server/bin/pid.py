import os, sys, traceback

def to_none_list(s, spliter):
    ts = s.split(spliter)
    return list(filter(None, ts))

def exec(cmd):
    print("Execute command:\n  %s" % cmd)
    stream = os.popen(cmd)
    output = stream.read()
    print("Result:")
    rs = to_none_list(output, "\n")
    if len(rs) > 0:
        for _ in map(lambda x: print("  %s" % x), rs):
            pass
    else:
        print()
    return rs

def get_single(rs):
    if len(rs) == 0:
        raise Exception("No records.")
    if len(rs) > 1:
        raise Exception("More than 1 records.")
    return rs[0]

def exec_single(cmd):
    return get_single(exec(cmd))

def docker_find_by_filter(criteria):
    r = exec_single("docker ps -f %s -q" % criteria)
    return exec_single("docker inspect --format '{{.State.Pid}}' %s" % r)

def docker_find_cr_by_name(name):
    return docker_find_by_filter("name=%s" % name)

def docker_find_cr_by_id(cid):
    return docker_find_by_filter("id=%s" % cid)

def raise_invalid_arg_error(s):
    raise Exception("Invalid argument: %s" % s)

docker_funcs = {
    "name": docker_find_cr_by_name,
    "id": docker_find_cr_by_id
}

def docker_search(s):
    kv = to_none_list(s, "=")
    if len(kv) != 2:
        raise_invalid_arg_error(s)
    func = docker_funcs.get(kv[0], lambda x: raise_invalid_arg_error(s))
    return func(kv[1])


def k3s_find_img(img):
    r = exec_single("crictl images | grep -i %s" % img)
    ts = to_none_list(r, " ")
    if len(ts) > 3:
        return ts[2]
    raise Exception("No image id found:\n%s" % r)

def k3s_find_cr_by_img_id(img_id):
    r = exec_single("crictl ps --image=%s -q" % img_id)
    print(r)


type_map = {
    "docker": docker_search
}

def get_func_by_cmd(cmd):
    return type_map.get(
            cmd, 
            lambda x: sys.exit("Invalid command: %s" % cmd)
        )


if len(sys.argv) < 3:
    sys.exit(
"""USAGE: 
    %s type arguemnts

TYPES:
    docker
    k3s

ARGUMENTS:
    For docker type:
        name=CONTAINER_NAME    Use container name to search.
        id=CONTAINER_ID        Use container id to search.
    For k3s type:
        img=IMAGE              Search by: crictl ps | grep -i IMAGE
        con=xxx
""" % sys.argv[0])

func = get_func_by_cmd(sys.argv[1])
try:
    func(sys.argv[2])
except Exception as err:
    print("Error:\n  %s" % "".join(err.args))
    traceback.print_exc()

exit(0)


img_id = find_img(sys.argv[1])
img_id = sys.argv[1]
print("result: %s" % img_id)

if img_id is not None:
    r = find_container_by_img_id(img_id)




