#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.intrbiz.balsa.BalsaApplication;

public class App extends BalsaApplication
{
    @Override
    protected void setup()
    {
        // Setup the application routers
        router(new AppRouter());
    }
    
    public static void main(String[] args) throws Exception
    {
        App app = new App();
        app.start();
    }
}
