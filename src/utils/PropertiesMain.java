package utils;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class PropertiesMain
{
    public static void main(String[] args)
    {
        Properties p = new Properties();
        p.setDefaultProperties();
        p.createXML();
        XMLDecoder d = null;
        try
        {
            d = new XMLDecoder(new BufferedInputStream(new FileInputStream("./Sources/properties.xml")));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        Properties result = (Properties) d.readObject();
        d.close();
    }
}
