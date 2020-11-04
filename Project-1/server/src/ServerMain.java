import owm.ConnectToOWM;
import utils.MessageProtocol;

public class ServerMain
{
    public static void main(String[] args)
    {
        Server server = new Server(Server.DEFAULT_SERVER_PORT);
//        ConnectToOWM connectToOWM = new ConnectToOWM();
//        connectToOWM.getCityWeatherMap("" + 833, "clouds_new");
    }
}