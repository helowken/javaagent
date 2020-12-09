$d.newAInt("beforeCount");
$d.newAInt("returnCount");
$d.newAInt("afterCount");
$d.newAInt("errorCount");
$d.newAInt("catchCount");

function onBefore(data, inv) {
    test("onBefore", data, inv);
    $d.get("beforeCount").incrementAndGet();
}

function onReturn(data, inv) {
    test("onReturn", data, inv);
    $d.get("returnCount").incrementAndGet();
}

function onAfter(data, inv) {
    test("onAfter", data, inv);
    $d.get("afterCount").incrementAndGet();
}

function onError(data, inv) {
    test("onError", data, inv);
    $d.get("errorCount").incrementAndGet();
}

function onCatch(data, inv, error) {
    data.kvs.put("error", error);
    test("onCatch", data, inv);
    $d.get("catchCount").incrementAndGet();
}

function onComplete(data, inv) {
    test("onComplete", data, inv);
}


function test(step, data, inv) {
    print(inv.getName() + ", step: " + step + ", data: " + data);
}