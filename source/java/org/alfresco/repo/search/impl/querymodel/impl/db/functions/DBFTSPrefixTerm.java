package org.alfresco.repo.search.impl.querymodel.impl.db.functions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.search.adaptor.lucene.AnalysisMode;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQuery;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderJoinCommand;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderPredicatePartCommand;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderPredicatePartCommandType;
import org.alfresco.repo.search.impl.querymodel.impl.db.PropertySupport;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSPrefixTerm;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Andy
 */
public class DBFTSPrefixTerm extends FTSPrefixTerm implements DBQueryBuilderComponent
{
    DBQueryBuilderComponent builderSupport;

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#isSupported()
     */
    @Override
    public boolean isSupported()
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#prepare(org.alfresco.service.namespace
     * .NamespaceService, org.alfresco.service.cmr.dictionary.DictionaryService,
     * org.alfresco.repo.domain.qname.QNameDAO, org.alfresco.repo.domain.node.NodeDAO, java.util.Set, java.util.Map,
     * org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext)
     */
    @Override
    public void prepare(NamespaceService namespaceService, DictionaryService dictionaryService, QNameDAO qnameDAO, NodeDAO nodeDAO, TenantService tenantService, Set<String> selectors,
            Map<String, Argument> functionArgs, FunctionEvaluationContext functionContext, boolean supportBooleanFloatAndDouble)
    {
        Argument argument = functionArgs.get(ARG_TERM);
        String term = (String) argument.getValue(functionContext);
        // strip trailing wildcard *
        term = term.substring(0, term.length() - 1);
        PropertyArgument propertyArgument = (PropertyArgument) functionArgs.get(ARG_PROPERTY);

        argument = functionArgs.get(ARG_TOKENISATION_MODE);
        AnalysisMode mode = (AnalysisMode) argument.getValue(functionContext);
        if (mode != AnalysisMode.IDENTIFIER)
        {
            throw new QueryModelException("Analysis mode not supported for DB " + mode);
        }

        PropertySupport propertySupport = new PropertySupport();
        propertySupport.setValue(term + "%");

        QName propertyQName = QName.createQName(DBQuery.expandQName(functionContext.getAlfrescoPropertyName(propertyArgument.getPropertyName()), namespaceService));
        propertySupport.setPropertyQName(propertyQName);
        propertySupport.setPropertyDataType(DBQuery.getDataTypeDefinition(dictionaryService, propertyQName));
        propertySupport.setPair(qnameDAO.getQName(propertyQName));
        propertySupport.setJoinCommandType(DBQuery.getJoinCommandType(propertyQName));
        propertySupport.setFieldName(DBQuery.getFieldName(dictionaryService, propertyQName, supportBooleanFloatAndDouble));
        propertySupport.setCommandType(DBQueryBuilderPredicatePartCommandType.LIKE);
        builderSupport = propertySupport;

    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildJoins(java.util.Map,
     * java.util.List)
     */
    @Override
    public void buildJoins(Map<QName, DBQueryBuilderJoinCommand> singleJoins, List<DBQueryBuilderJoinCommand> multiJoins)
    {
        builderSupport.buildJoins(singleJoins, multiJoins);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildPredicateCommands(java.util.List)
     */
    @Override
    public void buildPredicateCommands(List<DBQueryBuilderPredicatePartCommand> predicatePartCommands)
    {
        builderSupport.buildPredicateCommands(predicatePartCommands);
    }

}
