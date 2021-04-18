package dev.armadeus.core.util.eval;

public enum Engines {

    GROOVY() {
        @Override
        public EvalResult<Object, String, String> eval() {
            return new EvalResult<>(null, null, null);
        }
    };

    public abstract EvalResult<Object, String, String> eval();

}
