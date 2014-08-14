#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.error.view.BalsaViewNotFound;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Status;
import com.intrbiz.metadata.Template;

// Routes for the URL root
@Prefix("/")
@Template("layout/main")
public class AppRouter extends Router<App>
{
    @Any("/")
    public void index()
    {
        // Encode the index view
        encode("index");
    }
       
    @Any("/**")
    @Catch(BalsaNotFound.class)
    @Status(HTTPStatus.NotFound)
    public void notFound()
    {
        // Encode the 404 view
        encode("notfound");
    }
    
    @Any("/**")
    @Catch()
    @Order(Order.LAST)
    @Status(HTTPStatus.InternalServerError)
    public void error()
    {
        // Log the internal error
        System.err.println("Internal server error whilst processing request");
        if (balsa().getException() != null)
            balsa().getException().printStackTrace();
        // Encode the error view
        encode("error");
    }
}
