package app;

import crawler.SemanticCrawler;
import impl.SemanticCrawlerImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class App
{
    public static void main(String[] args) throws Exception
    {
        Model model = ModelFactory.createDefaultModel();
        
        SemanticCrawler crawler = new SemanticCrawlerImpl();
        crawler.search(model, "http://dbpedia.org/resource/Zico");
        
        model.write(System.out);
    }
}
