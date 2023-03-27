package com.hhz.ifttt.serialization;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.BaseExpression;
import com.googlecode.aviator.Expression;
import org.apache.flink.annotation.Internal;
import org.apache.flink.api.common.typeutils.SimpleTypeSerializerSnapshot;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.common.typeutils.TypeSerializerSchemaCompatibility;
import org.apache.flink.api.common.typeutils.TypeSerializerSnapshot;
import org.apache.flink.api.common.typeutils.base.LocalDateSerializer;
import org.apache.flink.api.common.typeutils.base.TypeSerializerSingleton;
import org.apache.flink.api.java.typeutils.runtime.DataInputViewStream;
import org.apache.flink.api.java.typeutils.runtime.DataOutputViewStream;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.util.InstantiationUtil;

import javax.annotation.Nonnull;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * @program ifttt
 * @description: 自定义序列化类 表达式
 * @author: zhangyinghao
 * @create: 2021/11/23 11:32
 **/
public class ExpressionSerialization extends TypeSerializerSingleton<Expression> {
    public static final  ExpressionSerialization INSTANCE = new ExpressionSerialization();


    @Override
    public boolean isImmutableType() {
        return false;
    }

    @Override
    public Expression createInstance() {
        return null;
    }

    @Override
    public Expression copy(Expression expression) {
        return expression;
    }

    @Override
    public Expression copy(Expression expression, Expression t1) {
        return expression;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public void serialize(Expression expression, DataOutputView dataOutputView) throws IOException {
        DataOutputViewStream outViewWrapper = new DataOutputViewStream(dataOutputView);
        Throwable var4 = null;
        try {
            InstantiationUtil.serializeObject(outViewWrapper, expression);
        } catch (Throwable var13) {
            var4 = var13;
            throw var13;
        } finally {
            if (outViewWrapper != null) {
                if (var4 != null) {
                    try {
                        outViewWrapper.close();
                    } catch (Throwable var12) {
                        var4.addSuppressed(var12);
                    }
                } else {
                    outViewWrapper.close();
                }
            }
        }
    }

    @Override
    public Expression deserialize(DataInputView dataInputView) throws IOException {
        try {
            DataInputViewStream inViewWrapper = new DataInputViewStream(dataInputView);
            Throwable var3 = null;
            Expression var4;
            try {
                var4 = (Expression) InstantiationUtil.deserializeObject(inViewWrapper, Thread.currentThread().getContextClassLoader());
            } catch (Throwable var14) {
                var3 = var14;
                throw var14;
            } finally {
                if (inViewWrapper != null) {
                    if (var3 != null) {
                        try {
                            inViewWrapper.close();
                        } catch (Throwable var13) {
                            var3.addSuppressed(var13);
                        }
                    } else {
                        inViewWrapper.close();
                    }
                }

            }

            return var4;
        } catch (ClassNotFoundException var16) {
            throw new IOException("Could not deserialize object.", var16);
        }
    }

    @Override
    public Expression deserialize(Expression expression, DataInputView dataInputView) throws IOException {
        return this.deserialize(dataInputView);
    }

    @Override
    public void copy(DataInputView dataInputView, DataOutputView dataOutputView) throws IOException {
        Expression tmp = this.deserialize(dataInputView);
        this.serialize(tmp, dataOutputView);
    }

    @Override
    public TypeSerializerSnapshot<Expression> snapshotConfiguration(){
        return new BaseExpressionSerializerSnapshot();
    }

    public static final class BaseExpressionSerializerSnapshot extends SimpleTypeSerializerSnapshot<Expression> {

        public BaseExpressionSerializerSnapshot() {
            super(() -> {
                return ExpressionSerialization.INSTANCE;
            });
        }
        public BaseExpressionSerializerSnapshot(@Nonnull Supplier<? extends TypeSerializer<Expression>> serializerSupplier) {
            super(serializerSupplier);
        }
    }
}

