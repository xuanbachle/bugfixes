package parsers.javaparser;
import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.TreeContext;
import gumdiff.difftemplates.ActionHandling;
import gumdiff.difftemplates.DiffTemplates;
import gumdiff.jdtgum.MyJdtTreeGenerator;
import mainscala.RepairOptions;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.TextEdit;
import repair.representation.GenProgIndividual;

import java.io.IOException;
import java.util.*;
/**
 * Created by dxble on 7/31/15.
 */
public class ASTRewriteFactory {
    //private static LinkedHashMap<String,ASTRewrite> mapOriginalRewriter  = new LinkedHashMap<String,ASTRewrite>();
    private static LinkedHashMap<String,ASTRewrite> modifiedRewriter = new LinkedHashMap<>();
    private static HashMap<String, String> modifiedRewriterContent = new HashMap<>();
    public static String INVALID_GRAPH = "invalid_graph";
    /*static{
        mapOriginalRewriter = new HashMap<String,ASTRewrite>();
        modifiedRewriter = new LinkedHashMap<>();
    }*/

    public static ASTRewrite getRewriterUptoDate(String fileName) {
        fileName = standardize(fileName);

        if (fileName.indexOf(".") == -1)
            System.out.println(fileName);
        /*if (!mapOriginalRewriter.containsKey(fileName)) {
            CompilationUnit cu = JavaParser.getCompilationUnit(fileName);
            mapOriginalRewriter.put(fileName, ASTRewrite.create(cu.getAST()));
        }*/

        if(!modifiedRewriter.containsKey(fileName)){
            CompilationUnit cu = JavaParser.getCompilationUnit(fileName);
            ASTRewrite original = ASTRewrite.create(cu.getAST());
            updateModifiedRewriters(fileName, original);
        }
        return modifiedRewriter.get(fileName);

        /*else {
            return mapOriginalRewriter.get(fileName);
        }*/
    }

    public static void updateModifiedRewriters(String fileName, ASTRewrite rw){
        modifiedRewriter.put(fileName, rw);
    }

    public static String computeGraphsForModifiedRewriters() throws BadLocationException, IOException {
        if(modifiedRewriter.size()==0)
            throw new RuntimeException("Attempting to create graphs for empty rewriters!");

        Iterator<Map.Entry<String, ASTRewrite>> iter = modifiedRewriter.entrySet().iterator();
        int graphID = 0;
        String graphsText = "";
        while (iter.hasNext()) {
            Map.Entry<String, ASTRewrite> ent = iter.next();
            //ASTRewrite originalRewriter = mapOriginalRewriter.get(ent.getKey());//key is fileName
            String originalContent = JavaParser.modifyingFiles().get(ent.getKey());
            //System.out.println(JavaParser.modifyingFiles());
            org.eclipse.jface.text.Document modifiedDoc = new org.eclipse.jface.text.Document(originalContent);
            TextEdit edits = modifiedRewriter.get(ent.getKey()).rewriteAST(modifiedDoc, null);
            edits.apply(modifiedDoc);
            String modifiedContent = modifiedDoc.get();
            // if source file on disk is not modified (complied on memory), we do this
            if(!RepairOptions.chosenTestInvokerInstance().modifiedSourceFileOnDisk())
                modifiedRewriterContent.put(ent.getKey(), modifiedContent);
            String oneGraph= computeOneDiffGraph(originalContent, modifiedContent, graphID);
            graphsText += oneGraph;
            graphID++;
        }
        return graphsText;
    }

    public static String computeGraphsForModifiedRewriters2(GenProgIndividual variant) throws BadLocationException, IOException {
        //if(modifiedRewriter.size()==0)
        //    throw new RuntimeException("Attempting to create graphs for empty rewriters!");
        HashMap<String, String> rewriterContentBefLast = new HashMap<>();
        if(variant.getGenome().length()==0)
            return "";

        Boolean transferSuccess=variant.transferNotLastGene2StaticWriterFactory();
        if(!transferSuccess)
            return INVALID_GRAPH;

        Iterator<Map.Entry<String, ASTRewrite>> iter = modifiedRewriter.entrySet().iterator();

        //To get content before apply last gene
        while (iter.hasNext()) {
            Map.Entry<String, ASTRewrite> ent = iter.next();
            //ASTRewrite originalRewriter = mapOriginalRewriter.get(ent.getKey());//key is fileName
            String originalContent = JavaParser.modifyingFiles().get(ent.getKey());
            //System.out.println(JavaParser.modifyingFiles());
            org.eclipse.jface.text.Document modifiedDoc = new org.eclipse.jface.text.Document(originalContent);

            String modifiedContentNotLast = "";
            if(variant.getGenome().length() == 1){
                modifiedContentNotLast = originalContent;
            }else{
                TextEdit edits = modifiedRewriter.get(ent.getKey()).rewriteAST(modifiedDoc, null);
                edits.apply(modifiedDoc);
                modifiedContentNotLast = modifiedDoc.get();
            }

            rewriterContentBefLast.put(ent.getKey(), modifiedContentNotLast);
        }
        //Apply last gene
        transferSuccess = variant.transferLastGene2StaticWriterFactory();
        if(!transferSuccess)
            return INVALID_GRAPH;

        //After applying last gene, we compute frequency
        int graphID = 0;
        String graphsText = "";
        Iterator<Map.Entry<String, ASTRewrite>> iter2 = modifiedRewriter.entrySet().iterator();
        while (iter2.hasNext()){
            Map.Entry<String, ASTRewrite> ent = iter2.next();

            String modifiedContentByVariant = "";
            String originalContent = JavaParser.modifyingFiles().get(ent.getKey());
            org.eclipse.jface.text.Document modifiedDoc = new org.eclipse.jface.text.Document(originalContent);
            TextEdit edits = modifiedRewriter.get(ent.getKey()).rewriteAST(modifiedDoc, null);
            edits.apply(modifiedDoc);
            modifiedContentByVariant = modifiedDoc.get();
            String contentBefLast = rewriterContentBefLast.get(ent.getKey());
            if(contentBefLast == null)
                contentBefLast = originalContent;
            String oneGraph= computeOneDiffGraph(contentBefLast, modifiedContentByVariant, graphID);
            graphsText += oneGraph;
            graphID++;
            // if source file on disk is not modified (complied on memory), we do this
            if(!RepairOptions.chosenTestInvokerInstance().modifiedSourceFileOnDisk())
                modifiedRewriterContent.put(ent.getKey(), modifiedContentByVariant);
        }
        //now graphsText contains the graph of the diff of variant with full gene and variant without last gene
        return graphsText;
    }

