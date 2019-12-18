package agent.server.transform.impl;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.tools.asm.ProxyCallInfo;
import agent.server.transform.tools.asm.ProxyRegInfo;
import agent.server.transform.tools.asm.annotation.OnAfter;
import agent.server.transform.tools.asm.annotation.OnBefore;
import agent.server.transform.tools.asm.annotation.OnReturning;
import agent.server.transform.tools.asm.annotation.OnThrowing;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class AbstractAnnotationConfigTransformer extends AbstractConfigTransformer {

    @Override
    protected void transformMethod(Method method) throws Exception {
        ProxyRegInfo regInfo = new ProxyRegInfo(method);
        Class<?> anntClass;
        for (Method selfMethod : getClass().getDeclaredMethods()) {
            for (Annotation annt : selfMethod.getAnnotations()) {
                anntClass = annt.annotationType();
                if (OnBefore.class.equals(anntClass))
                    regInfo.addBefore(
                            newCallInfo(selfMethod, (OnBefore) annt)
                    );
                else if (OnAfter.class.equals(anntClass))
                    regInfo.addAfter(
                            newCallInfo(selfMethod, (OnAfter) annt)
                    );
                else if (OnReturning.class.equals(anntClass))
                    regInfo.addOnReturning(
                            newCallInfo(selfMethod, (OnReturning) annt)
                    );
                else if (OnThrowing.class.equals(anntClass))
                    regInfo.addOnThrowing(
                            newCallInfo(selfMethod, (OnThrowing) annt)
                    );
            }
        }
        if (!regInfo.isEmpty())
            addRegInfo(regInfo);
    }

    private ProxyCallInfo newCallInfo(Method selfMethod, OnBefore annt) {
        return newCallInfo(
                selfMethod,
                annt.mask(),
                annt.otherArgsFunc()
        );
    }

    private ProxyCallInfo newCallInfo(Method selfMethod, OnAfter annt) {
        return newCallInfo(
                selfMethod,
                annt.mask(),
                annt.otherArgsFunc()
        );
    }

    private ProxyCallInfo newCallInfo(Method selfMethod, OnReturning annt) {
        return newCallInfo(
                selfMethod,
                annt.mask(),
                annt.otherArgsFunc()
        );
    }

    private ProxyCallInfo newCallInfo(Method selfMethod, OnThrowing annt) {
        return newCallInfo(
                selfMethod,
                annt.mask(),
                annt.otherArgsFunc()
        );
    }

    private ProxyCallInfo newCallInfo(Method selfMethod, int mask, String otherArgsFuncName) {
        return new ProxyCallInfo(
                getThisOrNull(selfMethod),
                selfMethod,
                mask,
                getOtherArgs(otherArgsFuncName)
        );
    }

    private Object getThisOrNull(Method method) {
        return Modifier.isStatic(
                method.getModifiers()
        ) ? null : this;
    }

    private Object[] getOtherArgs(String funcName) {
        if (Utils.isBlank(funcName))
            return null;
        Object value = Utils.wrapToRtError(
                () -> ReflectionUtils.invoke(funcName, this)
        );
        if (value == null)
            return null;
        if (value.getClass().isArray())
            return (Object[]) value;
        return new Object[]{
                value
        };
    }

}
