#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;

// Routes for the URL root
@Prefix("/")
public class AppRouter extends Router
{    
    public void before()
    {
    }
    
    public void after()
    {
    }
    
    // Match requests for root
    @Any("/")
    public void index()
    {
        // Encode the index view
        encode("index");
    }
}