    private static String computeOneDiffGraph(String originalContent, String modifiedContent, int graphID) throws IOException {
        DiffTemplates diff = new DiffTemplates();
        MyJdtTreeGenerator stg = new MyJdtTreeGenerator();
        MyJdtTreeGenerator dtg = new MyJdtTreeGenerator();
        //System.out.println(originalContent);
        //System.out.println(modifiedContent);
        TreeContext stc= stg.generateFromString(originalContent);
        TreeContext dtc= dtg.generateFromString(modifiedContent);
        Matcher m=Matchers.getInstance().getMatcher(stc.getRoot(), dtc.getRoot());
        m.match();
        ActionGenerator g = new ActionGenerator(stc.getRoot(), dtc.getRoot(), m.getMappings());
        g.generate();
        List<Action> actions = g.getActions();
        if(actions.isEmpty())
            return "";

        diff.gatherAllInfor(actions, stg, dtg);
        return ActionHandling.serializeDiff2GSpanGraphString(actions, graphID, "", diff);
    }

    public static HashMap<String, String> getModifiedRewriterContent() throws BadLocationException {
        if(modifiedRewriterContent.isEmpty()) {
            if (modifiedRewriter.size() == 0)
                throw new RuntimeException("Attempting to get content of empty rewriters!");

            Iterator<Map.Entry<String, ASTRewrite>> iter = modifiedRewriter.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, ASTRewrite> ent = iter.next();
                //ASTRewrite originalRewriter = mapOriginalRewriter.get(ent.getKey());//key is fileName
                String originalContent = JavaParser.modifyingFiles().get(ent.getKey());
                //System.out.println(JavaParser.modifyingFiles());
                org.eclipse.jface.text.Document modifiedDoc = new org.eclipse.jface.text.Document(originalContent);
                TextEdit edits = modifiedRewriter.get(ent.getKey()).rewriteAST(modifiedDoc, null);
                edits.apply(modifiedDoc);
                String modifiedContent = modifiedDoc.get();
                modifiedRewriterContent.put(ent.getKey(), modifiedContent);
            }
        }
        return modifiedRewriterContent;
    }

    public static Boolean applyAllEditsOnModifiedRewriters(GenProgIndividual candidate){
        if(candidate.getHasValidGenome()) {
            Iterator<Map.Entry<String, ASTRewrite>> iter = modifiedRewriter.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, ASTRewrite> ent = iter.next();
                ASTRewriteUtils.applyEdits(ent.getValue(), ent.getKey());
            }
            //writeBackOriginalStateOfModifiedFiles();
        }else{
            candidate.setTestScore(0.0);
            candidate.setGenerScore(0.0);
        }

        clearAllModifiedRewriters();
        return candidate.getHasValidGenome();
    }

    public static void writeBackOriginalStateOfModifiedFiles(){
        if(RepairOptions.chosenTestInvokerInstance().modifiedSourceFileOnDisk()) {
            Iterator<Map.Entry<String, ASTRewrite>> iter = modifiedRewriter.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, ASTRewrite> ent = iter.next();
                CompilationUnit cu = JavaParser.getCompilationUnit(ent.getKey()); // key is fileName
                ASTRewrite originalFile = ASTRewrite.create(cu.getAST());
                ASTRewriteUtils.applyEdits(originalFile, ent.getKey());
            }
        }
    }

    public static void assertEmptyModifiedRewriter(){
        if(!modifiedRewriter.isEmpty() || !modifiedRewriterContent.isEmpty())
            throw new RuntimeException("Modified Rewriter should be empty when starting to write a variant!");
    }

    public static void clearAllModifiedRewriters(){
        modifiedRewriter.clear();
        modifiedRewriterContent.clear();
    }

    private static String standardize(String fileName){
        return fileName;
    }
}
