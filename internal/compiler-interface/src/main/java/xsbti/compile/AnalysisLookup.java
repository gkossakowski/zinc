package xsbti.compile;


import xsbti.Maybe;

import java.io.File;

public interface AnalysisLookup {
    Maybe<CompileAnalysis> lookupAnalysis(File classFile);
    Maybe<CompileAnalysis> lookupAnalysis(File binaryDependency, String binaryClassName);
    Maybe<CompileAnalysis> lookupAnalysis(String binaryClassName);
}
