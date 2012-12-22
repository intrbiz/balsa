#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.intrbiz.balsa.BalsaApplication;

public class App extends BalsaApplication
{
    public App()
    {
        super();
    }

    @Override
    protected void setup()
    {
        // Setup the application routers
        router(new AppRouter());
        // Set the applications template view
        template("layout/main");
    }
}
