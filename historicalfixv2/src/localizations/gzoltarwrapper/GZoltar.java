package localizations.gzoltarwrapper;

/**
 * Created by xledinh on 9/14/15.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.gzoltar.core.agent.AgentCreator;
import com.gzoltar.core.components.Component;
import com.gzoltar.core.components.Statement;
import com.gzoltar.core.diag.SFL;
import com.gzoltar.core.exec.parameters.ClassParameters;
import com.gzoltar.core.exec.parameters.TestParameters;
import com.gzoltar.core.instr.message.Response;
import com.gzoltar.core.instr.testing.TestResult;
import com.gzoltar.core.spectra.Spectra;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class GZoltar {
    private File agent = null;
    private String workingDirectory;
    private ClassParameters classParameters;
    private TestParameters testParameters;
    private ArrayList<String> classpaths;
    private Spectra spectra;

    public GZoltar(String wD) throws FileNotFoundException, IOException {
        this.workingDirectory = wD;
        this.classParameters = new ClassParameters();
        this.testParameters = new TestParameters();
        this.classpaths = new ArrayList();
        this.agent = AgentCreator.extract(new String[]{"com/gzoltar/core/components", "com/gzoltar/core/instr", "com/gzoltar/core/exec", "junit", "org/junit", "org/hamcrest", "org/objectweb/asm", "com/google/common"});
    }

    public void run() {
        Response r = this.launchAgent();
        this.spectra = new Spectra();
        this.spectra.registerResults(r.getTestResults());
        SFL.sfl(this.spectra);
    }

    public static void main(String[] args) throws IOException {
    }

    public List<TestResult> getTestResults() {
        return this.spectra.getTestResults();
    }

    public void addClassToInstrument(String name) {
        this.classParameters.addClassToInstrument(name);
    }

    public void addPackageToInstrument(String name) {
        this.classParameters.addPackageToInstrument(name);
    }

    public void addClassNotToInstrument(String name) {
        this.classParameters.addClassNotToInstrument(name);
    }

    public void addPackageNotToInstrument(String name) {
        this.classParameters.addPackageNotToInstrument(name);
    }

    public void addTestToExecute(String name) {
        this.testParameters.addTestToExecute(name);
    }

    public void addTestPackageToExecute(String name) {
        this.testParameters.addTestPackageToExecute(name);
    }

    public void addTestNotToExecute(String name) {
        this.testParameters.addTestNotToExecute(name);
    }

    public void addTestPackageNotToExecute(String name) {
        this.testParameters.addTestPackageNotToExecute(name);
    }

    public ClassParameters getClassParameters() {
        return this.classParameters;
    }

    public void setClassParameters(ClassParameters classParameters) {
        this.classParameters = classParameters;
    }

    public TestParameters getTestParameters() {
        return this.testParameters;
    }

    public void setTestParameters(TestParameters testParameters) {
        this.testParameters = testParameters;
    }

    public String getWorkingDirectory() {
        return this.workingDirectory;
    }

    public void setWorkingDirectory(String wD) {
        this.workingDirectory = wD;
    }

    public ArrayList<String> getClasspaths() {
        return this.classpaths;
    }

    public void setClassPaths(ArrayList<String> cPs) {
        this.classpaths.addAll(cPs);
    }

    public Spectra getSpectra() {
        return this.spectra;
    }

    public List<Component> getSuspiciousComponents() {
        return this.spectra.getComponents();
    }

    public List<Statement> getSuspiciousStatements() {
        List allComponents = this.spectra.getComponents();
        ArrayList statements = new ArrayList();
        Iterator i = allComponents.iterator();

        while(i.hasNext()) {
            Component c = (Component)i.next();
            if(c instanceof Statement) {
                statements.add((Statement)c);
            }
        }

        return statements;
    }

    private Response launchAgent() {
        return this.agent != null? localizations.gzoltarwrapper.Launcher.launch(this, this.agent):null;
    }
}

