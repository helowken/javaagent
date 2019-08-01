#include "jvmti.h"
#include "agent_jvmti_JvmtiUtils.h"


struct RefCount {
	int maxCount;
	int count;
};


static jint JNICALL
TagObjectByClass(jlong class_tag, jlong size, jlong* tag_ptr, jint length, void* user_data) {
	*tag_ptr = 1;
	struct RefCount* refCount = (struct RefCount*) user_data;
	refCount->count += 1;
	if (refCount->count >= refCount->maxCount) 
		return JVMTI_VISIT_ABORT;
	return JVMTI_VISIT_OBJECTS;
}


JNIEXPORT jobject JNICALL
Java_agent_jvmti_JvmtiUtils_findObjectsByClassHelper(JNIEnv* env, jobject thisObj, jclass targetClass, jint maxCount) {
	JavaVM* vm;
	(*env)->GetJavaVM(env, &vm);

	jvmtiEnv* jvmti;
	(*vm)->GetEnv(vm, (void**) &jvmti, JVMTI_VERSION_1_0);

	jvmtiCapabilities capabilities = {0};
	capabilities.can_tag_objects = 1;
	(*jvmti)->AddCapabilities(jvmti, &capabilities);

	jvmtiHeapCallbacks callbacks = {};
	callbacks.heap_iteration_callback = TagObjectByClass;
	struct RefCount refCount = {maxCount, 0};
	(*jvmti)->IterateThroughHeap(jvmti, JVMTI_HEAP_FILTER_TAGGED, targetClass, &callbacks, &refCount);

	jlong tag = 1;
	jint count;
	jobject* instances;
	(*jvmti)->GetObjectsWithTags(jvmti, 1, &tag, &count, &instances, NULL); 

	if (instances != NULL) {
		jclass arrayListClass = (*env)->FindClass(env, "java/util/ArrayList");
		if (arrayListClass == NULL)
			return NULL;

		jmethodID arrayListConstructor = (*env)->GetMethodID(env, 
				arrayListClass,
				"<init>",
				"()V"
		);
		if (arrayListConstructor == NULL)
			return NULL;

		jobject rsList = (*env)->NewObject(env, arrayListClass, arrayListConstructor);
		if (rsList == NULL)
			return NULL;

		jmethodID arrayListAdd = (*env)->GetMethodID(env,
				arrayListClass,
				"add",
				"(Ljava/lang/Object;)Z"
		);
		if (arrayListAdd == NULL)
			return NULL;

		int i;
		int len = count;
		if (count > maxCount)
			len = maxCount;
		for (i = 0; i < len; ++i) {
			(*env)->CallBooleanMethod(env, rsList, arrayListAdd, *(instances + i));
		}
		(*jvmti)->Deallocate(jvmti, (unsigned char*) instances);
		return rsList;
	}
	return NULL;
}


