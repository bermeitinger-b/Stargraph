package net.stargraph.core.query.agnostic;

import net.stargraph.StarGraphException;
import net.stargraph.core.query.nli.DataModelBinding;
import net.stargraph.core.query.nli.QueryPlanPattern;
import net.stargraph.core.query.nli.QueryType;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public final class SchemaAgnosticSPARQL {
    private QueryType queryType;
    private QueryPlanPattern triplePatterns;
    private List<DataModelBinding> bindings;
    private String saQuery;

    public SchemaAgnosticSPARQL(QueryType queryType, QueryPlanPattern triplePatterns, List<DataModelBinding> bindings) {
        this.queryType = Objects.requireNonNull(queryType);
        this.triplePatterns = Objects.requireNonNull(triplePatterns);
        this.bindings = Objects.requireNonNull(bindings);
        this.saQuery = createSAQueryString();
    }

    @Override
    public String toString() {
        return saQuery;
    }

    private String createSAQueryString() {
        switch (queryType) {
            case SELECT:
                return String.format("SELECT * WHERE {\n %s \n}", buildStatements());
            case ASK:
                return String.format("ASK {\n %s \n}", buildStatements());
            case AGGREGATE:
                throw new StarGraphException("TBD");
        }

        throw new StarGraphException("Unexpected: " + queryType);
    }

    private String buildStatements() {
        StringJoiner tripleJoiner = new StringJoiner(" . \n", "{ ", " }");

        triplePatterns.forEach(triplePattern -> {
            StringJoiner stmtJoiner = new StringJoiner(" ");
            for (String placeHolder : triplePattern.split("\\s")) {
                if (!isVar(placeHolder)) {
                    DataModelBinding binding = bindings.stream()
                            .filter(b -> b.getPlaceHolder().equals(placeHolder))
                            .findFirst()
                            .orElseThrow(() -> new StarGraphException("Unbounded '" + placeHolder + "'"));

                    stmtJoiner.add(getURI(binding));
                }
                else {
                    stmtJoiner.add(placeHolder);
                }
            }
            tripleJoiner.add(stmtJoiner.toString());
        });

        return tripleJoiner.toString();
    }

    private boolean isVar(String s) {
        return s.startsWith("?VAR");
    }

    private String getURI(DataModelBinding binding) {
        return String.format(":%s", binding.getTerm().replaceAll("\\s", "_"));
    }
}