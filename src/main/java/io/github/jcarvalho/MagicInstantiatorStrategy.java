package io.github.jcarvalho;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_5;

import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objenesis.ObjenesisException;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;

public class MagicInstantiatorStrategy implements InstantiatorStrategy {

    private final java.lang.reflect.Method DEFINE_CLASS = getDefineClassHandle();
    private final AtomicInteger uniquifier = new AtomicInteger(0);
    private final ClassLoader classLoader;

    public MagicInstantiatorStrategy(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public <T> ObjectInstantiator<T> newInstantiatorOf(Class<T> type) {
        try {
            String className = type.getName() + "$$Objenesis$$ObjectInstantiator$$" + uniquifier.getAndIncrement();

            ClassWriter cw = new ClassWriter(0);
            cw.visit(V1_5, ACC_PUBLIC | ACC_FINAL, className.replace('.', '/'), null, "sun/reflect/MagicAccessorImpl",
                    new String[] { ObjectInstantiator.class.getName().replace('.', '/') });
            cw.visitSource("ObjectInstantiator", null);

            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 1);
            }

            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "newInstance", "()Ljava/lang/Object;", null, null);
                mv.visitCode();
                mv.visitTypeInsn(NEW, Type.getInternalName(type));
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(0, 1);
            }

            byte[] bytes = cw.toByteArray();

            @SuppressWarnings("unchecked")
            Class<ObjectInstantiator<T>> clazz =
                    (Class<ObjectInstantiator<T>>) DEFINE_CLASS.invoke(classLoader, className, bytes, 0, bytes.length);
            return clazz.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new ObjenesisException(e);
        }
    }

    private static java.lang.reflect.Method getDefineClassHandle() {
        try {
            java.lang.reflect.Method method =
                    ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            throw new ObjenesisException(e);
        }
    }

}
