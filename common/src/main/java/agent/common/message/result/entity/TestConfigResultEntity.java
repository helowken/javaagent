package agent.common.message.result.entity;

import java.util.ArrayList;
import java.util.List;

public class TestConfigResultEntity {
    private String context;
    private List<ClassResultEntity> classEntityList = new ArrayList<>();

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public List<ClassResultEntity> getClassEntityList() {
        return classEntityList;
    }

    public void setClassEntityList(List<ClassResultEntity> classEntityList) {
        this.classEntityList = classEntityList;
    }

    public void addClassEntity(ClassResultEntity classResultEntity) {
        this.classEntityList.add(classResultEntity);
    }


    public static class ClassResultEntity {
        private String className;
        private List<MethodResultEntity> methodEntityList = new ArrayList<>();

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public List<MethodResultEntity> getMethodEntityList() {
            return methodEntityList;
        }

        public void setMethodEntityList(List<MethodResultEntity> methodEntityList) {
            this.methodEntityList = methodEntityList;
        }

        public void addMethodEntity(MethodResultEntity methodEntity) {
            this.methodEntityList.add(methodEntity);
        }
    }


    public static class MethodResultEntity {
        private String declareClass;
        private String methodName;
        private String signature;

        public String getDeclareClass() {
            return declareClass;
        }

        public void setDeclareClass(String declareClass) {
            this.declareClass = declareClass;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

    }
}
