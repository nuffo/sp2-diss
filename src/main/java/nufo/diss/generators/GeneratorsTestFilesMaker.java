package nufo.diss.generators;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class GeneratorsTestFilesMaker {
    public static void main(String[] args) {
        UniformGenerator uniformGeneratorDiscrete = new UniformGenerator(200, 610, Generator.Mode.DISCRETE);
        UniformGenerator uniformGeneratorContinuous = new UniformGenerator(200, 610, Generator.Mode.CONTINUOUS);


        EmpiricalGenerator empiricalGeneratorDiscrete = new EmpiricalGenerator(
                List.of(5.0, 10.0, 50.0, 70.0, 80.0),
                List.of(10.0, 50.0, 70.0, 80.0, 95.0),
                List.of(0.4, 0.3, 0.2, 0.06, 0.04),
                Generator.Mode.DISCRETE
        );
        EmpiricalGenerator empiricalGeneratorContinuous = new EmpiricalGenerator(
                List.of(5.0, 10.0, 50.0, 70.0, 80.0),
                List.of(10.0, 50.0, 70.0, 80.0, 95.0),
                List.of(0.4, 0.3, 0.2, 0.06, 0.04),
                Generator.Mode.CONTINUOUS
        );

        ExponentialGenerator exponentialGenerator = new ExponentialGenerator(2);

        TriangularGenerator triangularGenerator = new TriangularGenerator(60, 480, 120);

        writeToFile("uniform_discrete.dst", uniformGeneratorDiscrete, 100000, Type.INTEGER);
        writeToFile("uniform_continuous.dst", uniformGeneratorContinuous, 100000, Type.DOUBLE);

        writeToFile("empirical_discrete.dst", empiricalGeneratorDiscrete, 100000, Type.INTEGER);
        writeToFile("empirical_continuous.dst", empiricalGeneratorContinuous, 100000, Type.DOUBLE);

        writeToFile("exponential.dst", exponentialGenerator, 100000, Type.DOUBLE);
//
        writeToFile("triangular.dst", triangularGenerator, 100000, Type.DOUBLE);
    }

    private static void writeToFile(String filename, Generator generator, int count, Type type) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int i = 0; i < count; i++) {
                String value = (type == Type.INTEGER) ? String.valueOf(generator.nextInt()) : String.valueOf(generator.nextDouble());
                writer.write(value);
                writer.newLine();
            }
            System.out.println("File written successfully to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing to file " + filename);
            e.printStackTrace();
        }
    }

    enum Type {
        INTEGER, DOUBLE
    }
}
