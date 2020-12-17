function onBefore(data, inv) {
    test("onBefore", data, inv);
    $d.aInt("beforeCount").incrementAndGet();
}

function onReturn(data, inv) {
    test("onReturn", data, inv);
    $d.aInt("returnCount").incrementAndGet();
}

function onAfter(data, inv) {
    test("onAfter", data, inv);
    $d.aInt("afterCount").incrementAndGet();
}

function onError(data, inv) {
    test("onError", data, inv);
    $d.aInt("errorCount").incrementAndGet();
}

function onCatch(data, inv, error) {
    data.kvs.put("error", error);
    test("onCatch", data, inv);
    $d.aInt("catchCount").incrementAndGet();
}

function onComplete(data, inv) {
    test("onComplete", data, inv);
}


function test(step, data, inv) {
    print(inv.getName() + ", step: " + step + ", data: " + data);
}