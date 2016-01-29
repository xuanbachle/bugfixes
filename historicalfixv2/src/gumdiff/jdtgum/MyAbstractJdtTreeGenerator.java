/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2015 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright 2011-2015 Floréal Morandat <florealm@gmail.com>
 */

package gumdiff.jdtgum;

import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.gen.jdt.AbstractJdtVisitor;
import com.github.gumtreediff.tree.TreeContext;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public abstract class MyAbstractJdtTreeGenerator extends TreeGenerator {
    public CompilationUnit getCompUnit() {
        return compUnit;
    }

    protected CompilationUnit compUnit = null;
    private static char[] readerToCharArray(Reader r) throws IOException {
        StringBuilder fileData = new StringBuilder(1000);
        BufferedReader br = new BufferedReader(r);

        char[] buf = new char[10];
        int numRead = 0;
        while ((numRead = br.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        br.close();

        return  fileData.toString().toCharArray();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TreeContext generate(Reader r) throws IOException {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        Map pOptions = JavaCore.getOptions();
        pOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
        pOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_7);
        pOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
        parser.setSource(readerToCharArray(r));
        //parser.setCompilerOptions(pOptions);
        //parser.setUnitName("DefaultBuildContext.java");
        //parser.setEnvironment(new String[]{"/usr/java/jdk1.8.0_51/jre/lib/rt.jar"}, new String[]{"/home/dxble/Desktop/data_bigquery/alldata_withUnit/all/takari_io.takari.incrementalbuild/modifiedFiles/4/fix"}, new String[]{"UTF-8"}, true);
        //parser.setUnitName("Apple.java");
        //parser.setEnvironment(new String[]{"/usr/java/jdk1.8.0_51/jre/lib/rt.jar"}, new String[]{"/home/dxble/Test/tester"}, new String[]{"UTF-8"}, true);
        //parser.setResolveBindings(true);
        MyAbstractJdtVisitor v = createVisitor();
        compUnit=(CompilationUnit)parser.createAST(null);
        if (compUnit.getAST().hasBindingsRecovery()) {
            System.out.println("***********************Binding activated.");
        }
        compUnit.accept(v);
        return v.getTreeContext();
    }

    protected abstract MyAbstractJdtVisitor createVisitor();
}
