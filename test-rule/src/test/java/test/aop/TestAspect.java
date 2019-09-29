package test.aop;

import org.aspectj.lang.annotation.*;

@Aspect
public class TestAspect {

    @Pointcut("execution(* test.*.AAA.testAAA(..))")
    public void testPointcut() {
    }

    @Before("testPointcut()")
    public void testBefore() {
        System.out.println("In before.");
    }

    @Before("testPointcut()")
    public void testBefore2() {
        System.out.println("In before2.");
    }

    @After("testPointcut()")
    public void testAfter() {
        System.out.println("In after.");
    }

    @After("testPointcut()")
    public void testAfter2() {
        System.out.println("In after2.");
    }

    @AfterReturning("testPointcut()")
    public void testAfterReturn() {
        System.out.println("In after returning.");
    }

//    @Around("testPointcut()")
//    public void testAround() {
//        System.out.println("In around.");
//    }
}
