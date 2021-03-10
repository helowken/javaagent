package test.server;


import agent.server.transform.tools.asm.annotation.OnAfter;
import agent.server.transform.tools.asm.annotation.OnBefore;
import agent.server.transform.tools.asm.annotation.OnReturning;
import agent.server.transform.tools.asm.annotation.OnThrowingNotCatch;

public class TestProxy {
    @OnBefore
    void onBefore() {
    }

    @OnAfter
    void onAfter() {
    }

    @OnThrowingNotCatch
    void onThrowingNotCatch() {
    }

    @OnReturning
    void onReturning() {
    }
}
