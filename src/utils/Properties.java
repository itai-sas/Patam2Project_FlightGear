package utils;

import java.beans.XMLEncoder;
import java.io.*;
import java.util.*;

public class Properties implements Serializable
{
    private String ip;
    private Integer port;
    private Integer hertzRate;
    private HashMap<String,FeatureProperties> map;
    private String regularFlightCSV;

    public Properties()
    {
        map = new HashMap<>();
    }

    public void createXML()
    {
        XMLEncoder e = null;
        try
        {
            e = new XMLEncoder(
                    new BufferedOutputStream(
                            new FileOutputStream("./resources/properties.xml")));
            e.writeObject(this);

        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
        e.close();

    }

    public String getIp()
    {
        return ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public Integer getPort()
    {
        return port;
    }

    public void setPort(Integer port)
    {
        this.port = port;
    }

    public Integer getHertzRate()
    { return hertzRate; }

    public void setHertzRate(Integer hertzRate)
    {
        this.hertzRate = hertzRate;
    }

    public HashMap<String, FeatureProperties> getMap()
    {
        return map;
    }

    public void setMap(HashMap<String, FeatureProperties> map)
    {
        this.map = map;
    }

    public String getRegularFlightCSV()
    { return regularFlightCSV; }

    public void setRegularFlightCSV(String regularFlightCSV) { this.regularFlightCSV = regularFlightCSV; }

    public void setDefaultProperties(){
        ip="127.0.0.1";
        port=5400;
        hertzRate=10;
        regularFlightCSV="./resources/reg_flight.csv";
        //Gps
        map.put("latitude", new Properties.FeatureProperties("latitude-deg",14, (float) -90, 90F));
        map.put("longitude", new Properties.FeatureProperties("longitude-deg",15, (float) -180, 180F));
        map.put("altitude", new Properties.FeatureProperties("altimeter_indicated-altitude-ft",25, (float) -1412,Float.MAX_VALUE)); //-1412ft is the lowest place on Earth(Dead-Sea)

        map.put("airspeed", new Properties.FeatureProperties("airspeed-indicator_indicated-speed-kt",24, (float) -1,Float.MAX_VALUE));
        map.put("heading", new Properties.FeatureProperties("indicated-heading-deg",36, (float) 0, 359F));
        map.put("roll", new Properties.FeatureProperties("attitude-indicator_indicated-roll-deg",28, (float) -90, 90F));
        map.put("pitch", new Properties.FeatureProperties("attitude-indicator_indicated-pitch-deg",27, (float) -90, 90F));
        map.put("yaw", new Properties.FeatureProperties("side-slip-deg",20, (float) -90, 90F));

        map.put("throttle", new Properties.FeatureProperties("throttle",6, (float) 0, 1F));

        map.put("aileron", new Properties.FeatureProperties("aileron",0, (float) -1, 1F));
        map.put("elevators", new Properties.FeatureProperties("elevator",1, (float) -1, 1F));
        map.put("rudder", new Properties.FeatureProperties("rudder",2, (float) -1, 1F));
    }

    public boolean isValidProperties()
    {
        if(ip==null||port==null||hertzRate==null||map==null)
            return false;

        if(!validateIp(ip)||port<1024||port>65535||hertzRate<1)
            return false;
        ArrayList<FeatureProperties> fp = new ArrayList<>();
        for(Map.Entry<String,FeatureProperties> e : map.entrySet())
            fp.add(e.getValue());
        if(!isValidRegFlightCSV())
            return false;
        return true;
    }

    private boolean isValidRegFlightCSV()
    {
        HashSet<String> set = new HashSet<>();

        for(Properties.FeatureProperties fp : map.values()){
            set.add(fp.getColChosenName());
        }

        Scanner scanner = null;
        try
        {
            scanner = new Scanner(new BufferedReader(new FileReader(regularFlightCSV)));
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        String line = scanner.nextLine();
        String[] features= line.split(",");
        for(String feature: features){
            if(set.contains(feature)){
                set.remove(feature);
            }
        }

        if(set.size()!=0)
            return false;

        while(scanner.hasNext())
        {
            features = scanner.next().split(",");
            for(String f : features){
                try{
                    Double.parseDouble(f);
                }
                catch (NumberFormatException e){
                    return false;
                }
            }
        }
        scanner.close();

        return true;
    }

    private static boolean validateIp(final String ip)
    {
        String Pattern = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(Pattern);
    }

    public static class FeatureProperties implements Serializable
    {
        String colChosenName;
        Integer index;
        Float minVal, maxVal;

        public FeatureProperties() { }

        public FeatureProperties(String name, Integer index, Float minVal, Float maxVal) {
            this.colChosenName = name;
            this.index = index;
            this.minVal = minVal;
            this.maxVal = maxVal;
        }

        public Integer getIndex()
        { return index; }

        public void setIndex(Integer index) { this.index = index; }

        public Float getMinVal()
        {
            return minVal;
        }

        public void setMinVal(Float minVal)
        {
            this.minVal = minVal;
        }

        public Float getMaxVal()
        {
            return maxVal;
        }

        public void setMaxVal(Float maxVal)
        {
            this.maxVal = maxVal;
        }

        public String getColChosenName()
        { return colChosenName; }

        public void setColChosenName(String colChosenName)
        { this.colChosenName = colChosenName; }

    }
}
