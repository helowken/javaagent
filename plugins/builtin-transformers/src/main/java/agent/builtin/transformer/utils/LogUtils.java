package agent.builtin.transformer.utils;

import agent.base.utils.Logger;
import agent.server.utils.ParamValueUtils;
import agent.server.utils.ParamValueUtils.Expr;
import agent.server.utils.log.LogMgr;
import javassist.CtClass;
import javassist.CtMethod;

public class LogUtils {
    private static final Logger logger = Logger.getLogger(LogUtils.class);

    public static String newCurrTimeMillisStatement(String var) {
        return var + " = System.currentTimeMillis();";
    }

    public static Expr newCostTimegExpr(String stVar, String etVar) {
        return new Expr("(" + etVar + " - " + stVar + ")");
    }

    public static Expr newCostTimeStringExpr(String stVar, String etVar) {
        return new Expr("String.valueOf(" + etVar + " - " + stVar + ")");
    }

    public static void addCostTimeCode(CtMethod ctMethod, CostTimeCodeFunc func) throws Exception {
        final String stVar = "startTime";
        final String etVar = "endTime";
        ctMethod.addLocalVariable(stVar, CtClass.longType);
        ctMethod.insertBefore(
                newCurrTimeMillisStatement(stVar)
        );
        ctMethod.addLocalVariable(etVar, CtClass.longType);
        StringBuilder endBlock = new StringBuilder(
                newCurrTimeMillisStatement(etVar)
        ).append("\n");
        func.exec(stVar, etVar, endBlock);
        String content = endBlock.toString();
        logger.debug("Cost time code end block: {}", content);
        ctMethod.insertAfter(content);
    }

    public static void addLogTextCode(StringBuilder block, String logKey, String pvsCode) {
        block.append(LogMgr.class.getName())
                .append(".logText(")
                .append(ParamValueUtils.convertToString(logKey))
                .append(", ")
                .append(pvsCode)
                .append(");");
    }

    public interface CostTimeCodeFunc {
        void exec(String stVar, String etVar, StringBuilder endBlock);
    }
}
