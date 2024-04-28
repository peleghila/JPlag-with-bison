package de.jplag.bison;

import de.jplag.Language;
import de.jplag.ParsingException;
import de.jplag.Token;
//import de.jplag.cpp2.CPPParserAdapter;
import de.jplag.cpp.Scanner;
import org.kohsuke.MetaInfServices;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@MetaInfServices(Language.class)
public class BisonLanguage implements Language {
    private static final String IDENTIFIER = "bison-cpp";
    private final BisonParserAdapter parser;
    private final Scanner cppScanner;

    public BisonLanguage() {
        parser = new BisonParserAdapter();
        cppScanner = new Scanner();
    }

    @Override
    public String[] suffixes() {
        //return new String[] {".ypp"};
        return new String[] {".ypp",".cpp", ".CPP", ".cxx", ".CXX", ".c++", ".C++", ".c", ".C", ".cc", ".CC", ".h", ".H", ".hpp", ".HPP", ".hh", ".HH"};
    }

    @Override
    public String getName() {
        return "C/C++ Scanner [basic markup]";
    } // for viewer
    //public String getName() { return "Text Parser (naive)"; } //for viewer
    //public String getName() { return "Bison/C++ parser"; }

    @Override
    public String getIdentifier() { return IDENTIFIER; }

    @Override
    public int minimumTokenMatch() {
        return 12;
    }

    @Override
    public List<Token> parse(Set<File> files) throws ParsingException {
        List<Token> res = new ArrayList<>();
        res.addAll(this.parser.scan(files.stream().filter(f -> f.getName().endsWith(".ypp")).collect(Collectors.toSet())));
        res.addAll(this.cppScanner.scan(files.stream().filter(f -> !f.getName().endsWith(".ypp")).collect(Collectors.toSet())));
        return res;
    }
}
