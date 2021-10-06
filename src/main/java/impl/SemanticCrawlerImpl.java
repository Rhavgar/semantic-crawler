package impl;

import crawler.SemanticCrawler;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;

public class SemanticCrawlerImpl implements SemanticCrawler
{
    public List<String> uriIgnore;
    
    public SemanticCrawlerImpl()
    {
        this.uriIgnore = new ArrayList<>();
    }
    
    private boolean uriVerify(String uri)
    {
        return uriIgnore.contains(uri);
    }
    
    private String uriGet(Resource origin, Statement stmt)
    {
        if(stmt.getSubject().equals(origin))
        {
            return stmt.getObject().asNode().getURI();
        }
        
        return stmt.getSubject().getURI();
    }
    
    private boolean SameAs(Property property)
    {
        return property.getURI().equals(OWL.sameAs.getURI());
    }
    
    private void statementGet(Model write, Model read, Resource subject, List<Resource> blank)
    {
        StmtIterator stmts = read.listStatements();
        while(stmts.hasNext())
        {
            Statement stmt = stmts.next();
            Resource stmtSub = stmt.getSubject();
            RDFNode stmtObj = stmt.getObject();
            
            if(stmtSub.equals(subject))
            {
                write.add(stmt);
                if(stmtObj.isAnon() && !blank.contains(stmtObj.asResource()))
                {
                    blank.add(stmtObj.asResource());
                    statementGet(write, read, stmtObj.asResource(), blank);
                }
            }
        }
    }
    
    @Override
    public void search(Model graph, String uri)
    {
        this.uriIgnore.add(uri);
        
        CharsetEncoder encoder = Charset.forName("ISO-8859-1").newEncoder();
        if(encoder.canEncode(uri))
        {
            List<Resource> blank = new ArrayList<Resource>();
            
            Model model = ModelFactory.createDefaultModel();
            model.read(uri);
            Resource resource = model.getResource(uri);
            
            statementGet(graph, model, resource, blank);
            
            StmtIterator stmts = model.listStatements();
            while(stmts.hasNext())
            {
                Statement stmt = stmts.next();
                Resource stmtSub = stmt.getSubject();
                RDFNode stmtObj = stmt.getObject();
                Property stmtPrp = stmt.getPredicate();
                
                if(SameAs(stmtPrp) && (stmtSub.equals(resource) || stmtObj.asResource().equals(resource)))
                {
                    String uriNext = uriGet(resource, stmt);
                    
                    try
                    {
                        if(!uriVerify(uriNext))
                        {
                            search(graph, uriNext);
                        }
                    }
                    catch(Exception e)
                        {
                            e.equals(null);
                        }
                }
            }
        }
        
        else return;
    }
}
