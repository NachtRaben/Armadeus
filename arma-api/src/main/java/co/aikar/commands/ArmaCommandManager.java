package co.aikar.commands;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Stack;

public abstract class ArmaCommandManager<IT, I extends CommandIssuer, FT, MF extends MessageFormatter<FT>, CEC extends CommandExecutionContext<CEC, I>, CC extends ConditionContext<I>>
        extends CommandManager<IT, I, FT, MF, CEC, CC> {

    public abstract List<String> getAnnotationValues(AnnotatedElement object, Class<? extends Annotation> annoClass, int options);

}
