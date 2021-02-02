package test;

import agent.base.utils.Utils;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.tools.Tool;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.VmIdentifier;

public class DebugTest {
    public static void main(String[] args) {
        TestTool tool = new TestTool();
        tool.test(args);
    }

    private static class TestTool extends Tool {

        @Override
        public void run() {
//            printClassAndLoader();
            findPrivateKey();
        }

        public void test(String[] args){
            execute(args);
        }
    }

    private static void findPrivateKey() {
        Klass keyClass = VM.getVM().getSystemDictionary().find("java/security/PrivateKey", null, null);
        VM.getVM().getObjectHeap().iterateObjectsOfKlass(
                new DefaultHeapVisitor() {
                    @Override
                    public boolean doObj(Oop obj) {
                        InstanceKlass c = (InstanceKlass) obj.getKlass();
                        OopField f = (OopField) c.findField("key", "[B");
                        TypeArray key = (TypeArray) f.getValue(obj);
                        key.printOn(System.out);
                        return false;
                    }
                },
                keyClass
        );
    }

    private static void printClassAndLoader() {
        VM.getVM().getSystemDictionary().classesDo(
                (klass, loader) -> {
                    String className = klass.getName().asString();
                    System.out.print(className);

                    String loaderName = (loader == null) ? "Bootstrap ClassLoader" : loader.getKlass().getName().asString();
                    System.out.println(" loaded by " + loaderName);
                }
        );
    }

    private static void printVmMonitors() throws Exception {
        MonitoredHost host = MonitoredHost.getMonitoredHost((String) null);
        host.activeVms().forEach(
                pid -> Utils.wrapToRtError(
                        () -> {
                            if (pid == 20720) {
                                MonitoredVm vm = host.getMonitoredVm(
                                        new VmIdentifier(pid.toString())
                                );
                                vm.findByPattern(".*").forEach(
                                        monitor -> System.out.println(
                                                monitor.getName() + " = " + monitor.getValue()
                                        )
                                );
                                System.out.println("=========================");
                            }
                        }
                )
        );
    }
}
