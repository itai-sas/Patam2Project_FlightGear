package model;

import utils.Properties;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class FGPlayer
{
    String ip;
    int port,hertzRate;
    Socket fg;
    PrintWriter out;

    public FGPlayer(Properties p)
    {
        ip=p.getIp();
        port=p.getPort();
        hertzRate=p.getHertzRate();
        try
        {
            fg=new Socket(ip,port);
            out = new PrintWriter(fg.getOutputStream());
        }
        catch (IOException e)
        { }

    }

    public void send(ArrayList<Float> row)
    {
        if(out!=null)
        {
            out.println(row);
            out.flush();
        }
    }

    public void close()
    {
        if(fg!=null)
        {
            try
            {
                fg.close();
            }
            catch (IOException e)
            { }
        }
        if(out!=null)
        {
            out.close();
        }
    }
}
