package com.exemple.tp2;

import java.awt.*;
import java.awt.Dimension;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import javax.swing.*;

public class ParserAST {
    public static final String projectPath = "/Users/benjaminadolphe/Downloads/IntelligenceArtificielleTP2-main";
    public static final String projectSourcePath = projectPath + "/src";
    public static final String jrePath = "/Users/benjaminadolphe/Library/Java/JavaVirtualMachines/openjdk-16.0.1/Contents/Home/bin";

    public static int class_compter = 0;
    public static int method_compter = 0;
    public static int fields_compter = 0;
    public static int max_parameter = 0;
    public static int app_line_compter = 0;

    public static List<String> packageList = new ArrayList<>();
    public static List<String> linePerMethodList = new ArrayList<>();
    public static List<String> classesWithMostMethods = new ArrayList<>();
    public static List<String> classesWithMostFields = new ArrayList<>();
    public static List<String> moreThanXList = new ArrayList<>();

    public static List<Map<String, Integer>> methodsWithNumberOfLinesByClass = new ArrayList<>();

    public static HashMap<String, Integer> classesMethodsHashMap = new HashMap<>();
    public static HashMap<String, Integer> classesFieldsHashMap = new HashMap<>();

    public static List<String> methodInvocations = new ArrayList<>();

    public static int allRelationsCounter = 0;

    public static void main(String[] args) throws IOException {

        // read java files
        final File folder = new File(projectSourcePath);
        ArrayList<File> javaFiles = listJavaFilesForFolder(folder);

        //
        for (File fileEntry : javaFiles) {
            String content = FileUtils.readFileToString(fileEntry);
            // System.out.println(content);

            CompilationUnit parse = parse(content.toCharArray());

            // print methods info
            //printMethodInfo(parse);

            // print variables info
            // printVariableInfo(parse);

            //print method invocations
            printMethodInvocationInfo(parse);
            countNumberClass(parse);
            countNumberPackages(parse);
            getNumberOfLinesPerMethod(parse);
            getAverageNumberOfFieldsPerClass(parse);
            putClassesMethodsInHashMap(parse);
            putClassesFieldsInHashMap(parse);
            getMethodsWithLines(parse);
            getMaxParameters(parse);
            getTotalNumberOfLines(parse);
        }
        getClassesWithMostMethods();
        getClassesWithMostFields();
        moreThanXMethods(2);
        showExo1();

        System.setProperty("java.awt.headless", "false");
        createDiagram();
    }

