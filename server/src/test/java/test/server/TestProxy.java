package test.server;


import agent.server.transform.tools.asm.annotation.OnAfter;
import agent.server.transform.tools.asm.annotation.OnBefore;
import agent.server.transform.tools.asm.annotation.OnReturning;
import agent.server.transform.tools.asm.annotation.OnThrowing;

public class TestProxy {
    @OnBefore
    void onBefore() {
    }

    @OnAfter
    void onAfter() {
    }

    @OnThrowing
    void onThrowing() {
    }

    @OnReturning
    void onReturning() {
    }
}
