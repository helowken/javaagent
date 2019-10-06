package test.aop;

import org.aspectj.lang.annotation.*;

@Aspect
public class TestAspect {

    @Pointcut("execution(* test.*.AAA.testAAA(..))")
    public void aaaPointcut() {
    }

    @Pointcut("execution(* test.*.AAA.testBBB(..))")
    public void bbbPointcut() {
    }

    @Pointcut("execution(* test.*.AAA.test*(..))")
    public void cccPointcut() {
    }

    @Before("aaaPointcut()")
    public void testBefore() {
        System.out.println("In before.");
    }

    @Before("aaaPointcut()")
    public void testBefore2() {
        System.out.println("In before2.");
    }

    @After("bbbPointcut()")
    public void testAfter() {
        System.out.println("In after.");
    }

    @After("bbbPointcut()")
    public void testAfter2() {
        System.out.println("In after2.");
    }

    @AfterReturning("cccPointcut()")
    public void testAfterReturn() {
        System.out.println("In after return.");
    }

//    @Around("aaaPointcut()")
//    public void testAround() {
//        System.out.println("In around.");
//    }
}
