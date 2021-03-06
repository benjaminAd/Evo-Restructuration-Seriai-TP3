package com.exemple.tp2;

import com.exemple.tp2.library.Cluster;
import com.exemple.tp2.library.Pair;
import com.exemple.tp2.library.Triplet;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ParserAST {
    public static final String projectPath = "/Users/benjaminadolphe/Downloads/seriousgame_environnement";

//        public static final String projectPath = "C:\\Users\\Alex\\Documents\\GitHub\\TP3_Refactoring\\After_Refactoring\\GoodBank";
//    public static final String projectPath = "C:\\Users\\Alex\\Documents\\GitHub\\uaa-develop\\server";
    //public static final String projectPath = "A:\\Projets\\seriousgame_environnement";

    public static final String projectSourcePath = projectPath + "/src";

    public static final String jrePath = "/Users/benjaminadolphe/Library/Java/JavaVirtualMachines/azul-17.0.1/Contents/Home/bin";
    //public static final String jrePath = "C:\\Program Files\\Java\\jre1.8.0_301";

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

    public static List<TypeDeclaration> typeDeclarationList = new ArrayList<>();
    public static List<String> typeDeclarationNames = new ArrayList<>();
    public static List<String> graphCouplageList = new ArrayList<>();

    public static Map<Pair<String, String>, Float> couplageMap = new HashMap<>();

    public static int allRelationsCounter = 0;

    private static int countLevel = 0;

    public static void main(String[] args) throws IOException {

        // read java files
        final File folder = new File(projectSourcePath);
        ArrayList<File> javaFiles = listJavaFilesForFolder(folder);
        StringBuilder allContent = new StringBuilder();
        for (File fileEntry : javaFiles) {
            String content = FileUtils.readFileToString(fileEntry);
            allContent.append(content);
        }
        CompilationUnit parse = parse(allContent.toString().toCharArray());

        // print methods info
        //printMethodInfo(parse);

        // print variables info
        // printVariableInfo(parse);

        //print method invocations
//        printMethodInvocationInfo(parse);
//        countNumberClass(parse);
//        countNumberPackages(parse);
//        getNumberOfLinesPerMethod(parse);
//        getAverageNumberOfFieldsPerClass(parse);
//        putClassesMethodsInHashMap(parse);
//        putClassesFieldsInHashMap(parse);
//        getMethodsWithLines(parse);
//        getMaxParameters(parse);
//        getTotalNumberOfLines(parse);
        addTypeDeclarationToList(parse);

//        getClassesWithMostMethods();
//        getClassesWithMostFields();
//        moreThanXMethods(2);
//        showExo1();

        System.setProperty("java.awt.headless", "false");
//        createDiagram();

        countAllRelations();
        createListGraphePondere();
        createGraphePondere();
        Cluster dendro = clusteringHierarchique();
        System.out.println("Cluster : " + dendro);
        List<Cluster> clusters = selection_cluster(dendro);
        for (Cluster cluster : clusters) {
            System.out.println("Un cluster : \n" + cluster);
        }
    }

    // read all java files from specific folder
    public static ArrayList<File> listJavaFilesForFolder(final File folder) {
        ArrayList<File> javaFiles = new ArrayList<File>();
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                javaFiles.addAll(listJavaFilesForFolder(fileEntry));
            } else if (fileEntry.getName().contains(".java")) {
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
    private static void printMethodInfo(CompilationUnit parse) {
        MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
        parse.accept(visitor);

        for (MethodDeclaration method : visitor.getMethods()) {
            System.out.println("Method name: " + method.getName()
                    + " Return type: " + method.getReturnType2());
        }

    }

    // navigate variables inside method
    private static void printVariableInfo(CompilationUnit parse) {

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
    private static void printMethodInvocationInfo(CompilationUnit parse) {

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

    private static void countNumberClass(CompilationUnit parse) {
        TypeDeclarationVisitor typeDeclarationVisitor = new TypeDeclarationVisitor();
        parse.accept(typeDeclarationVisitor);
        typeDeclarationVisitor.getTypes().forEach(typeDeclaration -> {
            if (!typeDeclaration.isInterface()) {
                class_compter += 1;

            }
            method_compter += typeDeclaration.getMethods().length;
        });
    }

    private static void countNumberPackages(CompilationUnit parse) {
        PackageVisitor packageVisitor = new PackageVisitor();
        parse.accept(packageVisitor);
        packageVisitor.getPackageDeclarations().forEach(packageDeclaration -> packageList.add(packageDeclaration.getName().toString()));
    }

    private static void getNumberOfLinesPerMethod(CompilationUnit parse) {
        MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
        parse.accept(visitor);
        for (MethodDeclaration method : visitor.getMethods()) {
            linePerMethodList.add("La m??thode " + method.getName() + " a " + getNumberOfLineOfAMethod(parse, method) + " lignes de codes");
        }
    }

    private static int getNumberOfLineOfAMethod(CompilationUnit parse, MethodDeclaration method) {
        if (method.getBody() == null) {
            return 0;
        }

        int beginning = parse.getLineNumber(method.getBody().getStartPosition());
        int end = parse.getLineNumber(method.getBody().getStartPosition() + method.getBody().getLength());

        return Math.max(end - beginning - 1, 0);
    }

    private static void getAverageNumberOfFieldsPerClass(CompilationUnit parse) {
        TypeDeclarationVisitor typeDeclarationVisitor = new TypeDeclarationVisitor();
        parse.accept(typeDeclarationVisitor);
        typeDeclarationVisitor.getTypes().forEach(typeDeclaration -> {
            if (!typeDeclaration.isInterface()) {
                fields_compter += typeDeclaration.getFields().length;
            }
        });
    }

    private static void putClassesMethodsInHashMap(CompilationUnit parse) {
        TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
        parse.accept(visitor);
        visitor.getTypes().forEach(type -> {
            if (!type.isInterface())
                classesMethodsHashMap.put(type.getName().toString(), type.getMethods().length);
        });
    }

    private static void getClassesWithMostMethods() {
        int numberOfClasses = (int) Math.ceil(0.1 * classesMethodsHashMap.size());

        List<String> classes = classesMethodsHashMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        classesWithMostMethods = classes.subList(0, numberOfClasses);
    }

    private static void putClassesFieldsInHashMap(CompilationUnit parse) {
        TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
        parse.accept(visitor);

        visitor.getTypes().forEach(type -> {
            if (!type.isInterface()) {
                classesFieldsHashMap.put(type.getName().toString(), type.getFields().length);
            }
        });
    }

    private static void getClassesWithMostFields() {
        int numberOfClasses = (int) Math.ceil(0.1 * classesFieldsHashMap.size());

        List<String> classes = classesFieldsHashMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        classesWithMostFields = classes.subList(0, numberOfClasses);
    }

    private static List<String> getClassesWithMostFieldsAndMethods() {
        List<String> res = new ArrayList<String>(classesWithMostMethods);
        res.retainAll(classesWithMostFields);
        return res;
    }

    private static void moreThanXMethods(int x) {
        classesMethodsHashMap.forEach((key, value) -> {
            if (value > x) {
                moreThanXList.add(key);
            }
        });
    }

    private static void getMethodsWithLines(CompilationUnit parse) {
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

    private static List<String> getMethodsWithMostLines() {

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

    private static void getMaxParameters(CompilationUnit parse) {
        MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
        parse.accept(visitor);
        visitor.getMethods().forEach(methodDeclaration -> {
            if (methodDeclaration.parameters().size() > max_parameter) {
                max_parameter = methodDeclaration.parameters().size();
            }
        });
    }

    private static void getTotalNumberOfLines(CompilationUnit parse) {
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

    private static void createDiagram() {
        try {
            String name = UUID.randomUUID().toString();
            FileWriter writer = new FileWriter("export/dot/" + name + ".dot");
            writer.write("digraph \"call-graph\" {\n");
            distinct(writer, methodInvocations);
            convertDiagramToPng(name);
        } catch (IOException e) {
            System.out.println("Une erreur s'est produite.");
        }
    }

    private static void convertDiagramToPng(String name) {
        try (InputStream dot = new FileInputStream("export/dot/" + name + ".dot")) {
            MutableGraph g = new Parser().read(dot);
            Graphviz.fromGraph(g).width(10000).render(Format.PNG).toFile(new File("export/images/" + name + ".png"));
            System.out.println("\nVotre graphique a ??t?? g??n??r?? au format PNG");
            showView("export/images/" + name + ".png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void showExo1() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("R??sultats de l'analyse du programme");
        Toolkit k = Toolkit.getDefaultToolkit();
        Dimension tailleEcran = k.getScreenSize();
        int largeurEcran = tailleEcran.width;
        int hauteurEcran = tailleEcran.height;
        frame.setSize(largeurEcran / 2, hauteurEcran / 4);

        JPanel panel = new JPanel();
        int row = 30 + linePerMethodList.size() + classesWithMostMethods.size() + classesWithMostFields.size() + getClassesWithMostFieldsAndMethods().size() + moreThanXList.size() + getMethodsWithMostLines().size();
        panel.setLayout(new GridLayout(row, 1));

        panel.add(new JLabel("Nombre de classes de l'application -> " + class_compter));

        panel.add(new JLabel("Nombre de m??thodes de l'application -> " + method_compter));

        panel.add(new JLabel("Nombre de paquets de l'application -> " + (int) packageList.stream().distinct().count()));

        panel.add(new JLabel("Nombre moyen de m??thodes par classes -> " + (method_compter / class_compter)));

        panel.add(new JLabel("--------------Lignes de code Par M??thodes----------- "));
        linePerMethodList.forEach(linePerMethod -> {
            panel.add(new JLabel(linePerMethod));
        });
        panel.add(new JLabel("-----------------------------------------"));
        panel.add(new JLabel("Nombre moyen d'attributs par classes -> " + (fields_compter / class_compter)));

        panel.add(new JLabel("-----------Les 10% de classes avec le plus grand nombre de m??thodes----------"));
        classesWithMostMethods.forEach(parameter -> {
            panel.add(new JLabel(parameter));
        });
        panel.add(new JLabel("---------------------------------------------------------------------"));

        panel.add(new JLabel("-----------Les 10% de classes avec le plus grand nombre d'attributs----------"));
        classesWithMostFields.forEach(parameter -> {
            panel.add(new JLabel(parameter));
        });
        panel.add(new JLabel("---------------------------------------------------------------------"));

        panel.add(new JLabel("Les classes appartenant aux deux cat??gories diff??rentes"));
        getClassesWithMostFieldsAndMethods().forEach(parameter -> {
            panel.add(new JLabel(parameter));
        });
        panel.add(new JLabel("---------------------------------------------------------------------"));

        panel.add(new JLabel("------------Voici les diff??rentes classes avec plus de x m??thodes : -----------"));
        moreThanXList.forEach(parameter -> {
            panel.add(new JLabel(parameter));
        });
        panel.add(new JLabel("---------------------------------------------------------------------"));

        panel.add(new JLabel("-----------Les 10% des me??thodes qui posse??dent le plus grand nombre de lignes de code (par classe)-------"));
        getMethodsWithMostLines().forEach(parameter -> {
            panel.add(new JLabel(parameter));
        });
        panel.add(new JLabel("---------------------------------------------------------------------"));

        panel.add(new JLabel("Le nombre maximal de param??tres est : " + max_parameter));

        panel.add(new JLabel("Nombre total de lignes de code -> " + app_line_compter));

        frame.add(new JScrollPane(panel));
        frame.show();
    }

    private static void showView(String filename) throws IOException {
        Desktop.getDesktop().open(new File(filename));
    }

    // --- TP3 --- //

    // --- exo 1.1 --- //

    private static void countAllRelations() {
        for (TypeDeclaration typeDeclaration : typeDeclarationList) {
            if (typeDeclaration.isInterface()) continue;
            for (MethodDeclaration method : typeDeclaration.getMethods()) {
                MethodInvocationVisitor visitor2 = new MethodInvocationVisitor();
                method.accept(visitor2);
                for (MethodInvocation methodInvocation : visitor2.getMethods()) {
                    IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
                    if (methodBinding != null) {
                        ITypeBinding classTypeBinding = methodBinding.getDeclaringClass();
                        if (classTypeBinding != null && !classTypeBinding.getName().equals(typeDeclaration.getName().toString()) && typeDeclarationNames.contains(classTypeBinding.getName())) {
//                            System.out.println("Relation : " + typeDeclaration.getName().toString() + " --> " + classTypeBinding.getName() + " pour la m??thode : " + methodInvocation.getName());
                            allRelationsCounter += 1;
                        }
                    }
                }
            }
        }
    }

    private static int numberOfRelationBetweenTwoClasses(TypeDeclaration A, TypeDeclaration B) {
        int numberOfRelations = 0;

        if (A.isInterface() || B.isInterface()) {
            return 0;
        }

        // pour chaque m??thodes de A
        for (MethodDeclaration Amethod : A.getMethods()) {
            MethodInvocationVisitor visitor = new MethodInvocationVisitor();
            Amethod.accept(visitor);
            for (MethodInvocation methodInvocation : visitor.getMethods()) {
                IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
                if (methodBinding != null) {
                    ITypeBinding classTypeBinding = methodBinding.getDeclaringClass();
                    if (classTypeBinding != null && classTypeBinding.getName().equals(B.getName().toString())) {
                        numberOfRelations += 1;
                    }
                }
            }
        }

        for (MethodDeclaration Bmethod : B.getMethods()) {
            MethodInvocationVisitor visitor = new MethodInvocationVisitor();
            Bmethod.accept(visitor);
            for (MethodInvocation methodInvocation : visitor.getMethods()) {
                IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
                if (methodBinding != null) {
                    ITypeBinding classTypeBinding = methodBinding.getDeclaringClass();
                    if (classTypeBinding != null && classTypeBinding.getName().equals(A.getName().toString())) {
                        numberOfRelations += 1;
                    }
                }
            }
        }

        return numberOfRelations;
    }

    private static String calculCouplage(TypeDeclaration A, TypeDeclaration B) {
        if (allRelationsCounter == 0) return "0";
//        System.out.println("Nombre de relation entre " + A.getName().toString() + " et " + B.getName().toString() + " est de " + numberOfRelationBetweenTwoClasses(A, B) + "/" + allRelationsCounter);
        return numberOfRelationBetweenTwoClasses(A, B) + "/" + allRelationsCounter;
    }

    private static float calculCouplageFloat(String couplage) {
        String[] elements = couplage.split("/");
        return (float) Integer.parseInt(elements[0]) / Integer.parseInt(elements[1]);
    }

    private static void addTypeDeclarationToList(CompilationUnit parse) {
        TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
        parse.accept(visitor);
        typeDeclarationList.addAll(visitor.getTypes());
        typeDeclarationNames.addAll(visitor.getTypes().stream().map((element) -> element.getName().toString()).toList());
    }

    private static void createListGraphePondere() {
        for (int i = 0; i < typeDeclarationList.size(); i++) {
            for (int j = i; j < typeDeclarationList.size(); j++) {
                TypeDeclaration typeDeclaration = typeDeclarationList.get(i);
                TypeDeclaration typeDeclaration1 = typeDeclarationList.get(j);
                if (typeDeclaration1.getName().equals(typeDeclaration.getName())) {
                    continue;
                }
                String couplageString = calculCouplage(typeDeclaration, typeDeclaration1);
                float couplageFloat = calculCouplageFloat(couplageString);
//                System.out.println("couplage btw " + typeDeclaration.getName().toString() + " et " + typeDeclaration1.getName().toString() + " " + couplageFloat);
                DecimalFormat df = new DecimalFormat("#.####");
                couplageMap.put(new Pair<String, String>(typeDeclaration.getName().toString(), typeDeclaration1.getName().toString()), couplageFloat);
                graphCouplageList.add("\t" + "\"" + typeDeclaration.getName() + "\"--\"" + typeDeclaration1.getName() + "\"[label=\"" + df.format(couplageFloat) + " (" + couplageString + ")" + "\"];\n");
            }
        }
    }

    // --- exo 1.2 --- //

    private static void createGraphePondere() {
        try {
            String name = UUID.randomUUID().toString();
            FileWriter writer = new FileWriter("export/dot/graphePondere/" + name + ".dot");
            writer.write("graph \"call-graph\" {\n");
            distinct(writer, graphCouplageList);
            convertDiagramToPng("graphePondere/" + name);
        } catch (IOException e) {
            System.out.println("Une erreur s'est produite.");
        }
    }

    private static void distinct(FileWriter writer, List<String> graphCouplageList) throws IOException {
        graphCouplageList.stream().distinct().collect(Collectors.toList()).forEach(couplageElement -> {
            try {
                writer.write(couplageElement);
            } catch (IOException e) {
                System.out.println("Une erreur est survenue au niveau de l'??criture des liens");
            }
        });
        writer.write("}");
        writer.close();
        System.out.println("\nun fichier a bien ??t?? cr????");
    }

    // --- exo 2.1 --- //
    private static Triplet<Cluster, Cluster, Float> clusterProche(Map<Pair<String, String>, Float> couplages, List<Cluster> clusters) {
        Pair<Cluster, Cluster> pair = new Pair<>();
        float max = 0;
        Cluster first = null;
        Cluster second = null;
        for (Cluster cluster : clusters) {
            for (Cluster cluster1 : clusters) {
                if (cluster == cluster1) continue;
                float couplage = calculCouplageBetweenClusters(couplages, cluster, cluster1);
                if (couplage >= max) {
                    max = couplage;
                    first = cluster;
                    second = cluster1;
                }
            }
        }
        pair.setKey(first);
        pair.setValue(second);
        return new Triplet<>(pair, max);
    }

    private static float calculCouplageBetweenClusters(Map<Pair<String, String>, Float> couplages, Cluster c1, Cluster c2) {
        float sum = 0;
        for (String aClass : c1.getClasses()) {
            for (String bClass : c2.getClasses()) {
                for (Map.Entry<Pair<String, String>, Float> mapEntry : couplages.entrySet()) {
                    if (mapEntry.getKey().equals(new Pair<>(aClass, bClass))) {
                        sum += mapEntry.getValue();
                    }
                }
            }
        }
        return sum;
    }

    private static ArrayList<Cluster> convertNameToClusters() {
        return new ArrayList<>(typeDeclarationNames.stream().map(Cluster::new).toList());
    }

    private static Cluster clusteringHierarchique() {
        List<Cluster> clusters = convertNameToClusters();
        while (clusters.size() > 1) {
            Triplet<Cluster, Cluster, Float> triplet = clusterProche(couplageMap, clusters);
            Cluster c3 = new Cluster(triplet.getFirst(), triplet.getSecond(), triplet.getThird(), countLevel);
            countLevel++;
            clusters.remove(triplet.getFirst());
            clusters.remove(triplet.getSecond());
            clusters.add(c3);
        }
        return clusters.get(0);
    }

    private static List<Cluster> selection_cluster(Cluster dendro) {
        List<Cluster> R = new ArrayList<>();
        Stack<Cluster> parcoursCluster = new Stack<>();
        parcoursCluster.push(dendro);
        while (!parcoursCluster.isEmpty()) {
            Cluster pere = parcoursCluster.pop();
            Pair<Cluster, Cluster> fils = pere.getPairs();
            Cluster f1 = fils.getKey();
            Cluster f2 = fils.getValue();
            if (f1 == null || f2 == null) {
                R.add(pere);
                continue;
            }
            if (S(pere) > moyenne(S(f1, f2))) {
                R.add(pere);
            } else {
                parcoursCluster.push(f1);
                parcoursCluster.push(f2);
            }
        }
        return R;
    }

    private static float S(Cluster p) {
        float s1 = p.getValue();
        float s2 = S2(p);
        return s1 / s2;
    }

    private static float S2(Cluster p) {
        float sum = 0;
        for (String aClass : p.getClasses()) {
            for (String typeDeclarationName : typeDeclarationNames) {
                if (p.getClasses().contains(typeDeclarationName)) continue;
                for (Map.Entry<Pair<String, String>, Float> entry : couplageMap.entrySet()) {
                    if (entry.getKey().equals(new Pair<>(aClass, typeDeclarationName))) {
                        sum += entry.getValue();
                    }
                }
            }
        }
        if (sum == 0) sum = 1;
        return sum;
    }

    private static Pair<Float, Float> S(Cluster c1, Cluster c2) {
        return new Pair<>(S(c1), S(c2));
    }

    private static float moyenne(Pair<Float, Float> pair) {
        return (pair.getValue() + pair.getKey()) / 2;
    }

}