    // read all java files from specific folder
    public static ArrayList<File> listJavaFilesForFolder(final File folder) {
        ArrayList<File> javaFiles = new ArrayList<File>();
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                javaFiles.addAll(listJavaFilesForFolder(fileEntry));
            } else if (fileEntry.getName().contains(".java")) {
                // System.out.println(fileEntry.getName());
                javaFiles.add(fileEntry);
            }
        }

        return javaFiles;
    }

    // create AST
    private static CompilationUnit parse(char[] classSource) {
        ASTParser parser = ASTParser.newParser(AST.JLS4); // java +1.6
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        parser.setBindingsRecovery(true);

        Map options = JavaCore.getOptions();
        parser.setCompilerOptions(options);

        parser.setUnitName("");

        String[] sources = {projectSourcePath};
        String[] classpath = {jrePath};

        parser.setEnvironment(classpath, sources, new String[]{"UTF-8"}, true);
        parser.setSource(classSource);

        return (CompilationUnit) parser.createAST(null); // create and parse
    }

    // navigate method information
    public static void printMethodInfo(CompilationUnit parse) {
        MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
        parse.accept(visitor);

        for (MethodDeclaration method : visitor.getMethods()) {
            System.out.println("Method name: " + method.getName()
                    + " Return type: " + method.getReturnType2());
        }

    }

    // navigate variables inside method
    public static void printVariableInfo(CompilationUnit parse) {

        MethodDeclarationVisitor visitor1 = new MethodDeclarationVisitor();
        parse.accept(visitor1);
        for (MethodDeclaration method : visitor1.getMethods()) {

            VariableDeclarationFragmentVisitor visitor2 = new VariableDeclarationFragmentVisitor();
            method.accept(visitor2);

            for (VariableDeclarationFragment variableDeclarationFragment : visitor2
                    .getVariables()) {
                System.out.println("variable name: "
                        + variableDeclarationFragment.getName()
                        + " variable Initializer: "
                        + variableDeclarationFragment.getInitializer());
            }

        }
    }

    // navigate method invocations inside method
    public static void printMethodInvocationInfo(CompilationUnit parse) {

        MethodDeclarationVisitor visitor1 = new MethodDeclarationVisitor();
        parse.accept(visitor1);
        for (MethodDeclaration method : visitor1.getMethods()) {

            MethodInvocationVisitor visitor2 = new MethodInvocationVisitor();
            method.accept(visitor2);
            StringBuilder methodName = new StringBuilder();
            if (method.resolveBinding() != null) {
                if (method.resolveBinding().getDeclaringClass() != null) {
                    methodName.append(method.resolveBinding().getDeclaringClass().getName()).append(".");
                }
            }
            methodName.append(method.getName().toString()).append("()");
            for (MethodInvocation methodInvocation : visitor2.getMethods()) {
                IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
                StringBuilder methodInvoName = new StringBuilder();
                if (methodBinding != null) {
                    ITypeBinding classTypeBinding = methodBinding.getDeclaringClass();
                    if (classTypeBinding != null) {
                        methodInvoName.append(classTypeBinding.getName()).append(".");
                    }
                }
                methodInvoName.append(methodInvocation.getName());
                methodInvocations.add("\t" + "\"" + methodName + "\"->\"" + methodInvoName + "()\";\n");
            }

        }
    }

    public static void countNumberClass(CompilationUnit parse) {
        TypeDeclarationVisitor typeDeclarationVisitor = new TypeDeclarationVisitor();
        parse.accept(typeDeclarationVisitor);
        typeDeclarationVisitor.getTypes().forEach(typeDeclaration -> {
            if (!typeDeclaration.isInterface()) {
                class_compter += 1;

            }
            method_compter += typeDeclaration.getMethods().length;
        });
    }

    public static void countNumberPackages(CompilationUnit parse) {
        PackageVisitor packageVisitor = new PackageVisitor();
        parse.accept(packageVisitor);
        packageVisitor.getPackageDeclarations().forEach(packageDeclaration -> packageList.add(packageDeclaration.getName().toString()));
    }

    public static void getNumberOfLinesPerMethod(CompilationUnit parse) {
        MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
        parse.accept(visitor);
        for (MethodDeclaration method : visitor.getMethods()) {
            linePerMethodList.add("La méthode " + method.getName() + " a " + getNumberOfLineOfAMethod(parse, method) + " lignes de codes");
        }
    }

    public static int getNumberOfLineOfAMethod(CompilationUnit parse, MethodDeclaration method) {
        if (method.getBody() == null) {
            return 0;
        }

        int beginning = parse.getLineNumber(method.getBody().getStartPosition());
        int end = parse.getLineNumber(method.getBody().getStartPosition() + method.getBody().getLength());

        return Math.max(end - beginning - 1, 0);
    }

    public static void getAverageNumberOfFieldsPerClass(CompilationUnit parse) {
        TypeDeclarationVisitor typeDeclarationVisitor = new TypeDeclarationVisitor();
        parse.accept(typeDeclarationVisitor);
        typeDeclarationVisitor.getTypes().forEach(typeDeclaration -> {
            if (!typeDeclaration.isInterface()) {
                fields_compter += typeDeclaration.getFields().length;
            }
        });
    }

    public static void putClassesMethodsInHashMap(CompilationUnit parse) {
        TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
        parse.accept(visitor);
        visitor.getTypes().forEach(type -> {
            if (!type.isInterface())
                classesMethodsHashMap.put(type.getName().toString(), type.getMethods().length);
        });
    }

    public static void getClassesWithMostMethods() {
        int numberOfClasses = (int) Math.ceil(0.1 * classesMethodsHashMap.size());

        List<String> classes = classesMethodsHashMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        classesWithMostMethods = classes.subList(0, numberOfClasses);
    }

    public static void putClassesFieldsInHashMap(CompilationUnit parse) {
        TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
        parse.accept(visitor);

        visitor.getTypes().forEach(type -> {
            if (!type.isInterface()) {
                classesFieldsHashMap.put(type.getName().toString(), type.getFields().length);
            }
        });
    }

    public static void getClassesWithMostFields() {
        int numberOfClasses = (int) Math.ceil(0.1 * classesFieldsHashMap.size());

        List<String> classes = classesFieldsHashMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        classesWithMostFields = classes.subList(0, numberOfClasses);
    }

    public static List<String> getClassesWithMostFieldsAndMethods() {
        List<String> res = new ArrayList<String>(classesWithMostMethods);
        res.retainAll(classesWithMostFields);
        return res;
    }

    public static void moreThanXMethods(int x) {
        classesMethodsHashMap.forEach((key, value) -> {
            if (value > x) {
                moreThanXList.add(key);
            }
        });
    }

    public static void getMethodsWithLines(CompilationUnit parse) {
        TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
        parse.accept(visitor);

        for (TypeDeclaration type : visitor.getTypes()) {
            if (type.isInterface())
                continue;

            Map<String, Integer> methodsWithLines = new HashMap<String, Integer>();

            for (MethodDeclaration method : type.getMethods())
                methodsWithLines.put(type.getName() + "." + method.getName(), getNumberOfLineOfAMethod(parse, method));

            methodsWithNumberOfLinesByClass.add(methodsWithLines);
        }
    }

    public static List<String> getMethodsWithMostLines() {

        List<String> methodsWithMostLines = new ArrayList<String>();

        for (Map<String, Integer> methodsWithLines : methodsWithNumberOfLinesByClass) {
            int numberOfMethods = (int) Math.ceil(0.1 * methodsWithLines.size());

            List<String> methods = methodsWithLines.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            methodsWithMostLines.addAll(methods.subList(0, numberOfMethods));
        }

        return methodsWithMostLines;
    }

    public static void getMaxParameters(CompilationUnit parse) {
        MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
        parse.accept(visitor);
        visitor.getMethods().forEach(methodDeclaration -> {
            if (methodDeclaration.parameters().size() > max_parameter) {
                max_parameter = methodDeclaration.parameters().size();
            }
        });
    }

    public static void getTotalNumberOfLines(CompilationUnit parse) {
        TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
        parse.accept(visitor);

        visitor.getTypes().forEach(typeDeclaration -> {
            int beginning = parse.getLineNumber(typeDeclaration.getStartPosition());
            int end = parse.getLineNumber(typeDeclaration.getStartPosition() + typeDeclaration.getLength() - 1);
            app_line_compter += Math.max((end - beginning), 0);
        });
        PackageVisitor visitor1 = new PackageVisitor();
        parse.accept(visitor1);
        app_line_compter += visitor1.getPackageDeclarations().size();

        ImportDeclarationVisitor visitor2 = new ImportDeclarationVisitor();
        parse.accept(visitor2);
        app_line_compter += visitor2.getImports().size();

        app_line_compter += parse.getCommentList().size();
    }

    public static void createDiagram() {
        try {
            String name = UUID.randomUUID().toString();
            FileWriter writer = new FileWriter("export/dot/" + name + ".dot");
            writer.write("digraph \"call-graph\" {\n");
            methodInvocations.stream().distinct().collect(Collectors.toList()).forEach(methodInvocation -> {
                try {
                    writer.write(methodInvocation);
                } catch (IOException e) {
                    System.out.println("Une erreur est survenue au niveau de l'écriture des liens");
                }
            });
            writer.write("}");
            writer.close();
            System.out.println("");
            System.out.println("un fichier a bien été créé");
            convertDiagramToPng(name);
        } catch (IOException e) {
            System.out.println("Une erreur s'est produite.");
        }
    }

    public static void convertDiagramToPng(String name) {
        try (InputStream dot = new FileInputStream("export/dot/" + name + ".dot")) {
            MutableGraph g = new Parser().read(dot);
            Graphviz.fromGraph(g).width(10000).render(Format.PNG).toFile(new File("export/images/" + name + ".png"));
            System.out.println("Votre graphique a été généré au format PNG");
            showView("export/images/" + name + ".png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showExo1() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Résultats de l'analyse du programme");
        Toolkit k = Toolkit.getDefaultToolkit();
        Dimension tailleEcran = k.getScreenSize();
        int largeurEcran = tailleEcran.width;
        int hauteurEcran = tailleEcran.height;
        frame.setSize(largeurEcran / 2, hauteurEcran / 4);

        JPanel panel = new JPanel();
        int row = 30 + linePerMethodList.size() + classesWithMostMethods.size() + classesWithMostFields.size() + getClassesWithMostFieldsAndMethods().size() + moreThanXList.size() + getMethodsWithMostLines().size();
        panel.setLayout(new GridLayout(row, 1));

        panel.add(new JLabel("Nombre de classes de l'application -> " + class_compter));

        panel.add(new JLabel("Nombre de méthodes de l'application -> " + method_compter));

        panel.add(new JLabel("Nombre de paquets de l'application -> " + (int) packageList.stream().distinct().count()));

        panel.add(new JLabel("Nombre moyen de méthodes par classes -> " + (method_compter / class_compter)));

        panel.add(new JLabel("--------------Lignes de code Par Méthodes----------- "));
        linePerMethodList.forEach(linePerMethod -> {
            panel.add(new JLabel(linePerMethod));
        });
        panel.add(new JLabel("-----------------------------------------"));
        panel.add(new JLabel("Nombre moyen d'attributs par classes -> " + (fields_compter / class_compter)));

        panel.add(new JLabel("-----------Les 10% de classes avec le plus grand nombre de méthodes----------"));
        classesWithMostMethods.forEach(parameter -> {
            panel.add(new JLabel(parameter));
        });
        panel.add(new JLabel("---------------------------------------------------------------------"));

        panel.add(new JLabel("-----------Les 10% de classes avec le plus grand nombre d'attributs----------"));
        classesWithMostFields.forEach(parameter -> {
            panel.add(new JLabel(parameter));
        });
        panel.add(new JLabel("---------------------------------------------------------------------"));

        panel.add(new JLabel("Les classes appartenant aux deux catégories différentes"));
        getClassesWithMostFieldsAndMethods().forEach(parameter -> {
            panel.add(new JLabel(parameter));
        });
        panel.add(new JLabel("---------------------------------------------------------------------"));

        panel.add(new JLabel("------------Voici les différentes classes avec plus de x méthodes : -----------"));
        moreThanXList.forEach(parameter -> {
            panel.add(new JLabel(parameter));
        });
        panel.add(new JLabel("---------------------------------------------------------------------"));

        panel.add(new JLabel("-----------Les 10% des méthodes qui possèdent le plus grand nombre de lignes de code (par classe)-------"));
        getMethodsWithMostLines().forEach(parameter -> {
            panel.add(new JLabel(parameter));
        });
        panel.add(new JLabel("---------------------------------------------------------------------"));

        panel.add(new JLabel("Le nombre maximal de paramètres est : " + max_parameter));

        panel.add(new JLabel("Nombre total de lignes de code -> " + app_line_compter));

        frame.add(new JScrollPane(panel));
        frame.show();
    }

    public static void showView(String filename) throws IOException {
        Desktop.getDesktop().open(new File(filename));
    }

    public static int getNumberOfMethodInvocation(TypeDeclaration A) {
        return A.getMethods().length;
    }

    public static void countAllRelations(CompilationUnit parse) {
        TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
        parse.accept(visitor);
        // pour chaque classe,si ce n'est pas uneinterface,
        for (TypeDeclaration typeDeclaration : visitor.getTypes()) {
            if (typeDeclaration.isInterface()) continue;
            for (MethodDeclaration method : typeDeclaration.getMethods()) {
                MethodInvocationVisitor visitor2 = new MethodInvocationVisitor();
                method.accept(visitor2);
                for (MethodInvocation methodInvocation : visitor2.getMethods()) {
                    IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
                    if (methodBinding != null) {
                        ITypeBinding classTypeBinding = methodBinding.getDeclaringClass();
                        if (classTypeBinding != null && !classTypeBinding.getName().equals(typeDeclaration.getName().toString())) {
                            allRelationsCounter += 1;
                        }
                    }
                }
            }
        }
    }

    public static int numberOfRelationBetweenAAndB(TypeDeclaration A, TypeDeclaration B) {
        int numberOfRelations = 0;

        if (A.isInterface() || B.isInterface()) {
            return 0;
        }

        // pour chaque méthodes de A
        for (MethodDeclaration Amethod : A.getMethods()) {
            MethodInvocationVisitor visitor = new MethodInvocationVisitor();
            Amethod.accept(visitor);
            // on regarde chaque invocation si elle invoque une méthode de B
            for (MethodInvocation methodInvocation : visitor.getMethods()) {
                IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
                if (methodBinding != null) {
                    ITypeBinding classTypeBinding = methodBinding.getDeclaringClass();
                    if (classTypeBinding != null && classTypeBinding.getName().equals(B.getName().toString()))
                        numberOfRelations += 1;
                }
            }
        }


        return numberOfRelations;
    }

//    public static float calculCouplage(TypeDeclaration A, TypeDeclaration B){
//
//    }
}
