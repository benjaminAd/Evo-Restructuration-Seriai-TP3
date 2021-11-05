package com.exemple.tp2;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import java.util.ArrayList;
import java.util.List;

public class PackageVisitor extends ASTVisitor {
    List<PackageDeclaration> packageDeclarations = new ArrayList<>();

    @Override
    public boolean visit(PackageDeclaration node) {
        packageDeclarations.add(node);
        return super.visit(node);
    }

    public List<PackageDeclaration> getPackageDeclarations() {
        return packageDeclarations;
    }
}
