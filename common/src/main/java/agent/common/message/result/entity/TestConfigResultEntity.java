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
        private List<InvokeResultEntity> invokeList = new ArrayList<>();

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public List<InvokeResultEntity> getInvokeList() {
            return invokeList;
        }

        public void setInvokeList(List<InvokeResultEntity> invokeList) {
            this.invokeList = invokeList;
        }

        public void addInvokeEntity(InvokeResultEntity invokeEntity) {
            this.invokeList.add(invokeEntity);
        }
    }


    public static class InvokeResultEntity {
        private String declareClass;
        private String name;
        private String desc;

        public String getDeclareClass() {
            return declareClass;
        }

        public void setDeclareClass(String declareClass) {
            this.declareClass = declareClass;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

    }
}
