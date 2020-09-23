function onBefore(data, inv) {
    test("onBefore", data, inv);
}

function onReturn(data, inv) {
    test("onReturn", data, inv);
}

function onAfter(data, inv) {
    test("onAfter", data, inv);
}

function onError(data, inv) {
    test("onError", data, inv);
}

function onCatch(data, inv, error) {
    data.kvs.put("error", error);
    test("onCatch", data, inv);
}

function onComplete(data, inv) {
    test("onComplete", data, inv);
}


function test(step, data, inv) {
    print(inv.getName() + ", step: " + step + ", data: " + data);
}