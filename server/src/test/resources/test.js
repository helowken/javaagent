function onBefore_test1(pvs) {
    print("onBefore..." + pvs["methodName"]);
    aa.test();
}

function onAfter_test1(pvs) {
    print("onAfter..."+ pvs["methodName"]);
    aa.test();
}

function onReturning_test1(pvs) {
    print("onReturning..."+ pvs["methodName"]);
    aa.test();
}

function onThrowing_test1(pvs) {
    print("onThrowing..."+ pvs["methodName"]);
    aa.test();
}

function onCatching_test1(pvs) {
    print("onCatching..."+ pvs["methodName"]);
    aa.test();
    commonFunc();
}

function commonFunc() {
    print("common ....");
}
