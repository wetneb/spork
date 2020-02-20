package se.kth.spork.merge.spoon;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.*;
import spoon.support.compiler.VirtualFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A class for dealing with parsing.
 *
 * @author Simon Larsén
 */
public class Parser {
    public static final String IMPORT_STATEMENTS = "spork_import_statements";

    /**
     * Parse a Java file to a Spoon tree. Any import statements in the file are attached to the returned module's
     * metadata with the {@link Parser#IMPORT_STATEMENTS} key. The imports are sorted in ascending lexicographical
     * order.
     *
     * TODO preserve order of import statements
     *
     * @param javaFile Path to a Java file.
     * @return The root module of the Spoon tree.
     */
    public static CtModule parse(Path javaFile) {
        Launcher launcher = new Launcher();
        // Reading from a virtual file is a workaround to a bug in Spoon
        // Sometimes, the class comment is dropped when reading from the file system
        launcher.addInputResource(new VirtualFile(read(javaFile)));
        launcher.buildModel();

        CtModel model = launcher.getModel();
        CtModule module = model.getUnnamedModule();

        List<CtImport> imports = new ArrayList<>(parseImportStatements(model));
        imports.sort(Comparator.comparing(CtElement::prettyprint));
        module.putMetadata(IMPORT_STATEMENTS, imports);

        return module;
    }


    /**
     * Parse unique import statements from all types of the given model.
     *
     * Obviously, as a set is returned, the order of the statements is not preserved.
     *
     * @param model A model.
     * @return A list of import statements.
     */
    public static Set<CtImport> parseImportStatements(CtModel model) {
        Set<CtImport> importStatements = new HashSet<>();

        for (CtType<?> type : model.getAllTypes()) {
            importStatements.addAll(
                    parseImportStatements(type)
            );
        }

        return importStatements;
    }

    /**
     * Parse import statements from the given type. Note that all types in a single file will get the same
     * import statements attached to them, so there is no need to parse imports from multiple types in a single
     * file.
     *
     * The order of the import statements is preserved.
     *
     * @param type A Java type.
     * @return A list of import statements.
     */
    public static List<CtImport> parseImportStatements(CtType<?> type) {
        CtCompilationUnit cu = type.getFactory().CompilationUnit().getOrCreate(type);
        return cu.getImports();
    }

    /**
     * Read the contents of a file.
     *
     * @param path Path to a file.
     * @return The contents of the file.
     */
    public static String read(Path path) {
        try {
            return String.join("\n", Files.readAllLines(path));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading from " + path);
        }
    }
}
