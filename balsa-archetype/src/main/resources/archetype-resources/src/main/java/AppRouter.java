#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Template;

// Routes for the URL root
@Prefix("/")
@Template("layout/main")
public class AppRouter extends Router
{
    @Any("/")
    public void index()
    {
        // Encode the index view
        encode("index");
    }
}
