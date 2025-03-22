package nufo.diss.generators;

public abstract class Generator {
    protected Mode mode;

    Generator(Mode mode) {
        this.mode = mode;
    }

    public abstract int nextInt();
    public abstract double nextDouble();

    public enum Mode {
        DISCRETE,
        CONTINUOUS
    }
}
