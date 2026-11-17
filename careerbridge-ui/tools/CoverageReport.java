import java.io.File;
import org.jacoco.core.analysis.*;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.*;
import org.jacoco.report.html.HTMLFormatter;

/** Turns jacoco.exec (written while the tests ran) into an HTML report and a short text summary. */
public class CoverageReport {
    public static void main(String[] a) throws Exception {
        File exec = new File(a[0]), classes = new File(a[1]), src = new File(a[2]), out = new File(a[3]);
        ExecFileLoader loader = new ExecFileLoader();
        loader.load(exec);
        CoverageBuilder cb = new CoverageBuilder();
        new Analyzer(loader.getExecutionDataStore(), cb).analyzeAll(classes);
        IBundleCoverage bundle = cb.getBundle("CareerBridge backend");

        IReportVisitor html = new HTMLFormatter().createVisitor(new FileMultiReportOutput(new File(out, "html")));
        html.visitInfo(loader.getSessionInfoStore().getInfos(), loader.getExecutionDataStore().getContents());
        html.visitBundle(bundle, new DirectorySourceFileLocator(src, "utf-8", 4));
        html.visitEnd();

        StringBuilder s = new StringBuilder("Code coverage (JaCoCo) - tests run against backend/src/main\n\n");
        s.append(String.format("%-48s %8s %8s %8s%n", "Class", "Line %", "Branch %", "Method %"));
        for (IPackageCoverage p : bundle.getPackages())
            for (IClassCoverage c : p.getClasses())
                s.append(String.format("%-48s %8s %8s %8s%n", c.getName().replace('/', '.').replace("com.careerbridge.", ""),
                        pct(c.getLineCounter()), pct(c.getBranchCounter()), pct(c.getMethodCounter())));
        s.append(String.format("%n%-48s %8s %8s %8s%n", "TOTAL", pct(bundle.getLineCounter()), pct(bundle.getBranchCounter()), pct(bundle.getMethodCounter())));
        java.nio.file.Files.writeString(new File(out, "coverage-summary.txt").toPath(), s.toString());
        System.out.println(s);
    }

    static String pct(ICounter c) { return c.getTotalCount() == 0 ? "-" : Math.round(c.getCoveredRatio() * 100) + "%"; }
}
